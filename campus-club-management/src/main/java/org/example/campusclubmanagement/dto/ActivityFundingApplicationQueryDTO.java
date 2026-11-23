package org.example.campusclubmanagement.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 经费申请中心查询参数
 */
@Data
public class ActivityFundingApplicationQueryDTO implements Serializable {

    private String keyword;

    private Long current = 1L;

    private Long size = 10L;
}
