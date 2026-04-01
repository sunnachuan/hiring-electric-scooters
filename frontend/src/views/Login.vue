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
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const loginFormRef = ref()
const forgotPasswordFormRef = ref()
const loading = ref(false)
const showForgotPasswordDialog = ref(false)
const forgotPasswordLoading = ref(false)

const loginForm = ref({
  username: '',
  password: ''
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
</style>