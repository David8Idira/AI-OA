<template>
  <div class="user-center-container">
    <el-tabs v-model="activeTab" class="user-tabs">
      <!-- 个人信息 -->
      <el-tab-pane label="个人信息" name="profile">
        <el-card>
          <el-form :model="profileForm" label-width="100px" style="max-width: 600px">
            <el-form-item label="头像">
              <el-avatar :size="80" :src="profileForm.avatar">
                {{ profileForm.nickname?.charAt(0) }}
              </el-avatar>
              <el-button size="small" style="margin-left: 16px">更换头像</el-button>
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profileForm.nickname" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.mobile" />
            </el-form-item>
            <el-form-item label="部门">
              <el-input v-model="profileForm.deptName" disabled />
            </el-form-item>
            <el-form-item label="职位">
              <el-input v-model="profileForm.position" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 修改密码 -->
      <el-tab-pane label="修改密码" name="password">
        <el-card>
          <el-form :model="passwordForm" label-width="100px" style="max-width: 500px">
            <el-form-item label="当前密码" required>
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" required>
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" required>
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 通知设置 -->
      <el-tab-pane label="通知设置" name="notifications">
        <el-card>
          <el-form label-width="120px" style="max-width: 500px">
            <el-form-item label="邮件通知">
              <el-switch v-model="notifSettings.email" />
            </el-form-item>
            <el-form-item label="站内消息">
              <el-switch v-model="notifSettings.internal" />
            </el-form-item>
            <el-form-item label="审批提醒">
              <el-switch v-model="notifSettings.approval" />
            </el-form-item>
            <el-form-item label="报销状态变更">
              <el-switch v-model="notifSettings.reimburse" />
            </el-form-item>
            <el-form-item label="系统公告">
              <el-switch v-model="notifSettings.announcement" />
            </el-form-item>
            <el-form-item label="免打扰时段">
              <el-time-picker
                v-model="notifSettings.dndRange"
                is-range
                format="HH:mm"
                value-format="HH:mm"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveNotifications">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const activeTab = ref('profile')

const profileForm = reactive({
  username: userStore.username || 'admin',
  nickname: userStore.nickname || '管理员',
  email: 'admin@example.com',
  mobile: '13800138000',
  avatar: '',
  deptName: '研发部',
  position: '技术经理'
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const notifSettings = reactive({
  email: true,
  internal: true,
  approval: true,
  reimburse: true,
  announcement: true,
  dndRange: ['22:00', '07:00']
})

const saveProfile = () => ElMessage.success('个人信息已保存')
const saveNotifications = () => ElMessage.success('通知设置已保存')

const changePassword = () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('请填写所有必填项')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  if (passwordForm.newPassword.length < 8) {
    ElMessage.error('密码长度不能少于8位')
    return
  }
  ElMessage.success('密码修改成功')
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}
</script>

<style lang="scss" scoped>
.user-center-container {
  padding: 20px;

  .user-tabs {
    max-width: 800px;
  }
}
</style>
