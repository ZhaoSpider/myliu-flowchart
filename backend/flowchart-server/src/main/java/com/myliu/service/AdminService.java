package com.myliu.service;

import com.myliu.dto.AdminCreateUserRequest;
import com.myliu.dto.AdminUpdateUserRequest;
import com.myliu.vo.AdminFileVO;
import com.myliu.vo.AdminUserVO;
import com.myliu.vo.PageVO;
import com.myliu.vo.SystemStatsVO;

/**
 * 管理员服务接口
 *
 * @author MyLiu
 * @since 1.0.0
 */
public interface AdminService {

    /**
     * 获取用户列表（分页）
     */
    PageVO<AdminUserVO> getUserList(Integer page, Integer size, String keyword, Integer status);

    /**
     * 获取用户详情
     */
    AdminUserVO getUserById(Long id);

    /**
     * 创建用户
     */
    AdminUserVO createUser(AdminCreateUserRequest request);

    /**
     * 更新用户
     */
    AdminUserVO updateUser(Long id, AdminUpdateUserRequest request);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 重置用户密码
     */
    void resetPassword(Long id, String newPassword);

    /**
     * 获取系统统计
     */
    SystemStatsVO getSystemStats();

    /**
     * 检查是否为管理员
     */
    boolean isAdmin(Long userId);

    // ==================== 文件管理 ====================

    /**
     * 获取文件列表（分页）
     */
    PageVO<AdminFileVO> getFileList(Integer page, Integer size, String keyword, Integer status, Long creatorId);

    /**
     * 删除文件
     */
    void deleteFile(Long id);
}
