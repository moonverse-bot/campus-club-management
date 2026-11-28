package org.example.campusclubmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动经费申请实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_funding_application")
public class ActivityFundingApplication extends BaseEntity {

    private Long activityId;

    private Long clubId;

    private Long applicantId;

    private BigDecimal amount;

    private String reason;

    /** 0-待审批，1-已通过，2-已拒绝 */
    private Integer status;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private String reviewRemark;
}
