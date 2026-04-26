<template>
  <div class="email-test-container">
    <div class="email-test-card">
      <el-card class="box-card">
        <template #header>
          <div class="card-header">
            <h2>📧 邮件发送测试</h2>
            <p>测试电动滑板车租赁服务的邮件发送功能</p>
          </div>
        </template>
        
        <!-- 邮件配置状态 -->
        <div class="status-section">
          <el-button type="primary" @click="checkEmailStatus" :loading="statusLoading">
            {{ statusLoading ? '检查中...' : '检查邮件配置状态' }}
          </el-button>
          
          <div v-if="emailStatus" class="status-info">
            <el-alert 
              :title="emailStatus.message" 
              :type="emailStatus.success ? 'success' : 'error'"
              :closable="false"
              show-icon
            >
              <template v-if="emailStatus.success">
                <p><strong>发件人邮箱：</strong>{{ emailStatus.username }}</p>
                <p><strong>SMTP服务器：</strong>{{ emailStatus.host }}:{{ emailStatus.port }}</p>
                <p><strong>检查时间：</strong>{{ emailStatus.checkTime }}</p>
              </template>
            </el-alert>
          </div>
        </div>
        
        <!-- 测试邮件发送 -->
        <div class="test-section">
          <h3>发送测试邮件</h3>
          
          <el-form :model="testForm" :rules="testRules" ref="testFormRef" label-width="120px">
            <el-form-item label="收件人邮箱" prop="toEmail">
              <el-input 
                v-model="testForm.toEmail" 
                placeholder="请输入要测试的邮箱地址"
                size="large"
              />
            </el-form-item>
            
            <el-form-item label="邮件主题" prop="subject">
              <el-input 
                v-model="testForm.subject" 
                placeholder="请输入邮件主题"
                size="large"
              />
            </el-form-item>
            
            <el-form-item label="邮件内容" prop="message">
              <el-input 
                v-model="testForm.message" 
                type="textarea" 
                :rows="4"
                placeholder="请输入邮件内容"
                size="large"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="primary" 
                size="large" 
                @click="sendTestEmail"
                :loading="sending"
                style="width: 100%"
              >
                {{ sending ? '发送中...' : '发送测试邮件' }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 测试结果 -->
        <div v-if="testResult" class="result-section">
          <el-alert 
            :title="testResult.message" 
            :type="testResult.success ? 'success' : 'error'"
            :closable="false"
            show-icon
          >
            <template v-if="testResult.success">
              <p><strong>收件人：</strong>{{ testResult.toEmail }}</p>
              <p><strong>发送时间：</strong>{{ testResult.sentTime }}</p>
            </template>
            <template v-else>
              <p><strong>错误信息：</strong>{{ testResult.message }}</p>
            </template>
          </el-alert>
        </div>
        
        <!-- 注册成功邮件测试 -->
        <div class="registration-test-section">
          <h3>注册成功邮件测试</h3>
          
          <el-form :model="registrationForm" :rules="registrationRules" ref="registrationFormRef" label-width="120px">
            <el-form-item label="收件人邮箱" prop="toEmail">
              <el-input 
                v-model="registrationForm.toEmail" 
                placeholder="请输入要测试的邮箱地址"
                size="large"
              />
            </el-form-item>
            
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="registrationForm.username" 
                placeholder="请输入测试用户名"
                size="large"
              />
            </el-form-item>
            
            <el-form-item label="真实姓名" prop="fullName">
              <el-input 
                v-model="registrationForm.fullName" 
                placeholder="请输入真实姓名"
                size="large"
              />
            </el-form-item>
            
            <el-form-item label="手机号码" prop="phone">
              <el-input 
                v-model="registrationForm.phone" 
                placeholder="请输入手机号码"
                size="large"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="success" 
                size="large" 
                @click="sendRegistrationEmail"
                :loading="sendingRegistration"
                style="width: 100%"
              >
                {{ sendingRegistration ? '发送中...' : '发送注册成功邮件' }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 返回按钮 -->
        <div class="action-section">
          <el-button @click="goBack" size="large">返回个人中心</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api'

const router = useRouter()

// 邮件配置状态
const emailStatus = ref(null)
const statusLoading = ref(false)

// 测试邮件表单
const testForm = reactive({
  toEmail: '',
  subject: '邮件发送功能测试',
  message: '这是一封测试邮件，用于验证电动滑板车租赁服务的邮件发送功能是否正常工作。'
})

const testFormRef = ref()
const sending = ref(false)
const testResult = ref(null)

// 注册成功邮件表单
const registrationForm = reactive({
  toEmail: '',
  username: '',
  fullName: '',
  phone: ''
})

const registrationFormRef = ref()
const sendingRegistration = ref(false)

// 表单验证规则
const testRules = {
  toEmail: [
    { required: true, message: '请输入收件人邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  subject: [
    { required: true, message: '请输入邮件主题', trigger: 'blur' }
  ],
  message: [
    { required: true, message: '请输入邮件内容', trigger: 'blur' }
  ]
}

const registrationRules = {
  toEmail: [
    { required: true, message: '请输入收件人邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ]
}

// 检查邮件配置状态
const checkEmailStatus = async () => {
  statusLoading.value = true
  try {
    const response = await api.get('/api/email/status')
    emailStatus.value = response.data
    ElMessage.success('邮件配置检查完成')
  } catch (error) {
    ElMessage.error('邮件配置检查失败：' + (error.response?.data?.message || error.message))
    emailStatus.value = {
      success: false,
      message: '邮件配置检查失败'
    }
  } finally {
    statusLoading.value = false
  }
}

// 发送测试邮件
const sendTestEmail = async () => {
  if (!testFormRef.value) return
  
  const valid = await testFormRef.value.validate()
  if (!valid) return
  
  sending.value = true
  testResult.value = null
  
  try {
    const response = await api.post('/api/email/test', testForm)
    testResult.value = response.data
    ElMessage.success('测试邮件发送成功')
    
    // 清空表单
    testForm.toEmail = ''
    testForm.subject = '邮件发送功能测试'
    testForm.message = '这是一封测试邮件，用于验证电动滑板车租赁服务的邮件发送功能是否正常工作。'
    testFormRef.value.resetFields()
  } catch (error) {
    testResult.value = {
      success: false,
      message: error.response?.data?.message || '邮件发送失败'
    }
    ElMessage.error('测试邮件发送失败：' + (error.response?.data?.message || error.message))
  } finally {
    sending.value = false
  }
}

// 发送注册成功邮件
const sendRegistrationEmail = async () => {
  if (!registrationFormRef.value) return
  
  const valid = await registrationFormRef.value.validate()
  if (!valid) return
  
  sendingRegistration.value = true
  
  try {
    const response = await api.post('/api/email/test-registration', registrationForm)
    ElMessage.success('注册成功邮件发送成功')
    
    // 清空表单
    registrationForm.toEmail = ''
    registrationForm.username = ''
    registrationForm.fullName = ''
    registrationForm.phone = ''
    registrationFormRef.value.resetFields()
  } catch (error) {
    ElMessage.error('注册成功邮件发送失败：' + (error.response?.data?.message || error.message))
  } finally {
    sendingRegistration.value = false
  }
}

// 返回个人中心
const goBack = () => {
  router.push('/profile')
}
</script>

<style scoped>
.email-test-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.email-test-card {
  width: 100%;
  max-width: 800px;
}

.card-header {
  text-align: center;
  color: #333;
}

.card-header h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
}

.card-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.status-section,
.test-section,
.registration-test-section {
  margin-bottom: 30px;
}

.status-section h3,
.test-section h3,
.registration-test-section h3 {
  color: #333;
  border-left: 4px solid #409EFF;
  padding-left: 10px;
  margin-bottom: 20px;
}

.status-info {
  margin-top: 15px;
}

.result-section {
  margin-top: 20px;
}

.action-section {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

:deep(.el-card__header) {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-form-item__label) {
  font-weight: 600;
}
</style>