package org.example.campusclubmanagement.service;

import org.example.campusclubmanagement.common.PageResult;
import org.example.campusclubmanagement.dto.UserQueryDTO;
import org.example.campusclubmanagement.dto.UserVO;
import org.example.campusclubmanagement.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    PageResult<UserVO> pageList(UserQueryDTO queryDTO);

    UserVO getById(Long id);

    void update(User user);

    void delete(Long id);

    void updateStatus(Long id, Integer status);
}
