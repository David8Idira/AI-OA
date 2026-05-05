/**
 * Asset Scan Page - 资产扫码页面
 */
import router from '@ohos.router';

@Component
export struct AssetScan {
  @State scanResult: string = '';
  @State isScanning: boolean = false;
  @State hasCamera: boolean = false;
  @State manualCode: string = '';
  @State registeredList: Array<{ code: string, name: string, time: string }> = [];
  @State showManualInput: boolean = false;

  aboutToAppear() {
    this.checkCameraSupport();
  }

  checkCameraSupport() {
    // 检查是否支持相机
    this.hasCamera = false; // 模拟
  }

  build() {
    Column() {
      // 顶部导航栏
      this.NavigationBar()

      Column() {
        // 扫码区域
        this.ScanArea()

        // 手动输入切换
        Row() {
          Text('或')
            .fontSize(14)
            .fontColor('#999999')

          Text('手动输入资产编码')
            .fontSize(14)
            .fontColor('#667EEA')
            .onClick(() => {
              this.showManualInput = !this.showManualInput;
            })
        }
        .margin({ top: 16 })

        // 手动输入区域
        if (this.showManualInput) {
          this.ManualInputArea()
        }

        // 已登记列表
        this.RegisteredList()
      }
      .width('100%')
      .layoutWeight(1)
      .padding(16)
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

      Text('出入库扫码登记')
        .fontSize(18)
        .fontWeight(FontWeight.Medium)
        .layoutWeight(1)
        .textAlign(TextAlign.Center)

      Button('重置')
        .fontSize(14)
        .backgroundColor(Color.Transparent)
        .fontColor('#667EEA')
        .onClick(() => {
          this.registeredList = [];
          this.scanResult = '';
        })
    }
    .width('100%')
    .height(56)
    .padding({ left: 16, right: 16 })
    .backgroundColor('#FFFFFF')
    .shadow({ radius: 4, color: '#08000000', offsetX: 0, offsetY: 2 })
  }

  @Builder
  ScanArea() {
    Column() {
      if (this.isScanning) {
        // 扫码中状态
        Column() {
          Image($r('app.media.ic_scan'))
            .width(80)
            .height(80)

          Text('正在扫描...')
            .fontSize(14)
            .fontColor('#999999')
            .margin({ top: 16 })

          Text(this.scanResult)
            .fontSize(16)
            .fontColor('#333333')
            .margin({ top: 8 })
        }
        .width('100%')
        .padding(60)
        .backgroundColor('#FFFFFF')
        .borderRadius(16)
      } else {
        // 默认状态
        Column() {
          Image($r('app.media.ic_scan'))
            .width(64)
            .height(64)
            .opacity(0.5)

          Text('对准资产二维码进行扫描')
            .fontSize(14)
            .fontColor('#999999')
            .margin({ top: 16 })

          Row() {
            Button('拍照扫描')
              .width('45%')
              .height(40)
              .backgroundColor('#667EEA')
              .borderRadius(20)
              .onClick(() => this.startScan())

            Button('切换摄像头')
              .width('45%')
              .height(40)
              .backgroundColor('#FFFFFF')
              .fontColor('#667EEA')
              .border({ width: 1, color: '#667EEA', radius: 20 })
              .margin({ left: 12 })
              .enabled(this.hasCamera)
              .onClick(() => this.switchCamera())
          }
          .margin({ top: 24 })
        }
        .width('100%')
        .padding(40)
        .backgroundColor('#FFFFFF')
        .borderRadius(16)
      }
    }
    .width('100%')
  }

  @Builder
  ManualInputArea() {
    Column() {
      Text('手动输入资产编码')
        .fontSize(14)
        .fontColor('#666666')
        .width('100%')
        .margin({ top: 16 })

      Row() {
        TextInput({ placeholder: '请输入资产编码' })
          .width('70%')
          .height(44)
          .borderRadius(8)
          .backgroundColor('#FFFFFF')
          .onChange((value: string) => {
            this.manualCode = value;
          })

        Button('确认')
          .width('25%')
          .height(44)
          .backgroundColor('#667EEA')
          .borderRadius(8)
          .margin({ left: 12 })
          .onClick(() => this.handleManualSubmit())
      }
      .width('100%')
      .margin({ top: 8 })
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ top: 16 })
  }

  @Builder
  RegisteredList() {
    Column() {
      Row() {
        Text('已登记资产')
          .fontSize(16)
          .fontWeight(FontWeight.Medium)

        Text(`(${this.registeredList.length})`)
          .fontSize(14)
          .fontColor('#999999')
          .margin({ left: 4 })
      }
      .width('100%')
      .margin({ top: 24, bottom: 12 })

      if (this.registeredList.length === 0) {
        Text('暂无登记记录')
          .fontSize(14)
          .fontColor('#999999')
          .width('100%')
          .textAlign(TextAlign.Center)
          .padding(40)
      } else {
        ForEach(this.registeredList, (item: { code: string, name: string, time: string }) => {
          Row() {
            Column() {
              Text(item.code)
                .fontSize(14)
                .fontColor('#333333')
              Text(item.name)
                .fontSize(12)
                .fontColor('#999999')
                .margin({ top: 4 })
            }
            .layoutWeight(1)
            .alignItems(HorizontalAlign.Start)

            Text(item.time)
              .fontSize(12)
              .fontColor('#999999')
          }
          .width('100%')
          .padding(16)
          .backgroundColor('#FFFFFF')
          .borderRadius(8)
          .margin({ bottom: 8 })
        })
      }
    }
    .width('100%')
  }

  startScan() {
    this.isScanning = true;
    // 模拟扫码
    setTimeout(() => {
      this.scanResult = 'AST-2024-001';
      this.isScanning = false;
      this.registerScanResult(this.scanResult);
    }, 2000);
  }

  switchCamera() {
    // 切换摄像头
  }

  handleManualSubmit() {
    if (this.manualCode.trim()) {
      this.registerScanResult(this.manualCode.trim());
      this.manualCode = '';
    }
  }

  registerScanResult(code: string) {
    // 检查是否已存在
    const exists = this.registeredList.some(item => item.code === code);
    if (!exists) {
      this.registeredList.push({
        code: code,
        name: this.getAssetNameByCode(code),
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      });
    }
  }

  getAssetNameByCode(code: string): string {
    // 模拟根据编码获取资产名称
    return '资产-' + code;
  }
}