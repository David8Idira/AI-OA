/**
 * Knowledge List Page - 知识库列表页面
 */
import router from '@ohos.router';
import { KnowledgeDoc, KnowledgeStats } from '../../model/knowledge/KnowledgeModel';
import { knowledgeService } from '../../service/KnowledgeService';

@Component
export struct KnowledgeList {
  @State documents: KnowledgeDoc[] = [];
  @State stats: KnowledgeStats = { totalDocs: 0, totalViews: 0, totalLikes: 0, totalComments: 0 };
  @State loading: boolean = false;
  @State selectedCategory: string = '';

  private scroller: Scroller = new Scroller();
  private categories = ['全部', '公司制度', '产品文档', '技术文档', '培训资料', '常见问题'];

  aboutToAppear() {
    this.loadDocuments();
    this.loadStats();
  }

  loadDocuments() {
    this.loading = true;
    knowledgeService.getDocumentList({
      category: this.selectedCategory === '全部' ? '' : this.selectedCategory
    }).then((list) => {
      this.documents = list;
    }).catch(() => {
      // 处理错误
    }).finally(() => {
      this.loading = false;
    });
  }

  loadStats() {
    knowledgeService.getKnowledgeStats().then((s) => {
      this.stats = s;
    }).catch(() => {
      // 处理错误
    });
  }

  build() {
    Column() {
      // 顶部导航栏
      this.NavigationBar()

      Scroll(this.scroller) {
        Column() {
          // 分类标签
          this.CategoryTabs()

          // 统计信息
          this.StatsSection()

          // 文档列表
          this.DocumentListSection()
        }
        .width('100%')
        .padding(16)
      }
      .layoutWeight(1)
      .scrollBar(BarState.Auto)
      .edgeEffect(EdgeEffect.Spring)

      // 底部搜索入口
      this.SearchBar()
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

      Text('知识库')
        .fontSize(18)
        .fontWeight(FontWeight.Medium)
        .layoutWeight(1)
        .textAlign(TextAlign.Center)

      Image($r('app.media.ic_add'))
        .width(24)
        .height(24)
        .onClick(() => {
          // 新建文档
        })
    }
    .width('100%')
    .height(56)
    .padding({ left: 16, right: 16 })
    .backgroundColor('#FFFFFF')
    .shadow({ radius: 4, color: '#08000000', offsetX: 0, offsetY: 2 })
  }

  @Builder
  CategoryTabs() {
    Scroll() {
      Row() {
        ForEach(this.categories, (cat: string) => {
          Text(cat)
            .fontSize(14)
            .padding({ left: 16, right: 16, top: 10, bottom: 10 })
            .backgroundColor(this.selectedCategory === cat || (cat === '全部' && this.selectedCategory === '') ? '#667EEA' : '#FFFFFF')
            .fontColor(this.selectedCategory === cat || (cat === '全部' && this.selectedCategory === '') ? '#FFFFFF' : '#666666')
            .borderRadius(20)
            .margin({ right: 8 })
            .onClick(() => {
              this.selectedCategory = cat === '全部' ? '' : cat;
              this.loadDocuments();
            })
        })
      }
    }
    .width('100%')
    .height(50)
    .scrollBar(BarState.Off)
    .margin({ bottom: 16 })
  }

