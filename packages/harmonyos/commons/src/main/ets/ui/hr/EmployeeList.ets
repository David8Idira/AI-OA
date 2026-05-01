import router from '@ohos.router'

interface Employee {
  id: string
  name: string
  avatar: string
  dept: string
  position: string
  phone: string
  email: string
  status: string
}

class EmployeeListState {
  employees: Employee[] = []
  loading: boolean = false
  searchText: string = ''
  selectedDept: string = '全部'
}

@Entry
@Component
struct EmployeeList {
  @State state: EmployeeListState = new EmployeeListState()
  private scrollController: Scroller = new Scroller()

  private departments: string[] = ['全部', '技术部', '市场部', '财务部', '人力资源部', '行政部']

  aboutToAppear() {
    this.loadEmployees()
  }

  async loadEmployees(): Promise<void> {
    this.state.loading = true
    setTimeout(() => {
      this.state.employees = [
        { id: '1', name: '张三', avatar: '👨', dept: '技术部', position: '高级工程师', phone: '13800138001', email: 'zhangsan@company.com', status: '在职' },
        { id: '2', name: '李四', avatar: '👩', dept: '市场部', position: '市场经理', phone: '13800138002', email: 'lisi@company.com', status: '在职' },
        { id: '3', name: '王五', avatar: '👨', dept: '财务部', position: '财务主管', phone: '13800138003', email: 'wangwu@company.com', status: '在职' },
        { id: '4', name: '赵六', avatar: '👩', dept: '人力资源部', position: 'HR专员', phone: '13800138004', email: 'zhaoliu@company.com', status: '在职' },
        { id: '5', name: '钱七', avatar: '👨', dept: '技术部', position: '架构师', phone: '13800138005', email: 'qianqi@company.com', status: '在职' },
        { id: '6', name: '孙八', avatar: '👩', dept: '行政部', position: '行政助理', phone: '13800138006', email: 'sunba@company.com', status: '在职' },
        { id: '7', name: '周九', avatar: '👨', dept: '技术部', position: '工程师', phone: '13800138007', email: 'zhoujiu@company.com', status: '出差' },
        { id: '8', name: '吴十', avatar: '👩', dept: '市场部', position: '市场专员', phone: '13800138008', email: 'wushi@company.com', status: '在职' }
      ]
      this.state.loading = false
    }, 500)
  }

  private getFilteredEmployees(): Employee[] {
    return this.state.employees.filter(emp => {
      const matchDept = this.state.selectedDept === '全部' || emp.dept === this.state.selectedDept
      const matchSearch = emp.name.includes(this.state.searchText) || emp.dept.includes(this.state.searchText) || emp.position.includes(this.state.searchText)
      return matchDept && matchSearch
    })
  }

  private getStatusColor(status: string): string {
    switch (status) {
      case '在职': return '#52C41A'
      case '出差': return '#FAAD14'
      case '离职': return '#FF4D4F'
      default: return '#999999'
    }
  }

  build() {
    Column() {
      // 顶部导航
      Row() {
        Text('员工列表')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#333333')
      }
      .width('100%')
      .height(56)
      .padding({ left: 16 })
      .backgroundColor('#FFFFFF')
      .shadow({ radius: 4, color: '#10000000', offsetX: 0, offsetY: 2 })

      // 搜索框
      Row() {
        TextInput({ placeholder: '搜索员工姓名/部门/职位' })
          .width('100%')
          .height(40)
          .backgroundColor('#F5F5F5')
          .borderRadius(20)
          .placeholderColor('#999999')
          .onChange((val: string) => {
            this.state.searchText = val
          })
      }
      .width('100%')
      .padding(16)

      // 部门筛选
      Scroll() {
        Row() {
          ForEach(this.departments, (dept: string) => {
            Text(dept)
              .fontSize(13)
              .fontColor(this.state.selectedDept === dept ? '#FFFFFF' : '#666666')
              .backgroundColor(this.state.selectedDept === dept ? '#1890FF' : '#E6E6E6')
              .borderRadius(16)
              .padding({ left: 14, right: 14, top: 6, bottom: 6 })
              .margin({ right: 8 })
              .onClick(() => {
                this.state.selectedDept = dept
              })
          })
        }
      }
      .width('100%')
      .padding({ left: 16, right: 16 })
      .scrollable(ScrollDirection.Horizontal)
      .scrollBar(BarState.Off)

      // 员工列表
      Scroll(this.scrollController) {
        Column() {
          if (this.state.loading) {
            Row() {
              Text('加载中...')
                .fontSize(14)
                .fontColor('#999999')
            }
            .width('100%')
            .padding(40)
          } else {
            Text(`共 ${this.getFilteredEmployees().length} 名员工`)
              .fontSize(12)
              .fontColor('#999999')
              .alignSelf(HorizontalAlign.Start)
              .margin({ bottom: 12, left: 4 })

            ForEach(this.getFilteredEmployees(), (emp: Employee) => {
              this.buildEmployeeItem(emp)
            })
          }
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
  buildEmployeeItem(emp: Employee) {
    Row() {
      Text(emp.avatar)
        .fontSize(36)
        .width(50)
        .height(50)
        .backgroundColor('#F0F5FF')
        .borderRadius(25)
        .textAlign(TextAlign.Center)

      Column() {
        Row() {
          Text(emp.name)
            .fontSize(16)
            .fontWeight(FontWeight.Medium)
            .fontColor('#333333')

          Text(emp.status)
            .fontSize(11)
            .fontColor('#FFFFFF')
            .backgroundColor(this.getStatusColor(emp.status))
            .borderRadius(8)
            .padding({ left: 6, right: 6, top: 2, bottom: 2 })
            .margin({ left: 8 })
        }

        Text(`${emp.dept} · ${emp.position}`)
          .fontSize(12)
          .fontColor('#666666')
          .margin({ top: 4 })

        Text(`${emp.phone} | ${emp.email}`)
          .fontSize(11)
          .fontColor('#999999')
          .margin({ top: 2 })
      }
      .alignItems(HorizontalAlign.Start)
      .layoutWeight(1)
      .margin({ left: 12 })

      Text('>')
        .fontSize(16)
        .fontColor('#CCCCCC')
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
  }
}