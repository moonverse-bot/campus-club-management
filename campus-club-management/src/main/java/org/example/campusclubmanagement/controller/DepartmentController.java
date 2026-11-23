package org.example.campusclubmanagement.controller;

import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.common.Result;
import org.example.campusclubmanagement.entity.Department;
import org.example.campusclubmanagement.service.DepartmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 */
@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 根据社团ID查询部门列表
     */
    @GetMapping("/club/{clubId}")
    public Result<List<Department>> listByClubId(@PathVariable Long clubId) {
        return Result.success(departmentService.listByClubId(clubId));
    }

    /**
     * 根据ID查询部门
     */
    @GetMapping("/{id}")
    public Result<Department> getById(@PathVariable Long id) {
        return Result.success(departmentService.getById(id));
    }

    /**
     * 新增部门
     */
    @PostMapping
    public Result<Void> add(@RequestBody Department department) {
        departmentService.add(department);
        return Result.success();
    }

    /**
     * 更新部门
     */
    @PutMapping
    public Result<Void> update(@RequestBody Department department) {
        departmentService.update(department);
        return Result.success();
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return Result.success();
    }
}
