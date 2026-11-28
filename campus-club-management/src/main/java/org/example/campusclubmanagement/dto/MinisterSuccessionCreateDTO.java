package org.example.campusclubmanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 部长卸任推荐请求
 */
@Data
public class MinisterSuccessionCreateDTO implements Serializable {

    @NotNull(message = "社团不能为空")
    private Long clubId;

    @NotNull(message = "推荐接任成员不能为空")
    private Long candidateId;

    private String reason;
}
