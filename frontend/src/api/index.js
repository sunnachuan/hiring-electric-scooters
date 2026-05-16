import axios from 'axios'

// 使用相对路径，通过 Vite 代理转发，避免 CORS 问题
const api = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 检查token是否即将过期（在过期前5分钟）
const isTokenExpiringSoon = () => {
  const token = localStorage.getItem('token')
  if (!token) return false
  
  try {
    // 简单的token过期检查（实际项目中应该解析JWT）
    // 这里使用localStorage存储token时间戳
    const tokenTimestamp = localStorage.getItem('token_timestamp')
    if (!tokenTimestamp) return false
    
    const tokenAge = Date.now() - parseInt(tokenTimestamp)
    // 假设token有效期为7天（604800秒），在过期前5分钟提醒刷新
    const maxAge = 604800 * 1000 // 7天转换为毫秒
    const warningThreshold = 5 * 60 * 1000 // 5分钟
    
    return tokenAge > (maxAge - warningThreshold)
  } catch (error) {
    return false
  }
}

// 尝试刷新token
const tryRefreshToken = async () => {
  try {
    // 这里可以调用刷新token的API
    // 暂时使用重新登录的方式
    console.log('Token即将过期，建议重新登录以获取新token')
    return false
  } catch (error) {
    console.error('Token刷新失败:', error)
    return false
  }
}

api.interceptors.request.use(
  async (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      
      // 检查token是否即将过期
      if (isTokenExpiringSoon()) {
        console.warn('Token即将过期，建议重新登录')
        // 在实际项目中，这里应该调用刷新token的API
      }
    }
    
    // 添加用户认证信息到请求头（后端SecurityUtils需要）
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (userInfo && userInfo.id) {
      config.headers['X-User-Id'] = userInfo.id
      config.headers['X-Username'] = userInfo.username || ''
      config.headers['X-Email'] = userInfo.email || ''
      config.headers['X-Role'] = userInfo.role || 'USER'
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token_timestamp')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api