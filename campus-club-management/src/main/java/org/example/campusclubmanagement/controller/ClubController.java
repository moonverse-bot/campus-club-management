package org.example.campusclubmanagement.controller;

import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.common.PageResult;
import org.example.campusclubmanagement.common.Result;
import org.example.campusclubmanagement.dto.ClubQueryDTO;
import org.example.campusclubmanagement.entity.Club;
import org.example.campusclubmanagement.service.ClubService;
import org.springframework.web.bind.annotation.*;

/**
 * 社团管理控制器
 */
@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    /**
     * 分页查询社团
     */
    @GetMapping
    public Result<PageResult<Club>> pageList(ClubQueryDTO queryDTO) {
        return Result.success(clubService.pageList(queryDTO));
    }

    /**
     * 根据ID查询社团
     */
    @GetMapping("/{id}")
    public Result<Club> getById(@PathVariable Long id) {
        return Result.success(clubService.getById(id));
    }

    /**
     * 新增社团
     */
    @PostMapping
    public Result<Void> add(@RequestBody Club club) {
        clubService.add(club);
        return Result.success();
    }

    /**
     * 更新社团
     */
    @PutMapping
    public Result<Void> update(@RequestBody Club club) {
        clubService.update(club);
        return Result.success();
    }

    /**
     * 删除社团
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        clubService.delete(id);
        return Result.success();
    }
}
