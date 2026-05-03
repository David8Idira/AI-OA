import router from '@ohos.router'

// AI-OA 考勤打卡页面
@Entry
@Component
struct CheckInPage {
  @State checkinTime: string = '--:--:--'
  @State checkoutTime: string = '--:--:--'
  @State hasCheckedIn: boolean = false
  @State hasCheckedOut: boolean = false
  @State currentDate: string = ''

  aboutToAppear() {
    // 获取当前日期
    this.currentDate = this.getCurrentDate()
    // 模拟已有打卡记录
    this.checkinTime = '09:00:00'
    this.hasCheckedIn = true
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
        Button(this.hasCheckedIn && !this.hasCheckedOut ? '下班打卡' : '上班打卡')
          .width('60%')
          .height(48)
          .margin({ top: 30 })
          .borderRadius(24)
          .backgroundColor('#1890FF')
          .onClick(() => {
            this.doCheckIn()
          })
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

  private doCheckIn() {
    const now = new Date()
    const time = now.toLocaleTimeString('zh-CN', { hour12: false })
    
    if (!this.hasCheckedIn) {
      this.checkinTime = time
      this.hasCheckedIn = true
    } else if (!this.hasCheckedOut) {
      this.checkoutTime = time
      this.hasCheckedOut = true
    }
  }

  private getCurrentDate(): string {
    const now = new Date()
    return now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
  }
}
