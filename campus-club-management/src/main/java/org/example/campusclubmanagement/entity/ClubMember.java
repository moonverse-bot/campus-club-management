package org.example.campusclubmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 社团成员实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("club_member")
public class ClubMember extends BaseEntity {

    private Long clubId;

    private Long userId;

    private Long departmentId;

    private String position;

    private LocalDate joinDate;

    private Integer status;

    private String remark;
}
