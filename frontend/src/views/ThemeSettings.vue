<template>
  <div class="theme-settings">
    <div class="settings-header">
      <div class="back-btn-container">
        <el-button @click="$router.back()" type="primary" size="large" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回个人中心
        </el-button>
      </div>
      <h1>主题与显示设置</h1>
      <p class="subtitle">自定义界面外观，提升使用体验</p>
    </div>

    <div class="settings-content">
      <!-- 主题设置 -->
      <el-card class="setting-section" shadow="never">
        <template #header>
          <div class="section-header">
            <el-icon><Brush /></el-icon>
            <span>主题设置</span>
          </div>
        </template>

        <div class="theme-options">
          <div 
            v-for="theme in availableThemes" 
            :key="theme.value"
            class="theme-option"
            :class="{ active: currentTheme === theme.value }"
            @click="setTheme(theme.value)"
          >
            <div class="theme-preview" :class="theme.value">
              <div class="preview-header"></div>
              <div class="preview-content">
                <div class="preview-text"></div>
                <div class="preview-text short"></div>
              </div>
            </div>
            <div class="theme-info">
              <h4>{{ theme.name }}</h4>
              <p>{{ theme.description }}</p>
            </div>
            <div class="theme-check" v-if="currentTheme === theme.value">
              <el-icon><Check /></el-icon>
            </div>
          </div>
        </div>

        <div class="system-theme-option">
          <el-switch
            v-model="followSystem"
            @change="toggleFollowSystem"
            active-text="跟随系统主题"
            inactive-text="自定义主题"
          />
          <span class="help-text">开启后，界面将自动跟随系统深色/浅色模式</span>
        </div>
      </el-card>

      <!-- 字体大小设置 -->
      <el-card class="setting-section" shadow="never">
        <template #header>
          <div class="section-header">
            <el-icon><Brush /></el-icon>
            <span>字体大小</span>
          </div>
        </template>

        <div class="font-size-options">
          <div 
            v-for="fontSize in availableFontSizes" 
            :key="fontSize.value"
            class="font-size-option"
            :class="{ active: currentFontSize === fontSize.value }"
            @click="setFontSize(fontSize.value)"
          >
            <div class="font-size-preview" :style="{ fontSize: fontSize.scale * 16 + 'px' }">
              Aa
            </div>
            <div class="font-size-info">
              <h4>{{ fontSize.name }}</h4>
              <p>{{ fontSize.description }}</p>
            </div>
            <div class="font-size-check" v-if="currentFontSize === fontSize.value">
              <el-icon><Check /></el-icon>
            </div>
          </div>
        </div>


      </el-card>

      <!-- 预览区域 -->
      <el-card class="setting-section" shadow="never">
        <template #header>
          <div class="section-header">
            <el-icon><View /></el-icon>
            <span>实时预览</span>
          </div>
        </template>

        <div class="preview-area">
          <div class="preview-card">
            <div class="preview-header">
              <h3>设备监控面板</h3>
              <el-tag :type="getStatusTagType('online')">在线</el-tag>
            </div>
            <div class="preview-content">
              <div class="preview-item">
                <span class="label">设备ID:</span>
                <span class="value">S001</span>
              </div>
              <div class="preview-item">
                <span class="label">电量:</span>
                <span class="value">85%</span>
              </div>
              <div class="preview-item">
                <span class="label">位置:</span>
                <span class="value">市中心广场</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button @click="resetSettings" type="default" size="large">
          <el-icon><Refresh /></el-icon>
          恢复默认设置
        </el-button>
        <el-button @click="$router.back()" type="primary" size="large">
          <el-icon><Check /></el-icon>
          完成设置
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { ArrowLeft, Brush, View, Check, Refresh } from '@element-plus/icons-vue'

const themeStore = useThemeStore()

// 计算属性
const currentTheme = computed(() => themeStore.currentTheme)
const currentFontSize = computed(() => themeStore.currentFontSize)
const followSystem = computed(() => themeStore.followSystem)
const availableThemes = computed(() => themeStore.availableThemes)
const availableFontSizes = computed(() => themeStore.availableFontSizes)
const fontSizeConfig = computed(() => themeStore.fontSizeConfig)

// 方法
const setTheme = (theme) => {
  themeStore.setTheme(theme)
}

const setFontSize = (fontSize) => {
  themeStore.setFontSize(fontSize)
}

const toggleFollowSystem = () => {
  themeStore.toggleFollowSystem()
}

const resetSettings = () => {
  themeStore.resetSettings()
}

