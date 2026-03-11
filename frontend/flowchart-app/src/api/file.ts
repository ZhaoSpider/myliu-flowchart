import request from '@/utils/request'
import type { FlowchartFile, FlowchartVersion, FileCreateForm, Result, PageResult } from '@/types'

export const fileApi = {
  /**
   * 获取文件列表
   */
  list: (params: { page?: number; size?: number; keyword?: string }) => 
    request.get<Result<PageResult<FlowchartFile>>>('/files', { params }),

  /**
   * 获取文件详情
   */
  get: (id: number) => 
    request.get<Result<FlowchartFile>>(`/files/${id}`),

  /**
   * 创建文件
   */
  create: (data: FileCreateForm) => 
    request.post<Result<FlowchartFile>>('/files', data),

  /**
   * 更新文件
   */
  update: (id: number, data: Partial<FileCreateForm>) => 
    request.put<Result<FlowchartFile>>(`/files/${id}`, data),

  /**
   * 删除文件
   */
  delete: (id: number) => 
    request.delete<Result<void>>(`/files/${id}`),

  /**
   * 保存文件内容
   */
  saveContent: (id: number, content: string) => 
    request.put<Result<void>>(`/files/${id}/content`, { content }),

  /**
   * 获取文件内容
   */
  getContent: (id: number) => 
    request.get<Result<{ content: string }>>(`/files/${id}/content`),

  /**
   * 获取版本历史
   */
  getVersions: (id: number) => 
    request.get<Result<FlowchartVersion[]>>(`/files/${id}/versions`),

  /**
   * 导出文件
   */
  export: (id: number, format: 'png' | 'svg' | 'json') => 
    request.get(`/files/${id}/export`, { 
      params: { format },
      responseType: 'blob'
    })
}
