package org.example.campusclubmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.common.PageResult;
import org.example.campusclubmanagement.dto.ClubQueryDTO;
import org.example.campusclubmanagement.entity.Club;
import org.example.campusclubmanagement.exception.BusinessException;
import org.example.campusclubmanagement.mapper.ClubMapper;
import org.example.campusclubmanagement.service.ClubService;
import org.example.campusclubmanagement.service.PermissionChecker;
import org.springframework.stereotype.Service;

/**
 * 社团服务实现
 */
@Service
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {

    private final ClubMapper clubMapper;
    private final PermissionChecker permissionChecker;

    @Override
    public PageResult<Club> pageList(ClubQueryDTO queryDTO) {
        LambdaQueryWrapper<Club> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.like(Club::getClubName, queryDTO.getKeyword())
                    .or()
                    .like(Club::getClubCode, queryDTO.getKeyword());
        }
        if (queryDTO.getCategory() != null && !queryDTO.getCategory().isEmpty()) {
            wrapper.eq(Club::getCategory, queryDTO.getCategory());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Club::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(Club::getCreateTime);

        Page<Club> page = clubMapper.selectPage(
                new Page<>(queryDTO.getCurrent(), queryDTO.getSize()),
                wrapper
        );

        return new PageResult<>(page.getTotal(), page.getPages(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    @Override
    public Club getById(Long id) {
        Club club = clubMapper.selectById(id);
        if (club == null) {
            throw new BusinessException("社团不存在");
        }
        return club;
    }

    @Override
    public void add(Club club) {
        permissionChecker.checkAdmin();
        // 检查社团名称是否已存在
        Long count = clubMapper.selectCount(new LambdaQueryWrapper<Club>()
                .eq(Club::getClubName, club.getClubName()));
        if (count > 0) {
            throw new BusinessException("社团名称已存在");
        }
        if (club.getMemberCount() == null) {
            club.setMemberCount(0);
        }
        if (club.getStatus() == null) {
            club.setStatus(1);
        }
        clubMapper.insert(club);
    }

    @Override
    public void update(Club club) {
        permissionChecker.checkAdmin();
        if (club.getId() == null) {
            throw new BusinessException("社团ID不能为空");
        }
        clubMapper.updateById(club);
    }

    @Override
    public void delete(Long id) {
        permissionChecker.checkAdmin();
        clubMapper.deleteById(id);
    }

    @Override
    public PageResult<Club> listAll(ClubQueryDTO queryDTO) {
        queryDTO.setCurrent(1L);
        queryDTO.setSize(100L);
        return pageList(queryDTO);
    }
}
