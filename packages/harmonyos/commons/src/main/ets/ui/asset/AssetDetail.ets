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
  spec: string
  brand: string
  manager: string
  remark: string
}

class AssetDetailState {
  asset: Asset | null = null
  loading: boolean = false
}

@Entry
@Component
struct AssetDetail {
  @State state: AssetDetailState = new AssetDetailState()

  aboutToAppear() {
    const params = router.getParams() as Record<string, string>
    if (params && params['assetId']) {
      this.loadAssetDetail(params['assetId'])
    }
  }

  async loadAssetDetail(id: string): Promise<void> {
    this.state.loading = true
    setTimeout(() => {
      const assets: Record<string, Asset> = {
        '1': { id: '1', name: '联想笔记本电脑', code: 'ZC-2024-001', type: '电子设备', status: '使用中', location: 'A栋301', purchaseDate: '2024-01-15', value: 8000, spec: 'ThinkPad X1 Carbon Gen11', brand: '联想', manager: '张三', remark: '日常工作使用' },
        '2': { id: '2', name: 'Dell显示器', code: 'ZC-2024-002', type: '电子设备', status: '使用中', location: 'A栋301', purchaseDate: '2024-01-15', value: 2500, spec: 'U2723DE 27寸4K', brand: 'Dell', manager: '张三', remark: '' },
        '3': { id: '3', name: '办公转椅', code: 'ZC-2023-015', type: '家具', status: '使用中', location: 'A栋302', purchaseDate: '2023-06-20', value: 800, spec: '人体工学椅', brand: 'Herman Miller', manager: '李四', remark: '' },
        '4': { id: '4', name: '投影仪', code: 'ZC-2023-008', type: '电子设备', status: '闲置', location: 'B栋201', purchaseDate: '2023-03-10', value: 5000, spec: 'Epson CB-2255U', brand: 'Epson', manager: '王五', remark: '待维修' },
        '5': { id: '5', name: '别克商务车', code: 'CL-2022-001', type: '车辆', status: '使用中', location: '地下车库', purchaseDate: '2022-11-01', value: 250000, spec: 'GL8 陆尊', brand: '别克', manager: '赵六', remark: '公里数：35000' },
        '6': { id: '6', name: '碎纸机', code: 'ZC-2024-005', type: '办公设备', status: '维修中', location: 'A栋101', purchaseDate: '2024-02-01', value: 1200, spec: '科密金碎王', brand: '科密', manager: '李四', remark: '卡纸维修中' }
      }
      this.state.asset = assets[id] || null
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

  build() {
    Column() {
      // 顶部导航
      Row() {
        Text('<')
          .fontSize(20)
          .fontColor('#333333')
          .onClick(() => router.back())
        Text('资产详情')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#333333')
          .layoutWeight(1)
      }
      .width('100%')
      .height(56)
      .padding({ left: 16, right: 16 })
      .backgroundColor('#FFFFFF')
      .shadow({ radius: 4, color: '#10000000', offsetX: 0, offsetY: 2 })

      if (this.state.loading) {
        Row() {
          Text('加载中...')
            .fontSize(14)
            .fontColor('#999999')
        }
        .width('100%')
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Center)
        .justifyContent(FlexAlign.Center)
      } else if (this.state.asset) {
        Scroll() {
          Column() {
            // 基本信息卡片
            this.buildInfoCard('基本信息', [
              { label: '资产名称', value: this.state.asset.name },
              { label: '资产编号', value: this.state.asset.code },
              { label: '资产类型', value: this.state.asset.type },
              { label: '使用状态', value: this.state.asset.status, color: this.getStatusColor(this.state.asset.status) },
              { label: '存放地点', value: this.state.asset.location }
            ])

            // 规格信息卡片
            this.buildInfoCard('规格信息', [
              { label: '品牌', value: this.state.asset.brand },
              { label: '型号规格', value: this.state.asset.spec },
              { label: '购置日期', value: this.state.asset.purchaseDate },
              { label: '资产价值', value: '¥' + this.state.asset.value.toLocaleString() }
            ])

            // 维护信息卡片
            this.buildInfoCard('维护信息', [
              { label: '负责人', value: this.state.asset.manager },
              { label: '备注', value: this.state.asset.remark || '无' }
            ])

            // 操作按钮
            Row() {
              Button('借用记录')
                .fontSize(14)
                .fontColor('#1890FF')
                .backgroundColor('#E6F0FF')
                .borderRadius(8)
                .height(40)
                .layoutWeight(1)
                .margin({ right: 8 })

              Button('维护记录')
                .fontSize(14)
                .fontColor('#52C41A')
                .backgroundColor('#E6FFED')
                .borderRadius(8)
                .height(40)
                .layoutWeight(1)
            }
            .width('100%')
            .padding(16)
          }
          .padding(16)
        }
        .width('100%')
        .layoutWeight(1)
        .backgroundColor('#F5F5F5')
      } else {
        Row() {
          Text('未找到资产信息')
            .fontSize(14)
            .fontColor('#999999')
        }
        .width('100%')
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Center)
        .justifyContent(FlexAlign.Center)
      }
    }
    .width('100%')
    .height('100%')
  }

  @Builder
  buildInfoCard(title: string, items: { label: string, value: string, color?: string }[]) {
    Column() {
      Text(title)
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .alignSelf(HorizontalAlign.Start)
        .margin({ bottom: 12 })

      Column() {
        ForEach(items, (item: { label: string, value: string, color?: string }, index: number) => {
          Row() {
            Text(item.label)
              .fontSize(14)
              .fontColor('#666666')
              .width(100)
            Text(item.value)
              .fontSize(14)
              .fontColor(item.color || '#333333')
              .layoutWeight(1)
          }
          .width('100%')
          .padding({ top: 8, bottom: 8 })

          if (index < items.length - 1) {
            Divider().color('#F0F0F0')
          }
        })
      }
      .width('100%')
      .backgroundColor('#FFFFFF')
      .borderRadius(12)
      .padding(16)
    }
    .width('100%')
    .margin({ bottom: 12 })
  }
}