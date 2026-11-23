package org.example.campusclubmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 活动实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity")
public class Activity extends BaseEntity {

    private Long clubId;

    private String title;

    private String content;

    private String coverImage;

    private String location;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer maxParticipants;

    private Integer currentParticipants;

    private Integer status;

    private Long organizerId;

    /** 0-待审批，1-已通过，2-已拒绝 */
    private Integer approvalStatus;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private String reviewRemark;
}
