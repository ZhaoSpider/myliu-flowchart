<template>
  <div class="files-view">
    <header class="header">
      <div class="logo">
        <span>MyLiu Flowchart</span>
      </div>
      <nav class="nav">
        <el-button type="primary" @click="createNew">新建</el-button>
        <el-button text @click="goHome">首页</el-button>
      </nav>
    </header>
    
    <main class="main">
      <div class="toolbar">
        <el-input 
          v-model="keyword" 
          placeholder="搜索文件..." 
          style="width: 300px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      
      <div class="file-grid" v-loading="loading">
        <div 
          v-for="file in files" 
          :key="file.id" 
          class="file-card"
          @click="openFile(file.id)"
        >
          <div class="file-thumbnail">
            <img v-if="file.thumbnail" :src="file.thumbnail" alt="缩略图" />
            <el-icon v-else :size="48"><Document /></el-icon>
          </div>
          <div class="file-info">
            <div class="file-name">{{ file.fileName }}</div>
            <div class="file-date">{{ formatDate(file.updatedAt) }}</div>
          </div>
          <div class="file-actions">
            <el-button text size="small" @click.stop="deleteFile(file.id)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
      
      <el-empty v-if="!loading && files.length === 0" description="暂无文件" />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Document, Delete } from '@element-plus/icons-vue'
import { fileApi } from '@/api/file'
import type { FlowchartFile } from '@/types'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const keyword = ref('')
const files = ref<FlowchartFile[]>([])

const fetchFiles = async () => {
  loading.value = true
  try {
    const { data } = await fileApi.list({ keyword: keyword.value })
    files.value = data.list
  } catch (error) {
    console.error('获取文件列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  fetchFiles()
}

const createNew = () => {
  router.push('/editor')
}

const goHome = () => {
  router.push('/')
}

const openFile = (id: number) => {
  router.push(`/editor/${id}`)
}

const deleteFile = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除此文件吗？', '提示', {
      type: 'warning'
    })
    await fileApi.delete(id)
    ElMessage.success('删除成功')
    fetchFiles()
  } catch (error) {
    // 取消删除
  }
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  fetchFiles()
})
</script>

<style scoped lang="scss">
.files-view {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e0e0e0;
  
  .logo {
    font-size: 20px;
    font-weight: bold;
    color: #333;
  }
}

.main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.toolbar {
  margin-bottom: 24px;
}

.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.file-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
  
  .file-thumbnail {
    height: 140px;
    background: #f9f9f9;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #ccc;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }
  }
  
  .file-info {
    padding: 12px;
    
    .file-name {
      font-weight: 500;
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .file-date {
      font-size: 12px;
      color: #999;
    }
  }
  
  .file-actions {
    padding: 0 12px 12px;
    text-align: right;
  }
}
</style>
