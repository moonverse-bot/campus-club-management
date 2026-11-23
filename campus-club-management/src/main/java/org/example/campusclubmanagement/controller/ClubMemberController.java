package org.example.campusclubmanagement.controller;

import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.common.Result;
import org.example.campusclubmanagement.entity.ClubMember;
import org.example.campusclubmanagement.service.ClubMemberService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社团成员管理控制器
 */
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class ClubMemberController {

    private final ClubMemberService clubMemberService;

    /**
     * 根据社团ID查询成员列表
     */
    @GetMapping("/club/{clubId}")
    public Result<List<ClubMember>> listByClubId(@PathVariable Long clubId) {
        return Result.success(clubMemberService.listByClubId(clubId));
    }

    /**
     * 根据用户ID查询社团列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<ClubMember>> listByUserId(@PathVariable Long userId) {
        return Result.success(clubMemberService.listByUserId(userId));
    }

    /**
     * 新增成员（加入社团）
     */
    @PostMapping
    public Result<Void> add(@RequestBody ClubMember member) {
        clubMemberService.add(member);
        return Result.success();
    }

    /**
     * 更新成员信息
     */
    @PutMapping
    public Result<Void> update(@RequestBody ClubMember member) {
        clubMemberService.update(member);
        return Result.success();
    }

    /**
     * 删除成员
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        clubMemberService.delete(id);
        return Result.success();
    }

    /**
     * 退出社团
     */
    @PostMapping("/quit")
    public Result<Void> quitClub(@RequestParam Long clubId, @RequestParam Long userId) {
        clubMemberService.quitClub(clubId, userId);
        return Result.success();
    }
}
