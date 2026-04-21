<template>
  <el-config-provider :locale="zhCn">
    <router-view />
    <AiSidebar />
  </el-config-provider>
</template>

<script setup lang="ts">
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import AiSidebar from './components/AiSidebar.vue'
</script>

<style>
#app {
  width: 100%;
  height: 100%;
}
</style>
