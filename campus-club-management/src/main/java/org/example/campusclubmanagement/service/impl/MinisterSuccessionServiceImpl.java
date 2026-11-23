package org.example.campusclubmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.dto.MinisterSuccessionCreateDTO;
import org.example.campusclubmanagement.dto.MinisterSuccessionReviewDTO;
import org.example.campusclubmanagement.dto.MinisterSuccessionVO;
import org.example.campusclubmanagement.dto.UserVO;
import org.example.campusclubmanagement.entity.Club;
import org.example.campusclubmanagement.entity.ClubMember;
import org.example.campusclubmanagement.entity.Department;
import org.example.campusclubmanagement.entity.MinisterSuccession;
import org.example.campusclubmanagement.entity.Role;
import org.example.campusclubmanagement.entity.User;
import org.example.campusclubmanagement.entity.UserRole;
import org.example.campusclubmanagement.exception.BusinessException;
import org.example.campusclubmanagement.mapper.ClubMapper;
import org.example.campusclubmanagement.mapper.ClubMemberMapper;
import org.example.campusclubmanagement.mapper.DepartmentMapper;
import org.example.campusclubmanagement.mapper.MinisterSuccessionMapper;
import org.example.campusclubmanagement.mapper.RoleMapper;
import org.example.campusclubmanagement.mapper.UserMapper;
import org.example.campusclubmanagement.mapper.UserRoleMapper;
import org.example.campusclubmanagement.service.MinisterSuccessionService;
import org.example.campusclubmanagement.util.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 部长卸任推荐服务实现
 */
