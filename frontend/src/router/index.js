import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    redirect: '/scooters'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/scooters',
    name: 'Scooters',
    component: () => import('@/views/Scooters.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/my-bookings',
    name: 'MyBookings',
    component: () => import('@/views/MyBookings.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/account-settings',
    name: 'AccountSettings',
    component: () => import('@/views/AccountSettings.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/change-password',
    name: 'ChangePassword',
    component: () => import('@/views/ChangePassword.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/insurance-terms',
    name: 'InsuranceTerms',
    component: () => import('@/views/InsuranceTerms.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/bookings',
    name: 'Bookings',
    component: () => import('@/views/Bookings.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/feedback',
    name: 'Feedback',
    component: () => import('@/views/Feedback.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/map',
    name: 'Map',
    component: () => import('@/views/Map.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/Dashboard.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/locations',
    name: 'LocationManagement',
    component: () => import('@/views/admin/LocationManagement.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/scooters',
    name: 'AdminScooters',
    component: () => import('@/views/admin/Scooters.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/feedback',
    name: 'AdminFeedback',
    component: () => import('@/views/admin/Feedback.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/device-monitor',
    name: 'DeviceMonitor',
    component: () => import('@/views/admin/DeviceMonitor.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/operations',
    name: 'Operations',
    component: () => import('@/views/admin/Operations.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/unlock',
    name: 'Unlock',
    component: () => import('@/views/Unlock.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.meta.requiresGuest && authStore.isAuthenticated) {
    next('/')
  } else if (to.meta.requiresAdmin && authStore.userInfo?.role !== 'ADMIN') {
    next('/')
  } else {
    next()
  }
})

export default router