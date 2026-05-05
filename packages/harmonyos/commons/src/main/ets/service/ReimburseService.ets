/**
 * Reimburse Service - 报销服务
 */
import { ReimburseInfo, ReimburseStats, ReimburseQueryParams, ReimburseCreateParams } from '../model/reimburse/ReimburseModel';

export class ReimburseService {
  private static instance: ReimburseService = new ReimburseService();

  static getInstance(): ReimburseService {
    return this.instance;
  }

  /**
   * 获取报销列表
   */
  getReimburseList(params: ReimburseQueryParams): Promise<ReimburseInfo[]> {
    return new Promise((resolve) => {
      setTimeout(() => {
        const mockData: ReimburseInfo[] = [
          { id: '1', title: '4月份差旅费报销', amount: 5680.00, applicant: '张三', status: 'PENDING', createTime: '2024-04-28', updateTime: '2024-04-28', description: '北京出差费用', invoiceCount: 5 },
          { id: '2', title: '办公用品采购', amount: 1250.00, applicant: '李四', status: 'APPROVED', createTime: '2024-04-25', updateTime: '2024-04-26', description: '采购办公用品', invoiceCount: 2 },
          { id: '3', title: '客户招待费', amount: 3500.00, applicant: '王五', status: 'PAID', createTime: '2024-04-20', updateTime: '2024-04-22', description: '客户招待', invoiceCount: 3 },
          { id: '4', title: '交通费用报销', amount: 890.00, applicant: '赵六', status: 'DRAFT', createTime: '2024-04-30', updateTime: '-', description: '市内交通', invoiceCount: 8 },
          { id: '5', title: '团建活动费用', amount: 4200.00, applicant: '钱七', status: 'REJECTED', createTime: '2024-04-15', updateTime: '2024-04-18', description: '部门团建', invoiceCount: 1 }
        ];

        // 简单过滤
        let filtered = mockData;
        if (params.status) {
          filtered = filtered.filter(r => r.status === params.status);
        }
        if (params.keyword) {
          filtered = filtered.filter(r => r.title.includes(params.keyword) || r.applicant.includes(params.keyword));
        }

        resolve(filtered);
      }, 800);
    });
  }

  /**
   * 获取报销统计
   */
  getReimburseStats(): Promise<ReimburseStats> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          pendingAmount: 12580,
          pendingCount: 5,
          monthTotal: 45680,
          invoiceCount: 32
        });
      }, 500);
    });
  }

  /**
   * 创建报销单
   */
  createReimburse(params: ReimburseCreateParams): Promise<string> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve('reimburse-' + Date.now());
      }, 1000);
    });
  }

  /**
   * 获取报销详情
   */
  getReimburseDetail(id: string): Promise<ReimburseInfo> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          id: id,
          title: '报销详情',
          amount: 5680.00,
          applicant: '张三',
          status: 'PENDING',
          createTime: '2024-04-28',
          updateTime: '2024-04-28',
          description: '报销详情描述'
        });
      }, 500);
    });
  }
}

export const reimburseService = ReimburseService.getInstance();