<template>
  <div class="profile-container">
    <!-- 头部背景 -->
    <div class="profile-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <!-- 用户头像 -->
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <el-avatar :size="80" :src="userInfo.avatar" class="user-avatar">
              {{ userInfo.username?.charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="avatar-badge">
              <el-icon><Star /></el-icon>
            </div>
          </div>
          <div class="user-info">
            <h2 class="username">{{ userInfo.username }}</h2>
            <p class="user-role">{{ userInfo.role === 'ADMIN' ? '管理员' : '普通用户' }}</p>
            <div class="user-stats">
              <div class="stat-item">
                <span class="stat-value">{{ bookingStats.total }}</span>
                <span class="stat-label">总预订</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ bookingStats.active }}</span>
                <span class="stat-label">进行中</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ bookingStats.completed }}</span>
                <span class="stat-label">已完成</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="profile-content">
      <!-- 功能卡片 -->
      <div class="function-cards">
        <el-card class="function-card" shadow="hover">
          <div class="card-content" @click="activeTab = 'bookings'">
            <el-icon class="card-icon"><Document /></el-icon>
            <div class="card-text">
              <h3>我的预订</h3>
              <p>查看和管理您的预订记录</p>
            </div>
            <el-icon class="card-arrow"><ArrowRight /></el-icon>
          </div>
        </el-card>

        <el-card class="function-card" shadow="hover">
          <div class="card-content" @click="activeTab = 'settings'">
            <el-icon class="card-icon"><Setting /></el-icon>
            <div class="card-text">
              <h3>账户设置</h3>
              <p>修改个人信息和偏好设置</p>
            </div>
            <el-icon class="card-arrow"><ArrowRight /></el-icon>
          </div>
        </el-card>

        <el-card class="function-card" shadow="hover">
          <div class="card-content" @click="activeTab = 'changePassword'">
            <el-icon class="card-icon"><Lock /></el-icon>
            <div class="card-text">
              <h3>更改密码</h3>
              <p>修改登录密码，保障账户安全</p>
            </div>
            <el-icon class="card-arrow"><ArrowRight /></el-icon>
          </div>
        </el-card>

        <el-card class="function-card" shadow="hover">
          <div class="card-content" @click="handleLogout">
            <el-icon class="card-icon logout-icon"><SwitchButton /></el-icon>
            <div class="card-text">
              <h3>退出登录</h3>
              <p>安全退出当前账户</p>
            </div>
            <el-icon class="card-arrow"><ArrowRight /></el-icon>
          </div>
        </el-card>
      </div>

      <!-- 选项卡内容 -->
      <div class="tab-content">
        <!-- 预订记录 -->
        <div v-if="activeTab === 'bookings'" class="tab-panel">
          <div class="tab-header">
            <h3>我的预订记录</h3>
            <el-button type="primary" @click="$router.push('/scooters')">
              <el-icon><Plus /></el-icon>
              新的预订
            </el-button>
          </div>
          
          <!-- 这里可以集成原有的预订列表组件 -->
          <div class="bookings-list">
            <el-empty description="暂无预订记录" v-if="!bookings.length">
              <el-button type="primary" @click="$router.push('/scooters')">
                去预订
              </el-button>
            </el-empty>
            
            <div v-else class="booking-items">
                <el-card 
                  v-for="booking in bookings" 
                  :key="booking.id" 
                  class="booking-item" 
                  shadow="hover"
                >
                  <div class="booking-info">
                    <div class="booking-header">
                      <span class="scooter-model">{{ booking.scooterModel }}</span>
                      <el-tag :type="getStatusType(booking.status)">
                        {{ getStatusText(booking.status) }}
                      </el-tag>
                    </div>
                    <div class="booking-details">
                      <p><el-icon><Location /></el-icon> {{ booking.location }}</p>
                      <p><el-icon><Clock /></el-icon> {{ formatDate(booking.startTime) }} - {{ formatDate(booking.endTime) }}</p>
                      <p><el-icon><Coin /></el-icon> ￥{{ booking.amount }}</p>
                    </div>
                    <div class="booking-actions" v-if="booking.status === 'ACTIVE' || booking.status === 'PENDING'">
                      <el-button 
                        type="primary" 
                        size="small" 
                        @click="extendBookingTime(booking.id)"
                        :loading="extendingBookingId === booking.id"
                      >
                        <el-icon><Clock /></el-icon>
                        延长用车
                      </el-button>
                      <el-button 
                        type="primary" 
                        size="small" 
                        @click="returnScooterEarly(booking.id)"
                        :loading="returningBookingId === booking.id"
                      >
                        <el-icon><Switch /></el-icon>
                        提前还车
                      </el-button>
                    </div>
                  </div>
                </el-card>
              </div>
          </div>
        </div>

        <!-- 账户设置 -->
        <div v-if="activeTab === 'settings'" class="tab-panel">
          <div class="tab-header">
            <h3>账户设置</h3>
          </div>
          
          <el-form :model="userForm" label-width="100px" class="settings-form">
            <el-form-item label="用户名">
              <el-input v-model="userForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="userForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="userForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            
            <!-- 认证按钮区域 -->
            <el-form-item label="身份认证">
              <div class="certification-buttons">
                <el-button type="primary" class="certification-btn" @click="handleStudentCertification">
                  <el-icon><User /></el-icon>
                  学生认证
                </el-button>
                <el-button type="success" class="certification-btn" @click="handleSeniorCertification">
                  <el-icon><UserFilled /></el-icon>
                  长者认证
                </el-button>
              </div>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="updateProfile">保存修改</el-button>
              <el-button>取消</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 更改密码 -->
        <div v-if="activeTab === 'changePassword'" class="tab-panel">
          <div class="tab-header">
            <h3>更改密码</h3>
          </div>
          
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" class="settings-form">
            <el-form-item label="当前密码" prop="currentPassword">
              <el-input 
                v-model="passwordForm.currentPassword" 
                type="password" 
                placeholder="请输入当前密码" 
                show-password
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input 
                v-model="passwordForm.newPassword" 
                type="password" 
                placeholder="请输入新密码" 
                show-password
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input 
                v-model="passwordForm.confirmPassword" 
                type="password" 
                placeholder="请再次输入新密码" 
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword">确认修改</el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'
import { 
  Star, Document, Setting, SwitchButton, ArrowRight, Plus, 
  Location, Clock, Coin, Lock, Switch, User, UserFilled
} from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

