package org.example.campusclubmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.entity.Announcement;
import org.example.campusclubmanagement.exception.BusinessException;
import org.example.campusclubmanagement.mapper.AnnouncementMapper;
import org.example.campusclubmanagement.service.AnnouncementService;
import org.example.campusclubmanagement.service.PermissionChecker;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告服务实现
 */
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final PermissionChecker permissionChecker;

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
        checkAnnouncementPermission(announcement.getClubId());
        if (announcement.getIsTop() == null) {
            announcement.setIsTop(0);
        }
        announcementMapper.insert(announcement);
    }

    @Override
    public void update(Announcement announcement) {
        if (announcement.getId() == null) {
            throw new BusinessException("公告ID不能为空");
        }
        Announcement existing = announcementMapper.selectById(announcement.getId());
        if (existing == null) {
            throw new BusinessException("公告不存在");
        }
        checkAnnouncementPermission(existing.getClubId());
        // 公告归属不可通过更新接口变更，避免绕过发布权限
        announcement.setClubId(existing.getClubId());
        announcementMapper.updateById(announcement);
    }

    @Override
    public void delete(Long id) {
        Announcement existing = announcementMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("公告不存在");
        }
        checkAnnouncementPermission(existing.getClubId());
        announcementMapper.deleteById(id);
    }

    /** 全校公告(无 clubId)仅系统管理员可发；社团公告限本社团管理者 */
    private void checkAnnouncementPermission(Long clubId) {
        if (clubId == null) {
            permissionChecker.checkAdmin();
        } else {
            permissionChecker.checkClubManager(clubId);
        }
    }
}
