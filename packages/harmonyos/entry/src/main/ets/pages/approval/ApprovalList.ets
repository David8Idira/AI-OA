import router from '@ohos.router'

// AI-OA 审批列表页面
@Entry
@Component
struct ApprovalList {
  @State tabIndex: number = 0
  @State pendingList: any[] = [
    { id: 1, title: '请假申请 - 张三', type: '请假', time: '2小时前', status: '待审批' },
    { id: 2, title: '报销申请 - 李四', type: '报销', time: '5小时前', status: '待审批' },
  ]
  @State approvedList: any[] = [
    { id: 3, title: '设备采购 - 王五', type: '采购', time: '1天前', status: '已通过' },
  ]

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
        Text('待审批 (2)')
          .fontSize(16)
          .fontColor(this.tabIndex === 0 ? '#1890FF' : '#999999')
          .borderWidth({ bottom: this.tabIndex === 0 ? 2 : 0 })
          .borderColor('#1890FF')
          .padding({ bottom: 4 })
          .onClick(() => {
            this.tabIndex = 0
          })
        
        Text('已审批')
          .fontSize(16)
          .fontColor(this.tabIndex === 1 ? '#1890FF' : '#999999')
          .borderWidth({ bottom: this.tabIndex === 1 ? 2 : 0 })
          .borderColor('#1890FF')
          .padding({ bottom: 4 })
          .margin({ left: 30 })
          .onClick(() => {
            this.tabIndex = 1
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
    List() {
      ForEach(this.pendingList, (item: any) => {
        ListItem() {
          Row() {
            Column() {
              Text(item.title)
                .fontSize(16)
                .fontWeight(FontWeight.Medium)
              Text(item.type + ' | ' + item.time)
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
                router.pushUrl({ url: 'pages/approval/ApprovalDetail' })
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
  }

  @Builder
  buildApprovedList() {
    List() {
      ForEach(this.approvedList, (item: any) => {
        ListItem() {
          Row() {
            Column() {
              Text(item.title)
                .fontSize(16)
                .fontWeight(FontWeight.Medium)
              Text(item.type + ' | ' + item.time)
                .fontSize(12)
                .fontColor('#999999')
                .margin({ top: 4 })
            }
            .width('70%')
            .alignItems(HorizontalAlign.Start)

            Text(item.status)
              .fontSize(14)
              .fontColor('#52C41A')
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
