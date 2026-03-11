<template>
  <div class="editor-view">
    <!-- 顶部工具栏 -->
    <header class="toolbar">
      <div class="toolbar-left">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <el-divider direction="vertical" />
        <span class="file-name">{{ fileName }}</span>
      </div>
      
      <div class="toolbar-center">
        <el-button-group>
          <el-button @click="undo" :disabled="!canUndo">
            <el-icon><Back /></el-icon>
          </el-button>
          <el-button @click="redo" :disabled="!canRedo">
            <el-icon><Right /></el-icon>
          </el-button>
        </el-button-group>
        
        <el-button-group>
          <el-button @click="zoomIn">
            <el-icon><ZoomIn /></el-icon>
          </el-button>
          <el-button @click="zoomOut">
            <el-icon><ZoomOut /></el-icon>
          </el-button>
          <el-button @click="resetZoom">100%</el-button>
        </el-button-group>
      </div>
      
      <div class="toolbar-right">
        <el-button @click="save">
          <el-icon><DocumentChecked /></el-icon>
          保存
        </el-button>
        <el-dropdown @command="handleExport">
          <el-button type="primary">
            导出 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="png">导出 PNG</el-dropdown-item>
              <el-dropdown-item command="svg">导出 SVG</el-dropdown-item>
              <el-dropdown-item command="json">导出 JSON</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>
    
    <div class="editor-container">
      <!-- 左侧节点面板 -->
      <aside class="sidebar">
        <div class="sidebar-title">基础图形</div>
        <div class="node-list">
          <div 
            v-for="node in nodeTypes" 
            :key="node.type"
            class="node-item"
            draggable="true"
            @dragstart="handleDragStart($event, node)"
          >
            <div class="node-preview" :style="{ backgroundColor: node.color }">
              {{ node.label }}
            </div>
          </div>
        </div>
      </aside>
      
      <!-- 画布区域 -->
      <main class="canvas-wrapper">
        <div ref="canvasRef" class="canvas"></div>
      </main>
      
      <!-- 右侧属性面板 -->
      <aside class="properties" v-if="selectedNode">
        <div class="properties-title">属性</div>
        <el-form label-width="60px" size="small">
          <el-form-item label="标签">
            <el-input v-model="selectedNode.label" @change="updateNodeLabel" />
          </el-form-item>
          <el-form-item label="宽度">
            <el-input-number v-model="selectedNode.width" :min="40" :max="500" @change="updateNodeSize" />
          </el-form-item>
          <el-form-item label="高度">
            <el-input-number v-model="selectedNode.height" :min="20" :max="500" @change="updateNodeSize" />
          </el-form-item>
        </el-form>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Graph, Shape } from '@antv/x6'
import { Selection } from '@antv/x6-plugin-selection'
import { Snapline } from '@antv/x6-plugin-snapline'
import { Keyboard } from '@antv/x6-plugin-keyboard'
import { History } from '@antv/x6-plugin-history'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, Back, Right, ZoomIn, ZoomOut, 
  DocumentChecked, ArrowDown 
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 状态
const canvasRef = ref<HTMLDivElement>()
const fileName = ref('未命名流程图')
const selectedNode = ref<any>(null)
let graph: Graph | null = null

// 撤销/重做状态
const canUndo = computed(() => graph?.canUndo() || false)
const canRedo = computed(() => graph?.canRedo() || false)

// 节点类型
const nodeTypes = [
  { type: 'rect', label: '矩形', color: '#E3F2FD', shape: 'rect' },
  { type: 'circle', label: '圆形', color: '#E8F5E9', shape: 'circle' },
  { type: 'diamond', label: '菱形', color: '#FFF3E0', shape: 'path' },
  { type: 'parallelogram', label: '平行四边形', color: '#F3E5F5', shape: 'path' },
  { type: 'ellipse', label: '椭圆', color: '#E0F7FA', shape: 'ellipse' },
  { type: 'text', label: '文本', color: '#FAFAFA', shape: 'text' }
]

// 初始化画布
const initGraph = () => {
  if (!canvasRef.value) return
  
  graph = new Graph({
    container: canvasRef.value,
    width: canvasRef.value.offsetWidth,
    height: canvasRef.value.offsetHeight,
    background: {
      color: '#f5f5f5'
    },
    grid: {
      visible: true,
      type: 'dot',
      size: 20,
      args: {
        color: '#ddd',
        thickness: 1
      }
    },
    panning: {
      enabled: true,
      modifiers: []
    },
    mousewheel: {
      enabled: true,
      modifiers: ['ctrl', 'meta']
    },
    connecting: {
      anchor: 'center',
      connectionPoint: 'anchor',
      snap: true,
      allowBlank: false,
      allowLoop: false,
      allowNode: true,
      allowEdge: false,
      createEdge() {
        return new Shape.Edge({
          attrs: {
            line: {
              stroke: '#333',
              strokeWidth: 1,
              targetMarker: {
                name: 'block',
                width: 8,
                height: 6
              }
            }
          }
        })
      }
    }
  })
  
  // 启用插件
  graph.use(new Selection({ enabled: true, multiple: true }))
  graph.use(new Snapline({ enabled: true }))
  graph.use(new Keyboard({ enabled: true }))
  graph.use(new History({ enabled: true }))
  
  // 绑定事件
  graph.on('node:click', ({ node }) => {
    selectedNode.value = {
      id: node.id,
      label: node.attr('text/text') || '',
      width: node.size().width,
      height: node.size().height
    }
  })
  
  graph.on('blank:click', () => {
    selectedNode.value = null
  })
  
  // 快捷键
  graph.bindKey(['delete', 'backspace'], () => {
    const cells = graph?.getSelectedCells()
    if (cells?.length) {
      graph?.removeCells(cells)
    }
  })
  
  graph.bindKey('ctrl+z', () => graph?.undo())
  graph.bindKey('ctrl+y', () => graph?.redo())
  graph.bindKey('ctrl+s', (e) => {
    e.preventDefault()
    save()
  })
}

