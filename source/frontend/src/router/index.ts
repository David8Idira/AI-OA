import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

// Basic layout
import Layout from '@/components/layout/MainLayout.vue'

// Public routes
const loginRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录', public: true }
  }
]

// Protected routes
const protectedRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/Home.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'approval',
        name: 'Approval',
        component: () => import('@/views/approval/List.vue'),
        meta: { title: '审批中心' }
      },
      {
        path: 'approval/detail/:id',
        name: 'ApprovalDetail',
        component: () => import('@/views/approval/Detail.vue'),
        meta: { title: '审批详情' }
      },
      {
        path: 'approval/form',
        name: 'ApprovalForm',
        component: () => import('@/views/approval/Form.vue'),
        meta: { title: '新建审批' }
      },
      {
        path: 'approval/form/:id',
        name: 'ApprovalFormEdit',
        component: () => import('@/views/approval/Form.vue'),
        meta: { title: '编辑审批' }
      },
      {
        path: 'reimburse',
        name: 'Reimburse',
        component: () => import('@/views/reimburse/List.vue'),
        meta: { title: '报销管理' }
      },
      {
        path: 'ai',
        name: 'AI',
        component: () => import('@/views/ai/Chat.vue'),
        meta: { title: 'AI助手' }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/chat/List.vue'),
        meta: { title: '企业聊天' }
      },
      {
        path: 'chat/:id',
        name: 'ChatDetail',
        component: () => import('@/views/chat/Chat.vue'),
        meta: { title: '聊天详情' }
      },
      {
        path: 'report',
        name: 'Report',
        component: () => import('@/views/report/List.vue'),
        meta: { title: '智能报表' }
      },
      {
        path: 'knowledge',
        name: 'Knowledge',
        component: () => import('@/views/knowledge/Index.vue'),
        meta: { title: '知识库' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/Settings.vue'),
        meta: { title: '系统设置' }
      },
      {
        path: 'attendance',
        name: 'Attendance',
        component: () => import('@/views/attendance/Checkin.vue'),
        meta: { title: '考勤打卡' }
      },
      {
        path: 'attendance/records',
        name: 'AttendanceRecords',
        component: () => import('@/views/attendance/Records.vue'),
        meta: { title: '考勤记录' }
      },
      {
        path: 'attendance/statistics',
        name: 'AttendanceStatistics',
        component: () => import('@/views/attendance/Statistics.vue'),
        meta: { title: '考勤统计' }
      },
      {
        path: 'attendance/rules',
        name: 'AttendanceRules',
        component: () => import('@/views/attendance/Rules.vue'),
        meta: { title: '考勤规则' }
      },
      {
        path: 'attendance/groups',
        name: 'AttendanceGroups',
        component: () => import('@/views/attendance/Groups.vue'),
        meta: { title: '考勤组' }
      },
      {
        path: 'attendance/exceptions',
        name: 'AttendanceExceptions',
        component: () => import('@/views/attendance/Exceptions.vue'),
        meta: { title: '考勤异常' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: [...loginRoutes, ...protectedRoutes]
})

// Navigation guard
router.beforeEach((to, from, next) => {
  NProgress.start()
  
  const token = localStorage.getItem('token')
  
  if (to.meta.public) {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
