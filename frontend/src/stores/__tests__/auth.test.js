import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../auth'

// Mock the api module
vi.mock('@/api', () => ({
  default: {
    post: vi.fn(),
    defaults: {
      headers: {
        common: {}
      }
    }
  }
}))

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    // Clear localStorage before each test
    localStorage.clear()
    // Reset mocks
    vi.clearAllMocks()
  })

  it('initializes with default values', () => {
    const store = useAuthStore()
    expect(store.token).toBeNull()
    expect(store.userInfo).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })

  it('successfully logs in', async () => {
    const mockApi = await import('@/api')
    const store = useAuthStore()
    
    mockApi.default.post.mockResolvedValue({
      data: {
        token: 'test-token-123',
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
        role: 'USER'
      }
    })

    const result = await store.login({
      username: 'testuser',
      password: 'correct-password'
    })

    expect(result.success).toBe(true)
    expect(store.token).toBe('test-token-123')
    expect(store.userInfo.username).toBe('testuser')
    expect(store.isAuthenticated).toBe(true)
    expect(localStorage.getItem('token')).toBe('test-token-123')
    expect(mockApi.default.headers.common['Authorization']).toBe('Bearer test-token-123')
  })

  it('handles login failure', async () => {
    const mockApi = await import('@/api')
    const store = useAuthStore()
    
    mockApi.default.post.mockRejectedValue({
      response: {
        data: '用户名或密码错误'
      }
    })

    const result = await store.login({
      username: 'testuser',
      password: 'wrong-password'
    })

    expect(result.success).toBe(false)
    expect(result.message).toBe('用户名或密码错误')
    expect(store.isAuthenticated).toBe(false)
  })

  it('successfully registers', async () => {
    const mockApi = await import('@/api')
    const store = useAuthStore()
    
    mockApi.default.post.mockResolvedValue({
      data: {
        token: 'test-token-456',
        id: 2,
        username: 'newuser',
        email: 'newuser@example.com',
        role: 'USER'
      }
    })

    const result = await store.register({
      username: 'newuser',
      email: 'newuser@example.com',
      password: 'StrongPass123',
      role: 'USER'
    })

    expect(result.success).toBe(true)
    expect(store.token).toBe('test-token-456')
    expect(store.isAuthenticated).toBe(true)
  })

  it('handles registration failure', async () => {
    const mockApi = await import('@/api')
    const store = useAuthStore()
    
    mockApi.default.post.mockRejectedValue({
      response: {
        data: '用户名已存在'
      }
    })

    const result = await store.register({
      username: 'existinguser',
      email: 'test@example.com',
      password: 'StrongPass123',
      role: 'USER'
    })

    expect(result.success).toBe(false)
    expect(result.message).toBe('用户名已存在')
  })

  it('successfully logs out', async () => {
    const mockApi = await import('@/api')
    const store = useAuthStore()
    
    // First set authenticated state
    store.token = 'test-token'
    store.isAuthenticated = true
    store.userInfo = { username: 'testuser' }
    localStorage.setItem('token', 'test-token')
    localStorage.setItem('userInfo', JSON.stringify({ username: 'testuser' }))
    mockApi.default.headers.common['Authorization'] = 'Bearer test-token'

    store.logout()

    expect(store.token).toBeNull()
    expect(store.userInfo).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(localStorage.getItem('token')).toBeNull()
    expect(mockApi.default.headers.common['Authorization']).toBeUndefined()
  })

  it('initializes auth from localStorage', async () => {
    const mockApi = await import('@/api')
    localStorage.setItem('token', 'saved-token')
    
    const store = useAuthStore()
    store.initializeAuth()
    
    expect(mockApi.default.headers.common['Authorization']).toBe('Bearer saved-token')
  })
})
