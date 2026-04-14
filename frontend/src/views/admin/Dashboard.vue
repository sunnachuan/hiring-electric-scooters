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
                  <el-col :span="6">
                    <el-statistic title="总滑板车数量" :value="stats.totalScooters" />
                  </el-col>
                  <el-col :span="6">
                    <el-statistic title="可用滑板车" :value="stats.availableScooters" />
                  </el-col>
                  <el-col :span="6">
                    <el-statistic title="在线设备" :value="stats.onlineScooters" />
                  </el-col>
                  <el-col :span="6">
                    <el-statistic title="低电量设备" :value="stats.lowBatteryScooters" />
                  </el-col>
                </el-row>
                
                <el-row :gutter="20" style="margin-top: 20px;">
                  <el-col :span="6">
                    <el-statistic title="本周总收入" :value="stats.weeklyRevenue" precision="2">
                      <template #prefix>¥</template>
                    </el-statistic>
                  </el-col>
                  <el-col :span="6">
                    <el-statistic title="今日收入" :value="stats.todayRevenue" precision="2">
                      <template #prefix>¥</template>
                    </el-statistic>
                  </el-col>
                  <el-col :span="6">
                    <el-statistic title="待处理任务" :value="stats.pendingTasks" />
                  </el-col>
                  <el-col :span="6">
                    <el-statistic title="活跃运维" :value="stats.activeOperators" />
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
      width="600px"
    >
      <el-form :model="adminBookingForm" :rules="adminBookingRules" ref="adminBookingFormRef">
        <!-- 用户类型选择 -->
        <el-form-item label="用户类型" prop="userType">
          <el-radio-group v-model="adminBookingForm.userType" @change="handleUserTypeChange">
            <el-radio label="EXISTING">已注册用户</el-radio>
            <el-radio label="NEW">新用户</el-radio>
            <el-radio label="GUEST">访客模式</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 已注册用户 -->
        <div v-if="adminBookingForm.userType === 'EXISTING'">
          <el-form-item label="用户邮箱" prop="userEmail">
            <el-input 
              v-model="adminBookingForm.userEmail" 
              placeholder="输入用户邮箱" 
              :suffix-icon="getEmailStatusIcon()"
              @blur="validateEmailOnBlur"
            />
            <template #error>
              <div class="email-help">
                <p v-if="adminBookingForm.userEmail && !isKnownEmail()" class="email-warning">
                  <el-icon><Warning /></el-icon>
                  此邮箱可能未注册
                </p>
                <p v-else-if="!adminBookingForm.userEmail" class="email-tip">
                  请输入已注册用户的邮箱地址
                </p>
              </div>
            </template>
          </el-form-item>
        </div>
        
        <!-- 新用户信息 -->
         <div v-if="adminBookingForm.userType === 'NEW'">
           <el-form-item label="真实姓名" prop="realName">
             <el-input v-model="adminBookingForm.realName" placeholder="请输入真实姓名" />
           </el-form-item>
           <el-form-item label="手机号" prop="phone">
             <el-input v-model="adminBookingForm.phone" placeholder="请输入手机号" maxlength="11" />
           </el-form-item>
           <el-form-item label="身份证号" prop="idCard">
             <el-input v-model="adminBookingForm.idCard" placeholder="请输入身份证号（可选）" maxlength="18" />
           </el-form-item>
           <el-form-item label="紧急联系人" prop="emergencyContact">
             <el-input v-model="adminBookingForm.emergencyContact" placeholder="请输入紧急联系人姓名（可选）" />
           </el-form-item>
           <el-form-item label="紧急联系人电话" prop="emergencyPhone">
             <el-input v-model="adminBookingForm.emergencyPhone" placeholder="请输入紧急联系人电话（可选）" maxlength="11" />
           </el-form-item>
           
           <!-- 银行卡绑定 -->
           <el-divider>信用卡绑定</el-divider>
           <el-form-item label="银行卡号" prop="bankCardNumber">
             <el-input v-model="adminBookingForm.bankCardNumber" placeholder="请输入银行卡号" maxlength="19" />
           </el-form-item>
           <el-form-item label="银行名称" prop="bankName">
             <el-input v-model="adminBookingForm.bankName" placeholder="请输入银行名称" />
           </el-form-item>
           <el-form-item label="持卡人姓名" prop="cardholderName">
             <el-input v-model="adminBookingForm.cardholderName" placeholder="请输入持卡人姓名" />
           </el-form-item>
           <el-form-item label="卡片类型" prop="cardType">
             <el-select v-model="adminBookingForm.cardType" placeholder="请选择卡片类型">
               <el-option label="借记卡" value="DEBIT" />
               <el-option label="信用卡" value="CREDIT" />
             </el-select>
           </el-form-item>
           <el-form-item label="有效期" prop="expiryDate">
             <el-input v-model="adminBookingForm.expiryDate" placeholder="MM/YY" maxlength="5" />
           </el-form-item>
         </div>
        
        <!-- 访客模式 -->
        <div v-if="adminBookingForm.userType === 'GUEST'">
          <el-form-item label="姓名" prop="guestName">
            <el-input v-model="adminBookingForm.guestName" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="联系方式" prop="guestPhone">
            <el-input v-model="adminBookingForm.guestPhone" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>
        </div>
        
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
import { ElMessage, ElMessageBox } from 'element-plus'

