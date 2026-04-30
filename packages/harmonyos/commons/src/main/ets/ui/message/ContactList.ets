// 联系人列表页面
import router from '@ohos.router'

// 联系人模型
interface Contact {
  id: string
  name: string
  avatar?: string
  position?: string
  department?: string
  phone?: string
  email?: string
}

// 部门模型
interface Department {
  id: string
  name: string
  memberCount: number
  children?: Department[]
}

// 页面状态
class ContactListState {
  contacts: Contact[] = []
  departments: Department[] = []
  searchKeyword: string = ''
  loading: boolean = false
  currentTabIndex: number = 0
}

@Entry
@Component
struct ContactList {
  @State state: ContactListState = new ContactListState()
  private tabController: TabsController = new TabsController()

  aboutToAppear() {
    this.loadData()
  }

  // 加载数据
  async loadData(): Promise<void> {
    this.state.loading = true
    this.state.contacts = this.getMockContacts()
    this.state.departments = this.getMockDepartments()
    this.state.loading = false
  }

  // 获取模拟联系人数据
  private getMockContacts(): Contact[] {
    return [
      { id: '1', name: '张三', position: '产品经理', department: '产品部', phone: '138****0001', email: 'zhangsan@company.com' },
      { id: '2', name: '李四', position: '高级工程师', department: '研发部', phone: '138****0002', email: 'lisi@company.com' },
      { id: '3', name: '王五', position: 'UI设计师', department: '设计部', phone: '138****0003', email: 'wangwu@company.com' },
      { id: '4', name: '赵六', position: '测试工程师', department: '测试部', phone: '138****0004', email: 'zhaoliu@company.com' },
      { id: '5', name: '钱七', position: '项目经理', department: '项目部', phone: '138****0005', email: 'qianqi@company.com' },
      { id: '6', name: '孙八', position: '前端工程师', department: '研发部', phone: '138****0006', email: 'sunba@company.com' },
      { id: '7', name: '周九', position: '后端工程师', department: '研发部', phone: '138****0007', email: 'zhoujiu@company.com' },
      { id: '8', name: '吴十', position: '运维工程师', department: '运维部', phone: '138****0008', email: 'wushi@company.com' }
    ]
  }

  // 获取模拟部门数据
  private getMockDepartments(): Department[] {
    return [
      { id: 'd1', name: '总经理办公室', memberCount: 3 },
      { id: 'd2', name: '产品部', memberCount: 15, children: [
        { id: 'd2-1', name: '产品一组', memberCount: 8 },
        { id: 'd2-2', name: '产品二组', memberCount: 7 }
      ]},
      { id: 'd3', name: '研发部', memberCount: 50, children: [
        { id: 'd3-1', name: '前端组', memberCount: 15 },
        { id: 'd3-2', name: '后端组', memberCount: 20 },
        { id: 'd3-3', name: '移动端组', memberCount: 15 }
      ]},
      { id: 'd4', name: '设计部', memberCount: 12 },
      { id: 'd5', name: '测试部', memberCount: 18 },
      { id: 'd6', name: '运维部', memberCount: 8 },
      { id: 'd7', name: '市场部', memberCount: 20 },
      { id: 'd8', name: '人力资源部', memberCount: 10 }
    ]
  }

  // 字母索引
  private alphabet: string[] = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('')

  // 获取名字首字母
  private getPinyin(name: string): string {
    // 简化模拟：直接返回名字首字母
    return name.charAt(0).toUpperCase()
  }

  // 按字母分组联系人
  private getGroupedContacts(): Map<string, Contact[]> {
    const grouped = new Map<string, Contact[]>()
    for (const contact of this.state.contacts) {
      const letter = this.getPinyin(contact.name)
      if (!grouped.has(letter)) {
        grouped.set(letter, [])
      }
      grouped.get(letter)!.push(contact)
    }
    return grouped
  }

