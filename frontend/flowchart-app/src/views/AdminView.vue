<template>
  <div class="admin-view">
    <header class="header">
      <div class="logo">
        <el-icon :size="24"><Setting /></el-icon>
        <span>MyLiu 管理后台</span>
      </div>
      <nav class="nav">
        <el-button text @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
        <el-button text type="danger" @click="logout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </nav>
    </header>

    <div class="main-container">
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <el-menu :default-active="activeMenu" @select="handleMenuSelect">
          <el-menu-item index="dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="files">
            <el-icon><Document /></el-icon>
            <span>文件管理</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <!-- 主内容区 -->
      <main class="content">
        <!-- 仪表盘 -->
        <div v-if="activeMenu === 'dashboard'" class="dashboard">
          <h2>系统概览</h2>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-icon user-icon">
                  <el-icon :size="32"><User /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.totalUsers }}</div>
                  <div class="stat-label">总用户数</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-icon file-icon">
                  <el-icon :size="32"><Document /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.totalFiles }}</div>
                  <div class="stat-label">总文件数</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-icon version-icon">
                  <el-icon :size="32"><Clock /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.totalVersions }}</div>
                  <div class="stat-label">版本记录</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-icon memory-icon">
                  <el-icon :size="32"><Cpu /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.usedMemory }}MB</div>
                  <div class="stat-label">JVM内存使用</div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <el-row :gutter="20" style="margin-top: 20px">
            <el-col :span="12">
              <el-card>
                <template #header>
                  <span>今日统计</span>
                </template>
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="今日新用户">{{ stats.todayNewUsers }}</el-descriptions-item>
                  <el-descriptions-item label="今日新文件">{{ stats.todayNewFiles }}</el-descriptions-item>
                  <el-descriptions-item label="系统运行时间">{{ formatUptime(stats.uptime) }}</el-descriptions-item>
                  <el-descriptions-item label="JVM最大内存">{{ stats.maxMemory }}MB</el-descriptions-item>
                </el-descriptions>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card>
                <template #header>
                  <span>内存使用情况</span>
                </template>
                <el-progress 
                  :percentage="memoryPercentage" 
                  :color="memoryColor"
                  :stroke-width="20"
                />
                <div style="margin-top: 10px; color: #666">
                  已用: {{ stats.usedMemory }}MB / 最大: {{ stats.maxMemory }}MB
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 用户管理 -->
        <div v-if="activeMenu === 'users'" class="users-panel">
          <div class="panel-header">
            <h2>用户管理</h2>
            <el-button type="primary" @click="showCreateDialog">
              <el-icon><Plus /></el-icon>
              新增用户
            </el-button>
          </div>

          <div class="search-bar">
            <el-input 
              v-model="userSearch.keyword" 
              placeholder="搜索用户名/昵称/邮箱" 
              style="width: 280px" 
              clearable
              @keyup.enter="fetchUsers"
              @clear="fetchUsers"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select v-model="userSearch.status" placeholder="全部状态" clearable style="width: 130px">
              <el-option label="全部" :value="undefined" />
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
            <el-button type="primary" @click="fetchUsers">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </div>

          <el-table :data="users" v-loading="loading" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="70" align="center" />
            <el-table-column prop="username" label="用户名" min-width="100" />
            <el-table-column prop="nickname" label="昵称" min-width="100" />
            <el-table-column prop="email" label="邮箱" min-width="180" />
            <el-table-column label="手机号" min-width="120">
              <template #default="{ row }">
                <span v-if="row.phone">{{ row.phone }}</span>
                <span v-else class="empty-text">—</span>
              </template>
            </el-table-column>
            <el-table-column label="角色" min-width="120">
              <template #default="{ row }">
                <template v-if="row.roles && row.roles.length > 0">
                  <el-tag 
                    v-for="role in row.roles" 
                    :key="role" 
                    :type="getRoleTagType(role)"
                    size="small" 
                    class="role-tag"
                  >
                    {{ role }}
                  </el-tag>
                </template>
                <span v-else class="empty-text">—</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="注册时间" min-width="160">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="120" align="center">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-tooltip content="编辑" placement="top">
                    <el-button link type="primary" size="small" @click="showEditDialog(row)">
                      <el-icon><Edit /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="重置密码" placement="top">
                    <el-button link type="warning" size="small" @click="showResetPasswordDialog(row)">
                      <el-icon><Key /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="删除" placement="top">
                    <el-button link type="danger" size="small" @click="handleDeleteUser(row)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </el-tooltip>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="userSearch.page"
              v-model:page-size="userSearch.size"
              :total="userTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchUsers"
              @current-change="fetchUsers"
            />
          </div>
        </div>

        <!-- 文件管理 -->
        <div v-if="activeMenu === 'files'" class="files-panel">
          <div class="panel-header">
            <h2>文件管理</h2>
          </div>

          <div class="search-bar">
            <el-input 
              v-model="fileSearch.keyword" 
              placeholder="搜索文件名" 
              style="width: 280px" 
              clearable
              @keyup.enter="fetchFiles"
              @clear="fetchFiles"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select v-model="fileSearch.status" placeholder="全部状态" clearable style="width: 130px">
              <el-option label="全部" :value="undefined" />
              <el-option label="草稿" :value="1" />
              <el-option label="已发布" :value="2" />
            </el-select>
            <el-button type="primary" @click="fetchFiles">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </div>

          <el-table :data="files" v-loading="fileLoading" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="70" align="center" />
            <el-table-column prop="fileName" label="文件名" min-width="200">
              <template #default="{ row }">
                <div class="file-name">
                  <el-icon class="file-icon"><Document /></el-icon>
                  <span>{{ row.fileName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="文件大小" width="120" align="right">
              <template #default="{ row }">
                {{ formatFileSize(row.fileSize) }}
              </template>
            </el-table-column>
            <el-table-column label="创建者" min-width="120">
              <template #default="{ row }">
                <span v-if="row.creatorNickname">{{ row.creatorNickname }}</span>
                <span v-else-if="row.creatorName">{{ row.creatorName }}</span>
                <span v-else class="empty-text">—</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 2 ? 'success' : 'info'" size="small">
                  {{ row.status === 2 ? '已发布' : '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" min-width="160">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="更新时间" min-width="160">
              <template #default="{ row }">
                {{ formatDate(row.updatedAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="100" align="center">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-tooltip content="查看" placement="top">
                    <el-button link type="primary" size="small" @click="viewFile(row)">
                      <el-icon><View /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="删除" placement="top">
                    <el-button link type="danger" size="small" @click="handleDeleteFile(row)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </el-tooltip>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="fileSearch.page"
              v-model:page-size="fileSearch.size"
              :total="fileTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchFiles"
              @current-change="fetchFiles"
            />
          </div>
        </div>
      </main>
    </div>

    <!-- 新增/编辑用户对话框 -->
    <el-dialog v-model="userDialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username" v-if="!isEdit">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码（至少6位）" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入11位手机号（可选）" maxlength="11" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveUser">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="resetPasswordVisible" title="重置密码" width="400px">
      <el-form :model="resetPasswordForm" :rules="resetPasswordRules" ref="resetPasswordFormRef" label-width="80px">
        <el-form-item label="新密码" prop="password">
          <el-input v-model="resetPasswordForm.password" type="password" placeholder="请输入新密码（至少6位）" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { 
  DataBoard, User, Document, Plus, Clock, Cpu, View,
  Setting, HomeFilled, SwitchButton, Search, Edit, Key, Delete
} from '@element-plus/icons-vue'
import { adminApi, type AdminUser, type AdminFile, type SystemStats } from '@/api/admin'
import { useUserStore } from '@/store/user'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

const activeMenu = ref('dashboard')
const loading = ref(false)

// 系统统计
const stats = ref<SystemStats>({
  totalUsers: 0,
  todayNewUsers: 0,
  totalFiles: 0,
  todayNewFiles: 0,
  totalVersions: 0,
  uptime: 0,
  maxMemory: 0,
  usedMemory: 0,
  freeMemory: 0
})

// 内存使用百分比
const memoryPercentage = computed(() => {
  if (stats.value.maxMemory === 0) return 0
  return Math.round((stats.value.usedMemory / stats.value.maxMemory) * 100)
})

const memoryColor = computed(() => {
  const p = memoryPercentage.value
  if (p < 50) return '#67c23a'
  if (p < 80) return '#e6a23c'
  return '#f56c6c'
})

// 用户列表
const users = ref<AdminUser[]>([])
const userTotal = ref(0)
const userSearch = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined as number | undefined
})

// 用户表单
const userDialogVisible = ref(false)
const isEdit = ref(false)
const editingUserId = ref<number | null>(null)
const userFormRef = ref<FormInstance>()
const userForm = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  status: 1
})

// 验证手机号
const validatePhone = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback() // 手机号可选
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号格式'))
  } else {
    callback()
  }
}

const userRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ]
}

