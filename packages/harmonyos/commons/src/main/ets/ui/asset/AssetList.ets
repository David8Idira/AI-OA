/**
 * Asset List Page - 资产列表页面
 */
import router from '@ohos.router';
import { AssetInfo, AssetCategory } from '../../model/asset/AssetModel';
import { assetService } from '../../service/AssetService';

@Component
export struct AssetList {
  @State assets: AssetInfo[] = [];
  @State categories: AssetCategory[] = [];
  @State loading: boolean = false;
  @State currentPage: number = 1;
  @State pageSize: number = 10;
  @State total: number = 0;

  @State filterName: string = '';
  @State filterCategory: string = '';
  @State filterStatus: string = '';

  private scroller: Scroller = new Scroller();

  aboutToAppear() {
    this.loadAssets();
    this.loadCategories();
  }

  loadAssets() {
    this.loading = true;
    assetService.getAssetList({
      pageNum: this.currentPage,
      pageSize: this.pageSize,
      assetName: this.filterName,
      categoryId: this.filterCategory,
      assetStatus: this.filterStatus
    }).then((result) => {
      this.assets = result.records;
      this.total = result.total;
    }).catch((err) => {
      console.error('加载资产列表失败', err);
      this.assets = [];
      this.total = 0;
    }).finally(() => {
      this.loading = false;
    });
  }

  loadCategories() {
    assetService.getCategories().then((cats) => {
      this.categories = cats;
    }).catch(() => {
      // 处理错误
    });
  }

  build() {
    Column() {
      // 搜索筛选区域
      this.FilterArea()

      // 资产列表
      this.AssetListContent()
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F7FA')
  }

  @Builder
  FilterArea() {
    Column() {
      // 筛选条件行
      Row() {
        TextInput({ placeholder: '资产名称' })
          .width('30%')
          .height(36)
          .borderRadius(8)
          .backgroundColor('#FFFFFF')
          .onChange((value: string) => {
            this.filterName = value;
          })

        Select(this.categories.map(c => c.categoryName))
          .width('25%')
          .height(36)
          .backgroundColor('#FFFFFF')
          .borderRadius(8)
          .onSelect((index: number) => {
            this.filterCategory = this.categories[index]?.id || '';
          })

        Select(['全部', '正常', '领用中', '维修中', '报废'].map(s => s))
          .width('20%')
          .height(36)
          .backgroundColor('#FFFFFF')
          .borderRadius(8)
          .onSelect((index: number) => {
            this.filterStatus = index === 0 ? '' : ['1', '2', '3', '4'][index - 1];
          })

        Button('搜索')
          .height(36)
          .backgroundColor('#667EEA')
          .borderRadius(8)
          .onClick(() => {
            this.currentPage = 1;
            this.loadAssets();
          })
      }
      .width('100%')
      .padding(16)
      .justifyContent(FlexAlign.SpaceBetween)
    }
    .width('100%')
    .backgroundColor('#FFFFFF')
    .margin({ bottom: 12 })
  }

  @Builder
  AssetListContent() {
    Column() {
      if (this.loading) {
        LoadingProgress()
          .width(40)
          .height(40)
          .color('#667EEA')
      } else {
        List(this.scroller) {
          ForEach(this.assets, (asset: AssetInfo) => {
            ListItem() {
              this.AssetCard(asset)
            }
          })
        }
        .width('100%')
        .layoutWeight(1)
        .scrollBar(BarState.Auto)
        .edgeEffect(EdgeEffect.Spring)

        // 分页
        this.Pagination()
      }
    }
    .width('100%')
    .layoutWeight(1)
    .padding({ left: 16, right: 16 })
  }

  @Builder
  AssetCard(asset: AssetInfo) {
    Row() {
      Column() {
        Row() {
          Text(asset.assetCode)
            .fontSize(14)
            .fontColor('#666666')
          Blank()
          Text(asset.categoryName)
            .fontSize(12)
            .backgroundColor('#E3F2FD')
            .fontColor('#1976D2')
            .padding({ left: 8, right: 8, top: 2, bottom: 2 })
            .borderRadius(4)
        }
        .width('100%')

        Text(asset.assetName)
          .fontSize(16)
          .fontWeight(FontWeight.Medium)
          .margin({ top: 8 })

        Row() {
          Text(`型号: ${asset.model || '-'}`)
            .fontSize(12)
            .fontColor('#999999')
          Text(`数量: ${asset.currentQuantity}`)
            .fontSize(12)
            .fontColor('#999999')
            .margin({ left: 16 })
          Text(`价格: ¥${asset.purchasePrice || '0.00'}`)
            .fontSize(12)
            .fontColor('#999999')
            .margin({ left: 16 })
        }
        .margin({ top: 8 })

        Row() {
          Text(asset.location || '-')
            .fontSize(12)
            .fontColor('#999999')
            .layoutWeight(1)

          // 状态标签
          Text(this.getStatusText(asset.assetStatus))
            .fontSize(12)
            .fontColor(this.getStatusColor(asset.assetStatus))
            .backgroundColor(this.getStatusBgColor(asset.assetStatus))
            .padding({ left: 8, right: 8, top: 2, bottom: 2 })
            .borderRadius(4)
        }
        .margin({ top: 8 })
      }
      .layoutWeight(1)
      .padding(16)
    }
    .width('100%')
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
    .onClick(() => {
      router.pushUrl({ url: 'pages/asset/AssetDetail', params: { id: asset.id } });
    })
  }

  @Builder
  Pagination() {
    Row() {
      Text(`共 ${this.total} 条`)
        .fontSize(12)
        .fontColor('#999999')
        .layoutWeight(1)

      Button('上一页')
        .height(32)
        .fontSize(12)
        .enabled(this.currentPage > 1)
        .onClick(() => {
          this.currentPage--;
          this.loadAssets();
        })

      Text(`${this.currentPage}/${Math.ceil(this.total / this.pageSize)}`)
        .fontSize(12)
        .margin({ left: 12, right: 12 })

      Button('下一页')
        .height(32)
        .fontSize(12)
        .enabled(this.currentPage < Math.ceil(this.total / this.pageSize))
        .onClick(() => {
          this.currentPage++;
          this.loadAssets();
        })
    }
    .width('100%')
    .height(48)
    .padding({ left: 16, right: 16 })
    .alignItems(VerticalAlign.Center)
    .backgroundColor('#FFFFFF')
  }

  getStatusText(status: string): string {
    const map: Record<string, string> = { '1': '正常', '2': '领用中', '3': '维修中', '4': '报废' };
    return map[status] || '未知';
  }

  getStatusColor(status: string): string {
    const map: Record<string, string> = { '1': '#67C23A', '2': '#409EFF', '3': '#E6A23C', '4': '#F56C6C' };
    return map[status] || '#999999';
  }

  getStatusBgColor(status: string): string {
    const map: Record<string, string> = {
      '1': '#F0F9EB', '2': '#ECF5FF', '3': '#FDF6EC', '4': '#FEF0F0'
    };
    return map[status] || '#F5F7FA';
  }
}