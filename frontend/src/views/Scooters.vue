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
        
        <el-form-item label="租赁时长" prop="durationType">
          <el-select v-model="bookingForm.durationType" placeholder="选择时长" style="width: 100%">
            <el-option label="1小时" value="1h" />
            <el-option label="4小时" value="4h" />
            <el-option label="1天" value="1d" />
            <el-option label="1周" value="1w" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="信用卡号" prop="cardNumber">
          <el-input 
            v-model="bookingForm.cardNumber" 
            placeholder="输入信用卡号（模拟支付）"
            maxlength="16"
          />
        </el-form-item>
        
        <div class="price-preview" v-if="selectedScooter && bookingForm.durationType">
          <p>预估价格: <strong>¥{{ calculatePrice().toFixed(2) }}</strong></p>
        </div>
      </el-form>
      
      <template #footer>
        <el-button @click="handleBookingClose">取消</el-button>
        <el-button type="primary" @click="handleBooking" :loading="bookingLoading">
          确认预订
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, Bicycle, ShoppingCart } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/api'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const isAdmin = computed(() => authStore.userInfo?.role === 'ADMIN')

const scooters = ref([])
const loading = ref(false)
const showBookingDialog = ref(false)
const bookingLoading = ref(false)
const bookingFormRef = ref()

const bookingForm = ref({
  scooterId: null,
  durationType: '',
  cardNumber: ''
})

const bookingRules = {
  scooterId: [{ required: true, message: '请选择滑板车', trigger: 'change' }],
  durationType: [{ required: true, message: '请选择租赁时长', trigger: 'change' }],
  cardNumber: [
    { required: true, message: '请输入信用卡号', trigger: 'blur' },
    { min: 12, message: '信用卡号至少12位', trigger: 'blur' }
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

const calculatePrice = () => {
  if (!selectedScooter.value || !bookingForm.value.durationType) return 0
  
  const scooter = selectedScooter.value
  const duration = bookingForm.value.durationType
  
  switch (duration) {
    case '1h': return scooter.hourlyRate
    case '4h': return scooter.hourlyRate * 4
    case '1d': return scooter.dailyRate
    case '1w': return scooter.dailyRate * 7
    default: return 0
  }
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
    durationType: '',
    cardNumber: ''
  }
}

const handleBooking = async () => {
  if (!bookingFormRef.value) return
  
  const valid = await bookingFormRef.value.validate()
  if (!valid) return
  
  bookingLoading.value = true
  
  try {
    await api.post('/bookings', bookingForm.value)
    ElMessage.success('预订成功')
    handleBookingClose()
    loadScooters()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '预订失败')
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
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 40px 0;
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
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 12px 0;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
}

.page-subtitle {
  font-size: 18px;
  opacity: 0.9;
  margin: 0 0 24px 0;
  position: relative;
  z-index: 1;
}

.booking-btn {
  position: relative;
  z-index: 1;
  padding: 12px 32px;
  font-size: 16px;
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
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
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
  margin-bottom: 20px;
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
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.price-label {
  font-size: 14px;
  color: #666;
}

.price-value {
  font-size: 18px;
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
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 20px;
  color: white;
  text-align: center;
}

.price-preview p {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>