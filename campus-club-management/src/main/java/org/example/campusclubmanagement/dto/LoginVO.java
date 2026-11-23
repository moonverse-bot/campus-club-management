package org.example.campusclubmanagement.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应DTO
 */
@Data
public class LoginVO implements Serializable {

    private String token;

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;
}
