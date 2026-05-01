import router from '@ohos.router'

interface Asset {
  id: string
  name: string
  code: string
  type: string
  status: string
  location: string
  purchaseDate: string
  value: number
}

class AssetListState {
  assets: Asset[] = []
  loading: boolean = false
  searchText: string = ''
  selectedType: string = '全部'
}

@Entry
@Component
struct AssetList {
  @State state: AssetListState = new AssetListState()
  private scrollController: Scroller = new Scroller()

  private types: string[] = ['全部', '办公设备', '电子设备', '家具', '车辆', '其他']

  aboutToAppear() {
    this.loadAssets()
  }

  async loadAssets(): Promise<void> {
    this.state.loading = true
    setTimeout(() => {
      this.state.assets = [
        { id: '1', name: '联想笔记本电脑', code: 'ZC-2024-001', type: '电子设备', status: '使用中', location: 'A栋301', purchaseDate: '2024-01-15', value: 8000 },
        { id: '2', name: 'Dell显示器', code: 'ZC-2024-002', type: '电子设备', status: '使用中', location: 'A栋301', purchaseDate: '2024-01-15', value: 2500 },
        { id: '3', name: '办公转椅', code: 'ZC-2023-015', type: '家具', status: '使用中', location: 'A栋302', purchaseDate: '2023-06-20', value: 800 },
        { id: '4', name: '投影仪', code: 'ZC-2023-008', type: '电子设备', status: '闲置', location: 'B栋201', purchaseDate: '2023-03-10', value: 5000 },
        { id: '5', name: '别克商务车', code: 'CL-2022-001', type: '车辆', status: '使用中', location: '地下车库', purchaseDate: '2022-11-01', value: 250000 },
        { id: '6', name: '碎纸机', code: 'ZC-2024-005', type: '办公设备', status: '维修中', location: 'A栋101', purchaseDate: '2024-02-01', value: 1200 }
      ]
      this.state.loading = false
    }, 500)
  }

  private getStatusColor(status: string): string {
    switch (status) {
      case '使用中': return '#52C41A'
      case '闲置': return '#FAAD14'
      case '维修中': return '#FF4D4F'
      default: return '#999999'
    }
  }

  private getFilteredAssets(): Asset[] {
    return this.state.assets.filter(asset => {
      const matchType = this.state.selectedType === '全部' || asset.type === this.state.selectedType
      const matchSearch = asset.name.includes(this.state.searchText) || asset.code.includes(this.state.searchText)
      return matchType && matchSearch
    })
  }

  build() {
    Column() {
      // 顶部导航
      Row() {
        Text('资产管理')
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
        TextInput({ placeholder: '搜索资产名称/编号' })
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

      // 类型筛选
      Scroll() {
        Row() {
          ForEach(this.types, (type: string) => {
            Text(type)
              .fontSize(13)
              .fontColor(this.state.selectedType === type ? '#FFFFFF' : '#666666')
              .backgroundColor(this.state.selectedType === type ? '#1890FF' : '#E6E6E6')
              .borderRadius(16)
              .padding({ left: 16, right: 16, top: 6, bottom: 6 })
              .margin({ right: 8 })
              .onClick(() => {
                this.state.selectedType = type
              })
          })
        }
      }
      .width('100%')
      .padding({ left: 16, right: 16 })
      .scrollable(ScrollDirection.Horizontal)
      .scrollBar(BarState.Off)

      // 资产列表
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
            ForEach(this.getFilteredAssets(), (asset: Asset) => {
              this.buildAssetItem(asset)
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
  buildAssetItem(asset: Asset) {
    Row() {
      Column() {
        Row() {
          Text(asset.name)
            .fontSize(16)
            .fontWeight(FontWeight.Medium)
            .fontColor('#333333')

          Text(asset.status)
            .fontSize(11)
            .fontColor('#FFFFFF')
            .backgroundColor(this.getStatusColor(asset.status))
            .borderRadius(8)
            .padding({ left: 6, right: 6, top: 2, bottom: 2 })
            .margin({ left: 8 })
        }

        Text(`编号：${asset.code}`)
          .fontSize(12)
          .fontColor('#666666')
          .margin({ top: 4 })

        Row() {
          Text(`类型：${asset.type}`)
            .fontSize(12)
            .fontColor('#999999')
          Text(` | 位置：${asset.location}`)
            .fontSize(12)
            .fontColor('#999999')
        }
        .margin({ top: 2 })

        Text(`购置日期：${asset.purchaseDate} | 价值：¥${asset.value.toLocaleString()}`)
          .fontSize(12)
          .fontColor('#999999')
          .margin({ top: 2 })
      }
      .alignItems(HorizontalAlign.Start)
      .layoutWeight(1)

      Text('>')
        .fontSize(16)
        .fontColor('#CCCCCC')
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
    .onClick(() => {
      router.pushUrl({ url: 'pages/AssetDetail', params: { assetId: asset.id } })
    })
  }
}