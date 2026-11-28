package org.example.campusclubmanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.common.PageResult;
import org.example.campusclubmanagement.common.Result;
import org.example.campusclubmanagement.dto.ActivityFundingApplicationCreateDTO;
import org.example.campusclubmanagement.dto.ActivityFundingApplicationQueryDTO;
import org.example.campusclubmanagement.dto.ActivityFundingApplicationVO;
import org.example.campusclubmanagement.dto.ActivityReviewDTO;
import org.example.campusclubmanagement.service.ActivityFundingApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动经费申请控制器
 */
@RestController
@RequestMapping("/funding-applications")
@RequiredArgsConstructor
public class ActivityFundingApplicationController {

    private final ActivityFundingApplicationService fundingApplicationService;

    @PostMapping
    public Result<Void> create(@Valid @RequestBody ActivityFundingApplicationCreateDTO createDTO) {
        fundingApplicationService.create(createDTO);
        return Result.success();
    }

    @GetMapping
    public Result<PageResult<ActivityFundingApplicationVO>> pageForSuperAdmin(
            ActivityFundingApplicationQueryDTO queryDTO) {
        return Result.success(fundingApplicationService.pageForSuperAdmin(queryDTO));
    }

    @GetMapping("/my")
    public Result<List<ActivityFundingApplicationVO>> listMine() {
        return Result.success(fundingApplicationService.listMine());
    }

    @PutMapping("/{id}/review")
    public Result<Void> review(@PathVariable Long id, @Valid @RequestBody ActivityReviewDTO reviewDTO) {
        fundingApplicationService.review(id, reviewDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fundingApplicationService.delete(id);
        return Result.success();
    }
}
