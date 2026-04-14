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
          <div class="card-content" @click="$router.push('/my-bookings')">
            <el-icon class="card-icon"><Document /></el-icon>
            <div class="card-text">
              <h3>我的预订</h3>
              <p>查看和管理您的预订记录</p>
            </div>
            <el-icon class="card-arrow"><ArrowRight /></el-icon>
          </div>
        </el-card>

        <el-card class="function-card" shadow="hover">
          <div class="card-content" @click="$router.push('/account-settings')">
            <el-icon class="card-icon"><Setting /></el-icon>
            <div class="card-text">
              <h3>账户设置</h3>
              <p>修改个人信息和偏好设置</p>
            </div>
            <el-icon class="card-arrow"><ArrowRight /></el-icon>
          </div>
        </el-card>

        <el-card class="function-card" shadow="hover">
          <div class="card-content" @click="$router.push('/change-password')">
            <el-icon class="card-icon"><Lock /></el-icon>
            <div class="card-text">
              <h3>更改密码</h3>
              <p>修改登录密码，保障账户安全</p>
            </div>
            <el-icon class="card-arrow"><ArrowRight /></el-icon>
          </div>
        </el-card>

        <el-card class="function-card" shadow="hover">
          <div class="card-content" @click="$router.push('/insurance-terms')">
            <el-icon class="card-icon"><Document /></el-icon>
            <div class="card-text">
              <h3>保险与条款</h3>
              <p>查看交通保险和免责条款</p>
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

      <!-- 功能卡片区域 -->
      <div class="function-cards">
        <!-- 功能卡片已经移动到上面 -->
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

// 重要免责条款
const importantTerms = [
  '用户未满16周岁或未取得相应驾驶资格时发生的事故',
  '用户酒后驾驶、吸毒后驾驶或疲劳驾驶导致的事故',
  '用户违反交通规则（如闯红灯、逆行等）造成的事故',
  '用户故意损坏车辆或进行危险操作导致的损失',
  '用户未在规定区域内使用车辆发生的事故',
  '用户未按规定佩戴安全护具造成的人身伤害',
  '不可抗力因素（如自然灾害、战争等）导致的损失'
]

