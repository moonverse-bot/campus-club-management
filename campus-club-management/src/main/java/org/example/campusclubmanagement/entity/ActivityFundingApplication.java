package org.example.campusclubmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

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
}
