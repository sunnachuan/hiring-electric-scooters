<template>
  <div class="login-page">
    <!-- 动态背景 -->
    <div class="login-background">
      <!-- 背景图片 -->
      <img src="@/assets/images/a1.jpg" alt="注册背景" class="login-background-image" />
      
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
    
    <!-- 注册表单 -->
    <div class="login-container">
      <el-card class="login-card">
        <template #header>
          <div class="card-header">
            <h2>注册</h2>
          </div>
        </template>
        
        <el-form 
          :model="registerForm" 
          :rules="rules" 
          ref="registerFormRef"
          @submit.prevent="handleRegister"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="用户名"
              size="large"
              prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="邮箱"
              size="large"
              prefix-icon="Message"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="确认密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          
          <!-- 条款同意复选框 -->
          <el-form-item prop="termsAgreed">
            <el-checkbox v-model="registerForm.termsAgreed">
              我已阅读并同意
              <el-link type="primary" :underline="false" @click="showTermsDialog = true">《滑板车租赁服务协议》</el-link>
            </el-checkbox>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              :loading="loading"
              @click="handleRegister"
              style="width: 100%"
            >
              注册
            </el-button>
          </el-form-item>
          
          <div class="login-link">
            <span>已有账号？</span>
            <el-link type="primary" @click="$router.push('/login')">立即登录</el-link>
          </div>
        </el-form>
      </el-card>
    </div>

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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const registerFormRef = ref()
const loading = ref(false)
const showTermsDialog = ref(false)

const registerForm = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  termsAgreed: false
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
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

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  const valid = await registerFormRef.value.validate()
  if (!valid) return
  
  loading.value = true
  
  try {
    const result = await authStore.register(registerForm.value)
    
    if (result.success) {
      ElMessage.success('注册成功')
      router.push('/login')
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('注册失败，请重试')
  } finally {
    loading.value = false
  }
}

const agreeAndClose = () => {
  registerForm.value.termsAgreed = true
  showTermsDialog.value = false
}
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

.login-link {
  text-align: center;
  margin-top: 20px;
}

.login-link span {
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
  background: white;
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