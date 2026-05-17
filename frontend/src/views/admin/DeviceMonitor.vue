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
          <div id="device-monitor-map" class="leaflet-map"></div>
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
          max-height="450"
          style="width: 100%;"
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
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const loading = ref(false)
const scooters = ref([])
const selectedScooter = ref(null)
const detailVisible = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const autoRefresh = ref(false)
const refreshInterval = ref(null)
const batteryOrientation = ref('vertical')
const activeBookingsCount = ref(0)
const deviceMap = ref(null)
const mapMarkers = ref([])

// 调试：检查过滤条件
console.log('搜索关键词:', searchKeyword.value)
console.log('状态过滤器:', statusFilter.value)

// 统计信息
const stats = computed(() => {
  const onlineCount = scooters.value.filter(s => s.isOnline).length
  const lowBatteryCount = scooters.value.filter(s => s.batteryLevel < 30).length
  
  return {
    onlineCount,
    lowBatteryCount,
    unlockedCount: activeBookingsCount.value, // 使用真实的活跃预订数量
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
        filtered = filtered.filter(s => s.batteryLevel < 30)
        break
      case 'unlocked':
        filtered = filtered.filter(s => !s.isLocked)
        break
    }
  }
  
  // 按ID升序排序
  return filtered.sort((a, b) => a.id - b.id)
})

// 有位置信息的设备
const scootersWithLocation = computed(() => {
  return scooters.value.filter(s => s.latitude && s.longitude)
})

// 获取设备状态文本
const getStatusText = (scooter) => {
  if (!scooter.isOnline) return '离线'
  if (scooter.hasActiveBooking && !scooter.isLocked) return '已解锁'
  if (scooter.hasActiveBooking && scooter.isLocked) return '预订中'
  return '在线'
}

// 获取状态标签类型
const getStatusTagType = (scooter) => {
  if (!scooter.isOnline) return 'info'
  if (scooter.hasActiveBooking && !scooter.isLocked) return 'warning'
  if (scooter.hasActiveBooking && scooter.isLocked) return 'primary'
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
}

// 初始化 Leaflet 地图
const initDeviceMap = async () => {
  await nextTick()
  const mapContainer = document.getElementById('device-monitor-map')
  if (!mapContainer) return

  if (deviceMap.value) {
    deviceMap.value.remove()
    deviceMap.value = null
  }

  if (!window.L) {
    const script = document.createElement('script')
    script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'
    const link = document.createElement('link')
    link.rel = 'stylesheet'
    link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'

    await new Promise((resolve) => {
      script.onload = () => {
        document.head.appendChild(link)
        resolve()
      }
      document.head.appendChild(script)
    })
  }

  deviceMap.value = L.map('device-monitor-map').setView([39.9042, 116.4074], 13)

  const tileLayers = [
    {
      url: 'https://{s}.tile.openstreetmap.de/{z}/{x}/{y}.png',
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
      subdomains: ['a', 'b', 'c']
    },
    {
      url: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    },
    {
      url: 'https://{s}.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png',
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
      subdomains: ['a', 'b', 'c']
    }
  ]

  let tileLayerAdded = false
  for (const layerConfig of tileLayers) {
    try {
      const layer = L.tileLayer(layerConfig.url, {
        attribution: layerConfig.attribution,
        subdomains: layerConfig.subdomains || ['a', 'b', 'c'],
        maxZoom: 19,
        errorTileUrl: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjU2IiBoZWlnaHQ9IjI1NiIgdmlld0JveD0iMCAwIDI1NiAyNTYiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIyNTYiIGhlaWdodD0iMjU2IiBmaWxsPSIjRjBGMEYwIi8+Cjx0ZXh0IHg9IjEyOCIgeT0iMTI4IiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iMTIiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGZpbGw9IiM2NjYiPk1hcCBUaWxlPC90ZXh0Pgo8L3N2Zz4K'
      })
      layer.on('tileerror', function(error) {
        console.warn('设备监控地图瓦片加载错误:', error)
      })
      layer.addTo(deviceMap.value)
      tileLayerAdded = true
      break
    } catch (error) {
      console.warn(`设备监控地图瓦片 ${layerConfig.url} 加载失败:`, error)
      continue
    }
  }

  if (!tileLayerAdded) {
    console.warn('设备监控地图所有在线瓦片加载失败，使用离线模式')
    const mapEl = document.getElementById('device-monitor-map')
    if (mapEl) {
      mapEl.style.background = 'var(--color-bg-secondary)'
      const offlineMsg = document.createElement('div')
      offlineMsg.style.cssText = 'position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);text-align:center;color:var(--color-text-secondary);z-index:1000;'
      offlineMsg.innerHTML = '<p>地图加载失败，请检查网络连接</p>'
      mapEl.appendChild(offlineMsg)
    }
  }

  updateMapMarkers()
}

