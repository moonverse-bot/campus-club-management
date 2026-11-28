package org.example.campusclubmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 注册请求DTO
 */
@Data
public class RegisterDTO implements Serializable {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码至少6位")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private String phone;

    private String email;

    private String college;

    private String major;

    private String grade;
}
