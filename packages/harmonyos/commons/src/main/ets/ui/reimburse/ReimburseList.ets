/**
 * Reimburse List Page - 报销列表页面
 */
import router from '@ohos.router';
import { ReimburseInfo, ReimburseStats } from '../../model/reimburse/ReimburseModel';
import { reimburseService } from '../../service/ReimburseService';

@Component
export struct ReimburseList {
  @State reimburses: ReimburseInfo[] = [];
  @State stats: ReimburseStats = { pendingAmount: 0, pendingCount: 0, monthTotal: 0, invoiceCount: 0 };
  @State loading: boolean = false;
  @State filterStatus: string = '';
  @State filterKeyword: string = '';
  @State showCreateDialog: boolean = false;

  private scroller: Scroller = new Scroller();
  private statusOptions = ['全部', '草稿', '待审批', '已通过', '已拒绝', '已打款'];

  aboutToAppear() {
    this.loadReimburses();
    this.loadStats();
  }

  loadReimburses() {
    this.loading = true;
    reimburseService.getReimburseList({
      status: this.filterStatus,
      keyword: this.filterKeyword
    }).then((result) => {
      this.reimburses = result;
    }).catch(() => {
      // 处理错误
    }).finally(() => {
      this.loading = false;
    });
  }

  loadStats() {
    reimburseService.getReimburseStats().then((s) => {
      this.stats = s;
    }).catch(() => {
      // 处理错误
    });
  }

