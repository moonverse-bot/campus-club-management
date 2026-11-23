package org.example.campusclubmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.campusclubmanagement.entity.Dynamic;

/**
 * 动态服务接口
 */
public interface DynamicService {

    IPage<Dynamic> pageList(Long current, Long size, Long clubId);

    Dynamic getById(Long id);

    void add(Dynamic dynamic);

    void update(Dynamic dynamic);

    void delete(Long id);
}
