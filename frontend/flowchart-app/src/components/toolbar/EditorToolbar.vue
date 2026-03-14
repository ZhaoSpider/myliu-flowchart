<template>
  <div class="editor-toolbar">
    <div class="toolbar-group">
      <el-tooltip content="撤销 (Ctrl+Z)" placement="bottom">
        <el-button @click="$emit('undo')" :disabled="!canUndo">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
      </el-tooltip>
      <el-tooltip content="重做 (Ctrl+Y)" placement="bottom">
        <el-button @click="$emit('redo')" :disabled="!canRedo">
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </el-tooltip>
    </div>

    <el-divider direction="vertical" />

    <div class="toolbar-group">
      <el-tooltip content="放大" placement="bottom">
        <el-button @click="$emit('zoomIn')">
          <el-icon><ZoomIn /></el-icon>
        </el-button>
      </el-tooltip>
      <el-tooltip content="缩小" placement="bottom">
        <el-button @click="$emit('zoomOut')">
          <el-icon><ZoomOut /></el-icon>
        </el-button>
      </el-tooltip>
      <el-tooltip content="适应画布" placement="bottom">
        <el-button @click="$emit('fit')">
          <el-icon><FullScreen /></el-icon>
        </el-button>
      </el-tooltip>
      <span class="zoom-level">{{ Math.round(zoom * 100) }}%</span>
    </div>

    <el-divider direction="vertical" />

    <div class="toolbar-group">
      <el-tooltip content="导出 PNG" placement="bottom">
        <el-button @click="$emit('exportPNG')">
          <el-icon><Picture /></el-icon>
        </el-button>
      </el-tooltip>
      <el-tooltip content="导出 SVG" placement="bottom">
        <el-button @click="$emit('exportSVG')">
          <el-icon><Document /></el-icon>
        </el-button>
      </el-tooltip>
    </div>

    <el-divider direction="vertical" />

    <div class="toolbar-group">
      <el-button type="primary" @click="$emit('save')">
        <el-icon><Check /></el-icon>
        保存
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  ArrowLeft,
  ArrowRight,
  ZoomIn,
  ZoomOut,
  FullScreen,
  Picture,
  Document,
  Check,
} from '@element-plus/icons-vue'

defineProps<{
  canUndo: boolean
  canRedo: boolean
  zoom: number
}>()

defineEmits<{
  undo: []
  redo: []
  zoomIn: []
  zoomOut: []
  fit: []
  exportPNG: []
  exportSVG: []
  save: []
}>()
</script>

<style scoped>
.editor-toolbar {
  height: 48px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 8px;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 4px;
}

.zoom-level {
  font-size: 12px;
  color: #606266;
  min-width: 40px;
  text-align: center;
}
</style>
