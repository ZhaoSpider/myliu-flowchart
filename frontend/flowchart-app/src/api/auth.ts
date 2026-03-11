import request from '@/utils/request'
import type { LoginForm, RegisterForm, User, Result } from '@/types'

export const authApi = {
  /**
   * 用户登录
   */
  login: (data: LoginForm) => 
    request.post<Result<{ token: string; user: User }>>('/auth/login', data),

  /**
   * 用户注册
   */
  register: (data: RegisterForm) => 
    request.post<Result<void>>('/auth/register', data),

  /**
   * 获取当前用户信息
   */
  getCurrentUser: () => 
    request.get<Result<User>>('/auth/current'),

  /**
   * 退出登录
   */
  logout: () => 
    request.post<Result<void>>('/auth/logout')
}
