package org.example.campusclubmanagement.service;

import org.example.campusclubmanagement.common.PageResult;
import org.example.campusclubmanagement.dto.ActivityFundingApplicationCreateDTO;
import org.example.campusclubmanagement.dto.ActivityFundingApplicationQueryDTO;
import org.example.campusclubmanagement.dto.ActivityFundingApplicationVO;

/**
 * 活动经费申请服务
 */
public interface ActivityFundingApplicationService {

    void create(ActivityFundingApplicationCreateDTO createDTO);

    PageResult<ActivityFundingApplicationVO> pageForSuperAdmin(ActivityFundingApplicationQueryDTO queryDTO);

    void delete(Long id);
}
