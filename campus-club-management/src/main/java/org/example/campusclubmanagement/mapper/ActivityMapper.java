package org.example.campusclubmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.campusclubmanagement.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

    @Update("UPDATE activity SET current_participants = current_participants + 1 " +
            "WHERE id = #{activityId} AND deleted = 0 " +
            "AND (max_participants IS NULL OR max_participants = 0 OR current_participants < max_participants)")
    int increaseParticipants(@Param("activityId") Long activityId);
}
