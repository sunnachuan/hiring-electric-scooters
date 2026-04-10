<template>
  <div class="scooters-container">
    <div class="page-header">
      <h1 class="page-title">滑板车租赁</h1>
      <p class="page-subtitle">选择您喜欢的滑板车开始租赁之旅</p>
      <el-button 
        type="primary" 
        size="large" 
        @click="showBookingDialog = true" 
        v-if="!isAdmin"
        class="booking-btn"
      >
        <el-icon><Plus /></el-icon>
        快速预订
      </el-button>
    </div>
    
    <div class="scooters-grid">
  <el-card 
    v-for="(scooter, index) in scooters" 
    :key="scooter.id" 
    class="scooter-card card-hover fade-in"
    :class="{ 'unavailable': scooter.status !== 'AVAILABLE' }"
    shadow="hover"
    :style="{ animationDelay: `${index * 0.1}s` }"
  >
        <!-- 滑板车图片 -->
        <div class="scooter-image" v-if="scooter.imageUrl">
          <el-image 
            :src="scooter.imageUrl" 
            fit="cover" 
            class="scooter-img"
            :preview-src-list="[scooter.imageUrl]"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon><Picture /></el-icon>
                <span>图片加载失败</span>
              </div>
            </template>
          </el-image>
        </div>
        
        <div class="scooter-header">
          <div class="scooter-model">
            <el-icon class="scooter-icon"><Bicycle /></el-icon>
            <h3>{{ scooter.model }}</h3>
          </div>
          <el-tag 
            :type="getScooterStatusType(scooter)"
            class="status-tag"
          >
            {{ getScooterStatusText(scooter) }}
          </el-tag>
        </div>
        
        <!-- 点位信息 -->
        <div class="location-info" v-if="scooter.locationName">
          <el-tag size="small" type="info">
            <el-icon><Location /></el-icon>
            {{ scooter.locationName }}
          </el-tag>
        </div>
        
        <!-- 可用数量 -->
        <div class="quantity-info" v-if="scooter.status === 'AVAILABLE'">
          <el-tag type="info" size="small">
            <el-icon><Collection /></el-icon>
            可用: {{ scooter.availableQuantity }}/{{ scooter.totalQuantity }}
          </el-tag>
        </div>
        
        <div class="scooter-details">
          <div class="price-info">
            <div class="price-item">
              <span class="price-label">小时价</span>
              <span class="price-value">¥{{ scooter.hourlyRate.toFixed(2) }}</span>
            </div>
            <div class="price-item">
              <span class="price-label">日价</span>
              <span class="price-value">¥{{ scooter.dailyRate.toFixed(2) }}</span>
            </div>
          </div>
          
          <div class="scooter-features">
            <el-tag size="small" type="info">电动</el-tag>
            <el-tag size="small" type="info">便携</el-tag>
            <el-tag size="small" type="info">环保</el-tag>
          </div>
        </div>
        
        <div class="scooter-actions" v-if="!isAdmin">
          <el-button 
            :type="getBookingButtonType(scooter)"
            class="book-btn"
            :disabled="scooter.status !== 'AVAILABLE' || scooter.availableQuantity <= 0"
            @click="openBookingDialog(scooter)"
          >
            <el-icon><ShoppingCart /></el-icon>
            {{ getBookingButtonText(scooter) }}
          </el-button>
        </div>
      </el-card>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>
    
    <!-- 预订对话框 -->
    <el-dialog 
      v-model="showBookingDialog" 
      title="预订滑板车" 
      width="500px"
      :before-close="handleBookingClose"
    >
      <el-form :model="bookingForm" :rules="bookingRules" ref="bookingFormRef">
        <el-form-item label="滑板车" prop="scooterId">
          <el-select v-model="bookingForm.scooterId" placeholder="选择滑板车" style="width: 100%">
            <el-option
              v-for="scooter in availableScooters"
              :key="scooter.id"
              :label="`${scooter.model} - ¥${scooter.hourlyRate}/小时`"
              :value="scooter.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="租赁时长" prop="hours">
          <div class="duration-input-group">
            <el-input-number 
              v-model="bookingForm.hours" 
              :min="1" 
              :max="168" 
              :step="1"
              placeholder="自定义时长"
              style="width: 150px; margin-right: 10px"
              @change="updatePricePreview"
            />
            <el-select 
              v-model="bookingForm.hours" 
              placeholder="快速选择" 
              style="width: 150px"
              @change="updatePricePreview"
            >
              <el-option label="1小时" :value="1" />
              <el-option label="4小时" :value="4" />
              <el-option label="1天 (24小时)" :value="24" />
              <el-option label="2天 (48小时)" :value="48" />
              <el-option label="3天 (72小时)" :value="72" />
              <el-option label="4天 (96小时)" :value="96" />
              <el-option label="5天 (120小时)" :value="120" />
              <el-option label="6天 (144小时)" :value="144" />
              <el-option label="1周 (168小时)" :value="168" />
            </el-select>
          </div>
          <div class="duration-tips">
            <small>支持1-168小时（7天）租赁，可自定义或快速选择</small>
          </div>
        </el-form-item>
        
        <!-- 支付信息提示 -->
        <el-form-item>
          <el-alert 
            title="模拟支付" 
            type="info" 
            description="点击确认预订后，系统将自动完成支付流程" 
            show-icon 
            :closable="false"
          />
        </el-form-item>
        
        <div class="price-preview" v-if="selectedScooter && bookingForm.hours">
          <p>预估价格: <strong>¥{{ calculatePrice().toFixed(2) }}</strong></p>
          <el-button 
            type="text" 
            size="small" 
            @click="showPriceDetails = true"
            class="price-details-btn"
          >
            <el-icon><InfoFilled /></el-icon>
            查看价格详情
          </el-button>
        </div>
      </el-form>
      
      <template #footer>
        <el-button @click="handleBookingClose">取消</el-button>
        <el-button type="primary" @click="handleBooking" :loading="bookingLoading">
          确认预订
        </el-button>
      </template>
    </el-dialog>

    <!-- 价格详情弹窗 -->
    <el-dialog 
      v-model="showPriceDetails" 
      title="价格计算详情" 
      width="600px"
      center
    >
      <div v-if="getPriceDetails()" class="price-details">
        <div class="detail-section">
          <h4>基础信息</h4>
          <p>基准单价：¥{{ getPriceDetails().basePrice }}/小时</p>
          <p>租赁时长：{{ getPriceDetails().hours }} 小时</p>
          <p>价格区间：{{ getPriceDetails().pricingTier }}</p>
        </div>
        
        <div class="detail-section">
          <h4>计算明细</h4>
          <p>折扣比例：{{ (getPriceDetails().discountRate * 100).toFixed(0) }}%</p>
          <p>有效计费时长：{{ getPriceDetails().effectiveHours }} 小时</p>
          <p>计算公式：¥{{ getPriceDetails().basePrice }} × {{ getPriceDetails().effectiveHours }} × {{ getPriceDetails().discountRate }}</p>
        </div>
        
        <div class="detail-section total-section">
          <h4>总价</h4>
          <p class="total-price">¥{{ getPriceDetails().totalPrice.toFixed(2) }}</p>
        </div>
        
        <div class="detail-section">
          <h4>阶梯定价方案</h4>
          <el-table :data="getPricingTable()" size="small" stripe>
            <el-table-column prop="duration" label="租赁时长" width="120" />
            <el-table-column prop="discount" label="折扣" width="80" />
            <el-table-column prop="effectiveHours" label="计费时长" width="100" />
            <el-table-column prop="price" label="价格" align="right" />
          </el-table>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showPriceDetails = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, Bicycle, ShoppingCart, Location, InfoFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/api'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const isAdmin = computed(() => authStore.userInfo?.role === 'ADMIN')