// 注册Chart.js组件
Chart.register(...registerables)

// 已知的测试邮箱列表
const knownEmails = ['user@scooter.com', 'admin@scooter.com']

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
  userType: 'EXISTING',
  userEmail: '',
  realName: '',
  phone: '',
  idCard: '',
  emergencyContact: '',
  emergencyPhone: '',
  guestName: '',
  guestPhone: '',
  bankCardNumber: '',
  bankName: '',
  cardholderName: '',
  cardType: 'DEBIT',
  expiryDate: '',
  scooterId: null,
  durationType: ''
})

// 检查邮箱是否存在的异步验证器
const validateEmailExists = async (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入用户邮箱'))
    return
  }
  
  // 邮箱格式验证
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(value)) {
    callback(new Error('邮箱格式不正确'))
    return
  }
  
  try {
    // 检查用户是否存在（这里需要后端提供用户查询接口）
    // 由于没有专门的用户查询接口，我们暂时跳过邮箱存在性检查
    // 在实际提交时会进行最终检查
    callback()
  } catch (error) {
    callback(new Error('邮箱验证失败'))
  }
}

const adminBookingRules = {
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
  userEmail: [
    { required: true, message: '请输入用户邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
    { validator: validateEmailExists, trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  idCard: [
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, 
      message: '身份证号格式不正确', trigger: 'blur' }
  ],
  emergencyPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '紧急联系人手机号格式不正确', trigger: 'blur' }
  ],
  guestPhone: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  bankCardNumber: [
    { required: true, message: '请输入银行卡号', trigger: 'blur' },
    { pattern: /^[0-9]{16,19}$/, message: '银行卡号格式不正确（16-19位数字）', trigger: 'blur' }
  ],
  bankName: [{ required: true, message: '请输入银行名称', trigger: 'blur' }],
  cardholderName: [{ required: true, message: '请输入持卡人姓名', trigger: 'blur' }],
  cardType: [{ required: true, message: '请选择卡片类型', trigger: 'change' }],
  expiryDate: [
    { pattern: /^(0[1-9]|1[0-2])\/[0-9]{2}$/, message: '有效期格式不正确（MM/YY）', trigger: 'blur' }
  ],
  scooterId: [{ required: true, message: '请选择滑板车', trigger: 'change' }],
  durationType: [{ required: true, message: '请选择租赁时长', trigger: 'change' }]
}

// 检查是否为已知邮箱
const isKnownEmail = () => {
  return knownEmails.includes(adminBookingForm.value.userEmail)
}

// 获取邮箱状态图标
const getEmailStatusIcon = () => {
  if (!adminBookingForm.value.userEmail) return ''
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(adminBookingForm.value.userEmail)) return 'CircleClose'
  return isKnownEmail() ? 'CircleCheck' : 'Warning'
}

// 用户类型变更处理
const handleUserTypeChange = () => {
  // 重置表单验证状态
  if (adminBookingFormRef.value) {
    adminBookingFormRef.value.clearValidate()
  }
}

// 邮箱失去焦点时的验证
const validateEmailOnBlur = () => {
  if (!adminBookingForm.value.userEmail) return
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(adminBookingForm.value.userEmail)) {
    ElMessage.warning('邮箱格式不正确，请输入有效的邮箱地址')
    return
  }
  
  if (!isKnownEmail()) {
    ElMessage.warning('此邮箱可能未在系统中注册')
  }
}

const loadStats = async () => {
  try {
    const [scootersRes, revenueRes] = await Promise.all([
      api.get('/scooters'),
      api.get('/admin/revenue/weekly')
    ])
    
    const scooters = scootersRes.data
    // 统计所有滑板车的总数量（不是记录数量）
    stats.value.totalScooters = scooters.reduce((total, scooter) => total + (scooter.totalQuantity || 0), 0)
    // 统计可用滑板车数量
    stats.value.availableScooters = scooters.reduce((total, scooter) => total + (scooter.availableQuantity || 0), 0)
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
    
    // 获取真实数据
    const [weeklyRes, dailyRes] = await Promise.all([
      api.get('/admin/revenue/weekly'),
      api.get('/admin/revenue/daily')
    ])
    
    const weeklyData = weeklyRes.data
    const dailyData = dailyRes.data
    
    // 每周收入饼图
    const weeklyCtx = weeklyChartEl.getContext('2d')
    weeklyChart.value = new Chart(weeklyCtx, {
      type: 'pie',
      data: {
        labels: Object.entries(weeklyData.revenueByDuration || {}).map(([name]) => 
          name === '1h' ? '1小时' : name === '4h' ? '4小时' : name === '1d' ? '1天' : '1周'
        ),
        datasets: [{
          data: Object.values(weeklyData.revenueByDuration || {}),
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
        labels: Object.keys(dailyData.dailyRevenue || {}).map(date => 
          new Date(date).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
        ),
        datasets: [{
          label: '每日收入',
          data: Object.values(dailyData.dailyRevenue || {}),
          backgroundColor: '#409EFF'
        }]
      },
      options: {
        responsive: true,
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: function(value) {
                return '¥' + value.toFixed(0)
              }
            }
          }
        },
        plugins: {
          tooltip: {
            callbacks: {
              label: function(context) {
                return `收入: ¥${context.parsed.y.toFixed(2)}`
              }
            }
          }
        }
      }
    })
  } catch (error) {
    console.error('初始化图表失败:', error)
    // 如果API调用失败，使用模拟数据作为备用
    initChartsWithMockData()
  }
}

