import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8082/api',
  timeout: 10000
})

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
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
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api