<template>
  <div class="my-bookings-container">
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button @click="goBack" type="primary" size="large">
        <el-icon><ArrowLeft /></el-icon>
        返回个人中心
      </el-button>
    </div>

    <!-- 页面标题 -->
    <div class="page-header">
      <h1>我的预订记录</h1>
      <el-button type="primary" @click="$router.push('/scooters')">
        <el-icon><Plus /></el-icon>
        新的预订
      </el-button>
    </div>

    <!-- 预订列表 -->
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
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus, Location, Clock, Coin } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()

// 真实数据
const bookings = ref([])
const extendingBookingId = ref(null)
const returningBookingId = ref(null)
const loading = ref(false)

// 返回个人中心
const goBack = () => {
  router.push('/profile')
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'PENDING': 'warning',
    'ACTIVE': 'success',
    'COMPLETED': 'info',
    'CANCELLED': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待开始',
    'ACTIVE': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
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

// 加载预订数据
const loadBookings = async () => {
  loading.value = true
  try {
    const response = await api.get('/bookings/user')
    bookings.value = response.data.map(booking => ({
      id: booking.id,
      scooterModel: booking.scooter?.model || '未知型号',
      location: booking.scooter?.locationName || '未知位置',
      startTime: booking.startTime,
      endTime: booking.endTime,
      amount: booking.totalPrice || 0,
      status: booking.status
    }))
  } catch (error) {
    console.error('加载预订数据失败:', error)
    ElMessage.error('加载预订数据失败')
    bookings.value = []
  } finally {
    loading.value = false
  }
}

// 延长用车时间
const extendBookingTime = async (bookingId) => {
  extendingBookingId.value = bookingId
  try {
    // TODO: 实现延长用车时间的API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('用车时间已延长')
  } catch (error) {
    ElMessage.error('延长用车时间失败')
  } finally {
    extendingBookingId.value = null
  }
}

// 提前还车
const returnScooterEarly = async (bookingId) => {
  returningBookingId.value = bookingId
  try {
    // 调用真实API提前还车
    await api.put(`/bookings/${bookingId}/return`)
    ElMessage.success('滑板车已归还')
    // 重新加载数据
    await loadBookings()
  } catch (error) {
    console.error('提前还车失败:', error)
    ElMessage.error('归还失败: ' + (error.response?.data?.message || error.message))
  } finally {
    returningBookingId.value = null
  }
}

// 初始化数据
onMounted(() => {
  loadBookings()
})
</script>

<style scoped>
.my-bookings-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.back-button {
  margin-bottom: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.page-header h1 {
  margin: 0;
  color: #303133;
  font-size: 28px;
  font-weight: 600;
}

.booking-items {
  display: grid;
  gap: 16px;
}

.booking-item {
  transition: transform 0.2s ease;
}

.booking-item:hover {
  transform: translateY(-2px);
}

.booking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.scooter-model {
  font-size: 18px;
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

.booking-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .my-bookings-container {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .booking-actions {
    flex-direction: column;
  }
}
</style>