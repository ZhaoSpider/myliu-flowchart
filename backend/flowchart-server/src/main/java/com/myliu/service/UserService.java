package com.myliu.service;

import com.myliu.dto.LoginRequest;
import com.myliu.dto.RegisterRequest;
import com.myliu.dto.UpdatePasswordRequest;
import com.myliu.dto.UpdateProfileRequest;
import com.myliu.vo.LoginVO;
import com.myliu.vo.UserVO;

/**
 * 用户服务接口
 *
 * @author MyLiu
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应（Token + 用户信息）
     */
    LoginVO login(LoginRequest request);

    /**
     * 用户注册
     *
     * @param request 注册请求
     */
    void register(RegisterRequest request);

    /**
     * 更新个人信息
     *
     * @param userId 用户ID
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    UserVO updateProfile(Long userId, UpdateProfileRequest request);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param request 修改密码请求
     */
    void updatePassword(Long userId, UpdatePasswordRequest request);

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getUserById(Long userId);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserVO getUserByUsername(String username);
}
