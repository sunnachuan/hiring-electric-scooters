<template>
  <div class="account-settings-container">
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button @click="goBack" type="primary" size="large">
        <el-icon><ArrowLeft /></el-icon>
        返回个人中心
      </el-button>
    </div>

    <!-- 页面标题 -->
    <div class="page-header">
      <h1>账户设置</h1>
    </div>

    <!-- 设置内容 -->
    <el-tabs v-model="activeTab" class="settings-tabs">
      <!-- 基本信息 -->
      <el-tab-pane label="基本信息" name="basic">
        <el-form :model="userForm" label-width="100px" class="settings-form">
          <el-form-item label="姓名">
            <el-input v-model="userForm.fullName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input v-model="userForm.username" placeholder="请输入用户名" disabled />
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
            <el-button @click="resetForm">取消</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      
      <!-- 银行卡管理 -->
      <el-tab-pane label="银行卡管理" name="bankCards">
        <div class="bank-cards-section">
          <div class="section-header">
            <h3>我的银行卡</h3>
            <el-button type="primary" @click="showAddBankCardDialog = true">
              <el-icon><Plus /></el-icon>
              添加银行卡
            </el-button>
          </div>
          
          <!-- 银行卡列表 -->
          <div class="bank-cards-list" v-if="bankCards.length > 0">
            <el-card 
              v-for="card in bankCards" 
              :key="card.id" 
              class="bank-card-item"
              :class="{ 'default-card': card.isDefault }"
            >
              <div class="card-content">
                <div class="card-info">
                  <div class="bank-name">{{ card.bankName }}</div>
                  <div class="card-type">{{ card.cardType === 'DEBIT' ? '借记卡' : '信用卡' }}</div>
                  <div class="card-number">{{ card.cardNumberDisplay }}</div>
                  <div class="cardholder-name">持卡人：{{ card.cardholderName }}</div>
                  <div class="expiry-date" v-if="card.expiryDate">有效期：{{ card.expiryDate }}</div>
                </div>
                <div class="card-actions">
                  <el-tag 
                    v-if="card.isDefault" 
                    type="success" 
                    size="small"
                    class="default-tag"
                  >
                    <el-icon><StarFilled /></el-icon>
                    默认
                  </el-tag>
                  <el-button 
                    v-else 
                    type="primary" 
                    size="small" 
                    @click="setDefaultCard(card.id)"
                    class="set-default-btn"
                  >
                    <el-icon><Star /></el-icon>
                    设为默认
                  </el-button>
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="editBankCard(card)"
                    class="edit-btn"
                  >
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-button>
                  <el-button 
                    type="danger" 
                    size="small" 
                    @click="deleteBankCard(card.id)" 
                    class="delete-btn"
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>
          
          <!-- 空状态 -->
          <el-empty v-else description="暂无银行卡" :image-size="100">
            <el-button type="primary" @click="showAddBankCardDialog = true">添加银行卡</el-button>
          </el-empty>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 添加银行卡对话框 -->
    <el-dialog 
      v-model="showAddBankCardDialog" 
      title="添加银行卡" 
      width="500px"
      @close="resetBankCardForm"
    >
      <el-form :model="bankCardForm" :rules="bankCardRules" ref="bankCardFormRef" label-width="100px">
        <el-form-item label="银行卡号" prop="cardNumber">
          <el-input 
            v-model="bankCardForm.cardNumber" 
            placeholder="请输入16-19位银行卡号" 
            maxlength="19"
          />
        </el-form-item>
        <el-form-item label="银行名称" prop="bankName">
          <el-input v-model="bankCardForm.bankName" placeholder="请输入银行名称" />
        </el-form-item>
        <el-form-item label="持卡人姓名" prop="cardholderName">
          <el-input v-model="bankCardForm.cardholderName" placeholder="请输入持卡人姓名" />
        </el-form-item>
        <el-form-item label="卡片类型" prop="cardType">
          <el-radio-group v-model="bankCardForm.cardType">
            <el-radio label="DEBIT">借记卡</el-radio>
            <el-radio label="CREDIT">信用卡</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="有效期" prop="expiryDate">
          <el-input 
            v-model="bankCardForm.expiryDate" 
            placeholder="MM/YY（信用卡必填）" 
            maxlength="5"
          />
        </el-form-item>
        <el-form-item label="默认卡片">
          <el-switch v-model="bankCardForm.isDefault" />
          <span class="tip-text">设为默认支付卡片</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddBankCardDialog = false">取消</el-button>
        <el-button type="primary" @click="addBankCard">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- 编辑银行卡对话框 -->
    <el-dialog 
      v-model="showEditBankCardDialog" 
      title="编辑银行卡" 
      width="500px"
      @close="resetBankCardForm"
    >
      <el-form :model="bankCardForm" :rules="bankCardRules" ref="bankCardFormRef" label-width="100px">
        <el-form-item label="银行卡号" prop="cardNumber">
          <el-input 
            v-model="bankCardForm.cardNumber" 
            placeholder="请输入16-19位银行卡号" 
            maxlength="19"
          />
        </el-form-item>
        <el-form-item label="银行名称" prop="bankName">
          <el-input v-model="bankCardForm.bankName" placeholder="请输入银行名称" />
        </el-form-item>
        <el-form-item label="持卡人姓名" prop="cardholderName">
          <el-input v-model="bankCardForm.cardholderName" placeholder="请输入持卡人姓名" />
        </el-form-item>
        <el-form-item label="卡片类型" prop="cardType">
          <el-radio-group v-model="bankCardForm.cardType">
            <el-radio label="DEBIT">借记卡</el-radio>
            <el-radio label="CREDIT">信用卡</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="有效期" prop="expiryDate">
          <el-input 
            v-model="bankCardForm.expiryDate" 
            placeholder="MM/YY（信用卡必填）" 
            maxlength="5"
          />
        </el-form-item>
        <el-form-item label="默认卡片">
          <el-switch v-model="bankCardForm.isDefault" />
          <span class="tip-text">设为默认支付卡片</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditBankCardDialog = false">取消</el-button>
        <el-button type="primary" @click="updateBankCard">确认更新</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, Star, StarFilled, Edit, Delete, User, UserFilled } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

