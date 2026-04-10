<template>
  <div id="app">
    <el-container v-if="isAuthenticated" class="app-container">
      <el-header class="app-header">
        <div class="header-content">
          <div class="header-left">
            <el-button 
              @click="toggleSidebar" 
              class="sidebar-toggle" 
              :icon="isCollapsed ? 'Expand' : 'Fold'" 
              size="small"
              circle
            />
            <h1>电动滑板车租赁系统</h1>
          </div>
          <div class="user-info">
            <span>欢迎, {{ userInfo?.username }}</span>
          </div>
        </div>
      </el-header>
      
      <el-container>
        <!-- 桌面版导航栏 -->
        <el-aside 
          v-if="!isMobile"
          :width="sidebarWidth" 
          class="sidebar desktop-sidebar" 
          :class="{ 'sidebar-collapsed': isCollapsed }"
        >
          <el-menu
            :default-active="$route.path"
            router
            class="sidebar-menu"
            :collapse="isCollapsed"
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
          >
            <el-menu-item 
              index="/map"
              @mouseenter="(e) => showTooltip(e, '地图找车')"
              @click="hideTooltip"
            >
              <el-icon><MapLocation /></el-icon>
              <template #title>
                <span>地图找车</span>
              </template>
            </el-menu-item>
            <el-menu-item 
              index="/scooters"
              @mouseenter="(e) => showTooltip(e, '滑板车列表')"
              @click="hideTooltip"
            >
              <el-icon><Location /></el-icon>
              <template #title>
                <span>滑板车列表</span>
              </template>
            </el-menu-item>
            <el-menu-item 
              index="/feedback"
              @mouseenter="(e) => showTooltip(e, '意见反馈')"
              @click="hideTooltip"
            >
              <el-icon><ChatDotRound /></el-icon>
              <template #title>
                <span>意见反馈</span>
              </template>
            </el-menu-item>
            <el-menu-item 
              index="/profile"
              @mouseenter="(e) => showTooltip(e, '个人中心')"
              @click="hideTooltip"
            >
              <el-icon><User /></el-icon>
              <template #title>
                <span>个人中心</span>
              </template>
            </el-menu-item>
            
            <el-divider v-if="isAdmin" />
            
            <el-menu-item 
              v-if="isAdmin" 
              index="/admin/dashboard"
              @mouseenter="(e) => showTooltip(e, '管理仪表盘')"
              @click="hideTooltip"
            >
              <el-icon><DataAnalysis /></el-icon>
              <template #title>
                <span>管理仪表盘</span>
              </template>
            </el-menu-item>
            <el-menu-item 
              v-if="isAdmin" 
              index="/admin/scooters"
              @mouseenter="(e) => showTooltip(e, '滑板车管理')"
              @click="hideTooltip"
            >
              <el-icon><Setting /></el-icon>
              <template #title>
                <span>滑板车管理</span>
              </template>
            </el-menu-item>
            <el-menu-item 
              v-if="isAdmin" 
              index="/admin/locations"
              @mouseenter="(e) => showTooltip(e, '点位管理')"
              @click="hideTooltip"
            >
              <el-icon><MapLocation /></el-icon>
              <template #title>
                <span>点位管理</span>
              </template>
            </el-menu-item>
            <el-menu-item 
              v-if="isAdmin" 
              index="/admin/feedback"
              @mouseenter="(e) => showTooltip(e, '反馈管理')"
              @click="hideTooltip"
            >
              <el-icon><ChatLineRound /></el-icon>
              <template #title>
                <span>反馈管理</span>
              </template>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <!-- 手机版导航栏 -->
        <div v-if="isMobile && !isCollapsed" class="mobile-sidebar">
          <!-- 遮罩层，点击收起导航栏 -->
          <div class="mobile-sidebar-mask" @click="toggleMobileSidebar"></div>
          
          <div class="mobile-sidebar-content">
            <!-- 向上收起指示器 -->
            <div class="mobile-sidebar-handle" @click="toggleMobileSidebar">
              <div class="handle-bar"></div>
            </div>
            
            <div 
              v-for="item in filteredMobileMenuItems" 
              :key="item.index"
              class="mobile-menu-item"
              :class="{ 'active': $route.path === item.index }"
              @click="navigateTo(item.index)"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <span class="menu-text">{{ item.text }}</span>
            </div>
          </div>
        </div>
        
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
      
      <!-- Footer组件 -->
      <Footer />
    </el-container>
    
    <router-view v-else />
    
    <!-- 气泡提示 -->
    <div 
      v-if="tooltipVisible && isCollapsed" 
      class="tooltip" 
      :style="{ 
        left: tooltipPosition.x + 'px', 
        top: tooltipPosition.y + 'px' 
      }"
    >
      {{ tooltipText }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Footer from '@/components/Footer.vue'
import { 
  MapLocation, Location, User, ChatDotRound,
  DataAnalysis, Setting, ChatLineRound 
} from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const isAuthenticated = computed(() => authStore.isAuthenticated)
const userInfo = computed(() => authStore.userInfo)
const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

// 导航栏收起/展开状态
const isCollapsed = ref(false)

// 判断是否为手机端
const isMobile = ref(false)

// 桌面版气泡提示相关状态
const tooltipVisible = ref(false)
const tooltipText = ref('')
const tooltipPosition = ref({ x: 0, y: 0 })

// 手机版气泡提示相关状态
const mobileTooltipVisible = ref(false)
const mobileTooltipText = ref('')
const mobileTooltipPosition = ref({ x: 0, y: 0 })

// 手机版菜单项
const mobileMenuItems = ref([
  { index: '/map', text: '地图找车', icon: 'MapLocation' },
  { index: '/scooters', text: '滑板车列表', icon: 'Location' },
  { index: '/feedback', text: '意见反馈', icon: 'ChatDotRound' },
  { index: '/profile', text: '个人中心', icon: 'User' },
  { index: '/admin/dashboard', text: '管理仪表盘', icon: 'DataAnalysis' },
  { index: '/admin/scooters', text: '滑板车管理', icon: 'Setting' },
  { index: '/admin/locations', text: '点位管理', icon: 'MapLocation' },
  { index: '/admin/feedback', text: '反馈管理', icon: 'ChatLineRound' }
])

// 计算侧边栏宽度
const sidebarWidth = computed(() => {
  return isCollapsed.value ? '64px' : '200px'
})

// 过滤手机版菜单项，只显示有权限的菜单
const filteredMobileMenuItems = computed(() => {
  return mobileMenuItems.value.filter(item => {
    // 如果是管理员菜单，检查用户是否为管理员
    if (item.index.startsWith('/admin')) {
      return isAdmin.value
    }
    // 普通菜单对所有用户可见
    return true
  })
})

// 切换桌面版侧边栏状态
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

// 切换手机版导航栏状态
const toggleMobileSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

// 导航到指定页面
const navigateTo = (path) => {
  router.push(path)
  // 手机端导航后自动收起导航栏
  if (isMobile.value) {
    isCollapsed.value = true
  }
}

// 显示桌面版气泡提示
const showTooltip = (event, text) => {
  if (!isCollapsed.value) return // 只在收起状态下显示气泡
  
  tooltipText.value = text
  tooltipPosition.value = {
    x: event.currentTarget.getBoundingClientRect().right + 10,
    y: event.currentTarget.getBoundingClientRect().top + event.currentTarget.offsetHeight / 2
  }
  tooltipVisible.value = true
}

// 隐藏桌面版气泡提示
const hideTooltip = () => {
  tooltipVisible.value = false
}

// 响应式处理
const checkScreenSize = () => {
  if (window.innerWidth <= 768) {
    isMobile.value = true
    isCollapsed.value = true
  } else {
    isMobile.value = false
    isCollapsed.value = false
  }
}

// 监听窗口大小变化
onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
  // 添加全局点击事件监听，点击任意位置隐藏气泡
  document.addEventListener('click', hideTooltip)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
  document.removeEventListener('click', hideTooltip)
})


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

