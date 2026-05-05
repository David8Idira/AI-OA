/**
 * Report Create Page - 创建报表页面
 */
import router from '@ohos.router';
import { reportService } from '../../service/ReportService';

@Component
export struct ReportCreate {
  @State reportName: string = '';
  @State selectedType: string = '';
  @State selectedPeriod: string = '';
  @State dateRange: string[] = ['', ''];
  @State isCreating: boolean = false;
  @State showSuccessDialog: boolean = false;

  private reportTypes = ['月度报告', '统计报表', 'AI报告', '分析报告'];
  private periods = ['2024-01', '2024-02', '2024-03', '2024-04', '2024-Q1'];

  build() {
    Column() {
      // 顶部导航栏
      this.NavigationBar()

      Scroll() {
        Column() {
          // 报表名称
          this.ReportNameSection()

          // 报表类型
          this.TypeSection()

          // 报表周期
          this.PeriodSection()

          // 日期范围
          this.DateRangeSection()

          // 创建按钮
          this.CreateButton()
        }
        .width('100%')
        .padding(16)
      }
      .layoutWeight(1)
      .scrollBar(BarState.Auto)
      .edgeEffect(EdgeEffect.Spring)
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F7FA')
  }

  @Builder
  NavigationBar() {
    Row() {
      Image($r('app.media.ic_back'))
        .width(24)
        .height(24)
        .onClick(() => router.back())

      Text('创建报表')
        .fontSize(18)
        .fontWeight(FontWeight.Medium)
        .layoutWeight(1)
        .textAlign(TextAlign.Center)

      Text('')
        .width(24)
    }
    .width('100%')
    .height(56)
    .padding({ left: 16, right: 16 })
    .backgroundColor('#FFFFFF')
    .shadow({ radius: 4, color: '#08000000', offsetX: 0, offsetY: 2 })
  }

  @Builder
  ReportNameSection() {
    Column() {
      Text('报表名称')
        .fontSize(14)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .width('100%')
        .margin({ bottom: 8 })

      TextInput({ placeholder: '请输入报表名称' })
        .width('100%')
        .height(48)
        .borderRadius(8)
        .backgroundColor('#FFFFFF')
        .onChange((value: string) => {
          this.reportName = value;
        })
    }
    .width('100%')
    .margin({ bottom: 16 })
  }

  @Builder
  TypeSection() {
    Column() {
      Text('报表类型')
        .fontSize(14)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .width('100%')
        .margin({ bottom: 8 })

      Row() {
        ForEach(this.reportTypes, (type: string) => {
          Text(type)
            .fontSize(14)
            .padding({ left: 16, right: 16, top: 10, bottom: 10 })
            .backgroundColor(this.selectedType === type ? '#667EEA' : '#FFFFFF')
            .fontColor(this.selectedType === type ? '#FFFFFF' : '#666666')
            .borderRadius(20)
            .margin({ right: 12 })
            .onClick(() => {
              this.selectedType = type;
            })
        })
      }
      .width('100%')
      .scrollBar(BarState.Off)
    }
    .width('100%')
    .margin({ bottom: 16 })
  }

  @Builder
  PeriodSection() {
    Column() {
      Text('报表周期')
        .fontSize(14)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .width('100%')
        .margin({ bottom: 8 })

      Row() {
        ForEach(this.periods, (period: string) => {
          Text(period)
            .fontSize(14)
            .padding({ left: 16, right: 16, top: 10, bottom: 10 })
            .backgroundColor(this.selectedPeriod === period ? '#667EEA' : '#FFFFFF')
            .fontColor(this.selectedPeriod === period ? '#FFFFFF' : '#666666')
            .borderRadius(20)
            .margin({ right: 12 })
            .onClick(() => {
              this.selectedPeriod = period;
            })
        })
      }
      .width('100%')
      .scrollBar(BarState.Off)
    }
    .width('100%')
    .margin({ bottom: 16 })
  }

  @Builder
  DateRangeSection() {
    Column() {
      Text('日期范围（可选）')
        .fontSize(14)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .width('100%')
        .margin({ bottom: 8 })

      Row() {
        TextInput({ placeholder: '开始日期' })
          .width('45%')
          .height(44)
          .borderRadius(8)
          .backgroundColor('#FFFFFF')
          .onChange((value: string) => {
            this.dateRange[0] = value;
          })

        Text('至')
          .fontSize(14)
          .fontColor('#999999')
          .margin({ left: 12, right: 12 })

        TextInput({ placeholder: '结束日期' })
          .width('45%')
          .height(44)
          .borderRadius(8)
          .backgroundColor('#FFFFFF')
          .onChange((value: string) => {
            this.dateRange[1] = value;
          })
      }
      .width('100%')
    }
    .width('100%')
    .margin({ bottom: 32 })
  }

  @Builder
  CreateButton() {
    Button() {
      if (this.isCreating) {
        Row() {
          LoadingProgress()
            .width(20)
            .height(20)
            .color('#FFFFFF')
            .margin({ right: 8 })

          Text('创建中...')
            .fontSize(16)
            .fontColor('#FFFFFF')
        }
      } else {
        Text('创建报表')
          .fontSize(16)
          .fontColor('#FFFFFF')
      }
    }
    .width('100%')
    .height(48)
    .backgroundColor(this.canCreate() ? '#667EEA' : '#CCCCCC')
    .borderRadius(24)
    .enabled(this.canCreate() && !this.isCreating)
    .onClick(() => this.handleCreate())
  }

  canCreate(): boolean {
    return this.reportName.trim().length > 0 &&
           this.selectedType.length > 0 &&
           this.selectedPeriod.length > 0;
  }

  handleCreate() {
    if (!this.canCreate()) return;

    this.isCreating = true;
    reportService.createReport({
      name: this.reportName,
      type: this.selectedType,
      period: this.selectedPeriod,
      dateRange: this.dateRange
    }).then(() => {
      this.showSuccessDialog = true;
    }).catch(() => {
      // 处理错误
    }).finally(() => {
      this.isCreating = false;
    });
  }
}