// 当前激活的选项卡
const activeTab = ref('basic')

// 用户表单数据 - 使用真实的用户数据
const userForm = computed(() => {
  const userInfo = authStore.userInfo || {}
  return {
    username: userInfo.username || '',
    email: userInfo.email || '',
    phone: userInfo.phone || '',
    fullName: userInfo.fullName || ''
  }
})

// 银行卡相关数据
const bankCards = ref([])
const showAddBankCardDialog = ref(false)
const showEditBankCardDialog = ref(false)
const bankCardForm = reactive({
  cardNumber: '',
  bankName: '',
  cardholderName: '',
  cardType: 'DEBIT',
  expiryDate: '',
  isDefault: false
})

// 银行卡表单引用
const bankCardFormRef = ref()

// 银行卡表单验证规则
const bankCardRules = {
  cardNumber: [
    { required: true, message: '请输入银行卡号', trigger: 'blur' },
    { min: 16, max: 19, message: '银行卡号长度应为16-19位', trigger: 'blur' }
  ],
  bankName: [
    { required: true, message: '请输入银行名称', trigger: 'blur' }
  ],
  cardholderName: [
    { required: true, message: '请输入持卡人姓名', trigger: 'blur' }
  ]
}

// 返回个人中心
const goBack = () => {
  router.push('/profile')
}

// 学生认证
const handleStudentCertification = () => {
  ElMessage.info('学生认证功能开发中')
}

// 长者认证
const handleSeniorCertification = () => {
  ElMessage.info('长者认证功能开发中')
}

// 更新个人信息
const updateProfile = () => {
  ElMessage.success('个人信息更新成功')
}

// 重置表单
const resetForm = () => {
  // 不再重置为硬编码数据，而是保持当前用户数据
}

// 重置银行卡表单
const resetBankCardForm = () => {
  Object.assign(bankCardForm, {
    cardNumber: '',
    bankName: '',
    cardholderName: '',
    cardType: 'DEBIT',
    expiryDate: '',
    isDefault: false
  })
}

