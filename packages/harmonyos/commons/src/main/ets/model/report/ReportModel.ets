/**
 * Report Data Models
 */
export interface ReportInfo {
  id: string;
  name: string;
  type: string;
  period: string;
  status: 'completed' | 'generating';
  createTime: string;
  updateTime: string;
}

export interface ReportStats {
  monthTotal: number;
  pendingCount: number;
  reimburseCount: number;
  aiReportCount: number;
}

export interface ReportCreateParams {
  name: string;
  type: string;
  period: string;
  dateRange?: string[];
}