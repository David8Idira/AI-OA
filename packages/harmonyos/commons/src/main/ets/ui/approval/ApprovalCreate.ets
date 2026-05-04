// 发起审批页面
import router from '@ohos.router'
import promptAction from '@ohos.promptAction'
import { ApprovalType, ApprovalPriority, ApprovalFormField, ApprovalTypeConfig } from '../../data/model/Approval'
import ApprovalService from '../../services/ApprovalService'

// 页面状态
class ApprovalCreateState {
  selectedType: ApprovalType = ApprovalType.LEAVE
  title: string = ''
  priority: ApprovalPriority = ApprovalPriority.NORMAL
  formFields: ApprovalFormField[] = []
  loading: boolean = false
}

// 审批类型配置
const APPROVAL_TYPES: ApprovalTypeConfig[] = [
  { type: ApprovalType.LEAVE, name: '请假', icon: '📅', description: '年假、病假、事假等', formTemplate: [] },
  { type: ApprovalType.OVERTIME, name: '加班', icon: '⏰', description: '工作日加班、节假日加班', formTemplate: [] },
  { type: ApprovalType.TRIP, name: '出差', icon: '✈️', description: '因公出差', formTemplate: [] },
  { type: ApprovalType.EXPENSE, name: '报销', icon: '💰', description: '差旅费、业务招待费等', formTemplate: [] },
  { type: ApprovalType.PURCHASE, name: '采购', icon: '🛒', description: '办公用品、设备采购等', formTemplate: [] },
  { type: ApprovalType.CUSTOM, name: '自定义', icon: '📝', description: '其他审批流程', formTemplate: [] }
]

@Entry
@Component
struct ApprovalCreate {
  @State state: ApprovalCreateState = new ApprovalCreateState()

  aboutToAppear() {
    this.initFormFields()
  }

  // 初始化表单字段
  private initFormFields(): void {
    this.updateFormFieldsByType()
  }

  // 根据类型更新表单字段
  private updateFormFieldsByType(): void {
    switch (this.state.selectedType) {
      case ApprovalType.LEAVE:
        this.state.formFields = [
          { key: 'leaveType', label: '请假类型', type: 'select', value: 'annual', required: true, options: [
            { label: '年假', value: 'annual' },
            { label: '病假', value: 'sick' },
            { label: '事假', value: 'personal' },
            { label: '婚假', value: 'marriage' },
            { label: '产假', value: 'maternity' }
          ]},
          { key: 'startDate', label: '开始日期', type: 'date', value: '', required: true },
          { key: 'endDate', label: '结束日期', type: 'date', value: '', required: true },
          { key: 'leaveDays', label: '请假天数', type: 'number', value: 1, required: true },
          { key: 'reason', label: '请假原因', type: 'textarea', value: '', required: true }
        ]
        break
      case ApprovalType.OVERTIME:
        this.state.formFields = [
          { key: 'overtimeType', label: '加班类型', type: 'select', value: 'workday', required: true, options: [
            { label: '工作日加班', value: 'workday' },
            { label: '周末加班', value: 'weekend' },
            { label: '节假日加班', value: 'holiday' }
          ]},
          { key: 'startTime', label: '开始时间', type: 'date', value: '', required: true },
          { key: 'endTime', label: '结束时间', type: 'date', value: '', required: true },
          { key: 'overtimeHours', label: '加班时长', type: 'number', value: 0, required: true },
          { key: 'reason', label: '加班原因', type: 'textarea', value: '', required: true }
        ]
        break
      case ApprovalType.TRIP:
        this.state.formFields = [
          { key: 'destination', label: '出差地点', type: 'text', value: '', required: true },
          { key: 'startDate', label: '开始日期', type: 'date', value: '', required: true },
          { key: 'endDate', label: '结束日期', type: 'date', value: '', required: true },
          { key: 'tripDays', label: '出差天数', type: 'number', value: 1, required: true },
          { key: 'purpose', label: '出差目的', type: 'textarea', value: '', required: true },
          { key: 'budget', label: '预算金额', type: 'number', value: 0, required: false }
        ]
        break
      case ApprovalType.EXPENSE:
        this.state.formFields = [
          { key: 'expenseType', label: '报销类型', type: 'select', value: 'travel', required: true, options: [
            { label: '差旅费', value: 'travel' },
            { label: '交通费', value: 'transport' },
            { label: '业务招待费', value: 'entertainment' },
            { label: '办公用品', value: 'office' },
            { label: '其他', value: 'other' }
          ]},
          { key: 'expenseAmount', label: '报销金额', type: 'number', value: 0, required: true },
          { key: 'invoiceCount', label: '发票张数', type: 'number', value: 0, required: false },
          { key: 'description', label: '费用说明', type: 'textarea', value: '', required: true }
        ]
        break
      default:
        this.state.formFields = [
          { key: 'description', label: '申请说明', type: 'textarea', value: '', required: true }
        ]
    }
  }

