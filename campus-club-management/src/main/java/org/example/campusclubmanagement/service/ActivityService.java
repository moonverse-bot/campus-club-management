package org.example.campusclubmanagement.service;

import org.example.campusclubmanagement.common.PageResult;
import org.example.campusclubmanagement.dto.ActivityManageOptionDTO;
import org.example.campusclubmanagement.dto.ActivityQueryDTO;
import org.example.campusclubmanagement.dto.ActivityRegistrationVO;
import org.example.campusclubmanagement.dto.ActivityReviewDTO;
import org.example.campusclubmanagement.dto.ActivityVO;
import org.example.campusclubmanagement.entity.Activity;

import java.util.List;

/**
 * 活动服务接口
 */
public interface ActivityService {

    PageResult<ActivityVO> pageList(ActivityQueryDTO queryDTO);

    ActivityVO getById(Long id);

    List<ActivityManageOptionDTO> listManageOptions();

    void add(Activity activity);

    void update(Activity activity);

    void delete(Long id);

    void reviewActivity(Long id, ActivityReviewDTO reviewDTO);

    void register(Long activityId);

    List<ActivityRegistrationVO> listRegistrations(Long activityId);

    void reviewRegistration(Long registrationId, ActivityReviewDTO reviewDTO);
}
