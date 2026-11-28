package org.example.campusclubmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("department")
public class Department extends BaseEntity {

    @NotNull(message = "所属社团不能为空")
    private Long clubId;

    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    private String description;

    private Long parentId;

    private Integer sortOrder;
}
