package com.myliu.controller;

import com.myliu.dto.LoginRequest;
import com.myliu.dto.RegisterRequest;
import com.myliu.dto.UpdatePasswordRequest;
import com.myliu.dto.UpdateProfileRequest;
import com.myliu.result.Result;
import com.myliu.security.UserPrincipal;
import com.myliu.service.UserService;
import com.myliu.vo.LoginVO;
import com.myliu.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Tag(name = "认证管理", description = "用户登录、注册、登出等接口")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "通过用户名密码登录，返回JWT Token")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        LoginVO loginVO = userService.login(request);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "注册新用户账号")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success("注册成功", null);
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户", description = "获取当前登录用户的详细信息")
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return Result.unauthorized();
        }
        UserVO user = userService.getUserById(principal.getUserId());
        return Result.success(user);
    }

    /**
     * 更新个人信息
     */
    @Operation(summary = "更新个人信息", description = "更新当前用户的个人信息")
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        if (principal == null) {
            return Result.unauthorized();
        }
        UserVO user = userService.updateProfile(principal.getUserId(), request);
        return Result.success("更新成功", user);
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码", description = "修改当前用户的密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdatePasswordRequest request) {
        if (principal == null) {
            return Result.unauthorized();
        }
        userService.updatePassword(principal.getUserId(), request);
        return Result.success("密码修改成功", null);
    }

    /**
     * 退出登录
     */
    @Operation(summary = "退出登录", description = "退出当前登录状态")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // JWT无状态，客户端删除Token即可
        return Result.success("退出成功", null);
    }
}
