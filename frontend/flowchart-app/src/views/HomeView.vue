<template>
  <div class="home-view">
    <header class="header">
      <div class="logo">
        <img src="@/assets/logo.svg" alt="MyLiu Flowchart" />
        <span>MyLiu Flowchart</span>
      </div>
      <nav class="nav">
        <el-button type="primary" @click="createNew">新建流程图</el-button>
        <el-button @click="goFiles">我的文件</el-button>
        <el-dropdown v-if="userStore.isLoggedIn" @command="handleCommand">
          <span class="user-dropdown">
            <el-avatar :size="32" :src="userStore.userInfo?.avatar">
              {{ userStore.nickname?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <span class="username">{{ userStore.nickname }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>个人中心
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button v-else @click="router.push('/login')">登录</el-button>
      </nav>
    </header>
    
    <main class="main">
      <section class="hero">
        <h1>专业的流程图绘制工具</h1>
        <p>简洁、高效、易用的在线流程图编辑器</p>
        <div class="actions">
          <el-button type="primary" size="large" @click="createNew">
            开始绘制
          </el-button>
        </div>
      </section>
      
      <section class="features">
        <div class="feature-item">
          <el-icon :size="48"><Edit /></el-icon>
          <h3>拖拽绘制</h3>
          <p>直观的拖拽操作，轻松创建流程图</p>
        </div>
        <div class="feature-item">
          <el-icon :size="48"><Document /></el-icon>
          <h3>版本管理</h3>
          <p>自动保存历史版本，随时回溯</p>
        </div>
        <div class="feature-item">
          <el-icon :size="48"><Download /></el-icon>
          <h3>多格式导出</h3>
          <p>支持PNG、SVG、JSON等格式导出</p>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Edit, Document, Download, ArrowDown, User, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const createNew = () => {
  router.push('/editor')
}

const goFiles = () => {
  router.push('/files')
}

const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    userStore.logout()
  }
}
</script>

<style scoped lang="scss">
.home-view {
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
    
    img {
      width: 40px;
      height: 40px;
    }
  }

  .nav {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .user-dropdown {
    display: flex;
    align-items: center;
    gap: 8px;
    color: white;
    cursor: pointer;
    padding: 4px 12px;
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.1);

    &:hover {
      background: rgba(255, 255, 255, 0.2);
    }

    .username {
      max-width: 100px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 60px 40px;
}

.hero {
  text-align: center;
  color: white;
  margin-bottom: 80px;
  
  h1 {
    font-size: 48px;
    margin-bottom: 20px;
  }
  
  p {
    font-size: 20px;
    opacity: 0.9;
    margin-bottom: 40px;
  }
}

.features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
  
  .feature-item {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    border-radius: 16px;
    padding: 40px;
    text-align: center;
    color: white;
    
    h3 {
      margin: 20px 0 10px;
      font-size: 20px;
    }
    
    p {
      opacity: 0.8;
    }
  }
}
</style>
