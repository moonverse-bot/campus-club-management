package org.example.campusclubmanagement.controller;

import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.common.PageResult;
import org.example.campusclubmanagement.common.Result;
import org.example.campusclubmanagement.dto.ActivityFundingApplicationCreateDTO;
import org.example.campusclubmanagement.dto.ActivityFundingApplicationQueryDTO;
import org.example.campusclubmanagement.dto.ActivityFundingApplicationVO;
import org.example.campusclubmanagement.service.ActivityFundingApplicationService;
import org.springframework.web.bind.annotation.*;

/**
 * 活动经费申请控制器
 */
@RestController
@RequestMapping("/funding-applications")
@RequiredArgsConstructor
public class ActivityFundingApplicationController {

    private final ActivityFundingApplicationService fundingApplicationService;

    @PostMapping
    public Result<Void> create(@RequestBody ActivityFundingApplicationCreateDTO createDTO) {
        fundingApplicationService.create(createDTO);
        return Result.success();
    }

    @GetMapping
    public Result<PageResult<ActivityFundingApplicationVO>> pageForSuperAdmin(
            ActivityFundingApplicationQueryDTO queryDTO) {
        return Result.success(fundingApplicationService.pageForSuperAdmin(queryDTO));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fundingApplicationService.delete(id);
        return Result.success();
    }
}
