// 主题管理工具函数

// 主题配置
const THEMES = {
  light: {
    name: '浅色主题',
    value: 'light',
    description: '适合白天使用的明亮主题'
  },
  dark: {
    name: '深色主题',
    value: 'dark',
    description: '适合夜间使用的护眼主题'
  },
  'high-contrast': {
    name: '高对比度主题',
    value: 'high-contrast',
    description: '适合视力障碍用户的高对比度主题'
  }
}

// 字体大小配置
const FONT_SIZES = {
  small: {
    name: '小字体',
    value: 'small',
    scale: 0.875,
    description: '适合密集信息显示'
  },
  normal: {
    name: '正常字体',
    value: 'normal',
    scale: 1,
    description: '标准字体大小'
  },
  large: {
    name: '大字体',
    value: 'large',
    scale: 1.125,
    description: '适合视力不佳用户'
  }
}

// 存储键名
const STORAGE_KEYS = {
  THEME: 'scooter_theme',
  FONT_SIZE: 'scooter_font_size'
}

// 获取当前主题
function getCurrentTheme() {
  const saved = localStorage.getItem(STORAGE_KEYS.THEME)
  return saved && THEMES[saved] ? saved : 'light'
}

// 获取当前字体大小
function getCurrentFontSize() {
  const saved = localStorage.getItem(STORAGE_KEYS.FONT_SIZE)
  return saved && FONT_SIZES[saved] ? saved : 'normal'
}

// 应用主题
function applyTheme(theme) {
  const html = document.documentElement
  
  // 移除所有主题属性
  html.removeAttribute('data-theme')
  
  // 设置新主题
  if (theme !== 'light') {
    html.setAttribute('data-theme', theme)
  } else {
    html.removeAttribute('data-theme')
  }
  
  // 强制重新渲染样式
  html.style.display = 'none'
  html.offsetHeight // 触发重排
  html.style.display = ''
  
  // 添加过渡类
  html.classList.add('theme-transition')
  setTimeout(() => {
    html.classList.remove('theme-transition')
  }, 300)
  
  // 保存到本地存储
  localStorage.setItem(STORAGE_KEYS.THEME, theme)
}

// 应用字体大小
function applyFontSize(fontSize) {
  const html = document.documentElement
  
  // 移除所有字体大小属性
  Object.values(FONT_SIZES).forEach(fs => {
    html.removeAttribute(`data-font-size-${fs.value}`)
  })
  
  // 设置新字体大小
  html.setAttribute('data-font-size', fontSize)
  
  // 保存到本地存储
  localStorage.setItem(STORAGE_KEYS.FONT_SIZE, fontSize)
}

// 初始化主题设置
function initializeTheme() {
  const theme = getCurrentTheme()
  const fontSize = getCurrentFontSize()
  
  applyTheme(theme)
  applyFontSize(fontSize)
}

// 获取主题配置
function getThemeConfig() {
  return {
    themes: THEMES,
    fontSizes: FONT_SIZES,
    currentTheme: getCurrentTheme(),
    currentFontSize: getCurrentFontSize()
  }
}

// 重置为默认设置
function resetToDefaults() {
  applyTheme('light')
  applyFontSize('normal')
}

export {
  THEMES,
  FONT_SIZES,
  getCurrentTheme,
  getCurrentFontSize,
  applyTheme,
  applyFontSize,
  initializeTheme,
  getThemeConfig,
  resetToDefaults
}