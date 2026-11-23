package org.example.campusclubmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.campusclubmanagement.entity.ClubMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubMemberMapper extends BaseMapper<ClubMember> {
}
