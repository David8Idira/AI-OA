/**
 * Knowledge Search Page - 知识搜索页面
 */
import router from '@ohos.router';
import { SearchResult, KnowledgeDoc } from '../../model/knowledge/KnowledgeModel';
import { knowledgeService } from '../../service/KnowledgeService';

@Component
export struct KnowledgeSearch {
  @State keyword: string = '';
  @State semanticQuery: string = '';
  @State keywordResults: SearchResult[] = [];
  @State semanticResults: SearchResult[] = [];
  @State isSearching: boolean = false;
  @State isSemanticSearching: boolean = false;
  @State activeTab: string = 'keyword';
  @State hasSearched: boolean = false;

  private scroller: Scroller = new Scroller();

  aboutToAppear() {
    // 检查是否有传入的搜索词
    const params = router.getParams() as Record<string, string>;
    if (params?.keyword) {
      this.keyword = params.keyword;
      this.handleKeywordSearch();
    }
  }

  build() {
    Column() {
      // 搜索区域
      this.SearchArea()

      // 搜索结果
      if (this.hasSearched) {
        this.SearchResults()
      } else {
        this.SearchHints()
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F7FA')
  }

  @Builder
  SearchArea() {
    Column() {
      // 返回按钮和标题
      Row() {
        Image($r('app.media.ic_back'))
          .width(24)
          .height(24)
          .onClick(() => router.back())

        Text('知识搜索')
          .fontSize(18)
          .fontWeight(FontWeight.Medium)
          .layoutWeight(1)
          .textAlign(TextAlign.Center)

        Text('')
          .width(24)
      }
      .width('100%')
      .height(56)
      .padding({ left: 16, right: 16 })

      // 关键词搜索
      Row() {
        TextInput({ placeholder: '搜索知识库...', text: this.keyword })
          .width('70%')
          .height(40)
          .borderRadius(20)
          .backgroundColor('#F5F7FA')
          .onChange((value: string) => {
            this.keyword = value;
          })

        Button('搜索')
          .width('25%')
          .height(40)
          .backgroundColor('#667EEA')
          .borderRadius(20)
          .onClick(() => this.handleKeywordSearch())
      }
      .width('100%')
      .padding({ left: 16, right: 16 })

      // 语义搜索
      Row() {
        TextInput({ placeholder: '语义搜索（支持自然语言）...', text: this.semanticQuery })
          .width('70%')
          .height(40)
          .borderRadius(20)
          .backgroundColor('#F0F9EB')
          .onChange((value: string) => {
            this.semanticQuery = value;
          })

        Button('语义')
          .width('25%')
          .height(40)
          .backgroundColor('#67C23A')
          .borderRadius(20)
          .onClick(() => this.handleSemanticSearch())
      }
      .width('100%')
      .padding({ left: 16, right: 16 })
      .margin({ top: 12 })
    }
    .width('100%')
    .backgroundColor('#FFFFFF')
    .padding({ bottom: 16 })
  }

  @Builder
  SearchHints() {
    Column() {
      Text('热门搜索')
        .fontSize(14)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .width('100%')
        .padding({ left: 16, bottom: 12 })

      Row() {
        ForEach(['考勤制度', '年假申请', '报销流程', '员工手册', '产品介绍'], (tag: string) => {
          Text(tag)
            .fontSize(12)
            .padding({ left: 14, right: 14, top: 8, bottom: 8 })
            .backgroundColor('#F5F7FA')
            .fontColor('#666666')
            .borderRadius(16)
            .margin({ right: 8 })
            .onClick(() => {
              this.keyword = tag;
              this.handleKeywordSearch();
            })
        })
      }
      .width('100%')
      .padding({ left: 16, right: 16 })
      .scrollBar(BarState.Off)

      Text('搜索历史')
        .fontSize(14)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .width('100%')
        .padding({ left: 16, top: 24, bottom: 12 })

      Column() {
        Row() {
          Image($r('app.media.ic_history'))
            .width(16)
            .height(16)
            .margin({ right: 8 })

          Text('员工年假政策')
            .fontSize(14)
            .fontColor('#666666')

          Blank()

          Text('2024-04-28')
            .fontSize(12)
            .fontColor('#999999')
        }
        .width('100%')
        .padding({ left: 16, right: 16, top: 12, bottom: 12 })

        Row() {
          Image($r('app.media.ic_history'))
            .width(16)
            .height(16)
            .margin({ right: 8 })

          Text('报销制度')
            .fontSize(14)
            .fontColor('#666666')

          Blank()

          Text('2024-04-25')
            .fontSize(12)
            .fontColor('#999999')
        }
        .width('100%')
        .padding({ left: 16, right: 16, top: 12, bottom: 12 })
      }
      .width('100%')
      .backgroundColor('#FFFFFF')
      .borderRadius(12)
      .margin({ left: 16, right: 16, top: 8 })
    }
    .width('100%')
    .padding({ top: 16 })
  }

  @Builder
  SearchResults() {
    Column() {
      // Tab切换
      Row() {
        Text('关键词搜索')
          .fontSize(14)
          .fontColor(this.activeTab === 'keyword' ? '#667EEA' : '#999999')
          .onClick(() => {
            this.activeTab = 'keyword';
          })

        Text(' | ')
          .fontSize(14)
          .fontColor('#E8E8E8')

        Text('语义搜索')
          .fontSize(14)
          .fontColor(this.activeTab === 'semantic' ? '#667EEA' : '#999999')
          .onClick(() => {
            this.activeTab = 'semantic';
          })

        Blank()

        Text(`找到 ${this.activeTab === 'keyword' ? this.keywordResults.length : this.semanticResults.length} 条结果`)
          .fontSize(12)
          .fontColor('#999999')
      }
      .width('100%')
      .padding({ left: 16, right: 16, top: 16, bottom: 12 })

      Scroll(this.scroller) {
        Column() {
          if (this.activeTab === 'keyword') {
            if (this.isSearching) {
              LoadingProgress()
                .width(40)
                .height(40)
                .color('#667EEA')
            } else if (this.keywordResults.length === 0) {
              this.EmptyResults()
            } else {
              ForEach(this.keywordResults, (result: SearchResult) => {
                this.ResultCard(result)
              })
            }
          } else {
            if (this.isSemanticSearching) {
              LoadingProgress()
                .width(40)
                .height(40)
                .color('#667EEA')
            } else if (this.semanticResults.length === 0) {
              this.EmptyResults()
            } else {
              ForEach(this.semanticResults, (result: SearchResult) => {
                this.SemanticResultCard(result)
              })
            }
          }
        }
        .width('100%')
        .padding({ left: 16, right: 16 })
      }
      .layoutWeight(1)
      .scrollBar(BarState.Auto)
      .edgeEffect(EdgeEffect.Spring)
    }
    .width('100%')
    .layoutWeight(1)
  }

  @Builder
  EmptyResults() {
    Column() {
      Image($r('app.media.ic_search'))
        .width(60)
        .height(60)
        .opacity(0.5)

      Text('未找到相关结果')
        .fontSize(14)
        .fontColor('#999999')
        .margin({ top: 16 })

      Text('试试其他关键词或语义搜索')
        .fontSize(12)
        .fontColor('#CCCCCC')
        .margin({ top: 8 })
    }
    .width('100%')
    .padding(60)
    .alignItems(HorizontalAlign.Center)
  }

  @Builder
  ResultCard(result: SearchResult) {
    Column() {
      Row() {
        Text(result.doc.title)
          .fontSize(14)
          .fontWeight(FontWeight.Medium)
          .fontColor('#333333')
          .layoutWeight(1)

        Text(this.getTypeLabel(result.doc.docType))
          .fontSize(12)
          .fontColor('#667EEA')
          .backgroundColor('#ECF5FF')
          .padding({ left: 6, right: 6, top: 2, bottom: 2 })
          .borderRadius(4)
      }
      .width('100%')

      Text(result.matchedContent)
        .fontSize(12)
        .fontColor('#666666')
        .margin({ top: 8 })
        .maxLines(3)
        .textOverflow({ overflow: TextOverflow.Ellipsis })

      Row() {
        Text(`浏览: ${result.doc.viewCount}`)
          .fontSize(12)
          .fontColor('#999999')

        Text(`相关度: ${Math.round(result.relevanceScore * 100)}%`)
          .fontSize(12)
          .fontColor('#67C23A')
          .margin({ left: 16 })

        Blank()

        Text(result.doc.updateTime)
          .fontSize(12)
          .fontColor('#999999')
      }
      .margin({ top: 8 })
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
    .onClick(() => {
      router.pushUrl({ url: 'pages/knowledge/KnowledgeDetail', params: { id: result.doc.id } });
    })
  }

  @Builder
  SemanticResultCard(result: SearchResult) {
    Column() {
      Row() {
        Text(result.doc.title)
          .fontSize(14)
          .fontWeight(FontWeight.Medium)
          .fontColor('#333333')
          .layoutWeight(1)

        Text('语义匹配')
          .fontSize(12)
          .fontColor('#67C23A')
          .backgroundColor('#F0F9EB')
          .padding({ left: 6, right: 6, top: 2, bottom: 2 })
          .borderRadius(4)
      }
      .width('100%')

      Text(result.matchedContent)
        .fontSize(12)
        .fontColor('#666666')
        .margin({ top: 8 })
        .maxLines(3)
        .textOverflow({ overflow: TextOverflow.Ellipsis })

      Row() {
        Text(`相关度: 高`)
          .fontSize(12)
          .fontColor('#67C23A')

        Blank()

        Text(result.doc.updateTime)
          .fontSize(12)
          .fontColor('#999999')
      }
      .margin({ top: 8 })
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
    .onClick(() => {
      router.pushUrl({ url: 'pages/knowledge/KnowledgeDetail', params: { id: result.doc.id } });
    })
  }

  handleKeywordSearch() {
    if (!this.keyword.trim()) return;

    this.isSearching = true;
    this.activeTab = 'keyword';
    this.hasSearched = true;

    knowledgeService.searchDocuments({ keyword: this.keyword })
      .then((results) => {
        this.keywordResults = results;
      })
      .catch(() => {
        // 处理错误
      })
      .finally(() => {
        this.isSearching = false;
      });
  }

  handleSemanticSearch() {
    if (!this.semanticQuery.trim()) return;

    this.isSemanticSearching = true;
    this.activeTab = 'semantic';
    this.hasSearched = true;

    knowledgeService.semanticSearch(this.semanticQuery)
      .then((results) => {
        this.semanticResults = results;
      })
      .catch(() => {
        // 处理错误
      })
      .finally(() => {
        this.isSemanticSearching = false;
      });
  }

  getTypeLabel(type: string): string {
    const map: Record<string, string> = {
      'policy': '公司制度',
      'product': '产品文档',
      'tech': '技术文档',
      'training': '培训资料',
      'faq': '常见问题'
    };
    return map[type] || type;
  }
}