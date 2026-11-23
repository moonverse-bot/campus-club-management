package org.example.campusclubmanagement.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 当前用户可管理活动的社团选项
 */
@Data
public class ActivityManageOptionDTO implements Serializable {

    private Long clubId;

    private String clubName;

    private Boolean approvalRequired;
}
