package org.example.campusclubmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "负责社团不能为空")
    private Long clubId;

    @NotBlank(message = "活动标题不能为空")
    private String title;

    @NotBlank(message = "活动内容不能为空")
    private String content;

    private String coverImage;

    @NotBlank(message = "活动地点不能为空")
    private String location;

    @NotNull(message = "活动开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "活动结束时间不能为空")
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
