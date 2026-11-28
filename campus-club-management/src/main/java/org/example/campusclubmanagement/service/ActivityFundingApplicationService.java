package org.example.campusclubmanagement.service;

import org.example.campusclubmanagement.common.PageResult;
import org.example.campusclubmanagement.dto.ActivityFundingApplicationCreateDTO;
import org.example.campusclubmanagement.dto.ActivityFundingApplicationQueryDTO;
import org.example.campusclubmanagement.dto.ActivityFundingApplicationVO;
import org.example.campusclubmanagement.dto.ActivityReviewDTO;

import java.util.List;

/**
 * 活动经费申请服务
 */
public interface ActivityFundingApplicationService {

    void create(ActivityFundingApplicationCreateDTO createDTO);

    PageResult<ActivityFundingApplicationVO> pageForSuperAdmin(ActivityFundingApplicationQueryDTO queryDTO);

    /** 部长查看自己提交的经费申请及审批结果 */
    List<ActivityFundingApplicationVO> listMine();

    /** 超级管理员审批经费申请：status 1-通过 2-拒绝 */
    void review(Long id, ActivityReviewDTO reviewDTO);

    void delete(Long id);
}