// 超时处理步骤
const overtimeSteps = [
  {
    title: '超时15分钟',
    description: '系统自动发送短信/邮件提醒用户及时还车'
  },
  {
    title: '超时30分钟', 
    description: '按原费率1.5倍自动续费计费'
  },
  {
    title: '超时1小时',
    description: '按原费率2倍计费，客服人员电话联系用户'
  },
  {
    title: '超时2小时以上',
    description: '按原费率3倍计费，可能暂停账户使用，启动车辆定位'
  }
]
import api from '@/api'
import { 
  Star, StarFilled, Document, Setting, SwitchButton, ArrowRight, Plus, 
  Location, Clock, Coin, Lock, Switch, User, UserFilled, Edit, Delete
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

const downloadTerms = () => {
  ElMessage.info('条款文档下载功能即将上线')
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

// 提前还车（增强版：包含损坏检查）
const returnScooterEarly = async (bookingId) => {
  try {
    returningBookingId.value = bookingId
    
    // 检查是否已有损坏记录
    const hasDamage = await checkExistingDamage(bookingId)
    if (hasDamage) {
      ElMessage.warning('该预订已有损坏记录，请等待管理员处理后再尝试还车')
      return
    }
    
    // 显示还车确认和损坏检查弹窗
    const result = await showReturnConfirmationDialog(bookingId)
    if (!result) {
      return // 用户取消还车
    }
    
    // 如果有损坏报告，先提交损坏报告
    if (result.hasDamage) {
      await submitDamageReport(result.damageData, bookingId)
      ElMessage.success('损坏报告已提交，请等待管理员审核。还车流程暂停。')
      return
    }
    
    // 正常还车流程
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

// 检查是否已有损坏记录
const checkExistingDamage = async (bookingId) => {
  try {
    const response = await api.get(`/damage/booking/${bookingId}/has-damage`)
    return response.data
  } catch (error) {
    console.error('检查损坏记录失败:', error)
    // 临时解决方案：如果后端不可用，默认返回false
    return false
  }
}

// 显示还车确认和损坏检查弹窗
const showReturnConfirmationDialog = (bookingId) => {
  return new Promise((resolve) => {
    // 使用简单的Element Plus弹窗
    ElMessageBox.confirm(
      '请仔细检查车辆状况，如实报告损坏情况。\n\n选择车辆状况：',
      '还车确认',
      {
        distinguishCancelAndClose: true,
        confirmButtonText: '完好无损',
        cancelButtonText: '有损坏',
        showClose: false
      }
    ).then(() => {
      // 用户选择"完好无损"
      resolve({ hasDamage: false })
    }).catch((action) => {
      if (action === 'cancel') {
        // 用户选择"有损坏"
        ElMessageBox.prompt('请描述损坏情况：', '损坏报告', {
          confirmButtonText: '提交',
          cancelButtonText: '取消',
          inputPlaceholder: '请详细描述损坏部位和程度...',
          inputType: 'textarea'
        }).then(({ value }) => {
          resolve({
            hasDamage: true,
            damageData: {
              damageLevel: 'MINOR',
              damagedParts: ['其他'],
              description: value || '',
              imageUrls: []
            }
          })
        }).catch(() => {
          resolve(null)
        })
      } else {
        resolve(null)
      }
    })
  })
}

// 提交损坏报告
const submitDamageReport = async (damageData, bookingId) => {
  try {
    // 获取当前预订的滑板车信息
    const currentBooking = bookings.value.find(b => b.id === bookingId)
    if (!currentBooking) {
      throw new Error('预订信息不存在')
    }
    
    const reportData = {
      bookingId: bookingId,
      scooterId: currentBooking.scooterId,
      damageLevel: damageData.damageLevel,
      damagedParts: damageData.damagedParts,
      description: damageData.description,
      imageUrls: damageData.imageUrls || []
    }
    
    await api.post('/damage/report', reportData)
    
  } catch (error) {
    console.error('提交损坏报告失败:', error)
    throw error
  }
}

// 银行卡管理相关状态
const settingsTab = ref('basic')
const bankCards = ref([])
const showAddBankCardDialog = ref(false)
const showEditBankCardDialog = ref(false)
const currentEditingCard = ref(null)
const bankCardFormRef = ref()

// 银行卡表单
const bankCardForm = ref({
  cardNumber: '',
  bankName: '',
  cardholderName: '',
  cardType: 'DEBIT',
  expiryDate: '',
  isDefault: false
})

// 银行卡验证规则
const bankCardRules = {
  cardNumber: [
    { required: true, message: '请输入银行卡号', trigger: 'blur' },
    { pattern: /^[0-9]{16,19}$/, message: '银行卡号格式不正确（16-19位数字）', trigger: 'blur' }
  ],
  bankName: [
    { required: true, message: '请输入银行名称', trigger: 'blur' }
  ],
  cardholderName: [
    { required: true, message: '请输入持卡人姓名', trigger: 'blur' }
  ],
  expiryDate: [
    { pattern: /^(0[1-9]|1[0-2])\/[0-9]{2}$/, message: '有效期格式不正确（MM/YY）', trigger: 'blur' }
  ]
}

// 加载银行卡列表
const loadBankCards = async () => {
  try {
    const response = await api.get('/bank-cards')
    bankCards.value = response.data
  } catch (error) {
    console.error('加载银行卡列表失败:', error)
    bankCards.value = []
  }
}

// 添加银行卡
const addBankCard = async () => {
  try {
    const response = await api.post('/bank-cards', bankCardForm.value)
    if (response.data.success) {
      ElMessage.success('银行卡添加成功')
      showAddBankCardDialog.value = false
      resetBankCardForm()
      await loadBankCards()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error('添加银行卡失败:', error)
    ElMessage.error(error.response?.data?.message || '添加银行卡失败')
  }
}

// 编辑银行卡
const editBankCard = (card) => {
  currentEditingCard.value = card
  bankCardForm.value = {
    cardNumber: card.cardNumber,
    bankName: card.bankName,
    cardholderName: card.cardholderName,
    cardType: card.cardType,
    expiryDate: card.expiryDate || '',
    isDefault: card.isDefault
  }
  showEditBankCardDialog.value = true
}

// 更新银行卡
const updateBankCard = async () => {
  try {
    const response = await api.put(`/bank-cards/${currentEditingCard.value.id}`, bankCardForm.value)
    if (response.data.success) {
      ElMessage.success('银行卡更新成功')
      showEditBankCardDialog.value = false
      resetBankCardForm()
      await loadBankCards()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error('更新银行卡失败:', error)
    ElMessage.error(error.response?.data?.message || '更新银行卡失败')
  }
}

// 删除银行卡
const deleteBankCard = async (cardId) => {
  try {
    await ElMessageBox.confirm('确定要删除这张银行卡吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await api.delete(`/bank-cards/${cardId}`)
    if (response.data.success) {
      ElMessage.success('银行卡删除成功')
      await loadBankCards()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除银行卡失败:', error)
      ElMessage.error(error.response?.data?.message || '删除银行卡失败')
    }
  }
}

// 设置默认银行卡
const setDefaultCard = async (cardId) => {
  try {
    const response = await api.post(`/bank-cards/${cardId}/set-default`)
    if (response.data.success) {
      ElMessage.success('默认银行卡设置成功')
      await loadBankCards()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error('设置默认银行卡失败:', error)
    ElMessage.error(error.response?.data?.message || '设置默认银行卡失败')
  }
}

// 重置银行卡表单
const resetBankCardForm = () => {
  bankCardForm.value = {
    cardNumber: '',
    bankName: '',
    cardholderName: '',
    cardType: 'DEBIT',
    expiryDate: '',
    isDefault: false
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
  
  // 加载银行卡数据
  loadBankCards()
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

/* 保险与条款样式 */
.insurance-content {
  display: grid;
  gap: 20px;
}

.insurance-card,
.terms-card,
.overtime-card {
  transition: transform 0.3s ease;
}

.insurance-card:hover,
.terms-card:hover,
.overtime-card:hover {
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #409EFF;
}

.insurance-status {
  display: grid;
  gap: 12px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.status-item:last-child {
  border-bottom: none;
}

.status-item .label {
  color: #606266;
  font-weight: 500;
}

/* 银行卡管理样式 */
.settings-tabs {
  margin-top: 20px;
}

.bank-cards-section {
  padding: 20px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #EBEEF5;
}

.section-header h4 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.bank-cards-list {
  display: grid;
  gap: 16px;
}

.bank-card-item {
  transition: all 0.3s ease;
  border: 1px solid #EBEEF5;
}

.bank-card-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.bank-card-item.default-card {
  border-color: #409EFF;
  background: linear-gradient(135deg, #f0f8ff 0%, #e6f7ff 100%);
}

.card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
}

.card-info {
  flex: 1;
}

.bank-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.card-type {
  display: inline-block;
  background: #f0f2f5;
  color: #606266;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-bottom: 8px;
}

.card-number {
  font-family: 'Courier New', monospace;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  letter-spacing: 1px;
  margin-bottom: 8px;
}

.cardholder-name,
.expiry-date {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.card-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-end;
}

/* 默认标签样式 */
.default-tag {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
  color: white;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(102, 194, 58, 0.2);
}

.default-tag .el-icon {
  margin-right: 4px;
  font-size: 12px;
}

/* 设为默认按钮样式 */
.set-default-btn {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border: none;
  color: white;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.2);
  transition: all 0.3s ease;
}

.set-default-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.3);
}

.set-default-btn .el-icon {
  margin-right: 4px;
  font-size: 12px;
}

/* 编辑按钮样式 */
.edit-btn {
  background: linear-gradient(135deg, #E6A23C 0%, #ebb563 100%);
  border: none;
  color: white;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(230, 162, 60, 0.2);
  transition: all 0.3s ease;
}

.edit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(230, 162, 60, 0.3);
}

.edit-btn .el-icon {
  margin-right: 4px;
  font-size: 12px;
}

/* 删除按钮样式 */
.delete-btn {
  background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%);
  border: none;
  color: white;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(245, 108, 108, 0.2);
  transition: all 0.3s ease;
}

.delete-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(245, 108, 108, 0.3);
  background: linear-gradient(135deg, #f56c6c 0%, #f9a7a7 100%);
}

.delete-btn .el-icon {
  margin-right: 4px;
  font-size: 12px;
}

/* 对话框样式 */
.tip-text {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .card-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .card-actions {
    flex-direction: row;
    align-items: center;
    width: 100%;
    justify-content: flex-end;
  }
  
  .section-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}

.status-item .value {
  color: #303133;
  text-align: right;
}

.terms-list {
  display: grid;
  gap: 12px;
}

.term-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #e6a23c;
}

.term-icon {
  color: #e6a23c;
  font-size: 16px;
  margin-top: 2px;
}

.term-text {
  color: #606266;
  line-height: 1.5;
  flex: 1;
}

.overtime-steps {
  display: grid;
  gap: 16px;
}

.step-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.step-number {
  width: 32px;
  height: 32px;
  background: #409EFF;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.step-content {
  flex: 1;
}

.step-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.step-desc {
  color: #606266;
  line-height: 1.5;
  font-size: 14px;
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
  
  .status-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .status-item .value {
    text-align: left;
  }
  
  .step-item {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }
  
  .step-number {
    align-self: center;
  }
}

/* 还车确认弹窗样式 - 精致版 */
.return-confirm-dialog-wrapper .el-message-box {
  width: 700px;
  max-width: 95vw;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.return-confirm-dialog {
  max-height: 80vh;
  overflow-y: auto;
  padding: 0;
}

/* 弹窗头部 */
.dialog-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 24px;
  border-radius: 12px 12px 0 0;
  text-align: center;
}

.dialog-header h3 {
  margin: 0 0 8px 0;
  font-size: 22px;
  font-weight: 600;
}

.dialog-subtitle {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}

/* 内容区域 */
.vehicle-condition-section,
.damage-report-section {
  padding: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.section-title .icon {
  font-size: 18px;
}

/* 车辆状况选项 */
.condition-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.condition-option {
  position: relative;
}

.condition-option input[type="radio"] {
  position: absolute;
  opacity: 0;
}

.condition-label {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.condition-label:hover {
  border-color: #409EFF;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

.condition-option input[type="radio"]:checked + .condition-label {
  border-color: #409EFF;
  background: #f0f7ff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.condition-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.condition-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.condition-title {
  font-weight: 600;
  color: #303133;
}

.condition-desc {
  font-size: 12px;
  color: #909399;
}

/* 损坏报告区域 */
.damage-report-section {
  background: #f8f9fa;
  border-radius: 0 0 12px 12px;
  margin-top: 0;
}

.form-group {
  margin-bottom: 24px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

/* 损坏部位网格 */
.damage-parts-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.damage-part {
  position: relative;
}

.damage-part input[type="checkbox"] {
  position: absolute;
  opacity: 0;
}

.part-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  text-align: center;
  transition: all 0.3s ease;
  background: white;
}

.part-label:hover {
  border-color: #409EFF;
  transform: translateY(-1px);
}

.damage-part input[type="checkbox"]:checked + .part-label {
  border-color: #409EFF;
  background: #f0f7ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.part-icon {
  font-size: 20px;
}

/* 表单元素 */
.form-textarea {
  width: 100%;
  padding: 12px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  resize: vertical;
  font-family: inherit;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.form-textarea:focus {
  outline: none;
  border-color: #409EFF;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

/* 文件上传区域 */
.upload-tips {
  margin-bottom: 12px;
  font-size: 12px;
  color: #909399;
  background: #f0f2f5;
  padding: 8px 12px;
  border-radius: 6px;
}

.upload-area {
  position: relative;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 32px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.upload-area:hover {
  border-color: #409EFF;
  background: #f0f7ff;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #909399;
}

.upload-icon {
  font-size: 32px;
}

.upload-text {
  font-size: 14px;
}

.file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.file-list {
  margin-top: 12px;
}

/* 按钮样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}

.btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-cancel {
  background: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
}

.btn-cancel:hover {
  background: #ebeef5;
  color: #409EFF;
}

.btn-primary {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  color: white;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

/* 损坏记录样式 */
.damage-records {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.damage-record {
  transition: all 0.3s ease;
}

.damage-record:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.record-header .scooter-model {
  font-weight: 600;
  color: #303133;
}

.record-details {
  line-height: 1.6;
}

.record-details p {
  margin: 4px 0;
  color: #606266;
}

.record-details strong {
  color: #303133;
}

/* 损坏状态标签样式 */
.damage-status-reported {
  background-color: #e6f7ff;
  border-color: #91d5ff;
  color: #1890ff;
}

.damage-status-under-review {
  background-color: #fff7e6;
  border-color: #ffd591;
  color: #fa8c16;
}

.damage-status-approved {
  background-color: #f6ffed;
  border-color: #b7eb8f;
  color: #52c41a;
}

.damage-status-compensated {
  background-color: #f9f0ff;
  border-color: #d3adf7;
  color: #722ed1;
}

.damage-status-repaired {
  background-color: #e6fffb;
  border-color: #87e8de;
  color: #13c2c2;
}

.damage-status-rejected {
  background-color: #fff2f0;
  border-color: #ffccc7;
  color: #ff4d4f;
}

.damage-status-cancelled {
  background-color: #f5f5f5;
  border-color: #d9d9d9;
  color: #8c8c8c;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .return-confirm-dialog-wrapper .el-message-box {
    width: 95vw;
    margin: 0 auto;
  }
  
  .vehicle-condition-section .el-radio-group {
    flex-direction: column;
    gap: 12px;
  }
  
  .damage-report-section .el-checkbox-group {
    flex-direction: column;
    gap: 8px;
  }
  
  .dialog-footer {
    flex-direction: column;
  }
  
  .record-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>