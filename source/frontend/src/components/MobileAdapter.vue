<template>
  <div :class="['mobile-adapter', deviceClass, orientationClass]">
    <!-- 移动端顶部导航 -->
    <div v-if="isMobile" class="mobile-header">
      <div class="header-left">
        <slot name="header-left">
          <el-button 
            v-if="showBack" 
            type="text" 
            @click="goBack"
            class="back-btn"
          >
            ← 返回
          </el-button>
        </slot>
      </div>
      
      <div class="header-title">
        <slot name="header-title">{{ title }}</slot>
      </div>
      
      <div class="header-right">
        <slot name="header-right">
          <el-button 
            v-if="showMenu" 
            type="text" 
            @click="toggleMenu"
            class="menu-btn"
          >
            ☰
          </el-button>
        </slot>
      </div>
    </div>
    
    <!-- 移动端底部导航 -->
    <div v-if="isMobile && showTabBar" class="mobile-tabbar">
      <div 
        v-for="tab in tabs" 
        :key="tab.index"
        :class="['tab-item', { active: currentTab === tab.index }]"
        @click="switchTab(tab.index)"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        <span class="tab-label">{{ tab.label }}</span>
      </div>
    </div>
    
    <!-- 主内容区域 -->
    <div :class="['mobile-content', { 'with-tabbar': isMobile && showTabBar }]">
      <slot></slot>
    </div>
    
    <!-- 移动端操作面板 -->
    <div v-if="isMobile && showActionPanel" :class="['action-panel', { show: actionPanelVisible }]">
      <div class="panel-header">
        <span>{{ actionPanelTitle }}</span>
        <el-button type="text" @click="closeActionPanel">✕</el-button>
      </div>
      <div class="panel-content">
        <slot name="action-panel"></slot>
      </div>
    </div>
    
    <!-- 移动端Toast提示 -->
    <transition name="fade">
      <div v-if="toast.visible" class="mobile-toast">
        {{ toast.message }}
      </div>
    </transition>
    
    <!-- 移动端Loading -->
    <div v-if="loading" class="mobile-loading">
      <div class="loading-spinner"></div>
      <p>{{ loadingText }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'

interface Tab {
  index: number
  icon: string
  label: string
  route?: string
}

interface Toast {
  visible: boolean
  message: string
}

const props = withDefaults(defineProps<{
  title?: string
  showBack?: boolean
  showMenu?: boolean
  showTabBar?: boolean
  showActionPanel?: boolean
  actionPanelTitle?: string
  loading?: boolean
  loadingText?: string
  tabs?: Tab[]
  initialTab?: number
}>(), {
  title: '',
  showBack: true,
  showMenu: false,
  showTabBar: false,
  showActionPanel: false,
  actionPanelTitle: '操作',
  loading: false,
  loadingText: '加载中...',
  tabs: () => [],
  initialTab: 0
})

const emit = defineEmits<{
  (e: 'back'): void
  (e: 'menu-toggle'): void
  (e: 'tab-change', index: number): void
  (e: 'action-panel-close'): void
}>()

const router = useRouter()

// 响应式状态
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1200)
const windowHeight = ref(typeof window !== 'undefined' ? window.innerHeight : 800)
const currentTab = ref(props.initialTab)
const actionPanelVisible = ref(false)

// Toast状态
const toast = ref<Toast>({
  visible: false,
  message: ''
})

// 计算属性
const isMobile = computed(() => windowWidth.value <= 768)

const isTablet = computed(() => windowWidth.value > 768 && windowWidth.value <= 1024)

const isDesktop = computed(() => windowWidth.value > 1024)

const deviceClass = computed(() => {
  if (isMobile.value) return 'device-mobile'
  if (isTablet.value) return 'device-tablet'
  return 'device-desktop'
})

const orientationClass = computed(() => {
  if (typeof window !== 'undefined') {
    return window.innerHeight > window.innerWidth ? 'orientation-portrait' : 'orientation-landscape'
  }
  return 'orientation-portrait'
})

// 方法
const goBack = () => {
  if (window.history.length > 1) {
    window.history.back()
  } else {
    router.push('/')
  }
  emit('back')
}

