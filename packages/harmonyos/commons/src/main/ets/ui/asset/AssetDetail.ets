/**
 * Asset Detail Page - 资产详情页面
 */
import router from '@ohos.router';
import { assetService } from '../../service/AssetService';
import { AssetInfo } from '../../model/asset/AssetModel';

@Component
export struct AssetDetail {
  @State asset: AssetInfo | null = null;
  @State loading: boolean = false;

  private assetId: string = '';

  aboutToAppear() {
    const params = router.getParams() as Record<string, string>;
    this.assetId = params?.id || '';
    if (this.assetId) {
      this.loadAssetDetail();
    }
  }

  loadAssetDetail() {
    this.loading = true;
    assetService.getAssetDetail(this.assetId)
      .then((data) => {
        this.asset = data;
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
      } else if (this.asset) {
        Scroll() {
          Column() {
            // 基本信息卡片
            this.BaseInfoCard()

            // 库存信息卡片
            this.StockInfoCard()

            // 操作按钮
            this.ActionButtons()
          }
          .width('100%')
          .padding(16)
        }
        .layoutWeight(1)
        .scrollBar(BarState.Auto)
        .edgeEffect(EdgeEffect.Spring)
      } else {
        Text('未找到资产信息')
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

      Text('资产详情')
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
  BaseInfoCard() {
    Column() {
      Text('基本信息')
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .width('100%')
        .padding({ bottom: 12 })

      Column() {
        this.InfoRow('资产编码', this.asset?.assetCode || '-')
        this.InfoRow('资产名称', this.asset?.assetName || '-')
        this.InfoRow('资产分类', this.asset?.categoryName || '-')
        this.InfoRow('规格型号', this.asset?.model || '-')
        this.InfoRow('负责人', this.asset?.responsiblePerson || '-')
        this.InfoRow('存放位置', this.asset?.location || '-')
        this.InfoRow('购买价格', `¥${this.asset?.purchasePrice?.toFixed(2) || '0.00'}`)
      }
      .width('100%')
      .padding(16)
      .backgroundColor('#FFFFFF')
      .borderRadius(12)
    }
    .width('100%')
    .margin({ bottom: 16 })
  }

  @Builder
  StockInfoCard() {
    Column() {
      Text('库存信息')
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .width('100%')
        .padding({ bottom: 12 })

      Row() {
        Column() {
          Text(this.asset?.currentQuantity?.toString() || '0')
            .fontSize(24)
            .fontWeight(FontWeight.Bold)
            .fontColor('#667EEA')
          Text('当前数量')
            .fontSize(12)
            .fontColor('#999999')
            .margin({ top: 4 })
        }
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Center)

        Column() {
          Text(this.asset?.warningQuantity?.toString() || '0')
            .fontSize(24)
            .fontWeight(FontWeight.Bold)
            .fontColor('#E6A23C')
          Text('预警数量')
            .fontSize(12)
            .fontColor('#999999')
            .margin({ top: 4 })
        }
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Center)
      }
      .width('100%')
      .padding(16)
      .backgroundColor('#FFFFFF')
      .borderRadius(12)

      // 库存预警提示
      if (this.asset && this.asset.currentQuantity <= this.asset.warningQuantity) {
        Text(`⚠️ 库存不足，当前数量低于预警数量`)
          .fontSize(12)
          .fontColor('#F56C6C')
          .backgroundColor('#FEF0F0')
          .padding(12)
          .borderRadius(8)
          .margin({ top: 12 })
          .width('100%')
      }
    }
    .width('100%')
    .margin({ bottom: 16 })
  }

  @Builder
  ActionButtons() {
    Row() {
      Button('领用')
        .width('45%')
        .height(44)
        .backgroundColor('#67C23A')
        .borderRadius(22)
        .onClick(() => {
          this.handleBorrow();
        })

      Button('归还')
        .width('45%')
        .height(44)
        .backgroundColor('#409EFF')
        .borderRadius(22)
        .onClick(() => {
          this.handleReturn();
        })
    }
    .width('100%')
    .justifyContent(FlexAlign.SpaceEvenly)
  }

  @Builder
  InfoRow(label: string, value: string) {
    Row() {
      Text(label)
        .fontSize(14)
        .fontColor('#666666')
        .width(100)

      Text(value)
        .fontSize(14)
        .fontColor('#333333')
        .layoutWeight(1)
    }
    .width('100%')
    .padding({ top: 8, bottom: 8 })
  }

  handleBorrow() {
    if (this.asset) {
      assetService.borrowAsset(this.asset.id)
        .then(() => {
          // 提示领用成功
          this.loadAssetDetail();
        })
        .catch(() => {
          // 处理错误
        });
    }
  }

  handleReturn() {
    if (this.asset) {
      assetService.returnAsset(this.asset.id)
        .then(() => {
          // 提示归还成功
          this.loadAssetDetail();
        })
        .catch(() => {
          // 处理错误
        });
    }
  }
}