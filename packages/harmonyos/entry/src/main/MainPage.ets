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
          Text('首页').fontSize(24)
        }
        .width('100%')
        .height('100%')
        .backgroundColor('#F5F5F5')
      }
      .tabBar('工作台')

      TabContent() {
        Column() {
          Text('审批').fontSize(24)
        }
        .width('100%')
        .height('100%')
        .backgroundColor('#F5F5F5')
      }
      .tabBar('审批')

      TabContent() {
        Column() {
          Text('消息').fontSize(24)
        }
        .width('100%')
        .height('100%')
        .backgroundColor('#F5F5F5')
      }
      .tabBar('消息')

      TabContent() {
        Column() {
          Text('我的').fontSize(24)
        }
        .width('100%')
        .height('100%')
        .backgroundColor('#F5F5F5')
      }
      .tabBar('我的')
    }
    .width('100%')
    .height('100%')
    .onChange((index: number) => {
      this.currentIndex = index
    })
  }

  private tabController: TabsController = new TabsController()
}