  build() {
    Column() {
      // 顶部导航栏
      this.NavigationBar()

      Scroll(this.scroller) {
        Column() {
          // 统计卡片
          this.StatsCards()

          // 筛选区域
          this.FilterArea()

          // 报销列表
          this.ReimburseListSection()
        }
        .width('100%')
        .padding(16)
      }
      .layoutWeight(1)
      .scrollBar(BarState.Auto)
      .edgeEffect(EdgeEffect.Spring)

      // 底部操作按钮
      this.BottomActions()
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

      Text('财务报销管理')
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
        this.StatCard(this.stats.pendingAmount.toLocaleString(), '待报销金额', '#E6A23C')
        this.StatCard(this.stats.pendingCount.toString(), '待审批单据', '#409EFF')
      }
      .width('100%')

      Row() {
        this.StatCard(this.stats.monthTotal.toLocaleString(), '本月报销总额', '#67C23A')
        this.StatCard(this.stats.invoiceCount.toString(), '已识别发票', '#667EEA')
      }
      .width('100%')
      .margin({ top: 12 })
    }
    .width('100%')
    .margin({ bottom: 16 })
  }

  @Builder
  StatCard(value: string, label: string, color: string) {
    Column() {
      Text(`¥${value}`)
        .fontSize(20)
        .fontWeight(FontWeight.Bold)
        .fontColor(color)

      Text(label)
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
  FilterArea() {
    Column() {
      Row() {
        TextInput({ placeholder: '标题/申请人' })
          .width('60%')
          .height(36)
          .borderRadius(8)
          .backgroundColor('#FFFFFF')
          .onChange((value: string) => {
            this.filterKeyword = value;
          })

        Select(this.statusOptions)
          .width('35%')
          .height(36)
          .borderRadius(8)
          .backgroundColor('#FFFFFF')
          .margin({ left: 12 })
          .onSelect((index: number) => {
            this.filterStatus = index === 0 ? '' : ['DRAFT', 'PENDING', 'APPROVED', 'REJECTED', 'PAID'][index - 1];
          })
      }
      .width('100%')

      Row() {
        Blank()

        Button('查询')
          .height(32)
          .fontSize(12)
          .backgroundColor('#667EEA')
          .borderRadius(16)
          .onClick(() => {
            this.loadReimburses();
          })

        Button('重置')
          .height(32)
          .fontSize(12)
          .backgroundColor('#FFFFFF')
          .fontColor('#666666')
          .border({ width: 1, color: '#E8E8E8', radius: 16 })
          .margin({ left: 8 })
          .onClick(() => {
            this.filterStatus = '';
            this.filterKeyword = '';
            this.loadReimburses();
          })
      }
      .width('100%')
      .margin({ top: 12 })
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 16 })
  }

  @Builder
  ReimburseListSection() {
    Column() {
      if (this.loading) {
        LoadingProgress()
          .width(40)
          .height(40)
          .color('#667EEA')
      } else if (this.reimburses.length === 0) {
        Column() {
          Image($r('app.media.ic_empty'))
            .width(80)
            .height(80)
            .opacity(0.5)

          Text('暂无报销记录')
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
        ForEach(this.reimburses, (item: ReimburseInfo) => {
          this.ReimburseCard(item)
        })
      }
    }
    .width('100%')
  }

  @Builder
  ReimburseCard(item: ReimburseInfo) {
    Column() {
      Row() {
        Column() {
          Text(item.title)
            .fontSize(14)
            .fontWeight(FontWeight.Medium)
            .fontColor('#333333')

          Row() {
            Text(item.applicant || '-')
              .fontSize(12)
              .fontColor('#666666')
              .margin({ right: 12 })

            Text(item.createTime)
              .fontSize(12)
              .fontColor('#999999')
          }
          .margin({ top: 6 })
        }
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Start)

        Column() {
          Text(`¥${item.amount.toFixed(2)}`)
            .fontSize(16)
            .fontWeight(FontWeight.Bold)
            .fontColor('#333333')

          Text(this.getStatusText(item.status))
            .fontSize(12)
            .fontColor(this.getStatusColor(item.status))
            .backgroundColor(this.getStatusBgColor(item.status))
            .padding({ left: 8, right: 8, top: 2, bottom: 2 })
            .borderRadius(4)
            .margin({ top: 4 })
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
      // 查看报销详情
    })
  }

  @Builder
  BottomActions() {
    Row() {
      Button() {
        Row() {
          Image($r('app.media.ic_camera'))
            .width(20)
            .height(20)
            .margin({ right: 8 })

          Text('OCR识别发票')
            .fontSize(14)
        }
      }
      .width('45%')
      .height(44)
      .backgroundColor('#67C23A')
      .borderRadius(22)
      .onClick(() => {
        // OCR扫描
      })

      Button() {
        Row() {
          Image($r('app.media.ic_add'))
            .width(20)
            .height(20)
            .margin({ right: 8 })

          Text('新建报销单')
            .fontSize(14)
        }
      }
      .width('45%')
      .height(44)
      .backgroundColor('#667EEA')
      .borderRadius(22)
      .onClick(() => {
        router.pushUrl({ url: 'pages/reimburse/ReimburseCreate' });
      })
    }
    .width('100%')
    .padding({ left: 16, right: 16, bottom: 16 })
    .justifyContent(FlexAlign.SpaceEvenly)
    .backgroundColor('#FFFFFF')
  }

  getStatusText(status: string): string {
    const map: Record<string, string> = {
      'DRAFT': '草稿',
      'PENDING': '待审批',
      'APPROVED': '已通过',
      'REJECTED': '已拒绝',
      'PAID': '已打款'
    };
    return map[status] || status;
  }

  getStatusColor(status: string): string {
    const map: Record<string, string> = {
      'DRAFT': '#909399',
      'PENDING': '#E6A23C',
      'APPROVED': '#67C23A',
      'REJECTED': '#F56C6C',
      'PAID': '#409EFF'
    };
    return map[status] || '#999999';
  }

  getStatusBgColor(status: string): string {
    const map: Record<string, string> = {
      'DRAFT': '#F4F4F5',
      'PENDING': '#FDF6EC',
      'APPROVED': '#F0F9EB',
      'REJECTED': '#FEF0F0',
      'PAID': '#ECF5FF'
    };
    return map[status] || '#F5F7FA';
  }
}