const getStatusTagType = (status) => {
  const types = {
    online: 'success',
    offline: 'info',
    unlocked: 'warning'
  }
  return types[status] || 'info'
}

onMounted(() => {
  themeStore.initialize()
})
</script>

<style scoped>
.theme-settings {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.settings-header {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
}

.back-btn-container {
  position: absolute;
  left: 0;
  top: 0;
}

.back-btn {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  font-size: 16px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 2px solid transparent;
}

/* 浅色模式按钮样式（默认） */
 .back-btn {
   background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
   border-color: #3b82f6;
   color: white;
 }
 
 .back-btn:hover {
   background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
   border-color: #2563eb;
   transform: translateY(-2px);
   box-shadow: 0 6px 20px rgba(59, 130, 246, 0.3);
 }
 
 /* 深色模式按钮样式 */
 html[data-theme="dark"] .back-btn {
   background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
   border-color: #6366f1;
   color: white;
 }
 
 html[data-theme="dark"] .back-btn:hover {
   background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
   border-color: #4f46e5;
   transform: translateY(-2px);
   box-shadow: 0 6px 20px rgba(99, 102, 241, 0.4);
 }
 
 /* 高对比度模式按钮样式 */
 html[data-theme="high-contrast"] .back-btn {
   background: linear-gradient(135deg, #ffff00 0%, #ffd700 100%);
   border-color: #ffff00;
   color: #000000;
   font-weight: 700;
 }
 
 html[data-theme="high-contrast"] .back-btn:hover {
   background: linear-gradient(135deg, #ffd700 0%, #ffa500 100%);
   border-color: #ffd700;
   transform: translateY(-2px);
   box-shadow: 0 6px 20px rgba(255, 255, 0, 0.5);
 }

.settings-header h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.subtitle {
  color: var(--color-text-secondary);
  margin: 0;
}

.settings-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.setting-section {
  border: 1px solid var(--color-border);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

/* 主题选项样式 */
.theme-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.theme-option {
  border: 2px solid var(--color-border);
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.theme-option:hover {
  border-color: var(--color-primary);
  transform: translateY(-2px);
}

.theme-option.active {
  border-color: var(--color-primary);
  background-color: var(--color-bg-tertiary);
}

.theme-preview {
  width: 100%;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.theme-preview.light {
  background: linear-gradient(135deg, var(--color-bg-primary) 0%, var(--color-bg-secondary) 100%);
}

.theme-preview.dark {
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
}

.theme-preview.high-contrast {
  background: linear-gradient(135deg, #000000 0%, #111111 100%);
}

.preview-header {
  height: 20px;
  background-color: var(--color-primary);
}

.preview-content {
  flex: 1;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.preview-text {
  height: 6px;
  background-color: var(--color-text-secondary);
  border-radius: 2px;
}

.preview-text.short {
  width: 70%;
}

.theme-info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
}

.theme-info p {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.theme-check {
  position: absolute;
  top: 12px;
  right: 12px;
  color: var(--color-primary);
}

.system-theme-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-top: 1px solid var(--color-border);
}

.help-text {
  font-size: 14px;
  color: var(--color-text-secondary);
}

/* 字体大小选项样式 */
.font-size-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.font-size-option {
  border: 2px solid var(--color-border);
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
}

.font-size-option:hover {
  border-color: var(--color-primary);
}

.font-size-option.active {
  border-color: var(--color-primary);
  background-color: var(--color-bg-tertiary);
}

.font-size-preview {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background-color: var(--color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.font-size-info h4 {
  margin: 0 0 4px 0;
}

.font-size-info p {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.font-size-check {
  position: absolute;
  top: 12px;
  right: 12px;
  color: var(--color-primary);
}

.preview-text p {
  margin: 0;
  line-height: 1.6;
}

/* 预览区域样式 */
.preview-area {
  display: flex;
  justify-content: center;
}

.preview-card {
  width: 300px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background-color: var(--color-bg-secondary);
  overflow: hidden;
}

.preview-header {
  padding: 16px;
  background-color: var(--color-bg-tertiary);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-header h3 {
  margin: 0;
  font-size: 16px;
}

.preview-content {
  padding: 16px;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.preview-item:last-child {
  margin-bottom: 0;
}

.preview-item .label {
  color: var(--color-text-secondary);
}

.preview-item .value {
  font-weight: 500;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  padding-top: 20px;
  border-top: 1px solid var(--color-border);
}

@media (max-width: 768px) {
  .theme-settings {
    padding: 16px;
  }
  
  .theme-options {
    grid-template-columns: 1fr;
  }
  
  .font-size-options {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>