  build() {
    Column() {
      // 顶部导航栏
      this.buildNavBar()

      // 搜索栏
      this.buildSearchBar()

      // Tab切换
      Tabs({ barPosition: BarPosition.Start, controller: this.tabController }) {
        TabContent() {
          this.buildContactList()
        }
        .tabBar('联系人')

        TabContent() {
          this.buildDepartmentList()
        }
        .tabBar('组织架构')

        TabContent() {
          this.buildStarredList()
        }
        .tabBar('收藏')
      }
      .onChange((index: number) => {
        this.state.currentTabIndex = index
      })
      .barHeight(44)
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

  @Builder
  buildSearchBar() {
    Row() {
      Image($r('app.media.icon_search'))
        .width(16)
        .height(16)
        .margin({ left: 12 })
        .opacity(0.5)

      Text('搜索')
        .fontSize(14)
        .fontColor('#999999')
        .margin({ left: 8 })
    }
    .width('100%')
    .height(40)
    .backgroundColor('#F5F5F5')
    .borderRadius(8)
    .margin({ left: 16, right: 16, top: 8, bottom: 8 })
  }

  // 联系人列表
  @Builder
  buildContactList() {
    List() {
      ForEach(this.state.contacts, (contact: Contact) => {
        ListItem() {
          Row() {
            // 头像
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

            // 打电话
            Text('📞')
              .fontSize(20)
              .padding(8)
              .onClick(() => {
                // 打电话
              })
          }
          .width('100%')
          .padding({ left: 16, right: 16, top: 12, bottom: 12 })
          .backgroundColor('#FFFFFF')
        }
        .onClick(() => {
          router.pushUrl({ url: 'pages/ChatDetail', params: { conversationId: contact.id, name: contact.name } })
        })
      })
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }

  // 组织架构列表
  @Builder
  buildDepartmentList() {
    List() {
      ForEach(this.state.departments, (dept: Department) => {
        ListItem() {
          Column() {
            Row() {
              Text('🏢')
                .fontSize(20)
                .margin({ right: 12 })

              Text(dept.name)
                .fontSize(16)
                .fontColor('#333333')
                .layoutWeight(1)

              Text(dept.memberCount + '人')
                .fontSize(13)
                .fontColor('#999999')

              Text('>')
                .fontSize(16)
                .fontColor('#CCCCCC')
                .margin({ left: 8 })
            }
            .width('100%')
            .padding({ left: 16, right: 16, top: 14, bottom: 14 })

            // 子部门
            if (dept.children) {
              ForEach(dept.children, (child: Department) => {
                Row() {
                  Text('📁')
                    .fontSize(16)
                    .margin({ right: 12, left: 32 })

                  Text(child.name)
                    .fontSize(15)
                    .fontColor('#666666')
                    .layoutWeight(1)

                  Text(child.memberCount + '人')
                    .fontSize(12)
                    .fontColor('#999999')
                }
                .width('100%')
                .padding({ left: 16, right: 16, top: 10, bottom: 10 })
                .backgroundColor('#FAFAFA')
              })
            }

            Divider().color('#F0F0F0').width('90%').margin({ left: 16 })
          }
          .width('100%')
        }
      })
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#FFFFFF')
  }

  // 收藏列表
  @Builder
  buildStarredList() {
    Column() {
      if (this.state.contacts.length === 0) {
        Blank()
        Text('暂无收藏联系人')
          .fontSize(14)
          .fontColor('#999999')
        Blank()
      } else {
        List() {
          ForEach(this.state.contacts.slice(0, 3), (contact: Contact) => {
            ListItem() {
              Row() {
                Text('⭐')
                  .fontSize(20)

                Text(contact.name)
                  .fontSize(16)
                  .fontColor('#333333')
                  .margin({ left: 12 })
                  .layoutWeight(1)

                Text('移除')
                  .fontSize(13)
                  .fontColor('#FF4D4F')
                  .onClick(() => {
                    // 移除收藏
                  })
              }
              .width('100%')
              .padding({ left: 16, right: 16, top: 14, bottom: 14 })
              .backgroundColor('#FFFFFF')
            }
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
