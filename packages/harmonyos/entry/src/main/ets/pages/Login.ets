import router from '@ohos.router'

// AI-OA 登录页面
@Entry
@Component
struct Login {
  @State username: string = ''
  @State password: string = ''
  @State isLoading: boolean = false

  build() {
    Column() {
      // Logo 区域
      Column() {
        Text('🤖')
          .fontSize(60)
          .margin({ top: 80 })
        
        Text('AI-OA 智能办公')
          .fontSize(24)
          .fontWeight(FontWeight.Bold)
          .margin({ top: 20 })
        
        Text('让办公更智能、更高效')
          .fontSize(14)
          .fontColor('#999999')
          .margin({ top: 8 })
      }
      .width('100%')
      .height(200)
      .justifyContent(FlexAlign.Center)

      // 登录表单
      Column() {
        TextInput({ placeholder: '请输入用户名' })
          .width('80%')
          .height(48)
          .margin({ top: 40 })
          .borderRadius(8)
          .onChange((value: string) => {
            this.username = value
          })

        TextInput({ placeholder: '请输入密码', type: InputType.Password })
          .width('80%')
          .height(48)
          .margin({ top: 16 })
          .borderRadius(8)
          .onChange((value: string) => {
            this.password = value
          })

        Button('登录')
          .width('80%')
          .height(48)
          .margin({ top: 32 })
          .borderRadius(8)
          .backgroundColor('#1890FF')
          .enabled(!this.isLoading)
          .onClick(() => {
            this.doLogin()
          })

        if (this.isLoading) {
          Text('登录中...')
            .fontSize(14)
            .fontColor('#999999')
            .margin({ top: 16 })
        }
      }
      .width('100%')
      .layoutWeight(1)

      // 注册入口
      Row() {
        Text('还没有账号？')
          .fontSize(14)
          .fontColor('#999999')
        Text('立即注册')
          .fontSize(14)
          .fontColor('#1890FF')
          .onClick(() => {
            // TODO: 跳转注册
          })
      }
      .margin({ bottom: 40 })
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#FFFFFF')
  }

  private doLogin() {
    if (!this.username || !this.password) {
      // 提示输入
      return
    }
    
    this.isLoading = true
    
    // 模拟登录
    setTimeout(() => {
      // 保存 token
      AppStorage.setOrCreate('auth_token', 'mock_token_' + this.username)
      AppStorage.setOrCreate('username', this.username)
      
      // 跳转到主页
      router.replaceUrl({ url: 'pages/Main' })
      
      this.isLoading = false
    }, 1000)
  }
}
