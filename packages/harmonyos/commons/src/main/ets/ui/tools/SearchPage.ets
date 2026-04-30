// 全局搜索页面
import router from '@ohos.router'

// 搜索历史
interface SearchHistoryItem {
  id: string
  keyword: string
  time: number
}

// 搜索结果
interface SearchResult {
  id: string
  type: 'approval' | 'document' | 'contact' | 'message'
  title: string
  subtitle: string
  icon: string
}

// 页面状态
class SearchPageState {
  keyword: string = ''
  history: SearchHistoryItem[] = []
  results: SearchResult[] = []
  hotSearches: string[] = []
  searching: boolean = false
  showHistory: boolean = true
}

@Entry
@Component
struct SearchPage {
  @State state: SearchPageState = new SearchPageState()
  private searchController: SearchController = new SearchController()

  aboutToAppear() {
    // 模拟搜索历史
    const now = Date.now()
    this.state.history = [
      { id: '1', keyword: '请假申请', time: now - 3600000 },
      { id: '2', keyword: '考勤规则', time: now - 86400000 },
      { id: '3', keyword: '产品需求', time: now - 172800000 }
    ]

    // 模拟热搜词
    this.state.hotSearches = [
      '考勤打卡', '请假申请', '审批流程', '产品发布', '版本更新'
    ]
  }

  // 执行搜索
  async performSearch(keyword: string): Promise<void> {
    if (!keyword.trim()) {
      this.state.results = []
      this.state.showHistory = true
      return
    }

    this.state.searching = true
    this.state.showHistory = false

    // 添加到历史记录
    const existingIndex = this.state.history.findIndex(h => h.keyword === keyword)
    if (existingIndex !== -1) {
      this.state.history.splice(existingIndex, 1)
    }
    this.state.history.unshift({
      id: Date.now().toString(),
      keyword: keyword,
      time: Date.now()
    })

    // 模拟搜索结果
    await new Promise(resolve => setTimeout(resolve, 500))

    this.state.results = [
      { id: '1', type: 'approval', title: '张三的请假申请', subtitle: '待审批 · 2026-04-15', icon: '📅' },
      { id: '2', type: 'approval', title: '李四的加班申请', subtitle: '已通过 · 2026-04-14', icon: '⏰' },
      { id: '3', type: 'document', title: '考勤管理制度', subtitle: '文档 · 更新于 2026-04-10', icon: '📄' },
      { id: '4', type: 'contact', title: '张三', subtitle: '产品部 · 产品经理', icon: '👤' },
      { id: '5', type: 'message', title: '张三的消息', subtitle: '好的，收到。', icon: '💬' }
    ]

    this.state.searching = false
  }

  // 清除搜索历史
  clearHistory(): void {
    this.state.history = []
  }

  // 获取结果类型文字
  private getResultTypeText(type: string): string {
    switch (type) {
      case 'approval': return '审批'
      case 'document': return '文档'
      case 'contact': return '联系人'
      case 'message': return '消息'
      default: return '其他'
    }
  }

  // 处理结果点击
  private handleResultClick(result: SearchResult): void {
    switch (result.type) {
      case 'approval':
        router.pushUrl({ url: 'pages/ApprovalDetail', params: { approvalId: result.id } })
        break
      case 'contact':
        router.pushUrl({ url: 'pages/ChatDetail', params: { conversationId: result.id, name: result.title } })
        break
      default:
        // 其他类型处理
        break
    }
  }

