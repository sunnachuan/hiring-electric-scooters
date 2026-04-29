<template>
  <div id="app">
    <el-container v-if="isAuthenticated" class="app-container">
      <el-header class="app-header">
        <div class="header-content">
          <div class="header-left">
            <el-button 
              v-if="!isMobile"
              @click="toggleSidebar" 
              class="sidebar-toggle" 
              :icon="isCollapsed ? 'Expand' : 'Fold'" 
              size="small"
              circle
            />
            <h1>电动滑板车租赁系统</h1>
          </div>
          <div class="user-info">
            <div class="welcome-card">
              <div class="user-avatar">
                <el-avatar 
                  :size="36" 
                  :src="userInfo?.avatar" 
                  :style="{ background: getAvatarColor(userInfo?.username) }"
                >
                  <span class="avatar-text">{{ getAvatarInitial(userInfo?.username) }}</span>
                </el-avatar>
              </div>
              <div class="welcome-content">
                <span class="welcome-text">欢迎回来，</span>
                <span class="username">{{ userInfo?.username }}</span>
              </div>
              <div class="status-indicator">
                <div class="status-dot"></div>
              </div>
            </div>
          </div>
        </div>
      </el-header>
      
      <el-container>
        <!-- 桌面版导航栏 -->
        <el-aside 
          v-if="!isMobile"
          width="200px" 
          class="sidebar desktop-sidebar"
          :class="{ collapsed: isCollapsed }"
        >
        <!-- 手机版导航栏 -->
        <el-aside 
          v-if="isMobile"
          width="200px" 
          class="sidebar mobile-sidebar"
          :class="{ collapsed: isCollapsed }"
        >
          <el-menu
            :default-active="activeMenu"
            router
            class="sidebar-menu"
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
          >
            <el-menu-item index="/map">
              <el-icon><MapLocation /></el-icon>
              <template #title>
                <span>地图找车</span>
              </template>
            </el-menu-item>
            <el-menu-item index="/scooters">
              <el-icon><Location /></el-icon>
              <template #title>
                <span>滑板车列表</span>
              </template>
            </el-menu-item>
            <el-menu-item index="/feedback">
              <el-icon><ChatDotRound /></el-icon>
              <template #title>
                <span>意见反馈</span>
              </template>
            </el-menu-item>
            <el-menu-item index="/profile">
              <el-icon><User /></el-icon>
              <template #title>
                <span>个人中心</span>
              </template>
            </el-menu-item>
            
            <el-divider v-if="isAdmin" />
            
            <el-menu-item v-if="isAdmin" index="/admin/dashboard">
              <el-icon><DataAnalysis /></el-icon>
              <template #title>
                <span>管理仪表盘</span>
              </template>
            </el-menu-item>
            <el-menu-item v-if="isAdmin" index="/admin/scooters">
              <el-icon><Setting /></el-icon>
              <template #title>
                <span>滑板车管理</span>
              </template>
            </el-menu-item>
            <el-menu-item v-if="isAdmin" index="/admin/locations">
              <el-icon><MapLocation /></el-icon>
              <template #title>
                <span>点位管理</span>
              </template>
            </el-menu-item>
            <el-menu-item v-if="isAdmin" index="/admin/feedback">
              <el-icon><ChatLineRound /></el-icon>
              <template #title>
                <span>反馈管理</span>
              </template>
            </el-menu-item>
            <el-menu-item v-if="isAdmin" index="/admin/device-monitor">
              <el-icon><Monitor /></el-icon>
              <template #title>
                <span>设备监控</span>
              </template>
            </el-menu-item>
          </el-menu>
        </el-aside>
          <el-menu
            :default-active="activeMenu"
            router
            class="sidebar-menu"
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
          >
            <el-menu-item index="/map">
              <el-icon><MapLocation /></el-icon>
              <template #title>
                <span>地图找车</span>
              </template>
            </el-menu-item>
            <el-menu-item index="/scooters">
              <el-icon><Location /></el-icon>
              <template #title>
                <span>滑板车列表</span>
              </template>
            </el-menu-item>
            <el-menu-item index="/feedback">
              <el-icon><ChatDotRound /></el-icon>
              <template #title>
                <span>意见反馈</span>
              </template>
            </el-menu-item>
            <el-menu-item index="/profile">
              <el-icon><User /></el-icon>
              <template #title>
                <span>个人中心</span>
              </template>
            </el-menu-item>
            
            <el-divider v-if="isAdmin" />
            
            <el-menu-item v-if="isAdmin" index="/admin/dashboard">
              <el-icon><DataAnalysis /></el-icon>
              <template #title>
                <span>管理仪表盘</span>
              </template>
            </el-menu-item>
            <el-menu-item v-if="isAdmin" index="/admin/scooters">
              <el-icon><Setting /></el-icon>
              <template #title>
                <span>滑板车管理</span>
              </template>
            </el-menu-item>
            <el-menu-item v-if="isAdmin" index="/admin/locations">
              <el-icon><MapLocation /></el-icon>
              <template #title>
                <span>点位管理</span>
              </template>
            </el-menu-item>
            <el-menu-item v-if="isAdmin" index="/admin/feedback">
              <el-icon><ChatLineRound /></el-icon>
              <template #title>
                <span>反馈管理</span>
              </template>
            </el-menu-item>
            <el-menu-item v-if="isAdmin" index="/admin/device-monitor">
              <el-icon><Monitor /></el-icon>
              <template #title>
                <span>设备监控</span>
              </template>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <!-- 手机版底部导航栏 -->
        <div v-if="isMobile" class="mobile-bottom-nav">
          <div class="nav-item" :class="{ active: activeMenu === '/map' }" @click="navigateTo('/map')">
            <el-icon><MapLocation /></el-icon>
            <span>地图找车</span>
          </div>
          <div class="nav-item" :class="{ active: activeMenu === '/scooters' }" @click="navigateTo('/scooters')">
            <el-icon><Location /></el-icon>
            <span>滑板车列表</span>
          </div>
          <div class="nav-item" :class="{ active: activeMenu === '/feedback' }" @click="navigateTo('/feedback')">
            <el-icon><ChatDotRound /></el-icon>
            <span>意见反馈</span>
          </div>
          <div class="nav-item" :class="{ active: activeMenu === '/profile' }" @click="navigateTo('/profile')">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </div>
        </div>
        
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
      
      <!-- Footer组件 -->
      <Footer v-if="!isMobile" />
    </el-container>
    
    <router-view v-else />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Footer from '@/components/Footer.vue'
