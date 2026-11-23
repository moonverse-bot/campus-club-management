package org.example.campusclubmanagement.service;

import org.example.campusclubmanagement.common.PageResult;
import org.example.campusclubmanagement.dto.ClubQueryDTO;
import org.example.campusclubmanagement.entity.Club;

/**
 * 社团服务接口
 */
public interface ClubService {

    PageResult<Club> pageList(ClubQueryDTO queryDTO);

    Club getById(Long id);

    void add(Club club);

    void update(Club club);

    void delete(Long id);

    PageResult<Club> listAll(ClubQueryDTO queryDTO);
}