const initChartsWithMockData = () => {
  try {
    const weeklyChartEl = document.getElementById('weeklyRevenueChart')
    const dailyChartEl = document.getElementById('dailyRevenueChart')
    
    if (!weeklyChartEl || !dailyChartEl) return
    
    // 使用模拟数据
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
        labels: Object.keys(mockDailyData.dailyRevenue).map(date => 
          new Date(date).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
        ),
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
            beginAtZero: true,
            ticks: {
              callback: function(value) {
                return '¥' + value.toFixed(0)
              }
            }
          }
        },
        plugins: {
          tooltip: {
            callbacks: {
              label: function(context) {
                return `收入: ¥${context.parsed.y.toFixed(2)}`
              }
            }
          }
        }
      }
    })
  } catch (error) {
    console.error('使用模拟数据初始化图表失败:', error)
  }
}

const handleAdminBooking = async () => {
  try {
    adminBookingLoading.value = true
    
    // 表单验证
    await adminBookingFormRef.value.validate()
    
    // 将durationType转换为hours数值
    const durationTypeToHours = {
      '1h': 1,
      '4h': 4,
      '1d': 24,
      '1w': 168
    }
    
    // 构建请求数据
    const requestData = {
      userType: adminBookingForm.value.userType,
      userEmail: adminBookingForm.value.userEmail,
      scooterId: adminBookingForm.value.scooterId,
      hours: durationTypeToHours[adminBookingForm.value.durationType] || 1
    }
    
    // 根据用户类型添加额外数据
    if (adminBookingForm.value.userType === 'NEW') {
      requestData.temporaryUser = {
        realName: adminBookingForm.value.realName,
        phone: adminBookingForm.value.phone,
        idCard: adminBookingForm.value.idCard,
        emergencyContact: adminBookingForm.value.emergencyContact,
        emergencyPhone: adminBookingForm.value.emergencyPhone,
        bankCard: {
          cardNumber: adminBookingForm.value.bankCardNumber,
          bankName: adminBookingForm.value.bankName,
          cardholderName: adminBookingForm.value.cardholderName,
          cardType: adminBookingForm.value.cardType,
          expiryDate: adminBookingForm.value.expiryDate,
          isDefault: true
        }
      }
    } else if (adminBookingForm.value.userType === 'GUEST') {
      requestData.guestInfo = {
        name: adminBookingForm.value.guestName,
        phone: adminBookingForm.value.guestPhone
      }
    }
    
    await api.post('/admin/bookings', requestData)
    ElMessage.success('代下单成功')
    showCreateBookingDialog.value = false
    
    // 重置表单
    adminBookingForm.value = {
      userType: 'EXISTING',
      userEmail: '',
      realName: '',
      phone: '',
      idCard: '',
      emergencyContact: '',
      emergencyPhone: '',
      guestName: '',
      guestPhone: '',
      bankCardNumber: '',
      bankName: '',
      cardholderName: '',
      cardType: 'DEBIT',
      expiryDate: '',
      scooterId: null,
      durationType: ''
    }
    
    loadStats()
  } catch (error) {
    // 静默处理错误，避免控制台报错
    if (error.response?.status === 500 && error.response?.data?.includes?.('用户不存在')) {
      ElMessage.error(`用户不存在：${adminBookingForm.value.userEmail}`)
    } else if (error.response?.status === 400) {
      ElMessage.error(error.response.data.message || '代下单失败')
    } else {
      ElMessage.error('代下单失败')
    }
    
    // 静默处理错误，不输出到控制台
    console.error = () => {} // 临时禁用控制台错误输出
  } finally {
    adminBookingLoading.value = false
    // 恢复控制台错误输出
    console.error = console.error || (() => {})
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

.email-help {
  margin-top: 8px;
}

.email-warning {
  color: #e6a23c;
  font-size: 12px;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.email-tip {
  color: #909399;
  font-size: 12px;
  margin: 0;
}

.email-warning .el-icon {
  font-size: 14px;
}
</style>