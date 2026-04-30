// 消息气泡组件
import { Message, MessageType, SenderType } from '../../data/model/Message'

// 消息气泡属性
@Component
export struct MessageBubble {
  @Prop message: Message
  @Prop isMine: boolean = false // 是否是我的消息

  // 格式化时间
  private formatTime(timestamp: number): string {
    const date = new Date(timestamp)
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  // 格式化日期
  private formatDate(timestamp: number): string {
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
    Row() {
      if (this.isMine) {
        Blank()
      }

      Column() {
        // 发送者名称（非我的消息显示）
        if (!this.isMine && this.message.senderType !== SenderType.SYSTEM) {
          Text(this.message.senderName)
            .fontSize(12)
            .fontColor('#999999')
            .margin({ bottom: 4 })
            .alignSelf(HorizontalAlign.Start)
        }

        // 消息气泡
        if (this.message.type === MessageType.TEXT) {
          Text(this.message.content)
            .fontSize(15)
            .fontColor(this.isMine ? '#FFFFFF' : '#333333')
            .padding({
              left: 14,
              right: 14,
              top: 10,
              bottom: 10
            })
            .backgroundColor(this.isMine ? '#1890FF' : '#FFFFFF')
            .borderRadius({
              topLeft: 16,
              topRight: 16,
              bottomLeft: this.isMine ? 16 : 4,
              bottomRight: this.isMine ? 4 : 16
            })
        } else if (this.message.type === MessageType.IMAGE) {
          Image(this.message.imageUrl || '')
            .width(200)
            .height(150)
            .borderRadius(12)
            .alt($r('app.media.icon_image_placeholder'))
        }

        // 时间戳
        Text(this.formatTime(this.message.createdAt))
          .fontSize(10)
          .fontColor('#BBBBBB')
          .margin({ top: 4 })
          .alignSelf(HorizontalAlign.End)
      }
      .alignItems(HorizontalAlign.End)
      .layoutWeight(1)

      if (!this.isMine) {
        Blank()
      }
    }
    .width('100%')
    .padding({ left: 12, right: 12, top: 4, bottom: 4 })
  }
}

// 消息时间分割线
@Component
export struct MessageDateDivider {
  @Prop timestamp: number

  private formatDate(timestamp: number): string {
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
    Row() {
      Line()
        .width(60)
        .height(1)
        .backgroundColor('#E0E0E0')

      Text(this.formatDate(this.timestamp))
        .fontSize(12)
        .fontColor('#999999')
        .padding({ left: 12, right: 12 })

      Line()
        .width(60)
        .height(1)
        .backgroundColor('#E0E0E0')
    }
    .width('100%')
    .padding({ top: 16, bottom: 8 })
    .justifyContent(FlexAlign.Center)
  }
}

// 消息气泡列表
@Component
export struct MessageBubbleList {
  @Link messages: Message[]
  @Prop myUserId: string

  aboutToAppear() {
    // 模拟消息数据
    this.messages = this.getMockMessages()
  }

  private getMockMessages(): Message[] {
    const now = Date.now()
    return [
      {
        id: '1',
        conversationId: 'conv1',
        senderId: 'user_001',
        senderName: '张三',
        senderType: SenderType.USER,
        type: MessageType.TEXT,
        content: '早上好，今天的会议安排在几点？',
        createdAt: now - 3600000 * 2,
        isRead: true
      },
      {
        id: '2',
        conversationId: 'conv1',
        senderId: this.myUserId,
        senderName: '我',
        senderType: SenderType.USER,
        type: MessageType.TEXT,
        content: '早上好！会议安排在上午10点，地点在A栋3层会议室。',
        createdAt: now - 3600000 * 1.5,
        isRead: true
      },
      {
        id: '3',
        conversationId: 'conv1',
        senderId: 'user_001',
        senderName: '张三',
        senderType: SenderType.USER,
        type: MessageType.TEXT,
        content: '好的，收到。我会准时参加。',
        createdAt: now - 3600000,
        isRead: false
      },
      {
        id: '4',
        conversationId: 'conv1',
        senderId: this.myUserId,
        senderName: '我',
        senderType: SenderType.USER,
        type: MessageType.IMAGE,
        content: '',
        imageUrl: 'https://img.freepik.com/free-vector/business-meeting-illustration_1262-16788.jpg',
        createdAt: now - 1800000,
        isRead: false
      }
    ]
  }

  build() {
    List() {
      ForEach(this.messages, (msg: Message) => {
        ListItem() {
          MessageBubble({
            message: msg,
            isMine: msg.senderId === this.myUserId
          })
        }
      })
    }
    .width('100%')
    .height('100%')
    .reverseLayout(true)
    .padding({ left: 8, right: 8 })
  }
}
