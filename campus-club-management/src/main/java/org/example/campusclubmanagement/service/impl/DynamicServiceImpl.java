package org.example.campusclubmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.entity.Dynamic;
import org.example.campusclubmanagement.exception.BusinessException;
import org.example.campusclubmanagement.mapper.DynamicMapper;
import org.example.campusclubmanagement.service.DynamicService;
import org.springframework.stereotype.Service;

/**
 * 动态服务实现
 */
@Service
@RequiredArgsConstructor
public class DynamicServiceImpl implements DynamicService {

    private final DynamicMapper dynamicMapper;

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
        dynamicMapper.updateById(dynamic);
    }

    @Override
    public void delete(Long id) {
        dynamicMapper.deleteById(id);
    }
}
