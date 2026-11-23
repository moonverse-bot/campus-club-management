package org.example.campusclubmanagement.service;

import org.example.campusclubmanagement.entity.ClubMember;

import java.util.List;

/**
 * 社团成员服务接口
 */
public interface ClubMemberService {

    List<ClubMember> listByClubId(Long clubId);

    List<ClubMember> listByUserId(Long userId);

    ClubMember getById(Long id);

    ClubMember getByClubAndUser(Long clubId, Long userId);

    void add(ClubMember member);

    void update(ClubMember member);

    void delete(Long id);

    void quitClub(Long clubId, Long userId);
}
