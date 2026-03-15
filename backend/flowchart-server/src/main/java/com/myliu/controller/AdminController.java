package com.myliu.controller;

import com.myliu.dto.AdminCreateUserRequest;
import com.myliu.dto.AdminUpdateUserRequest;
import com.myliu.result.Result;
import com.myliu.security.UserPrincipal;
import com.myliu.service.AdminService;
import com.myliu.vo.AdminFileVO;
import com.myliu.vo.AdminUserVO;
import com.myliu.vo.PageVO;
import com.myliu.vo.SystemStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 后台管理控制器
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "后台管理", description = "用户管理、文件管理、系统监控等管理接口")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ==================== 用户管理 ====================

    /**
     * 获取用户列表
     */
    @Operation(summary = "获取用户列表", description = "分页获取用户列表，支持关键词搜索和状态筛选")
    @GetMapping("/users")
    public Result<PageVO<AdminUserVO>> getUserList(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        
        if (!checkAdminPermission(principal)) {
            return Result.forbidden();
        }
        
        PageVO<AdminUserVO> result = adminService.getUserList(page, size, keyword, status);
        return Result.success(result);
    }

    /**
     * 获取用户详情
     */
    @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息")
    @GetMapping("/users/{id}")
    public Result<AdminUserVO> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        if (!checkAdminPermission(principal)) {
            return Result.forbidden();
        }
        
        AdminUserVO user = adminService.getUserById(id);
        return Result.success(user);
    }

    /**
     * 创建用户
     */
    @Operation(summary = "创建用户", description = "管理员创建新用户")
    @PostMapping("/users")
    public Result<AdminUserVO> createUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AdminCreateUserRequest request) {
        
        if (!checkAdminPermission(principal)) {
            return Result.forbidden();
        }
        
        AdminUserVO user = adminService.createUser(request);
        return Result.success("创建成功", user);
    }

    /**
     * 更新用户
     */
    @Operation(summary = "更新用户", description = "管理员更新用户信息")
    @PutMapping("/users/{id}")
    public Result<AdminUserVO> updateUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        
        if (!checkAdminPermission(principal)) {
            return Result.forbidden();
        }
        
        AdminUserVO user = adminService.updateUser(id, request);
        return Result.success("更新成功", user);
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户", description = "管理员删除用户")
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        if (!checkAdminPermission(principal)) {
            return Result.forbidden();
        }
        
        adminService.deleteUser(id);
        return Result.success("删除成功", null);
    }

    /**
     * 重置用户密码
     */
    @Operation(summary = "重置密码", description = "管理员重置用户密码")
    @PutMapping("/users/{id}/password")
    public Result<Void> resetPassword(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> request) {
        
        if (!checkAdminPermission(principal)) {
            return Result.forbidden();
        }
        
        String newPassword = request.get("password");
        if (newPassword == null || newPassword.length() < 6) {
            return Result.badRequest("密码长度不能少于6位");
        }
        
        adminService.resetPassword(id, newPassword);
        return Result.success("密码重置成功", null);
    }

    // ==================== 文件管理 ====================

    /**
     * 获取文件列表
     */
    @Operation(summary = "获取文件列表", description = "分页获取文件列表，支持关键词搜索和状态筛选")
    @GetMapping("/files")
    public Result<PageVO<AdminFileVO>> getFileList(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long creatorId) {
        
        if (!checkAdminPermission(principal)) {
            return Result.forbidden();
        }
        
        PageVO<AdminFileVO> result = adminService.getFileList(page, size, keyword, status, creatorId);
        return Result.success(result);
    }

    /**
     * 删除文件
     */
    @Operation(summary = "删除文件", description = "管理员删除文件")
    @DeleteMapping("/files/{id}")
    public Result<Void> deleteFile(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        if (!checkAdminPermission(principal)) {
            return Result.forbidden();
        }
        
        adminService.deleteFile(id);
        return Result.success("删除成功", null);
    }

    // ==================== 系统监控 ====================

    /**
     * 获取系统统计
     */
    @Operation(summary = "获取系统统计", description = "获取系统运行统计数据")
    @GetMapping("/stats")
    public Result<SystemStatsVO> getSystemStats(
            @AuthenticationPrincipal UserPrincipal principal) {
        
        if (!checkAdminPermission(principal)) {
            return Result.forbidden();
        }
        
        SystemStatsVO stats = adminService.getSystemStats();
        return Result.success(stats);
    }

    // ==================== 权限检查 ====================

    /**
     * 检查管理员权限
     */
    private boolean checkAdminPermission(UserPrincipal principal) {
        if (principal == null) {
            return false;
        }
        return adminService.isAdmin(principal.getUserId());
    }
}
