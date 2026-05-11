import router from '@ohos.router'
import promptAction from '@ohos.promptAction'
import approvalService from '../../../../commons/src/main/ets/services/ApprovalService'

// AI-OA 审批列表页面
@Entry
@Component
struct ApprovalList {
  @State tabIndex: number = 0
  @State pendingList: any[] = []
  @State approvedList: any[] = []
  @State isLoading: boolean = false
  @State errorMsg: string = ''
  @State totalPending: number = 0
  @State totalApproved: number = 0

  aboutToAppear() {
    this.loadPendingApprovals()
  }

  async loadPendingApprovals() {
    this.isLoading = true
    this.errorMsg = ''
    try {
      const res = await approvalService.getPendingApprovals({ page: 1, pageSize: 50 })
      if (res.code === 0 || res.code === 200) {
        this.pendingList = res.data?.approvals ?? []
        this.totalPending = res.data?.total ?? 0
      } else {
        this.errorMsg = res.message || '加载失败'
      }
    } catch (e) {
      this.errorMsg = '网络异常，请检查网络'
    } finally {
      this.isLoading = false
    }
  }

  async loadApprovedApprovals() {
    this.isLoading = true
    this.errorMsg = ''
    try {
      // 获取已通过的审批列表
      const res = await approvalService.getApprovals({ status: 'approved', page: 1, pageSize: 50 })
      if (res.code === 0 || res.code === 200) {
        this.approvedList = res.data?.approvals ?? []
        this.totalApproved = res.data?.total ?? 0
      } else {
        this.errorMsg = res.message || '加载失败'
      }
    } catch (e) {
      this.errorMsg = '网络异常，请检查网络'
    } finally {
      this.isLoading = false
    }
  }

