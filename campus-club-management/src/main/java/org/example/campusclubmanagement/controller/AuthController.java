package org.example.campusclubmanagement.controller;

import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.common.Result;
import org.example.campusclubmanagement.dto.LoginDTO;
import org.example.campusclubmanagement.dto.LoginVO;
import org.example.campusclubmanagement.dto.RegisterDTO;
import org.example.campusclubmanagement.dto.UserVO;
import org.example.campusclubmanagement.service.AuthService;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterDTO registerDTO) {
        authService.register(registerDTO);
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser() {
        UserVO userVO = authService.getCurrentUser();
        return Result.success(userVO);
    }

    @PostMapping("/heartbeat")
    public Result<Void> heartbeat() {
        authService.heartbeat();
        return Result.success();
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}
