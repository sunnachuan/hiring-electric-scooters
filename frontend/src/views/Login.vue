<template>
  <div class="login-page">
    <!-- 动态背景 -->
    <div class="login-background">
      <!-- 背景图片 -->
      <img src="@/assets/images/a1.jpg" alt="登录背景" class="login-background-image" />
      
      <!-- 浮动滑板车图标 -->
      <div class="floating-scooter">🚴</div>
      <div class="floating-scooter">🛴</div>
      <div class="floating-scooter">🚲</div>
      
      <!-- 光点效果 -->
      <div class="light-dots">
        <div class="light-dot"></div>
        <div class="light-dot"></div>
        <div class="light-dot"></div>
        <div class="light-dot"></div>
        <div class="light-dot"></div>
      </div>
    </div>
    
    <!-- 登录表单 -->
    <div class="login-container">
      <el-card class="login-card">
        <template #header>
          <div class="card-header">
            <h2>登录</h2>
          </div>
        </template>
        
        <el-form 
          :model="loginForm" 
          :rules="rules" 
          ref="loginFormRef"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="用户名"
              size="large"
              prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          
          <!-- 条款同意复选框 -->
          <el-form-item prop="termsAgreed">
            <el-checkbox v-model="loginForm.termsAgreed">
              我已阅读并同意
              <el-link type="primary" underline="never" @click="showTermsDialog = true">《滑板车租赁服务协议》</el-link>
            </el-checkbox>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              :loading="loading"
              @click="handleLogin"
              style="width: 100%"
            >
              登录
            </el-button>
          </el-form-item>
          
          <div class="forgot-password">
            <el-link type="primary" @click="showForgotPasswordDialog = true">忘记密码？</el-link>
          </div>
          
          <div class="register-link">
            <span>还没有账号？</span>
            <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
          </div>
        </el-form>
      </el-card>
    </div>
    
    <!-- 找回密码对话框 -->
    <el-dialog 
      v-model="showForgotPasswordDialog" 
      title="找回密码" 
      width="400px"
    >
      <el-form :model="forgotPasswordForm" :rules="forgotPasswordRules" ref="forgotPasswordFormRef">
        <el-form-item prop="email">
          <el-input
            v-model="forgotPasswordForm.email"
            placeholder="请输入注册时使用的邮箱"
            prefix-icon="Message"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showForgotPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleForgotPassword" :loading="forgotPasswordLoading">
          发送重置链接
        </el-button>
      </template>
    </el-dialog>

    <!-- 条款协议弹窗 -->
    <el-dialog 
      v-model="showTermsDialog" 
      title="滑板车租赁服务协议" 
      width="800px"
      top="5vh"
    >
      <div class="terms-dialog-content">
        <div class="terms-section">
          <p><strong>重要提示：</strong>在使用本滑板车租赁服务前，请仔细阅读以下条款。使用服务即表示您同意遵守本协议。</p>
          
          <div class="terms-item">
            <h5>一、用户责任与义务</h5>
            <ul>
              <li>用户须年满16周岁并具备完全民事行为能力</li>
              <li>用户应遵守交通法规，在指定区域内使用滑板车</li>
              <li>禁止酒后驾驶、超载使用或进行危险操作</li>
              <li>用户需妥善保管滑板车，承担使用期间的保管责任</li>
            </ul>
          </div>
          
          <div class="terms-item">
            <h5>二、费用与支付</h5>
            <ul>
              <li>租赁费用按实际使用时间计算，不足1小时按1小时计费</li>
              <li>用户需确保账户余额充足，逾期未支付将产生滞纳金</li>
              <li>如发生设备损坏或遗失，需按实际维修或重置费用赔偿</li>
            </ul>
          </div>
          
          <div class="terms-item">
            <h5>三、安全须知</h5>
            <ul>
              <li>骑行前请检查刹车、轮胎等关键部件是否正常</li>
              <li>建议佩戴安全头盔，夜间骑行需开启车灯</li>
              <li>禁止在雨天、雪天等恶劣天气条件下使用</li>
              <li>如发现设备故障，请立即停止使用并联系客服</li>
            </ul>
          </div>
          
          <div class="terms-item">
            <h5>四、免责声明</h5>
            <ul>
              <li>因用户违规操作造成的损失，平台不承担赔偿责任</li>
              <li>不可抗力因素导致的设备损坏，平台可免除相应责任</li>
              <li>用户应自行购买相关保险以覆盖潜在风险</li>
            </ul>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showTermsDialog = false">关闭</el-button>
        <el-button type="primary" @click="agreeAndClose">同意并关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const loginFormRef = ref()
