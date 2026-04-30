// 考勤打卡页面
import router from '@ohos.router'
import promptAction from '@ohos.promptAction'
import { CheckInRecord, CheckInType, CheckInStatus, AttendanceRule, AttendanceStatistics } from '../../data/model/Attendance'

// 页面状态
class CheckInPageState {
  currentTime: string = ''
  currentDate: string = ''
  weekday: string = ''
  statistics: AttendanceStatistics | null = null
  todayRecord: CheckInRecord | null = null
  recentRecords: CheckInRecord[] = []
  location: string = '当前地点'
  distance: number = 0
  loading: boolean = false
  canCheckIn: boolean = true
  rule: AttendanceRule | null = null
  private timer: number = -1
}

@Entry
@Component
struct CheckInPage {
  @State state: CheckInPageState = new CheckInPageState()

  aboutToAppear() {
    this.updateTime()
    this.loadData()
    // 启动定时器每秒更新时间
    this.state.timer = setInterval(() => {
      this.updateTime()
    }, 1000)
  }

  aboutToDisappear() {
    if (this.state.timer !== -1) {
      clearInterval(this.state.timer)
    }
  }

  // 更新时间显示
  private updateTime(): void {
    const now = new Date()
    this.state.currentTime = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    this.state.currentDate = now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    this.state.weekday = weekdays[now.getDay()]
  }

  // 加载数据
  async loadData(): Promise<void> {
    this.state.loading = true

    // 模拟考勤规则
    this.state.rule = {
      workStartTime: '09:00',
      workEndTime: '18:00',
      workDays: [1, 2, 3, 4, 5],
      lateThreshold: 15,
      earlyLeaveThreshold: 30,
      locationName: '公司总部',
      latitude: 31.2304,
      longitude: 121.4737,
      range: 200
    }

    // 模拟统计数据
    this.state.statistics = {
      todayCheckIn: false,
      todayCheckOut: false,
      thisMonthNormal: 18,
      thisMonthLate: 1,
      thisMonthAbsent: 0,
      thisMonthOvertime: 6
    }

    // 模拟今日打卡记录
    const today = new Date().toISOString().split('T')[0]
    this.state.todayRecord = {
      id: 'record1',
      userId: 'current_user',
      userName: '当前用户',
      date: today,
      workStartTime: Date.now() - 3600000 * 8,
      workEndTime: undefined,
      workStartStatus: CheckInStatus.NORMAL,
      workEndStatus: undefined,
      workStartLocation: '公司总部',
      workEndLocation: undefined,
      createdAt: Date.now() - 3600000 * 8
    }
    this.state.statistics.todayCheckIn = true

    // 模拟最近打卡记录
    const now = Date.now()
    this.state.recentRecords = [
      {
        id: 'r1',
        userId: 'current_user',
        userName: '当前用户',
        date: today,
        workStartTime: now - 3600000 * 8,
        workEndTime: now - 3600000,
        workStartStatus: CheckInStatus.NORMAL,
        workEndStatus: CheckInStatus.NORMAL,
        workStartLocation: '公司总部',
        workEndLocation: '公司总部',
        createdAt: now - 3600000 * 8
      }
    ]

    this.state.loading = false
  }

  // 执行打卡
  async performCheckIn(type: CheckInType): Promise<void> {
    this.state.loading = true

    // 模拟打卡API调用
    // const res = await ApiClient.checkIn({
    //   type: type,
    //   latitude: 31.2304,
    //   longitude: 121.4737,
    //   location: this.state.location
    // })

    setTimeout(() => {
      this.state.loading = false

      if (type === CheckInType.WORK_START) {
        this.state.statistics!.todayCheckIn = true
        this.state.todayRecord!.workStartTime = Date.now()
        this.state.todayRecord!.workStartStatus = CheckInStatus.NORMAL
        promptAction.showToast({ message: '上班打卡成功' })
      } else {
        this.state.statistics!.todayCheckOut = true
        this.state.todayRecord!.workEndTime = Date.now()
        this.state.todayRecord!.workEndStatus = CheckInStatus.NORMAL
        promptAction.showToast({ message: '下班打卡成功' })
      }

      // 刷新记录
      this.loadData()
    }, 1000)
  }

