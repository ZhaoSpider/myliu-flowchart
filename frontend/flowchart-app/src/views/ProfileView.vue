<template>
  <div class="profile-view">
    <header class="header">
      <div class="logo" @click="router.push('/')">
        <img src="@/assets/logo.svg" alt="MyLiu Flowchart" />
        <span>MyLiu Flowchart</span>
      </div>
      <nav class="nav">
        <el-button @click="router.push('/files')">我的文件</el-button>
        <el-button type="primary" @click="router.push('/editor')">新建流程图</el-button>
      </nav>
    </header>

    <main class="main">
      <div class="profile-card">
        <div class="profile-header">
          <el-avatar :size="80" :src="userStore.userInfo?.avatar">
            {{ userStore.nickname?.charAt(0)?.toUpperCase() }}
          </el-avatar>
          <div class="profile-info">
            <h2>{{ userStore.userInfo?.nickname || userStore.username }}</h2>
            <p>{{ userStore.userInfo?.email }}</p>
          </div>
        </div>

        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="info">
            <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
              <el-form-item label="用户名">
                <el-input v-model="userStore.username" disabled />
              </el-form-item>
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="form.nickname" placeholder="请输入昵称" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="form.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="form.phone" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="loading" @click="handleUpdateInfo">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="修改密码" name="password">
            <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
              </el-form-item>
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请确认新密码" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="passwordLoading" @click="handleUpdatePassword">
                  修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { authApi } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('info')
const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const loading = ref(false)
const passwordLoading = ref(false)

const form = reactive({
  nickname: '',
  email: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules: FormRules = {
  nickname: [
    { max: 50, message: '昵称不能超过50个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

onMounted(() => {
  if (userStore.userInfo) {
    form.nickname = userStore.userInfo.nickname || ''
    form.email = userStore.userInfo.email || ''
    form.phone = userStore.userInfo.phone || ''
  }
})

const handleUpdateInfo = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  loading.value = true
  try {
    await authApi.updateProfile({
      nickname: form.nickname,
      email: form.email,
      phone: form.phone
    })
    ElMessage.success('修改成功')
    await userStore.fetchCurrentUser()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '修改失败')
  } finally {
    loading.value = false
  }
}

const handleUpdatePassword = async () => {
  const valid = await passwordFormRef.value?.validate()
  if (!valid) return

  passwordLoading.value = true
  try {
    await authApi.updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '修改失败')
  } finally {
    passwordLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.profile-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;

  .logo {
    display: flex;
    align-items: center;
    gap: 12px;
    color: white;
    font-size: 24px;
    font-weight: bold;
    cursor: pointer;

    img {
      width: 40px;
      height: 40px;
    }
  }
}

.main {
  max-width: 600px;
  margin: 0 auto;
  padding: 40px 20px;
}

.profile-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;

  .profile-info {
    h2 {
      margin: 0 0 8px;
      color: #333;
    }

    p {
      margin: 0;
      color: #666;
    }
  }
}
</style>
