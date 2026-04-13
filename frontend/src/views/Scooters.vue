<template>
  <div class="scooters-container">
    <div class="page-header">
      <h1 class="page-title">滑板车租赁</h1>
      <p class="page-subtitle">选择您喜欢的滑板车开始租赁之旅</p>
      <div class="header-actions">
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
        <el-button 
          type="info" 
          size="small" 
          circle
          @click="showPricingScheme = true" 
          v-if="!isAdmin"
          class="pricing-btn"
          title="阶梯定价方案"
        >
          <el-icon><PriceTag /></el-icon>
        </el-button>
      </div>
    </div>
    
    <div class="scooters-grid" v-if="!loading">
      <el-card 
        v-for="scooter in groupedScooters" 
        :key="scooter.model" 
        class="scooter-card card-hover fade-in"
        shadow="hover"
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
        <div class="location-info" v-if="scooter.locations && scooter.locations.length > 0">
          <el-tag size="small" type="info">
            <el-icon><Location /></el-icon>
            {{ scooter.locations.join('、') }}
          </el-tag>
        </div>
        
        <!-- 可用数量 -->
        <div class="quantity-info" v-if="scooter.status === 'AVAILABLE'">
          <el-tag type="info" size="small">
            <el-icon><Collection /></el-icon>
            可用: {{ scooter.totalAvailableQuantity }}/{{ scooter.totalQuantity }}
          </el-tag>
        </div>
        
        <!-- 新增设备状态信息 -->
            <div class="device-status" v-if="scooter.batteryLevel !== undefined">
              <div class="status-item">
                <div class="battery-display">
                  <div class="battery-icon" :class="getBatteryClass(scooter.batteryLevel)">
                    <el-icon><Bicycle /></el-icon>
                  </div>
                  <span class="battery-text">{{ scooter.batteryLevel }}%</span>
                </div>
              </div>
          <div class="status-item">
            <el-tag 
              :type="getOnlineTagType(scooter)" 
              size="small"
              class="status-tag"
            >
              {{ getOnlineStatusText(scooter) }}
            </el-tag>
          </div>
          <div class="status-item">
            <el-tag 
              :type="getLockedTagType(scooter)" 
              size="small"
              class="status-tag"
            >
              {{ getLockedStatusText(scooter) }}
            </el-tag>
          </div>
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
            <el-tag size="small" type="info" v-for="feature in getScooterFeatures(scooter.model)" :key="feature">
              {{ feature }}
            </el-tag>
          </div>
        </div>
        
        <div class="scooter-actions" v-if="!isAdmin">
          <el-button 
            :type="getBookingButtonType(scooter)"
            class="book-btn"
            :disabled="scooter.status !== 'AVAILABLE' || scooter.totalAvailableQuantity <= 0"
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
      center
      :before-close="handleBookingClose"
    >
      <el-form :model="bookingForm" :rules="bookingRules" ref="bookingFormRef">
        <!-- 第一步：选择滑板车型号 -->
        <el-form-item label="滑板车型号" prop="scooterModel">
          <el-select v-model="bookingForm.scooterModel" placeholder="请先选择滑板车型号" style="width: 100%" @change="handleModelChange">
            <el-option
              v-for="model in availableModels"
              :key="model"
              :label="model"
              :value="model"
            />
          </el-select>
        </el-form-item>
        
        <!-- 第二步：选择具体位置 -->
        <el-form-item label="取车位置" prop="scooterId" v-if="bookingForm.scooterModel">
          <el-select v-model="bookingForm.scooterId" placeholder="请选择取车位置" style="width: 100%">
            <el-option
              v-for="scooter in filteredScooters"
              :key="scooter.id"
              :label="`${scooter.locationName} - 剩余${scooter.availableQuantity}辆`"
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
        
        <!-- 支付方式选择 -->
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-radio-group v-model="bookingForm.paymentMethod" @change="handlePaymentMethodChange">
            <el-radio label="saved">使用存储的银行卡</el-radio>
            <el-radio label="new">输入新银行卡</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 存储的银行卡选择 -->
        <el-form-item v-if="bookingForm.paymentMethod === 'saved'" label="选择银行卡">
          <el-select v-model="bookingForm.bankCardId" placeholder="请选择银行卡" style="width: 100%">
            <el-option
              v-for="card in bankCards"
              :key="card.id"
              :label="`${card.cardHolderName} - ****${card.cardNumber.slice(-4)}`"
              :value="card.id"
            />
          </el-select>
          <div style="margin-top: 10px;">
            <el-button type="text" size="small" @click="showBankCardDialog = true">
              <el-icon><Plus /></el-icon>
              管理银行卡
            </el-button>
          </div>
        </el-form-item>
        
        <!-- 新银行卡输入 -->
        <div v-if="bookingForm.paymentMethod === 'new'">
          <el-form-item label="银行卡号" prop="cardNumber">
            <el-input v-model="bookingForm.cardNumber" placeholder="请输入银行卡号" />
          </el-form-item>
          
          <el-form-item label="持卡人姓名" prop="cardHolderName">
            <el-input v-model="bookingForm.cardHolderName" placeholder="请输入持卡人姓名" />
          </el-form-item>
          
          <!-- 移除有效期和CVV输入框 -->
          
          <el-form-item>
            <el-checkbox v-model="bookingForm.saveCard">保存此银行卡以便下次使用</el-checkbox>
          </el-form-item>
        </div>
        
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
        

      </div>
      
      <template #footer>
        <el-button @click="showPriceDetails = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 阶梯定价方案弹窗 -->
    <el-dialog 
      v-model="showPricingScheme" 
      title="阶梯定价方案" 
      width="700px"
      center
    >
      <div class="pricing-scheme">
        <el-alert 
          title="定价说明" 
          type="info" 
          description="所有滑板车均采用统一的阶梯定价方案，租赁时间越长，折扣越大" 
          show-icon 
          :closable="false"
          style="margin-bottom: 20px;"
        />
        
        <el-table :data="getPricingTable()" size="small" stripe border>
          <el-table-column prop="duration" label="租赁时长" align="center" />
          <el-table-column prop="discount" label="折扣" align="center" />
          <el-table-column label="计费时长" align="center">
            <template #default="{ row }">
              <div>
                {{ row.durationLabel === '使用时长' ? '使用时长' : row.effectiveHours }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="说明" align="center">
            <template #default="{ row }">
              <el-tag 
                size="small" 
                :class="getPricingTagClass(row.duration)"
              >
                {{ getPricingDescription(row.duration) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pricing-tips" style="margin-top: 20px;">
          <h4>定价规则说明：</h4>
          <ul>
            <li><strong>1-3小时</strong>：按原价计费，无折扣</li>
            <li><strong>4-8小时</strong>：享受85折优惠</li>
            <li><strong>9-24小时</strong>：享受6折优惠，最高按12小时计费</li>
            <li><strong>2-3天</strong>：享受5折优惠，按天计费（每天按12小时计算）</li>
            <li><strong>3天以上</strong>：享受3折优惠，按天计费（每天按12小时计算）</li>
          </ul>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showPricingScheme = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 银行卡管理弹窗 -->
    <el-dialog 
      v-model="showBankCardDialog" 
      title="银行卡管理" 
      width="700px"
      center
    >
      <div class="bank-card-management">
        <!-- 添加新银行卡 -->
        <el-card class="add-card-form" shadow="never">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>添加新银行卡</span>
              <el-button type="text" @click="resetBankCardForm">重置</el-button>
            </div>
          </template>
          
          <el-form :model="bankCardForm" :rules="bankCardRules" ref="bankCardFormRef">
            <el-form-item label="银行卡号" prop="cardNumber">
              <el-input v-model="bankCardForm.cardNumber" placeholder="请输入银行卡号" />
            </el-form-item>
            
            <el-form-item label="持卡人姓名" prop="cardHolderName">
              <el-input v-model="bankCardForm.cardHolderName" placeholder="请输入持卡人姓名" />
            </el-form-item>
            
            <!-- 移除有效期和CVV输入框 -->
            
            <el-form-item>
              <el-checkbox v-model="bankCardForm.isDefault">设为默认支付方式</el-checkbox>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="addBankCard" :loading="bankCardLoading">
                添加银行卡
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <!-- 银行卡列表 -->
        <el-card class="card-list" shadow="never" style="margin-top: 20px;">
          <template #header>
            <span>我的银行卡</span>
          </template>
          
          <div v-if="bankCards.length === 0" class="empty-state">
            <el-empty description="暂无银行卡" />
          </div>
          
          <div v-else class="card-list-content">
            <div 
              v-for="card in bankCards" 
              :key="card.id" 
              class="card-item"
              :class="{ 'default-card': card.isDefault }"
            >
              <div class="card-info">
                <div class="card-number">
                  <el-icon><CreditCard /></el-icon>
                  **** **** **** {{ card.cardNumber.slice(-4) }}
                  <el-tag v-if="card.isDefault" size="small" type="success" style="margin-left: 10px;">
                    默认
                  </el-tag>
                </div>
                <div class="card-details">
                  <span class="card-holder">{{ card.cardHolderName }}</span>
                  <span class="card-expiry">{{ card.expiryMonth }}/{{ card.expiryYear }}</span>
                </div>
              </div>
              
              <div class="card-actions">
                <el-button 
                  v-if="!card.isDefault" 
                  type="text" 
                  size="small" 
                  @click="setDefaultCard(card.id)"
                >
                  设为默认
                </el-button>
                <el-button 
                  type="text" 
                  size="small" 
                  @click="deleteBankCard(card.id)"
                  style="color: #f56c6c;"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      
      <template #footer>
        <el-button @click="showBankCardDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, Bicycle, ShoppingCart, Location, InfoFilled, PriceTag, Picture, Collection, CreditCard, Coin, Goods } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/api'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const isAdmin = computed(() => authStore.userInfo?.role === 'ADMIN')

const scooters = ref([])
const loading = ref(false)
const showBookingDialog = ref(false)
const showPricingScheme = ref(false)
const showPriceDetails = ref(false)
const showBankCardDialog = ref(false)
const bookingLoading = ref(false)
const bankCardLoading = ref(false)
const bookingFormRef = ref()
const bankCardFormRef = ref()
const bankCards = ref([])

const bookingForm = ref({
  scooterModel: '',
  scooterId: null,
  hours: 1,
  paymentMethod: 'saved', // 'saved' 或 'new'
  bankCardId: null,
  cardNumber: '',
  cardHolderName: '',
  expiryMonth: '',
  expiryYear: '',
  cvv: '',
  saveCard: false
})

const bankCardForm = ref({
  cardNumber: '',
  cardHolderName: '',
  expiryMonth: '',
  expiryYear: '',
  cvv: '',
  isDefault: false
})

const bookingRules = {
  scooterModel: [{ required: true, message: '请选择滑板车型号', trigger: 'change' }],
  scooterId: [{ required: true, message: '请选择取车位置', trigger: 'change' }],
  hours: [
    { required: true, message: '请输入租赁时长', trigger: 'blur' },
    { type: 'number', min: 1, max: 168, message: '时长必须在1-168小时之间', trigger: 'blur' }
  ],
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

const bankCardRules = {
  cardNumber: [
    { required: true, message: '请输入银行卡号', trigger: 'blur' },
    { pattern: /^[0-9\s-]{12,19}$/, message: '银行卡号格式不正确', trigger: 'blur' }
  ],
  cardHolderName: [
    { required: true, message: '请输入持卡人姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度应在2-50个字符之间', trigger: 'blur' }
  ]
}

const availableScooters = computed(() => 
  scooters.value.filter(s => s.status === 'AVAILABLE')
)

// 获取所有可用的滑板车型号（去重）
const availableModels = computed(() => {
  const models = availableScooters.value.map(s => s.model)
  return [...new Set(models)]
})

// 根据选择的型号筛选滑板车
const filteredScooters = computed(() => {
  if (!bookingForm.value.scooterModel) {
    return []
  }
  return availableScooters.value.filter(s => s.model === bookingForm.value.scooterModel)
})

// 聚合同款滑板车数据
const groupedScooters = computed(() => {
  const grouped = {}
  
  availableScooters.value.forEach(scooter => {
    if (!grouped[scooter.model]) {
      grouped[scooter.model] = {
        model: scooter.model,
        imageUrl: getScooterImage(scooter.model),
        hourlyRate: scooter.hourlyRate,
        dailyRate: scooter.dailyRate,
        status: scooter.status,
        totalQuantity: 0,
        totalAvailableQuantity: 0,
        locations: [],
        scooters: [], // 保存原始滑板车数据用于预订
        onlineCount: 0,
        lowBatteryCount: 0,
        unlockedCount: 0
      }
    }
    
    const group = grouped[scooter.model]
    group.totalQuantity += scooter.totalQuantity
    group.totalAvailableQuantity += scooter.availableQuantity
    
    // 统计设备状态
    if (scooter.isOnline) group.onlineCount++
    if (scooter.batteryLevel < 20) group.lowBatteryCount++
    if (!scooter.isLocked) group.unlockedCount++
    
    if (scooter.locationName && !group.locations.includes(scooter.locationName)) {
        group.locations.push(scooter.locationName)
      }
      
      group.scooters.push(scooter)
    })
    return Object.values(grouped)
})

// 设备状态相关函数
const getBatteryClass = (level) => {
  if (level >= 50) return 'high'
  if (level >= 20) return 'medium'
  return 'low'
}

const getOnlineTagType = (scooter) => {
  if (!scooter.isOnline) return 'info'
  return 'success'
}

const getOnlineStatusText = (scooter) => {
  return scooter.isOnline ? '在线' : '离线'
}

const getLockedTagType = (scooter) => {
  if (!scooter.isLocked) return 'warning'
  return 'success'
}

const getLockedStatusText = (scooter) => {
  return scooter.isLocked ? '已锁定' : '已解锁'
}

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
  if (scooter.totalAvailableQuantity <= 0) return 'warning'
  return 'primary'
}

// 获取预订按钮文本
const getBookingButtonText = (scooter) => {
  if (scooter.status === 'UNAVAILABLE') return '不可预订'
  if (scooter.totalAvailableQuantity <= 0) return '已租完'
  return '立即预订'
}

// 滑板车图片映射
const scooterImages = {
  '城市通勤款': '/src/assets/images/b1.png',
  '校园轻便款': '/src/assets/images/b2.png',
  '商务精英款': '/src/assets/images/b3.png',
  '时尚潮流款': '/src/assets/images/b4.png',
  '休闲娱乐款': '/src/assets/images/b5.png'
}

// 获取滑板车图片
const getScooterImage = (model) => {
  return scooterImages[model] || ''
}

// 获取滑板车特色标签
const getScooterFeatures = (model) => {
  const featuresMap = {
    '城市通勤款': ['通勤', '续航', '轻便'],
    '校园轻便款': ['学生', '轻便', '经济'],
    '商务精英款': ['商务', '高端', '舒适'],
    '时尚潮流款': ['时尚', '潮流', '个性'],
    '休闲娱乐款': ['休闲', '娱乐', '舒适']
  }
  
  return featuresMap[model] || ['便携', '环保']
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
  // 使用示例价格10元/小时来显示定价方案
  const basePrice = 10
  const durations = [
      { hours: [1, 3], label: '1-3小时' },
      { hours: [4, 8], label: '4-8小时' },
      { hours: [9, 12], label: '9-12小时' },
      { hours: [24], label: '1天' },
      { hours: [48], label: '2天' },
      { hours: [72], label: '3天' },
      { hours: [96], label: '4天' },
      { hours: [120], label: '5天' },
      { hours: [144], label: '6天' },
      { hours: [168], label: '7天' }
    ]
  
  return durations.map(duration => {
      const hours = Array.isArray(duration.hours) ? duration.hours[0] : duration.hours
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
      
      // 前三行显示"使用时长"，后面行显示小时数
      const durationLabel = hours <= 24 ? '使用时长' : '计费时长'
      
      return {
        duration: duration.label,
        discount: `${(discountRate * 100).toFixed(0)}%`,
        effectiveHours: `${effectiveHours}小时`,
        durationLabel: durationLabel
      }
    })
}

// 获取定价标签类名
const getPricingTagClass = (duration) => {
  if (duration === '1-3小时') {
    return 'pricing-tag-gray' // 原价计费 - 浅灰色
  } else if (duration === '4-8小时') {
    return 'pricing-tag-yellow' // 85折 - 浅黄色
  } else if (duration === '9-12小时' || duration === '1天') {
    return 'pricing-tag-green' // 6折 - 浅绿色
  } else if (duration === '2天' || duration === '3天') {
    return 'pricing-tag-blue' // 5折 - 浅蓝色
  } else {
    return 'pricing-tag-purple' // 3折 - 浅紫色
  }
}

// 获取定价说明
const getPricingDescription = (duration) => {
  if (duration === '1-3小时') {
    return '原价计费'
  } else if (duration === '4-8小时') {
    return '85折优惠'
  } else if (duration === '9-12小时' || duration === '1天') {
    return '6折优惠'
  } else if (duration === '2天' || duration === '3天') {
    return '5折优惠'
  } else {
    return '3折优惠'
  }
}

const handleModelChange = () => {
  // 当型号改变时，重置位置选择
  bookingForm.value.scooterId = null
}

const openBookingDialog = (groupedScooter) => {
  // 检查滑板车是否可用
  if (groupedScooter.status === 'UNAVAILABLE' || groupedScooter.totalAvailableQuantity <= 0) {
    ElMessage.warning('该滑板车暂不可租用')
    return
  }
  
  // 设置默认选择的型号
  bookingForm.value.scooterModel = groupedScooter.model
  // 不设置默认位置，让用户选择
  bookingForm.value.scooterId = null
  showBookingDialog.value = true
}

const handleBookingClose = () => {
  showBookingDialog.value = false
  bookingForm.value = {
    scooterModel: null,
    scooterId: null,
    hours: 1,
    cardNumber: '123456789012' // 重置时保留默认信用卡号
  }
}



// 银行卡管理相关方法
const handlePaymentMethodChange = (method) => {
  if (method === 'saved') {
    loadBankCards()
  }
}

const loadBankCards = async () => {
  try {
    const response = await api.get('/bank-cards')
    bankCards.value = response.data
    
    // 如果有默认卡，自动选择
    const defaultCard = bankCards.value.find(card => card.isDefault)
    if (defaultCard) {
      bookingForm.value.bankCardId = defaultCard.id
    }
  } catch (error) {
    console.error('加载银行卡失败:', error)
  }
}

const addBankCard = async () => {
  if (!bankCardFormRef.value) return
  
  try {
    await bankCardFormRef.value.validate()
  } catch (error) {
    return
  }
  
  bankCardLoading.value = true
  
  try {
    const bankCardData = {
      cardNumber: bankCardForm.value.cardNumber,
      cardHolderName: bankCardForm.value.cardHolderName
    }
    const response = await api.post('/bank-cards', bankCardData)
    ElMessage.success('银行卡添加成功')
    
    // 重新加载银行卡列表
    await loadBankCards()
    
    // 重置表单
    resetBankCardForm()
  } catch (error) {
    console.error('添加银行卡失败:', error)
    ElMessage.error('添加银行卡失败，请重试')
  } finally {
    bankCardLoading.value = false
  }
}

const setDefaultCard = async (cardId) => {
  try {
    await api.put(`/bank-cards/${cardId}/default`)
    ElMessage.success('默认银行卡设置成功')
    await loadBankCards()
  } catch (error) {
    console.error('设置默认卡失败:', error)
    ElMessage.error('设置默认卡失败，请重试')
  }
}

const deleteBankCard = async (cardId) => {
  try {
    await ElMessageBox.confirm('确定要删除这张银行卡吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.delete(`/bank-cards/${cardId}`)
    ElMessage.success('银行卡删除成功')
    await loadBankCards()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除银行卡失败:', error)
      ElMessage.error('删除银行卡失败，请重试')
    }
  }
}

const resetBankCardForm = () => {
  bankCardForm.value = {
    cardNumber: '',
    cardHolderName: '',
    expiryMonth: '',
    expiryYear: '',
    cvv: '',
    isDefault: false
  }
}

// 修改预订处理逻辑
const handleBooking = async () => {
  if (!bookingFormRef.value) return
  
  try {
    await bookingFormRef.value.validate()
  } catch (error) {
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
    
    // 构建预订数据
    const bookingData = {
      scooterId: bookingForm.value.scooterId,
      hours: bookingForm.value.hours
    }
    
    // 根据支付方式添加支付信息
    if (bookingForm.value.paymentMethod === 'saved') {
      bookingData.bankCardId = bookingForm.value.bankCardId
    } else {
      bookingData.cardNumber = bookingForm.value.cardNumber
      
      // 如果需要保存银行卡
      if (bookingForm.value.saveCard) {
        try {
          const bankCardData = {
            cardNumber: bookingForm.value.cardNumber,
            cardHolderName: bookingForm.value.cardHolderName
          }
          await api.post('/bank-cards', bankCardData)
          ElMessage.success('银行卡已保存')
        } catch (error) {
          console.error('保存银行卡失败:', error)
          // 银行卡保存失败不影响预订
        }
      }
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
/* 弹窗样式优化 */
.el-dialog {
  border-radius: 12px !important;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2) !important;
  overflow: hidden;
}

.el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: white !important;
  padding: 16px 20px !important;
  margin: 0 !important;
}

.el-dialog__title {
  color: white !important;
  font-weight: 600 !important;
  font-size: 18px !important;
}

.el-dialog__body {
  padding: 24px 20px !important;
}

.el-dialog__footer {
  padding: 16px 20px !important;
  border-top: 1px solid #f0f0f0;
}

/* 弹窗在手机版的居中样式 */
@media (max-width: 768px) {
  .el-dialog {
    margin: 0 auto !important;
    max-width: 95vw !important;
    width: 95vw !important;
    top: 50% !important;
    left: 50% !important;
    transform: translate(-50%, -50%) !important;
    position: fixed !important;
  }
  
  .el-dialog__wrapper {
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    background: rgba(0, 0, 0, 0.5) !important;
  }
  
  .el-dialog__header {
    padding: 12px 16px !important;
  }
  
  .el-dialog__body {
    padding: 20px 16px !important;
    max-height: 70vh !important;
    overflow-y: auto !important;
  }
  
  .el-dialog__footer {
     padding: 12px 16px !important;
   }
   
   /* 弹窗内表单布局优化 */
   .el-form-item {
     margin-bottom: 16px !important;
   }
   
   .el-form-item__label {
     font-size: 14px !important;
     margin-bottom: 6px !important;
   }

/* 银行卡管理样式 */
.bank-card-management {
  max-height: 60vh;
  overflow-y: auto;
}

.add-card-form {
  margin-bottom: 20px;
}

.card-list-content {
  max-height: 300px;
  overflow-y: auto;
}

.card-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  margin-bottom: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.card-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.card-item.default-card {
  border-color: #67c23a;
  background-color: rgba(103, 194, 58, 0.05);
}

.card-info {
  flex: 1;
}

.card-number {
  display: flex;
  align-items: center;
  font-weight: 600;
  margin-bottom: 8px;
}

.card-number .el-icon {
  margin-right: 8px;
  color: #409eff;
}

.card-details {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #666;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

/* 支付方式选择样式 */
.el-radio-group {
  width: 100%;
}

.el-radio {
  margin-right: 0;
  width: 50%;
  text-align: center;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .card-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .card-actions {
    margin-top: 12px;
    width: 100%;
    justify-content: flex-end;
  }
  
  .el-radio {
    width: 100%;
    margin-bottom: 8px;
  }
}
   
   .el-input, .el-select, .el-input-number {
     width: 100% !important;
   }
   
   .duration-input-group {
     display: flex !important;
     flex-direction: column !important;
     gap: 8px !important;
   }
   
   .duration-input-group .el-input-number,
   .duration-input-group .el-select {
     width: 100% !important;
     margin-right: 0 !important;
   }
   
   .duration-tips small {
     font-size: 12px !important;
     color: #666 !important;
   }
 }

.scooters-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
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

.header-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 20px;
}

.booking-btn {
  transition: all 0.3s ease;
}

.booking-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.pricing-btn {
  transition: all 0.3s ease;
  width: 36px;
  height: 36px;
  font-size: 16px;
}

.pricing-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

.pricing-scheme {
  max-height: 500px;
  overflow-y: auto;
}

.pricing-tips ul {
  margin: 10px 0;
  padding-left: 20px;
}

.pricing-tips li {
  margin: 5px 0;
  color: #666;
  line-height: 1.6;
}

.pricing-tips strong {
  color: #333;
}

/* 定价标签浅色系样式 */
.pricing-tag-gray {
  background-color: #f8f9fa !important;
  border-color: #e9ecef !important;
  color: #6c757d !important;
}

.pricing-tag-yellow {
  background-color: #fff9c4 !important;
  border-color: #fff59d !important;
  color: #8d6e00 !important;
}

.pricing-tag-green {
  background-color: #c8e6c9 !important;
  border-color: #a5d6a7 !important;
  color: #2e7d32 !important;
}

.pricing-tag-blue {
  background-color: #bbdefb !important;
  border-color: #90caf9 !important;
  color: #1565c0 !important;
}

.pricing-tag-purple {
  background-color: #e1bee7 !important;
  border-color: #ce93d8 !important;
  color: #6a1b9a !important;
}

/* 更亮的info标签样式 */
.scooter-card .el-tag--info {
  background-color: #e3f2fd !important;
  border-color: #bbdefb !important;
  color: #1976d2 !important;
}

.scooter-card .el-tag--info:hover {
  background-color: #bbdefb !important;
  border-color: #90caf9 !important;
}

/* 标题图标样式 */
.title-icon {
  font-size: 36px;
  color: #667eea;
  margin-right: 12px;
  vertical-align: middle;
}

.page-title {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Footer样式 */
.page-footer {
  background-color: #f8f9fa;
  border-top: 1px solid #e9ecef;
  margin-top: 60px;
  padding: 40px 0 20px;
}

.footer-content {
  display: flex;
  justify-content: space-around;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-section {
  flex: 1;
  text-align: center;
  padding: 0 20px;
}

.footer-section h4 {
  color: #333;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 600;
}

.footer-section p {
  color: #666;
  margin: 8px 0;
  font-size: 14px;
  line-height: 1.5;
}

.footer-bottom {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
}

.footer-bottom p {
  color: #999;
  font-size: 14px;
  margin: 0;
}
</style>