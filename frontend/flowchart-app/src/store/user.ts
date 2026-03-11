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
      router.push('/')
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
