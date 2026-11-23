package org.example.campusclubmanagement.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息VO（包含角色）
 */
@Data
public class UserVO implements Serializable {

    private Long id;

    private String username;

    private String nickname;

    private String realName;

    private Integer gender;

    private String avatar;

    private String phone;

    private String email;

    private String college;

    private String major;

    private String grade;

    private Integer status;

    private LocalDateTime lastActiveTime;

    private Boolean online;

    private List<String> roles;
}
