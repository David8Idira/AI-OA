// 考勤记录页面
import router from '@ohos.router'
import { CheckInRecord, CheckInStatus, AttendanceMonthSummary } from '../../data/model/Attendance'

// 页面状态
class AttendanceRecordState {
  records: CheckInRecord[] = []
  currentMonth: number = new Date().getMonth() + 1
  currentYear: number = new Date().getFullYear()
  summary: AttendanceMonthSummary | null = null
  loading: boolean = false
  selectedMonth: number = new Date().getMonth() + 1
}

@Entry
@Component
struct AttendanceRecord {
  @State state: AttendanceRecordState = new AttendanceRecordState()

  aboutToAppear() {
    this.loadData()
  }

  // 加载数据
  async loadData(): Promise<void> {
    this.state.loading = true

    // 模拟月统计数据
    this.state.summary = {
      year: this.state.currentYear,
      month: this.state.currentMonth,
      userId: 'current_user',
      normalDays: 18,
      lateDays: 1,
      earlyLeaveDays: 0,
      absentDays: 0,
      restDays: 4,
      holidayDays: 2,
      workDays: 22,
      actualWorkDays: 19,
      totalWorkHours: 152,
      overtimeHours: 6
    }

    // 模拟打卡记录
    const now = Date.now()
    const records: CheckInRecord[] = []
    for (let i = 0; i < 10; i++) {
      const date = new Date(now - 86400000 * i)
      const dateStr = date.toISOString().split('T')[0]

      // 跳过周末
      if (date.getDay() === 0 || date.getDay() === 6) continue

      records.push({
        id: `r${i}`,
        userId: 'current_user',
        userName: '当前用户',
        date: dateStr,
        workStartTime: now - 86400000 * i + 3600000 * 9,
        workEndTime: now - 86400000 * i + 3600000 * 18,
        workStartStatus: i === 0 ? CheckInStatus.NORMAL : (Math.random() > 0.1 ? CheckInStatus.NORMAL : CheckInStatus.LATE),
        workEndStatus: i === 0 ? CheckInStatus.NORMAL : (Math.random() > 0.1 ? CheckInStatus.NORMAL : CheckInStatus.LEAVE_EARLY),
        workStartLocation: '公司总部',
        workEndLocation: '公司总部',
        createdAt: now - 86400000 * i
      })
    }
    this.state.records = records

    this.state.loading = false
  }

