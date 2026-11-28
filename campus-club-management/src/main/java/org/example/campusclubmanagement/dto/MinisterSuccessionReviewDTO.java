package org.example.campusclubmanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 部长卸任审批请求
 */
@Data
public class MinisterSuccessionReviewDTO implements Serializable {

    @NotNull(message = "审批结果不能为空")
    private Boolean approved;

    private String reviewRemark;
}
