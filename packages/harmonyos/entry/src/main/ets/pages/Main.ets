import router from '@ohos.router'

// AI-OA 主页面
@Entry
@Component
struct Main {
  @State currentIndex: number = 0
  private tabController: TabsController = new TabsController()

  aboutToAppear() {
    // 检查登录状态
    const token = AppStorage.get<string>('auth_token')
    if (!token) {
      router.replaceUrl({ url: 'pages/Login' })
    }
  }

  build() {
    Column() {
      if (this.currentIndex === 0) {
        this.buildHomeTab()
      } else if (this.currentIndex === 1) {
        this.buildApprovalTab()
      } else if (this.currentIndex === 2) {
        this.buildMessageTab()
      } else if (this.currentIndex === 3) {
        this.buildContactsTab()
      } else {
        this.buildProfileTab()
      }

      // 底部 TabBar
      this.buildBottomTabBar()
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }

  @Builder
  buildHomeTab() {
    Column() {
      Text('工作台')
        .fontSize(28)
        .fontWeight(FontWeight.Bold)
        .margin({ top: 20, left: 20 })
      
      Column() {
        // 快捷操作入口
        this.buildQuickActions()
      }
      .margin({ top: 20 })
    }
    .width('100%')
    .height('100%')
  }

  @Builder
  buildQuickActions() {
    Column() {
      Row() {
        this.buildActionCard('考勤打卡', '🕐', '#1890FF')
        this.buildActionCard('请假申请', '📋', '#52C41A')
        this.buildActionCard('报销审批', '💰', '#FA8C16')
        this.buildActionCard('AI 助手', '🤖', '#722ED1')
      }
      .width('100%')
      .justifyContent(FlexAlign.SpaceEvenly)
    }
    .width('90%')
  }

  @Builder
  buildActionCard(title: string, icon: string, color: string) {
    Column() {
      Text(icon)
        .fontSize(28)
      Text(title)
        .fontSize(12)
        .margin({ top: 8 })
        .fontColor('#333333')
    }
    .width(70)
    .height(70)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .justifyContent(FlexAlign.Center)
    .onClick(() => {
      if (title === 'AI 助手') {
        router.pushUrl({ url: 'pages/ai/AiChatPage' })
      } else if (title === '请假申请') {
        router.pushUrl({ url: 'pages/attendance/LeaveApply' })
      } else if (title === '考勤打卡') {
        router.pushUrl({ url: 'pages/attendance/CheckInPage' })
      }
    })
  }

  @Builder
  buildApprovalTab() {
    Column() {
      Text('审批')
        .fontSize(28)
        .fontWeight(FontWeight.Bold)
        .margin({ top: 20, left: 20 })
      
      Text('暂无待审批项')
        .fontSize(16)
        .fontColor('#999999')
        .margin({ top: 40 })
    }
    .width('100%')
    .height('100%')
  }

  @Builder
  buildMessageTab() {
    Column() {
      Text('消息')
        .fontSize(28)
        .fontWeight(FontWeight.Bold)
        .margin({ top: 20, left: 20 })
    }
    .width('100%')
    .height('100%')
  }

  @Builder
  buildContactsTab() {
    Column() {
      Text('通讯录')
        .fontSize(28)
        .fontWeight(FontWeight.Bold)
        .margin({ top: 20, left: 20 })
    }
    .width('100%')
    .height('100%')
  }

  @Builder
  buildProfileTab() {
    Column() {
      Text('我的')
        .fontSize(28)
        .fontWeight(FontWeight.Bold)
        .margin({ top: 20, left: 20 })
    }
    .width('100%')
    .height('100%')
  }

  @Builder
  buildBottomTabBar() {
    Row() {
      ForEach(['工作台', '审批', '消息', '通讯录', '我的'], (title: string, index: number) => {
        Column() {
          Text(this.getIcon(index))
            .fontSize(22)
          Text(title)
            .fontSize(11)
            .margin({ top: 2 })
            .fontColor(this.currentIndex === index ? '#1890FF' : '#999999')
        }
        .width('20%')
        .height(50)
        .justifyContent(FlexAlign.Center)
        .onClick(() => {
          this.currentIndex = index
        })
      })
    }
    .width('100%')
    .height(60)
    .backgroundColor('#FFFFFF')
    .padding({ top: 5 })
  }

  private getIcon(index: number): string {
    const icons = ['🏠', '✅', '💬', '📇', '👤']
    return icons[index]
  }
}
