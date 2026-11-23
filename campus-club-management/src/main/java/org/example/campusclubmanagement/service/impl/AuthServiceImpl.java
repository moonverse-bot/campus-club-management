package org.example.campusclubmanagement.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.dto.LoginDTO;
import org.example.campusclubmanagement.dto.LoginVO;
import org.example.campusclubmanagement.dto.RegisterDTO;
import org.example.campusclubmanagement.dto.UserVO;
import org.example.campusclubmanagement.entity.User;
import org.example.campusclubmanagement.entity.UserRole;
import org.example.campusclubmanagement.exception.BusinessException;
import org.example.campusclubmanagement.mapper.UserMapper;
import org.example.campusclubmanagement.mapper.UserRoleMapper;
import org.example.campusclubmanagement.service.AuthService;
import org.example.campusclubmanagement.util.JwtUtil;
import org.example.campusclubmanagement.util.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginDTO.getUsername()));

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 验证密码（MD5）
        String encryptedPassword = DigestUtil.md5Hex(loginDTO.getPassword());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 更新最后登录时间和在线活动时间
        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginTime(now);
        user.setLastActiveTime(now);
        userMapper.updateById(user);

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname() != null ? user.getNickname() : user.getRealName());
        loginVO.setAvatar(user.getAvatar());

        return loginVO;
    }

    @Override
    @Transactional
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, registerDTO.getUsername()));
        if (count > 0) {
            throw new BusinessException("学号/用户名已被注册");
        }

        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        user.setPassword(DigestUtil.md5Hex(registerDTO.getPassword()));
        user.setNickname(registerDTO.getRealName());
        user.setStatus(1);
        user.setGender(0);

        userMapper.insert(user);

        // 注册后默认分配学生角色（role_id=7）
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(7L);
        userRoleMapper.insert(userRole);
    }

    @Override
    public void heartbeat() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        int updated = userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, 1)
                .set(User::getLastActiveTime, LocalDateTime.now()));
        if (updated == 0) {
            throw new BusinessException(403, "账号不存在或已被禁用");
        }
    }

    @Override
    public void logout() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getLastActiveTime, null));
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        // 获取角色
        List<String> roles = userRoleMapper.findRoleCodesByUserId(userId);
        userVO.setRoles(roles != null ? roles : Collections.emptyList());

        return userVO;
    }
}