// 拖拽开始
const handleDragStart = (e: DragEvent, node: any) => {
  e.dataTransfer?.setData('nodeType', JSON.stringify(node))
}

// 拖拽到画布
const handleDrop = (e: DragEvent) => {
  e.preventDefault()
  const data = e.dataTransfer?.getData('nodeType')
  if (!data || !graph) return
  
  const nodeType = JSON.parse(data)
  const rect = canvasRef.value?.getBoundingClientRect()
  if (!rect) return
  
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  
  addNode(nodeType, x, y)
}

// 添加节点
const addNode = (nodeType: any, x: number, y: number) => {
  if (!graph) return
  
  const config: any = {
    x,
    y,
    width: 100,
    height: 40,
    attrs: {
      body: {
        fill: nodeType.color,
        stroke: '#333',
        strokeWidth: 1
      },
      label: {
        text: nodeType.label,
        fill: '#333'
      }
    }
  }
  
  let node: any
  
  switch (nodeType.type) {
    case 'rect':
      node = graph.addNode({ ...config, shape: 'rect' })
      break
    case 'circle':
      config.width = 60
      config.height = 60
      node = graph.addNode({ ...config, shape: 'circle' })
      break
    case 'diamond':
      config.width = 80
      config.height = 80
      node = graph.addNode({ 
        ...config, 
        shape: 'path',
        path: 'M 0 40 L 40 0 L 80 40 L 40 80 Z'
      })
      break
    default:
      node = graph.addNode({ ...config, shape: 'rect' })
  }
  
  return node
}

// 操作方法
const goBack = () => router.back()

const undo = () => graph?.undo()
const redo = () => graph?.redo()

const zoomIn = () => graph?.zoom(0.1)
const zoomOut = () => graph?.zoom(-0.1)
const resetZoom = () => graph?.zoom(1)

const save = async () => {
  if (!graph) return
  
  const json = graph.toJSON()
  console.log('保存数据:', json)
  
  // TODO: 调用API保存
  ElMessage.success('保存成功')
}

const handleExport = (format: string) => {
  if (!graph) return
  
  switch (format) {
    case 'png':
      graph.toPNG((dataUri) => {
        const link = document.createElement('a')
        link.download = `${fileName.value}.png`
        link.href = dataUri
        link.click()
      })
      break
    case 'svg':
      graph.toSVG((svg) => {
        const blob = new Blob([svg], { type: 'image/svg+xml' })
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.download = `${fileName.value}.svg`
        link.href = url
        link.click()
        URL.revokeObjectURL(url)
      })
      break
    case 'json':
      const json = JSON.stringify(graph.toJSON(), null, 2)
      const blob = new Blob([json], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.download = `${fileName.value}.json`
      link.href = url
      link.click()
      URL.revokeObjectURL(url)
      break
  }
  
  ElMessage.success(`导出 ${format.toUpperCase()} 成功`)
}

const updateNodeLabel = () => {
  if (!graph || !selectedNode.value) return
  const node = graph.getCellById(selectedNode.value.id)
  node?.attr('text/text', selectedNode.value.label)
}

const updateNodeSize = () => {
  if (!graph || !selectedNode.value) return
  const node = graph.getCellById(selectedNode.value.id)
  node?.resize(selectedNode.value.width, selectedNode.value.height)
}

// 生命周期
onMounted(() => {
  initGraph()
  
  // 监听拖拽事件
  canvasRef.value?.addEventListener('dragover', (e) => e.preventDefault())
  canvasRef.value?.addEventListener('drop', handleDrop)
  
  // 窗口大小变化时调整画布
  window.addEventListener('resize', () => {
    if (canvasRef.value && graph) {
      graph.resize(canvasRef.value.offsetWidth, canvasRef.value.offsetHeight)
    }
  })
})

onUnmounted(() => {
  graph?.dispose()
})
</script>

<style scoped lang="scss">
.editor-view {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f0f0f0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  background: white;
  border-bottom: 1px solid #e0e0e0;
  
  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 10px;
    
    .file-name {
      font-weight: 500;
    }
  }
  
  .toolbar-center {
    display: flex;
    gap: 10px;
  }
  
  .toolbar-right {
    display: flex;
    gap: 10px;
  }
}

.editor-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.sidebar {
  width: 200px;
  background: white;
  border-right: 1px solid #e0e0e0;
  padding: 16px;
  
  .sidebar-title {
    font-weight: 600;
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #e0e0e0;
  }
  
  .node-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
    
    .node-item {
      cursor: grab;
      
      &:active {
        cursor: grabbing;
      }
      
      .node-preview {
        height: 50px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 4px;
        border: 1px solid #ddd;
        font-size: 12px;
        transition: all 0.2s;
        
        &:hover {
          border-color: #409eff;
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
        }
      }
    }
  }
}

.canvas-wrapper {
  flex: 1;
  position: relative;
  
  .canvas {
    width: 100%;
    height: 100%;
  }
}

.properties {
  width: 240px;
  background: white;
  border-left: 1px solid #e0e0e0;
  padding: 16px;
  
  .properties-title {
    font-weight: 600;
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #e0e0e0;
  }
}
</style>
