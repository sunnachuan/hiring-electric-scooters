<template>
  <div class="change-password-container">
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button @click="goBack" type="primary" size="large">
        <el-icon><ArrowLeft /></el-icon>
        返回个人中心
      </el-button>
    </div>

    <!-- 页面标题 -->
    <div class="page-header">
      <h1>更改密码</h1>
    </div>

    <!-- 密码修改表单 -->
    <el-card class="password-form-card">
      <el-form 
        :model="passwordForm" 
        :rules="passwordRules" 
        ref="passwordFormRef" 
        label-width="100px" 
        class="password-form"
      >
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input 
            v-model="passwordForm.currentPassword" 
            type="password" 
            placeholder="请输入当前密码" 
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            placeholder="请输入新密码" 
            show-password
          />
          <div class="password-tips">
            <p>密码要求：</p>
            <ul>
              <li :class="{ 'valid': isLengthValid }">至少6个字符</li>
              <li :class="{ 'valid': hasLowerCase }">包含小写字母</li>
              <li :class="{ 'valid': hasNumber }">包含数字</li>
            </ul>
          </div>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码" 
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="changePassword" :loading="loading">
            确认修改
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const passwordFormRef = ref()

// 加载状态
const loading = ref(false)

// 密码表单数据
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码验证规则
const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请输入新密码'))
        } else if (!/[a-z]/.test(value)) {
          callback(new Error('密码必须包含小写字母'))
        } else if (!/\d/.test(value)) {
          callback(new Error('密码必须包含数字'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 密码强度验证
const isLengthValid = computed(() => passwordForm.newPassword.length >= 6)
const hasLowerCase = computed(() => /[a-z]/.test(passwordForm.newPassword))
const hasNumber = computed(() => /\d/.test(passwordForm.newPassword))

// 返回个人中心
const goBack = () => {
  router.push('/profile')
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    const valid = await passwordFormRef.value.validate()
    if (!valid) return
    
    loading.value = true
    
    // 调用实际的API
    const result = await authStore.changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    
    if (result.success) {
      ElMessage.success(result.message || '密码修改成功')
      resetForm()
    } else {
      ElMessage.error(result.message || '密码修改失败')
    }
  } catch (error) {
    if (error.errors) {
      ElMessage.error('请检查表单填写是否正确')
    } else {
      ElMessage.error('密码修改失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields()
  }
}
</script>

<style scoped>
.change-password-container {
  max-width: 600px;
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

.password-form-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.password-form {
  max-width: 500px;
  margin: 0 auto;
}

.password-tips {
  margin-top: 8px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
}

.password-tips p {
  margin: 0 0 8px 0;
  font-weight: 500;
  color: #606266;
}

.password-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #909399;
}

.password-tips li {
  margin-bottom: 4px;
  transition: color 0.3s ease;
}

.password-tips li.valid {
  color: #67c23a;
}

@media (max-width: 768px) {
  .change-password-container {
    padding: 16px;
  }
  
  .password-form {
    max-width: 100%;
  }
}
</style>