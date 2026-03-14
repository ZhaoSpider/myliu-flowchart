<template>
  <div ref="container" class="canvas-container"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Graph } from '@antv/x6'
import { Snapline } from '@antv/x6-plugin-snapline'
import { Keyboard } from '@antv/x6-plugin-keyboard'
import { History } from '@antv/x6-plugin-history'
import { Selection } from '@antv/x6-plugin-selection'
import { Transform } from '@antv/x6-plugin-transform'
import { Clipboard } from '@antv/x6-plugin-clipboard'

const container = ref<HTMLElement>()
let graph: Graph | null = null

// 初始化画布
const initGraph = () => {
  if (!container.value) return

  graph = new Graph({
    container: container.value,
    width: 800,
    height: 600,
    grid: {
      size: 10,
      visible: true,
      type: 'dot',
      args: {
        color: '#e5e5e5',
        thickness: 1,
      },
    },
    background: {
      color: '#f8f9fa',
    },
    panning: {
      enabled: true,
      eventTypes: ['leftMouseDown', 'mouseWheel'],
    },
    mousewheel: {
      enabled: true,
      zoomAtMousePosition: true,
      modifiers: 'ctrl',
      minScale: 0.5,
      maxScale: 3,
    },
    connecting: {
      router: 'manhattan',
      connector: {
        name: 'rounded',
        args: {
          radius: 8,
        },
      },
      anchor: 'center',
      connectionPoint: 'anchor',
      allowBlank: false,
      snap: {
        radius: 20,
      },
      createEdge() {
        return graph!.createEdge({
          shape: 'edge',
          attrs: {
            line: {
              strokeWidth: 1,
              targetMarker: {
                name: 'classic',
                size: 8,
              },
            },
          },
        })
      },
    },
    highlighting: {
      magnetAdsorbed: {
        name: 'stroke',
        args: {
          attrs: {
            fill: '#5f95ff',
            stroke: '#5f95ff',
          },
        },
      },
    },
  })

  // 安装插件
  graph.use(
    new Snapline({
      enabled: true,
    })
  )
  graph.use(
    new Keyboard({
      enabled: true,
    })
  )
  graph.use(
    new History({
      enabled: true,
    })
  )
  graph.use(
    new Selection({
      enabled: true,
      multiple: true,
      rubberband: true,
      movable: true,
      showNodeSelectionBox: true,
      showEdgeSelectionBox: true,
    })
  )
  graph.use(
    new Transform({
      resizing: true,
      rotating: true,
    })
  )
  graph.use(new Clipboard({ enabled: true }))

  // 注册基础节点
  registerNodes()

  // 键盘快捷键
  bindKeyboardShortcuts()
}

// 注册基础节点
const registerNodes = () => {
  if (!graph) return

  // 矩形节点
  Graph.registerNode('rect-node', {
    inherit: 'rect',
    width: 100,
    height: 60,
    attrs: {
      body: {
        stroke: '#5f95ff',
        fill: '#eff4ff',
        strokeWidth: 1,
        rx: 6,
        ry: 6,
      },
      text: {
        fill: '#262626',
        fontSize: 12,
      },
    },
  })

  // 圆形节点
  Graph.registerNode('circle-node', {
    inherit: 'circle',
    width: 60,
    height: 60,
    attrs: {
      body: {
        stroke: '#5f95ff',
        fill: '#eff4ff',
        strokeWidth: 1,
      },
      text: {
        fill: '#262626',
        fontSize: 12,
      },
    },
  })

  // 菱形节点
  Graph.registerNode('polygon-node', {
    inherit: 'polygon',
    width: 80,
    height: 80,
    attrs: {
      body: {
        stroke: '#5f95ff',
        fill: '#eff4ff',
        strokeWidth: 1,
        refPoints: '0,10 10,0 20,10 10,20',
      },
      text: {
        fill: '#262626',
        fontSize: 12,
      },
    },
  })
}

// 键盘快捷键
const bindKeyboardShortcuts = () => {
  if (!graph) return

  // Ctrl+Z 撤销
  graph.bindKey('ctrl+z', () => {
    graph?.undo()
  })

  // Ctrl+Y 重做
  graph.bindKey('ctrl+y', () => {
    graph?.redo()
  })

  // Ctrl+C 复制
  graph.bindKey('ctrl+c', () => {
    const cells = graph?.getSelectedCells()
    if (cells && cells.length) {
      graph?.copy(cells)
    }
  })

  // Ctrl+V 粘贴
  graph.bindKey('ctrl+v', () => {
    if (!graph?.isClipboardEmpty()) {
      const cells = graph?.paste({ offset: 32 })
      graph?.cleanSelection()
      if (cells) {
        graph?.select(cells)
      }
    }
  })

  // Delete 删除
  graph.bindKey('delete', () => {
    const cells = graph?.getSelectedCells()
    if (cells && cells.length) {
      graph?.removeCells(cells)
    }
  })
}

// 添加节点
const addNode = (type: string, x: number, y: number) => {
  if (!graph) return

  const node = graph.addNode({
    shape: type,
    x,
    y,
    label: type === 'rect-node' ? '矩形' : type === 'circle-node' ? '圆形' : '菱形',
  })

  return node
}

// 导出方法供父组件调用
defineExpose({
  addNode,
  getGraph: () => graph,
  exportToJSON: () => graph?.toJSON(),
  importFromJSON: (data: any) => graph?.fromJSON(data),
})

onMounted(() => {
  initGraph()
})

onUnmounted(() => {
  graph?.dispose()
  graph = null
})
</script>

<style scoped>
.canvas-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
}
</style>
