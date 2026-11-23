package org.example.campusclubmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.campusclubmanagement.entity.Announcement;

import java.util.List;

/**
 * 公告服务接口
 */
public interface AnnouncementService {

    List<Announcement> listTop();

    List<Announcement> listByClubId(Long clubId);

    IPage<Announcement> pageList(Long current, Long size, Long clubId);

    Announcement getById(Long id);

    void add(Announcement announcement);

    void update(Announcement announcement);

    void delete(Long id);
}
