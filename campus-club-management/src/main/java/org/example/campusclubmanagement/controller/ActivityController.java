package org.example.campusclubmanagement.controller;

import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.common.PageResult;
import org.example.campusclubmanagement.common.Result;
import org.example.campusclubmanagement.dto.ActivityManageOptionDTO;
import org.example.campusclubmanagement.dto.ActivityQueryDTO;
import org.example.campusclubmanagement.dto.ActivityRegistrationVO;
import org.example.campusclubmanagement.dto.ActivityReviewDTO;
import org.example.campusclubmanagement.dto.ActivityVO;
import org.example.campusclubmanagement.entity.Activity;
import org.example.campusclubmanagement.service.ActivityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动管理控制器
 */
@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    public Result<PageResult<ActivityVO>> pageList(ActivityQueryDTO queryDTO) {
        return Result.success(activityService.pageList(queryDTO));
    }

    @GetMapping("/manage-options")
    public Result<List<ActivityManageOptionDTO>> listManageOptions() {
        return Result.success(activityService.listManageOptions());
    }

    @GetMapping("/{id}")
    public Result<ActivityVO> getById(@PathVariable Long id) {
        return Result.success(activityService.getById(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Activity activity) {
        activityService.add(activity);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Activity activity) {
        activityService.update(activity);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        activityService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/review")
    public Result<Void> reviewActivity(@PathVariable Long id, @RequestBody ActivityReviewDTO reviewDTO) {
        activityService.reviewActivity(id, reviewDTO);
        return Result.success();
    }

    @PostMapping("/{id}/registrations")
    public Result<Void> register(@PathVariable Long id) {
        activityService.register(id);
        return Result.success();
    }

    @GetMapping("/{id}/registrations")
    public Result<List<ActivityRegistrationVO>> listRegistrations(@PathVariable Long id) {
        return Result.success(activityService.listRegistrations(id));
    }

    @PutMapping("/registrations/{registrationId}/review")
    public Result<Void> reviewRegistration(@PathVariable Long registrationId,
                                           @RequestBody ActivityReviewDTO reviewDTO) {
        activityService.reviewRegistration(registrationId, reviewDTO);
        return Result.success();
    }
}
