import request from '@/utils/request'
import type { Result, PageResult } from '@/types'

export interface AdminUser {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  status: number
  createdAt: string
  updatedAt: string
  roles: string[]
  roleIds: number[]
}

export interface AdminFile {
  id: number
  fileName: string
  fileSize: number
  thumbnail: string
  creatorId: number
  creatorName: string
  creatorNickname: string
  status: number
  createdAt: string
  updatedAt: string
}

export interface SystemStats {
  totalUsers: number
  todayNewUsers: number
  totalFiles: number
  todayNewFiles: number
  totalVersions: number
  uptime: number
  maxMemory: number
  usedMemory: number
  freeMemory: number
}

export const adminApi = {
  /**
   * 获取用户列表
   */
  getUserList: (params: { page?: number; size?: number; keyword?: string; status?: number }) =>
    request.get<Result<PageResult<AdminUser>>>('/admin/users', { params }),

  /**
   * 获取用户详情
   */
  getUserById: (id: number) =>
    request.get<Result<AdminUser>>(`/admin/users/${id}`),

  /**
   * 创建用户
   */
  createUser: (data: {
    username: string
    password: string
    nickname?: string
    email?: string
    phone?: string
    status?: number
    roleId?: number
  }) => request.post<Result<AdminUser>>('/admin/users', data),

  /**
   * 更新用户
   */
  updateUser: (id: number, data: {
    nickname?: string
    email?: string
    phone?: string
    status?: number
    roleId?: number
  }) => request.put<Result<AdminUser>>(`/admin/users/${id}`, data),

  /**
   * 删除用户
   */
  deleteUser: (id: number) =>
    request.delete<Result<void>>(`/admin/users/${id}`),

  /**
   * 重置密码
   */
  resetPassword: (id: number, password: string) =>
    request.put<Result<void>>(`/admin/users/${id}/password`, { password }),

  /**
   * 获取系统统计
   */
  getSystemStats: () =>
    request.get<Result<SystemStats>>('/admin/stats'),

  // ==================== 文件管理 ====================

  /**
   * 获取文件列表
   */
  getFileList: (params: { page?: number; size?: number; keyword?: string; status?: number; creatorId?: number }) =>
    request.get<Result<PageResult<AdminFile>>>('/admin/files', { params }),

  /**
   * 删除文件
   */
  deleteFile: (id: number) =>
    request.delete<Result<void>>(`/admin/files/${id}`),
}
