// 知识库模型

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
