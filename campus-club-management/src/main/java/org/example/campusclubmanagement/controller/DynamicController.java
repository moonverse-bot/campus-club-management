package org.example.campusclubmanagement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.common.Result;
import org.example.campusclubmanagement.entity.Dynamic;
import org.example.campusclubmanagement.service.DynamicService;
import org.springframework.web.bind.annotation.*;

/**
 * 动态管理控制器
 */
@RestController
@RequestMapping("/dynamics")
@RequiredArgsConstructor
public class DynamicController {

    private final DynamicService dynamicService;

    /**
     * 分页查询动态
     */
    @GetMapping
    public Result<IPage<Dynamic>> pageList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long clubId) {
        return Result.success(dynamicService.pageList(current, size, clubId));
    }

    /**
     * 根据ID查询动态
     */
    @GetMapping("/{id:\\d+}")
    public Result<Dynamic> getById(@PathVariable Long id) {
        return Result.success(dynamicService.getById(id));
    }

    /**
     * 新增动态
     */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody Dynamic dynamic) {
        dynamicService.add(dynamic);
        return Result.success();
    }

    /**
     * 更新动态
     */
    @PutMapping
    public Result<Void> update(@RequestBody Dynamic dynamic) {
        dynamicService.update(dynamic);
        return Result.success();
    }

    /**
     * 删除动态
     */
    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        dynamicService.delete(id);
        return Result.success();
    }
}
