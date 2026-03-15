<template>
  <div class="editor-view" :class="{ 'readonly-mode': isReadonly }">
    <!-- 顶部工具栏 -->
    <header class="toolbar">
      <div class="toolbar-left">
        <el-button text @click="goBack">
          <el-icon>
            <ArrowLeft />
          </el-icon>
          返回
        </el-button>
        <el-divider direction="vertical" />
        <span class="file-name" @click="!isReadonly && showRenameDialog">{{ fileName }}</span>
        <el-icon v-if="!isReadonly" class="edit-icon" @click="showRenameDialog"><Edit /></el-icon>
      </div>

      <div class="toolbar-center">
        <el-button-group>
          <el-button @click="undo" :disabled="!canUndo">
            <el-icon>
              <Back />
            </el-icon>
          </el-button>
          <el-button @click="redo" :disabled="!canRedo">
            <el-icon>
              <Right />
            </el-icon>
          </el-button>
        </el-button-group>

        <el-button-group>
          <el-button @click="zoomIn">
            <el-icon>
              <ZoomIn />
            </el-icon>
          </el-button>
          <el-button @click="zoomOut">
            <el-icon>
              <ZoomOut />
            </el-icon>
          </el-button>
        </el-button-group>
      </div>

      <div class="toolbar-right">
        <el-button v-if="!isReadonly" @click="save" :loading="isSaving">
          <el-icon>
            <DocumentChecked />
          </el-icon>
          保存
        </el-button>
        <el-tag v-else type="warning" style="margin-right: 12px">只读模式</el-tag>
        <el-dropdown @command="handleExport">
          <el-button type="primary">
            导出 <el-icon class="el-icon--right">
              <ArrowDown />
            </el-icon>
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
      <aside v-if="!isReadonly" class="sidebar">
        <div class="sidebar-content">
          <div class="sidebar-title">基础图形</div>
          <div class="node-list">
            <div v-for="node in nodeTypes" :key="node.type" class="node-item" draggable="true"
              @dragstart="handleDragStart($event, node)">
              <div class="node-preview" :class="`shape-${node.type}`">
                <span class="node-label">{{ node.label }}</span>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <!-- 画布区域 -->
      <main class="canvas-wrapper">
        <div ref="canvasRef" class="canvas"></div>
      </main>

      <!-- 右侧属性面板 -->
      <aside v-if="!isReadonly" class="properties" :style="rightSidebarStyle">
        <div class="resizer resizer-right" @mousedown="startResizeRight"></div>
        <div class="properties-content">
          <div class="properties-title">属性</div>
          <div v-if="!selectedNode" class="empty-properties">
            <el-empty description="请选择一个节点" :image-size="60" />
          </div>
          <el-form v-else label-width="60px" size="small">
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
        </div>
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
import { Export } from '@antv/x6-plugin-export'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Back, Right, ZoomIn, ZoomOut,
  DocumentChecked, ArrowDown, Edit
} from '@element-plus/icons-vue'
import { fileApi } from '@/api/file'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 状态
const canvasRef = ref<HTMLDivElement>()
const fileName = ref('未命名流程图')
const fileId = ref<number | null>(null)
const selectedNode = ref<any>(null)
const isSaving = ref(false)
const isLoading = ref(false)
const isReadonly = ref(false) // 只读模式
let graph: Graph | null = null

// 侧边栏宽度
const leftSidebarWidth = ref(200)
const rightSidebarWidth = ref(240)
const minSidebarWidth = 150
const maxSidebarWidth = 400
const minCanvasWidth = 400


const rightSidebarStyle = computed(() => ({
  width: `${rightSidebarWidth.value}px`
}))

// 拖动调整大小
let isResizingLeft = false
let isResizingRight = false
let startX = 0
let startWidth = 0

const startResizeLeft = (e: MouseEvent) => {
  isResizingLeft = true
  startX = e.clientX
  startWidth = leftSidebarWidth.value
  document.addEventListener('mousemove', handleResize)
  document.addEventListener('mouseup', stopResize)
}

const startResizeRight = (e: MouseEvent) => {
  isResizingRight = true
  startX = e.clientX
  startWidth = rightSidebarWidth.value
  document.addEventListener('mousemove', handleResize)
  document.addEventListener('mouseup', stopResize)
}

