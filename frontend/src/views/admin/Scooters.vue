<template>
  <div class="scooters-admin-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>滑板车管理</h2>
          <el-button type="primary" @click="showCreateDialog = true">
            新增滑板车
          </el-button>
        </div>
      </template>
      
      <el-table :data="scooters" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="model" label="型号" />
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <el-image 
              v-if="row.imageUrl" 
              :src="row.imageUrl" 
              :preview-src-list="[row.imageUrl]"
              fit="cover" 
              style="width: 60px; height: 40px;"
            >
              <template #error>
                <div class="image-slot">无图片</div>
              </template>
            </el-image>
            <span v-else>无图片</span>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="120">
          <template #default="{ row }">
            <span>{{ row.availableQuantity }}/{{ row.totalQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="hourlyRate" label="小时价(元)" width="100">
          <template #default="{ row }">
            {{ row.hourlyRate.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="dailyRate" label="日价(元)" width="100">
          <template #default="{ row }">
            {{ row.dailyRate.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'AVAILABLE' ? 'success' : 'danger'">
              {{ row.status === 'AVAILABLE' ? '可用' : '不可用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 新增滑板车对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      title="新增滑板车" 
      width="500px"
    >
      <el-form :model="createForm" :rules="rules" ref="createFormRef">
        <el-form-item label="型号" prop="model">
          <el-input v-model="createForm.model" placeholder="输入滑板车型号" />
        </el-form-item>
        <el-form-item label="图片URL" prop="imageUrl">
          <el-input v-model="createForm.imageUrl" placeholder="输入图片URL（可选）" />
        </el-form-item>
        <el-form-item label="总数量" prop="totalQuantity">
          <el-input-number 
            v-model="createForm.totalQuantity" 
            :min="1" 
            :step="1" 
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="小时价" prop="hourlyRate">
          <el-input-number 
            v-model="createForm.hourlyRate" 
            :min="1" 
            :step="0.5" 
            :precision="2"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="日价" prop="dailyRate">
          <el-input-number 
            v-model="createForm.dailyRate" 
            :min="1" 
            :step="1" 
            :precision="2"
            controls-position="right"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">
          确认新增
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 编辑滑板车对话框 -->
    <el-dialog 
      v-model="showEditDialog" 
      title="编辑滑板车" 
      width="500px"
    >
      <el-form :model="editForm" :rules="rules" ref="editFormRef">
        <el-form-item label="型号" prop="model">
          <el-input v-model="editForm.model" />
        </el-form-item>
        <el-form-item label="图片URL" prop="imageUrl">
          <el-input v-model="editForm.imageUrl" placeholder="输入图片URL（可选）" />
        </el-form-item>
        <el-form-item label="总数量" prop="totalQuantity">
          <el-input-number 
            v-model="editForm.totalQuantity" 
            :min="1" 
            :step="1" 
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="小时价" prop="hourlyRate">
          <el-input-number 
            v-model="editForm.hourlyRate" 
            :min="1" 
            :step="0.5" 
            :precision="2"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="日价" prop="dailyRate">
          <el-input-number 
            v-model="editForm.dailyRate" 
            :min="1" 
            :step="1" 
            :precision="2"
            controls-position="right"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleEdit" :loading="editing">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const scooters = ref([])
const loading = ref(false)
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const creating = ref(false)
const editing = ref(false)
const createFormRef = ref()
const editFormRef = ref()
const currentScooter = ref(null)

const createForm = ref({
  model: '',
  imageUrl: '',
  totalQuantity: 1,
  hourlyRate: 5,
  dailyRate: 25
})

const editForm = ref({
  model: '',
  imageUrl: '',
  totalQuantity: 1,
  hourlyRate: 5,
  dailyRate: 25
})

const rules = {
  model: [{ required: true, message: '请输入型号', trigger: 'blur' }],
  totalQuantity: [{ required: true, message: '请输入总数量', trigger: 'blur' }],
  hourlyRate: [{ required: true, message: '请输入小时价', trigger: 'blur' }],
  dailyRate: [{ required: true, message: '请输入日价', trigger: 'blur' }]
}

const loadScooters = async () => {
  loading.value = true
  try {
    const response = await api.get('/scooters')
    scooters.value = response.data
  } catch (error) {
    ElMessage.error('加载滑板车失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

const handleCreate = async () => {
  if (!createFormRef.value) return
  
  const valid = await createFormRef.value.validate()
  if (!valid) return
  
  creating.value = true
  
  try {
    await api.post('/scooters', null, {
      params: createForm.value
    })
    ElMessage.success('新增成功')
    showCreateDialog.value = false
    createForm.value = { model: '', hourlyRate: 5, dailyRate: 25 }
    loadScooters()
  } catch (error) {
    ElMessage.error('新增失败')
  } finally {
    creating.value = false
  }
}

const openEditDialog = (scooter) => {
  currentScooter.value = scooter
  editForm.value = {
    model: scooter.model,
    imageUrl: scooter.imageUrl || '',
    totalQuantity: scooter.totalQuantity || 1,
    hourlyRate: parseFloat(scooter.hourlyRate),
    dailyRate: parseFloat(scooter.dailyRate)
  }
  showEditDialog.value = true
}

const handleEdit = async () => {
  if (!editFormRef.value) return
  
  const valid = await editFormRef.value.validate()
  if (!valid) return
  
  editing.value = true
  
  try {
    await api.put(`/scooters/${currentScooter.value.id}`, null, {
      params: editForm.value
    })
    ElMessage.success('修改成功')
    showEditDialog.value = false
    loadScooters()
  } catch (error) {
    ElMessage.error('修改失败')
  } finally {
    editing.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除此滑板车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 注意：实际项目中应该有删除接口，这里暂时注释
    // await api.delete(`/scooters/${id}`)
    ElMessage.warning('删除功能暂未实现')
    // loadScooters()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadScooters()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>