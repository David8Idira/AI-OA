import router from '@ohos.router'
import { ApiClient } from '../commons/src/main/ets/data/api/ApiClient'

// 应用入口
@Entry
@Component
struct App {
  aboutToAppear() {
    // 初始化API客户端
    ApiClient.init({
      baseUrl: 'https://api.example.com',
      timeout: 30000
    })
  }

  onPageShow() {
    // 检查登录状态
    const token = AppStorage.get<string>('auth_token')
    if (!token) {
      router.replaceUrl({ url: 'pages/Login' })
    }
  }

  build() {
    Column() {
      router.replaceUrl({ url: 'pages/Main' })
    }
  }
}