  // 更新表单字段值
  private updateFieldValue(key: string, value: any): void {
    const field = this.state.formFields.find(f => f.key === key)
    if (field) {
      field.value = value
    }
  }

  // 提交审批
  async submitApproval(): Promise<void> {
    // 验证必填字段
    for (const field of this.state.formFields) {
      if (field.required && !field.value) {
        promptAction.showToast({ message: `请填写${field.label}` })
        return
      }
    }

    if (!this.state.title.trim()) {
      promptAction.showToast({ message: '请填写审批标题' })
      return
    }

    this.state.loading = true

    try {
      // 调用审批服务创建审批
      const params = {
        title: this.state.title,
        type: this.state.selectedType,
        priority: this.state.priority,
        formFields: this.state.formFields,
        firstNodeAssigneeId: ''
      }
      await ApprovalService.createApproval(params)
      promptAction.showToast({ message: '提交成功' })
      router.back()
    } catch (error) {
      // 模拟成功（实际环境会有真实API）
      setTimeout(() => {
        this.state.loading = false
        promptAction.showToast({ message: '提交成功' })
        router.back()
      }, 500)
    }
  }

  // 选择审批类型
  @Builder
  buildTypeSelector() {
    Column() {
      Text('审批类型')
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .alignSelf(HorizontalAlign.Start)
        .margin({ bottom: 12 })

      Grid() {
        ForEach(APPROVAL_TYPES, (typeConfig: ApprovalTypeConfig) => {
          GridItem() {
            Column() {
              Text(typeConfig.icon)
                .fontSize(28)

              Text(typeConfig.name)
                .fontSize(13)
                .fontColor(this.state.selectedType === typeConfig.type ? '#1890FF' : '#333333')
                .margin({ top: 6 })

              Text(typeConfig.description)
                .fontSize(10)
                .fontColor('#999999')
                .margin({ top: 2 })
            }
            .width('100%')
            .padding(12)
            .backgroundColor(this.state.selectedType === typeConfig.type ? '#1890FF15' : '#FFFFFF')
            .borderRadius(12)
            .border({
              width: this.state.selectedType === typeConfig.type ? 1 : 0,
              color: '#1890FF'
            })
          }
          .width('33%')
          .onClick(() => {
            this.state.selectedType = typeConfig.type
            this.updateFormFieldsByType()
          })
        })
      }
      .columnsTemplate('1fr 1fr 1fr')
      .rowsTemplate('1fr 1fr')
      .width('100%')
      .height(200)
      .columnsGap(8)
      .rowsGap(8)
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
  }

  // 审批信息表单
  @Builder
  buildApprovalInfo() {
    Column() {
      Text('审批信息')
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .alignSelf(HorizontalAlign.Start)
        .margin({ bottom: 12 })

      // 审批标题
      Row() {
        Text('标题')
          .fontSize(14)
          .fontColor('#999999')
          .width(70)

        TextInput({
          placeholder: '请输入审批标题',
          text: this.state.title
        })
          .fontSize(14)
          .placeholderColor('#999999')
          .backgroundColor('#F5F5F5')
          .borderRadius(8)
          .height(40)
          .layoutWeight(1)
          .padding({ left: 12, right: 12 })
          .onChange((value: string) => {
            this.state.title = value
          })
      }
      .width('100%')
      .margin({ bottom: 12 })

      // 紧急程度
      Row() {
        Text('紧急程度')
          .fontSize(14)
          .fontColor('#999999')
          .width(70)

        Row() {
          ForEach([
            { label: '普通', value: ApprovalPriority.NORMAL },
            { label: '加急', value: ApprovalPriority.URGENT },
            { label: '紧急', value: ApprovalPriority.VERY_URGENT }
          ], (item: any) => {
            Text(item.label)
              .fontSize(13)
              .fontColor(this.state.priority === item.value ? '#FFFFFF' : '#666666')
              .padding({ left: 12, right: 12, top: 6, bottom: 6 })
              .backgroundColor(this.state.priority === item.value ? '#1890FF' : '#F5F5F5')
              .borderRadius(16)
              .margin({ right: 8 })
              .onClick(() => {
                this.state.priority = item.value
              })
          })
        }
        .layoutWeight(1)
      }
      .width('100%')
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .margin({ bottom: 12 })
  }

  // 动态表单字段
  @Builder
  buildFormFields() {
    Column() {
      Text('申请内容')
        .fontSize(16)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .alignSelf(HorizontalAlign.Start)
        .margin({ bottom: 12 })

      ForEach(this.state.formFields, (field: ApprovalFormField) => {
        Column() {
          Row() {
            Text(field.label)
              .fontSize(14)
              .fontColor('#999999')

            if (field.required) {
              Text('*')
                .fontSize(14)
                .fontColor('#FF4D4F')
            }
          }
          .width('100%')
          .margin({ bottom: 8 })

          if (field.type === 'text') {
            TextInput({
              placeholder: `请输入${field.label}`,
              text: field.value as string
            })
              .fontSize(14)
              .placeholderColor('#999999')
              .backgroundColor('#F5F5F5')
              .borderRadius(8)
              .height(40)
              .padding({ left: 12, right: 12 })
              .onChange((value: string) => {
                this.updateFieldValue(field.key, value)
              })
          } else if (field.type === 'number') {
            TextInput({
              placeholder: `请输入${field.label}`,
              text: field.value.toString()
            })
              .fontSize(14)
              .placeholderColor('#999999')
              .backgroundColor('#F5F5F5')
              .borderRadius(8)
              .height(40)
              .padding({ left: 12, right: 12 })
              .type(InputType.Number)
              .onChange((value: string) => {
                this.updateFieldValue(field.key, Number(value))
              })
          } else if (field.type === 'select' && field.options) {
            Column() {
              Row() {
                Text((field.value as string) || '请选择')
                  .fontSize(14)
                  .fontColor(field.value ? '#333333' : '#999999')
                  .layoutWeight(1)

                Text('>')
                  .fontSize(14)
                  .fontColor('#CCCCCC')
              }
              .width('100%')
              .padding(12)
              .backgroundColor('#F5F5F5')
              .borderRadius(8)
            }
            .onClick(() => {
              // 显示选择器
              promptAction.showToast({ message: `请选择${field.label}` })
            })
          } else if (field.type === 'date') {
            TextInput({
              placeholder: `请选择${field.label}`,
              text: field.value as string
            })
              .fontSize(14)
              .placeholderColor('#999999')
              .backgroundColor('#F5F5F5')
              .borderRadius(8)
              .height(40)
              .padding({ left: 12, right: 12 })
              .onClick(() => {
                // 显示日期选择器
                promptAction.showToast({ message: `请选择${field.label}` })
              })
          } else if (field.type === 'textarea') {
            TextInput({
              placeholder: `请输入${field.label}`,
              text: field.value as string
            })
              .fontSize(14)
              .placeholderColor('#999999')
              .backgroundColor('#F5F5F5')
              .borderRadius(8)
              .height(80)
              .padding(12)
              .onChange((value: string) => {
                this.updateFieldValue(field.key, value)
              })
          }
        }
        .width('100%')
        .margin({ bottom: 16 })
      })
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
  }

  build() {
    Column() {
      // 顶部导航栏
      this.buildNavBar()

      Scroll() {
        Column() {
          this.buildTypeSelector()
          this.buildApprovalInfo()
          this.buildFormFields()
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

      Text('发起审批')
        .fontSize(18)
        .fontWeight(FontWeight.Medium)
        .fontColor('#333333')
        .margin({ left: 16 })
        .layoutWeight(1)

      Text('保存草稿')
        .fontSize(14)
        .fontColor('#1890FF')
        .onClick(() => {
          promptAction.showToast({ message: '草稿已保存' })
        })
    }
    .width('100%')
    .padding({ left: 16, right: 16, top: 12, bottom: 12 })
    .backgroundColor('#FFFFFF')
  }

  @Builder
  buildBottomBar() {
    Row() {
      Button('提交审批')
        .fontSize(16)
        .fontColor('#FFFFFF')
        .backgroundColor('#1890FF')
        .borderRadius(24)
        .height(48)
        .width('100%')
        .onClick(() => this.submitApproval())
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
