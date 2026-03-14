<template>
  <div class="node-sidebar">
    <h3 class="sidebar-title">节点库</h3>
    <div class="node-list">
      <div
        class="node-item"
        draggable="true"
        @dragstart="onDragStart($event, 'rect-node')"
      >
        <div class="node-preview rect-preview"></div>
        <span class="node-label"></span>
      </div>
      <div
        class="node-item"
        draggable="true"
        @dragstart="onDragStart($event, 'circle-node')"
      >
        <div class="node-preview circle-preview"></div>
        <span class="node-label"></span>
      </div>
      <div
        class="node-item"
        draggable="true"
        @dragstart="onDragStart($event, 'polygon-node')"
      >
        <div class="node-preview polygon-preview"></div>
        <span class="node-label"></span>
      </div>
    </div>

    <h3 class="sidebar-title" style="margin-top: 24px">操作</h3>
    <div class="operation-list">
      <el-button type="primary" @click="$emit('clear')" size="small" style="width: 100%; margin-bottom: 8px">
        <el-icon><Delete /></el-icon>
        清空画布
      </el-button>
      <el-button @click="$emit('export')" size="small" style="width: 100%; margin-bottom: 8px">
        <el-icon><Download /></el-icon>
        导出 JSON
      </el-button>
      <el-button @click="$emit('import')" size="small" style="width: 100%">
        <el-icon><Upload /></el-icon>
        导入 JSON
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Delete, Download, Upload } from '@element-plus/icons-vue'

const emit = defineEmits<{
  clear: []
  export: []
  import: []
}>()

// 拖拽开始
const onDragStart = (event: DragEvent, nodeType: string) => {
  if (event.dataTransfer) {
    event.dataTransfer.setData('nodeType', nodeType)
    event.dataTransfer.effectAllowed = 'copy'
  }
}
</script>

<style scoped>
.node-sidebar {
  width: 200px;
  height: 100%;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  padding: 16px;
  box-sizing: border-box;
}

.sidebar-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.node-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.node-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: move;
  transition: all 0.2s;
}

.node-item:hover {
  border-color: #409eff;
  background: #f5f7fa;
}

.node-preview {
  margin-bottom: 8px;
}

.rect-preview {
  width: 60px;
  height: 36px;
  background: #eff4ff;
  border: 2px solid #5f95ff;
  border-radius: 4px;
}

.circle-preview {
  width: 40px;
  height: 40px;
  background: #eff4ff;
  border: 2px solid #5f95ff;
  border-radius: 50%;
}

.polygon-preview {
  width: 40px;
  height: 40px;
  background: #eff4ff;
  border: 2px solid #5f95ff;
  transform: rotate(45deg);
}

.node-label {
  font-size: 12px;
  color: #606266;
}

.operation-list {
  display: flex;
  flex-direction: column;
}
</style>
