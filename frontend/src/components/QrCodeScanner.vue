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
      <span class="scanner-title">扫码</span>
      <div class="placeholder"></div>
    </div>
    
    <!-- 扫描区域 -->
    <div class="scanner-container">
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
      <p class="scan-hint">将二维码对准取景框</p>
    </div>
    
    <!-- 底部提示 -->
    <div class="scanner-footer">
      <p>支持扫描滑板车二维码</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'scan-success'])

const isScanning = ref(false)

// 自动开始扫描
const startScan = () => {
  isScanning.value = true
}

// 停止扫描
const stopScan = () => {
  isScanning.value = false
}

// 关闭扫描器
const handleClose = () => {
  stopScan()
  emit('close')
}

// 监听 visible 变化，自动开始/停止扫描
watch(() => props.visible, (newVal) => {
  if (newVal) {
    // 延迟一点开始扫描，让界面渲染完成
    setTimeout(startScan, 300)
  } else {
    stopScan()
  }
})

// 生命周期
onMounted(() => {
  if (props.visible) {
    setTimeout(startScan, 300)
  }
})

onUnmounted(() => {
  stopScan()
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
}

.close-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  border: none;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: scale(1.05);
}

.icon {
  width: 24px;
  height: 24px;
}

.scanner-title {
  color: white;
  font-size: 18px;
  font-weight: 500;
}

.placeholder {
  width: 44px;
}

.scanner-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.scan-frame {
  position: relative;
  width: 280px;
  height: 280px;
  border-radius: 20px;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.6);
  box-shadow: 0 0 40px rgba(16, 185, 129, 0.2);
}

.corner {
  position: absolute;
  width: 28px;
  height: 28px;
  border-color: #10B981;
  border-style: solid;
  border-width: 4px;
  border-radius: 4px;
}

.corner.top-left {
  top: 8px;
  left: 8px;
  border-right-width: 0;
  border-bottom-width: 0;
}

.corner.top-right {
  top: 8px;
  right: 8px;
  border-left-width: 0;
  border-bottom-width: 0;
}

.corner.bottom-left {
  bottom: 8px;
  left: 8px;
  border-right-width: 0;
  border-top-width: 0;
}

.corner.bottom-right {
  bottom: 8px;
  right: 8px;
  border-left-width: 0;
  border-top-width: 0;
}

.scan-line {
  position: absolute;
  left: 10px;
  right: 10px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #10B981, transparent);
  box-shadow: 0 0 15px #10B981, 0 0 30px rgba(16, 185, 129, 0.5);
  top: -2px;
  opacity: 0;
}

.scan-line.active {
  animation: scanMove 2s linear infinite;
  opacity: 1;
}

@keyframes scanMove {
  0% {
    top: 10px;
  }
  100% {
    top: calc(100% - 10px);
  }
}

.scan-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(16, 185, 129, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(16, 185, 129, 0.03) 1px, transparent 1px);
  background-size: 20px 20px;
}

.scan-hint {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  margin: 24px 0;
}

.scanner-footer {
  padding: 16px;
  text-align: center;
  background: rgba(0, 0, 0, 0.3);
}

.scanner-footer p {
  margin: 0;
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
}
</style>