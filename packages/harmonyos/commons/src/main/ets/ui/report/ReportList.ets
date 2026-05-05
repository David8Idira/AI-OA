/**
 * Report List Page - 报表列表页面
 */
import router from '@ohos.router';
import { ReportInfo, ReportStats } from '../../model/report/ReportModel';
import { reportService } from '../../service/ReportService';

@Component
export struct ReportList {
  @State reports: ReportInfo[] = [];
  @State stats: ReportStats = { monthTotal: 0, pendingCount: 0, reimburseCount: 0, aiReportCount: 0 };
  @State loading: boolean = false;
  @State showCreateDialog: boolean = false;

  private scroller: Scroller = new Scroller();

  aboutToAppear() {
    this.loadReports();
    this.loadStats();
  }

  loadReports() {
    this.loading = true;
    reportService.getReportList().then((list) => {
      this.reports = list;
    }).catch(() => {
      // 处理错误
    }).finally(() => {
      this.loading = false;
    });
  }

  loadStats() {
    reportService.getReportStats().then((s) => {
      this.stats = s;
    }).catch(() => {
      // 处理错误
    });
  }

  build() {
    Column() {
      // 顶部导航
      this.NavigationBar()

      Scroll(this.scroller) {
        Column() {
          // 统计卡片
          this.StatsCards()

          // 报表列表
          this.ReportListSection()
        }
        .width('100%')
        .padding(16)
      }
      .layoutWeight(1)
      .scrollBar(BarState.Auto)
      .edgeEffect(EdgeEffect.Spring)

      // 创建报表按钮
      this.CreateButton()
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

      Text('智能报表中心')
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
  StatsCards() {
    Column() {
      Row() {
        this.StatCard('本月报销总额', `¥${this.stats.monthTotal.toLocaleString()}`, '#667EEA')
        this.StatCard('待审批', `${this.stats.pendingCount}`, '#E6A23C')
      }
      .width('100%')

      Row() {
        this.StatCard('本月报销次数', `${this.stats.reimburseCount}`, '#409EFF')
        this.StatCard('AI生成报表', `${this.stats.aiReportCount}`, '#67C23A')
      }
      .width('100%')
      .margin({ top: 12 })
    }
    .width('100%')
    .margin({ bottom: 16 })
  }

  @Builder
  StatCard(title: string, value: string, color: string) {
    Column() {
      Text(value)
        .fontSize(20)
        .fontWeight(FontWeight.Bold)
        .fontColor(color)

      Text(title)
        .fontSize(12)
        .fontColor('#999999')
        .margin({ top: 4 })
    }
    .width('47%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .alignItems(HorizontalAlign.Center)
  }

  @Builder
  ReportListSection() {
    Column() {
      Row() {
        Text('我的报表')
          .fontSize(16)
          .fontWeight(FontWeight.Medium)

        Blank()

        Text('查看全部 >')
          .fontSize(12)
          .fontColor('#667EEA')
      }
      .width('100%')
      .margin({ bottom: 12 })

      if (this.reports.length === 0) {
        Column() {
          Image($r('app.media.ic_empty'))
            .width(80)
            .height(80)
            .opacity(0.5)

          Text('暂无报表')
            .fontSize(14)
            .fontColor('#999999')
            .margin({ top: 16 })
        }
        .width('100%')
        .padding(60)
        .backgroundColor('#FFFFFF')
        .borderRadius(12)
        .alignItems(HorizontalAlign.Center)
      } else {
        ForEach(this.reports, (report: ReportInfo) => {
          this.ReportCard(report)
        })
      }
    }
    .width('100%')
  }

  @Builder
  ReportCard(report: ReportInfo) {
    Column() {
      Row() {
        Column() {
          Text(report.name)
            .fontSize(14)
            .fontWeight(FontWeight.Medium)
            .fontColor('#333333')

          Row() {
            Text(report.type)
              .fontSize(12)
              .fontColor('#667EEA')
              .backgroundColor('#ECF5FF')
              .padding({ left: 6, right: 6, top: 2, bottom: 2 })
              .borderRadius(4)
              .margin({ right: 8 })

            Text(report.period)
              .fontSize(12)
              .fontColor('#999999')
          }
          .margin({ top: 6 })
        }
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Start)

        Column() {
          Text(report.status === 'completed' ? '已完成' : '生成中')
            .fontSize(12)
            .fontColor(report.status === 'completed' ? '#67C23A' : '#E6A23C')
            .backgroundColor(report.status === 'completed' ? '#F0F9EB' : '#FDF6EC')
            .padding({ left: 8, right: 8, top: 2, bottom: 2 })
            .borderRadius(4)

          Text(report.createTime)
            .fontSize(12)
            .fontColor('#999999')
            .margin({ top: 6 })
        }
        .alignItems(HorizontalAlign.End)
      }
      .width('100%')
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
    .onClick(() => {
      // 查看报表详情
    })
  }

  @Builder
  CreateButton() {
    Button() {
      Row() {
        Image($r('app.media.ic_add'))
          .width(20)
          .height(20)
          .margin({ right: 8 })

        Text('创建报表')
          .fontSize(16)
      }
    }
    .width('90%')
    .height(48)
    .backgroundColor('#667EEA')
    .borderRadius(24)
    .margin({ bottom: 16 })
    .onClick(() => {
      router.pushUrl({ url: 'pages/report/ReportCreate' });
    })
  }
}