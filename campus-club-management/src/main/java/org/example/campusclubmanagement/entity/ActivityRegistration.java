package org.example.campusclubmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 活动报名实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_registration")
public class ActivityRegistration extends BaseEntity {

    private Long activityId;

    private Long userId;

    /** 0-待审核，1-已通过，2-已拒绝 */
    private Integer status;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private String reviewRemark;
}
