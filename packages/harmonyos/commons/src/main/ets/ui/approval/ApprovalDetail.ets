// 审批详情页面
import router from '@ohos.router'
import promptAction from '@ohos.promptAction'
import { Approval, ApprovalStatus, ApprovalType, ApprovalPriority, ApprovalNode } from '../../data/model/Approval'
import { ApprovalFlowNode, ApprovalActionButtons } from '../components/ApprovalCard'

// 页面状态
class ApprovalDetailState {
  approval: Approval | null = null
  loading: boolean = false
  comment: string = ''
}

// 表单字段类型名称映射
const FORM_FIELD_NAMES: Record<string, string> = {
  leaveType: '请假类型',
  startDate: '开始日期',
  endDate: '结束日期',
  leaveDays: '请假天数',
  reason: '请假原因',
  overtimeType: '加班类型',
  overtimeHours: '加班时长',
  destination: '出差目的地',
  expenseType: '报销类型',
  expenseAmount: '报销金额',
  expenseDesc: '报销说明'
}

@Entry
@Component
struct ApprovalDetail {
  @State state: ApprovalDetailState = new ApprovalDetailState()

  aboutToAppear() {
    const params = router.getParams() as Record<string, string>
    if (params && params['approvalId']) {
      this.loadApproval(params['approvalId'])
    } else {
      // 模拟数据
      this.loadMockApproval()
    }
  }

  // 加载审批详情
  async loadApproval(approvalId: string): Promise<void> {
    this.state.loading = true
    // 实际调用API
    // const res = await ApiClient.getApproval(approvalId)
    // this.state.approval = res.data

    // 使用模拟数据
    this.loadMockApproval()
    this.state.loading = false
  }

  // 加载模拟审批数据
  private loadMockApproval(): void {
    const now = Date.now()
    this.state.approval = {
      id: 'a1',
      title: '张三 - 请假申请',
      type: ApprovalType.LEAVE,
      status: ApprovalStatus.PENDING,
      priority: ApprovalPriority.NORMAL,
      applicantId: 'user_001',
      applicantName: '张三',
      applicantAvatar: '',
      applicantDept: '产品部',
      formFields: [
        { key: 'leaveType', label: '请假类型', type: 'select', value: '年假', required: true, options: [{ label: '年假', value: 'annual' }, { label: '事假', value: 'personal' }] },
        { key: 'startDate', label: '开始日期', type: 'date', value: '2026-05-01', required: true },
        { key: 'endDate', label: '结束日期', type: 'date', value: '2026-05-03', required: true },
        { key: 'leaveDays', label: '请假天数', type: 'number', value: '3', required: true },
        { key: 'reason', label: '请假原因', type: 'textarea', value: '因私事需要处理，申请请假三天。', required: true }
      ],
      flowNodes: [
        {
          id: 'n1',
          name: '提交申请',
          type: 'start',
          assigneeId: 'user_001',
          assigneeName: '张三',
          status: 'approved',
          comment: '',
          operatedAt: now - 86400000
        },
        {
          id: 'n2',
          name: '直属上级审批',
          type: 'approval',
          assigneeId: 'user_002',
          assigneeName: '李四',
          status: 'pending',
          comment: ''
        },
        {
          id: 'n3',
          name: 'HR审批',
          type: 'approval',
          assigneeId: 'user_003',
          assigneeName: '王五',
          status: 'pending',
          comment: ''
        },
        {
          id: 'n4',
          name: '审批完成',
          type: 'end',
          assigneeId: '',
          assigneeName: '',
          status: 'pending'
        }
      ],
      currentNodeId: 'n2',
      createdAt: now - 86400000,
      updatedAt: now - 3600000
    }
  }

  // 获取状态颜色
  private getStatusColor(status: ApprovalStatus): string {
    switch (status) {
      case ApprovalStatus.PENDING:
        return '#FAAD14'
      case ApprovalStatus.APPROVED:
        return '#52C41A'
      case ApprovalStatus.REJECTED:
        return '#FF4D4F'
      default:
        return '#999999'
    }
  }

  // 获取状态文字
  private getStatusText(status: ApprovalStatus): string {
    switch (status) {
      case ApprovalStatus.PENDING:
        return '待审批'
      case ApprovalStatus.APPROVED:
        return '已同意'
      case ApprovalStatus.REJECTED:
        return '已拒绝'
      case ApprovalStatus.CANCELLED:
        return '已撤回'
      default:
        return '未知'
    }
  }

  // 同意审批
  async handleApprove(): Promise<void> {
    if (!this.state.approval) return

    // 实际调用API
    // await ApiClient.approvalAction({
    //   approvalId: this.state.approval.id,
    //   nodeId: this.state.approval.currentNodeId,
    //   action: 'approve',
    //   comment: this.state.comment
    // })

    promptAction.showToast({ message: '已同意' })
    router.back()
  }

