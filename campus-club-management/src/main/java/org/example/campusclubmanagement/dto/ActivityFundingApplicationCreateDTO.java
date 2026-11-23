package org.example.campusclubmanagement.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 活动经费申请参数
 */
@Data
public class ActivityFundingApplicationCreateDTO implements Serializable {

    private Long activityId;

    private BigDecimal amount;

    private String reason;
}
