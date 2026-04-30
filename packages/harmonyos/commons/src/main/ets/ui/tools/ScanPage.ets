// 扫一扫页面
import router from '@ohos.router'
import promptAction from '@ohos.promptAction'

// 页面状态
class ScanPageState {
  hasPermission: boolean = false
  flashOn: boolean = false
  scanHistory: ScanHistoryItem[] = []
}

// 扫描历史记录
interface ScanHistoryItem {
  id: string
  content: string
  type: 'text' | 'url' | 'contact' | 'product'
  time: number
}

@Entry
@Component
struct ScanPage {
  @State state: ScanPageState = new ScanPageState()

  aboutToAppear() {
    // 模拟扫码记录
    const now = Date.now()
    this.state.scanHistory = [
      { id: '1', content: 'https://example.com', type: 'url', time: now - 86400000 },
      { id: '2', content: 'M123456789', type: 'product', time: now - 172800000 }
    ]

    // 模拟权限请求
    this.checkPermission()
  }

  // 检查权限
  private checkPermission(): void {
    // 模拟权限已授予
    this.state.hasPermission = true
  }

  // 切换闪光灯
  private toggleFlash(): void {
    this.state.flashOn = !this.state.flashOn
    promptAction.showToast({ message: this.state.flashOn ? '闪光灯已开启' : '闪光灯已关闭' })
  }

  // 处理扫描结果
  private handleScanResult(result: string): void {
    // 解析扫描内容
    if (result.startsWith('http://') || result.startsWith('https://')) {
      promptAction.showToast({ message: '识别到链接: ' + result })
      // 可以打开链接或显示详情
    } else if (/^[A-Z0-9]+$/.test(result)) {
      promptAction.showToast({ message: '识别到编码: ' + result })
    } else {
      promptAction.showToast({ message: '扫描成功' })
    }

    // 添加到历史记录
    const newItem: ScanHistoryItem = {
      id: Date.now().toString(),
      content: result,
      type: result.startsWith('http') ? 'url' : 'text',
      time: Date.now()
    }
    this.state.scanHistory.unshift(newItem)
  }

  // 获取类型图标
  private getTypeIcon(type: string): string {
    switch (type) {
      case 'url': return '🔗'
      case 'contact': return '👤'
      case 'product': return '📦'
      default: return '📄'
    }
  }

  // 格式化时间
  private formatTime(timestamp: number): string {
    const now = Date.now()
    const diff = now - timestamp
    const day = 86400000

    if (diff < 60000) return '刚刚'
    if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
    if (diff < day) return Math.floor(diff / 3600000) + '小时前'
    if (diff < 7 * day) return Math.floor(diff / day) + '天前'
    return new Date(timestamp).toLocaleDateString('zh-CN')
  }

  build() {
    Column() {
      // 顶部导航栏
      this.buildNavBar()

      // 扫码区域
      Column() {
        // 模拟扫码框
        Stack() {
          // 背景
          Column()
            .width('100%')
            .height('100%')
            .backgroundColor('#000000')

          // 扫码框
          Column() {
            // 四个角的装饰
            Row() {
              Column().width(24).height(24).border({
                width: { left: 3, top: 3 },
                color: '#1890FF',
                radius: { topLeft: 8 }
              })
              Blank().layoutWeight(1)
              Column().width(24).height(24).border({
                width: { right: 3, top: 3 },
                color: '#1890FF',
                radius: { topRight: 8 }
              })
            }
            .width('100%')

            Blank().layoutWeight(1)

            Row() {
              Column().width(24).height(24).border({
                width: { left: 3, bottom: 3 },
                color: '#1890FF',
                radius: { bottomLeft: 8 }
              })
              Blank().layoutWeight(1)
              Column().width(24).height(24).border({
                width: { right: 3, bottom: 3 },
                color: '#1890FF',
                radius: { bottomRight: 8 }
              })
            }
            .width('100%')
          }
          .width(250)
          .height(250)
          .backgroundColor('#1A000000')
        }
        .width('100%')
        .aspectRatio(1)
        .backgroundColor('#000000')

        // 操作按钮
        Row() {
          // 闪光灯
          Column() {
            Text(this.state.flashOn ? '💡' : '🔦')
              .fontSize(28)

            Text('闪光灯')
              .fontSize(12)
              .fontColor('#FFFFFF')
              .margin({ top: 4 })
          }
          .onClick(() => this.toggleFlash())

          // 相册
          Column() {
            Text('🖼️')
              .fontSize(28)

            Text('相册')
              .fontSize(12)
              .fontColor('#FFFFFF')
              .margin({ top: 4 })
          }
          .margin({ left: 40 })
          .onClick(() => {
            promptAction.showToast({ message: '从相册选择' })
          })
        }
        .margin({ top: 32 })

        Text('将二维码/条码放入框内即可自动扫描')
          .fontSize(13)
          .fontColor('#999999')
          .margin({ top: 24 })
      }
      .width('100%')
      .backgroundColor('#000000')

      // 扫描历史
      Column() {
        Row() {
          Text('扫描历史')
            .fontSize(16)
            .fontWeight(FontWeight.Medium)
            .fontColor('#333333')

          Blank()

          Text('清空')
            .fontSize(13)
            .fontColor('#FF4D4F')
            .onClick(() => {
              this.state.scanHistory = []
            })
        }
        .width('100%')
        .margin({ bottom: 12 })

        if (this.state.scanHistory.length === 0) {
          Text('暂无扫描记录')
            .fontSize(14)
            .fontColor('#999999')
            .padding(20)
            .width('100%')
            .textAlign(TextAlign.Center)
        } else {
          List() {
            ForEach(this.state.scanHistory, (item: ScanHistoryItem) => {
              ListItem() {
                Row() {
                  Text(this.getTypeIcon(item.type))
                    .fontSize(20)
                    .margin({ right: 12 })

                  Column() {
                    Text(item.content)
                      .fontSize(14)
                      .fontColor('#333333')
                      .maxLines(1)
                      .textOverflow({ overflow: TextOverflow.Ellipsis })

                    Text(this.formatTime(item.time))
                      .fontSize(12)
                      .fontColor('#999999')
                      .margin({ top: 4 })
                  }
                  .alignItems(HorizontalAlign.Start)
                  .layoutWeight(1)
                }
                .width('100%')
                .padding(12)
              }
            })
          }
          .width('100%')
          .height(200)
        }
      }
      .width('100%')
      .padding(16)
      .backgroundColor('#FFFFFF')
      .layoutWeight(1)
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#000000')
  }

  @Builder
  buildNavBar() {
    Row() {
      Image($r('app.media.icon_back'))
        .width(24)
        .height(24)
        .onClick(() => router.back())

      Text('扫一扫')
        .fontSize(18)
        .fontWeight(FontWeight.Medium)
        .fontColor('#FFFFFF')
        .margin({ left: 16 })
        .layoutWeight(1)
    }
    .width('100%')
    .padding({ left: 16, right: 16, top: 12, bottom: 12 })
    .backgroundColor('#000000')
  }
}
