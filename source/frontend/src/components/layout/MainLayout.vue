<template>
  <el-container class="main-layout">
    <!-- Sidebar -->
    <el-aside :width="isCollapsed ? '64px' : '200px'" class="sidebar">
      <div class="logo">
        <el-icon v-if="isCollapsed"><Robot /></el-icon>
        <span v-else>AI-OA</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <template #title>工作台</template>
        </el-menu-item>
        
        <el-menu-item index="/approval">
          <el-icon><Document /></el-icon>
          <template #title>审批中心</template>
        </el-menu-item>
        
        <el-menu-item index="/reimburse">
          <el-icon><Coin /></el-icon>
          <template #title>报销管理</template>
        </el-menu-item>
        
        <el-menu-item index="/ai">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>AI助手</template>
        </el-menu-item>
        
        <el-menu-item index="/chat">
          <el-icon><Message /></el-icon>
          <template #title>企业聊天</template>
        </el-menu-item>
        
        <el-menu-item index="/report">
          <el-icon><DataLine /></el-icon>
          <template #title>智能报表</template>
        </el-menu-item>
        
        <el-menu-item index="/knowledge">
          <el-icon><Notebook /></el-icon>
          <template #title>知识库</template>
        </el-menu-item>
        
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <!-- Header -->
      <el-header class="header">
        <div class="header-left">
          <el-button text @click="toggleSidebar">
            <el-icon v-if="isCollapsed"><Expand /></el-icon>
            <el-icon v-else><Fold /></el-icon>
          </el-button>
        </div>
        
        <div class="header-right">
          <el-badge :value="3" class="header-icon">
            <el-button text><el-icon><Bell /></el-icon></el-button>
          </el-badge>
          
          <el-dropdown @command="handleUserCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.avatar" />
              <span class="username">{{ userStore.nickname }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- Main Content -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapsed = ref(false)
const activeMenu = computed(() => route.path)

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const handleUserCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      // Navigate to profile
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      await ElMessageBox.confirm('确定退出登录？', '提示')
      localStorage.removeItem('token')
      router.push('/login')
      break
  }
}
</script>

<style lang="scss" scoped>
.main-layout {
  height: 100vh;
}

.sidebar {
  background: #304156;
  transition: width 0.3s;
  
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    border-bottom: 1px solid #4a5568;
  }
  
  .sidebar-menu {
    border-right: none;
    background: transparent;
    
    :deep(.el-menu-item) {
      color: #bfcbd9;
      
      &:hover {
        background: #263445;
      }
      
      &.is-active {
        color: #409eff;
        background: #263445;
      }
    }
  }
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  
  .header-left {
    display: flex;
    align-items: center;
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .header-icon {
      margin-top: 8px;
    }
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      
      .username {
        font-size: 14px;
      }
    }
  }
}

.main-content {
  background: #f5f7fa;
  padding: 16px;
}
</style>
