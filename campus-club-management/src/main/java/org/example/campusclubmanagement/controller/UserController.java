package org.example.campusclubmanagement.controller;

import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.common.PageResult;
import org.example.campusclubmanagement.common.Result;
import org.example.campusclubmanagement.dto.UserQueryDTO;
import org.example.campusclubmanagement.dto.UserVO;
import org.example.campusclubmanagement.entity.User;
import org.example.campusclubmanagement.service.UserService;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户
     */
    @GetMapping
    public Result<PageResult<UserVO>> pageList(UserQueryDTO queryDTO) {
        return Result.success(userService.pageList(queryDTO));
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id:\\d+}")
    public Result<UserVO> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * 更新用户
     */
    @PutMapping
    public Result<Void> update(@RequestBody User user) {
        userService.update(user);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }
}
