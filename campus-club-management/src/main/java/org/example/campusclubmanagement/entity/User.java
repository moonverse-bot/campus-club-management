package org.example.campusclubmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    private String username;

    private String password;

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

    private LocalDateTime lastLoginTime;

    private LocalDateTime lastActiveTime;
}
