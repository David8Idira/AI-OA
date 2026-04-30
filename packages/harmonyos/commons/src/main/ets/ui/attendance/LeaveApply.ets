// 请假申请页面
import router from '@ohos.router'
import promptAction from '@ohos.promptAction'
import { LeaveType, LeaveRecord, LeaveApplyParams } from '../../data/model/Attendance'

// 页面状态
class LeaveApplyState {
  leaveType: LeaveType = LeaveType.ANNUAL
  startDate: string = ''
  endDate: string = ''
  totalDays: number = 1
  reason: string = ''
  leaveTypeName: string = '年假'
  records: LeaveRecord[] = []
  loading: boolean = false
}

// 请假类型配置
const LEAVE_TYPE_CONFIG: Record<LeaveType, { name: string; icon: string; maxDays: number }> = {
  [LeaveType.ANNUAL]: { name: '年假', icon: '🏖️', maxDays: 10 },
  [LeaveType.SICK]: { name: '病假', icon: '🏥', maxDays: 30 },
  [LeaveType.PERSONAL]: { name: '事假', icon: '📋', maxDays: 5 },
  [LeaveType.MARRIAGE]: { name: '婚假', icon: '💒', maxDays: 3 },
  [LeaveType.MATERNITY]: { name: '产假', icon: '👶', maxDays: 98 },
  [LeaveType.PATERNITY]: { name: '陪产假', icon: '👨', maxDays: 15 },
  [LeaveType.FUNERAL]: { name: '丧假', icon: '🕯️', maxDays: 3 },
  [LeaveType.OTHER]: { name: '其他', icon: '📝', maxDays: 0 }
}

@Entry
@Component
struct LeaveApply {
  @State state: LeaveApplyState = new LeaveApplyState()

  aboutToAppear() {
    this.loadLeaveRecords()
  }

  // 加载请假记录
  async loadLeaveRecords(): Promise<void> {
    this.state.loading = true

    // 模拟请假记录
    const now = Date.now()
    this.state.records = [
      {
        id: 'l1',
        userId: 'current_user',
        userName: '当前用户',
        type: LeaveType.ANNUAL,
        startDate: '2026-03-15',
        endDate: '2026-03-17',
        totalDays: 3,
        reason: '旅游',
        status: 'approved',
        createdAt: now - 2592000000
      },
      {
        id: 'l2',
        userId: 'current_user',
        userName: '当前用户',
        type: LeaveType.SICK,
        startDate: '2026-02-20',
        endDate: '2026-02-20',
        totalDays: 1,
        reason: '身体不适',
        status: 'approved',
        createdAt: now - 5184000000
      }
    ]

    this.state.loading = false
  }

  // 提交请假申请
  async submitLeave(): Promise<void> {
    if (!this.state.startDate) {
      promptAction.showToast({ message: '请选择开始日期' })
      return
    }
    if (!this.state.endDate) {
      promptAction.showToast({ message: '请选择结束日期' })
      return
    }
    if (!this.state.reason.trim()) {
      promptAction.showToast({ message: '请填写请假原因' })
      return
    }

    this.state.loading = true

    // 模拟提交
    // const params: LeaveApplyParams = {
    //   type: this.state.leaveType,
    //   startDate: this.state.startDate,
    //   endDate: this.state.endDate,
    //   totalDays: this.state.totalDays,
    //   reason: this.state.reason
    // }
    // const res = await ApiClient.applyLeave(params)

    setTimeout(() => {
      this.state.loading = false
      promptAction.showToast({ message: '请假申请已提交' })
      router.back()
    }, 1000)
  }

  // 计算请假天数
  private calculateDays(): void {
    if (this.state.startDate && this.state.endDate) {
      const start = new Date(this.state.startDate)
      const end = new Date(this.state.endDate)
      const diff = end.getTime() - start.getTime()
      this.state.totalDays = Math.max(1, Math.ceil(diff / 86400000) + 1)
    }
  }

  // 获取请假类型名称
  private getLeaveTypeName(type: LeaveType): string {
    return LEAVE_TYPE_CONFIG[type]?.name || '其他'
  }

