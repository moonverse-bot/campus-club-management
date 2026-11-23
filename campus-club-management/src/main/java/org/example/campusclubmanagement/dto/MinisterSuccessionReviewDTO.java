package org.example.campusclubmanagement.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 部长卸任审批请求
 */
@Data
public class MinisterSuccessionReviewDTO implements Serializable {

    private Boolean approved;

    private String reviewRemark;
}
