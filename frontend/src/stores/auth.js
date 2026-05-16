import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token'))
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const isAuthenticated = ref(!!token.value)

  const login = async (credentials) => {
    try {
      console.log('发送登录请求，URL:', '/auth/login', '数据:', credentials)
      const response = await api.post('/auth/login', credentials)
      console.log('登录响应数据:', response)
      const { token: newToken, ...userData } = response.data
      
      token.value = newToken
      userInfo.value = userData
      isAuthenticated.value = true
      
      localStorage.setItem('token', newToken)
      localStorage.setItem('userInfo', JSON.stringify(userData))
      // 记录token获取时间戳
      localStorage.setItem('token_timestamp', Date.now().toString())
      
      api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
      
      console.log('登录成功，用户信息:', userData)
      return { success: true }
    } catch (error) {
      console.error('登录请求失败:', error)
      console.error('错误详情:', {
        message: error?.message,
        response: error?.response?.data,
        status: error?.response?.status
      })
      
      let errorMessage = '登录失败'
      if (error.response?.data) {
        if (typeof error.response.data === 'string') {
          errorMessage = error.response.data
        } else if (error.response.data.message) {
          errorMessage = error.response.data.message
        } else if (error.response.data.error) {
          errorMessage = error.response.data.error
        }
      } else if (error.message) {
        errorMessage = error.message
      }
      
      return { 
        success: false, 
        message: errorMessage
      }
    }
  }

  const register = async (userData) => {
    try {
      console.log('发送注册请求:', userData)
      const response = await api.post('/auth/register', userData)
      console.log('注册响应:', response.data)
      
      const { token: newToken, ...userInfoData } = response.data
      
      if (!newToken) {
        throw new Error('注册成功但未返回token')
      }
      
      token.value = newToken
      userInfo.value = userInfoData
      isAuthenticated.value = true
      
      localStorage.setItem('token', newToken)
      localStorage.setItem('userInfo', JSON.stringify(userInfoData))
      // 记录token获取时间戳
      localStorage.setItem('token_timestamp', Date.now().toString())
      
      api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
      
      console.log('注册成功，用户信息已保存')
      return { success: true }
    } catch (error) {
      console.error('注册错误详情:', {
        error: error,
        response: error.response,
        data: error.response?.data
      })
      
      // 修复错误信息提取逻辑
      let errorMessage = '注册失败'
      if (error.response?.data) {
        // 后端返回的错误信息可能是字符串或对象
        if (typeof error.response.data === 'string') {
          errorMessage = error.response.data
        } else if (error.response.data.message) {
          errorMessage = error.response.data.message
        } else if (error.response.data.error) {
          errorMessage = error.response.data.error
        }
      } else if (error.message) {
        errorMessage = error.message
      }
      
      return { 
        success: false, 
        message: errorMessage
      }
    }
  }

  const logout = () => {
    token.value = null
    userInfo.value = null
    isAuthenticated.value = false
    
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('token_timestamp')
    
    delete api.defaults.headers.common['Authorization']
  }

  const changePassword = async (passwordData) => {
    try {
      const response = await api.post('/auth/change-password', {
        username: userInfo.value?.username,
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword
      })
      
      return { success: true, message: response.data }
    } catch (error) {
      return { 
        success: false, 
        message: error.response?.data || '密码修改失败' 
      }
    }
  }

  const initializeAuth = () => {
    if (token.value) {
      api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
    }
  }

  return {
    token,
    userInfo,
    isAuthenticated,
    login,
    register,
    logout,
    changePassword,
    initializeAuth
  }
})