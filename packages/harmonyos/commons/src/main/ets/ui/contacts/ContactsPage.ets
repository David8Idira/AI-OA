// 通讯录页面
import router from '@ohos.router'

// 联系人模型
interface Contact {
  id: string
  name: string
  avatar?: string
  position?: string
  department?: string
  phone?: string
}

// 部门模型
interface Department {
  id: string
  name: string
  memberCount: number
  avatar?: string
}

// 页面状态
class ContactsPageState {
  contacts: Contact[] = []
  departments: Department[] = []
  starredContacts: Contact[] = []
  searchKeyword: string = ''
  loading: boolean = false
  currentTabIndex: number = 0
}

@Entry
@Component
struct ContactsPage {
  @State state: ContactsPageState = new ContactsPageState()

  aboutToAppear() {
    this.loadData()
  }

  // 加载数据
  async loadData(): Promise<void> {
    this.state.loading = true

    // 模拟联系人数据
    this.state.contacts = [
      { id: '1', name: '张三', position: '产品经理', department: '产品部', phone: '138****0001' },
      { id: '2', name: '李四', position: '高级工程师', department: '研发部', phone: '138****0002' },
      { id: '3', name: '王五', position: 'UI设计师', department: '设计部', phone: '138****0003' },
      { id: '4', name: '赵六', position: '测试工程师', department: '测试部', phone: '138****0004' },
      { id: '5', name: '钱七', position: '项目经理', department: '项目部', phone: '138****0005' },
      { id: '6', name: '孙八', position: '前端工程师', department: '研发部', phone: '138****0006' },
      { id: '7', name: '周九', position: '后端工程师', department: '研发部', phone: '138****0007' },
      { id: '8', name: '吴十', position: '运维工程师', department: '运维部', phone: '138****0008' }
    ]

    // 模拟部门数据
    this.state.departments = [
      { id: 'd1', name: '总经理办公室', memberCount: 3, avatar: '🏢' },
      { id: 'd2', name: '产品部', memberCount: 15, avatar: '📱' },
      { id: 'd3', name: '研发部', memberCount: 50, avatar: '💻' },
      { id: 'd4', name: '设计部', memberCount: 12, avatar: '🎨' },
      { id: 'd5', name: '测试部', memberCount: 18, avatar: '🧪' },
      { id: 'd6', name: '运维部', memberCount: 8, avatar: '🔧' },
      { id: 'd7', name: '市场部', memberCount: 20, avatar: '📢' },
      { id: 'd8', name: '人力资源部', memberCount: 10, avatar: '👥' }
    ]

    // 模拟收藏联系人
    this.state.starredContacts = this.state.contacts.slice(0, 3)

    this.state.loading = false
  }

