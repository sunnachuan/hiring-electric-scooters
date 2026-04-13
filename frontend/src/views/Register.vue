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
        
        <!-- 免责条款确认 -->
        <el-form-item prop="termsAccepted">
          <el-checkbox v-model="registerForm.termsAccepted" class="terms-checkbox">
            <span class="terms-text">
              我已阅读并同意 <el-link type="primary" @click="showTermsDialog">《用户服务协议与免责条款》</el-link>
            </span>
          </el-checkbox>
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading"
            @click="handleRegister"
            style="width: 100%"
            :disabled="!registerForm.termsAccepted"
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

    <!-- 免责条款对话框 -->
    <el-dialog
      v-model="termsDialogVisible"
      title="用户服务协议与免责条款"
      width="80%"
      :close-on-click-modal="false"
    >
      <div class="terms-content">
        <h3>一、交通保险说明</h3>
        <p>1. 本服务为每辆电动滑板车提供基础交通意外保险，保障范围包括：</p>
        <ul>
          <li>第三方人身伤害：最高赔偿限额10万元</li>
          <li>第三方财产损失：最高赔偿限额5万元</li>
          <li>用户意外伤害：最高赔偿限额5万元</li>
        </ul>
        
        <h3>二、免责条款</h3>
        <p>在以下情况下，本公司不承担任何责任：</p>
        <ul>
          <li>用户未满16周岁或未取得相应驾驶资格</li>
          <li>用户酒后驾驶、吸毒后驾驶或疲劳驾驶</li>
          <li>用户违反交通规则导致的事故</li>
          <li>用户故意损坏车辆或进行危险操作</li>
          <li>用户未在规定区域内使用车辆</li>
          <li>用户未按规定佩戴安全护具</li>
          <li>不可抗力因素（如自然灾害、战争等）</li>
        </ul>

        <h3>三、超时未还车处理方案</h3>
        <p>1. 超时提醒机制：</p>
        <ul>
          <li>超时15分钟：系统自动发送短信/邮件提醒</li>
          <li>超时30分钟：按小时费率自动续费计费</li>
          <li>超时2小时：客服人员电话联系用户</li>
          <li>超时4小时：启动车辆定位和紧急处理程序</li>
        </ul>
        
        <p>2. 自动扣费规则：</p>
        <ul>
          <li>超时30分钟内：按原费率1.5倍计费</li>
          <li>超时1小时内：按原费率2倍计费</li>
          <li>超时2小时以上：按原费率3倍计费，并可能暂停账户使用</li>
        </ul>

        <h3>四、用户义务</h3>
        <p>用户在使用本服务时需遵守以下义务：</p>
        <ul>
          <li>按规定佩戴安全头盔等护具</li>
          <li>遵守交通规则，不闯红灯、不逆行</li>
          <li>不在机动车道行驶，使用非机动车道或人行道</li>
          <li>不超载、不载人，单人使用</li>
          <li>及时归还车辆至指定还车点</li>
          <li>妥善保管车辆，避免丢失或损坏</li>
        </ul>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="termsDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="acceptTerms">同意并继续</el-button>
        </span>
      </template>
    </el-dialog>
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
const termsDialogVisible = ref(false)

const registerForm = ref({
  username: '',
  email: '',
  password: '',
  discounts: [],
  termsAccepted: false
})

const router = useRouter()
const authStore = useAuthStore()

const registerFormRef = ref()
const loading = ref(false)
const termsDialogVisible = ref(false)

const registerForm = ref({
  username: '',
  email: '',
  password: '',
  discounts: [],
  termsAccepted: false
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

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (!/[a-zA-Z]/.test(value)) {
    callback(new Error('密码必须包含字母'))
  } else if (!/[0-9]/.test(value)) {
    callback(new Error('密码必须包含数字'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  termsAccepted: [
    { 
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请阅读并同意用户服务协议与免责条款'))
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
    const userData = {
      ...registerForm.value,
      isStudent: registerForm.value.discounts.includes('isStudent'),
      isSenior: registerForm.value.discounts.includes('isSenior')
    }
    
    await authStore.register(userData)
    ElMessage.success('注册成功')
    router.push('/login')
  } catch (error) {
    ElMessage.error(error.message || '注册失败，请重试')
  } finally {
    loading.value = false
  }
}

const showTermsDialog = () => {
  termsDialogVisible.value = true
}

const acceptTerms = () => {
  registerForm.value.termsAccepted = true
  termsDialogVisible.value = false
  ElMessage.success('已同意用户服务协议与免责条款')
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

.terms-checkbox {
  width: 100%;
  margin-top: 10px;
}

.terms-text {
  font-size: 14px;
  color: #606266;
}

.terms-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 0 10px;
}

.terms-content h3 {
  color: #409EFF;
  margin: 20px 0 10px 0;
  font-size: 16px;
}

.terms-content h3:first-child {
  margin-top: 0;
}

.terms-content p {
  margin: 8px 0;
  line-height: 1.6;
  color: #606266;
}

.terms-content ul {
  margin: 8px 0;
  padding-left: 20px;
}

.terms-content li {
  margin: 4px 0;
  line-height: 1.5;
  color: #606266;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>