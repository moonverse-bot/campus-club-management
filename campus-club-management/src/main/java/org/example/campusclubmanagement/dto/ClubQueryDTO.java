package org.example.campusclubmanagement.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 社团查询DTO
 */
@Data
public class ClubQueryDTO implements Serializable {

    private String keyword;

    private String category;

    private Integer status;

    private Long current = 1L;

    private Long size = 10L;
}
