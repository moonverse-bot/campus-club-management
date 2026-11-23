package org.example.campusclubmanagement.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 注册请求DTO
 */
@Data
public class RegisterDTO implements Serializable {

    private String username;

    private String password;

    private String realName;

    private String phone;

    private String email;

    private String college;

    private String major;

    private String grade;
}
