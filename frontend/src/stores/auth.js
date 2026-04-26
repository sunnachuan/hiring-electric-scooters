import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token'))
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const isAuthenticated = ref(!!token.value)

  const login = async (credentials) => {
    try {
      const response = await api.post('/auth/login', credentials)
      const { token: newToken, ...userData } = response.data
      
      token.value = newToken
      userInfo.value = userData
      isAuthenticated.value = true
      
      localStorage.setItem('token', newToken)
      localStorage.setItem('userInfo', JSON.stringify(userData))
      
      api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
      
      return { success: true }
    } catch (error) {
      return { 
        success: false, 
        message: error.response?.data?.message || '登录失败' 
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
      
      api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
      
      console.log('注册成功，用户信息已保存')
      return { success: true }
    } catch (error) {
      console.error('注册错误详情:', {
        error: error,
        response: error.response,
        data: error.response?.data
      })
      return { 
        success: false, 
        message: error.response?.data?.message || error.message || '注册失败' 
      }
    }
  }

  const logout = () => {
    token.value = null
    userInfo.value = null
    isAuthenticated.value = false
    
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    
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