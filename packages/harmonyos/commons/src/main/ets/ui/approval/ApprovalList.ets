// 审批列表页面
import router from '@ohos.router'
import { Approval, ApprovalStatus, ApprovalType, ApprovalPriority } from '../../data/model/Approval'
import { ApprovalCard } from '../components/ApprovalCard'

// 页面状态
class ApprovalListState {
  pendingApprovals: Approval[] = []
  initiatedApprovals: Approval[] = []
  processedApprovals: Approval[] = []
  loading: boolean = false
  currentTabIndex: number = 0
}

// Tab配置
const TAB_TITLES = ['待审批', '已发起', '已处理']

@Entry
@Component
struct ApprovalList {
  @State state: ApprovalListState = new ApprovalListState()
  private tabController: TabsController = new TabsController()

  aboutToAppear() {
    this.loadApprovals()
  }

  // 加载审批数据
  async loadApprovals(): Promise<void> {
    this.state.loading = true

    // 模拟待审批数据
    this.state.pendingApprovals = [
      {
        id: 'a1',
        title: '张三 - 请假申请',
        type: ApprovalType.LEAVE,
        status: ApprovalStatus.PENDING,
        priority: ApprovalPriority.NORMAL,
        applicantId: 'user_001',
        applicantName: '张三',
        applicantDept: '产品部',
        formFields: [],
        flowNodes: [],
        currentNodeId: 'n1',
        createdAt: Date.now() - 3600000,
        updatedAt: Date.now() - 3600000
      },
      {
        id: 'a2',
        title: '李四 - 加班申请',
        type: ApprovalType.OVERTIME,
        status: ApprovalStatus.PENDING,
        priority: ApprovalPriority.URGENT,
        applicantId: 'user_002',
        applicantName: '李四',
        applicantDept: '研发部',
        formFields: [],
        flowNodes: [],
        currentNodeId: 'n1',
        createdAt: Date.now() - 7200000,
        updatedAt: Date.now() - 7200000
      },
      {
        id: 'a3',
        title: '王五 - 出差申请',
        type: ApprovalType.TRIP,
        status: ApprovalStatus.PENDING,
        priority: ApprovalPriority.NORMAL,
        applicantId: 'user_003',
        applicantName: '王五',
        applicantDept: '市场部',
        formFields: [],
        flowNodes: [],
        currentNodeId: 'n1',
        createdAt: Date.now() - 10800000,
        updatedAt: Date.now() - 10800000
      }
    ]

    // 模拟我发起的审批
    this.state.initiatedApprovals = [
      {
        id: 'a4',
        title: '我的请假申请',
        type: ApprovalType.LEAVE,
        status: ApprovalStatus.APPROVED,
        priority: ApprovalPriority.NORMAL,
        applicantId: 'current_user',
        applicantName: '我',
        applicantDept: '产品部',
        formFields: [],
        flowNodes: [],
        currentNodeId: 'n2',
        createdAt: Date.now() - 86400000,
        updatedAt: Date.now() - 43200000
      },
      {
        id: 'a5',
        title: '我的报销申请',
        type: ApprovalType.EXPENSE,
        status: ApprovalStatus.PENDING,
        priority: ApprovalPriority.NORMAL,
        applicantId: 'current_user',
        applicantName: '我',
        applicantDept: '产品部',
        formFields: [],
        flowNodes: [],
        currentNodeId: 'n1',
        createdAt: Date.now() - 1800000,
        updatedAt: Date.now() - 1800000
      }
    ]

    // 模拟已处理的审批
    this.state.processedApprovals = [
      {
        id: 'a6',
        title: '赵六 - 采购申请',
        type: ApprovalType.PURCHASE,
        status: ApprovalStatus.APPROVED,
        priority: ApprovalPriority.URGENT,
        applicantId: 'user_004',
        applicantName: '赵六',
        applicantDept: '行政部',
        formFields: [],
        flowNodes: [],
        currentNodeId: 'n2',
        createdAt: Date.now() - 172800000,
        updatedAt: Date.now() - 86400000
      },
      {
        id: 'a7',
        title: '钱七 - 加班申请',
        type: ApprovalType.OVERTIME,
        status: ApprovalStatus.REJECTED,
        priority: ApprovalPriority.NORMAL,
        applicantId: 'user_005',
        applicantName: '钱七',
        applicantDept: '研发部',
        formFields: [],
        flowNodes: [],
        currentNodeId: 'n2',
        createdAt: Date.now() - 259200000,
        updatedAt: Date.now() - 172800000
      }
    ]

    this.state.loading = false
  }

