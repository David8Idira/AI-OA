import router from '@ohos.router'

interface Department {
  id: string
  name: string
  manager: string
  employeeCount: number
  children?: Department[]
}

class DepartmentListState {
  departments: Department[] = []
  loading: boolean = false
  expandedIds: Set<string> = new Set()
}

@Entry
@Component
struct DepartmentList {
  @State state: DepartmentListState = new DepartmentListState()
  private scrollController: Scroller = new Scroller()

  aboutToAppear() {
    this.loadDepartments()
  }

  async loadDepartments(): Promise<void> {
    this.state.loading = true
    setTimeout(() => {
      this.state.departments = [
        {
          id: '1',
          name: '技术部',
          manager: '张三',
          employeeCount: 25,
          children: [
            { id: '1-1', name: '前端组', manager: '李明', employeeCount: 8 },
            { id: '1-2', name: '后端组', manager: '王强', employeeCount: 10 },
            { id: '1-3', name: '移动端组', manager: '刘伟', employeeCount: 7 }
          ]
        },
        {
          id: '2',
          name: '市场部',
          manager: '李四',
          employeeCount: 15,
          children: [
            { id: '2-1', name: '市场策划', manager: '赵敏', employeeCount: 5 },
            { id: '2-2', name: '销售组', manager: '孙浩', employeeCount: 10 }
          ]
        },
        {
          id: '3',
          name: '财务部',
          manager: '王五',
          employeeCount: 8,
          children: []
        },
        {
          id: '4',
          name: '人力资源部',
          manager: '赵六',
          employeeCount: 6,
          children: []
        },
        {
          id: '5',
          name: '行政部',
          manager: '钱七',
          employeeCount: 5,
          children: []
        },
        {
          id: '6',
          name: '产品部',
          manager: '周八',
          employeeCount: 10,
          children: [
            { id: '6-1', name: '产品设计', manager: '吴霞', employeeCount: 4 },
            { id: '6-2', name: '产品运营', manager: '郑九', employeeCount: 6 }
          ]
        }
      ]
      this.state.loading = false
    }, 500)
  }

  private toggleExpand(id: string): void {
    if (this.state.expandedIds.has(id)) {
      this.state.expandedIds.delete(id)
    } else {
      this.state.expandedIds.add(id)
    }
  }

  build() {
    Column() {
      // 顶部导航
      Row() {
        Text('部门列表')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#333333')
      }
      .width('100%')
      .height(56)
      .padding({ left: 16 })
      .backgroundColor('#FFFFFF')
      .shadow({ radius: 4, color: '#10000000', offsetX: 0, offsetY: 2 })

      if (this.state.loading) {
        Row() {
          Text('加载中...')
            .fontSize(14)
            .fontColor('#999999')
        }
        .width('100%')
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Center)
        .justifyContent(FlexAlign.Center)
      } else {
        Scroll(this.scrollController) {
          Column() {
            Text(`共 ${this.state.departments.length} 个部门`)
              .fontSize(12)
              .fontColor('#999999')
              .alignSelf(HorizontalAlign.Start)
              .margin({ bottom: 12, left: 4 })

            ForEach(this.state.departments, (dept: Department) => {
              this.buildDepartmentItem(dept, 0)
            })
          }
          .padding(16)
        }
        .width('100%')
        .layoutWeight(1)
        .backgroundColor('#F5F5F5')
      }
    }
    .width('100%')
    .height('100%')
  }

  @Builder
  buildDepartmentItem(dept: Department, level: number) {
    Column() {
      Row() {
        if (dept.children && dept.children.length > 0) {
          Text(this.state.expandedIds.has(dept.id) ? '▼' : '▶')
            .fontSize(12)
            .fontColor('#999999')
            .width(20)
            .onClick(() => this.toggleExpand(dept.id))
        } else {
          Text('')
            .width(20)
        }

        Column() {
          Row() {
            Text(dept.name)
              .fontSize(level === 0 ? 16 : 15)
              .fontWeight(level === 0 ? FontWeight.Medium : FontWeight.Normal)
              .fontColor('#333333')

            Text(`${dept.employeeCount}人`)
              .fontSize(12)
              .fontColor('#999999')
              .backgroundColor('#F5F5F5')
              .borderRadius(8)
              .padding({ left: 8, right: 8, top: 2, bottom: 2 })
              .margin({ left: 8 })
          }

          Row() {
            Text(`负责人：${dept.manager}`)
              .fontSize(12)
              .fontColor('#666666')
              .margin({ top: 4 })
          }
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1)
        .margin({ left: level * 16 })
      }
      .width('100%')
      .padding(16)
      .backgroundColor(level === 0 ? '#FFFFFF' : '#FAFAFA')
      .borderRadius(level === 0 ? 12 : 8)
      .margin({ bottom: 8 })
      .onClick(() => {
        if (dept.children && dept.children.length > 0) {
          this.toggleExpand(dept.id)
        }
      })

      // 子部门
      if (dept.children && dept.children.length > 0 && this.state.expandedIds.has(dept.id)) {
        ForEach(dept.children, (child: Department) => {
          this.buildDepartmentItem(child, level + 1)
        })
      }
    }
    .width('100%')
  }
}