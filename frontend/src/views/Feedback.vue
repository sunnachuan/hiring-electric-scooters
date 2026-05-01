<template>
  <div class="feedback-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>意见反馈</h2>
          <p class="subtitle">我们重视您的每一个反馈，将尽快为您处理</p>
        </div>
      </template>
      
      <!-- 常见问题快捷提交 -->
      <div class="quick-feedback">
        <h3>常见问题快速反馈</h3>
        <p class="quick-tip">点击下方问题快速提交反馈</p>
        
        <div class="quick-buttons">
          <el-button 
            v-for="(question, index) in quickQuestions" 
            :key="index"
            class="quick-btn"
            @click="fillQuickQuestion(question)"
          >
            {{ question.title }}
          </el-button>
        </div>
      </div>
      
      <el-divider />
      
      <!-- 自定义反馈表单 -->
      <h3>自定义反馈</h3>
      <el-form :model="feedbackForm" :rules="rules" ref="feedbackFormRef">
        <el-form-item prop="title">
          <el-input
            v-model="feedbackForm.title"
            placeholder="反馈标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item prop="description">
          <el-input
            v-model="feedbackForm.description"
            type="textarea"
            :rows="4"
            placeholder="详细描述"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            提交反馈
          </el-button>
        </el-form-item>
      </el-form>
      
      <el-divider />
      
      <h3>我的反馈记录</h3>
      <el-table 
        :data="myFeedback" 
        v-loading="loading"
        empty-text="暂无反馈记录"
        :height="300"
      >
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'RESOLVED' ? 'success' : 'warning'">
              {{ row.status === 'RESOLVED' ? '已解决' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const feedbackFormRef = ref()
const loading = ref(false)
const submitting = ref(false)
const myFeedback = ref([])

const feedbackForm = ref({
  title: '',
  description: ''
})

// 常见问题模板（6个问题）
const quickQuestions = ref([
  {
    title: '设备无法启动',
    description: '我尝试启动滑板车，但无法正常启动。请检查设备状态并尽快处理。'
  },
  {
    title: '电池续航问题',
    description: '滑板车电池续航时间比预期短很多，希望能检查电池健康状况。'
  },
  {
    title: '预订系统问题',
    description: '在预订过程中遇到系统错误，无法完成预订流程。'
  },
  {
    title: '支付失败问题',
    description: '支付过程中出现错误，但账户已被扣款。请核实支付状态。'
  },
  {
    title: '设备损坏问题',
    description: '取车时发现滑板车有损坏，希望得到维修或更换。'
  },
  {
    title: '位置信息问题',
    description: '地图上显示的滑板车位置与实际位置不符，影响使用体验。'
  }
])

const rules = {
  title: [
    { required: true, message: '请输入反馈标题', trigger: 'blur' },
    { min: 5, message: '标题至少5个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入反馈描述', trigger: 'blur' },
    { min: 10, message: '描述至少10个字符', trigger: 'blur' }
  ]
}

const loadMyFeedback = async () => {
  loading.value = true
  try {
    const response = await api.get('/feedback/user')
    myFeedback.value = response.data
  } catch (error) {
    ElMessage.error('加载反馈记录失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

// 填充快捷问题
const fillQuickQuestion = async (question) => {
  try {
    await ElMessageBox.confirm(
      `是否确认提交"${question.title}"类型的反馈？\n\n系统将自动为您填写问题描述，您也可以修改后再提交。`,
      '确认提交反馈',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '取消',
        type: 'info',
        center: true
      }
    )
    
    // 用户确认提交
    feedbackForm.value.title = question.title
    feedbackForm.value.description = question.description
    
    // 自动提交
    await handleSubmit()
    
  } catch (cancel) {
    // 用户取消操作
    ElMessage.info('已取消操作')
  }
}

const handleSubmit = async () => {
  if (!feedbackFormRef.value) return
  
  const valid = await feedbackFormRef.value.validate()
  if (!valid) return
  
  // 额外的表单验证
  if (feedbackForm.value.title.trim().length < 5) {
    ElMessage.error('标题至少需要5个字符')
    return
  }
  
  if (feedbackForm.value.description.trim().length < 10) {
    ElMessage.error('描述至少需要10个字符')
    return
  }
  
  submitting.value = true
  
  try {
    await api.post('/feedback', {
      title: feedbackForm.value.title.trim(),
      description: feedbackForm.value.description.trim()
    })
    
    ElMessage.success({
      message: '反馈提交成功！我们会尽快处理您的反馈',
      duration: 3000,
      showClose: true
    })
    
    // 重置表单
    feedbackForm.value = { title: '', description: '' }
    
    // 重新加载反馈记录
    await loadMyFeedback()
    
  } catch (error) {
    ElMessage.error({
      message: '提交失败，请检查网络连接后重试',
      duration: 3000,
      showClose: true
    })
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadMyFeedback()
})
</script>

<style scoped>
/* 基础容器样式 */
.feedback-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 响应式容器 */
@media (max-width: 768px) {
  .feedback-container {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .feedback-container {
    padding: 12px;
  }
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.subtitle {
  color: #666;
  margin-top: 8px;
  font-size: 14px;
}

/* 响应式标题 */
@media (max-width: 768px) {
  .card-header h2 {
    font-size: 20px;
  }
  
  .subtitle {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .card-header h2 {
    font-size: 18px;
  }
  
  .subtitle {
    font-size: 12px;
  }
}

/* 快捷反馈区域 */
.quick-feedback {
  margin-bottom: 24px;
}

.quick-tip {
  color: #666;
  margin-bottom: 16px;
  font-size: 14px;
}

/* 响应式快捷反馈 */
@media (max-width: 768px) {
  .quick-feedback {
    margin-bottom: 20px;
  }
  
  .quick-tip {
    font-size: 13px;
    margin-bottom: 12px;
  }
}

@media (max-width: 480px) {
  .quick-feedback {
    margin-bottom: 16px;
  }
  
  .quick-tip {
    font-size: 12px;
    margin-bottom: 10px;
  }
}

/* 响应式网格布局 */
.quick-buttons {
  display: grid;
  grid-template-columns: repeat(6, 1fr); /* 电脑端：一行6个按钮，占满容器宽度 */
  gap: 12px;
  align-items: start; /* 顶部对齐，确保所有按钮从顶部开始 */
  justify-items: stretch; /* 水平拉伸填充 */
  width: 100%; /* 确保容器宽度为100% */
}

.quick-btn {
  height: auto; /* 改为自动高度 */
  min-height: 60px; /* 设置最小高度 */
  white-space: normal;
  word-break: break-word;
  text-align: center;
  padding: 12px 8px; /* 增加垂直内边距 */
  font-size: 13px;
  border-radius: 8px;
  background: var(--color-bg-secondary) !important;
  border: 1px solid var(--color-border) !important;
  color: var(--color-text-primary) !important;
  transition: all 0.3s ease;
  transition: all 0.3s ease;
  display: flex;
  align-items: center; /* 垂直居中 */
  justify-content: center; /* 水平居中 */
  flex-direction: column;
  margin: 0; /* 确保没有外边距 */
}

.quick-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background: var(--color-bg-tertiary) !important;
  border-color: var(--color-primary) !important;
  color: var(--color-primary) !important;
}

/* 平板端：保持单列布局 */
@media (max-width: 1024px) {
  .quick-buttons {
    gap: 10px;
  }
  
  .quick-btn {
    min-height: 55px;
    font-size: 12px;
    padding: 10px 6px;
  }
}

/* 手机端：改为2列布局，调整间距 */
@media (max-width: 768px) {
  .quick-buttons {
    grid-template-columns: repeat(2, 1fr); /* 手机端：一行两个按钮 */
    gap: 8px;
    justify-items: stretch; /* 手机端：水平拉伸填充 */
    width: 100%; /* 确保容器宽度为100% */
  }
  
  .quick-btn {
    min-height: 50px;
    font-size: 11px;
    padding: 8px 6px;
    margin: 0; /* 确保没有外边距 */
  }
}

/* 超小屏幕：保持2列布局 */
@media (max-width: 480px) {
  .quick-buttons {
    grid-template-columns: repeat(2, 1fr); /* 超小屏幕也保持2列 */
    gap: 6px;
    width: 100%; /* 确保容器宽度为100% */
  }
  
  .quick-btn {
    min-height: 45px;
    font-size: 10px;
    padding: 6px 4px;
    margin: 0; /* 确保没有外边距 */
  }
}

/* 表单响应式适配 */
:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input) {
  border-radius: 8px;
}

[data-theme="dark"] :deep(.el-textarea .el-textarea__inner) {
  background: var(--color-bg-secondary) !important;
  border-color: var(--color-border) !important;
  color: var(--color-text-primary) !important;
  border-radius: 8px;
}

[data-theme="dark"] :deep(.el-textarea .el-textarea__inner:focus) {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2) !important;
}

/* 响应式表单 */
@media (max-width: 768px) {
  :deep(.el-form-item__label) {
    font-size: 14px;
  }
  
  :deep(.el-input) {
    font-size: 14px;
  }
  
  :deep(.el-textarea) {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  :deep(.el-form-item__label) {
    font-size: 13px;
  }
  
  :deep(.el-input) {
    font-size: 13px;
  }
  
  :deep(.el-textarea) {
    font-size: 13px;
  }
}

/* 表格响应式适配 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
}

/* 响应式表格 */
@media (max-width: 1024px) {
  :deep(.el-table) {
    font-size: 13px;
  }
  
  :deep(.el-table th),
  :deep(.el-table td) {
    padding: 8px 4px;
  }
}

@media (max-width: 768px) {
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-table th),
  :deep(.el-table td) {
    padding: 6px 3px;
  }
}

@media (max-width: 480px) {
  :deep(.el-table) {
    font-size: 11px;
  }
  
  :deep(.el-table th),
  :deep(.el-table td) {
    padding: 4px 2px;
  }
  
  /* 隐藏部分列在小屏幕上 */
  :deep(.el-table-column--id) {
    display: none;
  }
  
  :deep(.el-table-column--createdAt) {
    width: 80px !important;
  }
}

/* 按钮响应式适配 */
:deep(.el-button) {
  border-radius: 6px;
  font-weight: 500;
}

@media (max-width: 768px) {
  :deep(.el-button) {
    font-size: 13px;
    padding: 8px 16px;
  }
}

@media (max-width: 480px) {
  :deep(.el-button) {
    font-size: 12px;
    padding: 6px 12px;
  }
}

/* 超小屏幕：单列布局 */
@media (max-width: 375px) {
  .quick-buttons {
    grid-template-columns: 1fr;
    gap: 4px;
  }
  
  .quick-btn {
    min-height: 40px;
    font-size: 10px;
    padding: 6px 4px;
  }
  
  :deep(.el-button) {
    font-size: 11px;
    padding: 4px 8px;
  }
}
</style>