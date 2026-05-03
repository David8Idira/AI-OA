import router from '@ohos.router'

// AI-OA 请假申请页面
@Entry
@Component
struct LeaveApply {
  @State leaveType: string = '年假'
  @State startDate: string = ''
  @State endDate: string = ''
  @State reason: string = ''
  @State isSubmitting: boolean = false

  build() {
    Column() {
      // 顶部
      Row() {
        Text('<')
          .fontSize(24)
          .onClick(() => {
            router.back()
          })
        Text('请假申请')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .margin({ left: 20 })
      }
      .width('90%')
      .height(50)
      .margin({ top: 20 })

      Scroll() {
        Column() {
          // 请假类型
          Column() {
            Text('请假类型')
              .fontSize(14)
              .fontColor('#666666')
              .width('90%')
              .margin({ top: 20, bottom: 8 })
            
            Row() {
              ForEach(['年假', '病假', '事假', '其他'], (type: string) => {
                Text(type)
                  .fontSize(14)
                  .padding({ left: 16, right: 16, top: 8, bottom: 8 })
                  .backgroundColor(this.leaveType === type ? '#1890FF' : '#F5F5F5')
                  .fontColor(this.leaveType === type ? '#FFFFFF' : '#333333')
                  .borderRadius(16)
                  .margin({ right: 8 })
                  .onClick(() => {
                    this.leaveType = type
                  })
              })
            }
            .width('90%')
          }
          .width('100%')

          // 开始日期
          Column() {
            Text('开始日期')
              .fontSize(14)
              .fontColor('#666666')
              .width('90%')
              .margin({ top: 20, bottom: 8 })
            
            Row() {
              Text(this.startDate || '选择开始日期')
                .fontSize(16)
                .fontColor(this.startDate ? '#333333' : '#999999')
              Text('📅')
                .margin({ left: 8 })
            }
            .width('90%')
            .height(48)
            .backgroundColor('#F5F5F5')
            .borderRadius(8)
            .padding({ left: 16 })
            .onClick(() => {
              // 日期选择
              this.startDate = '2026-05-04'
            })
          }
          .width('100%')

          // 结束日期
          Column() {
            Text('结束日期')
              .fontSize(14)
              .fontColor('#666666')
              .width('90%')
              .margin({ top: 20, bottom: 8 })
            
            Row() {
              Text(this.endDate || '选择结束日期')
                .fontSize(16)
                .fontColor(this.endDate ? '#333333' : '#999999')
              Text('📅')
                .margin({ left: 8 })
            }
            .width('90%')
            .height(48)
            .backgroundColor('#F5F5F5')
            .borderRadius(8)
            .padding({ left: 16 })
            .onClick(() => {
              // 日期选择
              this.endDate = '2026-05-05'
            })
          }
          .width('100%')

          // 请假原因
          Column() {
            Text('请假原因')
              .fontSize(14)
              .fontColor('#666666')
              .width('90%')
              .margin({ top: 20, bottom: 8 })
            
            TextArea({ placeholder: '请输入请假原因...' })
              .width('90%')
              .height(100)
              .borderRadius(8)
              .backgroundColor('#F5F5F5')
              .onChange((value: string) => {
                this.reason = value
              })
          }
          .width('100%')

          // 提交按钮
          Button('提交申请')
            .width('80%')
            .height(48)
            .margin({ top: 40 })
            .borderRadius(24)
            .backgroundColor('#1890FF')
            .enabled(!this.isSubmitting && this.startDate !== '' && this.endDate !== '')
            .onClick(() => {
              this.submitLeave()
            })
        }
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#FFFFFF')
  }

  private submitLeave() {
    this.isSubmitting = true
    
    setTimeout(() => {
      // 提交成功
      this.isSubmitting = false
      router.back()
    }, 1500)
  }
}
