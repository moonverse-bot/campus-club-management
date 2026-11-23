package org.example.campusclubmanagement.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户查询DTO
 */
@Data
public class UserQueryDTO implements Serializable {

    private String keyword;

    private String college;

    private Integer status;

    private Long current = 1L;

    private Long size = 10L;
}