const scooters = ref([])
const loading = ref(false)
const showBookingDialog = ref(false)
const showPriceDetails = ref(false)
const bookingLoading = ref(false)
const bookingFormRef = ref()

const bookingForm = ref({
  scooterId: null,
  hours: 1,
  cardNumber: '123456789012' // 默认信用卡号
})

const bookingRules = {
  scooterId: [{ required: true, message: '请选择滑板车', trigger: 'change' }],
  hours: [
    { required: true, message: '请输入租赁时长', trigger: 'blur' },
    { type: 'number', min: 1, max: 168, message: '时长必须在1-168小时之间', trigger: 'blur' }
  ]
}

const availableScooters = computed(() => 
  scooters.value.filter(s => s.status === 'AVAILABLE')
)

const selectedScooter = computed(() => 
  scooters.value.find(s => s.id === bookingForm.value.scooterId)
)

const loadScooters = async () => {
  loading.value = true
  try {
    const response = await api.get('/scooters')
    scooters.value = response.data
  } catch (error) {
    ElMessage.error('加载滑板车失败')
  } finally {
    loading.value = false
  }
}

// 获取滑板车状态类型
const getScooterStatusType = (scooter) => {
  if (scooter.status === 'UNAVAILABLE') return 'danger'
  if (scooter.availableQuantity <= 0) return 'warning'
  return 'success'
}