import { 
  MapLocation, Location, User, ChatDotRound,
  DataAnalysis, Setting, ChatLineRound, Monitor
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isAuthenticated = computed(() => authStore.isAuthenticated)
const userInfo = computed(() => authStore.userInfo)
const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

// 动态计算激活的菜单项
const activeMenu = computed(() => {
  const path = route.path
  
  // 如果是个人中心相关的页面，都激活个人中心菜单
  if (path.startsWith('/profile') || 
      path.startsWith('/my-bookings') || 
      path.startsWith('/account-settings') || 
      path.startsWith('/change-password') || 
      path.startsWith('/insurance-terms')) {
    return '/profile'
  }
  
  // 如果是管理员相关的页面，根据路径激活对应的管理员菜单
  if (path.startsWith('/admin')) {
    if (path.startsWith('/admin/dashboard')) return '/admin/dashboard'
    if (path.startsWith('/admin/scooters')) return '/admin/scooters'
    if (path.startsWith('/admin/locations')) return '/admin/locations'
    if (path.startsWith('/admin/feedback')) return '/admin/feedback'
    if (path.startsWith('/admin/device-monitor')) return '/admin/device-monitor'
    return '/admin/dashboard'
  }
  
  // 其他情况返回当前路径
  return path
})

// 导航栏收起/展开状态
const isCollapsed = ref(false)

// 判断是否为手机端
const isMobile = ref(false)

// 切换侧边栏状态
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

// 响应式处理
const checkScreenSize = () => {
  if (window.innerWidth <= 768) {
    isMobile.value = true
  } else {
    isMobile.value = false
  }
}

// 监听窗口大小变化
onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
})

// 获取用户头像颜色
const getAvatarColor = (username) => {
  if (!username) return '#667eea'
  const colors = [
    'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)'
  ]
  const index = username.charCodeAt(0) % colors.length
  return colors[index]
}

// 获取用户头像首字母
const getAvatarInitial = (username) => {
  if (!username) return 'U'
  return username.charAt(0).toUpperCase()
}

// 导航到指定页面
const navigateTo = (path) => {
  router.push(path)
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

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sidebar-toggle {
  background: rgba(255, 255, 255, 0.2) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  color: white !important;
}

.sidebar-toggle:hover {
  background: rgba(255, 255, 255, 0.3) !important;
}

.header-content h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 28px;
  color: white;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
}

.welcome-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 8px 16px;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  max-width: 280px;
  min-width: 160px;
}

/* 手机版欢迎样式 */
@media (max-width: 768px) {
  .welcome-card {
    gap: 8px;
    padding: 6px 12px;
    max-width: 200px;
    min-width: 140px;
  }
  
  /* 手机版隐藏欢迎文字 */
  .welcome-text {
    display: none;
  }
  
  .username {
    font-size: 13px;
    max-width: 80px;
  }
  
  .user-avatar .el-avatar {
    width: 30px !important;
    height: 30px !important;
  }
  
  .avatar-text {
    font-size: 14px;
  }
}