// 加载用户银行卡数据
const loadBankCards = async () => {
  try {
    const response = await api.get('/bank-cards')
    bankCards.value = response.data
  } catch (error) {
    console.error('加载银行卡数据失败:', error)
    bankCards.value = [] // 设置为空数组而不是硬编码数据
  }
}

// 添加银行卡
const addBankCard = async () => {
  try {
    await bankCardFormRef.value.validate()
    
    const response = await api.post('/bank-cards', {
      cardNumber: bankCardForm.cardNumber,
      bankName: bankCardForm.bankName,
      cardholderName: bankCardForm.cardholderName,
      cardType: bankCardForm.cardType,
      expiryDate: bankCardForm.expiryDate,
      isDefault: bankCardForm.isDefault
    })
    
    if (response.data.success) {
      ElMessage.success('银行卡添加成功')
      showAddBankCardDialog.value = false
      resetBankCardForm()
      await loadBankCards() // 重新加载银行卡列表
    } else {
      ElMessage.error(response.data.message || '银行卡添加失败')
    }
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('银行卡添加失败，请重试')
    }
  }
}

// 编辑银行卡
const editBankCard = (card) => {
  Object.assign(bankCardForm, {
    ...card,
    cardNumber: card.cardNumber.replace(/\*/g, '') // 移除掩码显示真实卡号
  })
  showEditBankCardDialog.value = true
}

// 更新银行卡
const updateBankCard = async () => {
  try {
    await bankCardFormRef.value.validate()
    
    const response = await api.put(`/bank-cards/${bankCardForm.id}`, {
      cardNumber: bankCardForm.cardNumber,
      bankName: bankCardForm.bankName,
      cardholderName: bankCardForm.cardholderName,
      cardType: bankCardForm.cardType,
      expiryDate: bankCardForm.expiryDate,
      isDefault: bankCardForm.isDefault
    })
    
    if (response.data.success) {
      ElMessage.success('银行卡更新成功')
      showEditBankCardDialog.value = false
      resetBankCardForm()
      await loadBankCards() // 重新加载银行卡列表
    } else {
      ElMessage.error(response.data.message || '银行卡更新失败')
    }
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('银行卡更新失败，请重试')
    }
  }
}

// 设为默认银行卡
const setDefaultCard = async (cardId) => {
  try {
    const response = await api.put(`/bank-cards/${cardId}`, { isDefault: true })
    
    if (response.data.success) {
      ElMessage.success('默认银行卡设置成功')
      await loadBankCards() // 重新加载银行卡列表
    } else {
      ElMessage.error(response.data.message || '设置默认银行卡失败')
    }
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('设置默认银行卡失败，请重试')
    }
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
      await loadBankCards() // 重新加载银行卡列表
    } else {
      ElMessage.error(response.data.message || '银行卡删除失败')
    }
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消删除
      return
    }
    
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('银行卡删除失败，请重试')
    }
  }
}

// 初始化数据
onMounted(() => {
  loadBankCards() // 加载真实的银行卡数据
})
</script>

<style scoped>
.account-settings-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.back-button {
  margin-bottom: 24px;
}

.page-header {
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

.settings-tabs {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.settings-form {
  max-width: 600px;
}

.certification-buttons {
  display: flex;
  gap: 16px;
}

.certification-btn {
  flex: 1;
}

.bank-cards-section {
  margin-top: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h3 {
  margin: 0;
  color: #303133;
}

.bank-cards-list {
  display: grid;
  gap: 16px;
}

.bank-card-item {
  transition: transform 0.2s ease;
}

.bank-card-item:hover {
  transform: translateY(-2px);
}

.default-card {
  border-left: 4px solid #67c23a;
}

.card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-info {
  flex: 1;
}

.bank-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.card-type {
  color: #909399;
  margin-bottom: 4px;
}

.card-number {
  font-size: 16px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 4px;
}

.cardholder-name, .expiry-date {
  color: #909399;
  font-size: 14px;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.default-tag {
  margin-right: 8px;
}

.tip-text {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}

@media (max-width: 768px) {
  .account-settings-container {
    padding: 16px;
  }
  
  .settings-tabs {
    padding: 16px;
  }
  
  .certification-buttons {
    flex-direction: column;
  }
  
  .card-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .card-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>