// 聊天详情页面
import router from '@ohos.router'
import promptAction from '@ohos.promptAction'
import { Message, MessageType, SenderType } from '../../data/model/Message'
import { MessageBubble, MessageDateDivider } from '../components/MessageBubble'

// 页面状态
class ChatDetailState {
  messages: Message[] = []
  inputText: string = ''
  loading: boolean = false
  conversationId: string = ''
  conversationName: string = ''
}

@Entry
@Component
struct ChatDetail {
  @State state: ChatDetailState = new ChatDetailState()
  private myUserId: string = 'current_user'
  private scroller: Scroller = new Scroller()
  private inputController: TextInputController = new TextInputController()

  aboutToAppear() {
    const params = router.getParams() as Record<string, string>
    if (params) {
      this.state.conversationId = params['conversationId'] || ''
      this.state.conversationName = params['name'] || '聊天'
    }
    this.loadMessages()
  }

  // 加载消息
  async loadMessages(): Promise<void> {
    this.state.loading = true
    // 模拟消息数据
    const now = Date.now()
    this.state.messages = [
      {
        id: '1',
        conversationId: this.state.conversationId,
        senderId: 'user_001',
        senderName: '张三',
        senderType: SenderType.USER,
        type: MessageType.TEXT,
        content: '早上好，今天的会议安排在几点？',
        createdAt: now - 7200000,
        isRead: true
      },
      {
        id: '2',
        conversationId: this.state.conversationId,
        senderId: this.myUserId,
        senderName: '我',
        senderType: SenderType.USER,
        type: MessageType.TEXT,
        content: '早上好！会议安排在上午10点，地点在A栋3层会议室。',
        createdAt: now - 5400000,
        isRead: true
      },
      {
        id: '3',
        conversationId: this.state.conversationId,
        senderId: 'user_001',
        senderName: '张三',
        senderType: SenderType.USER,
        type: MessageType.TEXT,
        content: '好的，收到。我会准时参加。',
        createdAt: now - 3600000,
        isRead: true
      },
      {
        id: '4',
        conversationId: this.state.conversationId,
        senderId: 'user_001',
        senderName: '张三',
        senderType: SenderType.USER,
        type: MessageType.TEXT,
        content: '对了，记得带上项目资料。',
        createdAt: now - 1800000,
        isRead: true
      },
      {
        id: '5',
        conversationId: this.state.conversationId,
        senderId: this.myUserId,
        senderName: '我',
        senderType: SenderType.USER,
        type: MessageType.TEXT,
        content: '好的，我已经准备好了。',
        createdAt: now - 600000,
        isRead: false
      }
    ]
    this.state.loading = false
  }

  // 发送消息
  async sendMessage(): Promise<void> {
    const text = this.state.inputText.trim()
    if (!text) {
      return
    }

    const newMessage: Message = {
      id: Date.now().toString(),
      conversationId: this.state.conversationId,
      senderId: this.myUserId,
      senderName: '我',
      senderType: SenderType.USER,
      type: MessageType.TEXT,
      content: text,
      createdAt: Date.now(),
      isRead: false
    }

    this.state.messages.push(newMessage)
    this.state.inputText = ''

    // 滚动到底部
    setTimeout(() => {
      this.scroller.scrollEdge(Edge.Bottom)
    }, 100)
  }

  // 判断是否显示日期分割线
  private needDateDivider(index: number): boolean {
    if (index === this.state.messages.length - 1) {
      return true
    }
    const currentMsg = this.state.messages[index]
    const nextMsg = this.state.messages[index + 1]
    const currentDate = new Date(currentMsg.createdAt).toDateString()
    const nextDate = new Date(nextMsg.createdAt).toDateString()
    return currentDate !== nextDate
  }

  // 获取分割线日期
  private getDividerDate(timestamp: number): string {
    const date = new Date(timestamp)
    const today = new Date()
    const yesterday = new Date(today)
    yesterday.setDate(yesterday.getDate() - 1)

    if (date.toDateString() === today.toDateString()) {
      return '今天'
    } else if (date.toDateString() === yesterday.toDateString()) {
      return '昨天'
    } else {
      return date.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' })
    }
  }

  build() {
    Column() {
      // 顶部导航栏
      this.buildNavBar()

      // 消息列表
      List({ scroller: this.scroller }) {
        ForEach(this.state.messages, (msg: Message, index: number) => {
          ListItem() {
            Column() {
              // 日期分割线（如果是最后一条或者日期变化）
              if (index === 0 || this.needDateDivider(index)) {
                MessageDateDivider({ timestamp: msg.createdAt })
              }

              // 消息气泡
              MessageBubble({
                message: msg,
                isMine: msg.senderId === this.myUserId
              })
            }
          }
        })
      }
      .width('100%')
      .layoutWeight(1)
      .padding({ top: 8 })
      .backgroundColor('#F5F5F5')
      .onReachEnd(() => {
        // 加载更多历史消息
      })

      // 输入区域
      this.buildInputArea()
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

      Text(this.state.conversationName)
        .fontSize(18)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .margin({ left: 16 })
        .layoutWeight(1)

      Image($r('app.media.icon_more'))
        .width(24)
        .height(24)
    }
    .width('100%')
    .padding({ left: 16, right: 16, top: 12, bottom: 12 })
    .backgroundColor('#FFFFFF')
  }

  @Builder
  buildInputArea() {
    Column() {
      Row() {
        // 扩展功能按钮
        Text('➕')
          .fontSize(24)
          .onClick(() => {
            // 显示扩展菜单
            promptAction.showToast({ message: '扩展功能' })
          })

        // 文本输入框
        TextInput({
          placeholder: '输入消息...',
          text: this.state.inputText,
          controller: this.inputController
        })
          .fontSize(16)
          .placeholderColor('#999999')
          .backgroundColor('#FFFFFF')
          .borderRadius(20)
          .height(40)
          .layoutWeight(1)
          .margin({ left: 12, right: 12 })
          .onChange((value: string) => {
            this.state.inputText = value
          })
          .onSubmit(() => {
            this.sendMessage()
          })

        // 发送按钮
        Text('发送')
          .fontSize(16)
          .fontColor(this.state.inputText.trim() ? '#1890FF' : '#999999')
          .padding({ left: 12, right: 12 })
          .onClick(() => {
            if (this.state.inputText.trim()) {
              this.sendMessage()
            }
          })
      }
      .width('100%')
      .padding({ left: 12, right: 12, top: 8, bottom: 8 })
      .alignItems(VerticalAlign.Center)
    }
    .width('100%')
    .backgroundColor('#FFFFFF')
    .border({
      width: { top: 1 },
      color: '#E0E0E0'
    })
  }
}
