package org.example.campusclubmanagement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.common.Result;
import org.example.campusclubmanagement.entity.Announcement;
import org.example.campusclubmanagement.service.AnnouncementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告管理控制器
 */
@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 获取置顶公告
     */
    @GetMapping("/top")
    public Result<List<Announcement>> listTop() {
        return Result.success(announcementService.listTop());
    }

    /**
     * 分页查询公告
     */
    @GetMapping
    public Result<IPage<Announcement>> pageList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long clubId) {
        return Result.success(announcementService.pageList(current, size, clubId));
    }

    /**
     * 根据社团ID查询公告
     */
    @GetMapping("/club/{clubId}")
    public Result<List<Announcement>> listByClubId(@PathVariable Long clubId) {
        return Result.success(announcementService.listByClubId(clubId));
    }

    /**
     * 根据ID查询公告
     */
    @GetMapping("/{id:\\d+}")
    public Result<Announcement> getById(@PathVariable Long id) {
        return Result.success(announcementService.getById(id));
    }

    /**
     * 新增公告
     */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody Announcement announcement) {
        announcementService.add(announcement);
        return Result.success();
    }

    /**
     * 更新公告
     */
    @PutMapping
    public Result<Void> update(@RequestBody Announcement announcement) {
        announcementService.update(announcement);
        return Result.success();
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.success();
    }
}
