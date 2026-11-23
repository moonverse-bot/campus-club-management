package org.example.campusclubmanagement.service;

import org.example.campusclubmanagement.dto.LoginDTO;
import org.example.campusclubmanagement.dto.LoginVO;
import org.example.campusclubmanagement.dto.RegisterDTO;
import org.example.campusclubmanagement.dto.UserVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    LoginVO login(LoginDTO loginDTO);

    void register(RegisterDTO registerDTO);

    UserVO getCurrentUser();

    void heartbeat();

    void logout();
}
