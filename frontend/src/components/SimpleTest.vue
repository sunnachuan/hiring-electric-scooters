<template>
  <div style="padding: 20px;">
    <h2>摄像头测试</h2>
    <button @click="startTest" style="padding: 10px 20px; font-size: 16px;">启动摄像头</button>
    <p style="margin-top: 20px;">状态: {{ status }}</p>
    <p>协议: {{ protocol }}</p>
    <p>主机: {{ hostname }}</p>
    <video ref="videoRef" style="width: 100%; max-width: 400px; margin-top: 20px; border: 1px solid #ccc;" playsinline></video>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const videoRef = ref(null)
const status = ref('等待测试')
const protocol = ref(window.location.protocol)
const hostname = ref(window.location.hostname)

const startTest = async () => {
  console.log('=== 简单测试 ===')
  console.log('navigator.mediaDevices:', !!navigator.mediaDevices)
  console.log('getUserMedia:', !!(navigator.mediaDevices && navigator.mediaDevices.getUserMedia))
  status.value = '正在启动...'
  
  try {
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      status.value = '不支持 API'
      return
    }
    
    const stream = await navigator.mediaDevices.getUserMedia({ video: true })
    console.log('成功获取 stream')
    status.value = '成功！'
    
    if (videoRef.value) {
      videoRef.value.srcObject = stream
      videoRef.value.onloadedmetadata = () => {
        videoRef.value.play()
      }
    }
  } catch (e) {
    console.error('错误:', e)
    status.value = '错误: ' + e.name
  }
}
</script>
