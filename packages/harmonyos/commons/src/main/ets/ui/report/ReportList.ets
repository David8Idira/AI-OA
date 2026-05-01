import router from '@ohos.router'

interface Report {
  id: string
  title: string
  type: string
  createTime: string
  status: string
  period: string
}

class ReportListState {
  reports: Report[] = []
  loading: boolean = false
  selectedType: string = '全部'
}

@Entry
@Component
struct ReportList {
  @State state: ReportListState = new ReportListState()
  private scrollController: Scroller = new Scroller()

  private reportTypes: string[] = ['全部', '考勤报表', '财务报表', '业务报表', '项目报表']

  aboutToAppear() {
    this.loadReports()
  }

  async loadReports(): Promise<void> {
    this.state.loading = true
    setTimeout(() => {
      this.state.reports = [
        { id: '1', title: '2024年4月考勤汇总表', type: '考勤报表', createTime: '2024-04-30 18:00', status: '已生成', period: '2024年4月' },
        { id: '2', title: '2024年Q1财务收支报告', type: '财务报表', createTime: '2024-04-15 10:00', status: '已生成', period: '2024年Q1' },
        { id: '3', title: '4月销售业绩报告', type: '业务报表', createTime: '2024-04-28 14:30', status: '已生成', period: '2024年4月' },
        { id: '4', title: 'AI-OA项目进度报告', type: '项目报表', createTime: '2024-04-25 16:00', status: '生成中', period: '2024年4月' },
        { id: '5', title: '2024年3月考勤汇总表', type: '考勤报表', createTime: '2024-03-31 18:00', status: '已生成', period: '2024年3月' },
        { id: '6', title: '3月销售业绩报告', type: '业务报表', createTime: '2024-03-28 14:30', status: '已生成', period: '2024年3月' },
        { id: '7', title: '2024年Q4财务收支报告', type: '财务报表', createTime: '2024-01-05 10:00', status: '已生成', period: '2024年Q4' },
        { id: '8', title: '年度人力资源报告', type: '业务报表', createTime: '2024-01-10 09:00', status: '已生成', period: '2023年度' }
      ]
      this.state.loading = false
    }, 500)
  }

  private getFilteredReports(): Report[] {
    return this.state.reports.filter(report => {
      return this.state.selectedType === '全部' || report.type === this.state.selectedType
    })
  }

  private getStatusColor(status: string): string {
    switch (status) {
      case '已生成': return '#52C41A'
      case '生成中': return '#FAAD14'
      case '生成失败': return '#FF4D4F'
      default: return '#999999'
    }
  }

  private getTypeIcon(type: string): string {
    switch (type) {
      case '考勤报表': return '📅'
      case '财务报表': return '💰'
      case '业务报表': return '📊'
      case '项目报表': return '📋'
      default: return '📄'
    }
  }

  build() {
    Column() {
      // 顶部导航
      Row() {
        Text('报表中心')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#333333')
      }
      .width('100%')
      .height(56)
      .padding({ left: 16 })
      .backgroundColor('#FFFFFF')
      .shadow({ radius: 4, color: '#10000000', offsetX: 0, offsetY: 2 })

      // 类型筛选
      Column() {
        Scroll() {
          Row() {
            ForEach(this.reportTypes, (type: string) => {
              Text(type)
                .fontSize(13)
                .fontColor(this.state.selectedType === type ? '#FFFFFF' : '#666666')
                .backgroundColor(this.state.selectedType === type ? '#1890FF' : '#E6E6E6')
                .borderRadius(16)
                .padding({ left: 16, right: 16, top: 6, bottom: 6 })
                .margin({ right: 8 })
                .onClick(() => {
                  this.state.selectedType = type
                })
            })
          }
        }
        .width('100%')
        .scrollable(ScrollDirection.Horizontal)
        .scrollBar(BarState.Off)
      }
      .width('100%')
      .padding({ left: 16, right: 16, top: 12 })

      // 报表列表
      Scroll(this.scrollController) {
        Column() {
          if (this.state.loading) {
            Row() {
              Text('加载中...')
                .fontSize(14)
                .fontColor('#999999')
            }
            .width('100%')
            .padding(40)
          } else {
            Text(`共 ${this.getFilteredReports().length} 份报表`)
              .fontSize(12)
              .fontColor('#999999')
              .alignSelf(HorizontalAlign.Start)
              .margin({ bottom: 12, left: 4 })

            ForEach(this.getFilteredReports(), (report: Report) => {
              this.buildReportItem(report)
            })
          }
        }
        .padding(16)
      }
      .width('100%')
      .layoutWeight(1)
      .backgroundColor('#F5F5F5')

      // 底部新建按钮
      Row() {
        Button('新建报表')
          .fontSize(16)
          .fontColor('#FFFFFF')
          .backgroundColor('#1890FF')
          .borderRadius(24)
          .width('80%')
          .height(48)
          .onClick(() => {
            router.pushUrl({ url: 'pages/ReportCreate' })
          })
      }
      .width('100%')
      .padding(16)
      .backgroundColor('#FFFFFF')
    }
    .width('100%')
    .height('100%')
  }

  @Builder
  buildReportItem(report: Report) {
    Row() {
      Text(this.getTypeIcon(report.type))
        .fontSize(28)
        .width(50)
        .height(50)
        .backgroundColor('#F0F5FF')
        .borderRadius(12)
        .textAlign(TextAlign.Center)

      Column() {
        Row() {
          Text(report.title)
            .fontSize(15)
            .fontWeight(FontWeight.Medium)
            .fontColor('#333333')

          Text(report.status)
            .fontSize(11)
            .fontColor('#FFFFFF')
            .backgroundColor(this.getStatusColor(report.status))
            .borderRadius(8)
            .padding({ left: 6, right: 6, top: 2, bottom: 2 })
            .margin({ left: 8 })
        }

        Row() {
          Text(report.type)
            .fontSize(12)
            .fontColor('#666666')

          Text(' | ')
            .fontSize(12)
            .fontColor('#CCCCCC')

          Text(report.period)
            .fontSize(12)
            .fontColor('#666666')
        }
        .margin({ top: 6 })

        Text(`创建时间：${report.createTime}`)
          .fontSize(11)
          .fontColor('#999999')
          .margin({ top: 4 })
      }
      .alignItems(HorizontalAlign.Start)
      .layoutWeight(1)
      .margin({ left: 12 })

      Column() {
        Text('查看')
          .fontSize(13)
          .fontColor('#1890FF')
      }
      .alignItems(HorizontalAlign.End)
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
    .onClick(() => {
      router.pushUrl({ url: 'pages/ReportDetail', params: { reportId: report.id } })
    })
  }
}