// 获取滑板车状态文本
const getScooterStatusText = (scooter) => {
  if (scooter.status === 'UNAVAILABLE') return '不可用'
  if (scooter.availableQuantity <= 0) return '已租完'
  return '可租用'
}

// 获取预订按钮类型
const getBookingButtonType = (scooter) => {
  if (scooter.status === 'UNAVAILABLE') return 'info'
  if (scooter.availableQuantity <= 0) return 'warning'
  return 'primary'
}

// 获取预订按钮文本
const getBookingButtonText = (scooter) => {
  if (scooter.status === 'UNAVAILABLE') return '不可预订'
  if (scooter.availableQuantity <= 0) return '已租完'
  return '立即预订'
}

// 新的分层定价计算逻辑
const calculatePrice = () => {
  if (!selectedScooter.value || !bookingForm.value.hours) return 0
  
  const basePrice = selectedScooter.value.hourlyRate // 使用滑板车实际单价
  const hours = bookingForm.value.hours
  
  // 分层定价逻辑
  if (hours <= 3) {
    // 1-3小时：100% 原价
    return basePrice * hours
  } else if (hours <= 8) {
    // 4-8小时：85% 折扣
    return basePrice * hours * 0.85
  } else if (hours <= 24) {
    // 9-24小时：60% 折扣，但最高收12小时费用
    const effectiveHours = Math.min(hours, 12)
    return basePrice * effectiveHours * 0.6
  } else if (hours <= 72) {
    // 1-3天：50% 折扣，每天按12小时计费
    const days = Math.ceil(hours / 24)
    return basePrice * 12 * days * 0.5
  } else {
    // 3天以上：30% 折扣，每天按12小时计费
    const days = Math.ceil(hours / 24)
    return basePrice * 12 * days * 0.3
  }
}

// 获取价格计算详情（用于弹窗显示）
const getPriceDetails = () => {
  if (!selectedScooter.value || !bookingForm.value.hours) return null
  
  const basePrice = selectedScooter.value.hourlyRate
  const hours = bookingForm.value.hours
  let details = {
    basePrice: basePrice,
    hours: hours,
    discountRate: 1,
    effectiveHours: hours,
    totalPrice: 0,
    pricingTier: ''
  }
  
  if (hours <= 3) {
    details.discountRate = 1
    details.effectiveHours = hours
    details.pricingTier = '1-3小时（原价）'
  } else if (hours <= 8) {
    details.discountRate = 0.85
    details.effectiveHours = hours
    details.pricingTier = '4-8小时（85折）'
  } else if (hours <= 24) {
    details.discountRate = 0.6
    details.effectiveHours = Math.min(hours, 12)
    details.pricingTier = '9-24小时（6折，最高12小时）'
  } else if (hours <= 72) {
    details.discountRate = 0.5
    const days = Math.ceil(hours / 24)
    details.effectiveHours = 12 * days
    details.pricingTier = '1-3天（5折，按天计费）'
  } else {
    details.discountRate = 0.3
    const days = Math.ceil(hours / 24)
    details.effectiveHours = 12 * days
    details.pricingTier = '3天以上（3折，按天计费）'
  }
  
  details.totalPrice = basePrice * details.effectiveHours * details.discountRate
  return details
}

