import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  THEMES,
  FONT_SIZES,
  getCurrentTheme,
  getCurrentFontSize,
  applyTheme,
  applyFontSize,
  detectSystemTheme,
  watchSystemTheme
} from '@/utils/theme'

export const useThemeStore = defineStore('theme', () => {
  // 状态
  const currentTheme = ref(getCurrentTheme())
  const currentFontSize = ref(getCurrentFontSize())
  const followSystem = ref(false)
  
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
      followSystem.value = false
    }
  }
  
  const setFontSize = (fontSize) => {
    if (FONT_SIZES[fontSize]) {
      currentFontSize.value = fontSize
      applyFontSize(fontSize)
    }
  }
  
  const toggleFollowSystem = () => {
    followSystem.value = !followSystem.value
    
    if (followSystem.value) {
      const systemTheme = detectSystemTheme()
      setTheme(systemTheme)
      
      // 监听系统主题变化
      watchSystemTheme((theme) => {
        if (followSystem.value) {
          setTheme(theme)
        }
      })
    }
  }
  
  const resetSettings = () => {
    setTheme('light')
    setFontSize('normal')
    followSystem.value = false
  }
  
  // 初始化
  const initialize = () => {
    // 应用初始设置
    applyTheme(currentTheme.value)
    applyFontSize(currentFontSize.value)
    
    // 监听系统主题变化（如果启用）
    if (followSystem.value) {
      watchSystemTheme((theme) => {
        if (followSystem.value) {
          setTheme(theme)
        }
      })
    }
  }
  
  return {
    // 状态
    currentTheme,
    currentFontSize,
    followSystem,
    
    // 计算属性
    themeConfig,
    fontSizeConfig,
    availableThemes,
    availableFontSizes,
    
    // 动作
    setTheme,
    setFontSize,
    toggleFollowSystem,
    resetSettings,
    initialize
  }
})