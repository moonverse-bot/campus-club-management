package org.example.campusclubmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.common.PageResult;
import org.example.campusclubmanagement.dto.UserQueryDTO;
import org.example.campusclubmanagement.dto.UserVO;
import org.example.campusclubmanagement.entity.ClubMember;
import org.example.campusclubmanagement.entity.User;
import org.example.campusclubmanagement.exception.BusinessException;
import org.example.campusclubmanagement.mapper.ClubMemberMapper;
import org.example.campusclubmanagement.mapper.UserMapper;
import org.example.campusclubmanagement.mapper.UserRoleMapper;
import org.example.campusclubmanagement.service.UserService;
import org.example.campusclubmanagement.util.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Map<String, Integer> ROLE_LEVELS = Map.of(
            "SUPER_ADMIN", 100,
            "ADMIN", 90,
            "PRESIDENT", 80,
            "VICE_PRESIDENT", 70,
            "MINISTER", 60,
            "VICE_MINISTER", 50,
            "MEMBER", 40,
            "STUDENT", 30
    );
    private static final int MIN_MANAGE_LEVEL = ROLE_LEVELS.get("VICE_MINISTER");

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final ClubMemberMapper clubMemberMapper;

    @Override
    public PageResult<UserVO> pageList(UserQueryDTO queryDTO) {
        List<String> currentRoles = getCurrentManagerRoles();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        applyDataScope(wrapper, currentRoles);

        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.and(w -> w
                    .like(User::getUsername, queryDTO.getKeyword())
                    .or()
                    .like(User::getNickname, queryDTO.getKeyword())
                    .or()
                    .like(User::getRealName, queryDTO.getKeyword()));
        }
        if (queryDTO.getCollege() != null && !queryDTO.getCollege().isEmpty()) {
            wrapper.like(User::getCollege, queryDTO.getCollege());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(User::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> page = userMapper.selectPage(
                new Page<>(queryDTO.getCurrent(), queryDTO.getSize()),
                wrapper
        );

        List<UserVO> records = page.getRecords().stream().map(this::toUserVO).toList();
        return new PageResult<>(page.getTotal(), page.getPages(), page.getCurrent(), page.getSize(), records);
    }

    @Override
    public UserVO getById(Long id) {
        if (!getCurrentUserId().equals(id)) {
            checkManagePermission(id);
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toUserVO(user);
    }

    @Override
    public void update(User user) {
        if (user.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (!getCurrentUserId().equals(user.getId())) {
            checkManagePermission(user.getId());
        }

        // 个人中心和用户管理共用此接口，只允许修改资料字段。
        user.setUsername(null);
        user.setPassword(null);
        user.setStatus(null);
        user.setLastLoginTime(null);
        user.setLastActiveTime(null);
        user.setCreateTime(null);
        user.setUpdateTime(null);
        user.setDeleted(null);
        userMapper.updateById(user);
    }

    @Override
    public void delete(Long id) {
        checkManagePermission(id);
        userMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("用户状态无效");
        }
        checkManagePermission(id);
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        List<String> roles = userRoleMapper.findRoleCodesByUserId(user.getId());
        vo.setRoles(roles != null ? roles : Collections.emptyList());
        vo.setOnline(Integer.valueOf(1).equals(user.getStatus())
                && user.getLastActiveTime() != null
                && user.getLastActiveTime().isAfter(LocalDateTime.now().minusMinutes(2)));
        return vo;
    }

    private void checkManagePermission(Long targetUserId) {
        Long currentUserId = getCurrentUserId();
        List<String> currentRoles = getCurrentManagerRoles();
        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException("用户不存在");
        }

        List<String> targetRoles = userRoleMapper.findRoleCodesByUserId(targetUserId);
        if (currentRoles.contains("SUPER_ADMIN") && (targetRoles == null || targetRoles.stream()
                .noneMatch(role -> role.equals("PRESIDENT") || role.equals("VICE_PRESIDENT")))) {
            throw new BusinessException(403, "超级管理员只能查看社长和副社长信息");
        }
        int currentLevel = getHighestRoleLevel(currentRoles);
        int targetLevel = getHighestRoleLevel(targetRoles);
        if (targetLevel >= currentLevel) {
            throw new BusinessException(403, "不能管理同级或上级用户");
        }
        if (!hasSystemScope(currentRoles) && !isSameClub(currentUserId, targetUserId)) {
            throw new BusinessException(403, "只能管理本社团内的成员");
        }
    }

    private void applyDataScope(LambdaQueryWrapper<User> wrapper, List<String> currentRoles) {
        String roleUserQuery = "SELECT ur.user_id FROM user_role ur " +
                "INNER JOIN role r ON ur.role_id = r.id AND r.deleted = 0 " +
                "WHERE r.role_code ";
        if (currentRoles.contains("SUPER_ADMIN")) {
            wrapper.inSql(User::getId, roleUserQuery + "IN ('PRESIDENT','VICE_PRESIDENT')");
            return;
        }

        int currentLevel = getHighestRoleLevel(currentRoles);
        List<String> manageableRoles = ROLE_LEVELS.entrySet().stream()
                .filter(entry -> entry.getValue() < currentLevel)
                .map(Map.Entry::getKey)
                .toList();
        String allowedRoles = toSqlStringList(manageableRoles);

        // 用户可拥有多个角色，只有其全部角色都低于当前角色时才可见。
        wrapper.inSql(User::getId, roleUserQuery + "IN (" + allowedRoles + ")");
        wrapper.notInSql(User::getId, roleUserQuery + "NOT IN (" + allowedRoles + ")");

        if (!hasSystemScope(currentRoles)) {
            List<Long> clubIds = getCurrentClubIds(getCurrentUserId());
            if (clubIds.isEmpty()) {
                wrapper.eq(User::getId, -1L);
            } else {
                String clubIdList = clubIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                wrapper.inSql(User::getId, "SELECT user_id FROM club_member " +
                        "WHERE deleted = 0 AND status = 1 AND club_id IN (" + clubIdList + ")");
            }
        }
    }

    private List<String> getCurrentManagerRoles() {
        List<String> roles = userRoleMapper.findRoleCodesByUserId(getCurrentUserId());
        if (getHighestRoleLevel(roles) < MIN_MANAGE_LEVEL) {
            throw new BusinessException(403, "当前角色没有用户管理权限");
        }
        return roles;
    }

    private int getHighestRoleLevel(Collection<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return 0;
        }
        if (roles.stream().anyMatch(role -> !ROLE_LEVELS.containsKey(role))) {
            throw new BusinessException(403, "用户角色信息无效");
        }
        return roles.stream().mapToInt(ROLE_LEVELS::get).max().orElse(0);
    }

    private boolean hasSystemScope(List<String> roles) {
        return roles.contains("SUPER_ADMIN") || roles.contains("ADMIN");
    }

    private boolean isSameClub(Long currentUserId, Long targetUserId) {
        List<Long> clubIds = getCurrentClubIds(currentUserId);
        if (clubIds.isEmpty()) {
            return false;
        }
        return clubMemberMapper.selectCount(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getUserId, targetUserId)
                .eq(ClubMember::getStatus, 1)
                .in(ClubMember::getClubId, clubIds)) > 0;
    }

    private List<Long> getCurrentClubIds(Long currentUserId) {
        return clubMemberMapper.selectList(new LambdaQueryWrapper<ClubMember>()
                        .select(ClubMember::getClubId)
                        .eq(ClubMember::getUserId, currentUserId)
                        .eq(ClubMember::getStatus, 1))
                .stream()
                .map(ClubMember::getClubId)
                .distinct()
                .toList();
    }

    private Long getCurrentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return userId;
    }

    private String toSqlStringList(List<String> values) {
        return values.stream().collect(Collectors.joining("','", "'", "'"));
    }
}
