<template>
  <div class="register-container">
    <el-card class="register-card">
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
            placeholder="密码（至少6位，包含字母和数字）"
            size="large"
            prefix-icon="Lock"
            show-password
            @input="checkPasswordStrength"
          />
          <div class="password-strength" v-if="registerForm.password">
            <div class="strength-bar">
              <div 
                class="strength-level" 
                :class="passwordStrength.level"
                :style="{ width: passwordStrength.width }"
              ></div>
            </div>
            <div class="strength-text">{{ passwordStrength.text }}</div>
            <div class="password-requirements">
              <div :class="{ 'requirement-met': hasMinLength }">✓ 至少6位字符</div>
              <div :class="{ 'requirement-met': hasLetter }">✓ 包含字母</div>
              <div :class="{ 'requirement-met': hasNumber }">✓ 包含数字</div>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-checkbox-group v-model="registerForm.discounts">
            <el-checkbox label="isStudent">我是学生</el-checkbox>
            <el-checkbox label="isSenior">我是老年人</el-checkbox>
          </el-checkbox-group>
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
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const registerFormRef = ref()
const loading = ref(false)

const registerForm = ref({
  username: '',
  email: '',
  password: '',
  discounts: []
})

// 密码强度检查相关
const passwordStrength = ref({
  level: 'weak',
  width: '0%',
  text: ''
})

const hasMinLength = computed(() => registerForm.value.password.length >= 6)
const hasLetter = computed(() => /[a-zA-Z]/.test(registerForm.value.password))
const hasNumber = computed(() => /[0-9]/.test(registerForm.value.password))

const checkPasswordStrength = () => {
  const password = registerForm.value.password
  let score = 0
  
  // 长度检查
  if (password.length >= 6) score += 1
  if (password.length >= 8) score += 1
  
  // 字符类型检查
  if (/[a-z]/.test(password)) score += 1
  if (/[A-Z]/.test(password)) score += 1
  if (/[0-9]/.test(password)) score += 1
  if (/[^a-zA-Z0-9]/.test(password)) score += 1
  
  // 设置强度等级
  if (score <= 2) {
    passwordStrength.value = { level: 'weak', width: '33%', text: '弱' }
  } else if (score <= 4) {
    passwordStrength.value = { level: 'medium', width: '66%', text: '中' }
  } else {
    passwordStrength.value = { level: 'strong', width: '100%', text: '强' }
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请输入密码'))
        } else if (!/[a-zA-Z]/.test(value)) {
          callback(new Error('密码必须包含字母'))
        } else if (!/[0-9]/.test(value)) {
          callback(new Error('密码必须包含数字'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  const valid = await registerFormRef.value.validate()
  if (!valid) return
  
  loading.value = true
  
  try {
    const userData = {
      ...registerForm.value,
      isStudent: registerForm.value.discounts.includes('isStudent'),
      isSenior: registerForm.value.discounts.includes('isSenior')
    }
    
    const result = await authStore.register(userData)
    
    if (result.success) {
      ElMessage.success('注册成功')
      router.push('/')
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('注册失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
  width: 450px;
  max-width: 90vw;
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

.password-strength {
  margin-top: 10px;
}

.strength-bar {
  width: 100%;
  height: 6px;
  background-color: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 5px;
}

.strength-level {
  height: 100%;
  transition: width 0.3s ease;
  border-radius: 3px;
}

.strength-level.weak {
  background-color: #f56c6c;
}

.strength-level.medium {
  background-color: #e6a23c;
}

.strength-level.strong {
  background-color: #67c23a;
}

.strength-text {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.password-requirements {
  font-size: 12px;
  color: #909399;
}

.password-requirements div {
  margin-bottom: 2px;
  color: #c0c4cc;
}

.password-requirements .requirement-met {
  color: #67c23a;
}

.login-link span {
  margin-right: 8px;
  color: #606266;
}
</style>