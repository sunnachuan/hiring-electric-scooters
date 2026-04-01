<template>
  <div class="feedback-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>意见反馈</h2>
        </div>
      </template>
      
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
      <el-table :data="myFeedback" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="row.priority === 'HIGH' ? 'danger' : 'info'">
              {{ row.priority === 'HIGH' ? '高' : '低' }}
            </el-tag>
          </template>
        </el-table-column>
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
import { ElMessage } from 'element-plus'

const feedbackFormRef = ref()
const loading = ref(false)
const submitting = ref(false)
const myFeedback = ref([])

const feedbackForm = ref({
  title: '',
  description: ''
})

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

const handleSubmit = async () => {
  if (!feedbackFormRef.value) return
  
  const valid = await feedbackFormRef.value.validate()
  if (!valid) return
  
  submitting.value = true
  
  try {
    await api.post('/feedback', feedbackForm.value)
    ElMessage.success('反馈提交成功')
    feedbackForm.value = { title: '', description: '' }
    loadMyFeedback()
  } catch (error) {
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadMyFeedback()
})
</script>

<style scoped>
.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
}
</style>