const handleResize = (e: MouseEvent) => {
  if (!isResizingLeft && !isResizingRight) return

  const windowWidth = window.innerWidth

  if (isResizingLeft) {
    const delta = e.clientX - startX
    const newWidth = Math.max(minSidebarWidth, Math.min(maxSidebarWidth, startWidth + delta))
    const availableWidth = windowWidth - rightSidebarWidth.value - minCanvasWidth
    leftSidebarWidth.value = Math.min(newWidth, availableWidth)
  }

  if (isResizingRight) {
    const delta = e.clientX - startX
    const newWidth = Math.max(minSidebarWidth, Math.min(maxSidebarWidth, startWidth - delta))
    const availableWidth = windowWidth - leftSidebarWidth.value - minCanvasWidth
    rightSidebarWidth.value = Math.min(newWidth, availableWidth)
  }
}

const stopResize = () => {
  isResizingLeft = false
  isResizingRight = false
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', stopResize)
}

// 撤销/重做状态
const canUndo = computed(() => graph?.canUndo() || false)
const canRedo = computed(() => graph?.canRedo() || false)

// 节点类型
const nodeTypes = [
  { type: 'rect', label: '', shape: 'rect' },
  { type: 'circle', label: '', shape: 'circle' },
  { type: 'diamond', label: '', shape: 'path' },
  { type: 'parallelogram', label: '', shape: 'path' },
  { type: 'ellipse', label: '', shape: 'ellipse' }
]

