package org.example.campusclubmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.common.PageResult;
import org.example.campusclubmanagement.dto.ActivityFundingApplicationCreateDTO;
import org.example.campusclubmanagement.dto.ActivityFundingApplicationQueryDTO;
import org.example.campusclubmanagement.dto.ActivityFundingApplicationVO;
import org.example.campusclubmanagement.entity.Activity;
import org.example.campusclubmanagement.entity.ActivityFundingApplication;
import org.example.campusclubmanagement.entity.ClubMember;
import org.example.campusclubmanagement.exception.BusinessException;
import org.example.campusclubmanagement.mapper.ActivityFundingApplicationMapper;
import org.example.campusclubmanagement.mapper.ActivityMapper;
import org.example.campusclubmanagement.mapper.ClubMemberMapper;
import org.example.campusclubmanagement.mapper.UserRoleMapper;
import org.example.campusclubmanagement.service.ActivityFundingApplicationService;
import org.example.campusclubmanagement.util.UserContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 活动经费申请服务实现
 */
@Service
@RequiredArgsConstructor
public class ActivityFundingApplicationServiceImpl implements ActivityFundingApplicationService {

    private final ActivityFundingApplicationMapper fundingApplicationMapper;
    private final ActivityMapper activityMapper;
    private final ClubMemberMapper clubMemberMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public void create(ActivityFundingApplicationCreateDTO createDTO) {
        Long currentUserId = getCurrentUserId();
        List<String> roles = userRoleMapper.findRoleCodesByUserId(currentUserId);
        if (roles != null && roles.contains("SUPER_ADMIN")) {
            throw new BusinessException(403, "超级管理员不能为自己申请活动经费");
        }
        validateApplication(createDTO);

        Activity activity = activityMapper.selectById(createDTO.getActivityId());
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        if (!Integer.valueOf(1).equals(activity.getApprovalStatus())) {
            throw new BusinessException("活动通过审批后才能申请经费");
        }
        ClubMember ministerMembership = clubMemberMapper.selectOne(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getClubId, activity.getClubId())
                .eq(ClubMember::getUserId, currentUserId)
                .eq(ClubMember::getPosition, "minister")
                .eq(ClubMember::getStatus, 1));
        if (ministerMembership == null) {
            throw new BusinessException(403, "只有负责社团的部长可以申请活动经费");
        }

        ActivityFundingApplication application = new ActivityFundingApplication();
        application.setActivityId(activity.getId());
        application.setClubId(activity.getClubId());
        application.setApplicantId(currentUserId);
        application.setAmount(createDTO.getAmount());
        application.setReason(createDTO.getReason().trim());
        fundingApplicationMapper.insert(application);
    }

    @Override
    public PageResult<ActivityFundingApplicationVO> pageForSuperAdmin(
            ActivityFundingApplicationQueryDTO queryDTO) {
        checkSuperAdmin();
        String keyword = queryDTO.getKeyword() == null ? null : queryDTO.getKeyword().trim();
        Page<ActivityFundingApplicationVO> page = fundingApplicationMapper.selectAdminPage(
                new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), keyword);
        return new PageResult<>(page.getTotal(), page.getPages(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    @Override
    public void delete(Long id) {
        checkSuperAdmin();
        ActivityFundingApplication application = fundingApplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException("经费申请不存在");
        }
        fundingApplicationMapper.deleteById(id);
    }

    private void validateApplication(ActivityFundingApplicationCreateDTO createDTO) {
        if (createDTO == null || createDTO.getActivityId() == null) {
            throw new BusinessException("申请活动不能为空");
        }
        BigDecimal amount = createDTO.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("申请金额必须大于0");
        }
        if (amount.scale() > 2 || amount.precision() - amount.scale() > 10) {
            throw new BusinessException("申请金额最多保留两位小数");
        }
        if (createDTO.getReason() == null || createDTO.getReason().isBlank()) {
            throw new BusinessException("经费用途说明不能为空");
        }
        if (createDTO.getReason().trim().length() > 500) {
            throw new BusinessException("经费用途说明不能超过500个字符");
        }
    }

    private void checkSuperAdmin() {
        List<String> roles = userRoleMapper.findRoleCodesByUserId(getCurrentUserId());
        if (roles == null || !roles.contains("SUPER_ADMIN")) {
            throw new BusinessException(403, "只有超级管理员可以使用经费申请中心");
        }
    }

    private Long getCurrentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }
}
