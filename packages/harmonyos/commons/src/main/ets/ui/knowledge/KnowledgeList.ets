import router from '@ohos.router'

interface Knowledge {
  id: string
  title: string
  category: string
  updateTime: string
  views: number
  summary: string
}

class KnowledgeListState {
  articles: Knowledge[] = []
  loading: boolean = false
  searchText: string = ''
  selectedCategory: string = '全部'
}

@Entry
@Component
struct KnowledgeList {
  @State state: KnowledgeListState = new KnowledgeListState()
  private scrollController: Scroller = new Scroller()

  private categories: string[] = ['全部', '企业文化', '规章制度', '产品知识', '技术文档', '培训资料', '常见问题']

  aboutToAppear() {
    this.loadArticles()
  }

  async loadArticles(): Promise<void> {
    this.state.loading = true
    setTimeout(() => {
      this.state.articles = [
        { id: '1', title: '员工手册 2024版', category: '规章制度', updateTime: '2024-04-15', views: 1523, summary: '公司各项规章制度、员工行为规范、考勤休假等规定。' },
        { id: '2', title: 'OA系统使用指南', category: '技术文档', updateTime: '2024-04-20', views: 892, summary: '详细介绍了OA系统的各项功能及使用方法。' },
        { id: '3', title: '公司产品介绍', category: '产品知识', updateTime: '2024-03-28', views: 756, summary: '公司核心产品功能介绍、市场定位、竞争优势。' },
        { id: '4', title: '新员工入职培训', category: '培训资料', updateTime: '2024-04-01', views: 1245, summary: '入职须知、公司文化、工作流程等内容。' },
        { id: '5', title: '财务报销流程', category: '规章制度', updateTime: '2024-04-18', views: 634, summary: '各类费用报销标准、流程及注意事项。' },
        { id: '6', title: '常见问题FAQ', category: '常见问题', updateTime: '2024-04-22', views: 2103, summary: '工作中常见的100个问题及解答。' },
        { id: '7', title: '企业价值观解读', category: '企业文化', updateTime: '2024-03-15', views: 456, summary: '公司使命、愿景、核心价值观详解。' },
        { id: '8', title: '技术架构文档', category: '技术文档', updateTime: '2024-04-25', views: 389, summary: '系统技术架构、数据库设计、接口文档。' },
        { id: '9', title: '绩效考核制度', category: '规章制度', updateTime: '2024-04-10', views: 567, summary: '绩效考核指标、评分标准、结果应用。' },
        { id: '10', title: '销售技巧培训', category: '培训资料', updateTime: '2024-04-05', views: 423, summary: '销售话术、客户沟通、异议处理等技巧。' }
      ]
      this.state.loading = false
    }, 500)
  }

  private getFilteredArticles(): Knowledge[] {
    return this.state.articles.filter(article => {
      const matchCategory = this.state.selectedCategory === '全部' || article.category === this.state.selectedCategory
      const matchSearch = article.title.includes(this.state.searchText) || article.summary.includes(this.state.searchText)
      return matchCategory && matchSearch
    })
  }

  private getCategoryColor(category: string): string {
    switch (category) {
      case '企业文化': return '#722ED1'
      case '规章制度': return '#1890FF'
      case '产品知识': return '#52C41A'
      case '技术文档': return '#FA8C16'
      case '培训资料': return '#EB2F96'
      case '常见问题': return '#13C2C2'
      default: return '#999999'
    }
  }

  build() {
    Column() {
      // 顶部导航
      Row() {
        Text('知识库')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#333333')
      }
      .width('100%')
      .height(56)
      .padding({ left: 16 })
      .backgroundColor('#FFFFFF')
      .shadow({ radius: 4, color: '#10000000', offsetX: 0, offsetY: 2 })

      // 搜索框
      Row() {
        TextInput({ placeholder: '搜索知识文章' })
          .width('100%')
          .height(40)
          .backgroundColor('#F5F5F5')
          .borderRadius(20)
          .placeholderColor('#999999')
          .onChange((val: string) => {
            this.state.searchText = val
          })
      }
      .width('100%')
      .padding(16)

      // 分类筛选
      Column() {
        Scroll() {
          Row() {
            ForEach(this.categories, (category: string) => {
              Text(category)
                .fontSize(13)
                .fontColor(this.state.selectedCategory === category ? '#FFFFFF' : '#666666')
                .backgroundColor(this.state.selectedCategory === category ? '#1890FF' : '#E6E6E6')
                .borderRadius(16)
                .padding({ left: 14, right: 14, top: 6, bottom: 6 })
                .margin({ right: 8 })
                .onClick(() => {
                  this.state.selectedCategory = category
                })
            })
          }
        }
        .width('100%')
        .scrollable(ScrollDirection.Horizontal)
        .scrollBar(BarState.Off)
      }
      .width('100%')
      .padding({ left: 16, right: 16 })

      // 知识列表
      Scroll(this.scrollController) {
        Column() {
          if (this.state.loading) {
            Row() {
              Text('加载中...')
                .fontSize(14)
                .fontColor('#999999')
            }
            .width('100%')
            .padding(40)
          } else {
            Text(`共 ${this.getFilteredArticles().length} 篇知识文章`)
              .fontSize(12)
              .fontColor('#999999')
              .alignSelf(HorizontalAlign.Start)
              .margin({ bottom: 12, left: 4 })

            ForEach(this.getFilteredArticles(), (article: Knowledge) => {
              this.buildArticleItem(article)
            })
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
  }

  @Builder
  buildArticleItem(article: Knowledge) {
    Column() {
      Row() {
        Column() {
          Row() {
            Text(article.title)
              .fontSize(16)
              .fontWeight(FontWeight.Medium)
              .fontColor('#333333')

            Text(article.category)
              .fontSize(11)
              .fontColor('#FFFFFF')
              .backgroundColor(this.getCategoryColor(article.category))
              .borderRadius(8)
              .padding({ left: 6, right: 6, top: 2, bottom: 2 })
              .margin({ left: 8 })
          }

          Text(article.summary)
            .fontSize(13)
            .fontColor('#666666')
            .maxLines(2)
            .textOverflow({ overflow: TextOverflow.Ellipsis })
            .margin({ top: 8 })

          Row() {
            Text(`更新时间：${article.updateTime}`)
              .fontSize(11)
              .fontColor('#999999')

            Text(' | ')
              .fontSize(11)
              .fontColor('#CCCCCC')

            Text('浏览 ' + article.views)
              .fontSize(11)
              .fontColor('#999999')
          }
          .margin({ top: 8 })
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1)
      }
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
    .onClick(() => {
      router.pushUrl({ url: 'pages/KnowledgeDetail', params: { articleId: article.id } })
    })
  }
}