  @Builder
  StatsSection() {
    Column() {
      Text('知识库统计')
        .fontSize(14)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .width('100%')
        .margin({ bottom: 12 })

      Row() {
        this.StatItem('文档总数', this.stats.totalDocs.toString())
        this.StatItem('总浏览量', this.stats.totalViews.toLocaleString())
        this.StatItem('总点赞数', this.stats.totalLikes.toString())
        this.StatItem('总评论数', this.stats.totalComments.toString())
      }
      .width('100%')
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 16 })
  }

  @Builder
  StatItem(label: string, value: string) {
    Column() {
      Text(value)
        .fontSize(16)
        .fontWeight(FontWeight.Bold)
        .fontColor('#667EEA')

      Text(label)
        .fontSize(12)
        .fontColor('#999999')
        .margin({ top: 4 })
    }
    .layoutWeight(1)
    .alignItems(HorizontalAlign.Center)
  }

  @Builder
  DocumentListSection() {
    Column() {
      Row() {
        Text('文档列表')
          .fontSize(16)
          .fontWeight(FontWeight.Medium)

        Blank()

        Text(`${this.documents.length} 篇`)
          .fontSize(12)
          .fontColor('#999999')
      }
      .width('100%')
      .margin({ bottom: 12 })

      if (this.loading) {
        LoadingProgress()
          .width(40)
          .height(40)
          .color('#667EEA')
      } else if (this.documents.length === 0) {
        Column() {
          Image($r('app.media.ic_empty'))
            .width(80)
            .height(80)
            .opacity(0.5)

          Text('暂无文档')
            .fontSize(14)
            .fontColor('#999999')
            .margin({ top: 16 })
        }
        .width('100%')
        .padding(60)
        .backgroundColor('#FFFFFF')
        .borderRadius(12)
        .alignItems(HorizontalAlign.Center)
      } else {
        ForEach(this.documents, (doc: KnowledgeDoc) => {
          this.DocumentCard(doc)
        })
      }
    }
    .width('100%')
  }

  @Builder
  DocumentCard(doc: KnowledgeDoc) {
    Column() {
      Row() {
        Column() {
          Text(doc.title)
            .fontSize(14)
            .fontWeight(FontWeight.Medium)
            .fontColor('#333333')

          Text(doc.summary || doc.content.substring(0, 50) + '...')
            .fontSize(12)
            .fontColor('#666666')
            .margin({ top: 6 })
            .maxLines(2)
            .textOverflow({ overflow: TextOverflow.Ellipsis })
        }
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Start)

        Text(this.getDocTypeLabel(doc.docType))
          .fontSize(12)
          .fontColor('#667EEA')
          .backgroundColor('#ECF5FF')
          .padding({ left: 8, right: 8, top: 2, bottom: 2 })
          .borderRadius(4)
      }
      .width('100%')

      Row() {
        Text(`浏览: ${doc.viewCount}`)
          .fontSize(12)
          .fontColor('#999999')

        Text(`点赞: ${doc.likeCount}`)
          .fontSize(12)
          .fontColor('#999999')
          .margin({ left: 16 })

        Text(`评论: ${doc.commentCount}`)
          .fontSize(12)
          .fontColor('#999999')
          .margin({ left: 16 })

        Blank()

        Text(doc.updateTime)
          .fontSize(12)
          .fontColor('#999999')
      }
      .margin({ top: 12 })
      .width('100%')
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
    .onClick(() => {
      router.pushUrl({ url: 'pages/knowledge/KnowledgeDetail', params: { id: doc.id } });
    })
  }

  @Builder
  SearchBar() {
    Row() {
      Text('🔍')
        .fontSize(16)
        .margin({ right: 8 })

      Text('搜索知识库...')
        .fontSize(14)
        .fontColor('#999999')
        .layoutWeight(1)

      Text('语义搜索')
        .fontSize(12)
        .fontColor('#667EEA')
        .backgroundColor('#ECF5FF')
        .padding({ left: 10, right: 10, top: 4, bottom: 4 })
        .borderRadius(12)
        .onClick(() => {
          router.pushUrl({ url: 'pages/knowledge/KnowledgeSearch' });
        })
    }
    .width('90%')
    .height(48)
    .padding({ left: 16, right: 16 })
    .backgroundColor('#FFFFFF')
    .borderRadius(24)
    .shadow({ radius: 8, color: '#08000000', offsetX: 0, offsetY: 2 })
    .margin({ bottom: 16 })
    .onClick(() => {
      router.pushUrl({ url: 'pages/knowledge/KnowledgeSearch' });
    })
  }

  getDocTypeLabel(type: string): string {
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