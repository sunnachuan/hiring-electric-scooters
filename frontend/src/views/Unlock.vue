<template>
  <div class="unlock-container">
    <div class="unlock-header">
      <h1 class="page-title">扫码解锁滑板车</h1>
      <p class="page-subtitle">扫描二维码快速解锁您的滑板车</p>
    </div>

    <div class="unlock-content">
      <!-- 扫码区域 -->
      <el-card class="scan-section" shadow="hover">
        <template #header>
          <div class="section-header">
            <h3>扫描二维码</h3>
            <el-button size="small" @click="toggleCamera">
              <el-icon><Camera /></el-icon>
              {{ isCameraActive ? '关闭摄像头' : '开启摄像头' }}
            </el-button>
          </div>
        </template>
        
        <div class="scan-area">
          <!-- 摄像头区域 -->
          <div v-if="isCameraActive" class="camera-container">
            <div class="camera-frame">
              <div class="scan-overlay">
                <div class="scan-frame">
                  <div class="scan-line"></div>
                </div>
                <div class="scan-tip">将二维码对准框内</div>
              </div>
              <!-- 模拟摄像头画面 -->
              <div class="mock-camera">
                <div class="qr-code-preview">
                  <div class="qr-code">
                    <div class="qr-pattern"></div>
                    <div class="qr-text">SCOOTER_123456</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 手动输入区域 -->
          <div v-else class="manual-input">
            <div class="input-group">
              <el-input
                v-model="manualQRCode"
                placeholder="请输入二维码内容"
                size="large"
                @keyup.enter="handleManualInput"
              >
                <template #prepend>
                  <el-icon><Scan /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" size="large" @click="handleManualInput">
                确认
              </el-button>
            </div>
            <div class="input-tip">
              <p>或者使用手机相机扫描二维码，然后粘贴内容</p>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 解锁状态区域 -->
      <el-card class="status-section" shadow="hover">
        <template #header>
          <h3>解锁状态</h3>
        </template>
        
        <div class="status-content">
          <div v-if="currentScooter" class="scooter-info">
            <div class="scooter-header">
              <div class="scooter-icon">
                <el-icon><Bicycle /></el-icon>
              </div>
              <div class="scooter-details">
                <h4>{{ currentScooter.model }}</h4>
                <p>设备ID: #{{ currentScooter.id }}</p>
              </div>
            </div>
            
            <div class="status-indicators">
              <div class="status-item">
                <div class="status-label">电量状态</div>
                <div class="battery-display">
                  <div class="battery-bar">
                    <div 
                      class="battery-fill" 
                      :class="getBatteryClass(currentScooter.batteryLevel)"
                      :style="{ width: currentScooter.batteryLevel + '%' }"
                    ></div>
                  </div>
                  <span class="battery-text">{{ currentScooter.batteryLevel }}%</span>
                </div>
              </div>
              
              <div class="status-item">
                <div class="status-label">锁定状态</div>
                <el-tag :type="currentScooter.isLocked ? 'warning' : 'success'" size="large">
                  <el-icon>
                    <component :is="currentScooter.isLocked ? 'Lock' : 'Unlock'" />
                  </el-icon>
                  {{ currentScooter.isLocked ? '已锁定' : '已解锁' }}
                </el-tag>
              </div>
              
              <div class="status-item">
                <div class="status-label">在线状态</div>
                <el-tag :type="currentScooter.isOnline ? 'success' : 'info'" size="large">
                  <el-icon><Monitor /></el-icon>
                  {{ currentScooter.isOnline ? '在线' : '离线' }}
                </el-tag>
              </div>
            </div>
            
            <div class="action-buttons">
              <el-button 
                v-if="currentScooter.isLocked"
                type="primary" 
                size="large" 
                @click="unlockScooter"
                :loading="unlockLoading"
                class="action-btn"
              >
                <el-icon><Unlock /></el-icon>
                解锁滑板车
              </el-button>
              
              <el-button 
                v-else
                type="warning" 
                size="large" 
                @click="lockScooter"
                :loading="lockLoading"
                class="action-btn"
              >
                <el-icon><Lock /></el-icon>
                锁定滑板车
              </el-button>
              
              <el-button 
                size="large" 
                @click="resetScanner"
                class="action-btn"
              >
                <el-icon><Refresh /></el-icon>
                重新扫描
              </el-button>
            </div>
          </div>
          
          <div v-else class="empty-state">
            <div class="empty-icon">
              <el-icon><Scan /></el-icon>
            </div>
            <h4>等待扫描二维码</h4>
            <p>请使用摄像头扫描滑板车上的二维码，或手动输入二维码内容</p>
          </div>
        </div>
      </el-card>

      <!-- 使用说明 -->
      <el-card class="instructions-section" shadow="hover">
        <template #header>
          <h3>使用说明</h3>
        </template>
        
        <div class="instructions-content">
          <el-steps :active="3" align-center>
            <el-step title="扫描二维码" description="使用摄像头扫描滑板车上的二维码" />
            <el-step title="验证设备" description="系统自动验证设备状态和电量" />
            <el-step title="解锁使用" description="点击解锁按钮开始使用滑板车" />
          </el-steps>
          
          <div class="safety-tips">
            <h4>安全提示</h4>
            <ul>
              <li>确保滑板车电量充足（建议不低于20%）</li>
              <li>检查滑板车外观是否完好</li>
              <li>骑行前请佩戴安全护具</li>
              <li>遵守交通规则，注意行车安全</li>
              <li>还车时请将滑板车停放在指定区域</li>
            </ul>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 解锁确认弹窗 -->
    <el-dialog v-model="showUnlockConfirm" title="确认解锁" width="400px" center>
      <div class="confirm-content">
        <div class="confirm-icon">
          <el-icon><Unlock /></el-icon>
        </div>
        <h4>确认解锁滑板车？</h4>
        <p>设备: {{ currentScooter?.model }} (#{{ currentScooter?.id }})</p>
        <p>电量: {{ currentScooter?.batteryLevel }}%</p>
        <p>解锁后将开始计费</p>
      </div>
      
      <template #footer>
        <el-button @click="showUnlockConfirm = false">取消</el-button>
        <el-button type="primary" @click="confirmUnlock">确认解锁</el-button>
      </template>
    </el-dialog>

    <!-- 锁定确认弹窗 -->
    <el-dialog v-model="showLockConfirm" title="确认锁定" width="400px" center>
      <div class="confirm-content">
        <div class="confirm-icon">
          <el-icon><Lock /></el-icon>
        </div>
        <h4>确认锁定滑板车？</h4>
        <p>设备: {{ currentScooter?.model }} (#{{ currentScooter?.id }})</p>
        <p>锁定后将停止计费</p>
        <p>请确保滑板车已停放在安全位置</p>
      </div>
      
      <template #footer>
        <el-button @click="showLockConfirm = false">取消</el-button>
        <el-button type="primary" @click="confirmLock">确认锁定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { 
  Camera, Search, Bicycle, Unlock, Lock, Monitor, Refresh 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()

// 响应式数据
const isCameraActive = ref(false)
const manualQRCode = ref('')
const currentScooter = ref(null)
const unlockLoading = ref(false)
const lockLoading = ref(false)
const showUnlockConfirm = ref(false)
const showLockConfirm = ref(false)

// 切换摄像头
const toggleCamera = () => {
  isCameraActive.value = !isCameraActive.value
  
  if (isCameraActive.value) {
    // 模拟摄像头启动
    setTimeout(() => {
      // 模拟扫描到二维码
      simulateQRScan()
    }, 2000)
  }
}

// 模拟二维码扫描
const simulateQRScan = () => {
  const mockQRCode = 'SCOOTER_' + Math.floor(Math.random() * 1000000)
  handleQRCodeScan(mockQRCode)
}

// 处理手动输入
const handleManualInput = () => {
  if (!manualQRCode.value.trim()) {
    ElMessage.warning('请输入二维码内容')
    return
  }
  
  handleQRCodeScan(manualQRCode.value.trim())
}

// 处理二维码扫描
const handleQRCodeScan = async (qrCode) => {
  try {
    // 模拟API调用获取设备信息
    const scooter = await fetchScooterByQRCode(qrCode)
    currentScooter.value = scooter
    
    ElMessage.success(`成功识别设备: ${scooter.model}`)
    
    // 关闭摄像头
    isCameraActive.value = false
  } catch (error) {
    ElMessage.error('二维码识别失败，请重试')
    console.error('QR code scan error:', error)
  }
}

// 解锁滑板车
const unlockScooter = () => {
  if (!currentScooter.value) return
  
  // 检查设备状态
  if (!currentScooter.value.isOnline) {
    ElMessage.warning('设备离线，无法解锁')
    return
  }
  
  if (currentScooter.value.batteryLevel < 10) {
    ElMessage.warning('电量过低，无法解锁')
    return
  }
  
  showUnlockConfirm.value = true
}

// 确认解锁
const confirmUnlock = async () => {
  unlockLoading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 更新设备状态
    currentScooter.value.isLocked = false
    
    ElMessage.success('滑板车解锁成功！')
    showUnlockConfirm.value = false
  } catch (error) {
    ElMessage.error('解锁失败，请重试')
  } finally {
    unlockLoading.value = false
  }
}

// 锁定滑板车
const lockScooter = () => {
  showLockConfirm.value = true
}

// 确认锁定
const confirmLock = async () => {
  lockLoading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 更新设备状态
    currentScooter.value.isLocked = true
    
    ElMessage.success('滑板车锁定成功！')
    showLockConfirm.value = false
  } catch (error) {
    ElMessage.error('锁定失败，请重试')
  } finally {
    lockLoading.value = false
  }
}

// 重置扫描器
const resetScanner = () => {
  currentScooter.value = null
  manualQRCode.value = ''
  isCameraActive.value = false
}

// 获取电量样式类
const getBatteryClass = (level) => {
  if (level >= 50) return 'high'
  if (level >= 20) return 'medium'
  return 'low'
}

// 模拟API调用
const fetchScooterByQRCode = async (qrCode) => {
  // 模拟网络请求
  await new Promise(resolve => setTimeout(resolve, 500))
  
  // 返回模拟数据
  return {
    id: Math.floor(Math.random() * 100) + 1,
    model: `Scooter ${Math.floor(Math.random() * 5) + 1}`,
    batteryLevel: Math.floor(Math.random() * 100),
    isLocked: true,
    isOnline: Math.random() > 0.2,
    qrCode: qrCode
  }
}

// 生命周期
onMounted(() => {
  // 检查是否有路由传来的二维码参数
  if (route.params.qrCode) {
    handleQRCodeScan(route.params.qrCode)
  } else {
    ElMessage.info('请扫描滑板车上的二维码开始使用')
  }
  
  // 添加键盘事件监听
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  // 清理资源
  isCameraActive.value = false
  document.removeEventListener('keydown', handleKeydown)
})

const handleKeydown = (event) => {
  if (event.key === 'Enter') {
    if (showUnlockConfirm.value) {
      confirmUnlock()
    } else if (showLockConfirm.value) {
      confirmLock()
    }
  }
}
</script>

<style scoped>
.unlock-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.unlock-header {
  text-align: center;
  margin-bottom: 30px;
  color: white;
}

.page-title {
  font-size: 32px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.unlock-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.scan-section, .status-section, .instructions-section {
  background: white;
  border-radius: 12px;
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

.scan-area {
  padding: 20px 0;
}

.camera-container {
  position: relative;
  max-width: 400px;
  margin: 0 auto;
}

.camera-frame {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.scan-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.scan-frame {
  width: 200px;
  height: 200px;
  border: 2px solid #409eff;
  border-radius: 8px;
  position: relative;
  overflow: hidden;
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #409eff;
  animation: scan 2s linear infinite;
}

.scan-tip {
  margin-top: 20px;
  color: white;
  font-size: 14px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.mock-camera {
  width: 100%;
  height: 300px;
  background: #1a1a1a;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qr-code-preview {
  padding: 20px;
  background: white;
  border-radius: 8px;
}

.qr-code {
  text-align: center;
}

.qr-pattern {
  width: 120px;
  height: 120px;
  background: 
    linear-gradient(45deg, #000 25%, transparent 25%), 
    linear-gradient(-45deg, #000 25%, transparent 25%), 
    linear-gradient(45deg, transparent 75%, #000 75%), 
    linear-gradient(-45deg, transparent 75%, #000 75%);
  background-size: 20px 20px;
  background-position: 0 0, 0 10px, 10px -10px, -10px 0px;
  margin: 0 auto 10px;
}

.qr-text {
  font-size: 12px;
  color: #666;
  font-family: monospace;
}

.manual-input {
  text-align: center;
}

.input-group {
  display: flex;
  gap: 12px;
  max-width: 500px;
  margin: 0 auto 20px;
}

.input-tip {
  color: #666;
  font-size: 14px;
}

.status-content {
  padding: 20px 0;
}

.scooter-info {
  text-align: center;
}

.scooter-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 24px;
}

.scooter-icon {
  width: 60px;
  height: 60px;
  background: #409eff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.scooter-details h4 {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
}

.scooter-details p {
  margin: 0;
  color: #666;
}

.status-indicators {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.status-item {
  text-align: center;
}

.status-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.battery-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.battery-bar {
  width: 60px;
  height: 12px;
  background: #ebeef5;
  border-radius: 6px;
  overflow: hidden;
}

.battery-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.3s ease;
}

.battery-fill.high { background: #67c23a; }
.battery-fill.medium { background: #e6a23c; }
.battery-fill.low { background: #f56c6c; }

.battery-text {
  font-size: 14px;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.action-btn {
  min-width: 140px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
}

.empty-icon {
  font-size: 64px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.empty-state h4 {
  margin: 0 0 8px 0;
  color: #606266;
}

.empty-state p {
  margin: 0;
  color: #909399;
}

.instructions-content {
  padding: 20px 0;
}

.safety-tips {
  margin-top: 30px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.safety-tips h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.safety-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #606266;
}

.safety-tips li {
  margin-bottom: 4px;
}

.confirm-content {
  text-align: center;
  padding: 20px 0;
}

.confirm-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 16px;
}

.confirm-content h4 {
  margin: 0 0 8px 0;
  color: #303133;
}

.confirm-content p {
  margin: 0 0 4px 0;
  color: #606266;
}

/* 动画效果 */
@keyframes scan {
  0% { transform: translateY(0); }
  100% { transform: translateY(200px); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .unlock-container {
    padding: 12px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .status-indicators {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .input-group {
    flex-direction: column;
  }
  
  .scan-frame {
    width: 150px;
    height: 150px;
  }
}
</style>