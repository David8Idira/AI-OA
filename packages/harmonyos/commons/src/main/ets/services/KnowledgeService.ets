import ApiClient, { ApiResponse } from '../data/api/ApiClient'

/**
 * 知识库模型
 */

// 文档类型
export enum DocumentType {
  FOLDER = 'folder',
  FILE = 'file',
  DOCUMENT = 'document',
  SPREADSHEET = 'spreadsheet',
  PRESENTATION = 'presentation'
}

// 文档状态
export enum DocumentStatus {
  DRAFT = 'draft',
  PUBLISHED = 'published',
  ARCHIVED = 'archived'
}

// 文档模型
export interface KnowledgeDocument {
  id: string
  title: string
  type: DocumentType
  parentId?: string
  parentPath?: string
  content?: string
  summary?: string
  tags?: string[]
  status: DocumentStatus
  creatorId: string
  creatorName: string
  creatorAvatar?: string
  createdAt: number
  updatedAt: number
  publishedAt?: number
  viewCount: number
  likeCount: number
  commentCount: number
  isPinned: boolean
  isStarred: boolean
  permission: 'private' | 'team' | 'public'
}

// 知识库分类
export interface KnowledgeCategory {
  id: string
  name: string
  parentId?: string
  icon?: string
  color?: string
  documentCount: number
  children?: KnowledgeCategory[]
  sortOrder: number
}

// 搜索结果
export interface KnowledgeSearchResult {
  documents: KnowledgeDocument[]
  total: number
  page: number
  pageSize: number
  highlights?: { documentId: string; field: string; snippet: string }[]
}

// 创建文档参数
export interface CreateDocumentParams {
  title: string
  type: DocumentType
  parentId?: string
  content?: string
  tags?: string[]
  permission?: 'private' | 'team' | 'public'
}

// 更新文档参数
export interface UpdateDocumentParams {
  title?: string
  content?: string
  tags?: string[]
  status?: DocumentStatus
  permission?: 'private' | 'team' | 'public'
  isPinned?: boolean
}

// 收藏文档
export interface StarDocumentParams {
  documentId: string
}

/**
 * 知识库服务类
 * 封装知识库模块所有API调用
 */
export class KnowledgeService {
  private client: ApiClient

  constructor() {
    this.client = ApiClient
  }

  // ============ 文档管理 ============

  /**
   * 获取文档列表
   * @param params.parentId 父文件夹ID，undefined 表示根目录
   */
  async getDocuments(params?: { parentId?: string; page?: number; pageSize?: number; sortBy?: 'name' | 'updatedAt' | 'createdAt' }): Promise<ApiResponse<{ documents: KnowledgeDocument[]; total: number }>> {
    return this.client.get<{ documents: KnowledgeDocument[]; total: number }>('/api/v1/knowledge/documents', params as Record<string, any>)
  }

  /**
   * 获取文档详情
   * @param documentId 文档ID
   */
  async getDocument(documentId: string): Promise<ApiResponse<KnowledgeDocument>> {
    return this.client.get<KnowledgeDocument>(`/api/v1/knowledge/documents/${documentId}`)
  }

  /**
   * 创建文档
   * @param params 创建参数
   */
  async createDocument(params: CreateDocumentParams): Promise<ApiResponse<KnowledgeDocument>> {
    return this.client.post<KnowledgeDocument>('/api/v1/knowledge/documents', params)
  }

  /**
   * 创建文件夹
   * @param name 文件夹名称
   * @param parentId 父文件夹ID
   */
  async createFolder(name: string, parentId?: string): Promise<ApiResponse<KnowledgeDocument>> {
    return this.client.post<KnowledgeDocument>('/api/v1/knowledge/documents', {
      title: name,
      type: DocumentType.FOLDER,
      parentId
    })
  }

  /**
   * 更新文档
   * @param documentId 文档ID
   * @param params 更新参数
   */
  async updateDocument(documentId: string, params: UpdateDocumentParams): Promise<ApiResponse<KnowledgeDocument>> {
    return this.client.put<KnowledgeDocument>(`/api/v1/knowledge/documents/${documentId}`, params)
  }

