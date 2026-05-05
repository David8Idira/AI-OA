/**
 * Employee List Page - 员工列表页面
 */
import router from '@ohos.router';
import { EmployeeInfo, DepartmentInfo } from '../../model/hr/HrModel';
import { hrService } from '../../service/HrService';

@Component
export struct EmployeeList {
  @State employees: EmployeeInfo[] = [];
  @State departments: DepartmentInfo[] = [];
  @State loading: boolean = false;
  @State currentPage: number = 1;
  @State pageSize: number = 10;
  @State total: number = 0;

  @State filterNo: string = '';
  @State filterName: string = '';
  @State filterPhone: string = '';
  @State filterDepartment: string = '';
  @State filterStatus: string = '';
  @State filterEmployeeStatus: string = '';

  private scroller: Scroller = new Scroller();

  aboutToAppear() {
    this.loadEmployees();
    this.loadDepartments();
  }

  loadEmployees() {
    this.loading = true;
    hrService.getEmployeeList({
      pageNum: this.currentPage,
      pageSize: this.pageSize,
      employeeNo: this.filterNo,
      name: this.filterName,
      phone: this.filterPhone,
      departmentId: this.filterDepartment,
      employeeStatus: this.filterEmployeeStatus,
      status: this.filterStatus
    }).then((result) => {
      this.employees = result.records;
      this.total = result.total;
    }).catch(() => {
      // 处理错误
    }).finally(() => {
      this.loading = false;
    });
  }

  loadDepartments() {
    hrService.getDepartmentList().then((depts) => {
      this.departments = depts;
    }).catch(() => {
      // 处理错误
    });
  }