.welcome-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.welcome-card:hover::before {
  left: 100%;
}

.welcome-card:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.user-avatar {
  position: relative;
}

.user-avatar .el-avatar {
  border: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.welcome-card:hover .user-avatar .el-avatar {
  border-color: rgba(255, 255, 255, 0.6);
  transform: scale(1.05);
}

.avatar-text {
  font-weight: 600;
  color: white;
  font-size: 16px;
}

.welcome-content {
  display: flex;
  align-items: center;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.welcome-text {
  font-size: 13px;
  opacity: 0.9;
  font-weight: 400;
  white-space: nowrap;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: white;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 140px;
  letter-spacing: 0.5px;
}

.status-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #52c41a;
  border-radius: 50%;
  position: relative;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.7;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.status-dot::after {
  content: '';
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  background: #52c41a;
  border-radius: 50%;
  opacity: 0.3;
  animation: ripple 2s infinite;
}

@keyframes ripple {
  0% {
    transform: scale(1);
    opacity: 0.3;
  }
  100% {
    transform: scale(2);
    opacity: 0;
  }
}

/* 手机版底部导航栏 */
.mobile-bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  z-index: 1000;
  height: 60px;
}

.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 6px 0;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #bfcbd9;
  font-size: 12px;
  text-align: center;
  position: relative;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

.nav-item.active {
  color: #409EFF;
  background: rgba(64, 158, 255, 0.1);
}

.nav-item .el-icon {
  font-size: 22px;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 24px;
}

.nav-item span {
  font-size: 10px;
  line-height: 1.2;
  text-align: center;
  width: 100%;
  padding: 0 4px;
}

/* 修复手机版底部导航图标位置问题 */
@media (max-width: 768px) {
  .nav-item {
    padding: 4px 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }
  
  .nav-item .el-icon {
    font-size: 20px;
    margin-bottom: 3px;
    height: 22px;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
  }
  
  .nav-item span {
    font-size: 9px;
    line-height: 1.1;
    text-align: center;
    width: 100%;
  }
}

/* 为手机版调整主内容区域，避免被底部导航栏遮挡 */
@media (max-width: 768px) {
  .main-content {
    padding-bottom: 60px !important;
  }
}

.sidebar {
  background: linear-gradient(180deg, #2c3e50 0%, #34495e 100%);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  position: relative;
}

.sidebar-menu {
  border: none;
  background: transparent !important;
}

.sidebar-menu .el-menu-item {
  height: 56px;
  line-height: 56px;
  margin: 4px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.1) !important;
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 图标样式 */
.sidebar-menu .el-menu-item .el-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 20px;
  flex-shrink: 0;
}

/* 收起状态样式 */
.sidebar.collapsed {
  width: 72px !important;
}

.sidebar.collapsed .sidebar-menu .el-menu-item {
  width: 56px;
  height: 56px;
  margin: 4px auto;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.sidebar.collapsed .sidebar-menu .el-menu-item span {
  display: none;
}

.sidebar.collapsed .sidebar-menu .el-menu-item .el-icon {
  margin: 0;
  font-size: 20px;
}

.sidebar.collapsed .sidebar-menu .el-menu-item.is-active {
  width: 56px;
  height: 56px;
  border-radius: 8px;
}

/* 气泡提示样式 */
.tooltip {
  position: fixed;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 12px;
  z-index: 10000;
  white-space: nowrap;
  pointer-events: none;
  animation: fadeIn 0.2s ease;
}

.main-content {
  background: #f8f9fa;
  padding: 24px;
  overflow-y: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  /* 手机版标题居中 */
  .header-content {
    justify-content: center;
  }
  
  .header-content h1 {
    font-size: 20px;
    text-align: center;
  }
  
  .header-left {
    gap: 12px;
  }
  
  .sidebar-toggle {
    display: block !important;
  }
  
  .desktop-sidebar {
    display: none !important;
  }
  
  .main-content {
    margin-left: 0 !important;
    padding: 16px;
  }
  
  /* 手机版完全隐藏整个欢迎区域 */
  .user-info {
    display: none !important;
  }
}

@media (max-width: 480px) {
  .header-content h1 {
    font-size: 18px;
  }
  
  .main-content {
    padding: 12px;
  }
}

.el-divider {
  border-color: rgba(255, 255, 255, 0.2) !important;
  margin: 16px 8px !important;
}

.el-icon {
  font-size: 18px;
}
</style>