  build() {
    Column() {
      // 顶部导航栏
      this.buildNavBar()

      // Tab切换
      Tabs({ barPosition: BarPosition.Start }) {
        TabContent() {
          this.buildContactTab()
        }
        .tabBar('联系人')

        TabContent() {
          this.buildDepartmentTab()
        }
        .tabBar('组织架构')

        TabContent() {
          this.buildStarredTab()
        }
        .tabBar('收藏')
      }
      .barHeight(44)
      .onChange((index: number) => {
        this.state.currentTabIndex = index
      })
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }

  @Builder
  buildNavBar() {
    Row() {
      Text('通讯录')
        .fontSize(20)
        .fontWeight(FontWeight.Bold)
        .fontColor('#333333')

      Blank()

      Image($r('app.media.icon_add'))
        .width(24)
        .height(24)
    }
    .width('100%')
    .padding({ left: 16, right: 16, top: 12, bottom: 12 })
    .backgroundColor('#FFFFFF')
  }

  // 联系人Tab
  @Builder
  buildContactTab() {
    Column() {
      // 搜索栏
      Row() {
        Image($r('app.media.icon_search'))
          .width(16)
          .height(16)
          .margin({ left: 12 })
          .opacity(0.5)

        Text('搜索联系人')
          .fontSize(14)
          .fontColor('#999999')
          .margin({ left: 8 })
      }
      .width('100%')
      .height(40)
      .backgroundColor('#F5F5F5')
      .borderRadius(8)
      .margin(16)

      // 字母索引
      List() {
        ForEach(this.state.contacts, (contact: Contact) => {
          ListItem() {
            Row() {
              // 头像
              Stack() {
                Column() {
                  Text(contact.name.charAt(0))
                    .fontSize(18)
                    .fontColor('#FFFFFF')
                }
                .width(44)
                .height(44)
                .borderRadius(8)
                .backgroundColor('#1890FF')
                .justifyContent(FlexAlign.Center)

                // 星标
                Text('⭐')
                  .fontSize(10)
                  .position({ x: 32, y: -4 })
              }

              // 信息
              Column() {
                Text(contact.name)
                  .fontSize(16)
                  .fontColor('#333333')

                Text(contact.position + ' · ' + contact.department)
                  .fontSize(13)
                  .fontColor('#999999')
                  .margin({ top: 4 })
              }
              .alignItems(HorizontalAlign.Start)
              .margin({ left: 12 })
              .layoutWeight(1)

              // 操作
              Row() {
                Text('📞')
                  .fontSize(20)
                  .padding(8)
                  .onClick(() => {
                    // 打电话
                  })

                Text('💬')
                  .fontSize(20)
                  .padding(8)
                  .onClick(() => {
                    router.pushUrl({ url: 'pages/ChatDetail', params: { conversationId: contact.id, name: contact.name } })
                  })
              }
            }
            .width('100%')
            .padding({ left: 16, right: 16, top: 12, bottom: 12 })
            .backgroundColor('#FFFFFF')
          }
        })
      }
      .width('100%')
      .layoutWeight(1)
      .backgroundColor('#F5F5F5')
    }
    .width('100%')
    .height('100%')
  }

  // 组织架构Tab
  @Builder
  buildDepartmentTab() {
    List() {
      ForEach(this.state.departments, (dept: Department) => {
        ListItem() {
          Column() {
            Row() {
              Text(dept.avatar || '🏢')
                .fontSize(24)
                .margin({ right: 12 })

              Text(dept.name)
                .fontSize(16)
                .fontColor('#333333')
                .layoutWeight(1)

              Text(dept.memberCount + '人')
                .fontSize(13)
                .fontColor('#999999')

              Text('>')
                .fontSize(14)
                .fontColor('#CCCCCC')
                .margin({ left: 8 })
            }
            .width('100%')
            .padding(16)

            Divider().color('#F0F0F0').margin({ left: 52 })
          }
          .width('100%')
        }
        .backgroundColor('#FFFFFF')
      })
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }

  // 收藏Tab
  @Builder
  buildStarredTab() {
    Column() {
      if (this.state.starredContacts.length === 0) {
        Blank()
        Column() {
          Text('⭐')
            .fontSize(48)
          Text('暂无收藏联系人')
            .fontSize(14)
            .fontColor('#999999')
            .margin({ top: 12 })
          Text('点击联系人右侧星标收藏')
            .fontSize(12)
            .fontColor('#BBBBBB')
            .margin({ top: 4 })
        }
        Blank()
      } else {
        List() {
          ForEach(this.state.starredContacts, (contact: Contact) => {
            ListItem() {
              Row() {
                Text('⭐')
                  .fontSize(16)
                  .margin({ right: 12 })

                Column() {
                  Text(contact.name)
                    .fontSize(16)
                    .fontColor('#333333')

                  Text(contact.position + ' · ' + contact.department)
                    .fontSize(13)
                    .fontColor('#999999')
                    .margin({ top: 4 })
                }
                .alignItems(HorizontalAlign.Start)
                .layoutWeight(1)

                Text('移除')
                  .fontSize(13)
                  .fontColor('#FF4D4F')
                  .onClick(() => {
                    // 移除收藏
                  })
              }
              .width('100%')
              .padding(16)
              .backgroundColor('#FFFFFF')
            }
            .onClick(() => {
              router.pushUrl({ url: 'pages/ChatDetail', params: { conversationId: contact.id, name: contact.name } })
            })
          })
        }
        .width('100%')
        .layoutWeight(1)
        .backgroundColor('#F5F5F5')
      }
    }
    .width('100%')
    .height('100%')
  }
}