// 初始化画布
const initGraph = async () => {
  if (!canvasRef.value) return

  graph = new Graph({
    container: canvasRef.value,
    width: canvasRef.value.offsetWidth,
    height: canvasRef.value.offsetHeight,
    background: {
      color: '#ffffff'
    },
    grid: {
      visible: true,
      type: 'dot',
      size: 20,
      args: {
        color: '#e0e0e0',
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
      anchor: 'center',           // 连线锚点位置：节点中心
      snap: { radius: 25 },       // 连接桩吸附半径，靠近时自动吸附
      allowBlank: false,          // 禁止连接到画布空白处
      allowLoop: false,           // 禁止节点连接自身
      allowNode: false,           // 禁止直接连接节点本身
      allowEdge: false,           // 禁止连接到其他连线上
      highlight: true,            // 拖动连线时高亮显示可连接的连接桩
      // 只允许从自定义的圆形连接桩拉线，避免整块节点表面都出现一堆连接点
      validateMagnet({ magnet }) {
        // 只读模式下禁止创建连线
        if (isReadonly.value) return false
        return magnet.getAttribute('port-group') != null
      },
      createEdge() {
        // 默认连线：纯线条，无箭头
        return new Shape.Edge({
          router: {
            name: 'manhattan'
          },
          connector: {
            name: 'rounded'
          }
        })
      },
      validateConnection({ sourceView, targetView, sourceMagnet, targetMagnet }) {
        if (sourceView === targetView) {
          return false
        }
        if (!sourceMagnet || !targetMagnet) {
          return false
        }
        return true
      }
    },
    highlighting: {
      magnetAdsorbed: {
        name: 'stroke',
        args: {
          attrs: {
            fill: '#5F95FF',
            stroke: '#5F95FF'
          }
        }
      }
    }
  })

  // 启用插件
  graph.use(new Selection({ enabled: !isReadonly.value, multiple: true }))
  graph.use(new Snapline({ enabled: true }))
  graph.use(new Keyboard({ enabled: true }))
  graph.use(new History({ enabled: !isReadonly.value }))
  graph.use(new Export())

  // 启用节点变换（调整大小、旋转）- 只读模式下禁用
  if (!isReadonly.value) {
    const { Transform } = await import('@antv/x6-plugin-transform')
    graph.use(new Transform({
      resizing: true,
      rotating: true,
    }))
  }

  // 绑定事件
  graph.on('node:click', ({ node }) => {
    selectedNode.value = {
      id: node.id,
      label: node.attr('text/text') || '',
      width: node.size().width,
      height: node.size().height
    }
  })

  // 双击进入编辑模式 - 内联编辑（只读模式下禁用）
  graph.on('node:dblclick', ({ node, e }) => {
    e.stopPropagation()
    if (!graph || isReadonly.value) return

    // 获取当前文本
    const currentText = String(node.attr('text/text') || '')

    // 获取节点在画布上的位置
    const bbox = node.getBBox()
    const graphContainer = canvasRef.value!
    const containerRect = graphContainer.getBoundingClientRect()

    // 创建编辑容器
    const editorContainer = document.createElement('div')
    editorContainer.style.position = 'absolute'
    editorContainer.style.zIndex = '1000'
    editorContainer.style.display = 'flex'
    editorContainer.style.alignItems = 'center'
    editorContainer.style.justifyContent = 'center'
    editorContainer.style.pointerEvents = 'none'

    // 计算位置（考虑画布缩放和平移）
    const currentGraph = graph!
    const zoom = currentGraph.zoom()
    const translate = currentGraph.translate()
    const x = containerRect.left + (bbox.x + translate.tx) * zoom
    const y = containerRect.top + (bbox.y + translate.ty) * zoom
    const width = bbox.width * zoom
    const height = bbox.height * zoom

    editorContainer.style.left = `${x}px`
    editorContainer.style.top = `${y}px`
    editorContainer.style.width = `${width}px`
    editorContainer.style.height = `${height}px`

    // 创建输入框
    const input = document.createElement('input')
    input.value = currentText
    input.style.width = '90%'
    input.style.padding = '4px 8px'
    input.style.border = 'none'
    input.style.background = 'transparent'
    input.style.color = '#fff'
    input.style.fontSize = '14px'
    input.style.textAlign = 'center'
    input.style.outline = 'none'
    input.style.pointerEvents = 'auto'

    editorContainer.appendChild(input)
    document.body.appendChild(editorContainer)
    input.focus()
    input.select()

    // 保存文本
    const saveText = () => {
      const newText = input.value
      node.attr('text/text', newText)
      if (selectedNode.value && selectedNode.value.id === node.id) {
        selectedNode.value.label = newText
      }
      if (document.body.contains(editorContainer)) {
        document.body.removeChild(editorContainer)
      }
    }

    // 取消编辑
    const cancelEdit = () => {
      if (document.body.contains(editorContainer)) {
        document.body.removeChild(editorContainer)
      }
    }

    input.addEventListener('blur', saveText)
    input.addEventListener('keydown', (evt) => {
      if (evt.key === 'Enter') {
        evt.preventDefault()
        input.blur()
      } else if (evt.key === 'Escape') {
        cancelEdit()
      }
    })
  })

  // 显示/隐藏连接桩
  graph.on('node:mouseenter', ({ node }) => {
    node.getPorts().forEach((port: any) => {
      node.portProp(port.id, 'attrs/circle/style/visibility', 'visible')
    })
  })

  graph.on('node:mouseleave', ({ node }) => {
    node.getPorts().forEach((port: any) => {
      node.portProp(port.id, 'attrs/circle/style/visibility', 'hidden')
    })
  })

  graph.on('blank:click', () => {
    selectedNode.value = null
  })

  // 快捷键
  // 只读模式下禁用删除快捷键
  if (!isReadonly.value) {
    graph.bindKey(['delete', 'backspace'], () => {
      const cells = graph?.getSelectedCells()
      if (cells?.length) {
        graph?.removeCells(cells)
      }
    })
  }

  graph.bindKey('ctrl+z', () => !isReadonly.value && graph?.undo())
  graph.bindKey('ctrl+y', () => !isReadonly.value && graph?.redo())
  graph.bindKey('ctrl+s', (e) => {
    e.preventDefault()
    if (isReadonly.value) return
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

  const ports = {
    groups: {
      top: {
        position: 'top',
        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: '#5F95FF',
            strokeWidth: 1,
            fill: '#fff',
            style: {
              visibility: 'hidden'
            }
          }
        }
      },
      right: {
        position: 'right',
        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: '#5F95FF',
            strokeWidth: 1,
            fill: '#fff',
            style: {
              visibility: 'hidden'
            }
          }
        }
      },
      bottom: {
        position: 'bottom',
        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: '#5F95FF',
            strokeWidth: 1,
            fill: '#fff',
            style: {
              visibility: 'hidden'
            }
          }
        }
      },
      left: {
        position: 'left',
        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: '#5F95FF',
            strokeWidth: 1,
            fill: '#fff',
            style: {
              visibility: 'hidden'
            }
          }
        }
      }
    },
    items: [
      { id: 'top', group: 'top' },
      { id: 'right', group: 'right' },
      { id: 'bottom', group: 'bottom' },
      { id: 'left', group: 'left' }
    ]
  }

  const baseConfig: any = {
    x: x - 50,
    y: y - 25,
    ports,
    attrs: {
      body: {
        fill: 'transparent',
        stroke: '#000',
        strokeWidth: 2
      },
      text: {
        text: nodeType.label || '',
        fill: '#000',
        fontSize: 14,
        textAnchor: 'middle',
        dominantBaseline: 'middle'
      }
    }
  }

  let node: any

  switch (nodeType.type) {
    case 'rect':
      node = graph.addNode({
        ...baseConfig,
        shape: 'rect',
        width: 100,
        height: 50
      })
      break
    case 'circle':
      node = graph.addNode({
        ...baseConfig,
        shape: 'circle',
        width: 60,
        height: 60
      })
      break
    case 'diamond':
      node = graph.addNode({
        ...baseConfig,
        shape: 'polygon',
        width: 80,
        height: 80,
        points: '40,0 80,40 40,80 0,40'
      })
      break
    case 'parallelogram':
      node = graph.addNode({
        ...baseConfig,
        shape: 'polygon',
        width: 100,
        height: 50,
        points: '20,0 100,0 80,50 0,50'
      })
      break
    case 'ellipse':
      node = graph.addNode({
        ...baseConfig,
        shape: 'ellipse',
        width: 100,
        height: 60
      })
      break
    default:
      node = graph.addNode({
        ...baseConfig,
        shape: 'rect',
        width: 100,
        height: 50
      })
  }

  return node
}

