package org.example.campusclubmanagement.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动查询DTO
 */
@Data
public class ActivityQueryDTO implements Serializable {

    private Long clubId;

    private String keyword;

    private Integer status;

    /** managed-我可管理的活动，other-其他活动，空-全部 */
    private String scope;

    private Long current = 1L;

    private Long size = 10L;
}