const updateMapMarkers = () => {
  if (!deviceMap.value) return

  mapMarkers.value.forEach(m => deviceMap.value.removeLayer(m))
  mapMarkers.value = []

  const scootersWithLoc = scooters.value.filter(s => s.latitude && s.longitude)

  scootersWithLoc.forEach(scooter => {
    let markerColor = '#67c23a'
    if (!scooter.isOnline) markerColor = '#909399'
    else if (scooter.batteryLevel < 20) markerColor = '#f56c6c'
    else if (!scooter.isLocked) markerColor = '#e6a23c'

    const markerIcon = L.divIcon({
      className: 'device-map-marker',
      html: `<div style="background:${markerColor};width:14px;height:14px;border-radius:50%;border:2px solid #fff;box-shadow:0 2px 4px rgba(0,0,0,0.3);"></div>`,
      iconSize: [18, 18],
      iconAnchor: [9, 9]
    })

    const marker = L.marker([scooter.latitude, scooter.longitude], { icon: markerIcon })
      .addTo(deviceMap.value)
      .bindPopup(`
        <b>滑板车 #${scooter.id}</b><br/>
        型号: ${scooter.model}<br/>
        电量: ${scooter.batteryLevel}%<br/>
        状态: ${getStatusText(scooter)}<br/>
        位置: ${scooter.latitude?.toFixed(4)}, ${scooter.longitude?.toFixed(4)}
      `)

    marker.on('click', () => {
      selectScooter(scooter)
    })

    mapMarkers.value.push(marker)
  })
}

// 切换电池图标方向
const toggleBatteryOrientation = () => {
  batteryOrientation.value = batteryOrientation.value === 'vertical' ? 'horizontal' : 'vertical'
  console.log('电池图标方向已切换为:', batteryOrientation.value)
}

