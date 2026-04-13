<template>
  <div class="operations">
    <el-card class="operations-header">
      <template #header>
        <div class="card-header">
          <h2>运维管理系统</h2>
          <span class="subtitle">管理充电、部署、收集和维修任务</span>
        </div>
      </template>
      
      <!-- 快速操作面板 -->
      <div class="quick-actions">
        <el-button type="primary" @click="showCreateTaskDialog = true">
          <el-icon><Plus /></el-icon>
          创建新任务
        </el-button>
        <el-button type="success" @click="autoCreateChargingTasks">
          <el-icon><CircleCheck /></el-icon>
          自动创建充电任务
        </el-button>
        <el-button type="info" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
      
      <!-- 统计卡片 -->
      <div class="stats-grid">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingCount }}</div>
              <div class="stat-label">待处理任务</div>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon in-progress">
              <el-icon><Loading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.inProgressCount }}</div>
              <div class="stat-label">进行中任务</div>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon completed">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.completedCount }}</div>
              <div class="stat-label">已完成任务</div>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon operators">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.operatorCount }}</div>
              <div class="stat-label">活跃运维人员</div>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>

    <!-- 任务列表 -->
    <el-card class="tasks-section">
      <template #header>
        <div class="section-header">
          <h3>任务列表</h3>
          <div class="filter-controls">
            <el-select v-model="taskFilter.status" placeholder="状态筛选" size="small">
              <el-option label="全部" value=""></el-option>
              <el-option label="待处理" value="PENDING"></el-option>
              <el-option label="已分配" value="ASSIGNED"></el-option>
              <el-option label="进行中" value="IN_PROGRESS"></el-option>
              <el-option label="已完成" value="COMPLETED"></el-option>
            </el-select>
            <el-select v-model="taskFilter.type" placeholder="类型筛选" size="small">
              <el-option label="全部" value=""></el-option>
              <el-option label="充电" value="CHARGING"></el-option>
              <el-option label="部署" value="DEPLOYMENT"></el-option>
              <el-option label="收集" value="COLLECTION"></el-option>
              <el-option label="维修" value="MAINTENANCE"></el-option>
            </el-select>
            <el-select v-model="taskFilter.priority" placeholder="优先级筛选" size="small">
              <el-option label="全部" value=""></el-option>
              <el-option label="紧急" value="URGENT"></el-option>
              <el-option label="高" value="HIGH"></el-option>
              <el-option label="正常" value="NORMAL"></el-option>
              <el-option label="低" value="LOW"></el-option>
            </el-select>
          </div>
        </div>
      </template>
      
      <el-table :data="filteredTasks" v-loading="loading">
        <el-table-column label="任务ID" prop="id" width="80"></el-table-column>
        <el-table-column label="任务类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTaskTypeTagType(row.taskType)" size="small">
              {{ getTaskTypeText(row.taskType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="getPriorityTagType(row.priority)" size="small">
              {{ row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="设备信息">
          <template #default="{ row }">
            <div v-if="row.scooter">
              <div>#{{ row.scooter.id }} - {{ row.scooter.model }}</div>
              <div class="scooter-detail">
                <span v-if="row.scooter.batteryLevel !== undefined">
                  电量: {{ row.scooter.batteryLevel }}%
                </span>
                <span v-if="row.scooter.locationName">
                  位置: {{ row.scooter.locationName }}
                </span>
              </div>
            </div>
            <span v-else class="no-scooter">无关联设备</span>
          </template>
        </el-table-column>
        <el-table-column label="描述" prop="description"></el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分配人员" width="120">
          <template #default="{ row }">
            <span v-if="row.assignedOperator">{{ row.assignedOperator.name }}</span>
            <span v-else class="unassigned">未分配</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="150">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="viewTaskDetails(row)">详情</el-button>
            <el-button 
              size="small" 
              type="primary" 
              v-if="row.status === 'PENDING'"
              @click="assignTask(row)"
            >
              分配
            </el-button>
            <el-button 
              size="small" 
              type="success" 
              v-if="row.status === 'ASSIGNED'"
              @click="startTask(row)"
            >
              开始
            </el-button>
            <el-button 
              size="small" 
              type="warning" 
              v-if="row.status === 'IN_PROGRESS'"
              @click="completeTask(row)"
            >
              完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建任务弹窗 -->
    <el-dialog v-model="showCreateTaskDialog" title="创建新任务" width="600px">
      <el-form :model="newTaskForm" :rules="taskRules" ref="taskFormRef">
        <el-form-item label="任务类型" prop="taskType">
          <el-select v-model="newTaskForm.taskType" placeholder="选择任务类型" style="width: 100%">
            <el-option label="充电任务" value="CHARGING"></el-option>
            <el-option label="部署任务" value="DEPLOYMENT"></el-option>
            <el-option label="收集任务" value="COLLECTION"></el-option>
            <el-option label="维修任务" value="MAINTENANCE"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="选择设备" prop="scooterId">
          <el-select v-model="newTaskForm.scooterId" placeholder="选择设备" style="width: 100%">
            <el-option 
              v-for="scooter in availableScooters" 
              :key="scooter.id"
              :label="`#${scooter.id} - ${scooter.model} (电量: ${scooter.batteryLevel}%)`"
              :value="scooter.id"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="newTaskForm.priority" placeholder="选择优先级" style="width: 100%">
            <el-option label="紧急" value="URGENT"></el-option>
            <el-option label="高" value="HIGH"></el-option>
            <el-option label="正常" value="NORMAL"></el-option>
            <el-option label="低" value="LOW"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="任务描述" prop="description">
          <el-input 
            v-model="newTaskForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入任务描述"
          ></el-input>
        </el-form-item>
        
        <el-form-item v-if="newTaskForm.taskType === 'DEPLOYMENT'" label="目标位置">
          <el-input v-model="newTaskForm.targetLocation" placeholder="请输入目标位置"></el-input>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateTaskDialog = false">取消</el-button>
        <el-button type="primary" @click="createTask" :loading="creatingTask">创建</el-button>
      </template>
    </el-dialog>

    <!-- 任务详情弹窗 -->
    <el-dialog v-model="showTaskDetail" title="任务详情" width="700px">
      <div v-if="selectedTask" class="task-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务ID">{{ selectedTask.id }}</el-descriptions-item>
          <el-descriptions-item label="任务类型">
            <el-tag :type="getTaskTypeTagType(selectedTask.taskType)">
              {{ getTaskTypeText(selectedTask.taskType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityTagType(selectedTask.priority)">
              {{ selectedTask.priority }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(selectedTask.status)">
              {{ getStatusText(selectedTask.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(selectedTask.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatTime(selectedTask.startedAt) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ formatTime(selectedTask.completedAt) }}</el-descriptions-item>
          <el-descriptions-item label="分配人员" :span="2">
            {{ selectedTask.assignedOperator?.name || '未分配' }}
          </el-descriptions-item>
          <el-descriptions-item label="任务描述" :span="2">
            {{ selectedTask.description }}
          </el-descriptions-item>
          <el-descriptions-item label="完成说明" :span="2" v-if="selectedTask.completionNotes">
            {{ selectedTask.completionNotes }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div v-if="selectedTask.scooter" class="scooter-info">
          <h4>关联设备信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="设备ID">{{ selectedTask.scooter.id }}</el-descriptions-item>
            <el-descriptions-item label="型号">{{ selectedTask.scooter.model }}</el-descriptions-item>
            <el-descriptions-item label="电量">{{ selectedTask.scooter.batteryLevel }}%</el-descriptions-item>
            <el-descriptions-item label="位置">{{ selectedTask.scooter.locationName }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, Refresh, Clock, Loading, CircleCheck, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 响应式数据
const loading = ref(false)
const creatingTask = ref(false)
const tasks = ref([])
const operators = ref([])
const availableScooters = ref([])
const selectedTask = ref(null)
const showCreateTaskDialog = ref(false)
const showTaskDetail = ref(false)

const taskFilter = ref({
  status: '',
  type: '',
  priority: ''
})

const newTaskForm = ref({
  taskType: '',
  scooterId: null,
  priority: 'NORMAL',
  description: '',
  targetLocation: ''
})

const taskRules = {
  taskType: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  scooterId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  description: [{ required: true, message: '请输入任务描述', trigger: 'blur' }]
}

// 统计信息
const stats = computed(() => {
  const pendingCount = tasks.value.filter(t => t.status === 'PENDING').length
  const inProgressCount = tasks.value.filter(t => t.status === 'IN_PROGRESS').length
  const completedCount = tasks.value.filter(t => t.status === 'COMPLETED').length
  const operatorCount = operators.value.filter(o => o.status === 'ACTIVE').length
  
  return { pendingCount, inProgressCount, completedCount, operatorCount }
})

// 过滤后的任务列表
const filteredTasks = computed(() => {
  let filtered = tasks.value
  
  if (taskFilter.value.status) {
    filtered = filtered.filter(t => t.status === taskFilter.value.status)
  }
  
  if (taskFilter.value.type) {
    filtered = filtered.filter(t => t.taskType === taskFilter.value.type)
  }
  
  if (taskFilter.value.priority) {
    filtered = filtered.filter(t => t.priority === taskFilter.value.priority)
  }
  
  return filtered
})

// 工具函数
const getTaskTypeTagType = (type) => {
  const types = {
    'CHARGING': 'success',
    'DEPLOYMENT': 'primary',
    'COLLECTION': 'warning',
    'MAINTENANCE': 'danger'
  }
  return types[type] || 'info'
}

const getTaskTypeText = (type) => {
  const texts = {
    'CHARGING': '充电',
    'DEPLOYMENT': '部署',
    'COLLECTION': '收集',
    'MAINTENANCE': '维修'
  }
  return texts[type] || type
}

const getPriorityTagType = (priority) => {
  const types = {
    'URGENT': 'danger',
    'HIGH': 'warning',
    'NORMAL': 'info',
    'LOW': ''
  }
  return types[priority] || 'info'
}

const getStatusTagType = (status) => {
  const types = {
    'PENDING': 'info',
    'ASSIGNED': 'primary',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'PENDING': '待处理',
    'ASSIGNED': '已分配',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return texts[status] || status
}

const formatTime = (time) => {
  if (!time) return '未开始'
  return new Date(time).toLocaleString('zh-CN')
}

// 操作方法
const refreshData = async () => {
  loading.value = true
  try {
    await Promise.all([loadTasks(), loadOperators(), loadScooters()])
    ElMessage.success('数据刷新成功')
  } catch (error) {
    console.error('刷新数据失败:', error)
    ElMessage.error('刷新数据失败')
  } finally {
    loading.value = false
  }
}

const autoCreateChargingTasks = async () => {
  try {
    // 模拟API调用
    ElMessage.info('正在自动创建充电任务...')
    setTimeout(() => {
      ElMessage.success('充电任务创建完成')
      refreshData()
    }, 1000)
  } catch (error) {
    ElMessage.error('创建充电任务失败')
  }
}

const viewTaskDetails = (task) => {
  selectedTask.value = task
  showTaskDetail.value = true
}

const assignTask = async (task) => {
  // 模拟分配任务逻辑
  ElMessage.info(`正在分配任务 #${task.id}`)
}

const startTask = async (task) => {
  // 模拟开始任务逻辑
  ElMessage.info(`任务 #${task.id} 已开始`)
}

const completeTask = async (task) => {
  // 模拟完成任务逻辑
  ElMessage.success(`任务 #${task.id} 已完成`)
}

const createTask = async () => {
  creatingTask.value = true
  try {
    // 模拟创建任务
    ElMessage.success('任务创建成功')
    showCreateTaskDialog.value = false
    refreshData()
  } catch (error) {
    ElMessage.error('创建任务失败')
  } finally {
    creatingTask.value = false
  }
}

// 数据加载方法
const loadTasks = async () => {
  // 模拟数据加载
  tasks.value = generateMockTasks()
}

const loadOperators = async () => {
  // 模拟数据加载
  operators.value = generateMockOperators()
}

const loadScooters = async () => {
  // 模拟数据加载
  availableScooters.value = generateMockScooters()
}

// 模拟数据生成（实际使用时删除）
const generateMockTasks = () => {
  return Array.from({ length: 15 }, (_, i) => ({
    id: i + 1,
    taskType: ['CHARGING', 'DEPLOYMENT', 'COLLECTION', 'MAINTENANCE'][i % 4],
    priority: ['URGENT', 'HIGH', 'NORMAL', 'LOW'][i % 4],
    status: ['PENDING', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED'][i % 4],
    description: `任务描述 ${i + 1}`,
    scooter: {
      id: i + 1,
      model: `Scooter ${(i % 5) + 1}`,
      batteryLevel: Math.floor(Math.random() * 100),
      locationName: `位置 ${i + 1}`
    },
    assignedOperator: i > 5 ? { name: `运维人员 ${i % 3 + 1}` } : null,
    createdAt: new Date(Date.now() - Math.random() * 86400000).toISOString(),
    startedAt: i > 5 ? new Date(Date.now() - Math.random() * 43200000).toISOString() : null,
    completedAt: i > 10 ? new Date(Date.now() - Math.random() * 21600000).toISOString() : null
  }))
}

const generateMockOperators = () => {
  return Array.from({ length: 5 }, (_, i) => ({
    id: i + 1,
    name: `运维人员 ${i + 1}`,
    role: ['CHARGER', 'DEPLOYER', 'MAINTENANCE'][i % 3],
    status: 'ACTIVE',
    currentTaskCount: Math.floor(Math.random() * 3)
  }))
}

const generateMockScooters = () => {
  return Array.from({ length: 20 }, (_, i) => ({
    id: i + 1,
    model: `Scooter ${(i % 5) + 1}`,
    batteryLevel: Math.floor(Math.random() * 100),
    locationName: `位置 ${i + 1}`
  }))
}

// 生命周期
onMounted(() => {
  refreshData()
})
</script>

<style scoped>
.operations {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.operations-header {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.subtitle {
  color: #666;
  font-size: 14px;
}

.quick-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  border: none;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-icon.pending { background: #e6a23c; }
.stat-icon.in-progress { background: #409eff; }
.stat-icon.completed { background: #67c23a; }
.stat-icon.operators { background: #909399; }

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.tasks-section {
  margin-top: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.filter-controls {
  display: flex;
  gap: 12px;
}

.scooter-detail {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.scooter-detail span {
  margin-right: 12px;
}

.no-scooter, .unassigned {
  color: #c0c4cc;
  font-style: italic;
}

.task-detail {
  max-height: 500px;
  overflow-y: auto;
}

.scooter-info {
  margin-top: 20px;
}

.scooter-info h4 {
  margin-bottom: 12px;
  color: #303133;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .operations {
    padding: 12px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .filter-controls {
    flex-wrap: wrap;
  }
  
  .quick-actions {
    flex-direction: column;
  }
}
</style>