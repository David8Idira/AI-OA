import router from '@ohos.router'
import aiService from '../../../../commons/src/main/ets/service/AiService'

// AI-OA AI 对话页面
@Entry
@Component
struct AiChatPage {
  @State inputText: string = ''
  @State messages: any[] = []
  @State isLoading: boolean = false
  @State errorMsg: string = ''
  @State sessionId: string = ''
  @State quotaUsed: number = 0
  @State quotaTotal: number = 1000

  aboutToAppear() {
    // 初始化欢迎消息
    this.messages.push({
      role: 'assistant',
      content: '您好！我是 AI-OA 智能助手，请问有什么可以帮助您的？'
    })
    this.loadQuota()
  }

  async loadQuota() {
    try {
      const res = await aiService.getQuotaInfo()
      if (res.code === 0 || res.code === 200) {
        this.quotaUsed = res.data?.used ?? 0
        this.quotaTotal = res.data?.total ?? 1000
      }
    } catch (e) {
      // 配额获取失败不影响使用
    }
  }

  async sendMessage() {
    const text = this.inputText.trim()
    if (!text || this.isLoading) return

    // 添加用户消息
    this.messages.push({
      role: 'user',
      content: text
    })
    this.inputText = ''

    // 添加一个临时的"正在输入"状态
    const loadingIndex = this.messages.length
    this.messages.push({
      role: 'assistant',
      content: '正在思考...',
      isLoading: true
    })
    this.isLoading = true
    this.errorMsg = ''

    try {
      const reply = await aiService.getCompletion(text, this.sessionId || undefined)
      // 替换 loading 消息
      if (loadingIndex < this.messages.length) {
        this.messages[loadingIndex] = {
          role: 'assistant',
          content: reply
        }
      } else {
        this.messages.push({
          role: 'assistant',
          content: reply
        })
      }
    } catch (e) {
      // 移除 loading 消息
      if (loadingIndex < this.messages.length) {
        this.messages.splice(loadingIndex, 1)
      }
      this.errorMsg = 'AI服务暂时不可用，请稍后重试'
    } finally {
      this.isLoading = false
    }
  }

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
        Blank()
        Text(`配额: ${this.quotaUsed}/${this.quotaTotal}`)
          .fontSize(10)
          .fontColor('#999999')
      }
      .width('90%')
      .height(50)
      .margin({ top: 20 })

      // 错误提示
      if (this.errorMsg) {
        Text('⚠️ ' + this.errorMsg)
          .fontSize(12)
          .fontColor('#FA8C16')
          .backgroundColor('#FFF7E6')
          .padding(8)
          .borderRadius(8)
          .width('90%')
          .margin({ top: 8 })
      }

      // 消息列表
      List() {
        ForEach(this.messages, (msg: any, index: number) => {
          ListItem() {
            Row() {
              if (msg.role === 'user') {
                Blank()
                Column() {
                  Text(msg.content)
                    .fontSize(14)
                    .padding(12)
                    .backgroundColor('#1890FF')
                    .fontColor('#FFFFFF')
                    .borderRadius(16)
                }
              } else {
                Row() {
                  Text('🤖')
                    .fontSize(18)
                    .margin({ right: 6 })
                  if (msg.isLoading) {
                    LoadingProgress()
                      .width(16)
                      .height(16)
                  } else {
                    Text(msg.content)
                      .fontSize(14)
                      .padding(12)
                      .backgroundColor('#F5F5F5')
                      .fontColor('#333333')
                      .borderRadius(16)
                  }
                }
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
        TextInput({ placeholder: '输入问题...', text: this.inputText })
          .layoutWeight(1)
          .height(40)
          .borderRadius(20)
          .backgroundColor('#F5F5F5')
          .onChange((value: string) => {
            this.inputText = value
          })
          .onSubmit((enterKey: EnterKeyType) => {
            if (enterKey === EnterKeyType.Send) {
              this.sendMessage()
            }
          })

        Button('发送')
          .height(36)
          .borderRadius(18)
          .backgroundColor(this.inputText.trim() ? '#1890FF' : '#CCCCCC')
          .enabled(!!this.inputText.trim() && !this.isLoading)
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
}