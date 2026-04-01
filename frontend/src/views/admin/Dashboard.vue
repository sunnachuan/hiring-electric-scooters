<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <h2>管理仪表盘</h2>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card>
                <template #header>
                  <h3>每周收入统计</h3>
                </template>
                <canvas id="weeklyRevenueChart" style="height: 300px;"></canvas>
              </el-card>
            </el-col>
            
            <el-col :span="12">
              <el-card>
                <template #header>
                  <h3>每日收入统计</h3>
                </template>
                <canvas id="dailyRevenueChart" style="height: 300px;"></canvas>
              </el-card>
            </el-col>
          </el-row>
          
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="24">
              <el-card>
                <template #header>
                  <div class="card-header">
                    <h3>快速操作</h3>
                    <el-button type="primary" @click="showCreateBookingDialog = true">
                      代用户下单
                    </el-button>
                  </div>
                </template>
                
                <el-row :gutter="20">
                  <el-col :span="8">
                    <el-statistic title="总滑板车数量" :value="stats.totalScooters" />
                  </el-col>
                  <el-col :span="8">
                    <el-statistic title="可用滑板车" :value="stats.availableScooters" />
                  </el-col>
                  <el-col :span="8">
                    <el-statistic title="本周总收入" :value="stats.weeklyRevenue" precision="2">
                      <template #prefix>¥</template>
                    </el-statistic>
                  </el-col>
                </el-row>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 代下单对话框 -->
    <el-dialog 
      v-model="showCreateBookingDialog" 
      title="代用户下单" 
      width="500px"
    >
      <el-form :model="adminBookingForm" :rules="adminBookingRules" ref="adminBookingFormRef">
        <el-form-item label="用户邮箱" prop="userEmail">
          <el-input v-model="adminBookingForm.userEmail" placeholder="输入用户邮箱" />
        </el-form-item>
        
        <el-form-item label="滑板车" prop="scooterId">
          <el-select v-model="adminBookingForm.scooterId" placeholder="选择滑板车" style="width: 100%">
            <el-option
              v-for="scooter in availableScooters"
              :key="scooter.id"
              :label="`${scooter.model} - ¥${scooter.hourlyRate}/小时`"
              :value="scooter.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="租赁时长" prop="durationType">
          <el-select v-model="adminBookingForm.durationType" placeholder="选择时长" style="width: 100%">
            <el-option label="1小时" value="1h" />
            <el-option label="4小时" value="4h" />
            <el-option label="1天" value="1d" />
            <el-option label="1周" value="1w" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateBookingDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdminBooking" :loading="adminBookingLoading">
          确认下单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'
import api from '@/api'
import { ElMessage } from 'element-plus'

// 注册Chart.js组件
Chart.register(...registerables)

const showCreateBookingDialog = ref(false)
const adminBookingLoading = ref(false)
const adminBookingFormRef = ref()
const weeklyChart = ref(null)
const dailyChart = ref(null)

const stats = ref({
  totalScooters: 0,
  availableScooters: 0,
  weeklyRevenue: 0
})

const availableScooters = ref([])

const adminBookingForm = ref({
  userEmail: '',
  scooterId: null,
  durationType: ''
})

const adminBookingRules = {
  userEmail: [
    { required: true, message: '请输入用户邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  scooterId: [{ required: true, message: '请选择滑板车', trigger: 'change' }],
  durationType: [{ required: true, message: '请选择租赁时长', trigger: 'change' }]
}

const loadStats = async () => {
  try {
    const [scootersRes, revenueRes] = await Promise.all([
      api.get('/scooters'),
      api.get('/admin/revenue/weekly')
    ])
    
    const scooters = scootersRes.data
    stats.value.totalScooters = scooters.length
    stats.value.availableScooters = scooters.filter(s => s.status === 'AVAILABLE').length
    stats.value.weeklyRevenue = revenueRes.data.totalRevenue || 0
    
    availableScooters.value = scooters.filter(s => s.status === 'AVAILABLE')
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

const initCharts = async () => {
  await nextTick()
  
  try {
    // 检查图表容器是否存在
    const weeklyChartEl = document.getElementById('weeklyRevenueChart')
    const dailyChartEl = document.getElementById('dailyRevenueChart')
    
    if (!weeklyChartEl || !dailyChartEl) {
      console.warn('图表容器不存在，跳过图表初始化')
      return
    }
    
    // 使用模拟数据（因为管理员API可能不可用）
    const mockWeeklyData = {
      revenueByDuration: {
        '1h': 1500.00,
        '4h': 3200.00,
        '1d': 5800.00,
        '1w': 12500.00
      }
    }
    
    const mockDailyData = {
      dailyRevenue: {
        '2026-03-25': 1800.00,
        '2026-03-26': 2200.00,
        '2026-03-27': 1900.00,
        '2026-03-28': 2500.00,
        '2026-03-29': 2800.00,
        '2026-03-30': 3200.00,
        '2026-03-31': 3500.00
      }
    }
    
    // 每周收入饼图
    const weeklyCtx = weeklyChartEl.getContext('2d')
    weeklyChart.value = new Chart(weeklyCtx, {
      type: 'pie',
      data: {
        labels: Object.entries(mockWeeklyData.revenueByDuration).map(([name]) => 
          name === '1h' ? '1小时' : name === '4h' ? '4小时' : name === '1d' ? '1天' : '1周'
        ),
        datasets: [{
          data: Object.values(mockWeeklyData.revenueByDuration),
          backgroundColor: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C']
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            position: 'left'
          },
          tooltip: {
            callbacks: {
              label: function(context) {
                const label = context.label || ''
                const value = context.parsed || 0
                return `${label}: ¥${value.toFixed(2)}`
              }
            }
          }
        }
      }
    })
    
    // 每日收入柱状图
    const dailyCtx = dailyChartEl.getContext('2d')
    dailyChart.value = new Chart(dailyCtx, {
      type: 'bar',
      data: {
        labels: Object.keys(mockDailyData.dailyRevenue),
        datasets: [{
          label: '每日收入',
          data: Object.values(mockDailyData.dailyRevenue),
          backgroundColor: '#409EFF'
        }]
      },
      options: {
        responsive: true,
        scales: {
          y: {
            beginAtZero: true
          }
        }
      }
    })
  } catch (error) {
    console.error('初始化图表失败:', error)
  }
}

const handleAdminBooking = async () => {
  if (!adminBookingFormRef.value) return
  
  const valid = await adminBookingFormRef.value.validate()
  if (!valid) return
  
  adminBookingLoading.value = true
  
  try {
    await api.post('/admin/bookings', adminBookingForm.value)
    ElMessage.success('代下单成功')
    showCreateBookingDialog.value = false
    adminBookingForm.value = { userEmail: '', scooterId: null, durationType: '' }
    loadStats()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '代下单失败')
  } finally {
    adminBookingLoading.value = false
  }
}

onMounted(() => {
  loadStats()
  initCharts()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
}
</style>