package org.example.campusclubmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 部长卸任推荐实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("minister_succession")
public class MinisterSuccession extends BaseEntity {

    private Long clubId;

    private Long departmentId;

    private Long ministerId;

    private Long candidateId;

    private String reason;

    /** 0-待审批，1-已通过，2-已拒绝 */
    private Integer status;

    private Long reviewerId;

    private String reviewRemark;

    private LocalDateTime reviewTime;
}
