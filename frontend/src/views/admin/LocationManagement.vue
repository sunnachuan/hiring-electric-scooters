<template>
  <div class="location-management">
    <div class="page-header">
      <div>
        <h1 class="page-title">点位管理</h1>
        <p class="page-subtitle">管理滑板车停放点位信息</p>
      </div>
      <el-button type="primary" size="large" @click="addLocation">
        <el-icon><Plus /></el-icon>
        添加点位
      </el-button>
    </div>
    
    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索点位名称或地址"
        clearable
        style="width: 300px"
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <el-select v-model="filterStatus" placeholder="状态筛选" clearable @change="handleFilter">
        <el-option label="全部" value="" />
        <el-option label="启用" value="ACTIVE" />
        <el-option label="停用" value="INACTIVE" />
      </el-select>
    </div>
    
    <!-- 点位列表 -->
    <div class="locations-list">
      <el-table
        :data="filteredLocations"
        v-loading="loading"
        style="width: 100%"
        :default-sort="{ prop: 'id', order: 'ascending' }"
      >
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="name" label="点位名称" min-width="120" />
        <el-table-column prop="address" label="地址（经纬度）" min-width="200" show-overflow-tooltip />
        <el-table-column prop="capacity" label="容量" width="100" />
        <el-table-column prop="availableCount" label="可用数量" width="100">
          <template #default="{ row }">
            <span :class="{ 'text-success': row.availableCount > 0, 'text-danger': row.availableCount === 0 }">
              {{ row.availableCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="bookedCount" label="已预订" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" type="info" @click="viewLocation(row)">查看</el-button>
              <el-button size="small" type="primary" @click="editLocation(row)">编辑</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    

  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

export default {
  name: 'LocationManagement',
  setup() {
    const loading = ref(false)
    const searchKeyword = ref('')
    const filterStatus = ref('')
    
    const locations = ref([])
    
    // 筛选后的点位列表
    const filteredLocations = computed(() => {
      let result = locations.value
      
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        result = result.filter(location => 
          location.name.toLowerCase().includes(keyword) ||
          (location.address && location.address.toLowerCase().includes(keyword))
        )
      }
      
      if (filterStatus.value) {
        result = result.filter(location => location.status === filterStatus.value)
      }
      
      return result
    })
    
    // 加载点位数据
    const loadLocations = async () => {
      try {
        loading.value = true
        
        // 使用滑板车数据来计算点位信息，确保与地图页面使用同一数据源
        const scootersResponse = await api.get('/scooters')
        const scooters = scootersResponse.data
        
        // 根据滑板车数据生成点位信息（与地图页面相同的逻辑）
        const locationsMap = new Map()
        
        // 先处理有滑板车的点位
        scooters.forEach(scooter => {
          const locationId = scooter.locationId || 1
          const locationName = scooter.locationName || `点位${locationId}`
          const lat = scooter.latitude || 39.9042
          const lng = scooter.longitude || 116.4074
          const address = `${lat.toFixed(4)}, ${lng.toFixed(4)}`
          
          if (!locationsMap.has(locationId)) {
            locationsMap.set(locationId, {
              id: locationId,
              name: locationName,
              address: address,
              latitude: lat,
              longitude: lng,
              capacity: 50,
              availableCount: 0,
              bookedCount: 0,
              status: 'ACTIVE'
            })
          }
          
          const location = locationsMap.get(locationId)
          
          if (scooter.status === 'AVAILABLE') {
            location.availableCount++
          } else {
            location.bookedCount++
          }
        })
        
        let locationsData = Array.from(locationsMap.values())
        
        // 确保有10个点位，如果不足则补充（使用真实的点位名称）
        const existingLocationIds = new Set(locationsData.map(loc => loc.id))
        
        // 定义10个点位的默认名称和坐标（容量统一为50）
        const defaultLocations = [
          { id: 1, name: '市中心广场', lat: 39.9042, lng: 116.4074, capacity: 50 },
          { id: 2, name: '大学城校区', lat: 39.9896, lng: 116.3509, capacity: 50 },
          { id: 3, name: '商业步行街', lat: 39.9138, lng: 116.3631, capacity: 50 },
          { id: 4, name: '地铁站出口', lat: 39.9022, lng: 116.3912, capacity: 50 },
          { id: 5, name: '公园入口', lat: 39.9163, lng: 116.3972, capacity: 50 },
          { id: 6, name: '火车站北广场', lat: 39.9028, lng: 116.4278, capacity: 50 },
          { id: 7, name: '科技园区', lat: 40.0412, lng: 116.2981, capacity: 50 },
          { id: 8, name: '体育中心', lat: 39.9924, lng: 116.3912, capacity: 50 },
          { id: 9, name: '购物中心', lat: 39.9334, lng: 116.4526, capacity: 50 },
          { id: 10, name: '医院门口', lat: 39.9048, lng: 116.4076, capacity: 50 }
        ]
        
        for (let i = 1; i <= 10; i++) {
          if (!existingLocationIds.has(i)) {
            const defaultLoc = defaultLocations.find(loc => loc.id === i) || { id: i, name: `点位${i}`, lat: 39.9042, lng: 116.4074, capacity: 10 }
            const defaultLocation = {
              id: defaultLoc.id,
              name: defaultLoc.name,
              address: `${defaultLoc.lat.toFixed(4)}, ${defaultLoc.lng.toFixed(4)}`,
              latitude: defaultLoc.lat,
              longitude: defaultLoc.lng,
              capacity: defaultLoc.capacity,
              availableCount: 0,
              bookedCount: 0,
              status: 'ACTIVE'
            }
            locationsData.push(defaultLocation)
          }
        }
        
        // 按ID排序
        locationsData.sort((a, b) => a.id - b.id)
        
        locations.value = locationsData
        
      } catch (error) {
        console.error('加载点位数据失败:', error)
        // API调用失败时使用模拟数据
        locations.value = getMockLocations()
      } finally {
        loading.value = false
      }
    }
    
    // 模拟点位数据（备用）
    const getMockLocations = () => {
      return [
        { id: 1, name: '市中心广场', address: '北京市东城区王府井大街', latitude: 39.9042, longitude: 116.4074, capacity: 50, availableCount: 3, bookedCount: 2, status: 'ACTIVE' },
        { id: 2, name: '大学城校区', address: '北京市海淀区中关村大街', latitude: 39.9896, longitude: 116.3509, capacity: 50, availableCount: 2, bookedCount: 1, status: 'ACTIVE' },
        { id: 3, name: '商业步行街', address: '北京市西城区西单北大街', latitude: 39.9138, longitude: 116.3631, capacity: 50, availableCount: 2, bookedCount: 1, status: 'ACTIVE' },
        { id: 4, name: '地铁站出口', address: '北京市朝阳区国贸地铁站', latitude: 39.9022, longitude: 116.3912, capacity: 50, availableCount: 2, bookedCount: 1, status: 'ACTIVE' },
        { id: 5, name: '公园入口', address: '北京市海淀区颐和园东门', latitude: 39.9163, longitude: 116.3972, capacity: 50, availableCount: 2, bookedCount: 1, status: 'ACTIVE' },
        { id: 6, name: '火车站北广场', address: '北京市西城区北京站', latitude: 39.9028, longitude: 116.4278, capacity: 50, availableCount: 2, bookedCount: 1, status: 'ACTIVE' },
        { id: 7, name: '科技园区', address: '北京市海淀区上地信息产业基地', latitude: 40.0412, longitude: 116.2981, capacity: 50, availableCount: 2, bookedCount: 1, status: 'ACTIVE' },
        { id: 8, name: '体育中心', address: '北京市朝阳区奥林匹克公园', latitude: 39.9924, longitude: 116.3912, capacity: 50, availableCount: 2, bookedCount: 1, status: 'ACTIVE' },
        { id: 9, name: '购物中心', address: '北京市朝阳区三里屯', latitude: 39.9334, longitude: 116.4526, capacity: 50, availableCount: 2, bookedCount: 1, status: 'ACTIVE' },
        { id: 10, name: '医院门口', address: '北京市西城区协和医院', latitude: 39.9048, longitude: 116.4076, capacity: 50, availableCount: 2, bookedCount: 1, status: 'ACTIVE' }
      ]
    }
    
    // 查看点位详情（只读模式）
    const viewLocation = (location) => {
      ElMessage.info(`查看点位: ${location.name}`)
    }
    
    // 编辑点位
    const editLocation = (location) => {
      ElMessageBox.confirm(
        `确定要编辑点位 "${location.name}" 吗？\n\n注意：当前为前端演示模式，修改不会保存到数据库。`,
        '编辑点位',
        {
          confirmButtonText: '继续编辑',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        // 打开编辑表单
        showEditDialog(location)
      }).catch(() => {
        // 用户取消操作
      })
    }
    
    // 显示编辑对话框
    const showEditDialog = (location) => {
      ElMessageBox.prompt('请输入新的点位名称', '编辑点位名称', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: location.name
      }).then(({ value }) => {
        if (value && value.trim()) {
          // 更新本地数据
          const index = locations.value.findIndex(loc => loc.id === location.id)
          if (index !== -1) {
            locations.value[index].name = value.trim()
            ElMessage.success('点位名称已更新（前端演示模式）')
          }
        }
      }).catch(() => {
        // 用户取消操作
      })
    }
    
    // 添加点位
    const addLocation = () => {
      ElMessageBox.confirm(
        '添加新点位？\n\n注意：当前为前端演示模式，新增点位不会保存到数据库。',
        '添加点位',
        {
          confirmButtonText: '继续添加',
          cancelButtonText: '取消',
          type: 'info'
        }
      ).then(() => {
        // 生成新点位的ID
        const newId = Math.max(...locations.value.map(loc => loc.id), 0) + 1
        
        // 添加新点位
        const newLocation = {
          id: newId,
          name: `新点位${newId}`,
          address: '请设置详细地址',
          latitude: 39.9042,
          longitude: 116.4074,
          capacity: 10,
          availableCount: 0,
          bookedCount: 0,
          status: 'ACTIVE'
        }
        
        locations.value.push(newLocation)
        ElMessage.success(`点位 "${newLocation.name}" 已添加（前端演示模式）`)
      }).catch(() => {
        // 用户取消操作
      })
    }
    
    // 搜索处理
    const handleSearch = () => {
      // 搜索逻辑已在computed中处理
    }
    
    // 筛选处理
    const handleFilter = () => {
      // 筛选逻辑已在computed中处理
    }
    
    onMounted(() => {
      loadLocations()
    })
    
    return {
      loading,
      searchKeyword,
      filterStatus,
      locations,
      filteredLocations,
      viewLocation,
      editLocation,
      addLocation,
      handleSearch,
      handleFilter
    }
  }
}
</script>

<style scoped>
.location-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 14px;
  color: #606266;
}

.search-section {
  margin-bottom: 20px;
  display: flex;
  gap: 15px;
  align-items: center;
}

.locations-list {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.text-success {
  color: #67c23a;
}

.text-danger {
  color: #f56c6c;
}

.coordinate-inputs {
  display: flex;
  gap: 4%;
}

.text-success {
  color: #67C23A;
  font-weight: 600;
}

.text-danger {
  color: #F56C6C;
  font-weight: 600;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .location-management {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .search-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-section .el-input {
    width: 100% !important;
  }
}
</style>