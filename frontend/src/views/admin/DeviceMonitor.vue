<template>
  <div class="device-monitor">
    <el-card class="monitor-header">
      <template #header>
        <div class="card-header">
          <h2>设备监控面板</h2>
          <span class="subtitle">实时监控滑板车位置和状态</span>
        </div>
      </template>
      
      <!-- 统计卡片 -->
      <div class="stats-grid">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon online">
              <i class="el-icon-monitor"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.onlineCount }}</div>
              <div class="stat-label">在线设备</div>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon low-battery">
              <i class="el-icon-battery"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.lowBatteryCount }}</div>
              <div class="stat-label">低电量设备</div>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon unlocked">
              <i class="el-icon-unlock"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.unlockedCount }}</div>
              <div class="stat-label">已解锁设备</div>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon total">
              <i class="el-icon-truck"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalCount }}</div>
              <div class="stat-label">总设备数</div>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>

    <!-- 地图和列表布局 -->
    <div class="monitor-content">
      <!-- 地图区域 -->
      <el-card class="map-section" shadow="never">
        <template #header>
          <div class="section-header">
            <h3>设备位置分布</h3>
            <div class="map-controls">
              <el-button size="small" @click="refreshMap">
                <i class="el-icon-refresh"></i> 刷新位置
              </el-button>
              <el-button size="small" @click="toggleAutoRefresh" :type="autoRefresh ? 'primary' : ''">
                <i class="el-icon-time"></i> {{ autoRefresh ? '停止自动刷新' : '自动刷新' }}
              </el-button>
            </div>
          </div>
        </template>
        
        <div class="map-container">
                  <!-- 模拟地图区域 -->
                  <div class="mock-map">
                    <!-- 地图背景 - 使用真实地图样式 -->
                    <div class="map-background">
                      <div class="map-grid">
                        <!-- 模拟地图网格 -->
                        <div class="grid-lines">
                          <div class="grid-line horizontal" v-for="i in 10" :key="'h' + i" :style="{ top: (i * 10) + '%' }"></div>
                          <div class="grid-line vertical" v-for="i in 10" :key="'v' + i" :style="{ left: (i * 10) + '%' }"></div>
                        </div>
                        
                        <!-- 模拟道路 -->
                        <div class="map-roads">
                          <div class="road main-road" style="top: 30%;"></div>
                          <div class="road main-road" style="top: 70%;"></div>
                          <div class="road side-road" style="left: 25%;"></div>
                          <div class="road side-road" style="left: 75%;"></div>
                        </div>
                        
                        <!-- 模拟建筑物 -->
                        <div class="map-buildings">
                          <div class="building large" style="top: 15%; left: 15%;"></div>
                          <div class="building medium" style="top: 15%; left: 60%;"></div>
                          <div class="building small" style="top: 50%; left: 20%;"></div>
                          <div class="building large" style="top: 50%; left: 70%;"></div>
                          <div class="building medium" style="top: 80%; left: 10%;"></div>
                          <div class="building small" style="top: 80%; left: 85%;"></div>
                        </div>
                        
                        <!-- 模拟公园 -->
                        <div class="map-parks">
                          <div class="park" style="top: 40%; left: 45%;"></div>
                        </div>
                      </div>
                    </div>
                    
                    <!-- 设备标记 -->
                    <div class="map-markers">
                      <div 
                        v-for="scooter in scootersWithLocation" 
                        :key="scooter.id"
                        class="map-marker"
                        :class="getMarkerClass(scooter)"
                        :style="getMarkerStyle(scooter)"
                        @click="selectScooter(scooter)"
                      >
                        <div class="marker-content">
                          <div class="battery-indicator" :class="getBatteryClass(scooter.batteryLevel)"></div>
                          <div class="marker-id">{{ scooter.id }}</div>
                        </div>
                        <div class="marker-tooltip" v-if="selectedScooter?.id === scooter.id">
                          <div class="tooltip-content">
                            <div class="tooltip-title">滑板车 #{{ scooter.id }}</div>
                            <div class="tooltip-info">
                              <span>电量: {{ scooter.batteryLevel }}%</span>
                              <span>状态: {{ getStatusText(scooter) }}</span>
                              <span>位置: {{ scooter.latitude?.toFixed(4) }}, {{ scooter.longitude?.toFixed(4) }}</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                    
                    <div class="map-legend">
                      <div class="legend-item">
                        <div class="legend-color online"></div>
                        <span>在线</span>
                      </div>
                      <div class="legend-item">
                        <div class="legend-color offline"></div>
                        <span>离线</span>
                      </div>
                      <div class="legend-item">
                        <div class="legend-color low-battery"></div>
                        <span>低电量</span>
                      </div>
                    </div>
                  </div>
                </div>
      </el-card>

      <!-- 设备列表 -->
      <el-card class="list-section" shadow="never">
        <template #header>
          <div class="section-header">
            <div class="header-content">
              <h3 class="section-title">设备状态列表</h3>
              <div class="list-controls">
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索设备"
                  size="small"
                  style="width: 180px;"
                  clearable
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
                <el-select
                  v-model="statusFilter"
                  placeholder="状态筛选"
                  size="small"
                  style="width: 120px;"
                  clearable
                >
                  <el-option label="全部" value="" />
                  <el-option label="在线" value="online" />
                  <el-option label="离线" value="offline" />
                  <el-option label="低电量" value="low-battery" />
                </el-select>
                <el-button 
                  v-if="searchKeyword || statusFilter" 
                  @click="resetFilters" 
                  size="small" 
                  type="primary" 
                  link
                >
                  重置筛选
                </el-button>
              </div>
            </div>
          </div>
        </template>
        
        <el-table 
          :data="filteredScooters" 
          v-loading="loading" 
          stripe
          :max-height="Math.max(500, Math.min(filteredScooters.length * 50, 500))"
          style="height: auto; min-height: 500px;"
        >
          <el-table-column label="设备ID" prop="id" width="80" fixed />
          <el-table-column label="型号" prop="model" width="120" />
          <el-table-column label="电量" width="120">
            <template #default="{ row }">
              <div class="battery-display">
                <!-- 精致渐变发光电池图标 -->
                <div class="premium-battery">
                  <div class="premium-battery-container">
                    <!-- 电池顶部 -->
                    <div class="premium-battery-tip"></div>
                    <!-- 电池主体 -->
                    <div class="premium-battery-body">
                      <!-- 电量填充层 -->
                      <div 
                        class="premium-battery-fill"
                        :class="getPremiumBatteryClass(row.batteryLevel)"
                        :style="{ height: row.batteryLevel + '%' }"
                      ></div>
                      <!-- 发光效果层 -->
                      <div 
                        class="premium-battery-glow"
                        :class="getPremiumBatteryClass(row.batteryLevel)"
                      ></div>
                    </div>
                  </div>
                </div>
                <!-- 电量百分比 -->
                <span class="battery-percentage" :class="getBatteryTextClass(row.batteryLevel)">
                  {{ formatBatteryLevel(row.batteryLevel) }}%
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row)" size="small">
                {{ getStatusText(row) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="位置" min-width="150">
            <template #default="{ row }">
              <span v-if="row.latitude && row.longitude">
                {{ row.latitude?.toFixed(4) }}, {{ row.longitude?.toFixed(4) }}
              </span>
              <span v-else class="no-location">未知位置</span>
            </template>
          </el-table-column>
          <el-table-column label="最后更新" width="140">
            <template #default="{ row }">
              {{ formatTime(row.lastUpdateTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="selectScooter(row)">
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 设备详情弹窗 -->
    <el-dialog v-model="detailVisible" title="设备详情" width="600px">
      <div v-if="selectedScooter" class="device-detail">
        <div class="detail-section">
          <h4>基本信息</h4>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="detail-item">
                <label>设备ID:</label>
                <span>{{ selectedScooter.id }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <label>型号:</label>
                <span>{{ selectedScooter.model }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
        
        <div class="detail-section">
          <h4>状态信息</h4>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="detail-item">
                <label>电量:</label>
                <span :class="getBatteryTextClass(selectedScooter.batteryLevel)">
                  {{ selectedScooter.batteryLevel }}%
                </span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <label>在线状态:</label>
                <el-tag :type="selectedScooter.isOnline ? 'success' : 'info'">
                  {{ selectedScooter.isOnline ? '在线' : '离线' }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <label>锁定状态:</label>
                <el-tag :type="selectedScooter.isLocked ? 'warning' : 'success'">
                  {{ selectedScooter.isLocked ? '已锁定' : '已解锁' }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <label>总里程:</label>
                <span>{{ selectedScooter.totalMileage || 0 }} km</span>
              </div>
            </el-col>
          </el-row>
        </div>
        
        <div class="detail-section">
          <h4>位置信息</h4>
          <div class="detail-item">
            <label>坐标:</label>
            <span v-if="selectedScooter.latitude && selectedScooter.longitude">
              {{ selectedScooter.latitude }}, {{ selectedScooter.longitude }}
            </span>
            <span v-else class="no-location">无位置信息</span>
          </div>
          <div class="detail-item">
            <label>最后更新:</label>
            <span>{{ formatTime(selectedScooter.lastUpdateTime) }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

// 响应式数据
const loading = ref(false)
const scooters = ref([])
const selectedScooter = ref(null)
const detailVisible = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const autoRefresh = ref(false)
const refreshInterval = ref(null)
const batteryOrientation = ref('vertical') // 电池图标方向：vertical(纵向) / horizontal(横向)

// 调试：检查过滤条件
console.log('搜索关键词:', searchKeyword.value)
console.log('状态过滤器:', statusFilter.value)

// 统计信息
const stats = computed(() => {
  const onlineCount = scooters.value.filter(s => s.isOnline).length
  const lowBatteryCount = scooters.value.filter(s => s.batteryLevel < 20).length
  const unlockedCount = scooters.value.filter(s => !s.isLocked).length
  
  return {
    onlineCount,
    lowBatteryCount,
    unlockedCount,
    totalCount: scooters.value.length
  }
})

// 过滤后的设备列表
const filteredScooters = computed(() => {
  let filtered = scooters.value
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(s => 
      s.id.toString().includes(keyword) || 
      s.model.toLowerCase().includes(keyword)
    )
  }
  
  if (statusFilter.value) {
    switch (statusFilter.value) {
      case 'online':
        filtered = filtered.filter(s => s.isOnline)
        break
      case 'offline':
        filtered = filtered.filter(s => !s.isOnline)
        break
      case 'low-battery':
        filtered = filtered.filter(s => s.batteryLevel < 20)
        break
      case 'unlocked':
        filtered = filtered.filter(s => !s.isLocked)
        break
    }
  }
  
  return filtered
})

// 有位置信息的设备
const scootersWithLocation = computed(() => {
  return scooters.value.filter(s => s.latitude && s.longitude)
})

// 获取设备状态文本
const getStatusText = (scooter) => {
  if (!scooter.isOnline) return '离线'
  if (!scooter.isLocked) return '已解锁'
  return '在线'
}

// 获取状态标签类型
const getStatusTagType = (scooter) => {
  if (!scooter.isOnline) return 'info'
  if (!scooter.isLocked) return 'warning'
  return 'success'
}

// 获取电量样式类
const getBatteryClass = (level) => {
  if (level >= 50) return 'high'
  if (level >= 20) return 'medium'
  return 'low'
}

const getBatteryTextClass = (level) => {
  if (level >= 50) return 'text-high'
  if (level >= 20) return 'text-medium'
  return 'text-low'
}

// 获取精致电池图标样式类
const getPremiumBatteryClass = (level) => {
  if (level >= 50) return 'premium-high'
  if (level >= 20) return 'premium-medium'
  return 'premium-low'
}

// 地图标记样式
const getMarkerClass = (scooter) => {
  const classes = []
  if (!scooter.isOnline) classes.push('offline')
  if (scooter.batteryLevel < 20) classes.push('low-battery')
  if (!scooter.isLocked) classes.push('unlocked')
  return classes.join(' ')
}

const getMarkerStyle = (scooter) => {
  // 模拟地图位置（实际应该使用真实坐标计算）
  const x = (scooter.latitude % 90 + 90) / 180 * 100
  const y = (scooter.longitude % 180 + 180) / 360 * 100
  return {
    left: `${x}%`,
    top: `${y}%`
  }
}

// 选择设备
const selectScooter = (scooter) => {
  selectedScooter.value = scooter
}

// 查看详情
const viewDetails = (scooter) => {
  selectedScooter.value = scooter
  detailVisible.value = true
}

// 重置过滤器
const resetFilters = () => {
  searchKeyword.value = ''
  statusFilter.value = ''
  console.log('过滤器已重置，总设备数量:', scooters.value.length)
  console.log('过滤后设备数量:', filteredScooters.value.length)
}

// 切换电池图标方向
const toggleBatteryOrientation = () => {
  batteryOrientation.value = batteryOrientation.value === 'vertical' ? 'horizontal' : 'vertical'
  console.log('电池图标方向已切换为:', batteryOrientation.value)
}

// 刷新地图
const refreshMap = async () => {
  await loadScooters()
  ElMessage.success('设备位置已刷新')
}

// 切换自动刷新
const toggleAutoRefresh = () => {
  autoRefresh.value = !autoRefresh.value
  
  if (autoRefresh.value) {
    refreshInterval.value = setInterval(() => {
      loadScooters()
    }, 30000) // 30秒刷新一次
    ElMessage.info('已开启自动刷新')
  } else {
    clearInterval(refreshInterval.value)
    ElMessage.info('已关闭自动刷新')
  }
}

// 格式化电量（保留两位小数）
const formatBatteryLevel = (level) => {
  if (level === null || level === undefined) return '0.00'
  return Number(level).toFixed(2)
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '未知'
  return new Date(time).toLocaleString('zh-CN')
}

// 加载设备数据
const loadScooters = async () => {
  try {
    loading.value = true
    // 使用配置了JWT令牌的axios实例
    const response = await api.get('/scooters')
    console.log('API调用成功，返回数据数量:', response.data.length)
    console.log('API返回的数据结构:', response.data[0])
    
    // 确保使用后端返回的真实数据
    scooters.value = response.data.map(scooter => {
      console.log('原始数据:', scooter)
      
      // 正确处理数据类型转换
      const batteryLevel = scooter.batteryLevel !== null && scooter.batteryLevel !== undefined 
        ? Number(scooter.batteryLevel) 
        : 100
      
      const isOnline = scooter.isOnline !== null && scooter.isOnline !== undefined 
        ? Boolean(scooter.isOnline)
        : false
        
      const isLocked = scooter.isLocked !== null && scooter.isLocked !== undefined 
        ? Boolean(scooter.isLocked)
        : true
        
      const totalMileage = scooter.totalMileage !== null && scooter.totalMileage !== undefined 
        ? Number(scooter.totalMileage)
        : 0
        
      return {
        id: scooter.id,
        model: scooter.model,
        batteryLevel: batteryLevel,
        isOnline: isOnline,
        isLocked: isLocked,
        latitude: scooter.latitude || null,
        longitude: scooter.longitude || null,
        locationName: scooter.locationName || '未知位置',
        totalMileage: totalMileage,
        lastUpdateTime: scooter.lastUpdateTime || new Date().toISOString()
      }
    })
    
    console.log('处理后设备数量:', scooters.value.length)
    console.log('过滤后设备数量:', filteredScooters.value.length)
  } catch (error) {
    console.error('加载设备数据失败:', error)
    console.log('使用模拟数据')
    // 如果API调用失败，使用模拟数据
    scooters.value = generateMockScooters()
    console.log('总设备数量:', scooters.value.length)
    console.log('过滤后设备数量:', filteredScooters.value.length)
  } finally {
    loading.value = false
  }
}

// 生成更真实的模拟数据（实际使用时删除）
const generateMockScooters = () => {
  const models = ['X-Turbo', 'Speed-2000', 'Eco-Ride', 'Power-Glide', 'City-Cruiser', 'Mountain-Pro', 'Urban-Express']
  const locations = [
    { name: '市中心', lat: 39.91, lng: 116.41 },
    { name: '大学城', lat: 39.93, lng: 116.38 },
    { name: '商业区', lat: 39.89, lng: 116.43 },
    { name: '居民区', lat: 39.95, lng: 116.35 },
    { name: '公园', lat: 39.87, lng: 116.46 }
  ]
  
  return Array.from({ length: 50 }, (_, i) => {
    const location = locations[i % locations.length]
    const model = models[i % models.length]
    
    // 更真实的电量分布：大部分在20-100%之间，少量低电量
    let batteryLevel
    if (Math.random() < 0.1) { // 10%的概率为低电量（<20%）
      batteryLevel = Math.floor(Math.random() * 20)
    } else if (Math.random() < 0.3) { // 30%的概率为中等电量（20-70%）
      batteryLevel = 20 + Math.floor(Math.random() * 50)
    } else { // 60%的概率为高电量（70-100%）
      batteryLevel = 70 + Math.floor(Math.random() * 30)
    }
    
    // 更真实的状态分布
    const isOnline = Math.random() > 0.15 // 85%在线率
    const isLocked = isOnline ? Math.random() > 0.1 : true // 在线设备90%锁定，离线设备全部锁定
    
    // 更真实的里程分布
    const totalMileage = Math.floor(Math.random() * 5000) // 0-5000公里
    
    // 更真实的位置分布（在选定位置附近随机分布）
    const latitude = location.lat + (Math.random() - 0.5) * 0.05
    const longitude = location.lng + (Math.random() - 0.5) * 0.05
    
    return {
      id: i + 1,
      model: model,
      batteryLevel: batteryLevel,
      isOnline: isOnline,
      isLocked: isLocked,
      latitude: latitude,
      longitude: longitude,
      locationName: location.name,
      totalMileage: totalMileage,
      lastUpdateTime: new Date(Date.now() - Math.random() * 7200000).toISOString() // 0-2小时前更新
    }
  })
}

// 生命周期
onMounted(() => {
  loadScooters()
})

onUnmounted(() => {
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value)
  }
})
</script>

<style scoped>
.device-monitor {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.monitor-header {
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 16px;
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

.stat-icon.online { background: #67c23a; }
.stat-icon.low-battery { background: #e6a23c; }
.stat-icon.unlocked { background: #f56c6c; }
.stat-icon.total { background: #409eff; }

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.monitor-content {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 20px;
  min-height: 500px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  gap: 20px;
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
  min-width: 120px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.map-container {
  height: 400px;
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
}

.mock-map {
  position: relative;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #87CEEB 0%, #98FB98 100%);
}

/* 地图背景 */
.map-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

/* 道路样式 */
.map-roads {
  position: absolute;
  width: 100%;
  height: 100%;
}

.road {
  position: absolute;
  background: #666;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.road.main-road {
  width: 100%;
  height: 6px;
  background: #555;
}

.road.side-road {
  width: 4px;
  height: 100%;
  background: #777;
}

/* 建筑物样式 */
.map-buildings {
  position: absolute;
  width: 100%;
  height: 100%;
}

.building {
  position: absolute;
  border-radius: 2px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

.building.large {
  width: 50px;
  height: 70px;
  background: #8B4513;
}

.building.medium {
  width: 35px;
  height: 50px;
  background: #A0522D;
}

.building.small {
  width: 25px;
  height: 35px;
  background: #CD853F;
}

/* 公园样式 */
.map-parks {
  position: absolute;
  width: 100%;
  height: 100%;
}

.park {
  position: absolute;
  width: 100px;
  height: 100px;
  background: #32CD32;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

/* 设备标记容器 */
.map-markers {
  position: relative;
  width: 100%;
  height: 100%;
}

.map-grid {
  position: relative;
  width: 100%;
  height: 100%;
}

.map-marker {
  position: absolute;
  width: 24px;
  height: 24px;
  transform: translate(-50%, -50%);
  cursor: pointer;
  transition: all 0.3s ease;
}

.map-marker:hover {
  transform: translate(-50%, -50%) scale(1.2);
  z-index: 10;
}

.marker-content {
  position: relative;
  width: 100%;
  height: 100%;
}

.battery-indicator {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  margin: 0 auto 2px;
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.battery-indicator.high { background: #67c23a; }
.battery-indicator.medium { background: #e6a23c; }
.battery-indicator.low { background: #f56c6c; }

.marker-id {
  font-size: 10px;
  text-align: center;
  color: white;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.8);
  font-weight: bold;
}

.map-marker.online .battery-indicator {
  animation: pulse 2s infinite;
}

.map-marker.offline .battery-indicator {
  background: #909399 !important;
}

.map-marker.low-battery .battery-indicator {
  animation: blink 1s infinite;
}

.map-marker.unlocked .marker-content {
  animation: bounce 0.5s ease-in-out infinite alternate;
}

.marker-tooltip {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  margin-bottom: 8px;
  background: white;
  border-radius: 6px;
  padding: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  min-width: 200px;
  z-index: 100;
}

.tooltip-title {
  font-weight: 600;
  margin-bottom: 4px;
}

.tooltip-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
  color: #666;
}

.map-legend {
  position: absolute;
  bottom: 16px;
  right: 16px;
  background: white;
  padding: 12px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-color.online { background: #67c23a; }
.legend-color.offline { background: #909399; }
.legend-color.low-battery { background: #f56c6c; }

.battery-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.battery-icon {
  display: flex;
  align-items: center;
}

/* 纵向电池样式 - 现代简洁设计 */
.battery-outline.vertical {
  width: 20px;
  height: 32px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 3px;
  position: relative;
  overflow: hidden;
}

.battery-fill.vertical {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  transition: height 0.3s ease;
  border-radius: 2px;
}

.battery-tip.vertical {
  position: absolute;
  top: -3px;
  left: 50%;
  transform: translateX(-50%);
  width: 8px;
  height: 3px;
  background: #e4e7ed;
  border-radius: 2px 2px 0 0;
}

/* 横向电池样式 - 现代简洁设计 */
.battery-outline.horizontal {
  width: 32px;
  height: 20px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 3px;
  position: relative;
  overflow: hidden;
}

.battery-fill.horizontal {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  transition: width 0.3s ease;
  border-radius: 2px;
}

.battery-tip.horizontal {
  position: absolute;
  right: -3px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 8px;
  background: #e4e7ed;
  border-radius: 0 2px 2px 0;
}

/* 通用电池填充颜色 - 现代简洁 */
.battery-fill.high { 
  background: #67c23a;
}
.battery-fill.medium { 
  background: #e6a23c;
}
.battery-fill.low { 
  background: #f56c6c;
}

.battery-percentage {
  font-size: 12px;
  font-weight: 500;
  min-width: 30px;
}

.battery-percentage.text-high { color: #67c23a; }
.battery-percentage.text-medium { color: #e6a23c; }
.battery-percentage.text-low { color: #f56c6c; }

.text-high { color: #67c23a; }
.text-medium { color: #e6a23c; }
.text-low { color: #f56c6c; }

/* 精致渐变发光电池图标样式 */
.premium-battery {
  display: flex;
  align-items: center;
  gap: 8px;
}

.premium-battery-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.premium-battery-tip {
  width: 10px;
  height: 4px;
  background: linear-gradient(145deg, #adb5bd, #ced4da);
  border-radius: 3px 3px 0 0;
  margin-bottom: 1px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.premium-battery-body {
  width: 26px;
  height: 36px;
  background: linear-gradient(145deg, #f8f9fa, #e9ecef);
  border: 2px solid #ced4da;
  border-radius: 6px;
  position: relative;
  overflow: hidden;
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.1),
    inset 0 1px 3px rgba(255, 255, 255, 0.8);
}

.premium-battery-fill {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  transition: height 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 4px;
}

.premium-battery-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 4px;
  opacity: 0.3;
  filter: blur(4px);
  transition: all 0.5s ease;
}

/* 高电量样式 - 绿色渐变发光 */
.premium-battery-fill.premium-high {
  background: linear-gradient(145deg, #52c41a, #73d13d, #95de64);
  box-shadow: 
    inset 0 2px 4px rgba(255, 255, 255, 0.4),
    0 0 8px rgba(82, 196, 26, 0.6);
}

.premium-battery-glow.premium-high {
  background: radial-gradient(circle at 50% 20%, rgba(82, 196, 26, 0.8), transparent 70%);
}

/* 中电量样式 - 橙色渐变发光 */
.premium-battery-fill.premium-medium {
  background: linear-gradient(145deg, #fa8c16, #faad14, #ffc069);
  box-shadow: 
    inset 0 2px 4px rgba(255, 255, 255, 0.4),
    0 0 8px rgba(250, 140, 22, 0.6);
}

.premium-battery-glow.premium-medium {
  background: radial-gradient(circle at 50% 20%, rgba(250, 140, 22, 0.8), transparent 70%);
}

/* 低电量样式 - 红色渐变发光 */
.premium-battery-fill.premium-low {
  background: linear-gradient(145deg, #f5222d, #ff4d4f, #ff7875);
  box-shadow: 
    inset 0 2px 4px rgba(255, 255, 255, 0.4),
    0 0 8px rgba(245, 34, 45, 0.6);
}

.premium-battery-glow.premium-low {
  background: radial-gradient(circle at 50% 20%, rgba(245, 34, 45, 0.8), transparent 70%);
}

.premium-battery-number {
  font-size: 14px;
  font-weight: 700;
  min-width: 24px;
  text-align: center;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.no-location {
  color: #c0c4cc;
  font-style: italic;
}

.device-detail {
  max-height: 400px;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-weight: 600;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item label {
  font-weight: 500;
  color: #606266;
}

.detail-item span {
  color: #303133;
}

/* 动画效果 */
@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0.3; }
}

@keyframes bounce {
  0% { transform: translateY(0); }
  100% { transform: translateY(-4px); }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .monitor-content {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .device-monitor {
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
  
  .map-controls, .list-controls {
  display: flex;
  gap: 12px;
  align-items: center;
  flex: 1;
  justify-content: flex-end;
}

.control-row {
    display: flex;
    gap: 12px;
    align-items: center;
    flex-wrap: nowrap;
  }
}

/* 列表区域样式 */
.list-section {
  min-height: 400px;
}

.list-section .el-table {
  margin-top: 16px;
}

/* 表格滚动条优化 */
.list-section .el-table__body-wrapper {
  max-height: 340px;
  overflow-y: auto;
}

/* 表格行高优化 */
.list-section .el-table .el-table__row {
  height: 40px;
}

.list-section .el-table .el-table__cell {
  padding: 8px 0;
}

/* 滚动条样式 */
.list-section .el-table__body-wrapper::-webkit-scrollbar {
  width: 6px;
}

.list-section .el-table__body-wrapper::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.list-section .el-table__body-wrapper::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.list-section .el-table__body-wrapper::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>