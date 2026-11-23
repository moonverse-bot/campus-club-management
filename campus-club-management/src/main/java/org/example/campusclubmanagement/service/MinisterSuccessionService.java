package org.example.campusclubmanagement.service;

import org.example.campusclubmanagement.dto.MinisterSuccessionCreateDTO;
import org.example.campusclubmanagement.dto.MinisterSuccessionReviewDTO;
import org.example.campusclubmanagement.dto.MinisterSuccessionVO;
import org.example.campusclubmanagement.dto.UserVO;

import java.util.List;

/**
 * 部长卸任推荐服务
 */
public interface MinisterSuccessionService {

    List<UserVO> listCandidates(Long clubId);

    void create(MinisterSuccessionCreateDTO createDTO);

    List<MinisterSuccessionVO> listMine();

    List<MinisterSuccessionVO> listForAdmin(Integer status);

    void review(Long id, MinisterSuccessionReviewDTO reviewDTO);
}