const forgotPasswordFormRef = ref()
const loading = ref(false)
const showForgotPasswordDialog = ref(false)
const showTermsDialog = ref(false)
const forgotPasswordLoading = ref(false)

const loginForm = ref({
  username: '',
  password: '',
  termsAgreed: false
})

const forgotPasswordForm = ref({
  email: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  termsAgreed: [
    { 
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请同意服务协议'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

const forgotPasswordRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  const valid = await loginFormRef.value.validate()
  if (!valid) return
  
  loading.value = true
  
  try {
    const result = await authStore.login(loginForm.value)
    
    if (result.success) {
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('登录失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleForgotPassword = async () => {
  if (!forgotPasswordFormRef.value) return
  
  const valid = await forgotPasswordFormRef.value.validate()
  if (!valid) return
  
  forgotPasswordLoading.value = true
  
  try {
    // 模拟发送重置链接（实际项目中需要调用后端API）
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success(`重置链接已发送到 ${forgotPasswordForm.value.email}，请查收邮件`)
    showForgotPasswordDialog.value = false
    forgotPasswordForm.value = { email: '' }
  } catch (error) {
    ElMessage.error('发送重置链接失败，请重试')
  } finally {
    forgotPasswordLoading.value = false
  }
}

const agreeAndClose = () => {
  loginForm.value.termsAgreed = true
  showTermsDialog.value = false
}

const handleKeydown = (event) => {
  if (event.key === 'Enter') {
    if (showForgotPasswordDialog.value) {
      handleForgotPassword()
    } else if (showTermsDialog.value) {
      agreeAndClose()
    } else {
      handleLogin()
    }
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
  
  // 检查用户是否已登录，如果已登录则直接跳转到主页
  if (authStore.isAuthenticated) {
    router.push('/')
  }
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
/* 导入动态背景样式 */
@import '@/assets/css/login-animations.css';

.login-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1;
  width: 100%;
  max-width: 400px;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  color: #409EFF;
}

.register-link {
  text-align: center;
  margin-top: 20px;
}

.forgot-password {
  text-align: center;
  margin-bottom: 15px;
}

.register-link span {
  margin-right: 8px;
  color: #606266;
}

/* 条款弹窗样式 */
.terms-dialog-content {
  max-height: 60vh;
  overflow-y: auto;
  padding: 0 10px;
}

.terms-section {
  line-height: 1.8;
  color: #495057;
}

.terms-section > p {
  margin-bottom: 20px;
  font-size: 15px;
  padding: 15px;
  background: #f0f7ff;
  border-radius: 8px;
  border-left: 4px solid #409EFF;
}

.terms-item {
  margin-bottom: 25px;
  background: var(--color-bg-primary);
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.terms-item h5 {
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.terms-item h5::before {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #409EFF;
  border-radius: 50%;
  margin-right: 10px;
}

.terms-item ul {
  margin: 0;
  padding-left: 20px;
}

.terms-item li {
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
  position: relative;
}

.terms-item li::before {
  content: '•';
  color: #409EFF;
  font-weight: bold;
  display: inline-block;
  width: 1em;
  margin-left: -1em;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .terms-dialog-content {
    max-height: 50vh;
    padding: 0 5px;
  }
  
  .terms-item {
    padding: 16px;
    margin-bottom: 20px;
  }
  
  .terms-item h5 {
    font-size: 15px;
  }
  
  .terms-item li {
    font-size: 13px;
  }
}
</style>