@Service
@RequiredArgsConstructor
public class MinisterSuccessionServiceImpl implements MinisterSuccessionService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_REJECTED = 2;

    private final MinisterSuccessionMapper successionMapper;
    private final ClubMemberMapper clubMemberMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;
    private final ClubMapper clubMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    public List<UserVO> listCandidates(Long clubId) {
        checkMinisterMembership(clubId);
        List<ClubMember> members = clubMemberMapper.selectList(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getClubId, clubId)
                .eq(ClubMember::getPosition, "member")
                .eq(ClubMember::getStatus, 1));
        if (members.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> userIds = members.stream().map(ClubMember::getUserId).collect(Collectors.toSet());
        return userMapper.selectBatchIds(userIds).stream()
                .filter(user -> Integer.valueOf(1).equals(user.getStatus()))
                .map(user -> {
                    UserVO vo = new UserVO();
                    BeanUtils.copyProperties(user, vo);
                    return vo;
                })
                .toList();
    }

    @Override
    @Transactional
    public void create(MinisterSuccessionCreateDTO createDTO) {
        if (createDTO == null || createDTO.getClubId() == null || createDTO.getCandidateId() == null) {
            throw new BusinessException("社团和推荐成员不能为空");
        }
        String reason = trimToNull(createDTO.getReason());
        if (reason != null && reason.length() > 500) {
            throw new BusinessException("推荐理由不能超过500个字符");
        }

        Long currentUserId = getCurrentUserId();
        ClubMember ministerMember = checkMinisterMembership(createDTO.getClubId());
        ClubMember candidateMember = clubMemberMapper.selectOne(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getClubId, createDTO.getClubId())
                .eq(ClubMember::getUserId, createDTO.getCandidateId())
                .eq(ClubMember::getStatus, 1));
        if (candidateMember == null || !"member".equals(candidateMember.getPosition())) {
            throw new BusinessException("只能推荐本社团的在社普通成员");
        }
        User candidate = userMapper.selectById(createDTO.getCandidateId());
        if (candidate == null || !Integer.valueOf(1).equals(candidate.getStatus())) {
            throw new BusinessException("推荐成员不存在或已被禁用");
        }

        Long pendingCount = successionMapper.selectCount(new LambdaQueryWrapper<MinisterSuccession>()
                .eq(MinisterSuccession::getClubId, createDTO.getClubId())
                .eq(MinisterSuccession::getMinisterId, currentUserId)
                .eq(MinisterSuccession::getStatus, STATUS_PENDING));
        if (pendingCount > 0) {
            throw new BusinessException("您在该社团已有待审批的卸任申请");
        }
        Long candidatePendingCount = successionMapper.selectCount(new LambdaQueryWrapper<MinisterSuccession>()
                .eq(MinisterSuccession::getClubId, createDTO.getClubId())
                .eq(MinisterSuccession::getCandidateId, createDTO.getCandidateId())
                .eq(MinisterSuccession::getStatus, STATUS_PENDING));
        if (candidatePendingCount > 0) {
            throw new BusinessException("该成员已有待审批的接任推荐");
        }

        MinisterSuccession succession = new MinisterSuccession();
        succession.setClubId(createDTO.getClubId());
        succession.setDepartmentId(ministerMember.getDepartmentId());
        succession.setMinisterId(currentUserId);
        succession.setCandidateId(createDTO.getCandidateId());
        succession.setReason(reason);
        succession.setStatus(STATUS_PENDING);
        successionMapper.insert(succession);
    }

    @Override
    public List<MinisterSuccessionVO> listMine() {
        Long currentUserId = getCurrentUserId();
        checkRole(currentUserId, "MINISTER", "只有社团部长可以查看卸任申请");
        List<MinisterSuccession> records = successionMapper.selectList(new LambdaQueryWrapper<MinisterSuccession>()
                .eq(MinisterSuccession::getMinisterId, currentUserId)
                .orderByDesc(MinisterSuccession::getCreateTime));
        return toVOList(records);
    }

    @Override
    public List<MinisterSuccessionVO> listForAdmin(Integer status) {
        checkSuperAdmin();
        if (status != null && status != STATUS_PENDING && status != STATUS_APPROVED && status != STATUS_REJECTED) {
            throw new BusinessException("审批状态无效");
        }
        List<MinisterSuccession> records = successionMapper.selectList(new LambdaQueryWrapper<MinisterSuccession>()
                .eq(status != null, MinisterSuccession::getStatus, status)
                .orderByAsc(MinisterSuccession::getStatus)
                .orderByDesc(MinisterSuccession::getCreateTime));
        return toVOList(records);
    }

    @Override
    @Transactional
    public void review(Long id, MinisterSuccessionReviewDTO reviewDTO) {
        checkSuperAdmin();
        if (reviewDTO == null || reviewDTO.getApproved() == null) {
            throw new BusinessException("审批结果不能为空");
        }
        String reviewRemark = trimToNull(reviewDTO.getReviewRemark());
        if (reviewRemark != null && reviewRemark.length() > 500) {
            throw new BusinessException("审批意见不能超过500个字符");
        }

        MinisterSuccession succession = successionMapper.selectById(id);
        if (succession == null) {
            throw new BusinessException("卸任申请不存在");
        }
        if (!Integer.valueOf(STATUS_PENDING).equals(succession.getStatus())) {
            throw new BusinessException("该申请已处理，不能重复审批");
        }

        ClubMember ministerMember = null;
        ClubMember candidateMember = null;
        if (reviewDTO.getApproved()) {
            ministerMember = getActiveMember(succession.getClubId(), succession.getMinisterId());
            candidateMember = getActiveMember(succession.getClubId(), succession.getCandidateId());
            if (!"minister".equals(ministerMember.getPosition())) {
                throw new BusinessException("原部长职位已变更，无法通过该申请");
            }
            if (!"member".equals(candidateMember.getPosition())) {
                throw new BusinessException("推荐成员已不再是普通成员，无法通过该申请");
            }
        }

        int updated = successionMapper.update(null, new LambdaUpdateWrapper<MinisterSuccession>()
                .eq(MinisterSuccession::getId, id)
                .eq(MinisterSuccession::getStatus, STATUS_PENDING)
                .set(MinisterSuccession::getStatus,
                        reviewDTO.getApproved() ? STATUS_APPROVED : STATUS_REJECTED)
                .set(MinisterSuccession::getReviewerId, getCurrentUserId())
                .set(MinisterSuccession::getReviewRemark, reviewRemark)
                .set(MinisterSuccession::getReviewTime, LocalDateTime.now()));
        if (updated == 0) {
            throw new BusinessException("该申请已被其他管理员处理");
        }
        if (!reviewDTO.getApproved()) {
            return;
        }

        ministerMember.setPosition("member");
        clubMemberMapper.updateById(ministerMember);

        candidateMember.setPosition("minister");
        candidateMember.setDepartmentId(ministerMember.getDepartmentId());
        clubMemberMapper.updateById(candidateMember);

        ensureRole(succession.getCandidateId(), "MINISTER");
        ensureRole(succession.getMinisterId(), "MEMBER");
        Long remainingMinisterPositions = clubMemberMapper.selectCount(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getUserId, succession.getMinisterId())
                .eq(ClubMember::getPosition, "minister")
                .eq(ClubMember::getStatus, 1));
        if (remainingMinisterPositions == 0) {
            removeRole(succession.getMinisterId(), "MINISTER");
        }
        Long remainingMemberPositions = clubMemberMapper.selectCount(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getUserId, succession.getCandidateId())
                .eq(ClubMember::getPosition, "member")
                .eq(ClubMember::getStatus, 1));
        if (remainingMemberPositions == 0) {
            removeRole(succession.getCandidateId(), "MEMBER");
        }
    }

    private ClubMember checkMinisterMembership(Long clubId) {
        if (clubId == null) {
            throw new BusinessException("社团ID不能为空");
        }
        Long currentUserId = getCurrentUserId();
        checkRole(currentUserId, "MINISTER", "只有社团部长可以发起卸任推荐");
        ClubMember member = clubMemberMapper.selectOne(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getClubId, clubId)
                .eq(ClubMember::getUserId, currentUserId)
                .eq(ClubMember::getPosition, "minister")
                .eq(ClubMember::getStatus, 1));
        if (member == null) {
            throw new BusinessException(403, "您不是该社团的部长");
        }
        return member;
    }

    private ClubMember getActiveMember(Long clubId, Long userId) {
        ClubMember member = clubMemberMapper.selectOne(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getClubId, clubId)
                .eq(ClubMember::getUserId, userId)
                .eq(ClubMember::getStatus, 1));
        if (member == null) {
            throw new BusinessException("原部长或推荐成员已退出社团");
        }
        return member;
    }

    private void checkSuperAdmin() {
        checkRole(getCurrentUserId(), "SUPER_ADMIN", "只有超级管理员可以审批卸任申请");
    }

    private void checkRole(Long userId, String roleCode, String message) {
        List<String> roles = userRoleMapper.findRoleCodesByUserId(userId);
        if (roles == null || !roles.contains(roleCode)) {
            throw new BusinessException(403, message);
        }
    }

    private void ensureRole(Long userId, String roleCode) {
        Role role = getRole(roleCode);
        Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, role.getId()));
        if (count == 0) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            userRoleMapper.insert(userRole);
        }
    }

    private void removeRole(Long userId, String roleCode) {
        Role role = getRole(roleCode);
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, role.getId()));
    }

    private Role getRole(String roleCode) {
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, roleCode));
        if (role == null) {
            throw new BusinessException("系统角色配置不完整");
        }
        return role;
    }

    private List<MinisterSuccessionVO> toVOList(List<MinisterSuccession> records) {
        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> userIds = records.stream()
                .flatMap(record -> Stream.of(record.getMinisterId(), record.getCandidateId()))
                .collect(Collectors.toSet());
        Set<Long> clubIds = records.stream().map(MinisterSuccession::getClubId).collect(Collectors.toSet());
        Set<Long> departmentIds = records.stream().map(MinisterSuccession::getDepartmentId)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, User> users = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, Club> clubs = clubMapper.selectBatchIds(clubIds).stream()
                .collect(Collectors.toMap(Club::getId, Function.identity()));
        Map<Long, Department> departments = departmentIds.isEmpty() ? Collections.emptyMap() :
                departmentMapper.selectBatchIds(departmentIds).stream()
                        .collect(Collectors.toMap(Department::getId, Function.identity()));

        return records.stream().map(record -> {
            MinisterSuccessionVO vo = new MinisterSuccessionVO();
            BeanUtils.copyProperties(record, vo);
            User minister = users.get(record.getMinisterId());
            User candidate = users.get(record.getCandidateId());
            Club club = clubs.get(record.getClubId());
            Department department = departments.get(record.getDepartmentId());
            if (minister != null) {
                vo.setMinisterName(displayName(minister));
                vo.setMinisterUsername(minister.getUsername());
            }
            if (candidate != null) {
                vo.setCandidateName(displayName(candidate));
                vo.setCandidateUsername(candidate.getUsername());
            }
            if (club != null) {
                vo.setClubName(club.getClubName());
            }
            if (department != null) {
                vo.setDepartmentName(department.getDeptName());
            }
            return vo;
        }).toList();
    }

    private String displayName(User user) {
        if (user.getRealName() != null && !user.getRealName().isBlank()) {
            return user.getRealName();
        }
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        return user.getUsername();
    }

    private Long getCurrentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return userId;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
