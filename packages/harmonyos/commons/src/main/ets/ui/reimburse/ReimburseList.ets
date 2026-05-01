import router from '@ohos.router'

interface Reimburse {
  id: string
  title: string
  amount: number
  type: string
  status: string
  createTime: string
  applicant: string
}

class ReimburseListState {
  reimburses: Reimburse[] = []
  loading: boolean = false
  selectedStatus: string = '全部'
}

@Entry
@Component
struct ReimburseList {
  @State state: ReimburseListState = new ReimburseListState()
  private scrollController: Scroller = new Scroller()

  private statuses: string[] = ['全部', '待审批', '审批中', '已通过', '已驳回', '已打款']

  aboutToAppear() {
    this.loadReimburses()
  }

  async loadReimburses(): Promise<void> {
    this.state.loading = true
    setTimeout(() => {
      this.state.reimburses = [
        { id: '1', title: '4月差旅费报销', amount: 3500, type: '差旅费', status: '待审批', createTime: '2024-04-28', applicant: '张三' },
        { id: '2', title: '办公采购报销', amount: 1200, type: '采购费', status: '已通过', createTime: '2024-04-25', applicant: '李四' },
        { id: '3', title: '客户招待费', amount: 2800, type: '招待费', status: '审批中', createTime: '2024-04-24', applicant: '王五' },
        { id: '4', title: '交通费报销', amount: 560, type: '交通费', status: '已打款', createTime: '2024-04-20', applicant: '赵六' },
        { id: '5', title: '培训费用报销', amount: 1500, type: '培训费', status: '已驳回', createTime: '2024-04-18', applicant: '钱七' },
        { id: '6', title: '3月通讯费报销', amount: 300, type: '通讯费', status: '已打款', createTime: '2024-04-15', applicant: '张三' },
        { id: '7', title: '设备维修费', amount: 800, type: '维修费', status: '已通过', createTime: '2024-04-12', applicant: '孙八' },
        { id: '8', title: '团建活动报销', amount: 2200, type: '团建费', status: '审批中', createTime: '2024-04-10', applicant: '李四' }
      ]
      this.state.loading = false
    }, 500)
  }

  private getFilteredReimburses(): Reimburse[] {
    return this.state.reimburses.filter(item => {
      return this.state.selectedStatus === '全部' || item.status === this.state.selectedStatus
    })
  }

  private getStatusColor(status: string): string {
    switch (status) {
      case '待审批': return '#FAAD14'
      case '审批中': return '#1890FF'
      case '已通过': return '#52C41A'
      case '已驳回': return '#FF4D4F'
      case '已打款': return '#722ED1'
      default: return '#999999'
    }
  }

  private getTypeIcon(type: string): string {
    switch (type) {
      case '差旅费': return '✈️'
      case '采购费': return '🛒'
      case '招待费': return '🍽️'
      case '交通费': return '🚗'
      case '培训费': return '📚'
      case '通讯费': return '📱'
      case '维修费': return '🔧'
      case '团建费': return '🎉'
      default: return '💰'
    }
  }

