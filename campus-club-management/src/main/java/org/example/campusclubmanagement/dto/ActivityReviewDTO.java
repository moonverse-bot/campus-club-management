package org.example.campusclubmanagement.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 活动报名审核参数
 */
@Data
public class ActivityReviewDTO implements Serializable {

    /** 1-通过，2-拒绝 */
    private Integer status;

    private String reviewRemark;
}
