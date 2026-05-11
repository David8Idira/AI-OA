import router from '@ohos.router'
import attendanceService from '../../../../commons/src/main/ets/services/AttendanceService'

// AI-OA 考勤打卡页面
@Entry
@Component
struct CheckInPage {
  @State checkinTime: string = '--:--:--'
  @State checkoutTime: string = '--:--:--'
  @State hasCheckedIn: boolean = false
  @State hasCheckedOut: boolean = false
  @State currentDate: string = ''
  @State isLoading: boolean = false
  @State errorMsg: string = ''
  @State canCheckIn: boolean = true
  @State canCheckOut: boolean = false

  aboutToAppear() {
    this.currentDate = this.getCurrentDate()
    this.loadTodayStatus()
  }

  async loadTodayStatus() {
    this.isLoading = true
    this.errorMsg = ''
    try {
      const res = await attendanceService.getTodayStatus()
      if (res.code === 0 || res.code === 200) {
        const data = res.data
        if (data.checkInRecord) {
          this.checkinTime = this.formatTime(data.checkInRecord.checkInTime)
          this.hasCheckedIn = true
          this.canCheckOut = true
        }
        if (data.checkOutRecord) {
          this.checkoutTime = this.formatTime(data.checkOutRecord.checkInTime)
          this.hasCheckedOut = true
          this.canCheckOut = false
        }
        this.canCheckIn = data.canCheckIn
      } else {
        this.errorMsg = res.message || '加载失败'
        // 降级：使用本地时间模拟
        this.initLocalTime()
      }
    } catch (e) {
      this.errorMsg = '网络异常，使用本地打卡模式'
      // 降级：使用本地时间
      this.initLocalTime()
    } finally {
      this.isLoading = false
    }
  }

  private initLocalTime() {
    const now = new Date()
    this.checkinTime = now.toLocaleTimeString('zh-CN', { hour12: false })
    this.hasCheckedIn = true
    this.hasCheckedOut = false
    this.canCheckIn = false
    this.canCheckOut = true
  }

  async doCheckIn() {
    if (this.isLoading) return
    this.isLoading = true
    this.errorMsg = ''

    try {
      const now = new Date()
      const type = (!this.hasCheckedIn) ? 'check_in' : 'check_out'

      const res = await attendanceService.checkIn({
        type: type,
        latitude: 0,  // TODO: 获取真实位置
        longitude: 0,
        location: '未知位置'
      })

      if (res.code === 0 || res.code === 200) {
        const time = now.toLocaleTimeString('zh-CN', { hour12: false })
        if (type === 'check_in') {
          this.checkinTime = time
          this.hasCheckedIn = true
          this.canCheckIn = false
          this.canCheckOut = true
        } else {
          this.checkoutTime = time
          this.hasCheckedOut = true
          this.canCheckOut = false
        }
      } else {
        // API调用失败，降级到本地模式
        this.degradeCheckIn(type, now)
      }
    } catch (e) {
      // 网络异常，降级到本地模式
      const type = (!this.hasCheckedIn) ? 'check_in' : 'check_out'
      this.degradeCheckIn(type, new Date())
    } finally {
      this.isLoading = false
    }
  }

  private degradeCheckIn(type: string, now: Date) {
    const time = now.toLocaleTimeString('zh-CN', { hour12: false })
    if (type === 'check_in') {
      this.checkinTime = time
      this.hasCheckedIn = true
      this.canCheckIn = false
      this.canCheckOut = true
    } else {
      this.checkoutTime = time
      this.hasCheckedOut = true
      this.canCheckOut = false
    }
  }

  build() {
    Column() {
      // 顶部标题
      Text('考勤打卡')
        .fontSize(24)
        .fontWeight(FontWeight.Bold)
        .margin({ top: 20 })

      // 当前日期
      Text(this.currentDate)
        .fontSize(16)
        .fontColor('#666666')
        .margin({ top: 8 })

      // 错误提示
      if (this.errorMsg) {
        Text('⚠️ ' + this.errorMsg)
          .fontSize(12)
          .fontColor('#FA8C16')
          .backgroundColor('#FFF7E6')
          .padding(8)
          .borderRadius(8)
          .margin({ top: 8 })
      }

      // 打卡卡片
      Column() {
        Row() {
          Column() {
            Text('上班打卡')
              .fontSize(14)
              .fontColor('#666666')
            Text(this.checkinTime)
              .fontSize(32)
              .fontWeight(FontWeight.Bold)
              .margin({ top: 8 })
            if (!this.hasCheckedIn) {
              Text('未打卡')
                .fontSize(12)
                .fontColor('#FF4D4F')
            } else {
              Text('已打卡')
                .fontSize(12)
                .fontColor('#52C41A')
            }
          }
          .width('50%')
          .height(120)
          .justifyContent(FlexAlign.Center)

          Column() {
            Text('下班打卡')
              .fontSize(14)
              .fontColor('#666666')
            Text(this.checkoutTime)
              .fontSize(32)
              .fontWeight(FontWeight.Bold)
              .margin({ top: 8 })
            if (!this.hasCheckedOut) {
              Text('未打卡')
                .fontSize(12)
                .fontColor('#FF4D4F')
            } else {
              Text('已打卡')
                .fontSize(12)
                .fontColor('#52C41A')
            }
          }
          .width('50%')
          .height(120)
          .justifyContent(FlexAlign.Center)
        }
        .width('100%')

        // 打卡按钮
        if (this.isLoading) {
          LoadingProgress()
            .width(32)
            .height(32)
            .margin({ top: 30 })
        } else {
          Button(this.hasCheckedIn && !this.hasCheckedOut ? '下班打卡' : '上班打卡')
            .width('60%')
            .height(48)
            .margin({ top: 30 })
            .borderRadius(24)
            .backgroundColor(this.canCheckIn || this.canCheckOut ? '#1890FF' : '#CCCCCC')
            .enabled(this.canCheckIn || this.canCheckOut)
            .onClick(() => {
              this.doCheckIn()
            })
        }
      }
      .width('90%')
      .backgroundColor('#FFFFFF')
      .borderRadius(16)
      .padding(20)
      .margin({ top: 40 })

      // 快捷操作
      Column() {
        Row() {
          Text('快捷操作')
            .fontSize(16)
            .fontWeight(FontWeight.Medium)
        }
        .width('90%')
        .margin({ top: 30, bottom: 12 })

        Row() {
          Button('请假申请')
            .height(40)
            .borderRadius(20)
            .backgroundColor('#FFF7E6')
            .fontColor('#FA8C16')
            .onClick(() => {
              router.pushUrl({ url: 'pages/attendance/LeaveApply' })
            })
          Button('查看记录')
            .height(40)
            .borderRadius(20)
            .backgroundColor('#E6F7FF')
            .fontColor('#1890FF')
            .margin({ left: 12 })
            .onClick(() => {
              router.pushUrl({ url: 'pages/attendance/AttendanceRecord' })
            })
        }
        .width('90%')
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }

  private formatTime(timestamp: number): string {
    const date = new Date(timestamp)
    return date.toLocaleTimeString('zh-CN', { hour12: false })
  }

  private getCurrentDate(): string {
    const now = new Date()
    return now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
  }
}