// 重置密码
const resetPasswordVisible = ref(false)
const resetPasswordFormRef = ref<FormInstance>()
const resetPasswordForm = reactive({
  password: ''
})
const resetPasswordUserId = ref<number | null>(null)

const resetPasswordRules: FormRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 文件列表
const files = ref<AdminFile[]>([])
const fileTotal = ref(0)
const fileLoading = ref(false)
const fileSearch = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined as number | undefined
})

// 获取角色标签类型
const getRoleTagType = (role: string) => {
  if (role.includes('超级管理员')) return 'danger'
  if (role.includes('管理员')) return 'warning'
  return 'info'
}

// 获取系统统计
const fetchStats = async () => {
  try {
    const { data } = await adminApi.getSystemStats()
    stats.value = data
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const { data } = await adminApi.getUserList(userSearch)
    users.value = data.list
    userTotal.value = data.total
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取文件列表
const fetchFiles = async () => {
  fileLoading.value = true
  try {
    const { data } = await adminApi.getFileList(fileSearch)
    files.value = data.list
    fileTotal.value = data.total
  } catch (error) {
    console.error('获取文件列表失败:', error)
  } finally {
    fileLoading.value = false
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  editingUserId.value = null
  Object.assign(userForm, {
    username: '',
    password: '',
    nickname: '',
    email: '',
    phone: '',
    status: 1
  })
  userDialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (user: AdminUser) => {
  isEdit.value = true
  editingUserId.value = user.id
  Object.assign(userForm, {
    username: user.username,
    password: '',
    nickname: user.nickname,
    email: user.email,
    phone: user.phone,
    status: user.status
  })
  userDialogVisible.value = true
}

// 保存用户
const handleSaveUser = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (isEdit.value && editingUserId.value) {
        await adminApi.updateUser(editingUserId.value, {
          nickname: userForm.nickname,
          email: userForm.email,
          phone: userForm.phone,
          status: userForm.status
        })
        ElMessage.success('更新成功')
      } else {
        await adminApi.createUser({
          username: userForm.username,
          password: userForm.password,
          nickname: userForm.nickname,
          email: userForm.email,
          phone: userForm.phone,
          status: userForm.status
        })
        ElMessage.success('创建成功')
      }
      userDialogVisible.value = false
      fetchUsers()
      fetchStats()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  })
}

// 删除用户
const handleDeleteUser = async (user: AdminUser) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${user.username}" 吗？`, '提示', { type: 'warning' })
    await adminApi.deleteUser(user.id)
    ElMessage.success('删除成功')
    fetchUsers()
    fetchStats()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 显示重置密码对话框
const showResetPasswordDialog = (user: AdminUser) => {
  resetPasswordUserId.value = user.id
  resetPasswordForm.password = ''
  resetPasswordVisible.value = true
}

// 重置密码
const handleResetPassword = async () => {
  if (!resetPasswordFormRef.value) return
  
  await resetPasswordFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      await adminApi.resetPassword(resetPasswordUserId.value!, resetPasswordForm.password)
      ElMessage.success('密码重置成功')
      resetPasswordVisible.value = false
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '重置失败')
    }
  })
}