// 操作方法
const goBack = () => router.back()

const undo = () => graph?.undo()
const redo = () => graph?.redo()

const zoomIn = () => graph?.zoom(0.1)
const zoomOut = () => graph?.zoom(-0.1)

const save = async () => {
  if (!graph) return
  
  const json = graph.toJSON()
  const content = JSON.stringify(json)
  
  // 如果没有文件ID，先创建文件
  if (!fileId.value) {
    try {
      const { value: newFileName } = await ElMessageBox.prompt('请输入文件名', '新建文件', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: fileName.value === '未命名流程图' ? '' : fileName.value,
        inputPlaceholder: '请输入文件名'
      })
      
      if (!newFileName) {
        ElMessage.warning('文件名不能为空')
        return
      }
      
      isSaving.value = true
      
      // 创建文件
      const { data: newFile } = await fileApi.create({ 
        fileName: newFileName, 
        content 
      })
      
      fileId.value = newFile.id
      fileName.value = newFile.fileName
      
      ElMessage.success('文件创建成功')
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error(error.response?.data?.message || '创建文件失败')
      }
    } finally {
      isSaving.value = false
    }
  } else {
    // 保存已有文件
    try {
      isSaving.value = true
      
      await fileApi.saveContent(fileId.value, content)
      
      ElMessage.success('保存成功')
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '保存失败')
    } finally {
      isSaving.value = false
    }
  }
}

// 加载文件内容
const loadFile = async (id: number) => {
  try {
    isLoading.value = true
    
    // 获取文件信息
    const { data: file } = await fileApi.get(id)
    fileName.value = file.fileName
    fileId.value = file.id
    
    // 获取文件内容
    const { data: content } = await fileApi.getContent(id)
    
    if (content && graph) {
      const json = JSON.parse(content)
      graph.fromJSON(json)
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '加载文件失败')
  } finally {
    isLoading.value = false
  }
}

// 重命名文件
const showRenameDialog = async () => {
  if (!fileId.value) {
    // 如果是新文件，直接保存
    save()
    return
  }
  
  try {
    const { value: newFileName } = await ElMessageBox.prompt('请输入新的文件名', '重命名', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: fileName.value,
      inputPlaceholder: '请输入文件名'
    })
    
    if (!newFileName) {
      ElMessage.warning('文件名不能为空')
      return
    }
    
    await fileApi.update(fileId.value!, { fileName: newFileName })
    fileName.value = newFileName
    ElMessage.success('重命名成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '重命名失败')
    }
  }
}

const handleExport = async (format: string) => {
  if (!graph) return

  switch (format) {
    case 'png':
      graph.toPNG((dataUri) => {
        const link = document.createElement('a')
        link.download = `${fileName.value}.png`
        link.href = dataUri
        link.click()
      })
      ElMessage.success('导出 PNG 成功')
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
      ElMessage.success('导出 SVG 成功')
      break
    case 'json':
      // 如果有文件ID，优先使用后端API导出
      if (fileId.value) {
        try {
          const response = await fileApi.export(fileId.value, 'json')
          const blob = response.data
          const url = URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.download = `${fileName.value}.json`
          link.href = url
          link.click()
          URL.revokeObjectURL(url)
          ElMessage.success('导出 JSON 成功')
        } catch (error: any) {
          // 如果后端导出失败，回退到本地导出
          console.warn('后端导出失败，使用本地导出:', error)
          const json = JSON.stringify(graph.toJSON(), null, 2)
          const blob = new Blob([json], { type: 'application/json' })
          const url = URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.download = `${fileName.value}.json`
          link.href = url
          link.click()
          URL.revokeObjectURL(url)
          ElMessage.success('导出 JSON 成功')
        }
      } else {
        // 新文件使用本地导出
        const json = JSON.stringify(graph.toJSON(), null, 2)
        const blob = new Blob([json], { type: 'application/json' })
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.download = `${fileName.value}.json`
        link.href = url
        link.click()
        URL.revokeObjectURL(url)
        ElMessage.success('导出 JSON 成功')
      }
      break
  }
}

