import router from '@ohos.router'

// AI-OA AI 对话页面
@Entry
@Component
struct AiChatPage {
  @State inputText: string = ''
  @State messages: any[] = [
    { role: 'assistant', content: '您好！我是 AI-OA 智能助手，请问有什么可以帮助您的？' }
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
        Text('AI 助手')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .margin({ left: 20 })
        Text('🤖')
          .fontSize(20)
          .margin({ left: 8 })
      }
      .width('90%')
      .height(50)
      .margin({ top: 20 })

      // 消息列表
      List() {
        ForEach(this.messages, (msg: any, index: number) => {
          ListItem() {
            Row() {
              if (msg.role === 'user') {
                Blank()
                Text(msg.content)
                  .fontSize(14)
                  .padding(12)
                  .backgroundColor('#1890FF')
                  .fontColor('#FFFFFF')
                  .borderRadius(16)
              } else {
                Text('🤖')
                  .fontSize(20)
                  .margin({ right: 8 })
                Text(msg.content)
                  .fontSize(14)
                  .padding(12)
                  .backgroundColor('#F5F5F5')
                  .fontColor('#333333')
                  .borderRadius(16)
              }
            }
            .width('90%')
            .margin({ top: 8, bottom: 8 })
            .justifyContent(msg.role === 'user' ? FlexAlign.End : FlexAlign.Start)
          }
        })
      }
      .layoutWeight(1)
      .width('100%')
      .padding(10)

      // 输入区域
      Row() {
        TextInput({ placeholder: '输入问题...' })
          .layoutWeight(1)
          .height(40)
          .borderRadius(20)
          .backgroundColor('#F5F5F5')
          .onChange((value: string) => {
            this.inputText = value
          })

        Button('发送')
          .height(36)
          .borderRadius(18)
          .backgroundColor('#1890FF')
          .margin({ left: 8 })
          .onClick(() => {
            this.sendMessage()
          })
      }
      .width('90%')
      .height(50)
      .padding({ bottom: 20 })
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#FFFFFF')
  }

  private sendMessage() {
    if (!this.inputText.trim()) return

    // 添加用户消息
    this.messages.push({
      role: 'user',
      content: this.inputText
    })

    const userInput = this.inputText
    this.inputText = ''

    // 模拟 AI 回复
    setTimeout(() => {
      this.messages.push({
        role: 'assistant',
        content: '收到您的问题"' + userInput + '"，正在分析中...'
      })
    }, 1000)
  }
}
