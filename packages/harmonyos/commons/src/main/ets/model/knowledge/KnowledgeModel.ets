/**
 * Knowledge Data Models
 */
export interface KnowledgeDoc {
  id: string;
  title: string;
  content: string;
  summary?: string;
  docType: string;
  category: string;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  createTime: string;
  updateTime: string;
  author: string;
}

export interface KnowledgeStats {
  totalDocs: number;
  totalViews: number;
  totalLikes: number;
  totalComments: number;
}

export interface KnowledgeQueryParams {
  keyword?: string;
  category?: string;
  docType?: string;
  isSemantic?: boolean;
}

export interface SearchResult {
  doc: KnowledgeDoc;
  relevanceScore: number;
  matchedContent: string;
}