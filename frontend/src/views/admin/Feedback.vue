<template>
  <div class="feedback-admin-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>反馈管理</h2>
          <div>
            <el-select v-model="filterPriority" placeholder="按优先级筛选" style="width: 120px; margin-right: 10px;">
              <el-option label="全部" value="" />
              <el-option label="高优先级" value="HIGH" />
              <el-option label="低优先级" value="LOW" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="按状态筛选" style="width: 120px;">
              <el-option label="全部" value="" />
              <el-option label="待处理" value="OPEN" />
              <el-option label="已解决" value="RESOLVED" />
            </el-select>
          </div>
        </div>
      </template>
      
      <el-table :data="filteredFeedback" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="user.username" label="用户" width="120" />
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
        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="togglePriority(row)"
            >
              {{ row.priority === 'HIGH' ? '设为低优先级' : '设为高优先级' }}
            </el-button>
            <el-button 
              v-if="row.status === 'OPEN'"
              type="success" 
              size="small" 
              @click="markAsResolved(row)"
            >
              标记为已处理
            </el-button>
            <el-button 
              v-else
              type="warning" 
              size="small" 
              @click="reopenFeedback(row)"
            >
              重新打开
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/api'
import { ElMessage } from 'element-plus'

const feedback = ref([])
const loading = ref(false)
const filterPriority = ref('')
const filterStatus = ref('')

const filteredFeedback = computed(() => {
  let result = feedback.value
  
  if (filterPriority.value) {
    result = result.filter(f => f.priority === filterPriority.value)
  }
  
  if (filterStatus.value) {
    result = result.filter(f => f.status === filterStatus.value)
  }
  
  return result
})

const loadFeedback = async () => {
  loading.value = true
  try {
    const response = await api.get('/admin/feedback')
    feedback.value = response.data
  } catch (error) {
    ElMessage.error('加载反馈失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

const togglePriority = async (feedbackItem) => {
  try {
    const newPriority = feedbackItem.priority === 'HIGH' ? 'LOW' : 'HIGH'
    await api.put(`/admin/feedback/${feedbackItem.id}/priority?priority=${newPriority}`)
    feedbackItem.priority = newPriority
    ElMessage.success('优先级更新成功')
  } catch (error) {
    ElMessage.error('更新优先级失败')
  }
}

const markAsResolved = async (feedbackItem) => {
  try {
    await api.put(`/admin/feedback/${feedbackItem.id}/status?status=RESOLVED`)
    feedbackItem.status = 'RESOLVED'
    ElMessage.success('反馈已标记为已处理')
  } catch (error) {
    ElMessage.error('标记为已处理失败')
  }
}

const reopenFeedback = async (feedbackItem) => {
  try {
    await api.put(`/admin/feedback/${feedbackItem.id}/status?status=OPEN`)
    feedbackItem.status = 'OPEN'
    ElMessage.success('反馈已重新打开')
  } catch (error) {
    ElMessage.error('重新打开反馈失败')
  }
}

onMounted(() => {
  loadFeedback()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 深色主题下的反馈管理页面样式优化 - 提高优先级 */
.feedback-admin-container[data-theme="dark"] .el-card {
  background: var(--color-bg-secondary) !important;
  border-color: var(--color-border) !important;
}

.feedback-admin-container[data-theme="dark"] .el-card__header {
  background: var(--color-bg-tertiary) !important;
  border-bottom-color: var(--color-border) !important;
  color: var(--color-text-primary) !important;
}

.feedback-admin-container[data-theme="dark"] .card-header h2 {
  color: var(--color-text-primary) !important;
}

/* 深色主题下的筛选器样式 */
.feedback-admin-container[data-theme="dark"] .card-header .el-select .el-input__wrapper {
  background: var(--color-bg-secondary) !important;
  border-color: var(--color-primary) !important;
  color: var(--color-text-primary) !important;
}

.feedback-admin-container[data-theme="dark"] .card-header .el-select .el-input__wrapper:hover {
  background: var(--color-bg-tertiary) !important;
  border-color: var(--color-primary-light) !important;
}

.feedback-admin-container[data-theme="dark"] .card-header .el-select .el-input__inner {
  color: var(--color-text-primary) !important;
}

.feedback-admin-container[data-theme="dark"] .card-header .el-select-dropdown {
  background: var(--color-bg-secondary) !important;
  border-color: var(--color-border) !important;
}

.feedback-admin-container[data-theme="dark"] .card-header .el-select-dropdown__item {
  color: var(--color-text-primary) !important;
  background: var(--color-bg-secondary) !important;
}

.feedback-admin-container[data-theme="dark"] .card-header .el-select-dropdown__item:hover {
  background: var(--color-bg-tertiary) !important;
  color: var(--color-primary) !important;
}

.feedback-admin-container[data-theme="dark"] .card-header .el-select-dropdown__item.selected {
  background: rgba(99, 102, 241, 0.1) !important;
  color: var(--color-primary) !important;
}

/* 深色主题下的表格样式 */
.feedback-admin-container[data-theme="dark"] .el-table {
  background: var(--color-bg-secondary) !important;
  color: var(--color-text-primary) !important;
}

.feedback-admin-container[data-theme="dark"] .el-table th {
  background: var(--color-bg-tertiary) !important;
  color: var(--color-text-primary) !important;
  border-bottom-color: var(--color-border) !important;
}

.feedback-admin-container[data-theme="dark"] .el-table td {
  background: var(--color-bg-secondary) !important;
  color: var(--color-text-primary) !important;
  border-bottom-color: var(--color-border) !important;
}

.feedback-admin-container[data-theme="dark"] .el-table tr:hover td {
  background: var(--color-bg-tertiary) !important;
}

/* 深色主题下的按钮样式 */
.feedback-admin-container[data-theme="dark"] .el-button {
  background: var(--color-bg-secondary) !important;
  border-color: var(--color-border) !important;
  color: var(--color-text-primary) !important;
}

.feedback-admin-container[data-theme="dark"] .el-button:hover {
  background: var(--color-bg-tertiary) !important;
  border-color: var(--color-primary) !important;
  color: var(--color-primary) !important;
}

.feedback-admin-container[data-theme="dark"] .el-button--primary {
  background: var(--color-primary) !important;
  border-color: var(--color-primary) !important;
  color: var(--color-text-primary) !important;
}

.feedback-admin-container[data-theme="dark"] .el-button--primary:hover {
  background: var(--color-primary-light) !important;
  border-color: var(--color-primary-light) !important;
}

.feedback-admin-container[data-theme="dark"] .el-button--success {
  background: var(--color-success) !important;
  border-color: var(--color-success) !important;
  color: var(--color-text-primary) !important;
}

.feedback-admin-container[data-theme="dark"] .el-button--success:hover {
  background: rgba(34, 197, 94, 0.8) !important;
  border-color: rgba(34, 197, 94, 0.8) !important;
}

.feedback-admin-container[data-theme="dark"] .el-button--warning {
  background: var(--color-warning) !important;
  border-color: var(--color-warning) !important;
  color: var(--color-text-primary) !important;
}

.feedback-admin-container[data-theme="dark"] .el-button--warning:hover {
  background: rgba(245, 158, 11, 0.8) !important;
  border-color: rgba(245, 158, 11, 0.8) !important;
}
</style>