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
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="togglePriority(row)"
            >
              {{ row.priority === 'HIGH' ? '设为低优先级' : '设为高优先级' }}
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
    ElMessage.success('优先级更新成功')
    loadFeedback()
  } catch (error) {
    ElMessage.error('更新失败')
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
</style>