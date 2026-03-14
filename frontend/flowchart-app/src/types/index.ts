/**
 * 用户相关类型
 */
export interface User {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  status: number
  createdAt: string
}

export interface LoginForm {
  username: string
  password: string
}

export interface RegisterForm {
  username: string
  password: string
  confirmPassword: string
  email: string
  nickname?: string
}

export interface UpdateProfileForm {
  nickname?: string
  email?: string
  phone?: string
}

export interface UpdatePasswordForm {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

/**
 * 流程图文件相关类型
 */
export interface FlowchartFile {
  id: number
  fileName: string
  filePath: string
  fileSize: number
  thumbnail: string
  creatorId: number
  projectId: number
  status: number
  createdAt: string
  updatedAt: string
}

export interface FlowchartVersion {
  id: number
  fileId: number
  versionNo: string
  content: string
  changeLog: string
  creatorId: number
  createdAt: string
}

export interface FileCreateForm {
  fileName: string
  content?: string
}

/**
 * API响应类型
 */
export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
}

/**
 * 流程图节点类型
 */
export interface FlowNode {
  id: string
  shape: string
  x: number
  y: number
  width: number
  height: number
  label?: string
  data?: Record<string, any>
}

export interface FlowEdge {
  id: string
  shape: string
  source: string
  target: string
  label?: string
  data?: Record<string, any>
}

export interface FlowGraph {
  nodes: FlowNode[]
  edges: FlowEdge[]
}
