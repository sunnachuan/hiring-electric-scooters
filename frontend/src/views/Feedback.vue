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
import { ElMessage } from 'element-plus'

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
const fillQuickQuestion = (question) => {
  feedbackForm.value.title = question.title
  feedbackForm.value.description = question.description
  ElMessage.success('问题已填充，请检查并提交')
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

.subtitle {
  color: #666;
  margin-top: 8px;
  font-size: 14px;
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

/* 电脑版：按钮左右到两侧 */
.quick-buttons {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  flex-wrap: nowrap;
}

.quick-btn {
  flex: 1;
  min-width: 0;
  height: 60px;
  white-space: normal;
  word-break: break-word;
  text-align: center;
  padding: 8px;
  font-size: 13px;
}

/* 平板端：每行3个按钮 */
@media (max-width: 1024px) {
  .quick-buttons {
    flex-wrap: wrap;
    justify-content: flex-start;
  }
  
  .quick-btn {
    flex: 0 0 calc(33.333% - 6px);
    height: 55px;
    font-size: 12px;
  }
}

/* 手机端：每行2个按钮 */
@media (max-width: 768px) {
  .quick-buttons {
    flex-wrap: wrap;
    justify-content: flex-start;
  }
  
  .quick-btn {
    flex: 0 0 calc(50% - 4px);
    height: 50px;
    font-size: 12px;
  }
}

/* 小屏幕手机：每行2个按钮 */
@media (max-width: 480px) {
  .quick-buttons {
    gap: 6px;
  }
  
  .quick-btn {
    flex: 0 0 calc(50% - 3px);
    height: 45px;
    font-size: 11px;
    padding: 6px;
  }
}

/* 超小屏幕：每行2个按钮 */
@media (max-width: 375px) {
  .quick-buttons {
    gap: 4px;
  }
  
  .quick-btn {
    flex: 0 0 calc(50% - 2px);
    height: 40px;
    font-size: 10px;
    padding: 4px;
  }
}
</style>