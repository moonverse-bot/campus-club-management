package org.example.campusclubmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.entity.Announcement;
import org.example.campusclubmanagement.exception.BusinessException;
import org.example.campusclubmanagement.mapper.AnnouncementMapper;
import org.example.campusclubmanagement.service.AnnouncementService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告服务实现
 */
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    @Override
    public List<Announcement> listTop() {
        return announcementMapper.selectList(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getIsTop, 1)
                .orderByDesc(Announcement::getCreateTime)
                .last("LIMIT 10"));
    }

    @Override
    public List<Announcement> listByClubId(Long clubId) {
        return announcementMapper.selectList(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getClubId, clubId)
                .orderByDesc(Announcement::getCreateTime));
    }

    @Override
    public IPage<Announcement> pageList(Long current, Long size, Long clubId) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        if (clubId != null) {
            wrapper.eq(Announcement::getClubId, clubId);
        }
        wrapper.orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getCreateTime);
        return announcementMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public Announcement getById(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        return announcement;
    }

    @Override
    public void add(Announcement announcement) {
        if (announcement.getIsTop() == null) {
            announcement.setIsTop(0);
        }
        announcementMapper.insert(announcement);
    }

    @Override
    public void update(Announcement announcement) {
        announcementMapper.updateById(announcement);
    }

    @Override
    public void delete(Long id) {
        announcementMapper.deleteById(id);
    }
}
