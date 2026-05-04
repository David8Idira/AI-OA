// 消息列表页面（会话列表）
import router from '@ohos.router'
import { Conversation, ConversationType } from '../../data/model/Conversation'
import IMService from '../../services/IMService'

// 页面状态
class MessageListState {
  conversations: Conversation[] = []
  loading: boolean = false
  searchKeyword: string = ''
}

@Entry
@Component
struct MessageList {
  @State state: MessageListState = new MessageListState()
  private tabController: TabsController = new TabsController()

  aboutToAppear() {
    this.loadConversations()
  }

  // 加载会话列表
  async loadConversations(): Promise<void> {
    this.state.loading = true
    try {
      const res = await IMService.getConversations()
      if (res.data && res.data.conversations) {
        // 转换服务层数据到页面模型
        this.state.conversations = res.data.conversations.map(c => ({
          id: c.id,
          name: c.name || '',
          avatar: c.avatar,
          type: c.type,
          lastMessage: c.lastMessage,
          lastMessageTime: c.lastMessageTime,
          unreadCount: c.unreadCount,
          isPinned: c.isPinned === 1 ? 1 : 0,
          isMuted: c.isMuted
        }))
      }
    } catch (error) {
      // 使用模拟数据
      this.state.conversations = this.getMockConversations()
    }
    this.state.loading = false
  }

  // 获取模拟会话数据
  private getMockConversations(): Conversation[] {
    const now = Date.now()
    return [
      {
        id: 'conv1',
        name: '张三',
        avatar: '',
        type: ConversationType.PRIVATE,
        lastMessage: '好的，收到。我会准时参加。',
        lastMessageTime: now - 1800000,
        unreadCount: 2,
        isPinned: 1,
        isMuted: false
      },
      {
        id: 'conv2',
        name: '产品研发群',
        avatar: '',
        type: ConversationType.GROUP,
        lastMessage: '【李四】：新版本已经在测试了',
        lastMessageTime: now - 3600000,
        unreadCount: 5,
        isPinned: 0,
        isMuted: false
      },
      {
        id: 'conv3',
        name: '王五',
        avatar: '',
        type: ConversationType.PRIVATE,
        lastMessage: '下午三点会议室见',
        lastMessageTime: now - 7200000,
        unreadCount: 0,
        isPinned: 0,
        isMuted: true
      },
      {
        id: 'conv4',
        name: '系统通知',
        avatar: '',
        type: ConversationType.SYSTEM,
        lastMessage: '您的请假申请已通过',
        lastMessageTime: now - 86400000,
        unreadCount: 1,
        isPinned: 0,
        isMuted: false
      },
      {
        id: 'conv5',
        name: '周七',
        avatar: '',
        type: ConversationType.PRIVATE,
        lastMessage: '文件已发送，请查收',
        lastMessageTime: now - 172800000,
        unreadCount: 0,
        isPinned: 0,
        isMuted: false
      }
    ]
  }

  // 格式化时间
  private formatTime(timestamp: number): string {
    const now = Date.now()
    const diff = now - timestamp
    const day = 86400000

    if (diff < 60000) {
      return '刚刚'
    } else if (diff < 3600000) {
      return Math.floor(diff / 60000) + '分钟前'
    } else if (diff < day) {
      return new Date(timestamp).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    } else if (diff < 2 * day) {
      return '昨天'
    } else if (diff < 7 * day) {
      const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      return weekdays[new Date(timestamp).getDay()]
    } else {
      return new Date(timestamp).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
    }
  }

  // 获取会话头像
  @Builder
  buildAvatar(conversation: Conversation) {
    if (conversation.avatar) {
      Image(conversation.avatar)
        .width(48)
        .height(48)
        .borderRadius(8)
        .alt($r('app.media.icon_avatar'))
    } else {
      Column() {
        Text(conversation.name.charAt(0))
          .fontSize(20)
          .fontColor('#FFFFFF')
      }
      .width(48)
      .height(48)
      .borderRadius(8)
      .backgroundColor(this.getAvatarColor(conversation.type))
      .justifyContent(FlexAlign.Center)
    }
  }

