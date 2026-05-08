<template>
  <div class="map-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">地图找车</h1>
        <p class="page-subtitle">查看附近的滑板车停放点位</p>
      </div>
    </div>
    
    <!-- 地图容器 -->
    <div id="map-container" class="amap-container"></div>
    
    <!-- 点位信息弹窗 -->
    <el-dialog
      v-model="showLocationDialog"
      :title="selectedLocation?.name"
      width="400px"
      center
      :close-on-click-modal="true"
    >
      <div class="location-info">
        <div class="info-item">
          <span class="info-label">地址：</span>
          <span class="info-value">{{ selectedLocation?.address || '未知地址' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">可用滑板车：</span>
          <span class="info-value available">{{ selectedLocation?.availableCount || 0 }} 辆</span>
        </div>
        <div class="info-item">
          <span class="info-label">已预订：</span>
          <span class="info-value booked">{{ selectedLocation?.bookedCount || 0 }} 辆</span>
        </div>
        <div class="info-item">
          <span class="info-label">总数量：</span>
          <span class="info-value">{{ selectedLocation?.totalCount || 0 }} 辆</span>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showLocationDialog = false">关闭</el-button>
          <el-button type="primary" @click="goToScooters">去租车</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import api from '@/api'

export default {
  name: 'Map',
  setup() {
    const router = useRouter()
    const showLocationDialog = ref(false)
    const selectedLocation = ref(null)
    const map = ref(null)
    const markers = ref([])
    
    // 10个固定点位的坐标（北京市中心区域）
    const locationCoordinates = {
      1: { lng: 116.4074, lat: 39.9042 }, // 市中心广场
      2: { lng: 116.3509, lat: 39.9896 }, // 大学城校区
      3: { lng: 116.3631, lat: 39.9138 }, // 商业步行街
      4: { lng: 116.3912, lat: 39.9022 }, // 地铁站出口
      5: { lng: 116.3972, lat: 39.9163 }, // 公园入口
      6: { lng: 116.3305, lat: 39.9786 }, // 科技园区
      7: { lng: 116.3789, lat: 39.9542 }, // 住宅小区
      8: { lng: 116.4201, lat: 39.9083 }, // 商务中心
      9: { lng: 116.4327, lat: 39.8998 }, // 星级酒店
      10: { lng: 116.3854, lat: 39.9247 }  // 购物中心
    }
    
    // 加载滑板车数据并生成点位信息
    const loadScootersData = async () => {
      try {
        const response = await api.get('/scooters')
        const scooters = response.data
        
        // 根据滑板车数据生成点位信息
        const locationsMap = new Map()
        
        scooters.forEach(scooter => {
          // 只有当locationId为null或undefined时才使用默认值1
          const locationId = (scooter.locationId === null || scooter.locationId === undefined) ? 1 : scooter.locationId
          const locationName = scooter.locationName || `点位${locationId}`
          const lat = scooter.latitude || locationCoordinates[locationId]?.lat || 39.9042
          const lng = scooter.longitude || locationCoordinates[locationId]?.lng || 116.4074
          // 地址显示为经纬度格式
          const address = `${lat.toFixed(6)}, ${lng.toFixed(6)}`
          
          if (!locationsMap.has(locationId)) {
            locationsMap.set(locationId, {
              id: locationId,
              name: locationName,
              address: address,
              lng: lng,
              lat: lat,
              availableCount: 0,
              bookedCount: 0,
              totalCount: 0
            })
          }
          
          const location = locationsMap.get(locationId)
          // 正确累加每个型号的车辆数量，而不是每个型号只算作1辆
          location.totalCount += scooter.totalQuantity || 0
          
          // 可用数量直接使用availableQuantity
          location.availableCount += scooter.availableQuantity || 0
          
          // 已预订数量 = 总数量 - 可用数量
          location.bookedCount += (scooter.totalQuantity || 0) - (scooter.availableQuantity || 0)
        })
        
        let locations = Array.from(locationsMap.values())
        
        console.log('从后端获取的滑板车数据:', scooters)
        console.log('计算后的点位数据:', locations)
        
        // 确保至少有10个点位，如果不足则补充
        const existingLocationIds = new Set(locations.map(loc => loc.id))
        
        for (let i = 1; i <= 10; i++) {
          if (!existingLocationIds.has(i)) {
            const lat = locationCoordinates[i]?.lat || 39.9042
            const lng = locationCoordinates[i]?.lng || 116.4074
            const defaultLocation = {
              id: i,
              name: getDefaultLocationName(i),
              address: `${lat.toFixed(6)}, ${lng.toFixed(6)}`,
              lng: lng,
              lat: lat,
              availableCount: 0,
              bookedCount: 0,
              totalCount: 0
            }
            locations.push(defaultLocation)
          }
        }
        
        // 按ID排序
        locations.sort((a, b) => a.id - b.id)
        
        return locations
      } catch (error) {
        console.error('加载滑板车数据失败:', error)
        // 网络错误时，返回空点位数据
        const emptyLocations = []
        for (let i = 1; i <= 5; i++) {
          emptyLocations.push({
            id: i,
            name: getDefaultLocationName(i),
            address: getDefaultLocationAddress(i),
            lng: locationCoordinates[i]?.lng || 116.4074,
            lat: locationCoordinates[i]?.lat || 39.9042,
            availableCount: 0,
            bookedCount: 0,
            totalCount: 0
          })
        }
        return emptyLocations
      }
    }
    
    // 初始化地图
    const initMap = async () => {
      try {
        // 加载滑板车数据
        const locations = await loadScootersData()
        
        // 使用Leaflet地图库（免费、无需API密钥）
        if (!window.L) {
          const script = document.createElement('script')
          script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'
          const link = document.createElement('link')
          link.rel = 'stylesheet'
          link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'
          
          script.onload = () => {
            document.head.appendChild(link)
            createLeafletMap(locations)
          }
          document.head.appendChild(script)
        } else {
          createLeafletMap(locations)
        }
        
      } catch (error) {
        console.error('地图初始化失败:', error)
        ElMessage.error('地图加载失败，请刷新页面重试')
      }
    }
    
    // 创建Leaflet地图实例
    const createLeafletMap = (locations) => {
      try {
        // 确保地图容器存在
        const mapContainer = document.getElementById('map-container')
        if (!mapContainer) {
          console.error('地图容器未找到')
          createStaticMap(locations)
          return
        }
        
        // 创建地图实例
        map.value = L.map('map-container').setView([39.9042, 116.4074], 13)
        
        // 添加地图瓦片图层（使用多个备用服务器）
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
        
        // 尝试添加第一个可用的瓦片图层
         let tileLayerAdded = false
         for (const layerConfig of tileLayers) {
           try {
             const layer = L.tileLayer(layerConfig.url, {
               attribution: layerConfig.attribution,
               subdomains: layerConfig.subdomains || ['a', 'b', 'c'],
               errorTileUrl: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjU2IiBoZWlnaHQ9IjI1NiIgdmlld0JveD0iMCAwIDI1NiAyNTYiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIyNTYiIGhlaWdodD0iMjU2IiBmaWxsPSIjRjBGMEYwIi8+Cjx0ZXh0IHg9IjEyOCIgeT0iMTI4IiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iMTIiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGZpbGw9IiM2NjYiPk1hcCBUaWxlPC90ZXh0Pgo8L3N2Zz4K'
             })
             
             // 监听瓦片加载错误
             layer.on('tileerror', function(error) {
               console.warn('瓦片加载错误:', error)
             })
             
             layer.addTo(map.value)
             tileLayerAdded = true
             console.log(`成功加载瓦片图层: ${layerConfig.url}`)
             break
           } catch (error) {
             console.warn(`瓦片图层 ${layerConfig.url} 加载失败:`, error)
             continue
           }
         }
        
        // 如果所有瓦片图层都失败，使用离线模式
        if (!tileLayerAdded) {
          console.warn('所有在线瓦片图层加载失败，使用离线模式')
          mapContainer.style.background = 'var(--color-bg-secondary)'
          const offlineMsg = document.createElement('div')
          offlineMsg.style.cssText = 'position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);text-align:center;color:var(--color-text-secondary);'
          offlineMsg.innerHTML = '<h3>地图加载失败</h3><p>网络连接问题，显示离线模式</p>'
          mapContainer.appendChild(offlineMsg)
        }
        
        // 创建标记点
        markers.value = []
        locations.forEach(location => {
          // 根据可用数量设置标记颜色 - 使用CSS变量
          const colorClass = location.availableCount > 0 ? 'marker-available' : 'marker-unavailable'
          const iconHtml = `
            <div class="custom-marker ${colorClass}">
              <div class="marker-content">
                <span class="marker-text">${location.availableCount}</span>
              </div>
            </div>
          `
          
          const icon = L.divIcon({
            html: iconHtml,
            className: 'custom-leaflet-marker',
            iconSize: [30, 30],
            iconAnchor: [15, 15]
          })
          
          const marker = L.marker([location.lat, location.lng], { icon })
          
          // 添加点击事件
          marker.on('click', () => {
            showLocationInfo(location)
          })
          
          marker.addTo(map.value)
          markers.value.push(marker)
        })
        
        // 自动调整地图视野包含所有标记点
        if (locations.length > 0) {
          const group = new L.featureGroup(markers.value)
          map.value.fitBounds(group.getBounds().pad(0.1))
        }
        
      } catch (error) {
        console.error('Leaflet地图创建失败:', error)
        // 如果Leaflet也失败，使用简单的静态地图
        createStaticMap(locations)
      }
    }
    
    // 创建静态地图（备用方案）
    const createStaticMap = (locations) => {
      const mapContainer = document.getElementById('map-container')
      if (!mapContainer) {
        console.error('静态地图容器未找到')
        return
      }
      mapContainer.innerHTML = `
        <div class="static-map">
          <div class="map-background">
            <div class="map-overlay">
              <div class="map-title">滑板车点位分布图</div>
              <div class="map-grid">
                ${locations.map(location => `
                  <div class="static-marker" style="left: ${(location.lng - 116.35) * 100}%; top: ${(location.lat - 39.9) * 100}%">
                    <div class="marker-dot" style="background-color: ${location.availableCount > 0 ? '#67C23A' : '#F56C6C'}"></div>
                    <div class="marker-label">${location.name}</div>
                  </div>
                `).join('')}
              </div>
            </div>
          </div>
        </div>
      `
      
      // 添加静态地图的点击事件
      locations.forEach(location => {
        const markerElement = mapContainer.querySelector(`.static-marker:nth-child(${location.id})`)
        if (markerElement) {
          markerElement.addEventListener('click', () => {
            showLocationInfo(location)
          })
        }
      })
    }
    
    // 显示点位信息
    const showLocationInfo = (location) => {
      selectedLocation.value = location
      showLocationDialog.value = true
    }
    
    // 跳转到滑板车页面
    const goToScooters = () => {
      showLocationDialog.value = false
      ElMessage.success('正在跳转到滑板车列表...')
      // 延迟1.5秒后跳转到滑板车页面，给用户更好的体验
      setTimeout(() => {
        router.push('/scooters')
      }, 1500)
    }
    
    // 获取默认点位名称
    const getDefaultLocationName = (locationId) => {
      switch (locationId) {
        case 1: return '市中心广场'
        case 2: return '大学城校区'
        case 3: return '商业步行街'
        case 4: return '地铁站出口'
        case 5: return '公园入口'
        case 6: return '火车站北广场'
        case 7: return '科技园区'
        case 8: return '体育中心'
        case 9: return '购物中心'
        case 10: return '医院门口'
        default: return `点位${locationId}`
      }
    }
    
    // 获取默认点位地址
    const getDefaultLocationAddress = (locationId) => {
      switch (locationId) {
        case 1: return '北京市东城区王府井大街'
        case 2: return '北京市海淀区中关村大街'
        case 3: return '北京市西城区西单北大街'
        case 4: return '北京市朝阳区国贸地铁站'
        case 5: return '北京市海淀区颐和园东门'
        default: return `点位${locationId}地址`
      }
    }
    
    // 组件挂载时初始化地图
    onMounted(() => {
      initMap()
    })
    
    // 组件卸载时销毁地图
    onUnmounted(() => {
      if (map.value) {
        map.value.remove()
      }
    })
    
    return {
      showLocationDialog,
      selectedLocation,
      showLocationInfo,
      goToScooters
    }
  }
}
</script>

<style scoped>
.map-container {
  padding: 16px;
  height: calc(100vh - 80px);
  display: flex;
  flex-direction: column;
}

.page-header {
  margin-bottom: 16px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.page-subtitle {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin: 6px 0 0 0;
}

.amap-container {
  flex: 1;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  min-height: 400px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .map-container {
    padding: 12px;
    height: calc(100vh - 60px);
  }
  
  .page-header {
    margin-bottom: 12px;
  }
  
  .page-title {
    font-size: 18px;
  }
  
  .page-subtitle {
    font-size: 12px;
    margin-top: 4px;
  }
  
  .amap-container {
    min-height: 300px;
  }
}

@media (max-width: 480px) {
  .map-container {
    padding: 8px;
    height: calc(100vh - 50px);
  }
  
  .page-title {
    font-size: 16px;
  }
  
  .page-subtitle {
    font-size: 11px;
  }
  
  .amap-container {
    min-height: 250px;
  }
}

.location-info {
  padding: 10px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--color-border);
}

.info-label {
  font-weight: 500;
  color: var(--color-text-secondary);
}

.info-value {
  font-weight: 600;
}

.info-value.available {
  color: var(--color-success);
}

.info-value.booked {
  color: var(--color-warning);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 自定义标记点样式 */
/* 地图标记点样式 - 深色主题适配 */
:deep(.custom-marker) {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 3px solid var(--color-bg-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

:deep(.marker-available) {
  background: var(--color-success) !important;
}

:deep(.marker-unavailable) {
  background: var(--color-error) !important;
}

:deep(.custom-marker:hover) {
  transform: scale(1.2);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
}

:deep(.marker-content) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

:deep(.marker-text) {
  color: white;
  font-size: 12px;
  font-weight: 600;
}

/* 静态地图样式 */
.static-map {
  width: 100%;
  height: 100%;
  position: relative;
}

.map-background {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 100%);
  position: relative;
  border-radius: 8px;
}

.map-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20px;
}

.map-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  text-align: center;
}

.map-grid {
  position: relative;
  width: 100%;
  height: calc(100% - 60px);
}

.static-marker {
  position: absolute;
  transform: translate(-50%, -50%);
  cursor: pointer;
  text-align: center;
  transition: all 0.3s ease;
}

.static-marker:hover {
  transform: translate(-50%, -50%) scale(1.2);
}

.marker-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 3px solid white;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  margin: 0 auto 5px;
}

.marker-label {
  font-size: 12px;
  font-weight: 600;
  color: #303133;
  background: rgba(255, 255, 255, 0.9);
  padding: 2px 6px;
  border-radius: 4px;
  white-space: nowrap;
}

/* Leaflet地图适配 */
:deep(.leaflet-container) {
  border-radius: 8px;
}

:deep(.custom-leaflet-marker) {
  background: transparent !important;
  border: none !important;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .map-container {
    padding: 10px;
    height: 60vh;
    max-height: 400px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .amap-container {
    border-radius: 4px;
    min-height: 280px;
    max-height: 350px;
  }
  
  :deep(.custom-marker) {
    width: 25px;
    height: 25px;
  }
  
  :deep(.marker-text) {
    font-size: 10px;
  }
  
  .marker-dot {
    width: 16px;
    height: 16px;
  }
  
  .marker-label {
    font-size: 10px;
  }
}

@media (max-width: 480px) {
  .map-container {
    padding: 5px;
    height: 55vh;
    max-height: 350px;
  }
  
  .page-header {
    margin-bottom: 15px;
  }
  
  .page-title {
    font-size: 18px;
  }
  
  .page-subtitle {
    font-size: 12px;
  }
  
  .map-title {
    font-size: 16px;
    margin-bottom: 15px;
  }
  
  .amap-container {
    min-height: 250px;
    max-height: 300px;
  }
}
</style>