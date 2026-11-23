package org.example.campusclubmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社团实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("club")
public class Club extends BaseEntity {

    private String clubName;

    private String clubCode;

    private String description;

    private String logo;

    private String category;

    private String advisor;

    private Long presidentId;

    private Integer memberCount;

    private Integer status;
}