  build() {
    Column() {
      // 顶部导航
      Row() {
        Text('报销管理')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#333333')
      }
      .width('100%')
      .height(56)
      .padding({ left: 16 })
      .backgroundColor('#FFFFFF')
      .shadow({ radius: 4, color: '#10000000', offsetX: 0, offsetY: 2 })

      // 状态筛选
      Column() {
        Scroll() {
          Row() {
            ForEach(this.statuses, (status: string) => {
              Text(status)
                .fontSize(13)
                .fontColor(this.state.selectedStatus === status ? '#FFFFFF' : '#666666')
                .backgroundColor(this.state.selectedStatus === status ? '#1890FF' : '#E6E6E6')
                .borderRadius(16)
                .padding({ left: 14, right: 14, top: 6, bottom: 6 })
                .margin({ right: 8 })
                .onClick(() => {
                  this.state.selectedStatus = status
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

      // 报销列表
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
            // 统计卡片
            Row() {
              Column() {
                Text('待审批')
                  .fontSize(12)
                  .fontColor('#666666')
                Text('2')
                  .fontSize(20)
                  .fontWeight(FontWeight.Bold)
                  .fontColor('#FAAD14')
                  .margin({ top: 4 })
              }
              .layoutWeight(1)
              .padding(16)
              .backgroundColor('#FFF7E6')
              .borderRadius(12)
              .margin({ right: 8 })

              Column() {
                Text('审批中')
                  .fontSize(12)
                  .fontColor('#666666')
                Text('2')
                  .fontSize(20)
                  .fontWeight(FontWeight.Bold)
                  .fontColor('#1890FF')
                  .margin({ top: 4 })
              }
              .layoutWeight(1)
              .padding(16)
              .backgroundColor('#E6F0FF')
              .borderRadius(12)
              .margin({ right: 8 })

              Column() {
                Text('已打款')
                  .fontSize(12)
                  .fontColor('#666666')
                Text('2')
                  .fontSize(20)
                  .fontWeight(FontWeight.Bold)
                  .fontColor('#722ED1')
                  .margin({ top: 4 })
              }
              .layoutWeight(1)
              .padding(16)
              .backgroundColor('#F9F0FF')
              .borderRadius(12)
            }
            .width('100%')
            .margin({ bottom: 16 })

            Text(`共 ${this.getFilteredReimburses().length} 条记录`)
              .fontSize(12)
              .fontColor('#999999')
              .alignSelf(HorizontalAlign.Start)
              .margin({ bottom: 12, left: 4 })

            ForEach(this.getFilteredReimburses(), (item: Reimburse) => {
              this.buildReimburseItem(item)
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
        Button('新建报销')
          .fontSize(16)
          .fontColor('#FFFFFF')
          .backgroundColor('#1890FF')
          .borderRadius(24)
          .width('80%')
          .height(48)
          .onClick(() => {
            router.pushUrl({ url: 'pages/ReimburseCreate' })
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
  buildReimburseItem(item: Reimburse) {
    Row() {
      Text(this.getTypeIcon(item.type))
        .fontSize(28)
        .width(50)
        .height(50)
        .backgroundColor('#F0F5FF')
        .borderRadius(12)
        .textAlign(TextAlign.Center)

      Column() {
        Row() {
          Text(item.title)
            .fontSize(15)
            .fontWeight(FontWeight.Medium)
            .fontColor('#333333')

          Text(item.status)
            .fontSize(11)
            .fontColor('#FFFFFF')
            .backgroundColor(this.getStatusColor(item.status))
            .borderRadius(8)
            .padding({ left: 6, right: 6, top: 2, bottom: 2 })
            .margin({ left: 8 })
        }

        Row() {
          Text(item.type)
            .fontSize(12)
            .fontColor('#666666')

          Text(' | ')
            .fontSize(12)
            .fontColor('#CCCCCC')

          Text('申请人：' + item.applicant)
            .fontSize(12)
            .fontColor('#666666')
        }
        .margin({ top: 6 })

        Row() {
          Text(`申请时间：${item.createTime}`)
            .fontSize(11)
            .fontColor('#999999')

          Text('¥' + item.amount.toLocaleString())
            .fontSize(15)
            .fontWeight(FontWeight.Bold)
            .fontColor('#FF4D4F')
            .margin({ left: 12 })
        }
        .margin({ top: 4 })
      }
      .alignItems(HorizontalAlign.Start)
      .layoutWeight(1)
      .margin({ left: 12 })

      Text('>')
        .fontSize(16)
        .fontColor('#CCCCCC')
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
    .onClick(() => {
      router.pushUrl({ url: 'pages/ReimburseDetail', params: { reimburseId: item.id } })
    })
  }
}