  /**
   * 删除文档
   * @param documentId 文档ID
   */
  async deleteDocument(documentId: string): Promise<ApiResponse<any>> {
    return this.client.delete<any>(`/api/v1/knowledge/documents/${documentId}`)
  }

  /**
   * 移动文档
   * @param documentId 文档ID
   * @param targetParentId 目标父文件夹ID
   */
  async moveDocument(documentId: string, targetParentId?: string): Promise<ApiResponse<any>> {
    return this.client.put<any>(`/api/v1/knowledge/documents/${documentId}/move`, { parentId: targetParentId })
  }

  /**
   * 复制文档
   * @param documentId 文档ID
   * @param targetParentId 目标父文件夹ID
   */
  async copyDocument(documentId: string, targetParentId?: string): Promise<ApiResponse<any>> {
    return this.client.post<any>(`/api/v1/knowledge/documents/${documentId}/copy`, { parentId: targetParentId })
  }

  /**
   * 重命名文档
   * @param documentId 文档ID
   * @param newName 新名称
   */
  async renameDocument(documentId: string, newName: string): Promise<ApiResponse<any>> {
    return this.client.put<any>(`/api/v1/knowledge/documents/${documentId}/rename`, { title: newName })
  }

  // ============ 收藏与标签 ============

  /**
   * 收藏文档
   * @param documentId 文档ID
   */
  async starDocument(documentId: string): Promise<ApiResponse<any>> {
    return this.client.post<any>('/api/v1/knowledge/stars', { documentId })
  }

  /**
   * 取消收藏
   * @param documentId 文档ID
   */
  async unstarDocument(documentId: string): Promise<ApiResponse<any>> {
    return this.client.delete<any>(`/api/v1/knowledge/stars/${documentId}`)
  }

  /**
   * 获取收藏列表
   */
  async getStarredDocuments(params?: { page?: number; pageSize?: number }): Promise<ApiResponse<{ documents: KnowledgeDocument[]; total: number }>> {
    return this.client.get<{ documents: KnowledgeDocument[]; total: number }>('/api/v1/knowledge/stars', params as Record<string, any>)
  }

  /**
   * 添加标签
   * @param documentId 文档ID
   * @param tag 标签名
   */
  async addTag(documentId: string, tag: string): Promise<ApiResponse<any>> {
    return this.client.post<any>(`/api/v1/knowledge/documents/${documentId}/tags`, { tag })
  }

  /**
   * 移除标签
   * @param documentId 文档ID
   * @param tag 标签名
   */
  async removeTag(documentId: string, tag: string): Promise<ApiResponse<any>> {
    return this.client.delete<any>(`/api/v1/knowledge/documents/${documentId}/tags/${encodeURIComponent(tag)}`)
  }

  // ============ 搜索 ============

  /**
   * 搜索文档
   * @param keyword 关键词
   * @param params 其他搜索参数
   */
  async search(keyword: string, params?: { parentId?: string; tags?: string[]; type?: DocumentType; page?: number; pageSize?: number }): Promise<ApiResponse<KnowledgeSearchResult>> {
    return this.client.get<KnowledgeSearchResult>('/api/v1/knowledge/search', { keyword, ...params } as Record<string, any>)
  }

  /**
   * 全文搜索（高亮）
   * @param keyword 关键词
   */
  async fullTextSearch(keyword: string, params?: { page?: number; pageSize?: number }): Promise<ApiResponse<KnowledgeSearchResult>> {
    return this.client.get<KnowledgeSearchResult>('/api/v1/knowledge/search/fulltext', { keyword, ...params } as Record<string, any>)
  }

  // ============ 分类管理 ============

