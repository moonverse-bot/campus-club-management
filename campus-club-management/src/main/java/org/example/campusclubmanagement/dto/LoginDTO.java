package org.example.campusclubmanagement.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求DTO
 */
@Data
public class LoginDTO implements Serializable {

    private String username;

    private String password;
}
