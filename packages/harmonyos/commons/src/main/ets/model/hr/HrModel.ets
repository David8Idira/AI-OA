/**
 * HR Data Models - 人力资源相关数据模型
 */
export interface EmployeeInfo {
  id: string;
  employeeNo: string;
  name: string;
  phone: string;
  email: string;
  departmentId: string;
  departmentName: string;
  position: string;
  employeeStatus: string;
  status: number;
  entryDate: string;
}

export interface DepartmentInfo {
  id: string;
  departmentCode: string;
  departmentName: string;
  parentId: string;
  manager: string;
  level: number;
  status: number;
}

export interface EmployeeQueryParams {
  pageNum: number;
  pageSize: number;
  employeeNo?: string;
  name?: string;
  phone?: string;
  departmentId?: string;
  employeeStatus?: string;
  status?: string;
}

export interface DepartmentQueryParams {
  pageNum: number;
  pageSize: number;
  departmentCode?: string;
  departmentName?: string;
  manager?: string;
  status?: string;
}