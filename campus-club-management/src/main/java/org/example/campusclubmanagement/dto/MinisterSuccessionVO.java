package org.example.campusclubmanagement.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部长卸任审批视图
 */
@Data
public class MinisterSuccessionVO implements Serializable {

    private Long id;

    private Long clubId;

    private String clubName;

    private Long departmentId;

    private String departmentName;

    private Long ministerId;

    private String ministerName;

    private String ministerUsername;

    private Long candidateId;

    private String candidateName;

    private String candidateUsername;

    private String reason;

    private Integer status;

    private String reviewRemark;

    private LocalDateTime reviewTime;

    private LocalDateTime createTime;
}