  build() {
    Column() {
      // 搜索栏
      Row() {
        Image($r('app.media.icon_back'))
          .width(20)
          .height(20)
          .onClick(() => router.back())

        Search({
          placeholder: '搜索审批、文档、联系人...',
          controller: this.searchController
        })
          .width('100%')
          .height(36)
          .backgroundColor('#F5F5F5')
          .borderRadius(18)
          .placeholderColor('#999999')
          .textFont({ size: 14 })
          .onSubmit((value: string) => {
            this.performSearch(value)
          })
          .onChange((value: string) => {
            this.state.keyword = value
            if (!value) {
              this.state.results = []
              this.state.showHistory = true
            }
          })
      }
      .width('100%')
      .padding({ left: 12, right: 12, top: 8, bottom: 8 })
      .backgroundColor('#FFFFFF')

      Scroll() {
        Column() {
          // 搜索历史
          if (this.state.showHistory) {
            this.buildHistorySection()

            // 热搜榜
            this.buildHotSearchSection()
          } else {
            // 搜索结果
            this.buildResultsSection()
          }
        }
        .padding(16)
      }
      .width('100%')
      .layoutWeight(1)
      .backgroundColor('#F5F5F5')
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#FFFFFF')
  }

  @Builder
  buildHistorySection() {
    if (this.state.history.length > 0) {
      Column() {
        Row() {
          Text('搜索历史')
            .fontSize(16)
            .fontWeight(FontWeight.Medium)
            .fontColor('#333333')

          Blank()

          Text('清除')
            .fontSize(13)
            .fontColor('#FF4D4F')
            .onClick(() => this.clearHistory())
        }
        .width('100%')
        .margin({ bottom: 12 })

        Flex({ wrap: FlexWrap.Wrap }) {
          ForEach(this.state.history, (item: SearchHistoryItem) => {
            Text(item.keyword)
              .fontSize(13)
              .fontColor('#666666')
              .backgroundColor('#F5F5F5')
              .borderRadius(16)
              .padding({ left: 12, right: 12, top: 6, bottom: 6 })
              .margin({ right: 8, bottom: 8 })
              .onClick(() => {
                this.state.keyword = item.keyword
                this.performSearch(item.keyword)
              })
          })
        }
        .width('100%')
      }
      .width('100%')
      .margin({ bottom: 24 })
    }
  }

  @Builder
  buildHotSearchSection() {
    Column() {
      Row() {
        Text('🔥')
          .fontSize(16)

        Text('热搜榜')
          .fontSize(16)
          .fontWeight(FontWeight.Medium)
          .fontColor('#333333')
          .margin({ left: 6 })
      }
      .width('100%')
      .margin({ bottom: 12 })

      ForEach(this.state.hotSearches, (keyword: string, index: number) => {
        Row() {
          Text((index + 1).toString())
            .fontSize(14)
            .fontWeight(FontWeight.Bold)
            .fontColor(index < 3 ? '#FF4D4F' : '#999999')
            .width(24)

          Text(keyword)
            .fontSize(14)
            .fontColor('#333333')
            .layoutWeight(1)
            .onClick(() => {
              this.state.keyword = keyword
              this.performSearch(keyword)
            })

          if (index < 3) {
            Text('热')
              .fontSize(10)
              .fontColor('#FFFFFF')
              .backgroundColor('#FF4D4F')
              .borderRadius(4)
              .padding({ left: 4, right: 4, top: 2, bottom: 2 })
          }
        }
        .width('100%')
        .padding({ top: 10, bottom: 10 })
      })
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
  }

  @Builder
  buildResultsSection() {
    Column() {
      if (this.state.searching) {
        Text('搜索中...')
          .fontSize(14)
          .fontColor('#999999')
          .padding(20)
          .width('100%')
          .textAlign(TextAlign.Center)
      } else if (this.state.results.length === 0) {
        Column() {
          Text('🔍')
            .fontSize(48)

          Text('未找到相关结果')
            .fontSize(14)
            .fontColor('#999999')
            .margin({ top: 12 })
        }
        .width('100%')
        .padding(40)
      } else {
        Text(`找到 ${this.state.results.length} 个结果`)
          .fontSize(13)
          .fontColor('#999999')
          .margin({ bottom: 12 })

        ForEach(this.state.results, (result: SearchResult) => {
          Row() {
            Text(result.icon)
              .fontSize(24)
              .margin({ right: 12 })

            Column() {
              Text(result.title)
                .fontSize(15)
                .fontColor('#333333')
                .maxLines(1)
                .textOverflow({ overflow: TextOverflow.Ellipsis })

              Text(result.subtitle)
                .fontSize(12)
                .fontColor('#999999')
                .margin({ top: 4 })
            }
            .alignItems(HorizontalAlign.Start)
            .layoutWeight(1)

            Text(this.getResultTypeText(result.type))
              .fontSize(11)
              .fontColor('#1890FF')
              .backgroundColor('#1890FF20')
              .borderRadius(4)
              .padding({ left: 6, right: 6, top: 2, bottom: 2 })
          }
          .width('100%')
          .padding(12)
          .backgroundColor('#FFFFFF')
          .borderRadius(8)
          .margin({ bottom: 8 })
          .onClick(() => this.handleResultClick(result))
        })
      }
    }
    .width('100%')
  }
}
