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

    /** 0-待审批，1-已通过，2-已拒绝 */
    private Integer status;

    private String reviewerName;

    private LocalDateTime reviewTime;

    private String reviewRemark;

    private LocalDateTime createTime;
}
