package org.example.campusclubmanagement.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动报名名单视图
 */
@Data
public class ActivityRegistrationVO implements Serializable {

    private Long id;

    private Long activityId;

    private Long userId;

    private String username;

    private String realName;

    private String nickname;

    private String college;

    private String major;

    private String grade;

    private String phone;

    private Integer status;

    private String reviewRemark;

    private LocalDateTime reviewTime;

    private LocalDateTime createTime;
}
