package org.example.campusclubmanagement.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.campusclubmanagement.entity.Activity;

/**
 * 活动列表及详情视图
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityVO extends Activity {

    private String clubName;

    private String organizerName;

    private Boolean canManage;

    private Boolean canApplyFunding;

    /** 0-待审核，1-已通过，2-已拒绝，null-未报名 */
    private Integer myRegistrationStatus;

    private Boolean canRegister;
}
