import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/views/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/layout/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'system/user',
        name: 'User',
        component: () => import('@/views/system/User.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'system/role',
        name: 'Role',
        component: () => import('@/views/system/Role.vue'),
        meta: { title: '角色管理', icon: 'Role' }
      },
      {
        path: 'system/menu',
        name: 'Menu',
        component: () => import('@/views/system/Menu.vue'),
        meta: { title: '菜单管理', icon: 'Menu' }
      },
      {
        path: 'system/tenant',
        name: 'Tenant',
        component: () => import('@/views/system/Tenant.vue'),
        meta: { title: '租户管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'system/audit',
        name: 'AuditLog',
        component: () => import('@/views/system/AuditLog.vue'),
        meta: { title: '审计日志', icon: 'Document' }
      },
      {
        path: 'iot/product',
        name: 'IotProduct',
        component: () => import('@/views/iot/Product.vue'),
        meta: { title: '产品管理', icon: 'Box' }
      },
      {
        path: 'iot/device',
        name: 'IotDevice',
        component: () => import('@/views/iot/Device.vue'),
        meta: { title: '设备管理', icon: 'Monitor' }
      },
      {
        path: 'iot/device/:id',
        name: 'IotDeviceDetail',
        component: () => import('@/views/iot/DeviceDetail.vue'),
        meta: { title: '设备详情', icon: 'Monitor', hidden: true }
      },
      {
        path: 'iot/mqtt',
        name: 'IotMqtt',
        component: () => import('@/views/iot/MqttConfig.vue'),
        meta: { title: 'MQTT配置', icon: 'Connection' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token
  
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
