/**
 * HR Service - 人力资源服务
 */
import { EmployeeInfo, DepartmentInfo, EmployeeQueryParams, DepartmentQueryParams } from '../model/hr/HrModel';
import ApiClient from '../data/api/ApiClient';

export class HrService {
  private static instance: HrService = new HrService();

  static getInstance(): HrService {
    return this.instance;
  }

  async getEmployeeList(params: EmployeeQueryParams): Promise<{ records: EmployeeInfo[], total: number }> {
    const res = await ApiClient.get<{ records: EmployeeInfo[], total: number }>('/api/v1/hr/employees', params as Record<string, any>);
    return res.data;
  }

  async getDepartmentList(): Promise<DepartmentInfo[]> {
    const res = await ApiClient.get<{ departments: DepartmentInfo[] }>('/api/v1/hr/departments');
    return res.data.departments;
  }

  async getDepartmentDetail(id: string): Promise<DepartmentInfo> {
    const res = await ApiClient.get<DepartmentInfo>(`/api/v1/hr/departments/${id}`);
    return res.data;
  }

  async getEmployeeDetail(id: string): Promise<EmployeeInfo> {
    const res = await ApiClient.get<EmployeeInfo>(`/api/v1/hr/employees/${id}`);
    return res.data;
  }
}

export const hrService = HrService.getInstance();