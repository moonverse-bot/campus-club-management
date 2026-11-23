package org.example.campusclubmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.campusclubmanagement.dto.ActivityFundingApplicationVO;
import org.example.campusclubmanagement.entity.ActivityFundingApplication;

@Mapper
public interface ActivityFundingApplicationMapper extends BaseMapper<ActivityFundingApplication> {

    @Select("""
            <script>
            SELECT f.id, f.activity_id, a.title AS activity_title,
                   f.club_id, c.club_name, f.applicant_id,
                   COALESCE(u.real_name, u.nickname, u.username) AS applicant_name,
                   u.username AS applicant_username,
                   f.amount, f.reason, f.create_time
            FROM activity_funding_application f
            LEFT JOIN activity a ON a.id = f.activity_id
            LEFT JOIN club c ON c.id = f.club_id
            LEFT JOIN user u ON u.id = f.applicant_id
            WHERE f.deleted = 0
            <if test="keyword != null and keyword != ''">
                AND (a.title LIKE CONCAT('%', #{keyword}, '%')
                    OR c.club_name LIKE CONCAT('%', #{keyword}, '%')
                    OR u.real_name LIKE CONCAT('%', #{keyword}, '%')
                    OR u.nickname LIKE CONCAT('%', #{keyword}, '%')
                    OR u.username LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY f.create_time DESC, f.id DESC
            </script>
            """)
    Page<ActivityFundingApplicationVO> selectAdminPage(Page<ActivityFundingApplicationVO> page,
                                                        @Param("keyword") String keyword);
}
