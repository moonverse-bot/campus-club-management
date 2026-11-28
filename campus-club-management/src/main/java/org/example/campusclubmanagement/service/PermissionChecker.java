package org.example.campusclubmanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.entity.ClubMember;
import org.example.campusclubmanagement.exception.BusinessException;
import org.example.campusclubmanagement.mapper.ClubMemberMapper;
import org.example.campusclubmanagement.mapper.UserRoleMapper;
import org.example.campusclubmanagement.util.UserContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 通用权限检查组件，供各 Service 复用
 */
@Component
@RequiredArgsConstructor
public class PermissionChecker {

    private final UserRoleMapper userRoleMapper;
    private final ClubMemberMapper clubMemberMapper;

    private static final Set<String> CLUB_MANAGER_POSITIONS = Set.of("president", "vice_president", "minister");

    public Long currentUserId() {
        Long id = UserContext.getUserId();
        if (id == null) {
            throw new BusinessException(401, "未登录");
        }
        return id;
    }

    public List<String> currentRoleCodes() {
        return userRoleMapper.findRoleCodesByUserId(currentUserId());
    }

    public boolean isSuperAdmin() {
        return currentRoleCodes().contains("SUPER_ADMIN");
    }

    public boolean isAdmin() {
        List<String> roles = currentRoleCodes();
        return roles.contains("SUPER_ADMIN") || roles.contains("ADMIN");
    }

    public void checkSuperAdmin() {
        if (!isSuperAdmin()) {
            throw new BusinessException(403, "仅超级管理员可操作");
        }
    }

    public void checkAdmin() {
        if (!isAdmin()) {
            throw new BusinessException(403, "当前角色没有操作权限");
        }
    }

    /** 当前用户在指定社团的在职职位 */
    public String positionInClub(Long clubId) {
        if (clubId == null) {
            return null;
        }
        ClubMember member = clubMemberMapper.selectOne(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getClubId, clubId)
                .eq(ClubMember::getUserId, currentUserId())
                .eq(ClubMember::getStatus, 1));
        return member == null ? null : member.getPosition();
    }

    public boolean isClubManager(Long clubId) {
        String position = positionInClub(clubId);
        return position != null && CLUB_MANAGER_POSITIONS.contains(position);
    }

    public boolean isClubPresident(Long clubId) {
        String position = positionInClub(clubId);
        return "president".equals(position) || "vice_president".equals(position);
    }

    /** 系统管理员，或指定社团的社长/副社长/部长可操作 */
    public void checkClubManager(Long clubId) {
        if (isAdmin()) {
            return;
        }
        if (!isClubManager(clubId)) {
            throw new BusinessException(403, "当前角色没有操作权限");
        }
    }

    /** 系统管理员，或指定社团的社长/副社长可操作 */
    public void checkClubPresident(Long clubId) {
        if (isAdmin()) {
            return;
        }
        if (!isClubPresident(clubId)) {
            throw new BusinessException(403, "当前角色没有操作权限");
        }
    }
}