  // 格式化时间
  private formatTime(timestamp: number | undefined): string {
    if (!timestamp) return '--:--'
    return new Date(timestamp).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  // 切换月份
  private changeMonth(delta: number): void {
    this.state.selectedMonth += delta
    if (this.state.selectedMonth > 12) {
      this.state.selectedMonth = 1
      this.state.currentYear++
    } else if (this.state.selectedMonth < 1) {
      this.state.selectedMonth = 12
      this.state.currentYear--
    }
    this.loadData()
  }

  // 获取状态颜色和文字
  private getStatusStyle(status: CheckInStatus | undefined): { color: string; text: string } {
    if (!status) return { color: '#999999', text: '--' }
    switch (status) {
      case CheckInStatus.NORMAL:
        return { color: '#52C41A', text: '正常' }
      case CheckInStatus.LATE:
        return { color: '#FAAD14', text: '迟到' }
      case CheckInStatus.LEAVE_EARLY:
        return { color: '#FAAD14', text: '早退' }
      case CheckInStatus.ABSENT:
        return { color: '#FF4D4F', text: '缺勤' }
      default:
        return { color: '#999999', text: '--' }
    }
  }

  build() {
    Column() {
      // 顶部导航栏
      this.buildNavBar()

      // 月份选择器
      this.buildMonthSelector()

      // 考勤汇总
      this.buildSummaryCard()

      // 打卡记录列表
      if (this.state.loading) {
        Blank()
        Text('加载中...')
          .fontSize(14)
          .fontColor('#999999')
        Blank()
      } else {
        List() {
          ForEach(this.state.records, (record: CheckInRecord) => {
            ListItem() {
              this.buildRecordItem(record)
            }
          })
        }
        .width('100%')
        .layoutWeight(1)
        .padding({ left: 16, right: 16 })
        .backgroundColor('#F5F5F5')
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }

  @Builder
  buildNavBar() {
    Row() {
      Image($r('app.media.icon_back'))
        .width(24)
        .height(24)
        .onClick(() => router.back())

      Text('考勤记录')
        .fontSize(18)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .margin({ left: 16 })
        .layoutWeight(1)
    }
    .width('100%')
    .padding({ left: 16, right: 16, top: 12, bottom: 12 })
    .backgroundColor('#FFFFFF')
  }

  @Builder
  buildMonthSelector() {
    Row() {
      Text('<')
        .fontSize(20)
        .fontColor('#1890FF')
        .padding(12)
        .onClick(() => this.changeMonth(-1))

      Text(`${this.state.currentYear}年${this.state.selectedMonth}月`)
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .layoutWeight(1)
        .textAlign(TextAlign.Center)

      Text('>')
        .fontSize(20)
        .fontColor('#1890FF')
        .padding(12)
        .onClick(() => this.changeMonth(1))
    }
    .width('100%')
    .padding({ left: 16, right: 16, top: 8, bottom: 8 })
    .backgroundColor('#FFFFFF')
  }

  @Builder
  buildSummaryCard() {
    Column() {
      Row() {
        Column() {
          Text('出勤天数')
            .fontSize(12)
            .fontColor('#666666')

          Text(`${this.state.summary?.actualWorkDays || 0}/${this.state.summary?.workDays || 0}`)
            .fontSize(24)
            .fontWeight(FontWeight.Bold)
            .fontColor('#333333')
            .margin({ top: 4 })

          Text('天')
            .fontSize(12)
            .fontColor('#999999')
        }
        .layoutWeight(1)

        Column() {
          Text('工作时长')
            .fontSize(12)
            .fontColor('#666666')

          Text(`${this.state.summary?.totalWorkHours || 0}`)
            .fontSize(24)
            .fontWeight(FontWeight.Bold)
            .fontColor('#333333')
            .margin({ top: 4 })

          Text('小时')
            .fontSize(12)
            .fontColor('#999999')
        }
        .layoutWeight(1)

        Column() {
          Text('加班时长')
            .fontSize(12)
            .fontColor('#666666')

          Text(`${this.state.summary?.overtimeHours || 0}`)
            .fontSize(24)
            .fontWeight(FontWeight.Bold)
            .fontColor('#1890FF')
            .margin({ top: 4 })

          Text('小时')
            .fontSize(12)
            .fontColor('#999999')
        }
        .layoutWeight(1)
      }
      .width('100%')

      Divider()
        .color('#F0F0F0')
        .margin({ top: 16, bottom: 16 })

      Row() {
        SummaryTag({ label: '正常', count: this.state.summary?.normalDays || 0, color: '#52C41A' })
        SummaryTag({ label: '迟到', count: this.state.summary?.lateDays || 0, color: '#FAAD14' })
        SummaryTag({ label: '早退', count: this.state.summary?.earlyLeaveDays || 0, color: '#FAAD14' })
        SummaryTag({ label: '缺勤', count: this.state.summary?.absentDays || 0, color: '#FF4D4F' })
      }
      .width('100%')
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .margin({ left: 16, right: 16, bottom: 12 })
    .borderRadius(12)
  }

  @Builder
  buildRecordItem(record: CheckInRecord) {
    Column() {
      Row() {
        Text(record.date)
          .fontSize(14)
          .fontColor('#333333')
          .fontWeight(FontWeight.Medium)

        Blank()

        const startStyle = this.getStatusStyle(record.workStartStatus)
        const endStyle = this.getStatusStyle(record.workEndStatus)

        Text(`上班 ${startStyle.text}`)
          .fontSize(12)
          .fontColor(startStyle.color)
          .backgroundColor(startStyle.color + '20')
          .borderRadius(4)
          .padding({ left: 6, right: 6, top: 2, bottom: 2 })
          .margin({ right: 8 })

        Text(`下班 ${endStyle.text}`)
          .fontSize(12)
          .fontColor(endStyle.color)
          .backgroundColor(endStyle.color + '20')
          .borderRadius(4)
          .padding({ left: 6, right: 6, top: 2, bottom: 2 })
      }
      .width('100%')
      .margin({ bottom: 8 })

      Row() {
        Column() {
          Text('上班打卡')
            .fontSize(12)
            .fontColor('#999999')

          Text(this.formatTime(record.workStartTime))
            .fontSize(14)
            .fontColor('#333333')
            .margin({ top: 2 })
        }

        Column() {
          Text('下班打卡')
            .fontSize(12)
            .fontColor('#999999')

          Text(this.formatTime(record.workEndTime))
            .fontSize(14)
            .fontColor('#333333')
            .margin({ top: 2 })
        }

        Blank()

        Column() {
          Text('打卡地点')
            .fontSize(12)
            .fontColor('#999999')

          Text(record.workStartLocation || '--')
            .fontSize(13)
            .fontColor('#666666')
            .margin({ top: 2 })
        }
      }
      .width('100%')
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 8 })
  }
}

// 汇总标签组件
@Component
struct SummaryTag {
  @Prop label: string
  @Prop count: number
  @Prop color: string

  build() {
    Row() {
      Circle()
        .size({ width: 8, height: 8 })
        .fill(this.color)
        .margin({ right: 4 })

      Text(`${this.label} ${this.count}天`)
        .fontSize(12)
        .fontColor('#666666')
    }
    .layoutWeight(1)
    .justifyContent(FlexAlign.Center)
  }
}
