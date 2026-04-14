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
      <div class="form-container">
        <div class="form-left">
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
        </div>
        
        <div class="form-right">
          <div class="password-tips">
            <h4>密码要求</h4>
            <ul>
              <li :class="{ 'valid': isLengthValid }">
                <span class="requirement-text">至少6个字符</span>
                <el-icon v-if="isLengthValid" class="check-icon"><CircleCheck /></el-icon>
              </li>
              <li :class="{ 'valid': hasLowerCase }">
                <span class="requirement-text">包含小写字母</span>
                <el-icon v-if="hasLowerCase" class="check-icon"><CircleCheck /></el-icon>
              </li>
              <li :class="{ 'valid': hasNumber }">
                <span class="requirement-text">包含数字</span>
                <el-icon v-if="hasNumber" class="check-icon"><CircleCheck /></el-icon>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, CircleCheck } from '@element-plus/icons-vue'
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 120px);
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
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.form-container {
  display: flex;
  gap: 40px;
  align-items: stretch;
  min-height: 280px;
}

.form-left {
  flex: 0 0 450px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.password-form {
  max-width: 100%;
}

.password-form .el-form-item {
  margin-bottom: 24px;
}

.password-form .el-form-item__label {
  font-size: 20px;
  font-weight: 500;
  color: #303133;
}

.password-form .el-input {
  font-size: 15px;
}

.password-form .el-input__wrapper {
  padding: 12px 16px;
  height: 48px;
  border-radius: 8px;
}

.password-form .el-button {
  padding: 12px 24px;
  font-size: 15px;
  height: 48px;
  border-radius: 8px;
}

.form-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.password-tips {
  padding: 24px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  font-size: 15px;
  min-height: 240px;
}

.password-tips h4 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.password-tips ul {
  margin: 0;
  padding-left: 0;
  list-style: none;
}

.password-tips li {
  margin-bottom: 16px;
  padding: 12px 16px;
  border-radius: 8px;
  background: white;
  border: 1px solid #e9ecef;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 15px;
  transition: all 0.3s ease;
}

.password-tips li.valid {
  background: #f6ffed;
  border-color: #b7eb8f;
  color: #52c41a;
}

.requirement-text {
  flex: 1;
  font-weight: 500;
}

.check-icon {
  color: #52c41a;
  font-size: 18px;
}

.password-tips li:not(.valid) {
  color: #606266;
}

/* 表单样式优化 */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #303133;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409EFF;
}

:deep(.el-button--primary) {
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

@media (max-width: 768px) {
  .change-password-container {
    padding: 16px;
    min-height: calc(100vh - 80px);
  }
  
  .password-form {
    max-width: 100%;
    padding: 0 8px;
  }
  
  .page-header h1 {
    font-size: 28px;
  }
  
  .password-tips {
    padding: 12px;
  }
  
  .password-tips li {
    padding: 6px 8px;
    font-size: 12px;
  }
}
</style>