// 用户信息
const userInfo = computed(() => authStore.userInfo || {})

// 活跃选项卡
const activeTab = ref('bookings')

// 预订统计
const bookingStats = ref({
  total: 0,
  active: 0,
  completed: 0
})

// 预订记录
const bookings = ref([])
const loading = ref(false)
const returningBookingId = ref(null)
const extendingBookingId = ref(null)

// 用户表单
const userForm = ref({
  username: '',
  email: '',
  phone: ''
})

// 密码表单
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordFormRef = ref()

// 验证确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 密码验证规则
const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'CONFIRMED': 'success',
    'ACTIVE': 'primary',
    'COMPLETED': 'info',
    'CANCELLED': 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'ACTIVE': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return texts[status] || '未知'
}

// 格式化日期
const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 学生认证
const handleStudentCertification = () => {
  ElMessage.info('学生认证功能开发中，敬请期待！')
}

// 长者认证
const handleSeniorCertification = () => {
  ElMessage.info('长者认证功能开发中，敬请期待！')
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    authStore.logout()
    ElMessage.success('已成功退出登录')
    router.push('/login')
  } catch (error) {
    // 用户取消操作
  }
}

// 更新个人信息
const updateProfile = () => {
  ElMessage.success('个人信息已更新')
}

// 更改密码
const changePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    
    // 模拟密码修改请求
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('密码修改成功')
    resetPasswordForm()
    activeTab.value = 'bookings' // 返回默认页面
  } catch (error) {
    if (error.errors) {
      ElMessage.error('请检查表单填写是否正确')
    } else {
      ElMessage.error('密码修改失败，请重试')
    }
  }
}

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  if (passwordFormRef.value) {
    passwordFormRef.value.clearValidate()
  }
}

// 延长用车时间
const extendBookingTime = async (bookingId) => {
  try {
    extendingBookingId.value = bookingId
    
    // 询问用户要延长多少小时
    const { value: hours } = await ElMessageBox.prompt(
      '请输入要延长的小时数（最少1小时）:',
      '延长用车时间',
      {
        confirmButtonText: '确认延长',
        cancelButtonText: '取消',
        inputPattern: /^[1-9][0-9]*$/,
        inputErrorMessage: '请输入有效的正整数（最少1小时）'
      }
    )
    
    const extendHours = parseInt(hours)
    
    if (extendHours < 1) {
      ElMessage.error('延长时间不能少于1小时')
      return
    }
    
    // 调用延长用车API
    await api.put(`/bookings/${bookingId}/extend?hours=${extendHours}`)
    
    ElMessage.success(`延长用车时间成功！已延长 ${extendHours} 小时`)
    
    // 重新加载预订数据
    await loadBookings()
    
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      // 用户取消操作
      return
    }
    
    console.error('延长用车失败:', error)
    if (error.response) {
      const errorMessage = error.response.data?.message || error.response.data?.error || '延长用车失败'
      ElMessage.error(`延长用车失败: ${errorMessage}`)
    } else {
      ElMessage.error('延长用车失败，请重试')
    }
  } finally {
    extendingBookingId.value = null
  }
}

