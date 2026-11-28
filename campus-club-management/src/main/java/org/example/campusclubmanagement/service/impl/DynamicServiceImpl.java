package org.example.campusclubmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.entity.Dynamic;
import org.example.campusclubmanagement.exception.BusinessException;
import org.example.campusclubmanagement.mapper.DynamicMapper;
import org.example.campusclubmanagement.service.DynamicService;
import org.example.campusclubmanagement.service.PermissionChecker;
import org.springframework.stereotype.Service;

/**
 * 动态服务实现
 */
@Service
@RequiredArgsConstructor
public class DynamicServiceImpl implements DynamicService {

    private final DynamicMapper dynamicMapper;
    private final PermissionChecker permissionChecker;

    @Override
    public IPage<Dynamic> pageList(Long current, Long size, Long clubId) {
        LambdaQueryWrapper<Dynamic> wrapper = new LambdaQueryWrapper<>();
        if (clubId != null) {
            wrapper.eq(Dynamic::getClubId, clubId);
        }
        wrapper.orderByDesc(Dynamic::getCreateTime);
        return dynamicMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public Dynamic getById(Long id) {
        Dynamic dynamic = dynamicMapper.selectById(id);
        if (dynamic == null) {
            throw new BusinessException("动态不存在");
        }
        return dynamic;
    }

    @Override
    public void add(Dynamic dynamic) {
        Long currentUserId = permissionChecker.currentUserId();
        dynamic.setUserId(currentUserId);
        // 指定社团时，仅该社团成员或系统管理员可发布
        if (dynamic.getClubId() != null
                && permissionChecker.positionInClub(dynamic.getClubId()) == null
                && !permissionChecker.isAdmin()) {
            throw new BusinessException(403, "只能发布自己所在社团的动态");
        }
        if (dynamic.getLikeCount() == null) {
            dynamic.setLikeCount(0);
        }
        if (dynamic.getCommentCount() == null) {
            dynamic.setCommentCount(0);
        }
        dynamicMapper.insert(dynamic);
    }

    @Override
    public void update(Dynamic dynamic) {
        if (dynamic.getId() == null) {
            throw new BusinessException("动态ID不能为空");
        }
        Dynamic existing = dynamicMapper.selectById(dynamic.getId());
        if (existing == null) {
            throw new BusinessException("动态不存在");
        }
        checkOwnOrAdmin(existing.getUserId());
        // 发布人与所属社团不可通过更新接口变更
        dynamic.setUserId(null);
        dynamic.setClubId(existing.getClubId());
        dynamicMapper.updateById(dynamic);
    }

    @Override
    public void delete(Long id) {
        Dynamic existing = dynamicMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("动态不存在");
        }
        checkOwnOrAdmin(existing.getUserId());
        dynamicMapper.deleteById(id);
    }

    private void checkOwnOrAdmin(Long ownerId) {
        if (permissionChecker.isAdmin()) {
            return;
        }
        if (!permissionChecker.currentUserId().equals(ownerId)) {
            throw new BusinessException(403, "只能操作自己发布的动态");
        }
    }
}
