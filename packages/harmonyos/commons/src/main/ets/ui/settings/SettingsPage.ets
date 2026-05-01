import router from '@ohos.router'

class SettingsState {
  username: string = '用户'
  phone: string = '138****8888'
  email: string = 'user@company.com'
  dept: string = '技术部'
  role: string = '普通员工'
  notifications: boolean = true
  darkMode: boolean = false
  biometric: boolean = false
}

@Entry
@Component
struct SettingsPage {
  @State state: SettingsState = new SettingsState()

  aboutToAppear() {
    this.loadUserInfo()
  }

  async loadUserInfo(): Promise<void> {
    const token = AppStorage.get<string>('auth_token')
    if (token) {
      const userName = AppStorage.get<string>('username')
      if (userName) {
        this.state.username = userName
      }
    }
    // 模拟数据
    this.state.username = AppStorage.get<string>('username') || '张三'
    this.state.phone = '138****8888'
    this.state.email = 'zhangsan@company.com'
    this.state.dept = '技术部'
    this.state.role = '高级工程师'
  }

  private getVersion(): string {
    return 'V2.0.1'
  }

  build() {
    Column() {
      // 顶部导航
      Row() {
        Text('设置')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#333333')
      }
      .width('100%')
      .height(56)
      .padding({ left: 16 })
      .backgroundColor('#FFFFFF')
      .shadow({ radius: 4, color: '#10000000', offsetX: 0, offsetY: 2 })

      Scroll() {
        Column() {
          // 用户信息卡片
          Column() {
            Row() {
              Text('👤')
                .fontSize(40)
                .width(60)
                .height(60)
                .backgroundColor('#F0F5FF')
                .borderRadius(30)
                .textAlign(TextAlign.Center)

              Column() {
                Text(this.state.username)
                  .fontSize(18)
                  .fontWeight(FontWeight.Bold)
                  .fontColor('#333333')

                Text(`${this.state.dept} · ${this.state.role}`)
                  .fontSize(13)
                  .fontColor('#666666')
                  .margin({ top: 4 })

                Text(this.state.phone)
                  .fontSize(12)
                  .fontColor('#999999')
                  .margin({ top: 2 })
              }
              .alignItems(HorizontalAlign.Start)
              .margin({ left: 16 })
              .layoutWeight(1)

              Text('>')
                .fontSize(16)
                .fontColor('#CCCCCC')
            }
            .width('100%')
            .padding(16)
          }
          .width('100%')
          .backgroundColor('#FFFFFF')
          .borderRadius(12)
          .margin({ bottom: 16 })

          // 消息通知
          this.buildSettingsSection('消息通知', [
            { icon: '🔔', title: '推送通知', subtitle: '接收系统通知和消息', type: 'switch', value: this.state.notifications, onChange: (val: boolean) => this.state.notifications = val }
          ])

          // 偏好设置
          this.buildSettingsSection('偏好设置', [
            { icon: '🌙', title: '深色模式', subtitle: '开启后界面将变为深色', type: 'switch', value: this.state.darkMode, onChange: (val: boolean) => this.state.darkMode = val },
            { icon: '🔐', title: '生物识别', subtitle: '使用指纹或面容解锁', type: 'switch', value: this.state.biometric, onChange: (val: boolean) => this.state.biometric = val }
          ])

          // 通用设置
          this.buildSettingsSection('通用设置', [
            { icon: '🌐', title: '语言', subtitle: '简体中文', type: 'arrow' },
            { icon: '📱', title: '清除缓存', subtitle: '当前缓存 128MB', type: 'arrow' },
            { icon: '📴', title: '离线模式', subtitle: '下载数据供离线使用', type: 'arrow' }
          ])

          // 关于我们
          this.buildSettingsSection('关于我们', [
            { icon: 'ℹ️', title: '关于AI-OA', subtitle: '了解产品更多信息', type: 'arrow' },
            { icon: '📖', title: '用户协议', subtitle: '查看软件许可协议', type: 'arrow' },
            { icon: '🔒', title: '隐私政策', subtitle: '了解我们如何保护隐私', type: 'arrow' },
            { icon: '⭐', title: '给我们评分', subtitle: '前往应用商店评分', type: 'arrow' }
          ])

          // 退出登录
          Button('退出登录')
            .fontSize(16)
            .fontColor('#FF4D4F')
            .backgroundColor('#FFFFFF')
            .borderRadius(12)
            .width('100%')
            .height(50)
            .borderWidth(1)
            .borderColor('#FF4D4F')
            .margin({ top: 16 })

          // 版本信息
          Text(`当前版本 ${this.getVersion()}`)
            .fontSize(12)
            .fontColor('#CCCCCC')
            .margin({ top: 16, bottom: 32 })
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
  buildSettingsSection(title: string, items: { icon: string, title: string, subtitle: string, type: string, value?: boolean, onChange?: (val: boolean) => void }[]) {
    Column() {
      Text(title)
        .fontSize(14)
        .fontWeight(FontWeight.Medium)
        .fontColor('#666666')
        .alignSelf(HorizontalAlign.Start)
        .margin({ bottom: 8, left: 4 })

      Column() {
        ForEach(items, (item: { icon: string, title: string, subtitle: string, type: string, value?: boolean, onChange?: (val: boolean) => void }, index: number) => {
          Row() {
            Text(item.icon)
              .fontSize(20)
              .width(32)

            Column() {
              Text(item.title)
                .fontSize(15)
                .fontColor('#333333')

              Text(item.subtitle)
                .fontSize(12)
                .fontColor('#999999')
                .margin({ top: 2 })
            }
            .alignItems(HorizontalAlign.Start)
            .layoutWeight(1)
            .margin({ left: 8 })

            if (item.type === 'switch') {
              Toggle({ type: ToggleType.Switch, isOn: item.value })
                .selectedColor('#1890FF')
                .onChange((isOn: boolean) => {
                  if (item.onChange) {
                    item.onChange(isOn)
                  }
                })
            } else if (item.type === 'arrow') {
              Text('>')
                .fontSize(16)
                .fontColor('#CCCCCC')
            }
          }
          .width('100%')
          .padding(12)

          if (index < items.length - 1) {
            Divider().color('#F0F0F0')
          }
        })
      }
      .width('100%')
      .backgroundColor('#FFFFFF')
      .borderRadius(12)
      .padding(4)
    }
    .width('100%')
    .margin({ bottom: 16 })
  }
}