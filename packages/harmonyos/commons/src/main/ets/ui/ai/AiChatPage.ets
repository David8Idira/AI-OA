import router from '@ohos.router'

interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  time: number
}

class AiChatState {
  messages: ChatMessage[] = []
  inputText: string = ''
  loading: boolean = false
}

@Entry
@Component
struct AiChatPage {
  @State state: AiChatState = new AiChatState()
  private scrollController: Scroller = new Scroller()

  aboutToAppear() {
    this.loadHistory()
  }

  async loadHistory(): Promise<void> {
    const now = Date.now()
    this.state.messages = [
      {
        id: '1',
        role: 'assistant',
        content: '您好！我是AI助手，有什么可以帮助您的吗？',
        time: now - 3600000
      }
    ]
  }

  private sendMessage(): void {
    const text = this.state.inputText.trim()
    if (!text) return

    const now = Date.now()
    this.state.messages.push({
      id: now.toString(),
      role: 'user',
      content: text,
      time: now
    })
    this.state.inputText = ''
    this.scrollToBottom()

    this.state.loading = true
    setTimeout(() => {
      this.state.messages.push({
        id: (now + 1).toString(),
        role: 'assistant',
        content: `您的问题是："${text}"\n\n抱歉，AI服务暂时不可用，请稍后再试。`,
        time: Date.now()
      })
      this.state.loading = false
      this.scrollToBottom()
    }, 1000)
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      this.scrollController.scrollEdge(ScrollEdge.End)
    }, 50)
  }

  private formatTime(timestamp: number): string {
    return new Date(timestamp).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  private quickQuestions: string[] = ['帮我写一份工作周报', '请假申请怎么写', '今天有什么会议', '查询本月考勤']

  build() {
    Column() {
      // 顶部导航
      Row() {
        Text('AI 助手')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#333333')
      }
      .width('100%')
      .height(56)
      .padding({ left: 16 })
      .backgroundColor('#FFFFFF')
      .shadow({ radius: 4, color: '#10000000', offsetX: 0, offsetY: 2 })

      // 消息列表
      Scroll(this.scrollController) {
        Column() {
          ForEach(this.state.messages, (msg: ChatMessage) => {
            if (msg.role === 'user') {
              this.buildUserMessage(msg)
            } else {
              this.buildAssistantMessage(msg)
            }
          })

          if (this.state.loading) {
            Row() {
              Text('🤖 AI 正在思考...')
                .fontSize(13)
                .fontColor('#999999')
            }
            .width('100%')
            .padding(16)
          }
        }
        .padding(16)
      }
      .width('100%')
      .layoutWeight(1)
      .backgroundColor('#F5F5F5')

      // 快捷问题
      Column() {
        Scroll() {
          Row() {
            ForEach(this.quickQuestions, (q: string) => {
              Text(q)
                .fontSize(12)
                .fontColor('#1890FF')
                .backgroundColor('#E6F0FF')
                .borderRadius(12)
                .padding({ left: 12, right: 12, top: 6, bottom: 6 })
                .margin({ right: 8 })
                .onClick(() => {
                  this.state.inputText = q
                })
            })
          }
        }
        .width('100%')
        .scrollable(ScrollDirection.Horizontal)
        .scrollBar(BarState.Off)
      }
      .width('100%')
      .padding(8)
      .backgroundColor('#FFFFFF')

      // 输入区域
      Row() {
        TextInput({ placeholder: '输入您的问题...', text: this.state.inputText })
          .layoutWeight(1)
          .height(40)
          .backgroundColor('#F5F5F5')
          .borderRadius(20)
          .padding({ left: 16, right: 16 })
          .onChange((val: string) => {
            this.state.inputText = val
          })
          .onSubmit(() => this.sendMessage())

        Button('发送')
          .fontSize(14)
          .fontColor('#FFFFFF')
          .backgroundColor('#1890FF')
          .borderRadius(20)
          .height(40)
          .width(64)
          .margin({ left: 8 })
          .onClick(() => this.sendMessage())
      }
      .width('100%')
      .padding({ left: 16, right: 16, top: 8, bottom: 8 })
      .backgroundColor('#FFFFFF')
    }
    .width('100%')
    .height('100%')
  }

  @Builder
  buildUserMessage(msg: ChatMessage) {
    Row() {
      Blank()
      Column() {
        Text(msg.content)
          .fontSize(14)
          .fontColor('#FFFFFF')
          .padding(12)
          .backgroundColor('#1890FF')
          .borderRadius(16)
        Text(this.formatTime(msg.time))
          .fontSize(11)
          .fontColor('#999999')
          .margin({ top: 4 })
      }
      .alignItems(HorizontalAlign.End)
    }
    .width('100%')
    .padding({ top: 8 })
  }

  @Builder
  buildAssistantMessage(msg: ChatMessage) {
    Row() {
      Column() {
        Text(msg.content)
          .fontSize(14)
          .fontColor('#333333')
          .padding(12)
          .backgroundColor('#FFFFFF')
          .borderRadius(16)
        Text(this.formatTime(msg.time))
          .fontSize(11)
          .fontColor('#999999')
          .margin({ top: 4 })
      }
      .alignItems(HorizontalAlign.Start)
      .layoutWeight(1)
    }
    .width('100%')
    .padding({ top: 8 })
  }
}