// 更新价格预览
const updatePricePreview = () => {
  // 触发价格重新计算
}

// 生成阶梯定价方案表格
const getPricingTable = () => {
  if (!selectedScooter.value) return []
  
  const basePrice = selectedScooter.value.hourlyRate
  const durations = [
    { hours: 1, label: '1小时' },
    { hours: 3, label: '3小时' },
    { hours: 4, label: '4小时' },
    { hours: 8, label: '8小时' },
    { hours: 9, label: '9小时' },
    { hours: 12, label: '12小时' },
    { hours: 24, label: '1天' },
    { hours: 48, label: '2天' },
    { hours: 72, label: '3天' },
    { hours: 96, label: '4天' },
    { hours: 120, label: '5天' },
    { hours: 144, label: '6天' },
    { hours: 168, label: '1周' }
  ]
  
  return durations.map(duration => {
    const hours = duration.hours
    let discountRate = 1
    let effectiveHours = hours
    let pricingTier = ''
    
    if (hours <= 3) {
      discountRate = 1
      effectiveHours = hours
      pricingTier = '原价'
    } else if (hours <= 8) {
      discountRate = 0.85
      effectiveHours = hours
      pricingTier = '85折'
    } else if (hours <= 24) {
      discountRate = 0.6
      effectiveHours = Math.min(hours, 12)
      pricingTier = '6折'
    } else if (hours <= 72) {
      discountRate = 0.5
      const days = Math.ceil(hours / 24)
      effectiveHours = 12 * days
      pricingTier = '5折'
    } else {
      discountRate = 0.3
      const days = Math.ceil(hours / 24)
      effectiveHours = 12 * days
      pricingTier = '3折'
    }
    
    const price = basePrice * effectiveHours * discountRate
    
    return {
      duration: duration.label,
      discount: `${(discountRate * 100).toFixed(0)}%`,
      effectiveHours: `${effectiveHours}小时`,
      price: `¥${price.toFixed(2)}`
    }
  })
}

const openBookingDialog = (scooter) => {
  // 检查滑板车是否可用
  if (scooter.status === 'UNAVAILABLE' || scooter.availableQuantity <= 0) {
    ElMessage.warning('该滑板车暂不可租用')
    return
  }
  
  bookingForm.value.scooterId = scooter.id
  showBookingDialog.value = true
}

const handleBookingClose = () => {
  showBookingDialog.value = false
  bookingForm.value = {
    scooterId: null,
    hours: 1,
    cardNumber: '123456789012' // 重置时保留默认信用卡号
  }
}

const handleBooking = async () => {
  if (!bookingFormRef.value) return
  
  try {
    await bookingFormRef.value.validate()
  } catch (error) {
    // 表单验证失败，直接返回
    return
  }
  
  bookingLoading.value = true
  
  try {
    // 模拟支付处理过程
    ElMessage.info('正在处理支付...')
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 模拟支付成功
    ElMessage.success('支付成功！')
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 提交预订
    console.log('发送预订请求:', bookingForm.value)
    console.log('请求完整URL:', api.defaults.baseURL + '/bookings')
    
    // 构建后端期望的请求格式
    const bookingData = {
      scooterId: bookingForm.value.scooterId,
      hours: bookingForm.value.hours,
      cardNumber: bookingForm.value.cardNumber
    }
    
    const response = await api.post('/bookings', bookingData)
    console.log('预订响应:', response.data)
    ElMessage.success('预订成功！您的滑板车已准备就绪')
    
    handleBookingClose()
    loadScooters()
  } catch (error) {
    console.error('预订错误详情:', error)
    if (error.response) {
      // 后端返回的错误
      const errorMessage = error.response.data?.message || error.response.data?.error || '预订失败'
      ElMessage.error(`预订失败: ${errorMessage}`)
    } else if (error.request) {
      // 网络错误
      ElMessage.error('网络连接错误，请检查网络连接后重试')
    } else {
      // 其他错误
      ElMessage.error('预订失败，请重试')
    }
  } finally {
    bookingLoading.value = false
  }
}

