/**
 * Report Service - 报表服务
 */
import { ReportInfo, ReportStats, ReportCreateParams } from '../model/report/ReportModel';

export class ReportService {
  private static instance: ReportService = new ReportService();

  static getInstance(): ReportService {
    return this.instance;
  }

  /**
   * 获取报表列表
   */
  getReportList(): Promise<ReportInfo[]> {
    return new Promise((resolve) => {
      setTimeout(() => {
        const mockData: ReportInfo[] = [
          { id: '1', name: '4月份财务汇总报告', type: '月度报告', period: '2024-04', status: 'completed', createTime: '2024-04-30 10:30', updateTime: '2024-04-30 10:35' },
          { id: '2', name: '报销统计报表', type: '统计报表', period: '2024-04', status: 'completed', createTime: '2024-04-28 14:20', updateTime: '2024-04-28 14:25' },
          { id: '3', name: 'AI智能分析报告', type: 'AI报告', period: '2024-04', status: 'generating', createTime: '2024-04-25 09:00', updateTime: '-' },
          { id: '4', name: '部门费用分析', type: '分析报告', period: '2024-Q1', status: 'completed', createTime: '2024-03-31 16:00', updateTime: '2024-03-31 16:10' }
        ];
        resolve(mockData);
      }, 800);
    });
  }

  /**
   * 获取报表统计
   */
  getReportStats(): Promise<ReportStats> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          monthTotal: 45680,
          pendingCount: 5,
          reimburseCount: 32,
          aiReportCount: 12
        });
      }, 500);
    });
  }

  /**
   * 创建报表
   */
  createReport(params: ReportCreateParams): Promise<string> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve('report-' + Date.now());
      }, 1000);
    });
  }

  /**
   * 获取报表详情
   */
  getReportDetail(id: string): Promise<ReportInfo> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          id: id,
          name: '报表详情',
          type: '月度报告',
          period: '2024-04',
          status: 'completed',
          createTime: '2024-04-30 10:30',
          updateTime: '2024-04-30 10:35'
        });
      }, 500);
    });
  }
}

export const reportService = ReportService.getInstance();