  private getAvatarColor(type: ConversationType): string {
    switch (type) {
      case ConversationType.GROUP:
        return '#1890FF'
      case ConversationType.SYSTEM:
        return '#FAAD14'
      default:
        return '#52C41A'
    }
  }

  // 会话列表项
  @Builder
  buildConversationItem(conversation: Conversation) {
    Row() {
      // 头像
      this.buildAvatar(conversation)

      // 内容区
      Column() {
        Row() {
          Text(conversation.name)
            .fontSize(16)
            .fontWeight(FontWeight.Medium)
            .fontColor('#333333')
            .maxLines(1)
            .textOverflow({ overflow: TextOverflow.Ellipsis })
            .layoutWeight(1)

          Text(this.formatTime(conversation.lastMessageTime))
            .fontSize(12)
            .fontColor('#999999')
        }

        Row() {
          Text(conversation.lastMessage || '')
            .fontSize(14)
            .fontColor('#666666')
            .maxLines(1)
            .textOverflow({ overflow: TextOverflow.Ellipsis })
            .layoutWeight(1)

          if (conversation.unreadCount > 0) {
            Text(conversation.unreadCount > 99 ? '99+' : conversation.unreadCount.toString())
              .fontSize(12)
              .fontColor('#FFFFFF')
              .backgroundColor('#FF4D4F')
              .borderRadius(10)
              .padding({ left: 6, right: 6, top: 2, bottom: 2 })
              .margin({ left: 8 })
          }
        }
        .margin({ top: 6 })
      }
      .layoutWeight(1)
      .margin({ left: 12 })
    }
    .width('100%')
    .padding({ left: 16, right: 16, top: 12, bottom: 12 })
    .backgroundColor('#FFFFFF')
    .onClick(() => {
      router.pushUrl({ url: 'pages/ChatDetail', params: { conversationId: conversation.id, name: conversation.name } })
    })
  }

  build() {
    Column() {
      // 顶部导航栏
      this.buildNavBar()

      // 搜索栏
      this.buildSearchBar()

      // 会话列表
      if (this.state.loading) {
        Blank()
        Text('加载中...')
          .fontSize(14)
          .fontColor('#999999')
        Blank()
      } else {
        List() {
          ForEach(this.state.conversations, (conv: Conversation) => {
            ListItem() {
              this.buildConversationItem(conv)
            }
            .swipeAction({
              end: {
                builder: this.buildSwipeActions(conv.id),
                actionAreaDistance: 80
              }
            })
          })
        }
        .width('100%')
        .layoutWeight(1)
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
      Text('消息')
        .fontSize(20)
        .fontWeight(FontWeight.Bold)
        .fontColor('#333333')

      Blank()

      Text('📷')
        .fontSize(24)
        .onClick(() => {
          router.pushUrl({ url: 'pages/ScanPage' })
        })
    }
    .width('100%')
    .padding({ left: 16, right: 16, top: 12, bottom: 12 })
    .backgroundColor('#FFFFFF')
  }

  @Builder
  buildSearchBar() {
    Row() {
      Image($r('app.media.icon_search'))
        .width(16)
        .height(16)
        .margin({ left: 12 })
        .opacity(0.5)

      Text('搜索')
        .fontSize(14)
        .fontColor('#999999')
        .margin({ left: 8 })
    }
    .width('100%')
    .height(40)
    .backgroundColor('#F5F5F5')
    .borderRadius(8)
    .margin({ left: 16, right: 16, top: 8, bottom: 8 })
    .onClick(() => {
      router.pushUrl({ url: 'pages/SearchPage' })
    })
  }

  @Builder
  buildSwipeActions(conversationId: string) {
    Row() {
      Button('置顶')
        .fontSize(14)
        .fontColor('#FFFFFF')
        .backgroundColor('#FAAD14')
        .borderRadius(0)
        .height('100%')
        .onClick(() => {
          // 置顶操作
        })

      Button('删除')
        .fontSize(14)
        .fontColor('#FFFFFF')
        .backgroundColor('#FF4D4F')
        .borderRadius(0)
        .height('100%')
        .onClick(() => {
          // 删除操作
        })
    }
    .height('100%')
    .width(160)
  }
}
