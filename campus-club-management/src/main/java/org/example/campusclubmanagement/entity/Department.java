package org.example.campusclubmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("department")
public class Department extends BaseEntity {

    private Long clubId;

    private String deptName;

    private String description;

    private Long parentId;

    private Integer sortOrder;
}
