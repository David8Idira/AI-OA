// 通讯录页面
import router from '@ohos.router'
import { hrService } from '../../service/HrService'
import { EmployeeInfo, DepartmentInfo } from '../../model/hr/HrModel'

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
    try {
      const res = await hrService.getEmployeeList({ pageNum: 1, pageSize: 100 });
      const deptRes = await hrService.getDepartmentList();
      this.state.contacts = res.records.map((emp: EmployeeInfo) => ({
        id: emp.id,
        name: emp.name,
        position: emp.position,
        department: emp.departmentName,
        phone: emp.phone ? emp.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : ''
      }));
      this.state.departments = deptRes.map((d: DepartmentInfo) => ({
        id: d.id,
        name: d.departmentName,
        memberCount: 0
      }));
      this.state.starredContacts = this.state.contacts.slice(0, 3);
    } catch (error) {
      // 使用空数据
      this.state.contacts = [];
      this.state.departments = [];
      this.state.starredContacts = [];
    }
    this.state.loading = false;
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
