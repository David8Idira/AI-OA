import router from '@ohos.router'
import { ApiClient } from '../../data/api/ApiClient'

// 快捷操作项
interface QuickAction {
  id: string
  title: string
  icon: string
  color: string
  action: () => void
}

// 工作台状态
@State
class HomeState {
  username: string = '用户'
  todayTasks: number = 0
  pendingApprovals: number = 0
  unreadMessages: number = 0
  loading: boolean = false
}

@Entry
@Component
struct HomeIndex {
  @State state: HomeState = new HomeState()

  aboutToAppear() {
    this.loadUserInfo()
    this.loadDashboardData()
  }

  // 加载用户信息
  async loadUserInfo(): Promise<void> {
    try {
      const token = AppStorage.get<string>('auth_token')
      if (token) {
        const userName = AppStorage.get<string>('username')
        if (userName) {
          this.state.username = userName
        }
      }
    } catch (e) {
      console.error('Failed to load user info')
    }
  }

  // 加载工作台数据
  async loadDashboardData(): Promise<void> {
    this.state.loading = true
    try {
      // 模拟数据加载
      this.state.todayTasks = 5
      this.state.pendingApprovals = 3
      this.state.unreadMessages = 12
    } catch (e) {
      console.error('Failed to load dashboard data')
    } finally {
      this.state.loading = false
    }
  }

  // 快捷操作列表
  private quickActions: QuickAction[] = [
    {
      id: '1',
      title: '发起审批',
      icon: '📝',
      color: '#1890FF',
      action: () => router.pushUrl({ url: 'pages/CreateApproval' })
    },
    {
      id: '2',
      title: '我的审批',
      icon: '✅',
      color: '#52C41A',
      action: () => router.pushUrl({ url: 'pages/Approvals' })
    },
    {
      id: '3',
      title: '日程安排',
      icon: '📅',
      color: '#FAAD14',
      action: () => router.pushUrl({ url: 'pages/Schedule' })
    },
    {
      id: '4',
      title: '通讯录',
      icon: '📇',
      color: '#722ED1',
      action: () => router.pushUrl({ url: 'pages/Contacts' })
    },
    {
      id: '5',
      title: '考勤打卡',
      icon: '📍',
      color: '#EB2F96',
      action: () => router.pushUrl({ url: 'pages/CheckIn' })
    },
    {
      id: '6',
      title: '更多',
      icon: '⋮',
      color: '#8C8C8C',
      action: () => {}
    }
  ]

  build() {
    Scroll() {
      Column() {
        // 顶部欢迎区域
        this.buildWelcomeSection()
        
        // 统计卡片区域
        this.buildStatsSection()
        
        // 快捷操作区域
        this.buildQuickActionsSection()
        
        // 最新动态区域
        this.buildRecentActivitySection()
      }
      .padding(16)
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }

  // 欢迎区域
  @Builder
  buildWelcomeSection() {
    Row() {
      Column() {
        Text(`你好，${this.state.username}`)
          .fontSize(24)
          .fontWeight(FontWeight.Bold)
          .fontColor('#333333')
        
        Text('今天是' + new Date().toLocaleDateString('zh-CN', {
          year: 'numeric',
          month: 'long',
          day: 'numeric',
          weekday: 'long'
        }))
          .fontSize(14)
          .fontColor('#999999')
          .margin({ top: 8 })
      }
      .alignItems(HorizontalAlign.Start)
      
      Blank()
      
      Image($r('app.media.icon_avatar'))
        .width(48)
        .height(48)
        .borderRadius(24)
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 16 })
  }

  // 统计卡片区域
  @Builder
  buildStatsSection() {
    Row() {
      this.buildStatCard('今日任务', this.state.todayTasks.toString(), '#1890FF')
      this.buildStatCard('待审批', this.state.pendingApprovals.toString(), '#52C41A')
      this.buildStatCard('未读消息', this.state.unreadMessages.toString(), '#FAAD14')
    }
    .width('100%')
    .margin({ bottom: 16 })
  }

  @Builder
  buildStatCard(title: string, value: string, color: string) {
    Column() {
      Text(value)
        .fontSize(28)
        .fontWeight(FontWeight.Bold)
        .fontColor(color)
      
      Text(title)
        .fontSize(12)
        .fontColor('#666666')
        .margin({ top: 4 })
    }
    .width('30%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .alignItems(HorizontalAlign.Center)
  }

  // 快捷操作区域
  @Builder
  buildQuickActionsSection() {
    Column() {
      Text('快捷操作')
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .alignSelf(HorizontalAlign.Start)
        .margin({ bottom: 12 })
      
      Grid() {
        ForEach(this.quickActions, (action: QuickAction) => {
          GridItem() {
            Column() {
              Text(action.icon)
                .fontSize(28)
              
              Text(action.title)
                .fontSize(12)
                .fontColor('#666666')
                .margin({ top: 8 })
            }
            .width('100%')
            .padding(12)
            .onClick(action.action)
          }
          .width('33%')
        })
      }
      .columnsTemplate('1fr 1fr 1fr')
      .rowsTemplate('1fr 1fr')
      .width('100%')
      .height(160)
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 16 })
  }

  // 最新动态区域
  @Builder
  buildRecentActivitySection() {
    Column() {
      Row() {
        Text('最新动态')
          .fontSize(16)
          .fontWeight(FontWeight.Medium)
          .fontColor('#333333')
        
        Blank()
        
        Text('查看更多 >')
          .fontSize(12)
          .fontColor('#1890FF')
          .onClick(() => router.pushUrl({ url: 'pages/Activity' }))
      }
      .width('100%')
      .margin({ bottom: 12 })
      
      // 动态列表占位
      Column() {
        Text('暂无最新动态')
          .fontSize(14)
          .fontColor('#999999')
          .padding(20)
      }
      .width('100%')
      .backgroundColor('#FAFAFA')
      .borderRadius(8)
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
  }
}
