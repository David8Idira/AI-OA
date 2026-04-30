// 审批卡片组件
import { Approval, ApprovalStatus, ApprovalPriority } from '../../data/model/Approval'

// 审批卡片属性
@Component
export struct ApprovalCard {
  @Prop approval: Approval
  onClick: () => void = () => {}

  // 获取状态颜色
  private getStatusColor(status: ApprovalStatus): string {
    switch (status) {
      case ApprovalStatus.PENDING:
        return '#FAAD14'
      case ApprovalStatus.APPROVED:
        return '#52C41A'
      case ApprovalStatus.REJECTED:
        return '#FF4D4F'
      case ApprovalStatus.CANCELLED:
        return '#999999'
      case ApprovalStatus.DRAFT:
        return '#8C8C8C'
      case ApprovalStatus.TRANSFERRED:
        return '#1890FF'
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
      case ApprovalStatus.DRAFT:
        return '草稿'
      case ApprovalStatus.TRANSFERRED:
        return '已转交'
      default:
        return '未知'
    }
  }

  // 获取优先级文字
  private getPriorityText(priority: ApprovalPriority): string {
    switch (priority) {
      case ApprovalPriority.VERY_URGENT:
        return '紧急'
      case ApprovalPriority.URGENT:
        return '加急'
      default:
        return ''
    }
  }

  // 格式化时间
  private formatTime(timestamp: number): string {
    const date = new Date(timestamp)
    return date.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  // 获取类型图标
  private getTypeIcon(type: string): string {
    switch (type) {
      case 'leave':
        return '📅'
      case 'overtime':
        return '⏰'
      case 'trip':
        return '✈️'
      case 'expense':
        return '💰'
      case 'purchase':
        return '🛒'
      default:
        return '📝'
    }
  }

  build() {
    Column() {
      Row() {
        // 类型图标
        Text(this.getTypeIcon(this.approval.type))
          .fontSize(32)
          .margin({ right: 12 })

        Column() {
          // 标题行
          Row() {
            Text(this.approval.title)
              .fontSize(16)
              .fontWeight(FontWeight.Medium)
              .fontColor('#333333')
              .maxLines(1)
              .textOverflow({ overflow: TextOverflow.Ellipsis })
              .layoutWeight(1)

            if (this.approval.priority === ApprovalPriority.VERY_URGENT) {
              Text('紧急')
                .fontSize(10)
                .fontColor('#FFFFFF')
                .backgroundColor('#FF4D4F')
                .borderRadius(4)
                .padding({ left: 6, right: 6, top: 2, bottom: 2 })
                .margin({ left: 8 })
            }
          }

          // 申请人信息
          Row() {
            Text(this.approval.applicantName)
              .fontSize(13)
              .fontColor('#666666')

            Text(' · ')
              .fontSize(13)
              .fontColor('#999999')

            Text(this.approval.applicantDept)
              .fontSize(13)
              .fontColor('#999999')
          }
          .margin({ top: 6 })

          // 时间行
          Row() {
            Text(this.formatTime(this.approval.createdAt))
              .fontSize(12)
              .fontColor('#999999')

            Blank()

            // 状态标签
            Text(this.getStatusText(this.approval.status))
              .fontSize(12)
              .fontColor(this.getStatusColor(this.approval.status))
              .padding({ left: 8, right: 8, top: 3, bottom: 3 })
              .backgroundColor(this.getStatusColor(this.approval.status) + '20')
              .borderRadius(4)
          }
          .margin({ top: 8 })
        }
        .layoutWeight(1)
      }
      .width('100%')
      .alignItems(VerticalAlign.Top)
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .onClick(this.onClick)
  }
}

// 审批流程节点
@Component
export struct ApprovalFlowNode {
  @Prop node: {
    name: string
    assigneeName: string
    status: string
    comment?: string
    operatedAt?: number
  }
  @Prop isLast: boolean = false

  private getStatusColor(status: string): string {
    switch (status) {
      case 'approved':
        return '#52C41A'
      case 'rejected':
        return '#FF4D4F'
      case 'pending':
        return '#FAAD14'
      default:
        return '#999999'
    }
  }

  private getStatusIcon(status: string): string {
    switch (status) {
      case 'approved':
        return '✓'
      case 'rejected':
        return '✗'
      case 'pending':
        return '○'
      default:
        return '●'
    }
  }

  build() {
    Row() {
      Column() {
        // 状态图标
        Text(this.getStatusIcon(this.node.status))
          .fontSize(16)
          .fontColor(this.getStatusColor(this.node.status))
          .width(28)
          .height(28)
          .borderRadius(14)
          .backgroundColor(this.getStatusColor(this.node.status) + '20')
          .textAlign(TextAlign.Center)

        // 连接线
        if (!this.isLast) {
          Line()
            .width(2)
            .height(40)
            .backgroundColor('#E0E0E0')
        }
      }
      .width(28)
      .alignItems(HorizontalAlign.Center)

      Column() {
        // 节点名称
        Text(this.node.name)
          .fontSize(14)
          .fontWeight(FontWeight.Medium)
          .fontColor('#333333')

        // 审批人
        Text(this.node.assigneeName)
          .fontSize(13)
          .fontColor('#666666')
          .margin({ top: 4 })

        // 审批意见
        if (this.node.comment) {
          Text(this.node.comment)
            .fontSize(12)
            .fontColor('#999999')
            .margin({ top: 4 })
            .padding(8)
            .backgroundColor('#F5F5F5')
            .borderRadius(4)
        }

        // 操作时间
        if (this.node.operatedAt) {
          Text(new Date(this.node.operatedAt).toLocaleString('zh-CN'))
            .fontSize(11)
            .fontColor('#BBBBBB')
            .margin({ top: 4 })
        }
      }
      .alignItems(HorizontalAlign.Start)
      .layoutWeight(1)
      .padding({ left: 12 })
    }
    .width('100%')
    .padding({ top: 8, bottom: 8 })
  }
}

// 审批操作按钮组
@Component
export struct ApprovalActionButtons {
  @Prop status: ApprovalStatus
  onApprove: () => void = () => {}
  onReject: () => void = () => {}
  onTransfer: () => void = () => {}
  onCancel: () => void = () => {}

  build() {
    if (this.status === ApprovalStatus.PENDING) {
      Row() {
        // 转交按钮
        Button('转交')
          .fontSize(14)
          .fontColor('#666666')
          .backgroundColor('#F5F5F5')
          .borderRadius(8)
          .height(40)
          .layoutWeight(1)
          .onClick(this.onTransfer)

        // 拒绝按钮
        Button('拒绝')
          .fontSize(14)
          .fontColor('#FFFFFF')
          .backgroundColor('#FF4D4F')
          .borderRadius(8)
          .height(40)
          .layoutWeight(1)
          .margin({ left: 12 })
          .onClick(this.onReject)

        // 同意按钮
        Button('同意')
          .fontSize(14)
          .fontColor('#FFFFFF')
          .backgroundColor('#52C41A')
          .borderRadius(8)
          .height(40)
          .layoutWeight(1)
          .margin({ left: 12 })
          .onClick(this.onApprove)
      }
      .width('100%')
    } else if (this.status === ApprovalStatus.DRAFT || this.status === ApprovalStatus.CANCELLED) {
      Button('撤回')
        .fontSize(14)
        .fontColor('#666666')
        .backgroundColor('#F5F5F5')
        .borderRadius(8)
        .height(40)
        .width('100%')
        .onClick(this.onCancel)
    }
  }
}
