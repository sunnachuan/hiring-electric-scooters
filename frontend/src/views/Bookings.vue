<template>
  <div class="bookings-container">
    <div class="page-header">
      <h1 class="page-title">我的预订</h1>
      <p class="page-subtitle">管理您的滑板车租赁记录</p>
    </div>
    
    <el-tabs v-model="activeTab" class="booking-tabs">
      <el-tab-pane label="进行中" name="active">
        <div class="bookings-grid">
          <el-card 
            v-for="(booking, index) in activeBookings" 
            :key="booking.id" 
            class="booking-card card-hover fade-in"
            :class="getStatusClass(booking.status)"
            shadow="hover"
            :style="{ animationDelay: `${index * 0.1}s` }"
          >
            <div class="booking-header">
              <div class="booking-info">
                <h3 class="scooter-model">
                  <el-icon><Bicycle /></el-icon>
                  {{ booking.scooter.model }}
                </h3>
                <el-tag :type="getStatusType(booking.status)" class="status-tag">
                  {{ getStatusText(booking.status) }}
                </el-tag>
              </div>
              <div class="booking-id">订单 #{{ booking.id }}</div>
            </div>
            
            <div class="booking-details">
              <div class="detail-item">
                <span class="detail-label">租赁时长</span>
                <span class="detail-value">{{ formatDuration(booking.durationType) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">总价格</span>
                <span class="detail-value price">¥{{ booking.totalPrice.toFixed(2) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">折扣</span>
                <span class="detail-value discount">{{ (booking.discountApplied * 10).toFixed(1) }}折</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">开始时间</span>
                <span class="detail-value">{{ formatDate(booking.startTime) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">结束时间</span>
                <span class="detail-value">{{ formatDate(booking.endTime) }}</span>
              </div>
            </div>
            
            <div class="booking-actions">
              <el-button 
                type="danger" 
                size="small" 
                @click="handleCancel(booking.id)"
                v-if="booking.status === 'PENDING'"
                class="action-btn"
              >
                <el-icon><Close /></el-icon>
                取消预订
              </el-button>
              <el-button 
                type="primary" 
                size="small" 
                @click="openExtendDialog(booking)"
                v-if="booking.status === 'ACTIVE'"
                class="action-btn"
              >
                <el-icon><Clock /></el-icon>
                延长租赁
              </el-button>
            </div>
          </el-card>
        </div>
        
        <div v-if="activeBookings.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无进行中的预订" />
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="历史记录" name="history">
        <div class="bookings-grid">
          <el-card 
            v-for="(booking, index) in historyBookings" 
            :key="booking.id" 
            class="booking-card history card-hover fade-in"
            :class="getStatusClass(booking.status)"
            shadow="hover"
            :style="{ animationDelay: `${index * 0.1}s` }"
          >
            <div class="booking-header">
              <div class="booking-info">
                <h3 class="scooter-model">
                  <el-icon><Bicycle /></el-icon>
                  {{ booking.scooter.model }}
                </h3>
                <el-tag :type="getStatusType(booking.status)" class="status-tag">
                  {{ getStatusText(booking.status) }}
                </el-tag>
              </div>
              <div class="booking-id">订单 #{{ booking.id }}</div>
            </div>
            
            <div class="booking-details">
              <div class="detail-item">
                <span class="detail-label">租赁时长</span>
                <span class="detail-value">{{ formatDuration(booking.durationType) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">总价格</span>
                <span class="detail-value price">¥{{ booking.totalPrice.toFixed(2) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">折扣</span>
                <span class="detail-value discount">{{ (booking.discountApplied * 10).toFixed(1) }}折</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">开始时间</span>
                <span class="detail-value">{{ formatDate(booking.startTime) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">结束时间</span>
                <span class="detail-value">{{ formatDate(booking.endTime) }}</span>
              </div>
            </div>
          </el-card>
        </div>
        
        <div v-if="historyBookings.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无历史预订记录" />
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>
    
    <!-- 延长预订对话框 -->
    <el-dialog 
      v-model="showExtendDialog" 
      title="延长预订" 
      width="400px"
    >
      <el-form :model="extendForm" ref="extendFormRef">
        <el-form-item label="延长时长">
          <el-select v-model="extendForm.durationType" placeholder="选择时长" style="width: 100%">
            <el-option label="1小时" value="1h" />
            <el-option label="4小时" value="4h" />
            <el-option label="1天" value="1d" />
            <el-option label="1周" value="1w" />
          </el-select>
        </el-form-item>
        
        <div class="price-preview" v-if="extendForm.durationType && currentBooking">
          <p>延长价格: <strong>¥{{ calculateExtendPrice().toFixed(2) }}</strong></p>
        </div>
      </el-form>
      
      <template #footer>
        <el-button @click="showExtendDialog = false">取消</el-button>
        <el-button type="primary" @click="handleExtend" :loading="extendLoading">
          确认延长
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bicycle, Close, Clock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/api'

const authStore = useAuthStore()
const isAdmin = computed(() => authStore.userInfo?.role === 'ADMIN')

const activeTab = ref('active')
const bookings = ref([])
const loading = ref(false)
const showExtendDialog = ref(false)
const extendLoading = ref(false)
const extendFormRef = ref()
const currentBooking = ref(null)

const extendForm = ref({
  durationType: ''
})

const activeBookings = computed(() => 
  bookings.value.filter(b => b.status === 'PENDING' || b.status === 'ACTIVE')
)

const historyBookings = computed(() => 
  bookings.value.filter(b => b.status === 'COMPLETED' || b.status === 'CANCELLED')
)

const loadBookings = async () => {
  loading.value = true
  try {
    const response = await api.get('/bookings/user')
    bookings.value = response.data
  } catch (error) {
    ElMessage.error('加载预订记录失败')
  } finally {
    loading.value = false
  }
}

const formatDuration = (durationType) => {
  const map = { '1h': '1小时', '4h': '4小时', '1d': '1天', '1w': '1周' }
  return map[durationType] || durationType
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

const getStatusType = (status) => {
  const types = { 'COMPLETED': 'success', 'CANCELLED': 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 
    'PENDING': '待开始', 
    'ACTIVE': '进行中', 
    'COMPLETED': '已完成', 
    'CANCELLED': '已取消' 
  }
  return texts[status] || status
}

const getStatusClass = (status) => {
  const classes = { 
    'PENDING': 'pending', 
    'ACTIVE': 'active', 
    'COMPLETED': 'completed', 
    'CANCELLED': 'cancelled' 
  }
  return classes[status] || ''
}

const handleCancel = async (bookingId) => {
  try {
    await ElMessageBox.confirm('确定要取消此预订吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.put(`/bookings/${bookingId}/cancel`)
    ElMessage.success('取消成功')
    loadBookings()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

const openExtendDialog = (booking) => {
  currentBooking.value = booking
  extendForm.value.durationType = ''
  showExtendDialog.value = true
}

const calculateExtendPrice = () => {
  if (!currentBooking.value || !extendForm.value.durationType) return 0
  
  const scooter = currentBooking.value.scooter
  const duration = extendForm.value.durationType
  const discount = currentBooking.value.discountApplied
  
  let basePrice = 0
  switch (duration) {
    case '1h': basePrice = scooter.hourlyRate; break
    case '4h': basePrice = scooter.hourlyRate * 4; break
    case '1d': basePrice = scooter.dailyRate; break
    case '1w': basePrice = scooter.dailyRate * 7; break
  }
  
  return basePrice * discount
}

const handleExtend = async () => {
  if (!extendForm.value.durationType) {
    ElMessage.warning('请选择延长时长')
    return
  }
  
  extendLoading.value = true
  
  try {
    await api.put(`/bookings/${currentBooking.value.id}/extend?durationType=${extendForm.value.durationType}`)
    ElMessage.success('延长成功')
    showExtendDialog.value = false
    loadBookings()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '延长失败')
  } finally {
    extendLoading.value = false
  }
}

onMounted(() => {
  loadBookings()
})
</script>

<style scoped>
.bookings-container {
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

.booking-tabs {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.bookings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.booking-card {
  border-radius: 12px;
  border: none;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.booking-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  transition: all 0.3s ease;
}

.booking-card.pending::before {
  background: linear-gradient(135deg, #ffd666 0%, #ffa940 100%);
}

.booking-card.active::before {
  background: linear-gradient(135deg, #73d13d 0%, #52c41a 100%);
}

.booking-card.completed::before {
  background: linear-gradient(135deg, #597ef7 0%, #1890ff 100%);
}

.booking-card.cancelled::before {
  background: linear-gradient(135deg, #ff7875 0%, #ff4d4f 100%);
}

.booking-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.booking-card.history {
  opacity: 0.8;
  filter: grayscale(0.2);
}

.booking-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.booking-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.scooter-model {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.scooter-model .el-icon {
  font-size: 24px;
  color: #667eea;
}

.status-tag {
  font-weight: 600;
  border-radius: 12px;
  padding: 4px 12px;
}

.booking-id {
  font-size: 14px;
  color: #999;
  font-weight: 500;
}

.booking-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.detail-label {
  font-size: 14px;
  color: #666;
}

.detail-value {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.detail-value.price {
  color: #f56c6c;
  font-weight: 700;
}

.detail-value.discount {
  color: #67c23a;
  font-weight: 700;
}

.booking-actions {
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.action-btn {
  width: 100%;
  padding: 10px;
  font-size: 14px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.loading-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
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
</style>