package org.example.campusclubmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.common.PageResult;
import org.example.campusclubmanagement.dto.ActivityManageOptionDTO;
import org.example.campusclubmanagement.dto.ActivityQueryDTO;
import org.example.campusclubmanagement.dto.ActivityRegistrationVO;
import org.example.campusclubmanagement.dto.ActivityReviewDTO;
import org.example.campusclubmanagement.dto.ActivityVO;
import org.example.campusclubmanagement.entity.Activity;
import org.example.campusclubmanagement.entity.ActivityRegistration;
import org.example.campusclubmanagement.entity.Club;
import org.example.campusclubmanagement.entity.ClubMember;
import org.example.campusclubmanagement.entity.User;
import org.example.campusclubmanagement.exception.BusinessException;
import org.example.campusclubmanagement.mapper.ActivityMapper;
import org.example.campusclubmanagement.mapper.ActivityRegistrationMapper;
import org.example.campusclubmanagement.mapper.ClubMapper;
import org.example.campusclubmanagement.mapper.ClubMemberMapper;
import org.example.campusclubmanagement.mapper.UserMapper;
import org.example.campusclubmanagement.mapper.UserRoleMapper;
import org.example.campusclubmanagement.service.ActivityService;
import org.example.campusclubmanagement.util.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 活动服务实现
 */
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private static final int REGISTRATION_PENDING = 0;
    private static final int REGISTRATION_APPROVED = 1;
    private static final int REGISTRATION_REJECTED = 2;
    private static final int APPROVAL_PENDING = 0;
    private static final int APPROVAL_APPROVED = 1;
    private static final int APPROVAL_REJECTED = 2;

    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final ClubMemberMapper clubMemberMapper;
    private final UserRoleMapper userRoleMapper;
    private final ClubMapper clubMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<ActivityVO> pageList(ActivityQueryDTO queryDTO) {
        ManagementScope managementScope = getManagementScope();
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getClubId() != null) {
            wrapper.eq(Activity::getClubId, queryDTO.getClubId());
        }
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isBlank()) {
            wrapper.like(Activity::getTitle, queryDTO.getKeyword().trim());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Activity::getStatus, queryDTO.getStatus());
        }
        if ("approval".equals(queryDTO.getScope())) {
            checkSuperAdmin(managementScope);
            wrapper.eq(Activity::getApprovalStatus, APPROVAL_PENDING);
        } else if ("managed".equals(queryDTO.getScope())) {
            applyManagementScope(wrapper, managementScope, false);
            if (managementScope.superAdmin()) {
                wrapper.eq(Activity::getApprovalStatus, APPROVAL_APPROVED);
            }
        } else if ("other".equals(queryDTO.getScope())) {
            applyManagementScope(wrapper, managementScope, true);
            wrapper.eq(Activity::getApprovalStatus, APPROVAL_APPROVED);
        } else {
            wrapper.eq(Activity::getApprovalStatus, APPROVAL_APPROVED);
        }
        wrapper.orderByDesc(Activity::getCreateTime);

        Page<Activity> page = activityMapper.selectPage(
                new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        List<ActivityVO> records = toActivityVOList(page.getRecords(), managementScope);
        return new PageResult<>(page.getTotal(), page.getPages(), page.getCurrent(), page.getSize(), records);
    }

    @Override
    public ActivityVO getById(Long id) {
        Activity activity = requireActivity(id);
        ManagementScope scope = getManagementScope();
        if (!Integer.valueOf(APPROVAL_APPROVED).equals(activity.getApprovalStatus())
                && !hasManagePermission(activity.getClubId(), scope)) {
            throw new BusinessException(403, "该活动尚未通过审批");
        }
        return toActivityVOList(List.of(activity), scope).get(0);
    }

    @Override
    public List<ActivityManageOptionDTO> listManageOptions() {
        ManagementScope scope = getManagementScope();
        if (scope.superAdmin() || (!scope.systemAdmin() && scope.clubIds().isEmpty())) {
            return Collections.emptyList();
        }
        return clubMapper.selectList(new LambdaQueryWrapper<Club>().orderByAsc(Club::getClubName)).stream()
                .filter(club -> scope.systemAdmin() || scope.clubIds().contains(club.getId()))
                .map(club -> {
                    ActivityManageOptionDTO option = new ActivityManageOptionDTO();
                    option.setClubId(club.getId());
                    option.setClubName(club.getClubName());
                    option.setApprovalRequired(scope.fundingClubIds().contains(club.getId()));
                    return option;
                })
                .sorted(Comparator.comparing(ActivityManageOptionDTO::getClubId))
                .toList();
    }

    @Override
    public void add(Activity activity) {
        validateActivity(activity);
        ManagementScope scope = getManagementScope();
        checkNotSuperAdmin(scope, "超级管理员不能新增活动");
        checkManagePermission(activity.getClubId(), scope);
        activity.setId(null);
        activity.setTitle(activity.getTitle().trim());
        activity.setOrganizerId(getCurrentUserId());
        activity.setCurrentParticipants(0);
        activity.setApprovalStatus(scope.fundingClubIds().contains(activity.getClubId())
                ? APPROVAL_PENDING : APPROVAL_APPROVED);
        activity.setReviewerId(null);
        activity.setReviewTime(null);
        activity.setReviewRemark(null);
        if (activity.getStatus() == null) {
            activity.setStatus(2);
        }
        activityMapper.insert(activity);
    }

    @Override
    public void update(Activity activity) {
        if (activity.getId() == null) {
            throw new BusinessException("活动ID不能为空");
        }
        Activity existing = requireActivity(activity.getId());
        ManagementScope scope = getManagementScope();
        checkNotSuperAdmin(scope, "超级管理员只能查看和删除活动");
        checkManagePermission(existing.getClubId(), scope);

        Long clubId = activity.getClubId() == null ? existing.getClubId() : activity.getClubId();
        if (clubMapper.selectById(clubId) == null) {
            throw new BusinessException("负责社团不存在");
        }
        checkManagePermission(clubId, scope);
        LocalDateTime startTime = activity.getStartTime() == null ? existing.getStartTime() : activity.getStartTime();
        LocalDateTime endTime = activity.getEndTime() == null ? existing.getEndTime() : activity.getEndTime();
        validateTime(startTime, endTime);
        if (activity.getTitle() == null || activity.getTitle().isBlank()) {
            throw new BusinessException("活动标题不能为空");
        }
        validateStatusAndCapacity(activity.getMaxParticipants(), activity.getStatus());

        activity.setTitle(activity.getTitle().trim());
        activity.setCurrentParticipants(null);
        activity.setOrganizerId(null);
        activity.setApprovalStatus(null);
        activity.setReviewerId(null);
        activity.setReviewTime(null);
        activity.setReviewRemark(null);
        activity.setCreateTime(null);
        activity.setUpdateTime(null);
        activity.setDeleted(null);
        activityMapper.updateById(activity);
        if (Integer.valueOf(APPROVAL_REJECTED).equals(existing.getApprovalStatus())
                && scope.fundingClubIds().contains(existing.getClubId())) {
            activityMapper.update(null, new LambdaUpdateWrapper<Activity>()
                    .eq(Activity::getId, existing.getId())
                    .set(Activity::getApprovalStatus, APPROVAL_PENDING)
                    .set(Activity::getReviewerId, null)
                    .set(Activity::getReviewTime, null)
                    .set(Activity::getReviewRemark, null));
        }
    }

    @Override
    public void delete(Long id) {
        Activity activity = requireActivity(id);
        checkManagePermission(activity.getClubId(), getManagementScope());
        activityMapper.deleteById(id);
    }

    @Override
    public void reviewActivity(Long id, ActivityReviewDTO reviewDTO) {
        checkSuperAdmin(getManagementScope());
        if (reviewDTO == null || (!Integer.valueOf(APPROVAL_APPROVED).equals(reviewDTO.getStatus())
                && !Integer.valueOf(APPROVAL_REJECTED).equals(reviewDTO.getStatus()))) {
            throw new BusinessException("审批结果只能为通过或拒绝");
        }
        String reviewRemark = reviewDTO.getReviewRemark() == null ? null : reviewDTO.getReviewRemark().trim();
        if (reviewRemark != null && reviewRemark.length() > 500) {
            throw new BusinessException("审批意见不能超过500个字符");
        }
        Activity activity = requireActivity(id);
        if (!Integer.valueOf(APPROVAL_PENDING).equals(activity.getApprovalStatus())) {
            throw new BusinessException("该活动申请已处理，不能重复审批");
        }
        int updated = activityMapper.update(null, new LambdaUpdateWrapper<Activity>()
                .eq(Activity::getId, id)
                .eq(Activity::getApprovalStatus, APPROVAL_PENDING)
                .set(Activity::getApprovalStatus, reviewDTO.getStatus())
                .set(Activity::getReviewerId, getCurrentUserId())
                .set(Activity::getReviewTime, LocalDateTime.now())
                .set(Activity::getReviewRemark, reviewRemark));
        if (updated == 0) {
            throw new BusinessException("该活动申请已被其他管理员处理");
        }
    }

    @Override
    @Transactional
    public void register(Long activityId) {
        Activity activity = requireActivity(activityId);
        Long userId = getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("当前账号不可报名活动");
        }

        ActivityRegistration existing = registrationMapper.selectOne(new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getActivityId, activityId)
                .eq(ActivityRegistration::getUserId, userId));
        Integer currentStatus = existing == null ? null : existing.getStatus();
        if (!canRegister(activity, currentStatus)) {
            throw new BusinessException(getRegisterDisabledReason(activity, currentStatus));
        }

        if (existing == null) {
            ActivityRegistration registration = new ActivityRegistration();
            registration.setActivityId(activityId);
            registration.setUserId(userId);
            registration.setStatus(REGISTRATION_PENDING);
            try {
                registrationMapper.insert(registration);
            } catch (DuplicateKeyException exception) {
                throw new BusinessException("您已报名该活动，请勿重复报名");
            }
        } else {
            int updated = registrationMapper.update(null, new LambdaUpdateWrapper<ActivityRegistration>()
                    .eq(ActivityRegistration::getId, existing.getId())
                    .eq(ActivityRegistration::getStatus, REGISTRATION_REJECTED)
                    .set(ActivityRegistration::getStatus, REGISTRATION_PENDING)
                    .set(ActivityRegistration::getReviewerId, null)
                    .set(ActivityRegistration::getReviewTime, null)
                    .set(ActivityRegistration::getReviewRemark, null));
            if (updated == 0) {
                throw new BusinessException("报名状态已变更，请刷新后重试");
            }
        }
    }

    @Override
    public List<ActivityRegistrationVO> listRegistrations(Long activityId) {
        Activity activity = requireActivity(activityId);
        checkManagePermission(activity.getClubId(), getManagementScope());
        List<ActivityRegistration> registrations = registrationMapper.selectList(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, activityId)
                        .orderByAsc(ActivityRegistration::getStatus)
                        .orderByDesc(ActivityRegistration::getCreateTime));
        if (registrations.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> userIds = registrations.stream().map(ActivityRegistration::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return registrations.stream().map(registration -> {
            ActivityRegistrationVO vo = new ActivityRegistrationVO();
            BeanUtils.copyProperties(registration, vo);
            User applicant = userMap.get(registration.getUserId());
            if (applicant != null) {
                vo.setUsername(applicant.getUsername());
                vo.setRealName(applicant.getRealName());
                vo.setNickname(applicant.getNickname());
                vo.setCollege(applicant.getCollege());
                vo.setMajor(applicant.getMajor());
                vo.setGrade(applicant.getGrade());
                vo.setPhone(applicant.getPhone());
            }
            return vo;
        }).toList();
    }

    @Override
    @Transactional
    public void reviewRegistration(Long registrationId, ActivityReviewDTO reviewDTO) {
        checkNotSuperAdmin(getManagementScope(), "超级管理员不能审核活动报名");
        if (reviewDTO == null || (!Integer.valueOf(REGISTRATION_APPROVED).equals(reviewDTO.getStatus())
                && !Integer.valueOf(REGISTRATION_REJECTED).equals(reviewDTO.getStatus()))) {
            throw new BusinessException("审核结果只能为通过或拒绝");
        }
        String reviewRemark = reviewDTO.getReviewRemark() == null ? null : reviewDTO.getReviewRemark().trim();
        if (reviewRemark != null && reviewRemark.length() > 500) {
            throw new BusinessException("审核意见不能超过500个字符");
        }

        ActivityRegistration registration = registrationMapper.selectById(registrationId);
        if (registration == null) {
            throw new BusinessException("报名记录不存在");
        }
        Activity activity = requireActivity(registration.getActivityId());
        checkManagePermission(activity.getClubId(), getManagementScope());
        if (!Integer.valueOf(REGISTRATION_PENDING).equals(registration.getStatus())) {
            throw new BusinessException("该报名已审核，不能重复处理");
        }
        if (Integer.valueOf(REGISTRATION_APPROVED).equals(reviewDTO.getStatus())) {
            if (Integer.valueOf(0).equals(activity.getStatus()) || Integer.valueOf(3).equals(activity.getStatus())) {
                throw new BusinessException("已结束或已取消的活动不能通过报名");
            }
            if (activityMapper.increaseParticipants(activity.getId()) == 0) {
                throw new BusinessException("活动报名人数已满");
            }
        }

        int updated = registrationMapper.update(null, new LambdaUpdateWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getId, registrationId)
                .eq(ActivityRegistration::getStatus, REGISTRATION_PENDING)
                .set(ActivityRegistration::getStatus, reviewDTO.getStatus())
                .set(ActivityRegistration::getReviewerId, getCurrentUserId())
                .set(ActivityRegistration::getReviewTime, LocalDateTime.now())
                .set(ActivityRegistration::getReviewRemark, reviewRemark));
        if (updated == 0) {
            throw new BusinessException("该报名已被其他管理员处理");
        }
    }

    private List<ActivityVO> toActivityVOList(List<Activity> activities, ManagementScope scope) {
        if (activities.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> clubIds = activities.stream().map(Activity::getClubId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> clubNames = clubIds.isEmpty() ? Collections.emptyMap() : clubMapper.selectBatchIds(clubIds)
                .stream().collect(Collectors.toMap(Club::getId, Club::getClubName));
        Set<Long> organizerIds = activities.stream().map(Activity::getOrganizerId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> organizerNames = organizerIds.isEmpty() ? Collections.emptyMap() : userMapper.selectBatchIds(organizerIds)
                .stream().collect(Collectors.toMap(User::getId,
                        user -> user.getRealName() != null && !user.getRealName().isBlank()
                                ? user.getRealName() : user.getUsername()));
        Set<Long> activityIds = activities.stream().map(Activity::getId).collect(Collectors.toSet());
        Map<Long, Integer> registrationStatuses = registrationMapper.selectList(new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getUserId, getCurrentUserId())
                        .in(ActivityRegistration::getActivityId, activityIds))
                .stream().collect(Collectors.toMap(ActivityRegistration::getActivityId, ActivityRegistration::getStatus));

        return activities.stream().map(activity -> {
            ActivityVO vo = new ActivityVO();
            BeanUtils.copyProperties(activity, vo);
            vo.setClubName(clubNames.get(activity.getClubId()));
            vo.setOrganizerName(organizerNames.get(activity.getOrganizerId()));
            vo.setCanManage(hasManagePermission(activity.getClubId(), scope));
            vo.setCanApplyFunding(Integer.valueOf(APPROVAL_APPROVED).equals(activity.getApprovalStatus())
                    && scope.fundingClubIds().contains(activity.getClubId()));
            Integer registrationStatus = registrationStatuses.get(activity.getId());
            vo.setMyRegistrationStatus(registrationStatus);
            vo.setCanRegister(canRegister(activity, registrationStatus));
            return vo;
        }).toList();
    }

    private ManagementScope getManagementScope() {
        Long userId = getCurrentUserId();
        List<String> roles = userRoleMapper.findRoleCodesByUserId(userId);
        boolean superAdmin = roles != null && roles.contains("SUPER_ADMIN");
        boolean systemAdmin = superAdmin || (roles != null && roles.contains("ADMIN"));
        List<ClubMember> memberships = clubMemberMapper.selectList(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getUserId, userId)
                .eq(ClubMember::getStatus, 1));
        Set<Long> clubIds = memberships.stream()
                .filter(member -> "president".equals(member.getPosition())
                        || "vice_president".equals(member.getPosition())
                        || "minister".equals(member.getPosition()))
                .map(ClubMember::getClubId).collect(Collectors.toSet());
        Set<Long> fundingClubIds = superAdmin ? Collections.emptySet() : memberships.stream()
                .filter(member -> "minister".equals(member.getPosition()))
                .map(ClubMember::getClubId).collect(Collectors.toSet());
        return new ManagementScope(superAdmin, systemAdmin, clubIds, fundingClubIds);
    }

    private void applyManagementScope(LambdaQueryWrapper<Activity> wrapper, ManagementScope scope, boolean negate) {
        if (scope.systemAdmin()) {
            if (negate) {
                wrapper.eq(Activity::getId, -1L);
            }
            return;
        }
        if (scope.clubIds().isEmpty()) {
            if (!negate) {
                wrapper.eq(Activity::getId, -1L);
            }
            return;
        }
        if (negate) {
            wrapper.not(condition -> condition.in(Activity::getClubId, scope.clubIds()));
        } else {
            wrapper.in(Activity::getClubId, scope.clubIds());
        }
    }

    private void checkSuperAdmin(ManagementScope scope) {
        if (!scope.superAdmin()) {
            throw new BusinessException(403, "只有超级管理员可以审批活动申请");
        }
    }

    private void checkNotSuperAdmin(ManagementScope scope, String message) {
        if (scope.superAdmin()) {
            throw new BusinessException(403, message);
        }
    }

    private void checkManagePermission(Long clubId, ManagementScope scope) {
        if (!hasManagePermission(clubId, scope)) {
            throw new BusinessException(403, "只能管理本社团负责的活动");
        }
    }

    private boolean hasManagePermission(Long clubId, ManagementScope scope) {
        return scope.systemAdmin() || scope.clubIds().contains(clubId);
    }

    private void validateActivity(Activity activity) {
        if (activity == null || activity.getTitle() == null || activity.getTitle().isBlank()) {
            throw new BusinessException("活动标题不能为空");
        }
        if (activity.getClubId() == null || clubMapper.selectById(activity.getClubId()) == null) {
            throw new BusinessException("负责社团不存在");
        }
        validateTime(activity.getStartTime(), activity.getEndTime());
        validateStatusAndCapacity(activity.getMaxParticipants(), activity.getStatus());
    }

    private void validateTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException("活动开始和结束时间不能为空");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException("活动结束时间必须晚于开始时间");
        }
    }

    private void validateStatusAndCapacity(Integer maxParticipants, Integer status) {
        if (maxParticipants != null && maxParticipants < 0) {
            throw new BusinessException("最大参与人数不能小于0");
        }
        if (status != null && (status < 0 || status > 3)) {
            throw new BusinessException("活动状态无效");
        }
    }

    private boolean canRegister(Activity activity, Integer registrationStatus) {
        if (!Integer.valueOf(APPROVAL_APPROVED).equals(activity.getApprovalStatus())
                || !Integer.valueOf(2).equals(activity.getStatus())
                || (activity.getEndTime() != null && !activity.getEndTime().isAfter(LocalDateTime.now()))
                || Integer.valueOf(REGISTRATION_PENDING).equals(registrationStatus)
                || Integer.valueOf(REGISTRATION_APPROVED).equals(registrationStatus)) {
            return false;
        }
        return activity.getMaxParticipants() == null || activity.getMaxParticipants() == 0
                || activity.getCurrentParticipants() == null
                || activity.getCurrentParticipants() < activity.getMaxParticipants();
    }

    private String getRegisterDisabledReason(Activity activity, Integer registrationStatus) {
        if (!Integer.valueOf(APPROVAL_APPROVED).equals(activity.getApprovalStatus())) {
            return "活动尚未通过审批，不能报名";
        }
        if (Integer.valueOf(REGISTRATION_PENDING).equals(registrationStatus)) {
            return "您的报名正在等待审核";
        }
        if (Integer.valueOf(REGISTRATION_APPROVED).equals(registrationStatus)) {
            return "您已报名成功，请勿重复报名";
        }
        if (!Integer.valueOf(2).equals(activity.getStatus())
                || (activity.getEndTime() != null && !activity.getEndTime().isAfter(LocalDateTime.now()))) {
            return "当前活动不可报名";
        }
        return "活动报名人数已满";
    }

    private Activity requireActivity(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        return activity;
    }

    private Long getCurrentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }

    private record ManagementScope(boolean superAdmin, boolean systemAdmin, Set<Long> clubIds,
                                   Set<Long> fundingClubIds) {
    }
}
