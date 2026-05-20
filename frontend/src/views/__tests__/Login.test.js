import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Login from '../Login.vue'
import ElementPlus from 'element-plus'

// Mock vue-router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

// Mock the auth store
vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    login: vi.fn().mockResolvedValue({ success: true })
  })
}))

describe('Login Component', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('renders login form', () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [ElementPlus]
      }
    })
    
    expect(wrapper.find('.login-card').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="用户名"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="密码"]').exists()).toBe(true)
    expect(wrapper.find('button').text()).toContain('登录')
  })

  it('shows register link', () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [ElementPlus]
      }
    })
    
    const registerLink = wrapper.find('.register-link')
    expect(registerLink.exists()).toBe(true)
    expect(registerLink.text()).toContain('立即注册')
  })

  it('has terms agreement checkbox', () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [ElementPlus]
      }
    })
    
    expect(wrapper.find('input[type="checkbox"]').exists()).toBe(true)
  })
})
