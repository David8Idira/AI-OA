/**
 * HR Service - 人力资源服务
 */
import { EmployeeInfo, DepartmentInfo, EmployeeQueryParams, DepartmentQueryParams } from '../model/hr/HrModel';

export class HrService {
  private static instance: HrService = new HrService();

  static getInstance(): HrService {
    return this.instance;
  }

  /**
   * 获取员工列表
   */
  getEmployeeList(params: EmployeeQueryParams): Promise<{ records: EmployeeInfo[], total: number }> {
    return new Promise((resolve) => {
      setTimeout(() => {
        const mockData: EmployeeInfo[] = [
          {
            id: '1',
            employeeNo: 'EMP001',
            name: '张三',
            phone: '13800138001',
            email: 'zhangsan@company.com',
            departmentId: 'D001',
            departmentName: '技术部',
            position: '高级工程师',
            employeeStatus: '2',
            status: 1,
            entryDate: '2022-01-15'
          },
          {
            id: '2',
            employeeNo: 'EMP002',
            name: '李四',
            phone: '13800138002',
            email: 'lisi@company.com',
            departmentId: 'D002',
            departmentName: '产品部',
            position: '产品经理',
            employeeStatus: '2',
            status: 1,
            entryDate: '2021-06-20'
          },
          {
            id: '3',
            employeeNo: 'EMP003',
            name: '王五',
            phone: '13800138003',
            email: 'wangwu@company.com',
            departmentId: 'D003',
            departmentName: '市场部',
            position: '市场专员',
            employeeStatus: '1',
            status: 1,
            entryDate: '2024-03-01'
          }
        ];
        resolve({ records: mockData, total: mockData.length });
      }, 800);
    });
  }

  /**
   * 获取部门列表
   */
  getDepartmentList(): Promise<DepartmentInfo[]> {
    return new Promise((resolve) => {
      setTimeout(() => {
        const mockData: DepartmentInfo[] = [
          { id: 'D001', departmentCode: 'DEPT001', departmentName: '技术部', parentId: '', manager: '赵总', level: 1, status: 1 },
          { id: 'D002', departmentCode: 'DEPT002', departmentName: '产品部', parentId: '', manager: '钱总', level: 1, status: 1 },
          { id: 'D003', departmentCode: 'DEPT003', departmentName: '市场部', parentId: '', manager: '孙总', level: 1, status: 1 },
          { id: 'D004', departmentCode: 'DEPT004', departmentName: '人力资源部', parentId: '', manager: '周总', level: 1, status: 1 }
        ];
        resolve(mockData);
      }, 500);
    });
  }

  /**
   * 获取部门详情
   */
  getDepartmentDetail(id: string): Promise<DepartmentInfo> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          id: id,
          departmentCode: 'DEPT001',
          departmentName: '技术部',
          parentId: '',
          manager: '赵总',
          level: 1,
          status: 1
        });
      }, 500);
    });
  }

  /**
   * 获取员工详情
   */
  getEmployeeDetail(id: string): Promise<EmployeeInfo> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          id: id,
          employeeNo: 'EMP001',
          name: '张三',
          phone: '13800138001',
          email: 'zhangsan@company.com',
          departmentId: 'D001',
          departmentName: '技术部',
          position: '高级工程师',
          employeeStatus: '2',
          status: 1,
          entryDate: '2022-01-15'
        });
      }, 500);
    });
  }
}

export const hrService = HrService.getInstance();