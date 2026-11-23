package org.example.campusclubmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.campusclubmanagement.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
