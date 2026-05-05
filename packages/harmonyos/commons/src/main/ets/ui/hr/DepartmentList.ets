/**
 * Department List Page - 部门列表页面
 */
import router from '@ohos.router';
import { DepartmentInfo } from '../../model/hr/HrModel';
import { hrService } from '../../service/HrService';

@Component
export struct DepartmentList {
  @State departments: DepartmentInfo[] = [];
  @State loading: boolean = false;
  @State currentPage: number = 1;
  @State pageSize: number = 10;
  @State total: number = 0;

  @State filterCode: string = '';
  @State filterName: string = '';
  @State filterManager: string = '';
  @State filterStatus: string = '';

  private scroller: Scroller = new Scroller();

  aboutToAppear() {
    this.loadDepartments();
  }

  loadDepartments() {
    this.loading = true;
    hrService.getDepartmentList().then((depts) => {
      this.departments = depts;
      this.total = depts.length;
    }).catch(() => {
      // 处理错误
    }).finally(() => {
      this.loading = false;
    });
  }

  build() {
    Column() {
      // 顶部操作栏
      this.TopActions()

      // 筛选区域
      this.FilterArea()

      // 部门列表
      this.DepartmentListContent()
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F7FA')
  }

  @Builder
  TopActions() {
    Row() {
      Text('部门管理')
        .fontSize(18)
        .fontWeight(FontWeight.Bold)

      Blank()

      Button('新增')
        .height(32)
        .fontSize(14)
        .backgroundColor('#667EEA')
        .borderRadius(16)
        .onClick(() => {
          // 新增部门
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
          this.loadDepartments();
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
        TextInput({ placeholder: '部门编码' })
          .width('28%')
          .height(36)
          .borderRadius(8)
          .backgroundColor('#F5F7FA')
          .onChange((value: string) => {
            this.filterCode = value;
          })

        TextInput({ placeholder: '部门名称' })
          .width('28%')
          .height(36)
          .borderRadius(8)
          .backgroundColor('#F5F7FA')
          .margin({ left: 8 })
          .onChange((value: string) => {
            this.filterName = value;
          })

        TextInput({ placeholder: '负责人' })
          .width('22%')
          .height(36)
          .borderRadius(8)
          .backgroundColor('#F5F7FA')
          .margin({ left: 8 })
          .onChange((value: string) => {
            this.filterManager = value;
          })

        Select(['全部', '启用', '禁用'].map(s => s))
          .width('15%')
          .height(36)
          .borderRadius(8)
          .backgroundColor('#F5F7FA')
          .margin({ left: 8 })
          .onSelect((index: number) => {
            this.filterStatus = index === 0 ? '' : ['1', '0'][index - 1];
          })
      }
      .width('100%')

      Row() {
        Blank()

        Button('查询')
          .height(36)
          .width(80)
          .backgroundColor('#667EEA')
          .borderRadius(8)
          .onClick(() => {
            this.currentPage = 1;
            this.loadDepartments();
          })

        Button('重置')
          .height(36)
          .width(80)
          .backgroundColor('#FFFFFF')
          .fontColor('#666666')
          .border({ width: 1, color: '#E8E8E8', radius: 8 })
          .margin({ left: 8 })
          .onClick(() => {
            this.filterCode = '';
            this.filterName = '';
            this.filterManager = '';
            this.filterStatus = '';
            this.loadDepartments();
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
  DepartmentListContent() {
    Column() {
      if (this.loading) {
        LoadingProgress()
          .width(40)
          .height(40)
          .color('#667EEA')
      } else {
        List(this.scroller) {
          ForEach(this.departments, (dept: DepartmentInfo) => {
            ListItem() {
              this.DepartmentCard(dept)
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
  DepartmentCard(dept: DepartmentInfo) {
    Row() {
      Column() {
        Row() {
          Text(dept.departmentCode)
            .fontSize(12)
            .fontColor('#999999')

          Text(`${dept.level}级`)
            .fontSize(12)
            .fontColor('#667EEA')
            .backgroundColor('#ECF5FF')
            .padding({ left: 6, right: 6, top: 2, bottom: 2 })
            .borderRadius(4)
            .margin({ left: 8 })
        }

        Text(dept.departmentName)
          .fontSize(16)
          .fontWeight(FontWeight.Medium)
          .margin({ top: 8 })

        Row() {
          Text(`负责人: ${dept.manager || '-'}`)
            .fontSize(12)
            .fontColor('#666666')

          if (dept.parentId) {
            Text('有上级部门')
              .fontSize(12)
              .fontColor('#E6A23C')
              .backgroundColor('#FDF6EC')
              .padding({ left: 6, right: 6, top: 2, bottom: 2 })
              .borderRadius(4)
              .margin({ left: 12 })
          }
        }
        .margin({ top: 6 })

        Row() {
          Text(dept.status === 1 ? '已启用' : '已禁用')
            .fontSize(12)
            .fontColor(dept.status === 1 ? '#67C23A' : '#909399')
            .backgroundColor(dept.status === 1 ? '#F0F9EB' : '#F4F4F5')
            .padding({ left: 8, right: 8, top: 2, bottom: 2 })
            .borderRadius(4)

          Blank()

          Text('查看详情 >')
            .fontSize(12)
            .fontColor('#667EEA')
        }
        .margin({ top: 8 })
      }
      .layoutWeight(1)
      .width('100%')
      .padding(16)
      .backgroundColor('#FFFFFF')
      .borderRadius(12)
      .margin({ bottom: 12 })
    }
    .width('100%')
    .onClick(() => {
      // 查看部门详情
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
          this.loadDepartments();
        })

      Text(`${this.currentPage}/${Math.ceil(this.total / this.pageSize) || 1}`)
        .fontSize(12)
        .margin({ left: 12, right: 12 })

      Button('下一页')
        .height(32)
        .fontSize(12)
        .enabled(this.currentPage < Math.ceil(this.total / this.pageSize))
        .onClick(() => {
          this.currentPage++;
          this.loadDepartments();
        })
    }
    .width('100%')
    .height(48)
    .padding({ left: 16, right: 16 })
    .alignItems(VerticalAlign.Center)
    .backgroundColor('#FFFFFF')
    .margin({ top: 12 })
  }
}