  /**
   * 获取分类列表
   * @param parentId 父分类ID，undefined 表示根分类
   */
  async getCategories(parentId?: string): Promise<ApiResponse<KnowledgeCategory[]>> {
    return this.client.get<KnowledgeCategory[]>('/api/v1/knowledge/categories', parentId ? { parentId } : undefined)
  }

  /**
   * 创建分类
   * @param name 分类名称
   * @param parentId 父分类ID
   * @param icon 图标
   */
  async createCategory(name: string, parentId?: string, icon?: string): Promise<ApiResponse<KnowledgeCategory>> {
    return this.client.post<KnowledgeCategory>('/api/v1/knowledge/categories', { name, parentId, icon })
  }

  /**
   * 更新分类
   * @param categoryId 分类ID
   * @param params 更新参数
   */
  async updateCategory(categoryId: string, params: { name?: string; icon?: string; sortOrder?: number }): Promise<ApiResponse<KnowledgeCategory>> {
    return this.client.put<KnowledgeCategory>(`/api/v1/knowledge/categories/${categoryId}`, params)
  }

  /**
   * 删除分类
   * @param categoryId 分类ID
   */
  async deleteCategory(categoryId: string): Promise<ApiResponse<any>> {
    return this.client.delete<any>(`/api/v1/knowledge/categories/${categoryId}`)
  }

  // ============ 互动功能 ============

  /**
   * 点赞文档
   * @param documentId 文档ID
   */
  async likeDocument(documentId: string): Promise<ApiResponse<any>> {
    return this.client.post<any>(`/api/v1/knowledge/documents/${documentId}/like`, {})
  }

  /**
   * 取消点赞
   * @param documentId 文档ID
   */
  async unlikeDocument(documentId: string): Promise<ApiResponse<any>> {
    return this.client.delete<any>(`/api/v1/knowledge/documents/${documentId}/like`)
  }

  /**
   * 获取文档评论列表
   * @param documentId 文档ID
   */
  async getComments(documentId: string, params?: { page?: number; pageSize?: number }): Promise<ApiResponse<any>> {
    return this.client.get<any>(`/api/v1/knowledge/documents/${documentId}/comments`, params as Record<string, any>)
  }

  /**
   * 添加评论
   * @param documentId 文档ID
   * @param content 评论内容
   * @param parentCommentId 父评论ID（回复）
   */
  async addComment(documentId: string, content: string, parentCommentId?: string): Promise<ApiResponse<any>> {
    return this.client.post<any>(`/api/v1/knowledge/documents/${documentId}/comments`, { content, parentCommentId })
  }

  /**
   * 删除评论
   * @param documentId 文档ID
   * @param commentId 评论ID
   */
  async deleteComment(documentId: string, commentId: string): Promise<ApiResponse<any>> {
    return this.client.delete<any>(`/api/v1/knowledge/documents/${documentId}/comments/${commentId}`)
  }

  // ============ 最近访问与推荐 ============

  /**
   * 获取最近访问的文档
   */
  async getRecentDocuments(params?: { page?: number; pageSize?: number }): Promise<ApiResponse<{ documents: KnowledgeDocument[]; total: number }>> {
    return this.client.get<{ documents: KnowledgeDocument[]; total: number }>('/api/v1/knowledge/recent', params as Record<string, any>)
  }

  /**
   * 获取推荐文档
   */
  async getRecommendedDocuments(params?: { page?: number; pageSize?: number }): Promise<ApiResponse<{ documents: KnowledgeDocument[]; total: number }>> {
    return this.client.get<{ documents: KnowledgeDocument[]; total: number }>('/api/v1/knowledge/recommended', params as Record<string, any>)
  }

  /**
   * 获取我的文档（我创建的）
   */
  async getMyDocuments(params?: { status?: DocumentStatus; page?: number; pageSize?: number }): Promise<ApiResponse<{ documents: KnowledgeDocument[]; total: number }>> {
    return this.client.get<{ documents: KnowledgeDocument[]; total: number }>('/api/v1/knowledge/my', params as Record<string, any>)
  }
}

// 导出服务实例
export default new KnowledgeService()
