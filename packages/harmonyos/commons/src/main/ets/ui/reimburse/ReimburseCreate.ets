/**
 * Reimburse Create Page - 创建报销单页面
 */
import router from '@ohos.router';
import { reimburseService } from '../../service/ReimburseService';

@Component
export struct ReimburseCreate {
  @State title: string = '';
  @State amount: string = '';
  @State description: string = '';
  @State selectedCategory: string = '';
  @State invoiceCount: number = 0;
  @State isSubmitting: boolean = false;

  private categories = ['差旅费', '办公用品', '招待费', '交通费', '团建费', '其他'];

  build() {
    Column() {
      // 顶部导航栏
      this.NavigationBar()

      Scroll() {
        Column() {
          // 报销标题
          this.TitleSection()

          // 报销金额
          this.AmountSection()

          // 费用类别
          this.CategorySection()

          // 发票数量
          this.InvoiceSection()

          // 报销说明
          this.DescriptionSection()

          // 提交按钮
          this.SubmitButton()
        }
        .width('100%')
        .padding(16)
      }
      .layoutWeight(1)
      .scrollBar(BarState.Auto)
      .edgeEffect(EdgeEffect.Spring)
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

      Text('新建报销单')
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
    .backgroundColor('#FFFFFF')
    .shadow({ radius: 4, color: '#08000000', offsetX: 0, offsetY: 2 })
  }

  @Builder
  TitleSection() {
    Column() {
      Text('报销标题')
        .fontSize(14)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .width('100%')
        .margin({ bottom: 8 })

      TextInput({ placeholder: '请输入报销标题' })
        .width('100%')
        .height(48)
        .borderRadius(8)
        .backgroundColor('#FFFFFF')
        .onChange((value: string) => {
          this.title = value;
        })
    }
    .width('100%')
    .margin({ bottom: 16 })
  }

  @Builder
  AmountSection() {
    Column() {
      Text('报销金额')
        .fontSize(14)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .width('100%')
        .margin({ bottom: 8 })

      Row() {
        Text('¥')
          .fontSize(24)
          .fontWeight(FontWeight.Bold)
          .fontColor('#333333')
          .margin({ right: 8 })

        TextInput({ placeholder: '0.00' })
          .width('80%')
          .height(48)
          .borderRadius(8)
          .backgroundColor('#FFFFFF')
          .fontSize(24)
          .fontWeight(FontWeight.Bold)
          .type(InputType.Number)
          .onChange((value: string) => {
            this.amount = value;
          })
      }
      .width('100%')
      .alignItems(VerticalAlign.Center)
    }
    .width('100%')
    .margin({ bottom: 16 })
  }

  @Builder
  CategorySection() {
    Column() {
      Text('费用类别')
        .fontSize(14)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .width('100%')
        .margin({ bottom: 8 })

      Row() {
        ForEach(this.categories, (cat: string) => {
          Text(cat)
            .fontSize(13)
            .padding({ left: 14, right: 14, top: 8, bottom: 8 })
            .backgroundColor(this.selectedCategory === cat ? '#667EEA' : '#FFFFFF')
            .fontColor(this.selectedCategory === cat ? '#FFFFFF' : '#666666')
            .borderRadius(16)
            .margin({ right: 8 })
            .onClick(() => {
              this.selectedCategory = cat;
            })
        })
      }
      .width('100%')
      .scrollBar(BarState.Off)
    }
    .width('100%')
    .margin({ bottom: 16 })
  }

  @Builder
  InvoiceSection() {
    Column() {
      Row() {
        Text('关联发票')
          .fontSize(14)
          .fontWeight(FontWeight.Medium)
          .fontColor('#333333')

        Blank()

        Text(`${this.invoiceCount} 张`)
          .fontSize(14)
          .fontColor('#667EEA')
      }
      .width('100%')
      .margin({ bottom: 8 })

      Row() {
        Button() {
          Row() {
            Image($r('app.media.ic_camera'))
              .width(20)
              .height(20)
              .margin({ right: 8 })

            Text('拍照识别发票')
              .fontSize(14)
              .fontColor('#67C23A')
          }
        }
        .width('48%')
        .height(44)
        .backgroundColor('#F0F9EB')
        .borderRadius(22)
        .border({ width: 1, color: '#67C23A' })
        .onClick(() => {
          // OCR扫描发票
        })

        Button() {
          Row() {
            Image($r('app.media.ic_add'))
              .width(20)
              .height(20)
              .margin({ right: 8 })

            Text('手动添加')
              .fontSize(14)
              .fontColor('#667EEA')
          }
        }
        .width('48%')
        .height(44)
        .backgroundColor('#ECF5FF')
        .borderRadius(22)
        .border({ width: 1, color: '#667EEA' })
        .onClick(() => {
          this.invoiceCount++;
        })
      }
      .width('100%')
      .justifyContent(FlexAlign.SpaceBetween)
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 16 })
  }

  @Builder
  DescriptionSection() {
    Column() {
      Text('报销说明')
        .fontSize(14)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .width('100%')
        .margin({ bottom: 8 })

      TextArea({ placeholder: '请输入报销说明（选填）' })
        .width('100%')
        .height(100)
        .borderRadius(8)
        .backgroundColor('#FFFFFF')
        .onChange((value: string) => {
          this.description = value;
        })
    }
    .width('100%')
    .margin({ bottom: 32 })
  }

  @Builder
  SubmitButton() {
    Button() {
      if (this.isSubmitting) {
        Row() {
          LoadingProgress()
            .width(20)
            .height(20)
            .color('#FFFFFF')
            .margin({ right: 8 })

          Text('提交中...')
            .fontSize(16)
            .fontColor('#FFFFFF')
        }
      } else {
        Text('提交报销')
          .fontSize(16)
          .fontColor('#FFFFFF')
      }
    }
    .width('100%')
    .height(48)
    .backgroundColor(this.canSubmit() ? '#667EEA' : '#CCCCCC')
    .borderRadius(24)
    .enabled(this.canSubmit() && !this.isSubmitting)
    .onClick(() => this.handleSubmit())
  }

  canSubmit(): boolean {
    return this.title.trim().length > 0 &&
           parseFloat(this.amount) > 0 &&
           this.selectedCategory.length > 0;
  }

  handleSubmit() {
    if (!this.canSubmit()) return;

    this.isSubmitting = true;
    reimburseService.createReimburse({
      title: this.title,
      amount: parseFloat(this.amount),
      description: this.description,
      invoiceIds: []
    }).then(() => {
      // 提交成功后返回
      router.back();
    }).catch(() => {
      // 处理错误
    }).finally(() => {
      this.isSubmitting = false;
    });
  }
}