// 查看文件
const viewFile = (file: AdminFile) => {
  // 管理员查看他人文件为只读模式
  router.push(`/editor/${file.id}?readonly=true`)
}

// 删除文件
const handleDeleteFile = async (file: AdminFile) => {
  try {
    await ElMessageBox.confirm(`确定要删除文件 "${file.fileName}" 吗？`, '提示', { type: 'warning' })
    await adminApi.deleteFile(file.id)
    ElMessage.success('删除成功')
    fetchFiles()
    fetchStats()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 菜单切换
const handleMenuSelect = (index: string) => {
  activeMenu.value = index
  if (index === 'users') {
    fetchUsers()
  } else if (index === 'files') {
    fetchFiles()
  }
}

// 格式化运行时间
const formatUptime = (ms: number) => {
  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (days > 0) return `${days}天 ${hours % 24}小时`
  if (hours > 0) return `${hours}小时 ${minutes % 60}分钟`
  return `${minutes}分钟`
}

// 格式化日期
const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 格式化文件大小
const formatFileSize = (bytes: number) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 返回首页
const goHome = () => {
  router.push('/')
}

// 退出登录
const logout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped lang="scss">
.admin-view {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
  overflow: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 60px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #1c1f26 0%, #2d3748 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  
  .logo {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #fff;
    font-size: 18px;
    font-weight: 600;
  }
  
  .nav {
    display: flex;
    gap: 8px;
    
    .el-button {
      color: rgba(255, 255, 255, 0.85);
      
      &:hover {
        color: #fff;
        background: rgba(255, 255, 255, 0.1);
      }
    }
  }
}

.main-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  width: 200px;
  flex-shrink: 0;
  background: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.06);
  overflow-y: auto;
  
  .el-menu {
    border-right: none;
    padding: 8px 0;
  }
  
  :deep(.el-menu-item) {
    height: 50px;
    line-height: 50px;
    margin: 4px 8px;
    border-radius: 8px;
    
    &:hover {
      background: #f5f7fa;
    }
    
    &.is-active {
      background: #ecf5ff;
      color: #409eff;
    }
  }
}

.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: #f5f7fa;
}

.dashboard {
  h2 {
    margin-bottom: 20px;
    color: #303133;
    font-weight: 600;
  }
}

.stat-card {
  :deep(.el-card__body) {
    display: flex;
    align-items: center;
    padding: 20px;
  }
  
  .stat-icon {
    width: 64px;
    height: 64px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    margin-right: 16px;
  }
  
  .user-icon { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
  .file-icon { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
  .version-icon { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
  .memory-icon { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
  
  .stat-info {
    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: #303133;
    }
    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

.users-panel, .files-panel {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  
  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h2 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .search-bar {
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 8px;
  }
  
  .empty-text {
    color: #c0c4cc;
  }
  
  .role-tag {
    margin-right: 4px;
  }
  
  .action-buttons {
    display: flex;
    justify-content: center;
    gap: 4px;
  }
  
  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
  }
}

.file-name {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .file-icon {
    color: #409eff;
  }
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
  
  th.el-table__cell {
    background: #f5f7fa;
    color: #606266;
    font-weight: 600;
  }
  
  .el-table__row {
    &:hover > td {
      background: #ecf5ff !important;
    }
  }
}
</style>
