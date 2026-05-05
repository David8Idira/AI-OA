/**
 * Knowledge Detail Page - 知识详情页面
 */
import router from '@ohos.router';
import { KnowledgeDoc } from '../../model/knowledge/KnowledgeModel';
import { knowledgeService } from '../../service/KnowledgeService';

@Component
export struct KnowledgeDetail {
  @State document: KnowledgeDoc | null = null;
  @State loading: boolean = false;
  @State isLiked: boolean = false;

  private docId: string = '';

  aboutToAppear() {
    const params = router.getParams() as Record<string, string>;
    this.docId = params?.id || '';
    if (this.docId) {
      this.loadDocumentDetail();
    }
  }

  loadDocumentDetail() {
    this.loading = true;
    knowledgeService.getDocumentDetail(this.docId)
      .then((doc) => {
        this.document = doc;
      })
      .catch(() => {
        // 处理错误
      })
      .finally(() => {
        this.loading = false;
      });
  }

  build() {
    Column() {
      // 顶部导航栏
      this.NavigationBar()

      if (this.loading) {
        LoadingProgress()
          .width(40)
          .height(40)
          .color('#667EEA')
      } else if (this.document) {
        Scroll() {
          Column() {
            // 文档头部
            this.DocumentHeader()

            // 文档内容
            this.DocumentContent()

            // 操作按钮
            this.ActionButtons()

            // 评论区
            this.CommentsSection()
          }
          .width('100%')
          .padding(16)
        }
        .layoutWeight(1)
        .scrollBar(BarState.Auto)
        .edgeEffect(EdgeEffect.Spring)
      } else {
        Text('未找到文档')
          .fontSize(14)
          .fontColor('#999999')
      }
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

      Text('文档详情')
        .fontSize(18)
        .fontWeight(FontWeight.Medium)
        .layoutWeight(1)
        .textAlign(TextAlign.Center)

      Row() {
        Image($r('app.media.ic_share'))
          .width(20)
          .height(20)
          .margin({ right: 12 })
          .onClick(() => {
            // 分享
          })

        Image($r('app.media.ic_more'))
          .width(20)
          .height(20)
          .onClick(() => {
            // 更多操作
          })
      }
    }
    .width('100%')
    .height(56)
    .padding({ left: 16, right: 16 })
    .backgroundColor('#FFFFFF')
    .shadow({ radius: 4, color: '#08000000', offsetX: 0, offsetY: 2 })
  }

  @Builder
  DocumentHeader() {
    Column() {
      Text(this.document?.title || '')
        .fontSize(20)
        .fontWeight(FontWeight.Bold)
        .fontColor('#333333')
        .width('100%')

      Row() {
        Text(this.document?.author || '')
          .fontSize(12)
          .fontColor('#667EEA')
          .backgroundColor('#ECF5FF')
          .padding({ left: 8, right: 8, top: 2, bottom: 2 })
          .borderRadius(4)
          .margin({ right: 8 })

        Text(this.getCategoryLabel(this.document?.category || ''))
          .fontSize(12)
          .fontColor('#E6A23C')
          .backgroundColor('#FDF6EC')
          .padding({ left: 8, right: 8, top: 2, bottom: 2 })
          .borderRadius(4)
      }
      .margin({ top: 12 })

      Row() {
        Text(`浏览: ${this.document?.viewCount || 0}`)
          .fontSize(12)
          .fontColor('#999999')
          .margin({ right: 16 })

        Text(`点赞: ${this.document?.likeCount || 0}`)
          .fontSize(12)
          .fontColor('#999999')
          .margin({ right: 16 })

        Text(`评论: ${this.document?.commentCount || 0}`)
          .fontSize(12)
          .fontColor('#999999')

        Blank()

        Text(this.document?.updateTime || '')
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
    .margin({ bottom: 16 })
  }

  @Builder
  DocumentContent() {
    Column() {
      Text(this.document?.content || '')
        .fontSize(14)
        .fontColor('#333333')
        .lineHeight(24)
        .width('100%')
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 16 })
  }

  @Builder
  ActionButtons() {
    Row() {
      Button() {
        Row() {
          Text(this.isLiked ? '👍' : '🤍')
            .fontSize(16)
            .margin({ right: 6 })
          Text('点赞')
            .fontSize(14)
            .fontColor(this.isLiked ? '#667EEA' : '#666666')
        }
      }
      .width('45%')
      .height(40)
      .backgroundColor(this.isLiked ? '#ECF5FF' : '#FFFFFF')
      .borderRadius(20)
      .border({ width: 1, color: this.isLiked ? '#667EEA' : '#E8E8E8' })
      .onClick(() => {
        this.isLiked = !this.isLiked;
      })

      Button() {
        Row() {
          Image($r('app.media.ic_bookmark'))
            .width(18)
            .height(18)
            .margin({ right: 6 })

          Text('收藏')
            .fontSize(14)
            .fontColor('#666666')
        }
      }
      .width('45%')
      .height(40)
      .backgroundColor('#FFFFFF')
      .borderRadius(20)
      .border({ width: 1, color: '#E8E8E8' })
      .onClick(() => {
        // 收藏
      })
    }
    .width('100%')
    .margin({ bottom: 16 })
  }

  @Builder
  CommentsSection() {
    Column() {
      Text('评论')
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .width('100%')
        .margin({ bottom: 12 })

      // 评论输入框
      Row() {
        TextInput({ placeholder: '写下你的评论...' })
          .layoutWeight(1)
          .height(40)
          .borderRadius(20)
          .backgroundColor('#F5F7FA')

        Button('发送')
          .height(36)
          .fontSize(12)
          .backgroundColor('#667EEA')
          .borderRadius(18)
          .margin({ left: 8 })
      }
      .width('100%')
      .margin({ bottom: 16 })

      // 模拟评论列表
      Column() {
        this.CommentItem('张三', '非常实用的文档，感谢分享！', '2024-04-28')
        this.CommentItem('李四', '内容很详细，对新员工很有帮助', '2024-04-27')
      }
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
  }

  @Builder
  CommentItem(author: string, content: string, time: string) {
    Column() {
      Row() {
        Text(author)
          .fontSize(14)
          .fontWeight(FontWeight.Medium)
          .fontColor('#333333')

        Blank()

        Text(time)
          .fontSize(12)
          .fontColor('#999999')
      }
      .width('100%')
      .margin({ bottom: 8 })

      Text(content)
        .fontSize(14)
        .fontColor('#666666')
        .width('100%')
    }
    .width('100%')
    .padding({ top: 12, bottom: 12 })
    .border({
      width: { bottom: 1 },
      color: '#F5F5F5'
    })
  }

  getCategoryLabel(category: string): string {
    return category;
  }
}