// 刷新地图
const refreshMap = async () => {
  await loadScooters()
  updateMapMarkers()
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

// 加载设备数据和活跃预订数量
const loadScooters = async () => {
  try {
    loading.value = true
    
    // 使用新的API端点获取设备及其预订状态
    const scootersResponse = await api.get('/device/with-booking-status')
    
    console.log('API调用成功，返回设备数据数量:', scootersResponse.data.length)
    console.log('API返回的设备数据结构:', scootersResponse.data[0])
    
    // 计算活跃预订数量
    activeBookingsCount.value = scootersResponse.data.filter(scooter => scooter.hasActiveBooking).length
    
    // 确保使用后端返回的真实数据
    scooters.value = scootersResponse.data.map(scooter => {
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
        
      const hasActiveBooking = scooter.hasActiveBooking !== null && scooter.hasActiveBooking !== undefined 
        ? Boolean(scooter.hasActiveBooking)
        : false
        
      return {
        id: scooter.id,
        model: scooter.model,
        batteryLevel: batteryLevel,
        isOnline: isOnline,
        isLocked: isLocked,
        hasActiveBooking: hasActiveBooking,
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
    // 模拟数据中不设置活跃预订数量，保持为0
    console.log('总设备数量:', scooters.value.length)
    console.log('过滤后设备数量:', filteredScooters.value.length)
  } finally {
    loading.value = false
  }
}

// 生成更真实的模拟数据（实际使用时删除）
const generateMockScooters = () => {
  const models = ['城市通勤款', '校园轻便款', '商务精英款', '时尚潮流款', '休闲娱乐款']
  const locations = [
    { name: '市中心', lat: 39.91, lng: 116.41 },
    { name: '大学城', lat: 39.93, lng: 116.38 },
    { name: '商业区', lat: 39.89, lng: 116.43 },
    { name: '居民区', lat: 39.95, lng: 116.35 },
    { name: '公园', lat: 39.87, lng: 116.46 }
  ]
  
  return Array.from({ length: 200 }, (_, i) => {
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
onMounted(async () => {
  await loadScooters()
  await initDeviceMap()
})

onUnmounted(() => {
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value)
  }
  if (deviceMap.value) {
    deviceMap.value.remove()
    deviceMap.value = null
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
  color: var(--color-text-secondary);
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
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--color-text-primary);
}

.stat-icon.online { background: #67c23a; }
.stat-icon.low-battery { background: #e6a23c; }
.stat-icon.unlocked { background: #f56c6c; }
.stat-icon.total { background: #409eff; }

.stat-value {
  font-size: 24px;
  font-weight: 600;
  /* 移除硬编码颜色，让主题模式能够正确应用 */
}

.stat-label {
  font-size: 14px;
  /* 移除硬编码颜色，让主题模式能够正确应用 */
}

/* 深色模式统计卡片数字颜色优化 - 提高对比度 */
[data-theme="dark"] .device-monitor .stat-card .stat-value {
  color: #ffffff !important;
  font-weight: 700 !important;
}

[data-theme="dark"] .device-monitor .stat-card .stat-label {
  color: #e2e8f0 !important;
}

/* 高对比度模式统计卡片数字颜色优化 - 强烈对比 */
[data-theme="high-contrast"] .device-monitor .stat-card .stat-value {
  color: #ffff00 !important;
  font-weight: 800 !important;
  text-shadow: 0 0 2px #000000 !important;
}

[data-theme="high-contrast"] .device-monitor .stat-card .stat-label {
  color: #ffffff !important;
  font-weight: 600 !important;
}

.monitor-content {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 20px;
  align-items: start;
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
  background: var(--color-bg-secondary);
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}

.leaflet-map {
  width: 100%;
  height: 100%;
}

.map-legend {
  position: absolute;
  bottom: 16px;
  right: 16px;
  background: var(--color-bg-secondary);
  padding: 12px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  gap: 12px;
  z-index: 1000;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-primary);
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
  color: var(--color-text-tertiary);
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
  color: var(--color-text-primary);
  font-weight: 600;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding: 8px 0;
  border-bottom: 1px solid var(--color-border);
}

.detail-item label {
  font-weight: 500;
  color: var(--color-text-secondary);
}

.detail-item span {
  color: var(--color-text-primary);
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
  
  .map-controls,.device-monitor .list-controls {
  display: flex;
  gap: 12px;
  align-items: center;
  flex: 1;
  justify-content: flex-end;
}

.list-section .section-header {
  padding: 0;
}

.list-section .header-content {
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

/* 设备监控页面按钮深色主题适配 - 提高优先级 */
[data-theme="dark"] .device-monitor .map-controls .el-button,
[data-theme="dark"] .device-monitor .list-controls .el-button {
  background: var(--color-bg-secondary-dark) !important;
  border-color: var(--color-border-dark) !important;
  color: var(--color-text-primary-dark) !important;
  transition: all 0.3s ease;
}

[data-theme="dark"] .device-monitor .map-controls .el-button:hover,
[data-theme="dark"] .device-monitor .list-controls .el-button:hover {
  background: var(--color-bg-tertiary-dark) !important;
  border-color: var(--color-primary-dark) !important;
  color: var(--color-primary-dark) !important;
  transform: translateY(-1px);
}

[data-theme="dark"] .device-monitor .map-controls .el-button--primary,
[data-theme="dark"] .device-monitor .list-controls .el-button--primary {
  background: var(--color-primary-dark) !important;
  border-color: var(--color-primary-dark) !important;
  color: var(--color-text-primary-dark) !important;
}

[data-theme="dark"] .device-monitor .map-controls .el-button--primary:hover,
[data-theme="dark"] .device-monitor .list-controls .el-button--primary:hover {
  background: var(--color-primary-light-dark) !important;
  border-color: var(--color-primary-light-dark) !important;
}

.control-row {
    display: flex;
    gap: 12px;
    align-items: center;
    flex-wrap: nowrap;
  }
}
</style>

<style>
/* ===== 设备管理 - Element Plus 组件主题适配 ===== */

/* 列表区域样式 */
.device-monitor .list-section {
  min-height: 400px;
}

.device-monitor .list-section .el-table {
  margin-top: 0;
  background: transparent;
  color: var(--color-text-primary);
}

.device-monitor .list-section .el-table__header-wrapper {
  position: sticky;
  top: 0;
  z-index: 2;
}

.device-monitor .list-section .el-table th.el-table__cell {
  background: var(--color-bg-tertiary) !important;
  color: var(--color-text-primary) !important;
  border-bottom: 1px solid var(--color-border) !important;
  font-weight: 600;
}

.device-monitor .list-section .el-table td.el-table__cell {
  background: var(--color-bg-secondary) !important;
  color: var(--color-text-primary) !important;
  border-bottom: 1px solid var(--color-border) !important;
}

.device-monitor .list-section .el-table .el-table__cell .cell {
  color: var(--color-text-primary) !important;
}

.device-monitor .list-section .el-table tr {
  background: transparent !important;
}

.device-monitor .list-section .el-table__body tr:hover > td.el-table__cell {
  background: rgba(99, 102, 241, 0.1) !important;
}

.device-monitor .list-section .el-table__empty-block {
  background: var(--color-bg-secondary) !important;
}

.device-monitor .list-section .el-table__empty-text {
  color: var(--color-text-tertiary) !important;
}

.device-monitor .list-section .el-table .el-table__row {
  height: 40px;
}

.device-monitor .list-section .el-table td {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 标签 */
.device-monitor .el-tag--success {
  background: rgba(34, 197, 94, 0.15) !important;
  color: var(--color-success) !important;
}

.device-monitor .el-tag--warning {
  background: rgba(245, 158, 11, 0.15) !important;
  color: var(--color-warning) !important;
}

.device-monitor .el-tag--info {
  background: rgba(14, 165, 233, 0.15) !important;
  color: var(--color-info) !important;
}

.device-monitor .el-tag--primary {
  background: rgba(99, 102, 241, 0.15) !important;
  color: var(--color-primary) !important;
}

/* 搜索框和筛选器 */
.device-monitor .list-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

.device-monitor .list-controls .el-input__wrapper {
  background-color: var(--color-bg-secondary) !important;
  border: 1px solid var(--color-primary) !important;
  box-shadow: 0 0 0 1px var(--color-primary) inset, 0 0 0 1px rgba(99, 102, 241, 0.2) !important;
}

.device-monitor .list-controls .el-input__wrapper:hover {
  border-color: var(--color-primary-light) !important;
  box-shadow: 0 0 0 1px var(--color-primary-light) inset, 0 0 0 1px rgba(139, 92, 246, 0.3) !important;
}

.device-monitor .list-controls .el-input__wrapper.is-focus {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2) !important;
}

.device-monitor .list-controls .el-select .el-input__wrapper {
  background-color: #ffffff !important;
  border: 1px solid #409eff !important;
  box-shadow: 0 0 0 1px #409eff inset, 0 0 0 1px rgba(64, 158, 255, 0.2) !important;
}

.device-monitor .list-controls .el-select .el-input__wrapper:hover {
  border-color: #66b1ff !important;
  box-shadow: 0 0 0 1px #66b1ff inset, 0 0 0 1px rgba(102, 177, 255, 0.3) !important;
}

.device-monitor .list-controls .el-select .el-input__wrapper.is-focus {
  border-color: #409eff !important;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2) !important;
}

.device-monitor .list-controls .el-button--primary.is-link {
  color: #409eff !important;
  font-weight: 500;
}

.device-monitor .list-controls .el-button--primary.is-link:hover {
  color: #66b1ff !important;
  text-decoration: underline;
}

/* 滚动条样式 */
.device-monitor .list-section .el-table__body-wrapper::-webkit-scrollbar {
  width: 6px;
}

.device-monitor .list-section .el-table__body-wrapper::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.device-monitor .list-section .el-table__body-wrapper::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.device-monitor .list-section .el-table__body-wrapper::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* ===== 深色模式 ===== */
[data-theme="dark"] .device-monitor .list-section .el-table th.el-table__cell {
  background: var(--color-bg-tertiary) !important;
  color: var(--color-text-primary) !important;
  border-bottom: 2px solid var(--color-border) !important;
}

[data-theme="dark"] .device-monitor .list-section .el-table td.el-table__cell {
  background: var(--color-bg-secondary) !important;
  color: var(--color-text-primary) !important;
  border-bottom: 1px solid var(--color-border) !important;
}

[data-theme="dark"] .device-monitor .list-section .el-table__body tr:hover > td.el-table__cell {
  background: rgba(99, 102, 241, 0.15) !important;
}

[data-theme="dark"] .device-monitor .list-section .el-table .el-table__cell .cell {
  color: var(--color-text-primary) !important;
}

[data-theme="dark"] .device-monitor .list-controls .el-input__wrapper {
  background-color: var(--color-bg-secondary) !important;
  border-color: var(--color-primary) !important;
  color: var(--color-text-primary) !important;
}

[data-theme="dark"] .device-monitor .list-controls .el-input__wrapper:hover {
  background-color: var(--color-bg-tertiary) !important;
  border-color: var(--color-primary-light) !important;
}

[data-theme="dark"] .device-monitor .list-controls .el-input__inner {
  color: var(--color-text-primary) !important;
}

[data-theme="dark"] .device-monitor .list-controls .el-select .el-input__wrapper {
  background-color: var(--color-bg-secondary) !important;
  border-color: var(--color-primary) !important;
  color: var(--color-text-primary) !important;
}

[data-theme="dark"] .device-monitor .list-controls .el-select .el-input__wrapper:hover {
  background-color: var(--color-bg-tertiary) !important;
  border-color: var(--color-primary-light) !important;
}

[data-theme="dark"] .device-monitor .list-controls .el-select .el-input__inner {
  color: var(--color-text-primary) !important;
}

[data-theme="dark"] .device-monitor .list-controls .el-select-dropdown {
  background-color: var(--color-bg-secondary) !important;
  border-color: var(--color-border) !important;
}

[data-theme="dark"] .device-monitor .list-controls .el-select-dropdown__item {
  color: var(--color-text-primary) !important;
  background-color: var(--color-bg-secondary) !important;
}

[data-theme="dark"] .device-monitor .list-controls .el-select-dropdown__item:hover {
  background-color: var(--color-bg-tertiary) !important;
  color: var(--color-primary) !important;
}

[data-theme="dark"] .device-monitor .list-controls .el-select-dropdown__item.selected {
  background-color: rgba(99, 102, 241, 0.1) !important;
  color: var(--color-primary) !important;
}

/* ===== 高对比度模式 ===== */
[data-theme="high-contrast"] .device-monitor .list-section .el-table th.el-table__cell {
  background: var(--color-bg-tertiary) !important;
  color: var(--color-text-primary) !important;
  border-bottom: 3px solid var(--color-border) !important;
  font-weight: 700;
}

[data-theme="high-contrast"] .device-monitor .list-section .el-table td.el-table__cell {
  background: var(--color-bg-secondary) !important;
  color: var(--color-text-primary) !important;
  border-bottom: 2px solid var(--color-border) !important;
}

[data-theme="high-contrast"] .device-monitor .list-section .el-table__body tr:hover > td.el-table__cell {
  background: rgba(255, 255, 0, 0.2) !important;
}

[data-theme="high-contrast"] .device-monitor .list-section .el-table .el-table__cell .cell {
  color: var(--color-text-primary) !important;
}

[data-theme="high-contrast"] .device-monitor .el-tag--success {
  background: var(--color-success) !important;
  color: #000000 !important;
  border: 2px solid #000000 !important;
  font-weight: 700;
}

[data-theme="high-contrast"] .device-monitor .el-tag--warning {
  background: var(--color-warning) !important;
  color: #000000 !important;
  border: 2px solid #000000 !important;
  font-weight: 700;
}

[data-theme="high-contrast"] .device-monitor .el-tag--info {
  background: var(--color-info) !important;
  color: #000000 !important;
  border: 2px solid #000000 !important;
  font-weight: 700;
}

[data-theme="high-contrast"] .device-monitor .el-tag--primary {
  background: var(--color-primary) !important;
  color: #000000 !important;
  border: 2px solid #000000 !important;
  font-weight: 700;
}
</style>