const updateNodeLabel = () => {
  if (!graph || !selectedNode.value) return
  const node = graph.getCellById(selectedNode.value.id)
  if (node) {
    node.attr('text/text', selectedNode.value.label)
  }
}

const updateNodeSize = () => {
  if (!graph || !selectedNode.value) return
  const node = graph.getCellById(selectedNode.value.id)
  if (node && node.isNode()) {
    node.setSize({ width: selectedNode.value.width, height: selectedNode.value.height })
  }
}

// 生命周期
onMounted(async () => {
  // 检查是否是只读模式
  isReadonly.value = route.query.readonly === 'true'

  await initGraph()

  // 监听拖拽事件（只读模式下不需要）
  if (!isReadonly.value) {
    canvasRef.value?.addEventListener('dragover', (e) => e.preventDefault())
    canvasRef.value?.addEventListener('drop', handleDrop)
  }

  // 窗口大小变化时调整画布
  window.addEventListener('resize', () => {
    if (canvasRef.value && graph) {
      graph.resize(canvasRef.value.offsetWidth, canvasRef.value.offsetHeight)
    }
  })

  // 如果有文件ID参数，加载文件
  const id = route.params.id
  if (id) {
    await loadFile(Number(id))
  }
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
  background: #fff;
  border-bottom: 1px solid #ddd;
  color: #333;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 10px;

    .file-name {
      font-weight: 500;
      cursor: pointer;
      
      &:hover {
        color: #409eff;
      }
    }

    .edit-icon {
      cursor: pointer;
      font-size: 14px;
      color: #999;
      
      &:hover {
        color: #409eff;
      }
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

  .resizer {
    width: 4px;
    background: transparent;
    cursor: col-resize;
    transition: background 0.2s;

    &:hover {
      background: #409eff;
    }
  }

  .resizer-left {
    position: absolute;
    right: 0;
    top: 0;
    bottom: 0;
  }

  .resizer-right {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
  }
}

.sidebar {
  width: 200px;
  background: #f5f5f5;
  border-right: 1px solid #ddd;
  overflow: hidden;
  position: relative;
  flex-shrink: 0;

  .sidebar-content {
    padding: 16px;
    height: 100%;
    overflow-y: auto;
    box-sizing: border-box;
  }

  .sidebar-title {
    font-weight: 600;
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #ddd;
    color: #333;
  }

  .node-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;

    .node-item {
      cursor: grab;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 8px;

      &:active {
        cursor: grabbing;
      }

      .node-preview {
        width: 100%;
        height: 50px;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 2px solid #333;
        background: transparent !important;
        font-size: 12px;
        margin-bottom: 6px;
        transition: all 0.2s;
        position: relative;

        .node-label {
          position: relative;
          z-index: 1;
          font-size: 11px;
          color: #333;
          display: block;
        }

        &.shape-rect {
          border-radius: 4px;
        }

        &.shape-circle {
          border-radius: 50%;
          width: 50px;
          height: 50px;
          margin: 0 auto 6px;
        }

        &.shape-diamond {
          transform: rotate(45deg);
          width: 40px;
          height: 40px;
          margin: 5px auto 11px;

          .node-label {
            transform: rotate(-45deg);
          }
        }

        &.shape-parallelogram {
          transform: skew(-20deg);

          .node-label {
            transform: skew(20deg);
          }
        }

        &.shape-ellipse {
          border-radius: 50%;
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
  background: #f5f5f5;
  border-left: 1px solid #ddd;
  overflow: hidden;
  position: relative;
  flex-shrink: 0;
  display: flex;

  .properties-content {
    padding: 16px;
    flex: 1;
    overflow-y: auto;
    box-sizing: border-box;
  }

  .properties-title {
    font-weight: 600;
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #ddd;
    color: #333;
  }

  .empty-properties {
    padding: 20px 0;
  }
}

/* 只读模式下隐藏连接桩 */
.readonly-mode .x6-port {
  display: none !important;
}
</style>