  build() {
    Column() {
      // 顶部操作栏
      this.TopActions()

      // 筛选区域
      this.FilterArea()

      // 员工列表
      this.EmployeeListContent()
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F7FA')
  }

  @Builder
  TopActions() {
    Row() {
      Text('员工管理')
        .fontSize(18)
        .fontWeight(FontWeight.Bold)

      Blank()

      Button('新增')
        .height(32)
        .fontSize(14)
        .backgroundColor('#667EEA')
        .borderRadius(16)
        .onClick(() => {
          // 新增员工
        })

      Button('导出')
        .height(32)
        .fontSize(14)
        .backgroundColor('#FFFFFF')
        .fontColor('#667EEA')
        .border({ width: 1, color: '#667EEA', radius: 16 })
        .margin({ left: 8 })
        .onClick(() => {
          // 导出
        })

      Button('刷新')
        .height(32)
        .fontSize(14)
        .backgroundColor('#FFFFFF')
        .fontColor('#666666')
        .border({ width: 1, color: '#E8E8E8', radius: 16 })
        .onClick(() => {
          this.loadEmployees();
        })
    }
    .width('100%')
    .height(56)
    .padding({ left: 16, right: 16 })
    .backgroundColor('#FFFFFF')
  }

  @Builder
  FilterArea() {
    Column() {
      Row() {
        TextInput({ placeholder: '员工编号' })
          .width('22%')
          .height(36)
          .borderRadius(8)
          .backgroundColor('#F5F7FA')
          .onChange((value: string) => {
            this.filterNo = value;
          })

        TextInput({ placeholder: '员工姓名' })
          .width('22%')
          .height(36)
          .borderRadius(8)
          .backgroundColor('#F5F7FA')
          .margin({ left: 8 })
          .onChange((value: string) => {
            this.filterName = value;
          })

        TextInput({ placeholder: '手机号' })
          .width('22%')
          .height(36)
          .borderRadius(8)
          .backgroundColor('#F5F7FA')
          .margin({ left: 8 })
          .onChange((value: string) => {
            this.filterPhone = value;
          })

        Select(this.departments.map(d => d.departmentName))
          .width('20%')
          .height(36)
          .borderRadius(8)
          .backgroundColor('#F5F7FA')
          .margin({ left: 8 })
          .onSelect((index: number) => {
            this.filterDepartment = this.departments[index]?.id || '';
          })
      }
      .width('100%')

      Row() {
        Select(['全部', '试用', '正式', '离职'].map(s => s))
          .width('22%')
          .height(36)
          .borderRadius(8)
          .backgroundColor('#F5F7FA')
          .onSelect((index: number) => {
            this.filterEmployeeStatus = index === 0 ? '' : ['1', '2', '3'][index - 1];
          })

        Select(['全部', '启用', '禁用'].map(s => s))
          .width('22%')
          .height(36)
          .borderRadius(8)
          .backgroundColor('#F5F7FA')
          .margin({ left: 8 })
          .onSelect((index: number) => {
            this.filterStatus = index === 0 ? '' : ['1', '0'][index - 1];
          })

        Blank()

        Button('查询')
          .height(36)
          .width(80)
          .backgroundColor('#667EEA')
          .borderRadius(8)
          .onClick(() => {
            this.currentPage = 1;
            this.loadEmployees();
          })

        Button('重置')
          .height(36)
          .width(80)
          .backgroundColor('#FFFFFF')
          .fontColor('#666666')
          .border({ width: 1, color: '#E8E8E8', radius: 8 })
          .margin({ left: 8 })
          .onClick(() => {
            this.filterNo = '';
            this.filterName = '';
            this.filterPhone = '';
            this.filterDepartment = '';
            this.filterEmployeeStatus = '';
            this.filterStatus = '';
            this.currentPage = 1;
            this.loadEmployees();
          })
      }
      .width('100%')
      .margin({ top: 12 })
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .margin({ bottom: 12 })
  }

  @Builder
  EmployeeListContent() {
    Column() {
      if (this.loading) {
        LoadingProgress()
          .width(40)
          .height(40)
          .color('#667EEA')
      } else {
        List(this.scroller) {
          ForEach(this.employees, (employee: EmployeeInfo) => {
            ListItem() {
              this.EmployeeCard(employee)
            }
          })
        }
        .width('100%')
        .layoutWeight(1)
        .scrollBar(BarState.Auto)
        .edgeEffect(EdgeEffect.Spring)

        this.Pagination()
      }
    }
    .width('100%')
    .layoutWeight(1)
    .padding({ left: 16, right: 16 })
  }

  @Builder
  EmployeeCard(employee: EmployeeInfo) {
    Row() {
      // 头像
      Column() {
        Text(employee.name?.substring(0, 1) || '?')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor('#FFFFFF')
          .width(48)
          .height(48)
          .backgroundColor('#667EEA')
          .borderRadius(24)
          .textAlign(TextAlign.Center)
      }
      .width(60)

      Column() {
        Row() {
          Text(employee.name || '-')
            .fontSize(16)
            .fontWeight(FontWeight.Medium)

          Text(employee.employeeNo || '')
            .fontSize(12)
            .fontColor('#999999')
            .margin({ left: 12 })
        }
        .width('100%')

        Row() {
          Text(employee.departmentName || '-')
            .fontSize(12)
            .fontColor('#666666')

          Text(employee.phone || '-')
            .fontSize(12)
            .fontColor('#666666')
            .margin({ left: 16 })
        }
        .margin({ top: 6 })
        .width('100%')

        Row() {
          Text(this.getEmployeeStatusText(employee.employeeStatus))
            .fontSize(12)
            .fontColor(this.getEmployeeStatusColor(employee.employeeStatus))
            .backgroundColor(this.getEmployeeStatusBgColor(employee.employeeStatus))
            .padding({ left: 8, right: 8, top: 2, bottom: 2 })
            .borderRadius(4)
            .margin({ right: 8 })

          if (employee.status === 1) {
            Text('已启用')
              .fontSize(12)
              .fontColor('#67C23A')
              .backgroundColor('#F0F9EB')
              .padding({ left: 8, right: 8, top: 2, bottom: 2 })
              .borderRadius(4)
          }
        }
        .margin({ top: 6 })
      }
      .layoutWeight(1)
      .padding({ left: 12 })

      Image($r('app.media.ic_arrow_right'))
        .width(20)
        .height(20)
        .opacity(0.5)
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
    .onClick(() => {
      // 查看员工详情
    })
  }

  @Builder
  Pagination() {
    Row() {
      Text(`共 ${this.total} 条`)
        .fontSize(12)
        .fontColor('#999999')
        .layoutWeight(1)

      Button('上一页')
        .height(32)
        .fontSize(12)
        .enabled(this.currentPage > 1)
        .onClick(() => {
          this.currentPage--;
          this.loadEmployees();
        })

      Text(`${this.currentPage}/${Math.ceil(this.total / this.pageSize)}`)
        .fontSize(12)
        .margin({ left: 12, right: 12 })

      Button('下一页')
        .height(32)
        .fontSize(12)
        .enabled(this.currentPage < Math.ceil(this.total / this.pageSize))
        .onClick(() => {
          this.currentPage++;
          this.loadEmployees();
        })
    }
    .width('100%')
    .height(48)
    .padding({ left: 16, right: 16 })
    .alignItems(VerticalAlign.Center)
    .backgroundColor('#FFFFFF')
    .margin({ top: 12 })
  }

  getEmployeeStatusText(status: string): string {
    const map: Record<string, string> = { '1': '试用', '2': '正式', '3': '离职' };
    return map[status] || '未知';
  }

  getEmployeeStatusColor(status: string): string {
    const map: Record<string, string> = { '1': '#E6A23C', '2': '#67C23A', '3': '#909399' };
    return map[status] || '#999999';
  }

  getEmployeeStatusBgColor(status: string): string {
    const map: Record<string, string> = { '1': '#FDF6EC', '2': '#F0F9EB', '3': '#F4F4F5' };
    return map[status] || '#F5F7FA';
  }
}