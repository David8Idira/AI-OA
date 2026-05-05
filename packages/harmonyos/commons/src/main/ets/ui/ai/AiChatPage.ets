/**
 * AI Chat Page - AI助手聊天界面
 * 对应Web前端: source/frontend/src/views/ai/Chat.vue
 */
import router from '@ohos.router';
import { ChatMessage, AI_MODEL_OPTIONS } from '../../model/ai/ChatModel';
import { aiService } from '../../service/AiService';

@Component
export struct AiChatPage {
  @State messages: ChatMessage[] = [];
  @State inputText: string = '';
  @State selectedModel: string = 'gpt-4o';
  @State isLoading: boolean = false;
  @State showModelSelector: boolean = false;
  @State quotaUsed: number = 156;
  @State quotaTotal: number = 1000;

  private scroller: Scroller = new Scroller();

  aboutToAppear() {
    // 初始化聊天历史
    this.loadQuotaInfo();
  }

  loadQuotaInfo() {
    // 模拟获取配额信息
    this.quotaUsed = 156;
    this.quotaTotal = 1000;
  }

  build() {
    Column() {
      // 顶部导航栏
      this.NavigationBar()

      // 消息列表区域
      Scroll(this.scroller) {
        Column() {
          if (this.messages.length === 0) {
            this.WelcomeView()
          } else {
            this.MessageList()
          }

          if (this.isLoading) {
            this.LoadingIndicator()
          }
        }
        .width('100%')
        .padding({ left: 16, right: 16, top: 16, bottom: 16 })
      }
      .layoutWeight(1)
      .scrollBar(BarState.Auto)
      .edgeEffect(EdgeEffect.Spring)

      // 快捷指令
      this.QuickCommands()

      // 输入区域
      this.InputArea()

      // 配额显示
      this.QuotaDisplay()
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F7FA')
  }

  @Builder
  NavigationBar() {
    Row() {
      Image($r('app.media.ic_back'))
        .width(24)
        .height(24)
        .onClick(() => router.back())

      Text('AI 助手')
        .fontSize(18)
        .fontWeight(FontWeight.Medium)
        .layoutWeight(1)
        .textAlign(TextAlign.Center)

      // 模型选择器
      Row() {
        Text(this.getModelName(this.selectedModel))
          .fontSize(12)
          .fontColor('#666666')
        Image($r('app.media.ic_dropdown'))
          .width(16)
          .height(16)
      }
      .onClick(() => this.showModelSelector = !this.showModelSelector)
      .padding(8)
    }
    .width('100%')
    .height(56)
    .padding({ left: 16, right: 16 })
    .backgroundColor('#FFFFFF')
    .shadow({ radius: 4, color: '#08000000', offsetX: 0, offsetY: 2 })
  }

  @Builder
  WelcomeView() {
    Column() {
      Image($r('app.media.ic_ai_assistant'))
        .width(64)
        .height(64)

      Text('AI 智能助手')
        .fontSize(24)
        .fontWeight(FontWeight.Bold)
        .margin({ top: 16 })

      Text('我可以帮你解答问题、生成内容、查询信息')
        .fontSize(14)
        .fontColor('#999999')
        .margin({ top: 8 })

      // 快捷提示按钮
      Column() {
        this.QuickPromptButton('本月报销总额是多少？')
        this.QuickPromptButton('帮我生成一份月度工作报告')
        this.QuickPromptButton('查询公司的休假政策')
        this.QuickPromptButton('帮我写一封邮件')
      }
      .margin({ top: 24 })
      .width('100%')
    }
    .width('100%')
    .padding({ top: 60 })
    .alignItems(HorizontalAlign.Center)
  }

  @Builder
  QuickPromptButton(text: string) {
    Button(text)
      .fontSize(14)
      .height(36)
      .backgroundColor('#FFFFFF')
      .fontColor('#667EEA')
      .border({ width: 1, color: '#667EEA', radius: 18 })
      .margin({ top: 8 })
      .onClick(() => {
        this.inputText = text;
        this.handleSend();
      })
  }

  @Builder
  MessageList() {
    ForEach(this.messages, (msg: ChatMessage, index: number) => {
      if (msg.role === 'user') {
        this.UserMessage(msg)
      } else {
        this.AIMessage(msg, index)
      }
    })
  }

  @Builder
  UserMessage(msg: ChatMessage) {
    Row() {
      Blank()
      Column() {
        Text(msg.content)
          .fontSize(14)
          .fontColor('#FFFFFF')
          .padding(12)
          .backgroundColor('#4A90E2')
          .borderRadius({ topLeft: 16, topRight: 16, bottomLeft: 16, bottomRight: 4 })
          .width('auto')
          .maxWidth(280)
      }
      .alignItems(HorizontalAlign.End)

      Text('👤')
        .fontSize(24)
        .margin({ left: 8 })
    }
    .width('100%')
    .margin({ top: 12 })
    .alignItems(VerticalAlign.Top)
  }

  @Builder
  AIMessage(msg: ChatMessage, index: number) {
    Row() {
      Text('🤖')
        .fontSize(24)
        .margin({ right: 8 })

      Column() {
        Text(msg.content)
          .fontSize(14)
          .fontColor('#333333')
          .padding(12)
          .backgroundColor('#FFFFFF')
          .borderRadius({ topLeft: 4, topRight: 16, bottomLeft: 16, bottomRight: 16 })
          .width('auto')
          .maxWidth(280)
          .shadow({ radius: 8, color: '#08000000', offsetX: 2, offsetY: 2 })

        // 参考来源
        if (msg.references && msg.references.length > 0) {
          Column() {
            Text('参考来源：')
              .fontSize(12)
              .fontColor('#999999')
              .margin({ top: 8 })

            ForEach(msg.references, (ref: any) => {
              Text(ref.title)
                .fontSize(12)
                .fontColor('#667EEA')
                .onClick(() => {
                  // 打开知识库链接
                  router.pushUrl({ url: 'pages/knowledge/KnowledgeDetail', params: { id: ref.id } });
                })
            })
          }
          ..alignItems(HorizontalAlign.Start)
          .margin({ top: 8 })
        }

        // 操作按钮
        Row() {
          Text('复制')
            .fontSize(12)
            .fontColor('#999999')
            .onClick(() => {
              // 复制到剪贴板
            })

          Text('重新生成')
            .fontSize(12)
            .fontColor('#999999')
            .margin({ left: 16 })
            .onClick(() => this.regenerate(index))
        }
        .margin({ top: 8 })
      }
      .alignItems(HorizontalAlign.Start)
    }
    .width('100%')
    .margin({ top: 12 })
    .alignItems(VerticalAlign.Top)
  }

  @Builder
  LoadingIndicator() {
    Row() {
      Text('🤖')
        .fontSize(24)
        .margin({ right: 8 })

      Row() {
        LoadingProgress()
          .width(20)
          .height(20)
          .color('#667EEA')
        Text('AI正在思考...')
          .fontSize(14)
          .fontColor('#999999')
          .margin({ left: 8 })
      }
      .padding(12)
      .backgroundColor('#FFFFFF')
      .borderRadius(16)
    }
    .width('100%')
    .margin({ top: 12 })
    .alignItems(VerticalAlign.Top)
  }

  @Builder
  QuickCommands() {
    Scroll() {
      Row() {
        ForEach(['总结本周工作', '生成月度报表', '帮我写周报', '查询审批进度'], (cmd: string) => {
          Text(cmd)
            .fontSize(12)
            .padding({ left: 12, right: 12, top: 6, bottom: 6 })
            .backgroundColor('#FFFFFF')
            .fontColor('#667EEA')
            .border({ width: 1, color: '#667EEA', radius: 14 })
            .margin({ right: 8 })
            .onClick(() => {
              this.inputText = cmd;
              this.handleSend();
            })
        })
      }
    }
    .width('100%')
    .height(40)
    .scrollBar(BarState.Off)
    .padding({ left: 16, right: 16 })
    .margin({ top: 8, bottom: 8 })
  }

  @Builder
  InputArea() {
    Row() {
      // 图片上传按钮
      Button() {
        Image($r('app.media.ic_picture'))
          .width(24)
          .height(24)
      }
      .width(36)
      .height(36)
      .backgroundColor('#FFFFFF')
      .borderRadius(18)
      .shadow({ radius: 4, color: '#08000000', offsetX: 1, offsetY: 1 })
      .onClick(() => {
        // 触发图片上传
      })

      // 输入框
      TextArea()
        .placeholder('输入消息... (Enter发送, Shift+Enter换行)')
        .fontSize(14)
        .height(40)
        .borderRadius(20)
        .backgroundColor('#FFFFFF')
        .layoutWeight(1)
        .margin({ left: 8, right: 8 })
        .onChange((value: string) => {
          this.inputText = value;
        })

      // 发送按钮
      Button() {
        Image($r('app.media.ic_send'))
          .width(20)
          .height(20)
      }
      .width(40)
      .height(40)
      .backgroundColor(this.inputText.trim().length > 0 ? '#667EEA' : '#CCCCCC')
      .borderRadius(20)
      .enabled(this.inputText.trim().length > 0 && !this.isLoading)
      .onClick(() => this.handleSend())
    }
    .width('100%')
    .height(56)
    .padding({ left: 16, right: 16 })
    .alignItems(VerticalAlign.Center)
  }

  @Builder
  QuotaDisplay() {
    Row() {
      Progress({ value: this.quotaUsed, total: this.quotaTotal })
        .width(150)
        .height(6)
        .color(this.quotaUsed / this.quotaTotal > 0.8 ? '#F56C6C' : '#667EEA')

      Text(`今日配额: ${this.quotaUsed}/${this.quotaTotal} tokens`)
        .fontSize(12)
        .fontColor('#909399')
        .margin({ left: 12 })
    }
    .width('100%')
    .height(32)
    .padding({ left: 16, right: 16 })
    .alignItems(VerticalAlign.Center)
    .backgroundColor('#FFFFFF')
  }

  getModelName(modelId: string): string {
    const model = AI_MODEL_OPTIONS.find(m => m.id === modelId);
    return model ? model.name : 'GPT-4o';
  }

  handleSend() {
    if (!this.inputText.trim() || this.isLoading) return;

    const userMessage: ChatMessage = {
      id: Date.now().toString(),
      role: 'user',
      content: this.inputText.trim(),
      timestamp: Date.now()
    };

    this.messages.push(userMessage);
    const question = this.inputText;
    this.inputText = '';
    this.isLoading = true;

    // 滚动到底部
    setTimeout(() => {
      this.scroller.scrollEdge(ScrollEdge.End);
    }, 100);

    // 模拟AI响应
    aiService.sendMessage(question, this.selectedModel)
      .then((reply: string) => {
        const aiMessage: ChatMessage = {
          id: (Date.now() + 1).toString(),
          role: 'assistant',
          content: reply,
          timestamp: Date.now(),
          references: [
            { id: '1', title: '公司报销管理制度', url: '/knowledge/doc/1', source: '知识库' }
          ]
        };
        this.messages.push(aiMessage);
        this.quotaUsed += Math.floor(reply.length / 4);
      })
      .catch((error: string) => {
        const errorMessage: ChatMessage = {
          id: (Date.now() + 1).toString(),
          role: 'assistant',
          content: error || '抱歉，AI服务暂时不可用',
          timestamp: Date.now()
        };
        this.messages.push(errorMessage);
      })
      .finally(() => {
        this.isLoading = false;
        setTimeout(() => {
          this.scroller.scrollEdge(ScrollEdge.End);
        }, 100);
      });
  }

  regenerate(index: number) {
    // 找到最后一条用户消息，重新发送
    const lastUserIdx = this.messages.slice(0, index).findLastIndex(m => m.role === 'user');
    if (lastUserIdx !== -1) {
      const lastMsg = this.messages[lastUserIdx];
      // 删除当前消息并重新生成
      this.messages.splice(index, 1);
      this.isLoading = true;
      // 模拟重新生成
      setTimeout(() => {
        const newMsg: ChatMessage = {
          id: Date.now().toString(),
          role: 'assistant',
          content: '这是重新生成的内容...',
          timestamp: Date.now()
        };
        this.messages.push(newMsg);
        this.isLoading = false;
      }, 1500);
    }
  }
}