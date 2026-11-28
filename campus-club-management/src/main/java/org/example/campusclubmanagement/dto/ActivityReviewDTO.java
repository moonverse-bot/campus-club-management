package org.example.campusclubmanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 活动报名审核参数
 */
@Data
public class ActivityReviewDTO implements Serializable {

    /** 1-通过，2-拒绝 */
    @NotNull(message = "审批结果不能为空")
    private Integer status;

    private String reviewRemark;
}
