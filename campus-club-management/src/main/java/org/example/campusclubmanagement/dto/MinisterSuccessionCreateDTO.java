package org.example.campusclubmanagement.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 部长卸任推荐请求
 */
@Data
public class MinisterSuccessionCreateDTO implements Serializable {

    private Long clubId;

    private Long candidateId;

    private String reason;
}
