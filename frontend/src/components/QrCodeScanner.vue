<template>
  <div v-if="visible" class="scanner-overlay" @click.self="handleClose">
    <!-- 顶部工具栏 -->
    <div class="scanner-header">
      <button class="close-btn" @click="handleClose">
        <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18"></line>
          <line x1="6" y1="6" x2="18" y2="18"></line>
        </svg>
      </button>
      <span class="scanner-title">扫一扫</span>
      <div class="placeholder"></div>
    </div>
    
    <!-- 扫描区域 -->
    <div class="scanner-container">
      <!-- 视频元素 -->
      <video 
        ref="videoRef" 
        class="scanner-video" 
        playsinline
        autoplay
        muted
      ></video>
      
      <!-- 隐藏的 canvas 用于二维码识别 -->
      <canvas ref="canvasRef" style="display: none;"></canvas>
      
      <!-- 扫描框 -->
      <div class="scan-frame">
        <!-- 四角装饰 -->
        <div class="corner top-left"></div>
        <div class="corner top-right"></div>
        <div class="corner bottom-left"></div>
        <div class="corner bottom-right"></div>
        
        <!-- 扫描线动画 -->
        <div class="scan-line" :class="{ active: isScanning }"></div>
        
        <!-- 扫描网格 -->
        <div class="scan-grid"></div>
      </div>
      
      <!-- 提示文字 -->
      <p class="scan-hint">{{ hintText }}</p>
    </div>
    
    <!-- 底部提示 -->
    <div class="scanner-footer">
      <p>支持扫描滑板车二维码</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import jsQR from 'jsqr'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'scan-success', 'error'])

const videoRef = ref(null)
const canvasRef = ref(null)
const isScanning = ref(false)
const streamRef = ref(null)
const animationFrameRef = ref(null)
const hintText = ref('正在打开摄像头...')

console.log('=== QrCodeScanner 组件加载 ===')
console.log('协议:', window.location.protocol)
console.log('主机:', window.location.hostname)
console.log('isSecureContext:', window.isSecureContext)
console.log('mediaDevices:', !!navigator.mediaDevices)

const startCamera = async () => {
  console.log('--- startCamera 开始 ---')
  hintText.value = '正在打开摄像头...'

  if (!window.isSecureContext) {
    console.error('当前不是安全上下文，摄像头不可用')
    hintText.value = '摄像头需要 HTTPS 连接，请使用 https:// 地址访问'
    emit('error', '非安全上下文：摄像头需要 HTTPS')
    return
  }

  if (!navigator.mediaDevices) {
    console.error('navigator.mediaDevices 不存在')
    hintText.value = '当前浏览器不支持摄像头 API'
    emit('error', '浏览器不支持 mediaDevices')
    return
  }

  if (!navigator.mediaDevices.getUserMedia) {
    console.error('getUserMedia 不存在')
    hintText.value = '当前浏览器不支持摄像头调用'
    emit('error', '浏览器不支持 getUserMedia')
    return
  }

  try {
    console.log('请求摄像头权限...')
    const stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'environment' }
    })
    console.log('权限获取成功')
    streamRef.value = stream

    await nextTick()

    if (videoRef.value) {
      videoRef.value.srcObject = stream
      console.log('视频流已绑定')
      hintText.value = '将二维码对准取景框'
      startScanning()
    }
  } catch (error) {
    console.error('摄像头失败:', error.name, error.message)
    if (error.name === 'NotAllowedError') {
      hintText.value = '摄像头权限被拒绝，请在浏览器设置中允许'
    } else if (error.name === 'NotFoundError') {
      hintText.value = '未检测到摄像头设备'
    } else if (error.name === 'NotReadableError') {
      hintText.value = '摄像头被其他应用占用'
    } else if (error.name === 'OverconstrainedError') {
      hintText.value = '摄像头不支持所需分辨率'
    } else {
      hintText.value = '摄像头无法使用，请检查连接是否为 HTTPS'
    }
    emit('error', error)
  }
}

const stopCamera = () => {
  if (streamRef.value) {
    streamRef.value.getTracks().forEach(track => track.stop())
    streamRef.value = null
  }
}

const startScanning = () => {
  isScanning.value = true
  scanQRCode()
}

const stopScanning = () => {
  isScanning.value = false
  if (animationFrameRef.value) {
    cancelAnimationFrame(animationFrameRef.value)
    animationFrameRef.value = null
  }
}

const scanQRCode = () => {
  if (!isScanning.value) return
  if (!videoRef.value || !canvasRef.value) {
    animationFrameRef.value = requestAnimationFrame(scanQRCode)
    return
  }
  
  const video = videoRef.value
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')
  
  if (video.readyState === video.HAVE_ENOUGH_DATA) {
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
    
    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
    const code = jsQR(imageData.data, imageData.width, imageData.height)
    
    if (code && code.data && code.data.trim().length >= 3) {
      handleScanSuccess(code.data)
      return
    }
  }
  
  animationFrameRef.value = requestAnimationFrame(scanQRCode)
}

const handleScanSuccess = (data) => {
  stopScanning()
  emit('scan-success', data)
  
  if (navigator.vibrate) {
    navigator.vibrate(100)
  }
  
  setTimeout(() => {
    handleClose()
  }, 300)
}

const handleClose = () => {
  stopScanning()
  stopCamera()
  emit('close')
}

watch(() => props.visible, async (newVal) => {
  if (newVal) {
    await nextTick()
    await startCamera()
  } else {
    handleClose()
  }
})

onMounted(() => {
  if (props.visible) {
    startCamera()
  }
})

onUnmounted(() => {
  handleClose()
})
</script>

<style scoped>
.scanner-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #000;
  z-index: 2000;
  display: flex;
  flex-direction: column;
}

.scanner-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: rgba(0, 0, 0, 0.5);
  position: relative;
  z-index: 10;
}

.close-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}

.icon {
  width: 24px;
  height: 24px;
}

.scanner-title {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.placeholder {
  width: 44px;
}

.scanner-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.scanner-video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.scan-frame {
  width: 250px;
  height: 250px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  position: relative;
  background: rgba(0, 0, 0, 0.1);
}

.corner {
  position: absolute;
  width: 20px;
  height: 20px;
  border: 3px solid #00d4aa;
}

.corner.top-left {
  top: -3px;
  left: -3px;
  border-right: none;
  border-bottom: none;
}

.corner.top-right {
  top: -3px;
  right: -3px;
  border-left: none;
  border-bottom: none;
}

.corner.bottom-left {
  bottom: -3px;
  left: -3px;
  border-right: none;
  border-top: none;
}

.corner.bottom-right {
  bottom: -3px;
  right: -3px;
  border-left: none;
  border-top: none;
}

.scan-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, #00d4aa, transparent);
  top: 50%;
  opacity: 0;
  transition: opacity 0.3s;
}

.scan-line.active {
  opacity: 1;
  animation: scan 2s linear infinite;
}

@keyframes scan {
  0% { top: 0; }
  100% { top: 100%; }
}

.scan-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(0, 212, 170, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 212, 170, 0.1) 1px, transparent 1px);
  background-size: 20px 20px;
  pointer-events: none;
}

.scan-hint {
  position: absolute;
  bottom: 60px;
  left: 0;
  right: 0;
  text-align: center;
  color: white;
  font-size: 14px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}

.scanner-footer {
  padding: 20px;
  background: rgba(0, 0, 0, 0.5);
  text-align: center;
}

.scanner-footer p {
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  margin: 0;
}
</style>
