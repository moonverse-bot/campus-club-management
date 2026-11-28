package org.example.campusclubmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.entity.Club;
import org.example.campusclubmanagement.entity.ClubMember;
import org.example.campusclubmanagement.exception.BusinessException;
import org.example.campusclubmanagement.mapper.ClubMapper;
import org.example.campusclubmanagement.mapper.ClubMemberMapper;
import org.example.campusclubmanagement.service.ClubMemberService;
import org.example.campusclubmanagement.service.PermissionChecker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 社团成员服务实现
 */
@Service
@RequiredArgsConstructor
public class ClubMemberServiceImpl implements ClubMemberService {

    private final ClubMemberMapper clubMemberMapper;
    private final ClubMapper clubMapper;
    private final PermissionChecker permissionChecker;

    @Override
    public List<ClubMember> listByClubId(Long clubId) {
        return clubMemberMapper.selectList(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getClubId, clubId)
                .eq(ClubMember::getStatus, 1)
                .orderByDesc(ClubMember::getJoinDate));
    }

    @Override
    public List<ClubMember> listByUserId(Long userId) {
        return clubMemberMapper.selectList(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getUserId, userId)
                .eq(ClubMember::getStatus, 1));
    }

    @Override
    public ClubMember getById(Long id) {
        ClubMember member = clubMemberMapper.selectById(id);
        if (member == null) {
            throw new BusinessException("成员不存在");
        }
        return member;
    }

    @Override
    public ClubMember getByClubAndUser(Long clubId, Long userId) {
        return clubMemberMapper.selectOne(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getClubId, clubId)
                .eq(ClubMember::getUserId, userId));
    }

    @Override
    @Transactional
    public void add(ClubMember member) {
        permissionChecker.checkClubManager(member.getClubId());
        // 非系统管理员只能添加普通成员，防止越权提拔
        if (!permissionChecker.isAdmin() && member.getPosition() != null
                && !"member".equals(member.getPosition())) {
            throw new BusinessException(403, "无权设置该成员职位");
        }
        // 检查是否已在社团中
        ClubMember exist = getByClubAndUser(member.getClubId(), member.getUserId());
        if (exist != null && exist.getStatus() == 1) {
            throw new BusinessException("该用户已是社团成员");
        }

        if (member.getPosition() == null) {
            member.setPosition("member");
        }
        if (member.getStatus() == null) {
            member.setStatus(1);
        }
        if (member.getJoinDate() == null) {
            member.setJoinDate(LocalDate.now());
        }

        if (exist != null) {
            // 重新加入
            member.setId(exist.getId());
            clubMemberMapper.updateById(member);
        } else {
            clubMemberMapper.insert(member);
        }

        // 更新社团成员数
        updateClubMemberCount(member.getClubId());
    }

    @Override
    public void update(ClubMember member) {
        if (member.getId() == null) {
            throw new BusinessException("成员ID不能为空");
        }
        ClubMember existing = clubMemberMapper.selectById(member.getId());
        if (existing == null) {
            throw new BusinessException("成员不存在");
        }
        permissionChecker.checkClubManager(existing.getClubId());
        // 非系统管理员不能调整职位，避免通过更新接口越权提拔
        if (!permissionChecker.isAdmin() && member.getPosition() != null
                && !member.getPosition().equals(existing.getPosition())) {
            throw new BusinessException(403, "无权修改成员职位");
        }
        // 成员归属不可通过更新接口变更
        member.setClubId(null);
        member.setUserId(null);
        clubMemberMapper.updateById(member);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ClubMember member = clubMemberMapper.selectById(id);
        if (member == null) {
            throw new BusinessException("成员不存在");
        }
        permissionChecker.checkClubManager(member.getClubId());
        clubMemberMapper.deleteById(id);
        updateClubMemberCount(member.getClubId());
    }

    @Override
    @Transactional
    public void quitClub(Long clubId, Long userId) {
        if (!permissionChecker.currentUserId().equals(userId)) {
            throw new BusinessException(403, "只能退出自己所在的社团");
        }
        ClubMember member = getByClubAndUser(clubId, userId);
        if (member == null) {
            throw new BusinessException("您不是该社团成员");
        }
        member.setStatus(0);
        clubMemberMapper.updateById(member);
        updateClubMemberCount(clubId);
    }

    private void updateClubMemberCount(Long clubId) {
        Long count = clubMemberMapper.selectCount(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getClubId, clubId)
                .eq(ClubMember::getStatus, 1));
        Club club = new Club();
        club.setId(clubId);
        club.setMemberCount(count.intValue());
        clubMapper.updateById(club);
    }
}
