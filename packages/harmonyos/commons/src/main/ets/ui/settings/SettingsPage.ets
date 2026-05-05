/**
 * Settings Page - 设置页面
 */
import router from '@ohos.router';
import { UserInfo } from '../../model/user/UserModel';

@Component
export struct SettingsPage {
  @State userInfo: UserInfo | null = null;
  @State notificationEnabled: boolean = true;
  @State soundEnabled: boolean = true;
  @State vibrationEnabled: boolean = true;
  @State activeTab: string = 'profile';

  private aboutToAppear() {
    this.loadUserInfo();
  }

  loadUserInfo() {
    // 模拟加载用户信息
    this.userInfo = {
      id: '1',
      name: '张三',
      phone: '13800138001',
      email: 'zhangsan@company.com',
      avatar: '',
      department: '技术部',
      position: '高级工程师'
    };
  }

  build() {
    Column() {
      // 顶部导航栏
      this.NavigationBar()

      Scroll() {
        Column() {
          // 用户信息卡片
          this.UserCard()

          // 设置选项卡
          this.SettingsTabs()

          // 设置内容
          if (this.activeTab === 'profile') {
            this.ProfileSettings()
          } else if (this.activeTab === 'notification') {
            this.NotificationSettings()
          } else {
            this.SystemSettings()
          }
        }
        .width('100%')
        .padding(16)
      }
      .layoutWeight(1)
      .scrollBar(BarState.Auto)
      .edgeEffect(EdgeEffect.Spring)

      // 退出登录按钮
      this.LogoutButton()
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F7FA')
  }

  @Builder
  NavigationBar() {
    Row() {
      Text('设置')
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
  UserCard() {
    Row() {
      // 头像
      Column() {
        if (this.userInfo?.avatar) {
          Image(this.userInfo.avatar)
            .width(64)
            .height(64)
            .borderRadius(32)
        } else {
          Text(this.userInfo?.name?.substring(0, 1) || '?')
            .fontSize(24)
            .fontWeight(FontWeight.Bold)
            .fontColor('#FFFFFF')
            .width(64)
            .height(64)
            .backgroundColor('#667EEA')
            .borderRadius(32)
            .textAlign(TextAlign.Center)
        }
      }
      .width(80)
      .alignItems(HorizontalAlign.Center)

      Column() {
        Text(this.userInfo?.name || '')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#333333')

        Text(this.userInfo?.department + ' - ' + this.userInfo?.position)
          .fontSize(12)
          .fontColor('#999999')
          .margin({ top: 4 })

        Text(this.userInfo?.phone || '')
          .fontSize(12)
          .fontColor('#999999')
          .margin({ top: 4 })
      }
      .layoutWeight(1)
      .padding({ left: 16 })
      .alignItems(HorizontalAlign.Start)

      Image($r('app.media.ic_arrow_right'))
        .width(20)
        .height(20)
        .opacity(0.5)
        .onClick(() => {
          // 编辑个人资料
        })
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 16 })
  }

  @Builder
  SettingsTabs() {
    Row() {
      this.TabItem('profile', '个人资料', this.activeTab === 'profile')
      this.TabItem('notification', '通知设置', this.activeTab === 'notification')
      this.TabItem('system', '系统', this.activeTab === 'system')
    }
    .width('100%')
    .height(44)
    .backgroundColor('#FFFFFF')
    .borderRadius(8)
    .margin({ bottom: 16 })
  }

  @Builder
  TabItem(key: string, label: string, isActive: boolean) {
    Text(label)
      .fontSize(14)
      .fontColor(isActive ? '#FFFFFF' : '#666666')
      .backgroundColor(isActive ? '#667EEA' : 'transparent')
      .borderRadius(8)
      .padding({ left: 20, right: 20, top: 8, bottom: 8 })
      .onClick(() => {
        this.activeTab = key;
      })
  }

  @Builder
  ProfileSettings() {
    Column() {
      // 基本信息
      this.SettingsSection('基本信息', [
        { icon: 'ic_user', label: '姓名', value: this.userInfo?.name || '-', action: 'edit' },
        { icon: 'ic_phone', label: '手机号', value: this.userInfo?.phone || '-', action: 'edit' },
        { icon: 'ic_email', label: '邮箱', value: this.userInfo?.email || '-', action: 'edit' },
        { icon: 'ic_department', label: '部门', value: this.userInfo?.department || '-', action: 'view' },
        { icon: 'ic_position', label: '职位', value: this.userInfo?.position || '-', action: 'view' }
      ])

      // 安全设置
      this.SettingsSection('安全设置', [
        { icon: 'ic_password', label: '修改密码', value: '', action: 'navigate' },
        { icon: 'ic_verify', label: '身份验证', value: '', action: 'navigate' },
        { icon: 'ic_device', label: '设备管理', value: '2台设备', action: 'navigate' }
      ])

      // 隐私设置
      this.SettingsSection('隐私设置', [
        { icon: 'ic_visibility', label: '个人资料可见性', value: '仅同事', action: 'select' },
        { icon: 'ic_block', label: '黑名单', value: '', action: 'navigate' }
      ])
    }
    .width('100%')
  }

  @Builder
  NotificationSettings() {
    Column() {
      // 消息通知
      this.SettingsSection('消息通知', [
        { icon: 'ic_bell', label: '接收新消息通知', value: '', action: 'switch', switchValue: this.notificationEnabled, onChange: (val: boolean) => { this.notificationEnabled = val; } },
        { icon: 'ic_approval', label: '审批通知', value: '', action: 'switch', switchValue: true, onChange: (val: boolean) => {} },
        { icon: 'ic_mention', label: '@提及通知', value: '', action: 'switch', switchValue: true, onChange: (val: boolean) => {} }
      ])

      // 声音与震动
      this.SettingsSection('声音与震动', [
        { icon: 'ic_sound', label: '消息声音', value: '', action: 'switch', switchValue: this.soundEnabled, onChange: (val: boolean) => { this.soundEnabled = val; } },
        { icon: 'ic_vibration', label: '消息震动', value: '', action: 'switch', switchValue: this.vibrationEnabled, onChange: (val: boolean) => { this.vibrationEnabled = val; } }
      ])

      // 免打扰
      this.SettingsSection('免打扰', [
        { icon: 'ic_dnd', label: '定时免打扰', value: '关闭', action: 'select' },
        { icon: 'ic_away', label: '离开时自动回复', value: '关闭', action: 'select' }
      ])
    }
    .width('100%')
  }

  @Builder
  SystemSettings() {
    Column() {
      // 通用设置
      this.SettingsSection('通用', [
        { icon: 'ic_language', label: '语言', value: '简体中文', action: 'select' },
        { icon: 'ic_theme', label: '主题', value: '跟随系统', action: 'select' },
        { icon: 'ic_font', label: '字体大小', value: '标准', action: 'select' }
      ])

      // 存储与数据
      this.SettingsSection('存储与数据', [
        { icon: 'ic_cache', label: '清除缓存', value: '126MB', action: 'action' },
        { icon: 'ic_download', label: '下载管理', value: '', action: 'navigate' },
        { icon: 'ic_backup', label: '数据备份', value: '', action: 'navigate' }
      ])

      // 关于
      this.SettingsSection('关于', [
        { icon: 'ic_info', label: '版本信息', value: 'v1.0.0', action: 'view' },
        { icon: 'ic_update', label: '检查更新', value: '', action: 'action' },
        { icon: 'ic_agreement', label: '用户协议', value: '', action: 'navigate' },
        { icon: 'ic_privacy', label: '隐私政策', value: '', action: 'navigate' }
      ])
    }
    .width('100%')
  }

  @Builder
  SettingsSection(title: string, items: any[]) {
    Column() {
      Text(title)
        .fontSize(12)
        .fontColor('#999999')
        .width('100%')
        .padding({ left: 4, bottom: 8 })

      Column() {
        ForEach(items, (item: any, index: number) => {
          Row() {
            Image($r(`app.media.${item.icon}`))
              .width(20)
              .height(20)
              .margin({ right: 12 })
              .opacity(0.6)

            Text(item.label)
              .fontSize(14)
              .fontColor('#333333')
              .layoutWeight(1)

            if (item.value) {
              Text(item.value)
                .fontSize(12)
                .fontColor('#999999')
                .margin({ right: 8 })
            }

            Image($r('app.media.ic_arrow_right'))
              .width(16)
              .height(16)
              .opacity(0.5)
          }
          .width('100%')
          .padding({ top: 14, bottom: 14 })
          .onClick(() => {
            // 处理设置项点击
          })

          if (index < items.length - 1) {
            Divider()
              .strokeWidth(0.5)
              .color('#F0F0F0')
          }
        })
      }
      .width('100%')
      .padding({ left: 12, right: 12 })
      .backgroundColor('#FFFFFF')
      .borderRadius(12)
    }
    .width('100%')
    .margin({ bottom: 16 })
  }

  @Builder
  LogoutButton() {
    Button('退出登录')
      .width('90%')
      .height(48)
      .backgroundColor('#FFFFFF')
      .fontColor('#F56C6C')
      .borderRadius(24)
      .border({ width: 1, color: '#F56C6C' })
      .margin({ bottom: 16 })
      .onClick(() => {
        // 退出登录
        router.clear();
        // 跳转到登录页
      })
  }
}