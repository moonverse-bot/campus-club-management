package org.example.campusclubmanagement.controller;

import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.common.Result;
import org.example.campusclubmanagement.dto.MinisterSuccessionCreateDTO;
import org.example.campusclubmanagement.dto.MinisterSuccessionReviewDTO;
import org.example.campusclubmanagement.dto.MinisterSuccessionVO;
import org.example.campusclubmanagement.dto.UserVO;
import org.example.campusclubmanagement.service.MinisterSuccessionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部长卸任推荐与审批控制器
 */
@RestController
@RequestMapping("/successions")
@RequiredArgsConstructor
public class MinisterSuccessionController {

    private final MinisterSuccessionService successionService;

    @GetMapping("/candidates")
    public Result<List<UserVO>> listCandidates(@RequestParam Long clubId) {
        return Result.success(successionService.listCandidates(clubId));
    }

    @PostMapping
    public Result<Void> create(@RequestBody MinisterSuccessionCreateDTO createDTO) {
        successionService.create(createDTO);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<List<MinisterSuccessionVO>> listMine() {
        return Result.success(successionService.listMine());
    }

    @GetMapping("/admin")
    public Result<List<MinisterSuccessionVO>> listForAdmin(@RequestParam(required = false) Integer status) {
        return Result.success(successionService.listForAdmin(status));
    }

    @PutMapping("/{id}/review")
    public Result<Void> review(@PathVariable Long id, @RequestBody MinisterSuccessionReviewDTO reviewDTO) {
        successionService.review(id, reviewDTO);
        return Result.success();
    }
}
