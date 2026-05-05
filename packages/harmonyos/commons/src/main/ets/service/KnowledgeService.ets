/**
 * Knowledge Service - 知识库服务
 */
import { KnowledgeDoc, KnowledgeStats, KnowledgeQueryParams, SearchResult } from '../model/knowledge/KnowledgeModel';

export class KnowledgeService {
  private static instance: KnowledgeService = new KnowledgeService();

  static getInstance(): KnowledgeService {
    return this.instance;
  }

  /**
   * 获取文档列表
   */
  getDocumentList(params: KnowledgeQueryParams): Promise<KnowledgeDoc[]> {
    return new Promise((resolve) => {
      setTimeout(() => {
        const mockData: KnowledgeDoc[] = [
          {
            id: '1',
            title: '员工手册 - 考勤管理制度',
            content: '公司实行打卡制度，员工应在规定时间上下班打卡...',
            summary: '详细说明了公司考勤管理的各项规定',
            docType: 'policy',
            category: '公司制度',
            viewCount: 1250,
            likeCount: 45,
            commentCount: 12,
            createTime: '2024-01-15',
            updateTime: '2024-04-20',
            author: '人力资源部'
          },
          {
            id: '2',
            title: 'AI-OA产品功能介绍',
            content: 'AI-OA是一款基于人工智能的办公自动化系统...',
            summary: '介绍AI-OA系统的核心功能和使用方法',
            docType: 'product',
            category: '产品文档',
            viewCount: 890,
            likeCount: 32,
            commentCount: 8,
            createTime: '2024-02-10',
            updateTime: '2024-04-15',
            author: '产品部'
          },
          {
            id: '3',
            title: 'HarmonyOS开发规范',
            content: '本文档介绍了HarmonyOS应用开发的编码规范...',
            summary: 'HarmonyOS开发团队内部技术规范',
            docType: 'tech',
            category: '技术文档',
            viewCount: 560,
            likeCount: 28,
            commentCount: 5,
            createTime: '2024-03-01',
            updateTime: '2024-04-10',
            author: '技术部'
          },
          {
            id: '4',
            title: '新员工培训资料',
            content: '欢迎加入我们的团队，本培训将帮助您快速了解公司...',
            summary: '新入职员工必读培训材料',
            docType: 'training',
            category: '培训资料',
            viewCount: 2100,
            likeCount: 78,
            commentCount: 23,
            createTime: '2024-01-05',
            updateTime: '2024-03-20',
            author: '人力资源部'
          },
          {
            id: '5',
            title: '常见问题FAQ',
            content: 'Q: 如何申请年假？A: 年假申请需要提前3天在OA系统提交...',
            summary: '收集了员工常见问题和解答',
            docType: 'faq',
            category: '常见问题',
            viewCount: 3200,
            likeCount: 95,
            commentCount: 42,
            createTime: '2024-01-10',
            updateTime: '2024-04-25',
            author: '行政部'
          }
        ];

        // 简单过滤
        let filtered = mockData;
        if (params.category) {
          filtered = filtered.filter(d => d.category === params.category);
        }

        resolve(filtered);
      }, 800);
    });
  }

  /**
   * 获取知识库统计
   */
  getKnowledgeStats(): Promise<KnowledgeStats> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          totalDocs: 156,
          totalViews: 45000,
          totalLikes: 1200,
          totalComments: 350
        });
      }, 500);
    });
  }

  /**
   * 获取文档详情
   */
  getDocumentDetail(id: string): Promise<KnowledgeDoc> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          id: id,
          title: '员工手册 - 考勤管理制度',
          content: '公司实行打卡制度，员工应在规定时间上下班打卡。上班时间：9:00，下班时间：18:00中午休息时间：12:00-13:00。\n\n一、考勤规定\n1. 员工必须按时打卡上下班\n2. 迟到超过30分钟视为旷工\n3. 因公外出需提前报备\n\n二、请假制度\n1. 年假：入职满1年享5天\n2. 病假：需提供医院证明\n3. 事假：需提前3天申请\n\n三、加班管理\n1. 加班需部门经理批准\n2. 加班可选择调休或加班费',
          summary: '详细说明了公司考勤管理的各项规定',
          docType: 'policy',
          category: '公司制度',
          viewCount: 1250,
          likeCount: 45,
          commentCount: 12,
          createTime: '2024-01-15',
          updateTime: '2024-04-20',
          author: '人力资源部'
        });
      }, 500);
    });
  }

  /**
   * 搜索文档
   */
  searchDocuments(params: KnowledgeQueryParams): Promise<SearchResult[]> {
    return new Promise((resolve) => {
      setTimeout(() => {
        const mockData: KnowledgeDoc[] = [
          {
            id: '1',
            title: '员工手册 - 考勤管理制度',
            content: '公司实行打卡制度...',
            docType: 'policy',
            category: '公司制度',
            viewCount: 1250,
            likeCount: 45,
            commentCount: 12,
            createTime: '2024-01-15',
            updateTime: '2024-04-20',
            author: '人力资源部'
          }
        ];

        resolve(mockData.map(doc => ({
          doc: doc,
          relevanceScore: 0.95,
          matchedContent: '...' + params.keyword + '...'
        })));
      }, 800);
    });
  }

  /**
   * 语义搜索
   */
  semanticSearch(query: string): Promise<SearchResult[]> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve([]);
      }, 1000);
    });
  }
}

export const knowledgeService = KnowledgeService.getInstance();