  // 拒绝审批
  async handleReject(): Promise<void> {
    if (!this.state.approval) return

    // 实际调用API
    // await ApiClient.approvalAction({
    //   approvalId: this.state.approval.id,
    //   nodeId: this.state.approval.currentNodeId,
    //   action: 'reject',
    //   comment: this.state.comment
    // })

    promptAction.showToast({ message: '已拒绝' })
    router.back()
  }

  // 转交审批
  handleTransfer(): void {
    // 显示转交选择弹窗
    promptAction.showToast({ message: '请选择转交人' })
  }

  // 撤回审批
  async handleCancel(): Promise<void> {
    if (!this.state.approval) return

    promptAction.showToast({ message: '已撤回' })
    router.back()
  }

  build() {
    Column() {
      // 顶部导航栏
      this.buildNavBar()

      if (this.state.loading || !this.state.approval) {
        Blank()
        Text('加载中...')
          .fontSize(14)
          .fontColor('#999999')
        Blank()
      } else {
        Scroll() {
          Column() {
            // 基本信息卡片
            this.buildBasicInfo()

            // 审批表单
            this.buildFormSection()

            // 审批流程
            this.buildFlowSection()

            // 审批意见
            this.buildCommentSection()
          }
          .padding(16)
        }
        .width('100%')
        .layoutWeight(1)
        .backgroundColor('#F5F5F5')

        // 底部操作按钮
        this.buildBottomActions()
      }
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

      Text('审批详情')
        .fontSize(18)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .margin({ left: 16 })
        .layoutWeight(1)

      Image($r('app.media.icon_more'))
        .width(24)
        .height(24)
    }
    .width('100%')
    .padding({ left: 16, right: 16, top: 12, bottom: 12 })
    .backgroundColor('#FFFFFF')
  }

  @Builder
  buildBasicInfo() {
    Column() {
      Row() {
        Column() {
          Text(this.state.approval!.title)
            .fontSize(18)
            .fontWeight(FontWeight.Bold)
            .fontColor('#333333')

          Row() {
            Text(this.state.approval!.applicantName)
              .fontSize(14)
              .fontColor('#666666')

            Text(' · ')
              .fontSize(14)
              .fontColor('#999999')

            Text(this.state.approval!.applicantDept)
              .fontSize(14)
              .fontColor('#999999')
          }
          .margin({ top: 8 })
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1)

        // 状态标签
        Text(this.getStatusText(this.state.approval!.status))
          .fontSize(13)
          .fontColor(this.getStatusColor(this.state.approval!.status))
          .padding({ left: 12, right: 12, top: 6, bottom: 6 })
          .backgroundColor(this.getStatusColor(this.state.approval!.status) + '20')
          .borderRadius(16)
      }
      .width('100%')
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
  }

  @Builder
  buildFormSection() {
    Column() {
      Text('申请信息')
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .alignSelf(HorizontalAlign.Start)
        .margin({ bottom: 12 })

      Column() {
        ForEach(this.state.approval!.formFields, (field: any) => {
          Row() {
            Text(field.label)
              .fontSize(14)
              .fontColor('#999999')
              .width(80)

            Text(field.value.toString())
              .fontSize(14)
              .fontColor('#333333')
              .layoutWeight(1)
          }
          .width('100%')
          .padding({ top: 10, bottom: 10 })
        })
      }
      .width('100%')
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
  }

  @Builder
  buildFlowSection() {
    Column() {
      Text('审批流程')
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .alignSelf(HorizontalAlign.Start)
        .margin({ bottom: 12 })

      Column() {
        ForEach(this.state.approval!.flowNodes, (node: ApprovalNode, index: number) => {
          ApprovalFlowNode({
            node: node,
            isLast: index === this.state.approval!.flowNodes.length - 1
          })
        })
      }
      .width('100%')
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
  }

  @Builder
  buildCommentSection() {
    Column() {
      Text('审批意见')
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .alignSelf(HorizontalAlign.Start)
        .margin({ bottom: 12 })

      TextInput({
        placeholder: '请输入审批意见（选填）',
        text: this.state.comment
      })
        .fontSize(14)
        .placeholderColor('#999999')
        .backgroundColor('#F5F5F5')
        .borderRadius(8)
        .height(100)
        .padding(12)
        .onChange((value: string) => {
          this.state.comment = value
        })
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
  }

  @Builder
  buildBottomActions() {
    Column() {
      ApprovalActionButtons({
        status: this.state.approval!.status,
        onApprove: () => this.handleApprove(),
        onReject: () => this.handleReject(),
        onTransfer: () => this.handleTransfer(),
        onCancel: () => this.handleCancel()
      })
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