.user-info span {
  font-size: 14px;
  opacity: 0.9;
}

.sidebar {
  background: linear-gradient(180deg, #2c3e50 0%, #34495e 100%);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  transition: width 0.3s ease, transform 0.3s ease;
  overflow: hidden;
  position: relative;
}

.sidebar-collapsed {
  width: 64px !important;
}

.sidebar-menu {
  border: none;
  background: transparent !important;
  transition: all 0.3s ease;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 200px;
}

.sidebar-menu .el-menu-item {
  height: 56px;
  line-height: 56px;
  margin: 4px 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
  overflow: hidden;
  position: relative;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.1) !important;
  transform: translateX(4px);
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 收起状态下隐藏菜单文字 */
.sidebar-menu.el-menu--collapse .el-menu-item span {
  display: none;
}

.sidebar-menu.el-menu--collapse .el-menu-item .el-icon {
  margin-right: 0;
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

.tooltip::before {
  content: '';
  position: absolute;
  left: -6px;
  top: 50%;
  transform: translateY(-50%);
  border: 6px solid transparent;
  border-right-color: rgba(0, 0, 0, 0.8);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateX(-10px); }
  to { opacity: 1; transform: translateX(0); }
}

.main-content {
  background: #f8f9fa;
  padding: 24px;
  overflow-y: auto;
  transition: margin-left 0.3s ease;
}

/* 手机版导航栏样式 */
.mobile-sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  animation: mobileSlideUp 0.3s ease;
}

.mobile-sidebar-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
}

.mobile-sidebar-content {
  width: 100%;
  background: linear-gradient(180deg, #2c3e50 0%, #34495e 100%);
  border-radius: 20px 20px 0 0;
  padding: 10px 0 20px 0;
  max-height: 70vh;
  overflow-y: auto;
  position: relative;
  z-index: 1;
}

.mobile-sidebar-handle {
  display: flex;
  justify-content: center;
  padding: 10px 0;
  cursor: pointer;
}

.handle-bar {
  width: 40px;
  height: 4px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 2px;
}

@keyframes mobileSlideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.mobile-menu-item {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  color: #bfcbd9;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.mobile-menu-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

.mobile-menu-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.mobile-menu-item .el-icon {
  font-size: 24px;
  margin-right: 16px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mobile-menu-item .menu-text {
  font-size: 16px;
  font-weight: 500;
}

/* 手机版气泡提示样式 */
.mobile-tooltip {
  position: fixed;
  background: rgba(0, 0, 0, 0.9);
  color: white;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  z-index: 10001;
  white-space: nowrap;
  pointer-events: none;
  animation: mobileFadeIn 0.2s ease;
  max-width: 200px;
  text-align: center;
}

.mobile-tooltip::before {
  content: '';
  position: absolute;
  left: 50%;
  top: -6px;
  transform: translateX(-50%);
  border: 6px solid transparent;
  border-bottom-color: rgba(0, 0, 0, 0.9);
}

@keyframes mobileFadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content h1 {
    font-size: 20px;
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
  
  .user-info span {
    display: none;
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
  margin-right: 8px;
}
</style>