package org.example.campusclubmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.campusclubmanagement.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    @Select("SELECT r.role_code FROM user_role ur " +
            "LEFT JOIN role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<String> findRoleCodesByUserId(Long userId);
}
