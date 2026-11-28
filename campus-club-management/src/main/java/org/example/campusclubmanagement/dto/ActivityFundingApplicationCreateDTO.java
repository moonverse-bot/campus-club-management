package org.example.campusclubmanagement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 活动经费申请参数
 */
@Data
public class ActivityFundingApplicationCreateDTO implements Serializable {

    @NotNull(message = "活动不能为空")
    private Long activityId;

    @NotNull(message = "申请金额不能为空")
    @DecimalMin(value = "0.01", message = "申请金额必须大于0")
    private BigDecimal amount;

    @NotBlank(message = "经费用途说明不能为空")
    @Size(max = 500, message = "经费用途说明不能超过500个字符")
    private String reason;
}
