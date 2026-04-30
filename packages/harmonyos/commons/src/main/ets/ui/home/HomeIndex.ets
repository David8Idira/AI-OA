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

// 轮播公告项
interface Announcement {
  id: string
  title: string
  content: string
  time: number
}

// 工作台状态
class HomeState {
  username: string = '用户'
  todayTasks: number = 0
  pendingApprovals: number = 0
  unreadMessages: number = 0
  loading: boolean = false
  announcements: Announcement[] = []
  currentAnnouncementIndex: number = 0
  attendanceStatus: AttendanceStatus | null = null
}

// 考勤状态
interface AttendanceStatus {
  checkedIn: boolean
  checkedOut: boolean
  checkInTime?: string
  checkOutTime?: string
}

@Entry
@Component
struct HomeIndex {
  @State state: HomeState = new HomeState()
  private swiperController: SwiperController = new SwiperController()

  aboutToAppear() {
    this.loadUserInfo()
    this.loadDashboardData()
    this.loadAnnouncements()
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

      // 模拟考勤状态
      this.state.attendanceStatus = {
        checkedIn: true,
        checkedOut: false,
        checkInTime: '09:00'
      }
    } catch (e) {
      console.error('Failed to load dashboard data')
    } finally {
      this.state.loading = false
    }
  }

  // 加载公告
  async loadAnnouncements(): Promise<void> {
    const now = Date.now()
    this.state.announcements = [
      {
        id: '1',
        title: '📢 公司通知',
        content: '五一劳动节放假安排：5月1日至5月5日放假调休，共5天。',
        time: now - 3600000
      },
      {
        id: '2',
        title: '📋 考勤提醒',
        content: '请各部门同事按时打卡，本月考勤统计将于下月初发布。',
        time: now - 86400000
      },
      {
        id: '3',
        title: '🎉 产品更新',
        content: 'AI-OA系统V2.0版本已上线，新增多项实用功能。',
        time: now - 172800000
      }
    ]
  }

  // 快捷操作列表
  private quickActions: QuickAction[] = [
    {
      id: '1',
      title: '考勤打卡',
      icon: '📍',
      color: '#1890FF',
      action: () => router.pushUrl({ url: 'pages/CheckIn' })
    },
    {
      id: '2',
      title: '请假申请',
      icon: '📅',
      color: '#52C41A',
      action: () => router.pushUrl({ url: 'pages/LeaveApply' })
    },
    {
      id: '3',
      title: '发起审批',
      icon: '✍️',
      color: '#FAAD14',
      action: () => router.pushUrl({ url: 'pages/ApprovalCreate' })
    },
    {
      id: '4',
      title: '我的审批',
      icon: '✅',
      color: '#722ED1',
      action: () => router.pushUrl({ url: 'pages/ApprovalList' })
    },
    {
      id: '5',
      title: '工作日志',
      icon: '📝',
      color: '#EB2F96',
      action: () => router.pushUrl({ url: 'pages/Log' })
    },
    {
      id: '6',
      title: '公告通知',
      icon: '📢',
      color: '#FA8C16',
      action: () => this.showAnnouncements()
    },
    {
      id: '7',
      title: '通讯录',
      icon: '📇',
      color: '#1890FF',
      action: () => router.pushUrl({ url: 'pages/Contacts' })
    },
    {
      id: '8',
      title: '扫一扫',
      icon: '📷',
      color: '#13C2C2',
      action: () => router.pushUrl({ url: 'pages/Scan' })
    }
  ]

  // 快捷功能入口（第二行）
  private quickEntries: QuickAction[] = [
    {
      id: '9',
      title: '日程安排',
      icon: '🗓️',
      color: '#1890FF',
      action: () => router.pushUrl({ url: 'pages/Schedule' })
    },
    {
      id: '10',
      title: '会议预约',
      icon: '🏢',
      color: '#52C41A',
      action: () => router.pushUrl({ url: 'pages/Meeting' })
    },
    {
      id: '11',
      title: '文档中心',
      icon: '📁',
      color: '#FAAD14',
      action: () => router.pushUrl({ url: 'pages/Documents' })
    },
    {
      id: '12',
      title: '更多功能',
      icon: '⋮',
      color: '#8C8C8C',
      action: () => {}
    }
  ]

  // 显示公告
  private showAnnouncements(): void {
    // 可以弹出公告列表弹窗
  }

  // 格式化时间
  private formatRelativeTime(timestamp: number): string {
    const now = Date.now()
    const diff = now - timestamp
    const hour = 3600000
    const day = 86400000

    if (diff < hour) {
      return Math.floor(diff / 60000) + '分钟前'
    } else if (diff < day) {
      return Math.floor(diff / hour) + '小时前'
    } else if (diff < 7 * day) {
      return Math.floor(diff / day) + '天前'
    } else {
      return new Date(timestamp).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
    }
  }

  build() {
    Scroll() {
      Column() {
        // 顶部欢迎区域
        this.buildWelcomeSection()

        // 轮播公告
        this.buildAnnouncementSection()

        // 统计卡片区域
        this.buildStatsSection()

        // 快捷操作区域
        this.buildQuickActionsSection()

        // 考勤快捷入口
        this.buildAttendanceCard()

        // 最新动态区域
        this.buildRecentActivitySection()
      }
      .padding(16)
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
    .scrollable(ScrollDirection.Vertical)
    .scrollBar(BarState.Off)
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
      .layoutWeight(1)

      Image($r('app.media.icon_avatar'))
        .width(48)
        .height(48)
        .borderRadius(24)
        .onClick(() => {
          router.pushUrl({ url: 'pages/Profile' })
        })
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
  }

  // 轮播公告
  @Builder
  buildAnnouncementSection() {
    Column() {
      Swiper(this.swiperController) {
        ForEach(this.state.announcements, (announcement: Announcement) => {
          Column() {
            Row() {
              Text(announcement.title)
                .fontSize(14)
                .fontWeight(FontWeight.Medium)
                .fontColor('#333333')

              Blank()

              Text(this.formatRelativeTime(announcement.time))
                .fontSize(11)
                .fontColor('#999999')
            }
            .width('100%')
            .margin({ bottom: 6 })

            Text(announcement.content)
              .fontSize(13)
              .fontColor('#666666')
              .maxLines(2)
              .textOverflow({ overflow: TextOverflow.Ellipsis })
          }
          .width('100%')
          .padding(12)
          .backgroundColor('#F0F5FF')
          .borderRadius(8)
        })
      }
      .width('100%')
      .height(80)
      .autoPlay(true)
      .interval(5000)
      .indicator({
        bottom: 4,
        selectedColor: '#1890FF',
        indicatorColor: '#E0E0E0'
      })
    }
    .width('100%')
    .margin({ bottom: 12 })
  }

  // 统计卡片区域
  @Builder
  buildStatsSection() {
    Row() {
      this.buildStatCard('今日任务', this.state.todayTasks.toString(), '#1890FF', () => {
        router.pushUrl({ url: 'pages/TaskList' })
      })
      this.buildStatCard('待审批', this.state.pendingApprovals.toString(), '#FAAD14', () => {
        router.pushUrl({ url: 'pages/ApprovalList' })
      })
      this.buildStatCard('未读消息', this.state.unreadMessages.toString(), '#FF4D4F', () => {
        router.pushUrl({ url: 'pages/MessageList' })
      })
      this.buildStatCard('考勤状态', this.state.attendanceStatus?.checkedIn ? '✓' : '—', '#52C41A', () => {
        router.pushUrl({ url: 'pages/CheckIn' })
      })
    }
    .width('100%')
    .margin({ bottom: 12 })
  }

  @Builder
  buildStatCard(title: string, value: string, color: string, onClick: () => void) {
    Column() {
      Text(value)
        .fontSize(22)
        .fontWeight(FontWeight.Bold)
        .fontColor(color)

      Text(title)
        .fontSize(11)
        .fontColor('#666666')
        .margin({ top: 4 })
    }
    .width('23%')
    .padding(12)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .alignItems(HorizontalAlign.Center)
    .onClick(onClick)
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

      // 第一行
      Grid() {
        ForEach(this.quickActions, (action: QuickAction) => {
          GridItem() {
            Column() {
              Text(action.icon)
                .fontSize(26)

              Text(action.title)
                .fontSize(12)
                .fontColor('#666666')
                .margin({ top: 6 })
            }
            .width('100%')
            .padding(10)
            .onClick(action.action)
          }
          .width('25%')
        })
      }
      .columnsTemplate('1fr 1fr 1fr 1fr')
      .rowsTemplate('1fr')
      .width('100%')
      .height(90)
      .columnsGap(8)
      .rowsGap(8)

      // 第二行
      Grid() {
        ForEach(this.quickEntries, (action: QuickAction) => {
          GridItem() {
            Column() {
              Text(action.icon)
                .fontSize(26)

              Text(action.title)
                .fontSize(12)
                .fontColor('#666666')
                .margin({ top: 6 })
            }
            .width('100%')
            .padding(10)
            .onClick(action.action)
          }
          .width('25%')
        })
      }
      .columnsTemplate('1fr 1fr 1fr 1fr')
      .rowsTemplate('1fr')
      .width('100%')
      .height(90)
      .columnsGap(8)
      .rowsGap(8)
      .margin({ top: 8 })
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
  }

  // 考勤快捷入口卡片
  @Builder
  buildAttendanceCard() {
    Column() {
      Row() {
        Column() {
          Text('📍 今日考勤')
            .fontSize(16)
            .fontWeight(FontWeight.Medium)
            .fontColor('#333333')

          Row() {
            if (this.state.attendanceStatus?.checkedIn) {
              Text(`上班 ${this.state.attendanceStatus.checkInTime || '--:--'}`)
                .fontSize(13)
                .fontColor('#52C41A')
                .margin({ right: 16 })
            } else {
              Text('上班 --:--')
                .fontSize(13)
                .fontColor('#FF4D4F')
                .margin({ right: 16 })
            }

            if (this.state.attendanceStatus?.checkedOut) {
              Text(`下班 ${this.state.attendanceStatus.checkOutTime || '--:--'}`)
                .fontSize(13)
                .fontColor('#52C41A')
            } else if (this.state.attendanceStatus?.checkedIn) {
              Text('下班 --:--')
                .fontSize(13)
                .fontColor('#FAAD14')
            }
          }
          .margin({ top: 8 })
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1)

        Row() {
          if (!this.state.attendanceStatus?.checkedIn) {
            Button('上班打卡')
              .fontSize(13)
              .fontColor('#FFFFFF')
              .backgroundColor('#1890FF')
              .borderRadius(16)
              .height(32)
              .padding({ left: 16, right: 16 })
              .onClick(() => router.pushUrl({ url: 'pages/CheckIn' }))
          } else if (!this.state.attendanceStatus?.checkedOut) {
            Button('下班打卡')
              .fontSize(13)
              .fontColor('#FFFFFF')
              .backgroundColor('#52C41A')
              .borderRadius(16)
              .height(32)
              .padding({ left: 16, right: 16 })
              .onClick(() => router.pushUrl({ url: 'pages/CheckIn' }))
          } else {
            Text('已完成')
              .fontSize(13)
              .fontColor('#52C41A')
              .backgroundColor('#52C41A20')
              .borderRadius(16)
              .padding({ left: 16, right: 16, top: 6, bottom: 6 })
          }
        }
      }
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
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

      // 动态列表
      Column() {
        // 动态项1
        Row() {
          Text('✅')
            .fontSize(20)
            .margin({ right: 12 })

          Column() {
            Text('您的请假申请已通过')
              .fontSize(14)
              .fontColor('#333333')

            Text('2小时前')
              .fontSize(11)
              .fontColor('#999999')
              .margin({ top: 4 })
          }
          .alignItems(HorizontalAlign.Start)
          .layoutWeight(1)
        }
        .width('100%')
        .padding({ top: 12, bottom: 12 })

        Divider().color('#F0F0F0')

        // 动态项2
        Row() {
          Text('📢')
            .fontSize(20)
            .margin({ right: 12 })

          Column() {
            Text('收到张三发来的消息')
              .fontSize(14)
              .fontColor('#333333')

            Text('好的，收到。我会准时参加。')
              .fontSize(12)
              .fontColor('#999999')
              .maxLines(1)
              .textOverflow({ overflow: TextOverflow.Ellipsis })
              .margin({ top: 4 })

            Text('3小时前')
              .fontSize(11)
              .fontColor('#999999')
              .margin({ top: 4 })
          }
          .alignItems(HorizontalAlign.Start)
          .layoutWeight(1)
        }
        .width('100%')
        .padding({ top: 12, bottom: 12 })

        Divider().color('#F0F0F0')

        // 动态项3
        Row() {
          Text('📋')
            .fontSize(20)
            .margin({ right: 12 })

          Column() {
            Text('收到一项待审批申请')
              .fontSize(14)
              .fontColor('#333333')

            Text('李四 - 加班申请')
              .fontSize(12)
              .fontColor('#666666')
              .margin({ top: 4 })

            Text('昨天')
              .fontSize(11)
              .fontColor('#999999')
              .margin({ top: 4 })
          }
          .alignItems(HorizontalAlign.Start)
          .layoutWeight(1)
        }
        .width('100%')
        .padding({ top: 12, bottom: 12 })
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
