/**
 * Knowledge Service - 知识库服务
 */
import { KnowledgeDoc, KnowledgeStats, KnowledgeQueryParams, SearchResult } from '../model/knowledge/KnowledgeModel';
import ApiClient from '../data/api/ApiClient';

export class KnowledgeService {
  private static instance: KnowledgeService = new KnowledgeService();

  static getInstance(): KnowledgeService {
    return this.instance;
  }

  async getDocumentList(params: KnowledgeQueryParams): Promise<KnowledgeDoc[]> {
    const res = await ApiClient.get<{ documents: KnowledgeDoc[] }>('/api/v1/knowledge/documents', params as Record<string, any>);
    return res.data.documents;
  }

  async getKnowledgeStats(): Promise<KnowledgeStats> {
    const res = await ApiClient.get<KnowledgeStats>('/api/v1/knowledge/stats');
    return res.data;
  }

  async getDocumentDetail(id: string): Promise<KnowledgeDoc> {
    const res = await ApiClient.get<KnowledgeDoc>(`/api/v1/knowledge/documents/${id}`);
    return res.data;
  }

  async searchDocuments(params: KnowledgeQueryParams): Promise<SearchResult[]> {
    const res = await ApiClient.get<SearchResult[]>('/api/v1/knowledge/search', params as Record<string, any>);
    return res.data;
  }

  async semanticSearch(query: string): Promise<SearchResult[]> {
    const res = await ApiClient.post<SearchResult[]>('/api/v1/knowledge/semantic-search', { query });
    return res.data;
  }
}

export const knowledgeService = KnowledgeService.getInstance();