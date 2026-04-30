import router from '@ohos.router'

// 主页面
@Entry
@Component
struct MainPage {
  @State currentIndex: number = 0

  build() {
    Tabs({ barPosition: BarPosition.End, controller: this.tabController }) {
      TabContent() {
        Column() {
          // HomeIndex 应该在这里渲染
          Text('工作台')
            .fontSize(24)
        }
        .width('100%')
        .height('100%')
        .backgroundColor('#F5F5F5')
      }
      .tabBar(this.buildTabBar('工作台', '🏠', 0))

      TabContent() {
        Column() {
          // 审批页面
          Text('审批')
            .fontSize(24)
        }
        .width('100%')
        .height('100%')
        .backgroundColor('#F5F5F5')
      }
      .tabBar(this.buildTabBar('审批', '✅', 1))

      TabContent() {
        Column() {
          // 消息页面
          Text('消息')
            .fontSize(24)
        }
        .width('100%')
        .height('100%')
        .backgroundColor('#F5F5F5')
      }
      .tabBar(this.buildTabBar('消息', '💬', 2))

      TabContent() {
        Column() {
          // 通讯录/我的页面
          Text('通讯录')
            .fontSize(24)
        }
        .width('100%')
        .height('100%')
        .backgroundColor('#F5F5F5')
      }
      .tabBar(this.buildTabBar('通讯录', '📇', 3))

      TabContent() {
        Column() {
          // 我的页面
          Text('我的')
            .fontSize(24)
        }
        .width('100%')
        .height('100%')
        .backgroundColor('#F5F5F5')
      }
      .tabBar(this.buildTabBar('我的', '👤', 4))
    }
    .width('100%')
    .height('100%')
    .onChange((index: number) => {
      this.currentIndex = index
    })
    .barHeight(60)
    .barMode(BarMode.Fixed)
  }

  private tabController: TabsController = new TabsController()

  @Builder
  buildTabBar(title: string, icon: string, index: number) {
    Column() {
      Text(icon)
        .fontSize(22)

      Text(title)
        .fontSize(11)
        .fontColor(this.currentIndex === index ? '#1890FF' : '#999999')
        .margin({ top: 4 })
    }
    .width('100%')
    .height('100%')
    .justifyContent(FlexAlign.Center)
    .backgroundColor(this.currentIndex === index ? '#1890FF10' : '#FFFFFF')
  }
}