  // 获取状态样式
  private getStatusStyle(status: string): { color: string; text: string } {
    switch (status) {
      case 'approved':
        return { color: '#52C41A', text: '已通过' }
      case 'rejected':
        return { color: '#FF4D4F', text: '已拒绝' }
      case 'pending':
        return { color: '#FAAD14', text: '审批中' }
      case 'cancelled':
        return { color: '#999999', text: '已撤回' }
      default:
        return { color: '#999999', text: '未知' }
    }
  }

  build() {
    Column() {
      // 顶部导航栏
      this.buildNavBar()

      Scroll() {
        Column() {
          // 请假类型选择
          this.buildLeaveTypeSelector()

          // 请假信息表单
          this.buildLeaveForm()

          // 请假记录
          this.buildLeaveRecords()
        }
        .padding(16)
      }
      .width('100%')
      .layoutWeight(1)
      .backgroundColor('#F5F5F5')

      // 底部提交按钮
      this.buildBottomBar()
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }

  @Builder
  buildNavBar() {
    Row() {
      Image($r('app.media.icon_back'))
        .width(24)
        .height(24)
        .onClick(() => router.back())

      Text('请假申请')
        .fontSize(18)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .margin({ left: 16 })
        .layoutWeight(1)
    }
    .width('100%')
    .padding({ left: 16, right: 16, top: 12, bottom: 12 })
    .backgroundColor('#FFFFFF')
  }

  @Builder
  buildLeaveTypeSelector() {
    Column() {
      Text('请假类型')
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .alignSelf(HorizontalAlign.Start)
        .margin({ bottom: 12 })

      Grid() {
        ForEach(Object.entries(LEAVE_TYPE_CONFIG), (entry: [string, any]) => {
          const type = entry[0] as LeaveType
          const config = entry[1]

          GridItem() {
            Column() {
              Text(config.icon)
                .fontSize(24)

              Text(config.name)
                .fontSize(12)
                .fontColor(this.state.leaveType === type ? '#1890FF' : '#333333')
                .margin({ top: 4 })

              if (config.maxDays > 0) {
                Text(`最多${config.maxDays}天`)
                  .fontSize(10)
                  .fontColor('#999999')
              }
            }
            .width('100%')
            .padding(10)
            .backgroundColor(this.state.leaveType === type ? '#1890FF15' : '#FFFFFF')
            .borderRadius(8)
            .border({
              width: this.state.leaveType === type ? 1 : 0,
              color: '#1890FF'
            })
          }
          .onClick(() => {
            this.state.leaveType = type
            this.state.leaveTypeName = config.name
          })
        })
      }
      .columnsTemplate('1fr 1fr 1fr 1fr')
      .rowsTemplate('1fr 1fr')
      .width('100%')
      .height(160)
      .columnsGap(8)
      .rowsGap(8)
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
  }

  @Builder
  buildLeaveForm() {
    Column() {
      Text('请假信息')
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .alignSelf(HorizontalAlign.Start)
        .margin({ bottom: 12 })

      // 开始日期
      Row() {
        Text('开始日期')
          .fontSize(14)
          .fontColor('#999999')
          .width(80)

        Text(this.state.startDate || '请选择开始日期')
          .fontSize(14)
          .fontColor(this.state.startDate ? '#333333' : '#999999')
          .layoutWeight(1)
          .onClick(() => {
            promptAction.showToast({ message: '请选择开始日期' })
          })

        Text('>')
          .fontSize(14)
          .fontColor('#CCCCCC')
      }
      .width('100%')
      .padding({ top: 12, bottom: 12 })
      .onClick(() => {
        // 显示日期选择器
        const tomorrow = new Date()
        tomorrow.setDate(tomorrow.getDate() + 1)
        this.state.startDate = tomorrow.toISOString().split('T')[0]
        this.calculateDays()
      })

      Divider().color('#F0F0F0')

      // 结束日期
      Row() {
        Text('结束日期')
          .fontSize(14)
          .fontColor('#999999')
          .width(80)

        Text(this.state.endDate || '请选择结束日期')
          .fontSize(14)
          .fontColor(this.state.endDate ? '#333333' : '#999999')
          .layoutWeight(1)
          .onClick(() => {
            promptAction.showToast({ message: '请选择结束日期' })
          })

        Text('>')
          .fontSize(14)
          .fontColor('#CCCCCC')
      }
      .width('100%')
      .padding({ top: 12, bottom: 12 })
      .onClick(() => {
        // 显示日期选择器
        const nextWeek = new Date()
        nextWeek.setDate(nextWeek.getDate() + 2)
        this.state.endDate = nextWeek.toISOString().split('T')[0]
        this.calculateDays()
      })

      Divider().color('#F0F0F0')

      // 请假天数
      Row() {
        Text('请假天数')
          .fontSize(14)
          .fontColor('#999999')
          .width(80)

        Text(`${this.state.totalDays}天`)
          .fontSize(14)
          .fontColor('#333333')
          .layoutWeight(1)
      }
      .width('100%')
      .padding({ top: 12, bottom: 12 })

      Divider().color('#F0F0F0')

      // 请假原因
      Row() {
        Text('请假原因')
          .fontSize(14)
          .fontColor('#999999')
          .width(80)
          .alignSelf(VerticalAlign.Top)
          .margin({ top: 12 })
      }
      .width('100%')

      TextInput({
        placeholder: '请输入请假原因',
        text: this.state.reason
      })
        .fontSize(14)
        .placeholderColor('#999999')
        .backgroundColor('#F5F5F5')
        .borderRadius(8)
        .height(80)
        .padding(12)
        .margin({ top: 8 })
        .onChange((value: string) => {
          this.state.reason = value
        })
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
  }

  @Builder
  buildLeaveRecords() {
    Column() {
      Text('请假记录')
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .alignSelf(HorizontalAlign.Start)
        .margin({ bottom: 12 })

      if (this.state.records.length === 0) {
        Text('暂无请假记录')
          .fontSize(14)
          .fontColor('#999999')
          .padding(20)
          .width('100%')
          .textAlign(TextAlign.Center)
      } else {
        ForEach(this.state.records, (record: LeaveRecord) => {
          Column() {
            Row() {
              Text(this.getLeaveTypeName(record.type))
                .fontSize(14)
                .fontColor('#333333')
                .fontWeight(FontWeight.Medium)

              Blank()

              const statusStyle = this.getStatusStyle(record.status)
              Text(statusStyle.text)
                .fontSize(12)
                .fontColor(statusStyle.color)
                .backgroundColor(statusStyle.color + '20')
                .borderRadius(4)
                .padding({ left: 8, right: 8, top: 2, bottom: 2 })
            }
            .width('100%')
            .margin({ bottom: 8 })

            Row() {
              Text(`${record.startDate} 至 ${record.endDate}`)
                .fontSize(13)
                .fontColor('#666666')
                .layoutWeight(1)

              Text(`${record.totalDays}天`)
                .fontSize(13)
                .fontColor('#999999')
            }
            .width('100%')

            Text(record.reason)
              .fontSize(12)
              .fontColor('#999999')
              .margin({ top: 6 })
              .maxLines(2)
              .textOverflow({ overflow: TextOverflow.Ellipsis })
          }
          .width('100%')
          .padding(12)
          .backgroundColor('#FAFAFA')
          .borderRadius(8)
          .margin({ bottom: 8 })
        })
      }
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
  }

  @Builder
  buildBottomBar() {
    Row() {
      Button('提交申请')
        .fontSize(16)
        .fontColor('#FFFFFF')
        .backgroundColor('#1890FF')
        .borderRadius(24)
        .height(48)
        .width('100%')
        .onClick(() => this.submitLeave())
    }
    .width('100%')
    .padding({ left: 16, right: 16, top: 12, bottom: 12 })
    .backgroundColor('#FFFFFF')
    .border({
      width: { top: 1 },
      color: '#E0E0E0'
    })
  }
}
