package org.example.campusclubmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 动态实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dynamic")
public class Dynamic extends BaseEntity {

    private Long clubId;

    private Long userId;

    private String content;

    private String images;

    private Integer likeCount;

    private Integer commentCount;
}
