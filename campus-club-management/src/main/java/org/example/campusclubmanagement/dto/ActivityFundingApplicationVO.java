package org.example.campusclubmanagement.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 经费申请中心记录
 */
@Data
public class ActivityFundingApplicationVO implements Serializable {

    private Long id;

    private Long activityId;

    private String activityTitle;

    private Long clubId;

    private String clubName;

    private Long applicantId;

    private String applicantName;

    private String applicantUsername;

    private BigDecimal amount;

    private String reason;

    private LocalDateTime createTime;
}