onMounted(() => {
  loadScooters()
})
</script>

<style scoped>
.scooters-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 30px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: white;
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="dots" width="20" height="20" patternUnits="userSpaceOnUse"><circle cx="10" cy="10" r="1" fill="rgba(255,255,255,0.1)"/></pattern></defs><rect width="100" height="100" fill="url(%23dots)"/></svg>');
  opacity: 0.3;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 12px 0;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
}

.page-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 24px 0;
  position: relative;
  z-index: 1;
}

.booking-btn {
  position: relative;
  z-index: 1;
  padding: 12px 24px;
  font-size: 14px;
  border-radius: 25px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.booking-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.scooters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 40px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .scooters-container {
    padding: 0 12px;
  }
  
  .page-header {
    margin-bottom: 24px;
    padding: 20px 16px;
    border-radius: 12px;
  }
  
  .page-title {
    font-size: 24px;
    margin-bottom: 8px;
  }
  
  .page-subtitle {
    font-size: 14px;
    margin-bottom: 16px;
  }
  
  .booking-btn {
    padding: 10px 20px;
    font-size: 13px;
  }
  
  .scooters-grid {
    grid-template-columns: 1fr;
    gap: 12px;
    margin-bottom: 24px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 20px;
  }
  
  .page-subtitle {
    font-size: 13px;
  }
  
  .scooters-grid {
    gap: 8px;
  }
}

.scooter-card {
  border-radius: 16px;
  border: none;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.scooter-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.scooter-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
}

.scooter-card.unavailable {
  opacity: 0.6;
  filter: grayscale(0.3);
}

/* 滑板车图片样式 */
.scooter-image {
  margin-bottom: 16px;
  border-radius: 12px;
  overflow: hidden;
  height: 200px;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.scooter-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #999;
  font-size: 14px;
}

.image-placeholder .el-icon {
  font-size: 48px;
}

/* 数量信息样式 */
.quantity-info {
  margin-bottom: 16px;
  text-align: center;
}

.quantity-info .el-tag {
  border-radius: 12px;
  padding: 6px 12px;
  font-weight: 500;
}

.scooter-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.location-info {
  margin-bottom: 12px;
  text-align: center;
}

.location-info .el-tag {
  border-radius: 12px;
  padding: 6px 12px;
  font-weight: 500;
}

.scooter-model {
  display: flex;
  align-items: center;
  gap: 12px;
}

.scooter-icon {
  font-size: 32px;
  color: #667eea;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.scooter-model h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
}

.status-tag {
  font-weight: 600;
  border-radius: 12px;
  padding: 4px 12px;
}

.scooter-details {
  margin-bottom: 20px;
}

.price-info {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.price-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #667eea;
  min-width: 0; /* 防止flex item溢出 */
}

.price-label {
  font-size: 13px;
  color: #666;
  white-space: nowrap; /* 防止文字换行 */
  margin-bottom: 4px;
}

.price-value {
  font-size: 16px;
  font-weight: 700;
  color: #667eea;
}

.scooter-features {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.scooter-actions {
  text-align: center;
}

.book-btn {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.book-btn:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.loading-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
}

.price-preview {
  margin-top: 15px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.price-preview p {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.price-details-btn {
  margin-top: 8px;
  color: #409eff;
}

.price-details {
  line-height: 1.8;
}

.detail-section {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.detail-section h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-weight: 600;
}

.detail-section p {
  margin: 5px 0;
  color: #666;
}

.total-section {
  border-bottom: none;
  background-color: #f0f7ff;
  padding: 15px;
  border-radius: 8px;
  margin-top: 20px;
}

.total-price {
  font-size: 1.5em;
  font-weight: bold;
  color: #409eff;
  margin: 10px 0 0 0 !important;
}

.duration-input-group {
  display: flex;
  align-items: center;
}

.duration-tips {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>