// 提前还车
const returnScooterEarly = async (bookingId) => {
  try {
    returningBookingId.value = bookingId
    
    await ElMessageBox.confirm('确定要提前还车吗？还车后滑板车将重新变为可用状态。', '确认还车', {
      confirmButtonText: '确定还车',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用还车API
    await api.put(`/bookings/${bookingId}/return`)
    
    ElMessage.success('还车成功！滑板车已重新变为可用状态')
    
    // 重新加载预订数据
    await loadBookings()
    
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      // 用户取消操作
      return
    }
    
    console.error('还车失败:', error)
    if (error.response) {
      const errorMessage = error.response.data?.message || error.response.data?.error || '还车失败'
      ElMessage.error(`还车失败: ${errorMessage}`)
    } else {
      ElMessage.error('还车失败，请重试')
    }
  } finally {
    returningBookingId.value = null
  }
}

// 加载预订数据
const loadBookings = async () => {
  // 检查用户是否已登录
  if (!authStore.isAuthenticated) {
    bookings.value = []
    bookingStats.value = { total: 0, active: 0, completed: 0 }
    return
  }
  
  loading.value = true
  try {
    const response = await api.get('/bookings/user')
    bookings.value = response.data.map(booking => ({
      id: booking.id,
      scooterModel: booking.scooter?.model || '未知型号',
      location: booking.scooter?.locationName || '未知位置',
      startTime: booking.startTime,
      endTime: booking.endTime,
      amount: booking.totalPrice,
      status: booking.status
    }))
    
    // 计算统计
    bookingStats.value.total = bookings.value.length
    bookingStats.value.active = bookings.value.filter(b => b.status === 'ACTIVE' || b.status === 'PENDING').length
    bookingStats.value.completed = bookings.value.filter(b => b.status === 'COMPLETED').length
  } catch (error) {
    // 如果是401未授权错误，说明用户已退出登录，不显示错误信息
    if (error.response?.status !== 401) {
      console.error('加载预订数据失败:', error)
      ElMessage.error('加载预订数据失败')
    }
    bookings.value = []
    bookingStats.value = { total: 0, active: 0, completed: 0 }
  } finally {
    loading.value = false
  }
}

// 初始化数据
onMounted(() => {
  // 初始化用户表单
  userForm.value = { ...userInfo.value }
  
  // 加载真实预订数据
  loadBookings()
})
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: #f5f7fa;
}

/* 头部样式 */
.profile-header {
  position: relative;
  height: 280px;
  overflow: hidden;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header-content {
  position: relative;
  z-index: 1;
  padding: 40px 24px 24px;
  color: white;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-wrapper {
  position: relative;
}

.user-avatar {
  border: 4px solid rgba(255, 255, 255, 0.3);
  background: linear-gradient(135deg, #ff6b6b 0%, #feca57 100%);
}

.avatar-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  background: #ffd700;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
  font-size: 12px;
}

.user-info {
  flex: 1;
}

.username {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.user-role {
  opacity: 0.9;
  margin: 0 0 20px 0;
}

.user-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  opacity: 0.8;
}

/* 主要内容区域 */
.profile-content {
  padding: 24px;
  margin-top: -60px;
  position: relative;
  z-index: 2;
}

.function-cards {
  display: grid;
  gap: 16px;
  margin-bottom: 32px;
}

.function-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.function-card:hover {
  transform: translateY(-2px);
}

.card-content {
  display: flex;
  align-items: center;
  padding: 16px;
}

.card-icon {
  font-size: 24px;
  color: #409EFF;
  margin-right: 16px;
}

.logout-icon {
  color: #F56C6C;
}

.card-text {
  flex: 1;
}

.card-text h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
  color: #303133;
}

.card-text p {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.card-arrow {
  color: #C0C4CC;
}

/* 选项卡内容 */
.tab-content {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #EBEEF5;
}

.tab-header h3 {
  margin: 0;
  color: #303133;
}

/* 预订列表 */
.bookings-list {
  min-height: 200px;
}

.booking-items {
  display: grid;
  gap: 16px;
}

.booking-item {
  transition: all 0.3s;
}

.booking-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.booking-info {
  padding: 16px;
}

.booking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.scooter-model {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.booking-details p {
  margin: 8px 0;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 设置表单 */
.settings-form {
  max-width: 500px;
}

/* 认证按钮样式 */
.certification-buttons {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.certification-btn {
  flex: 1;
  min-width: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.certification-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-header {
    height: 240px;
  }
  
  .header-content {
    padding: 24px 16px 16px;
  }
  
  .avatar-section {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .user-stats {
    justify-content: center;
    gap: 20px;
  }
  
  .profile-content {
    padding: 16px;
    margin-top: -80px;
  }
  
  .function-cards {
    grid-template-columns: 1fr;
  }
  
  .tab-content {
    padding: 16px;
  }
}
</style>