import router from '@ohos.router'

// AI-OA 考勤记录页面
@Entry
@Component
struct AttendanceRecord {
  @State records: any[] = [
    { date: '2026-05-02', checkin: '09:00:00', checkout: '18:00:00', status: '正常' },
    { date: '2026-05-01', checkin: '08:55:00', checkout: '18:05:00', status: '正常' },
    { date: '2026-04-30', checkin: '09:10:00', checkout: '18:00:00', status: '迟到' },
  ]

  build() {
    Column() {
      // 顶部
      Row() {
        Text('<')
          .fontSize(24)
          .onClick(() => {
            router.back()
          })
        Text('考勤记录')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .margin({ left: 20 })
      }
      .width('90%')
      .height(50)
      .margin({ top: 20 })

      // 统计卡片
      Row() {
        Column() {
          Text('正常')
            .fontSize(14)
            .fontColor('#52C41A')
          Text('18')
            .fontSize(24)
            .fontWeight(FontWeight.Bold)
            .margin({ top: 4 })
          Text('天')
            .fontSize(12)
            .fontColor('#999999')
        }
        .width('33%')
        .height(80)
        .backgroundColor('#F6FFED')
        .borderRadius(12)
        .justifyContent(FlexAlign.Center)

        Column() {
          Text('迟到')
            .fontSize(14)
            .fontColor('#FA8C16')
          Text('2')
            .fontSize(24)
            .fontWeight(FontWeight.Bold)
            .margin({ top: 4 })
          Text('天')
            .fontSize(12)
            .fontColor('#999999')
        }
        .width('33%')
        .margin({ left: 8 })
        .height(80)
        .backgroundColor('#FFF7E6')
        .borderRadius(12)
        .justifyContent(FlexAlign.Center)

        Column() {
          Text('缺勤')
            .fontSize(14)
            .fontColor('#FF4D4F')
          Text('0')
            .fontSize(24)
            .fontWeight(FontWeight.Bold)
            .margin({ top: 4 })
          Text('天')
            .fontSize(12)
            .fontColor('#999999')
        }
        .width('33%')
        .margin({ left: 8 })
        .height(80)
        .backgroundColor('#FFF1F0')
        .borderRadius(12)
        .justifyContent(FlexAlign.Center)
      }
      .width('90%')
      .margin({ top: 20 })

      // 记录列表
      List() {
        ForEach(this.records, (record: any, index: number) => {
          ListItem() {
            Row() {
              Column() {
                Text(record.date)
                  .fontSize(16)
                  .fontWeight(FontWeight.Medium)
                Text('上班: ' + record.checkin + ' | 下班: ' + record.checkout)
                  .fontSize(12)
                  .fontColor('#999999')
                  .margin({ top: 4 })
              }
              .width('70%')
              .alignItems(HorizontalAlign.Start)

              Text(record.status)
                .fontSize(14)
                .fontColor(record.status === '正常' ? '#52C41A' : '#FA8C16')
            }
            .width('90%')
            .height(60)
            .padding({ top: 12, bottom: 12 })
            .borderWidth({ bottom: 1 })
            .borderColor('#F0F0F0')
          }
        })
      }
      .width('90%')
      .margin({ top: 20 })
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#FFFFFF')
  }
}