  // 格式化时间
  private formatTime(timestamp: number | undefined): string {
    if (!timestamp) return '--:--'
    return new Date(timestamp).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  build() {
    Column() {
      // 顶部导航栏
      this.buildNavBar()

      Scroll() {
        Column() {
          // 打卡卡片
          this.buildCheckInCard()

          // 考勤统计
          this.buildStatisticsCard()

          // 打卡记录
          this.buildRecordList()
        }
        .padding(16)
      }
      .width('100%')
      .layoutWeight(1)
      .backgroundColor('#F5F5F5')
    }
    .width('100%')
    .height('100%')
  }

  @Builder
  buildNavBar() {
    Row() {
      Image($r('app.media.icon_back'))
        .width(24)
        .height(24)
        .onClick(() => router.back())

      Text('考勤打卡')
        .fontSize(18)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .margin({ left: 16 })
        .layoutWeight(1)

      Text('考勤记录')
        .fontSize(14)
        .fontColor('#1890FF')
        .onClick(() => {
          router.pushUrl({ url: 'pages/AttendanceRecord' })
        })
    }
    .width('100%')
    .padding({ left: 16, right: 16, top: 12, bottom: 12 })
    .backgroundColor('#FFFFFF')
  }

  @Builder
  buildCheckInCard() {
    Column() {
      // 日期时间显示
      Text(this.state.currentDate)
        .fontSize(14)
        .fontColor('#666666')

      Text(this.state.weekday)
        .fontSize(14)
        .fontColor('#666666')
        .margin({ top: 4 })

      Text(this.state.currentTime)
        .fontSize(56)
        .fontWeight(FontWeight.Light)
        .fontColor('#333333')
        .margin({ top: 16 })

      // 位置信息
      Row() {
        Text('📍')
          .fontSize(14)

        Text(this.state.location)
          .fontSize(13)
          .fontColor('#666666')
          .margin({ left: 4 })

        Text(`距离${this.state.distance}m`)
          .fontSize(12)
          .fontColor('#999999')
          .margin({ left: 8 })
      }
      .margin({ top: 16 })

      // 打卡按钮区域
      Row() {
        // 上班打卡
        Column() {
          Text(this.state.statistics?.todayCheckIn ? '✓' : '🔔')
            .fontSize(32)
            .fontColor(this.state.statistics?.todayCheckIn ? '#52C41A' : '#1890FF')

          Text(this.state.statistics?.todayCheckIn ? '已打卡' : '上班打卡')
            .fontSize(14)
            .fontColor('#333333')
            .margin({ top: 8 })

          Text(this.formatTime(this.state.todayRecord?.workStartTime))
            .fontSize(12)
            .fontColor('#999999')
            .margin({ top: 4 })
        }
        .width('40%')
        .padding(20)
        .backgroundColor(this.state.statistics?.todayCheckIn ? '#52C41A15' : '#1890FF15')
        .borderRadius(16)
        .onClick(() => {
          if (!this.state.statistics?.todayCheckIn) {
            this.performCheckIn(CheckInType.WORK_START)
          }
        })

        // 下班打卡
        Column() {
          Text(this.state.statistics?.todayCheckOut ? '✓' : '🔒')
            .fontSize(32)
            .fontColor(this.state.statistics?.todayCheckOut ? '#52C41A' : '#FAAD14')

          Text(this.state.statistics?.todayCheckOut ? '已打卡' : '下班打卡')
            .fontSize(14)
            .fontColor('#333333')
            .margin({ top: 8 })

          Text(this.formatTime(this.state.todayRecord?.workEndTime))
            .fontSize(12)
            .fontColor('#999999')
            .margin({ top: 4 })
        }
        .width('40%')
        .padding(20)
        .backgroundColor(this.state.statistics?.todayCheckOut ? '#52C41A15' : '#FAAD1415')
        .borderRadius(16)
        .onClick(() => {
          if (this.state.statistics?.todayCheckIn && !this.state.statistics?.todayCheckOut) {
            this.performCheckIn(CheckInType.WORK_END)
          } else if (!this.state.statistics?.todayCheckIn) {
            promptAction.showToast({ message: '请先上班打卡' })
          }
        })
      }
      .width('100%')
      .justifyContent(FlexAlign.SpaceEvenly)
      .margin({ top: 24 })

      // 打卡时间说明
      if (this.state.rule) {
        Row() {
          Text(`上班时间: ${this.state.rule.workStartTime}`)
            .fontSize(12)
            .fontColor('#999999')

          Text('  |  ')
            .fontSize(12)
            .fontColor('#E0E0E0')

          Text(`下班时间: ${this.state.rule.workEndTime}`)
            .fontSize(12)
            .fontColor('#999999')
        }
        .margin({ top: 20 })
      }
    }
    .width('100%')
    .padding(24)
    .backgroundColor('#FFFFFF')
    .borderRadius(16)
    .margin({ bottom: 12 })
  }

  @Builder
  buildStatisticsCard() {
    Column() {
      Text('本月考勤')
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .alignSelf(HorizontalAlign.Start)
        .margin({ bottom: 16 })

      Row() {
        StatItem({ label: '正常', value: (this.state.statistics?.thisMonthNormal || 0).toString(), color: '#52C41A' })
        StatItem({ label: '迟到', value: (this.state.statistics?.thisMonthLate || 0).toString(), color: '#FAAD14' })
        StatItem({ label: '缺勤', value: (this.state.statistics?.thisMonthAbsent || 0).toString(), color: '#FF4D4F' })
        StatItem({ label: '加班', value: (this.state.statistics?.thisMonthOvertime || 0).toString(), color: '#1890FF' })
      }
      .width('100%')
      .justifyContent(FlexAlign.SpaceBetween)
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
  }

  @Builder
  buildRecordList() {
    Column() {
      Row() {
        Text('最近打卡记录')
          .fontSize(16)
          .fontWeight(FontWeight.Medium)
          .fontColor('#333333')

        Blank()

        Text('查看全部 >')
          .fontSize(13)
          .fontColor('#1890FF')
          .onClick(() => {
            router.pushUrl({ url: 'pages/AttendanceRecord' })
          })
      }
      .width('100%')
      .margin({ bottom: 12 })

      ForEach(this.state.recentRecords, (record: CheckInRecord) => {
        Row() {
          Column() {
            Text(record.date)
              .fontSize(14)
              .fontColor('#333333')

            Text(`${this.formatTime(record.workStartTime)} - ${this.formatTime(record.workEndTime)}`)
              .fontSize(12)
              .fontColor('#999999')
              .margin({ top: 4 })
          }
          .alignItems(HorizontalAlign.Start)
          .layoutWeight(1)

          Column() {
            Text('上班')
              .fontSize(12)
              .fontColor('#52C41A')

            Text(this.getStatusText(record.workStartStatus))
              .fontSize(11)
              .fontColor('#999999')
              .margin({ top: 2 })
          }
          .margin({ right: 16 })

          Column() {
            Text('下班')
              .fontSize(12)
              .fontColor('#1890FF')

            Text(this.getStatusText(record.workEndStatus))
              .fontSize(11)
              .fontColor('#999999')
              .margin({ top: 2 })
          }
        }
        .width('100%')
        .padding(12)
        .backgroundColor('#FAFAFA')
        .borderRadius(8)
        .margin({ bottom: 8 })
      })
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
  }

  private getStatusText(status: CheckInStatus | undefined): string {
    if (!status) return '--'
    switch (status) {
      case CheckInStatus.NORMAL: return '正常'
      case CheckInStatus.LATE: return '迟到'
      case CheckInStatus.LEAVE_EARLY: return '早退'
      case CheckInStatus.ABSENT: return '缺勤'
      default: return '--'
    }
  }
}

// 统计项组件
@Component
struct StatItem {
  @Prop label: string
  @Prop value: string
  @Prop color: string

  build() {
    Column() {
      Text(this.value)
        .fontSize(24)
        .fontWeight(FontWeight.Bold)
        .fontColor(this.color)

      Text(this.label)
        .fontSize(12)
        .fontColor('#666666')
        .margin({ top: 4 })
    }
    .width('25%')
    .padding(8)
  }
}
