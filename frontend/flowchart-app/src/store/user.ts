import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, LoginForm } from '@/types'
import { authApi } from '@/api/auth'
import { ElMessage } from 'element-plus'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
  const userInfo = ref<User | null>(null)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')
  
  // 判断是否是管理员（拥有 ADMIN 或 SUPER_ADMIN 角色）
  const isAdmin = computed(() => {
    const roles = userInfo.value?.roles || []
    return roles.includes('ADMIN') || roles.includes('SUPER_ADMIN')
  })

  // 方法
  /**
   * 登录
   */
  async function login(form: LoginForm) {
    try {
      const { data } = await authApi.login(form)
      token.value = data.token
      userInfo.value = data.user
      ElMessage.success('登录成功')
      // 登录成功后跳转到之前的页面或首页
      const redirect = router.currentRoute.value.query.redirect as string
      router.push(redirect || '/')
      return true
    } catch (error) {
      return false
    }
  }

  /**
   * 退出登录
   */
  async function logout() {
    try {
      await authApi.logout()
    } finally {
      token.value = ''
      userInfo.value = null
      router.push('/login')
    }
  }

  /**
   * 获取当前用户信息
   */
  async function fetchCurrentUser() {
    try {
      const { data } = await authApi.getCurrentUser()
      userInfo.value = data
    } catch (error) {
      token.value = ''
      userInfo.value = null
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    username,
    nickname,
    isAdmin,
    login,
    logout,
    fetchCurrentUser
  }
}, {
  persist: {
    key: 'myliu-user',
    paths: ['token', 'userInfo']
  }
})
