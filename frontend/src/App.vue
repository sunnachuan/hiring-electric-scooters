<template>
  <div id="app">
    <el-container v-if="isAuthenticated" class="app-container">
      <el-header class="app-header">
        <div class="header-content">
          <h1>电动滑板车租赁系统</h1>
          <div class="user-info">
            <span>欢迎, {{ userInfo?.username }}</span>
            <el-button @click="logout" type="danger" size="small">退出登录</el-button>
          </div>
        </div>
      </el-header>
      
      <el-container>
        <el-aside width="200px" class="sidebar">
          <el-menu
            :default-active="$route.path"
            router
            class="sidebar-menu"
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
          >
            <el-menu-item index="/scooters">
              <el-icon><Location /></el-icon>
              <span>滑板车列表</span>
            </el-menu-item>
            <el-menu-item index="/bookings">
              <el-icon><Document /></el-icon>
              <span>我的预订</span>
            </el-menu-item>
            <el-menu-item index="/feedback">
              <el-icon><ChatDotRound /></el-icon>
              <span>意见反馈</span>
            </el-menu-item>
            
            <el-divider v-if="isAdmin" />
            
            <el-menu-item v-if="isAdmin" index="/admin/dashboard">
              <el-icon><DataAnalysis /></el-icon>
              <span>管理仪表盘</span>
            </el-menu-item>
            <el-menu-item v-if="isAdmin" index="/admin/scooters">
              <el-icon><Setting /></el-icon>
              <span>滑板车管理</span>
            </el-menu-item>
            <el-menu-item v-if="isAdmin" index="/admin/feedback">
              <el-icon><ChatLineRound /></el-icon>
              <span>反馈管理</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
    
    <router-view v-else />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const isAuthenticated = computed(() => authStore.isAuthenticated)
const userInfo = computed(() => authStore.userInfo)
const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

const logout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style>
@import '@/assets/css/animations.css';
</style>

<style scoped>
.app-container {
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1000;
}

.app-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grid" width="10" height="10" patternUnits="userSpaceOnUse"><path d="M 10 0 L 0 0 0 10" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="0.5"/></pattern></defs><rect width="100" height="100" fill="url(%23grid)"/></svg>');
  opacity: 0.3;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  position: relative;
  z-index: 1;
}

.header-content h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info span {
  font-size: 14px;
  opacity: 0.9;
}

.sidebar {
  background: linear-gradient(180deg, #2c3e50 0%, #34495e 100%);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.sidebar-menu {
  border: none;
  background: transparent !important;
}

.sidebar-menu .el-menu-item {
  height: 56px;
  line-height: 56px;
  margin: 4px 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.1) !important;
  transform: translateX(4px);
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.main-content {
  background: #f8f9fa;
  padding: 24px;
  overflow-y: auto;
}

.el-divider {
  border-color: rgba(255, 255, 255, 0.2) !important;
  margin: 16px 8px !important;
}

.el-icon {
  font-size: 18px;
  margin-right: 8px;
}
</style>