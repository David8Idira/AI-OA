import router from '@ohos.router'
import { ApiClient } from '../../data/api/ApiClient'
import { LoginParams, LoginResponse } from '../../data/model/User'

// 登录页面状态
@State
class LoginState {
  username: string = ''
  password: string = ''
  rememberMe: boolean = false
  loading: boolean = false
  errorMessage: string = ''
}

@Entry
@Component
struct LoginPage {
  @State state: LoginState = new LoginState()

  build() {
    Column() {
      // Logo区域
      Column() {
        Text('AI-OA')
          .fontSize(36)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1890FF')
        
        Text('智能办公平台')
          .fontSize(16)
          .fontColor('#666666')
          .margin({ top: 8 })
      }
      .width('100%')
      .padding({ top: 60, bottom: 40 })

      // 表单区域
      Column() {
        // 用户名输入
        TextInput({ placeholder: '请输入用户名' })
          .width('100%')
          .height(48)
          .borderRadius(8)
          .backgroundColor('#F5F5F5')
          .onChange((value: string) => {
            this.state.username = value
          })

        Divider()
          .width('100%')
          .margin({ top: 16, bottom: 16 })

        // 密码输入
        TextInput({ placeholder: '请输入密码' })
          .width('100%')
          .height(48)
          .type(InputType.Password)
          .borderRadius(8)
          .backgroundColor('#F5F5F5')
          .onChange((value: string) => {
            this.state.password = value
          })

        // 记住密码 & 忘记密码
        Row() {
          Checkbox()
            .select(this.state.rememberMe)
            .onChange((value: boolean) => {
              this.state.rememberMe = value
            })
          
          Text('记住密码')
            .fontSize(14)
            .fontColor('#666666')
            .margin({ left: 8 })
          
          Blank()
          
          Text('忘记密码？')
            .fontSize(14)
            .fontColor('#1890FF')
        }
        .width('100%')
        .margin({ top: 16 })
      }
      .width('100%')
      .padding({ left: 24, right: 24 })

      // 错误提示
      if (this.state.errorMessage) {
        Text(this.state.errorMessage)
          .fontSize(12)
          .fontColor('#FF4D4F')
          .padding({ left: 24, right: 24, top: 16 })
      }

      // 登录按钮
      Button('登 录')
        .width('90%')
        .height(48)
        .fontSize(16)
        .fontColor('#FFFFFF')
        .backgroundColor('#1890FF')
        .borderRadius(24)
        .margin({ top: 32 })
        .enabled(!this.state.loading)
        .onClick(() => this.handleLogin())

      // 注册提示
      Row() {
        Text('还没有账号？')
          .fontSize(14)
          .fontColor('#666666')
        
        Text('立即注册')
          .fontSize(14)
          .fontColor('#1890FF')
          .onClick(() => router.pushUrl({ url: 'pages/Register' }))
      }
      .margin({ top: 24 })
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#FFFFFF')
  }

  // 处理登录
  async handleLogin(): Promise<void> {
    // 校验输入
    if (!this.state.username.trim()) {
      this.state.errorMessage = '请输入用户名'
      return
    }
    if (!this.state.password.trim()) {
      this.state.errorMessage = '请输入密码'
      return
    }

    this.state.loading = true
    this.state.errorMessage = ''

    try {
      const params: LoginParams = {
        username: this.state.username,
        password: this.state.password,
        rememberMe: this.state.rememberMe
      }

      const response = await ApiClient.post<LoginResponse>('/api/auth/login', params)
      
      // 保存Token和用户信息
      AppStorage.setOrCreate('auth_token', response.data.token)
      AppStorage.setOrCreate('user_id', response.data.user.id)
      AppStorage.setOrCreate('username', response.data.user.name)
      
      // 跳转到主页
      router.replaceUrl({ url: 'pages/Main' })
    } catch (error) {
      this.state.errorMessage = error instanceof Error ? error.message : '登录失败，请重试'
    } finally {
      this.state.loading = false
    }
  }
}
