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
import { Edit, Document, Download } from '@element-plus/icons-vue'

const router = useRouter()

const createNew = () => {
  router.push('/editor')
}

const goFiles = () => {
  router.push('/files')
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
