package org.example.campusclubmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 动态实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dynamic")
public class Dynamic extends BaseEntity {

    @NotNull(message = "所属社团不能为空")
    private Long clubId;

    private Long userId;

    @NotBlank(message = "动态内容不能为空")
    private String content;

    private String images;

    private Integer likeCount;

    private Integer commentCount;
}
