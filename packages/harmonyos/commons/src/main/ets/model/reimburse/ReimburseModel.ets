/**
 * Reimburse Data Models
 */
export interface ReimburseInfo {
  id: string;
  title: string;
  amount: number;
  applicant: string;
  status: string;
  createTime: string;
  updateTime: string;
  description?: string;
  invoiceCount?: number;
}

export interface ReimburseStats {
  pendingAmount: number;
  pendingCount: number;
  monthTotal: number;
  invoiceCount: number;
}

export interface ReimburseQueryParams {
  status?: string;
  keyword?: string;
  dateRange?: string[];
}

export interface ReimburseCreateParams {
  title: string;
  amount: number;
  description: string;
  invoiceIds?: string[];
}