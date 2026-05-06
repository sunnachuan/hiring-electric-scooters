import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  THEMES,
  FONT_SIZES,
  getCurrentTheme,
  getCurrentFontSize,
  applyTheme,
  applyFontSize
} from '@/utils/theme'

export const useThemeStore = defineStore('theme', () => {
  // 状态
  const currentTheme = ref(getCurrentTheme())
  const currentFontSize = ref(getCurrentFontSize())
  
  // 计算属性
  const themeConfig = computed(() => THEMES[currentTheme.value])
  const fontSizeConfig = computed(() => FONT_SIZES[currentFontSize.value])
  const availableThemes = computed(() => Object.values(THEMES))
  const availableFontSizes = computed(() => Object.values(FONT_SIZES))
  
  // 动作
  const setTheme = (theme) => {
    // 检查主题是否存在
    const themeExists = Object.values(THEMES).some(t => t.value === theme)
    if (themeExists) {
      currentTheme.value = theme
      applyTheme(theme)
    }
  }
  
  const setFontSize = (fontSize) => {
    if (FONT_SIZES[fontSize]) {
      currentFontSize.value = fontSize
      applyFontSize(fontSize)
    }
  }
  
  const resetSettings = () => {
    setTheme('light')
    setFontSize('normal')
  }
  
  // 初始化
  const initialize = () => {
    // 应用初始设置
    applyTheme(currentTheme.value)
    applyFontSize(currentFontSize.value)
  }
  
  return {
    // 状态
    currentTheme,
    currentFontSize,
    
    // 计算属性
    themeConfig,
    fontSizeConfig,
    availableThemes,
    availableFontSizes,
    
    // 动作
    setTheme,
    setFontSize,
    resetSettings,
    initialize
  }
})