  build() {
    Column() {
      // 顶部
      Row() {
        Text('审批')
          .fontSize(24)
          .fontWeight(FontWeight.Bold)
      }
      .width('90%')
      .height(50)
      .margin({ top: 20 })

      // Tab 切换
      Row() {
        Text('待审批 (' + this.totalPending + ')')
          .fontSize(16)
          .fontColor(this.tabIndex === 0 ? '#1890FF' : '#999999')
          .borderWidth({ bottom: this.tabIndex === 0 ? 2 : 0 })
          .borderColor('#1890FF')
          .padding({ bottom: 4 })
          .onClick(() => {
            this.tabIndex = 0
            if (this.pendingList.length === 0) {
              this.loadPendingApprovals()
            }
          })

        Text('已审批 (' + this.totalApproved + ')')
          .fontSize(16)
          .fontColor(this.tabIndex === 1 ? '#1890FF' : '#999999')
          .borderWidth({ bottom: this.tabIndex === 1 ? 2 : 0 })
          .borderColor('#1890FF')
          .padding({ bottom: 4 })
          .margin({ left: 30 })
          .onClick(() => {
            this.tabIndex = 1
            if (this.approvedList.length === 0) {
              this.loadApprovedApprovals()
            }
          })
      }
      .width('90%')
      .margin({ top: 10 })

      // 列表内容
      if (this.tabIndex === 0) {
        this.buildPendingList()
      } else {
        this.buildApprovedList()
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }

  @Builder
  buildPendingList() {
    if (this.isLoading) {
      Row() {
        LoadingProgress()
          .width(32)
          .height(32)
        Text('加载中...')
          .fontSize(14)
          .fontColor('#999999')
          .margin({ left: 8 })
      }
      .width('100%')
      .justifyContent(FlexAlign.Center)
      .padding(20)
    } else if (this.errorMsg) {
      Column() {
        Text('❌')
          .fontSize(32)
          .margin({ bottom: 8 })
        Text(this.errorMsg)
          .fontSize(14)
          .fontColor('#FF4D4F')
        Button('重试')
          .height(32)
          .borderRadius(16)
          .backgroundColor('#1890FF')
          .margin({ top: 12 })
          .onClick(() => this.loadPendingApprovals())
      }
      .width('100%')
      .padding(20)
    } else if (this.pendingList.length === 0) {
      Column() {
        Text('📋')
          .fontSize(40)
          .margin({ bottom: 8 })
        Text('暂无待审批项')
          .fontSize(14)
          .fontColor('#999999')
      }
      .width('100%')
      .padding(40)
    } else {
      List() {
        ForEach(this.pendingList, (item: any) => {
          ListItem() {
            Row() {
              Column() {
                Text(item.title || item.type || '审批申请')
                  .fontSize(16)
                  .fontWeight(FontWeight.Medium)
                Text((item.type || '') + ' | ' + (item.createdAt ? this.formatTime(item.createdAt) : ''))
                  .fontSize(12)
                  .fontColor('#999999')
                  .margin({ top: 4 })
              }
              .width('70%')
              .alignItems(HorizontalAlign.Start)

              Button('审批')
                .height(32)
                .borderRadius(16)
                .backgroundColor('#1890FF')
                .onClick(() => {
                  router.pushUrl({ url: 'pages/approval/ApprovalDetail', params: { approvalId: item.id } })
                })
            }
            .width('90%')
            .padding(16)
            .backgroundColor('#FFFFFF')
            .borderRadius(12)
            .margin({ top: 8 })
          }
        })
      }
      .width('90%')
      .padding({ top: 10 })
      .onScrollIndexChange((start) => {
        // 滚动加载更多
      })
    }
  }

  @Builder
  buildApprovedList() {
    if (this.isLoading) {
      Row() {
        LoadingProgress()
          .width(32)
          .height(32)
        Text('加载中...')
          .fontSize(14)
          .fontColor('#999999')
          .margin({ left: 8 })
      }
      .width('100%')
      .justifyContent(FlexAlign.Center)
      .padding(20)
    } else if (this.errorMsg) {
      Column() {
        Text('❌')
          .fontSize(32)
          .margin({ bottom: 8 })
        Text(this.errorMsg)
          .fontSize(14)
          .fontColor('#FF4D4F')
        Button('重试')
          .height(32)
          .borderRadius(16)
          .backgroundColor('#1890FF')
          .margin({ top: 12 })
          .onClick(() => this.loadApprovedApprovals())
      }
      .width('100%')
      .padding(20)
    } else if (this.approvedList.length === 0) {
      Column() {
        Text('✅')
          .fontSize(40)
          .margin({ bottom: 8 })
        Text('暂无已审批记录')
          .fontSize(14)
          .fontColor('#999999')
      }
      .width('100%')
      .padding(40)
    } else {
      List() {
        ForEach(this.approvedList, (item: any) => {
          ListItem() {
            Row() {
              Column() {
                Text(item.title || item.type || '审批申请')
                  .fontSize(16)
                  .fontWeight(FontWeight.Medium)
                Text((item.type || '') + ' | ' + (item.createdAt ? this.formatTime(item.createdAt) : ''))
                  .fontSize(12)
                  .fontColor('#999999')
                  .margin({ top: 4 })
              }
              .width('70%')
              .alignItems(HorizontalAlign.Start)

              Text(item.status === 'approved' || item.status === 'pass' ? '已通过' : (item.status === 'rejected' ? '已拒绝' : item.status || '已处理'))
                .fontSize(14)
                .fontColor(item.status === 'approved' || item.status === 'pass' ? '#52C41A' : '#FF4D4F')
            }
            .width('90%')
            .padding(16)
            .backgroundColor('#FFFFFF')
            .borderRadius(12)
            .margin({ top: 8 })
          }
        })
      }
      .width('90%')
      .padding({ top: 10 })
    }
  }

  private formatTime(timestamp: number): string {
    const diff = Date.now() - timestamp
    if (diff < 60000) return '刚刚'
    if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
    if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
    return Math.floor(diff / 86400000) + '天前'
  }