const toggleMenu = () => {
  emit('menu-toggle')
}

const switchTab = (index: number) => {
  currentTab.value = index
  emit('tab-change', index)
  
  const tab = props.tabs[index]
  if (tab && tab.route) {
    router.push(tab.route)
  }
}

const openActionPanel = () => {
  actionPanelVisible.value = true
}

const closeActionPanel = () => {
  actionPanelVisible.value = false
  emit('action-panel-close')
}

const showToast = (message: string, duration = 2000) => {
  toast.value = {
    visible: true,
    message
  }
  
  setTimeout(() => {
    toast.value.visible = false
  }, duration)
}

const hideLoading = () => {
  // 通过v-model或emit通知父组件
}

// 窗口尺寸监听
const handleResize = () => {
  windowWidth.value = window.innerWidth
  windowHeight.value = window.innerHeight
}

// 生命周期
onMounted(() => {
  if (typeof window !== 'undefined') {
    window.addEventListener('resize', handleResize)
    window.addEventListener('orientationchange', handleResize)
    
    // 初始化
    handleResize()
  }
})

onUnmounted(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('resize', handleResize)
    window.removeEventListener('orientationchange', handleResize)
  }
})

// 暴露方法给父组件
defineExpose({
  showToast,
  openActionPanel,
  closeActionPanel,
  hideLoading
})
</script>

<style scoped>
.mobile-adapter {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 移动端样式 */
.device-mobile {
  font-size: 14px;
}

.device-mobile .mobile-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 44px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.device-mobile .header-title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.device-mobile .header-left,
.device-mobile .header-right {
  min-width: 60px;
}

.device-mobile .back-btn,
.device-mobile .menu-btn {
  color: white;
  font-size: 14px;
  padding: 8px;
}

.device-mobile .mobile-content {
  padding-top: 44px;
  min-height: calc(100vh - 44px);
  background-color: #f5f5f5;
}

.device-mobile .mobile-content.with-tabbar {
  padding-bottom: 56px;
}

.device-mobile .mobile-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: white;
  display: flex;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.device-mobile .tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  cursor: pointer;
  transition: color 0.3s;
}

.device-mobile .tab-item.active {
  color: #667eea;
}

.device-mobile .tab-icon {
  font-size: 20px;
  margin-bottom: 2px;
}

.device-mobile .tab-label {
  font-size: 10px;
}

/* 操作面板 */
.action-panel {
  position: fixed;
  bottom: -100%;
  left: 0;
  right: 0;
  height: 70%;
  background: white;
  border-radius: 16px 16px 0 0;
  z-index: 2000;
  transition: bottom 0.3s ease;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.15);
}

.action-panel.show {
  bottom: 0;
}

.action-panel .panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
  font-weight: 500;
}

.action-panel .panel-content {
  padding: 16px;
  overflow-y: auto;
  height: calc(100% - 60px);
}

/* Toast提示 */
.mobile-toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  z-index: 3000;
}

/* Loading */
.mobile-loading {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 2500;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.mobile-loading p {
  margin-top: 16px;
  color: #666;
  font-size: 14px;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 横屏模式 */
.orientation-landscape.device-mobile {
  flex-direction: row;
}

.orientation-landscape.device-mobile .mobile-header {
  width: 44px;
  height: 100%;
  flex-direction: column;
  justify-content: flex-start;
  padding: 12px 0;
}

.orientation-landscape.device-mobile .header-title {
  writing-mode: vertical-rl;
  text-orientation: mixed;
  flex: 1;
}

.orientation-landscape.device-mobile .mobile-content {
  padding-top: 0;
  padding-left: 44px;
  margin-left: 0;
}

/* 平板适配 */
.device-tablet {
  font-size: 15px;
}

.device-tablet .mobile-header {
  height: 50px;
}

.device-tablet .mobile-content {
  padding-top: 50px;
}

/* 桌面端 */
.device-desktop {
  max-width: 1200px;
  margin: 0 auto;
  background: white;
  min-height: 100vh;
}
</style>