  // 获取当前Tab的审批列表
  private getCurrentApprovals(): Approval[] {
    switch (this.state.currentTabIndex) {
      case 0:
        return this.state.pendingApprovals
      case 1:
        return this.state.initiatedApprovals
      case 2:
        return this.state.processedApprovals
      default:
        return []
    }
  }

  // 获取审批数量角标
  private getBadgeCount(index: number): number {
    switch (index) {
      case 0:
        return this.state.pendingApprovals.length
      case 1:
        return this.state.initiatedApprovals.length
      case 2:
        return 0
      default:
        return 0
    }
  }

  build() {
    Column() {
      // 顶部导航栏
      this.buildNavBar()

      // Tab切换
      Tabs({ barPosition: BarPosition.Start, controller: this.tabController }) {
        ForEach(TAB_TITLES, (title: string, index: number) => {
          TabContent() {
            this.buildApprovalList()
          }
          .tabBar(this.buildTabBar(title, index))
        })
      }
      .onChange((index: number) => {
        this.state.currentTabIndex = index
      })
      .barHeight(48)
      .barMode(BarMode.Fixed)
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }

  @Builder
  buildNavBar() {
    Row() {
      Text('审批')
        .fontSize(20)
        .fontWeight(FontWeight.Bold)
        .fontColor('#333333')

      Blank()

      // 发起审批按钮
      Button('+ 发起')
        .fontSize(14)
        .fontColor('#FFFFFF')
        .backgroundColor('#1890FF')
        .borderRadius(16)
        .height(32)
        .padding({ left: 16, right: 16 })
        .onClick(() => {
          router.pushUrl({ url: 'pages/ApprovalCreate' })
        })
    }
    .width('100%')
    .padding({ left: 16, right: 16, top: 12, bottom: 12 })
    .backgroundColor('#FFFFFF')
  }

  @Builder
  buildTabBar(title: string, index: number) {
    Column() {
      Row() {
        Text(title)
          .fontSize(15)
          .fontColor(this.state.currentTabIndex === index ? '#1890FF' : '#666666')

        const badge = this.getBadgeCount(index)
        if (badge > 0) {
          Text(badge.toString())
            .fontSize(10)
            .fontColor('#FFFFFF')
            .backgroundColor('#FF4D4F')
            .borderRadius(10)
            .padding({ left: 5, right: 5, top: 2, bottom: 2 })
            .margin({ left: 4 })
        }
      }
    }
    .padding({ top: 8, bottom: 8 })
    .width('100%')
  }

  @Builder
  buildApprovalList() {
    Column() {
      if (this.state.loading) {
        Blank()
        Text('加载中...')
          .fontSize(14)
          .fontColor('#999999')
        Blank()
      } else {
        const approvals = this.getCurrentApprovals()
        if (approvals.length === 0) {
          Blank()
          Column() {
            Text('📋')
              .fontSize(48)
            Text('暂无审批')
              .fontSize(14)
              .fontColor('#999999')
              .margin({ top: 12 })
          }
          Blank()
        } else {
          List() {
            ForEach(approvals, (approval: Approval) => {
              ListItem() {
                ApprovalCard({
                  approval: approval,
                  onClick: () => {
                    router.pushUrl({ url: 'pages/ApprovalDetail', params: { approvalId: approval.id } })
                  }
                })
              }
              .padding({ left: 16, right: 16, top: 8, bottom: 8 })
            })
          }
          .width('100%')
          .height('100%')
          .backgroundColor('#F5F5F5')
        }
      }
    }
    .width('100%')
    .height('100%')
  }
}
