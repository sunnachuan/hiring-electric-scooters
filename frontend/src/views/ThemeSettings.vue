<template>
  <div class="theme-settings">
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button @click="$router.back()" type="primary" size="large">
        <el-icon><ArrowLeft /></el-icon>
        返回个人中心
      </el-button>
    </div>

    <!-- 页面标题 -->
    <div class="page-header">
      <h1>主题与显示设置</h1>
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
  max-width: 50rem;
  margin: 0 auto;
  padding: 1.25rem;
}

.back-button {
  margin-bottom: 1.5rem;
}

.page-header {
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--color-border);
}

.page-header h1 {
  margin: 0 0 0.5rem 0;
  color: var(--color-text-primary);
  font-size: 1.75rem;
  font-weight: 600;
}

.subtitle {
  color: var(--color-text-secondary);
  margin: 0;
}

.settings-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.setting-section {
  border: 1px solid var(--color-border);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.125rem;
  font-weight: 600;
}

/* 主题选项样式 */
.theme-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(12.5rem, 1fr));
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.theme-option {
  border: 2px solid var(--color-border);
  border-radius: 0.5rem;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
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
  height: 5rem;
  border-radius: 0.375rem;
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
  height: 1.25rem;
  background-color: var(--color-primary);
}

.preview-content {
  flex: 1;
  padding: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.preview-text {
  height: 0.375rem;
  background-color: var(--color-text-secondary);
  border-radius: 0.125rem;
}

.preview-text.short {
  width: 70%;
}

.theme-info h4 {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
}

.theme-info p {
  margin: 0;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}

.theme-check {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  color: var(--color-primary);
}

.system-theme-option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 0;
  border-top: 1px solid var(--color-border);
}

.help-text {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}

/* 字体大小选项样式 */
.font-size-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(9.375rem, 1fr));
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.font-size-option {
  border: 2px solid var(--color-border);
  border-radius: 0.5rem;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.font-size-option:hover {
  border-color: var(--color-primary);
}

.font-size-option.active {
  border-color: var(--color-primary);
  background-color: var(--color-bg-tertiary);
}

.font-size-preview {
  width: 3rem;
  height: 3rem;
  border-radius: 0.5rem;
  background-color: var(--color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.font-size-info h4 {
  margin: 0 0 0.25rem 0;
}

.font-size-info p {
  margin: 0;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}

.font-size-check {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
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
  width: 18.75rem;
  border: 1px solid var(--color-border);
  border-radius: 0.5rem;
  background-color: var(--color-bg-secondary);
  overflow: hidden;
}

.preview-header {
  padding: 1rem;
  background-color: var(--color-bg-tertiary);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-header h3 {
  margin: 0;
  font-size: 1rem;
}

.preview-content {
  padding: 1rem;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
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
  gap: 0.75rem;
  justify-content: center;
  padding-top: 1.25rem;
  border-top: 1px solid var(--color-border);
}

@media (max-width: 768px) {
  .theme-settings {
    padding: 1rem;
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