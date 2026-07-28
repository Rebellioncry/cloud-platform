<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar" :class="{ 'is-collapse': isCollapse }">
      <div class="logo">
        <div class="logo-icon">
          <svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="16" cy="16" r="4" fill="currentColor"/>
            <circle cx="16" cy="4" r="2.5" fill="currentColor" opacity="0.7"/>
            <circle cx="16" cy="28" r="2.5" fill="currentColor" opacity="0.7"/>
            <circle cx="4" cy="16" r="2.5" fill="currentColor" opacity="0.7"/>
            <circle cx="28" cy="16" r="2.5" fill="currentColor" opacity="0.7"/>
            <circle cx="7.5" cy="7.5" r="2" fill="currentColor" opacity="0.5"/>
            <circle cx="24.5" cy="7.5" r="2" fill="currentColor" opacity="0.5"/>
            <circle cx="7.5" cy="24.5" r="2" fill="currentColor" opacity="0.5"/>
            <circle cx="24.5" cy="24.5" r="2" fill="currentColor" opacity="0.5"/>
            <line x1="16" y1="12" x2="16" y2="6.5" stroke="currentColor" stroke-width="1" opacity="0.4"/>
            <line x1="16" y1="20" x2="16" y2="25.5" stroke="currentColor" stroke-width="1" opacity="0.4"/>
            <line x1="12" y1="16" x2="6.5" y2="16" stroke="currentColor" stroke-width="1" opacity="0.4"/>
            <line x1="20" y1="16" x2="25.5" y2="16" stroke="currentColor" stroke-width="1" opacity="0.4"/>
            <line x1="13.2" y1="13.2" x2="9.5" y2="9.5" stroke="currentColor" stroke-width="1" opacity="0.3"/>
            <line x1="18.8" y1="13.2" x2="22.5" y2="9.5" stroke="currentColor" stroke-width="1" opacity="0.3"/>
            <line x1="13.2" y1="18.8" x2="9.5" y2="22.5" stroke="currentColor" stroke-width="1" opacity="0.3"/>
            <line x1="18.8" y1="18.8" x2="22.5" y2="22.5" stroke="currentColor" stroke-width="1" opacity="0.3"/>
          </svg>
        </div>
        <transition name="fade-text">
          <div v-show="!isCollapse" class="logo-text">
            <span class="logo-title">物联网平台</span>
            <span class="logo-sub">IoT Platform</span>
          </div>
        </transition>
      </div>

      <el-scrollbar class="menu-scroll">
        <el-menu
          :default-active="activeMenu"
          router
          :unique-opened="true"
          :collapse="isCollapse"
          :collapse-transition="false"
          class="sidebar-menu"
        >
          <template v-for="menu in menus" :key="menu.id">
            <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.path || menu.id">
              <template #title>
                <el-icon class="menu-icon"><component :is="getIcon(menu.icon)" /></el-icon>
                <span>{{ menu.menuName }}</span>
              </template>
              <template v-for="child in menu.children" :key="child.id">
                <el-sub-menu v-if="child.children && child.children.length > 0" :index="child.path || child.id">
                  <template #title>
                    <el-icon v-if="child.icon" class="menu-icon"><component :is="getIcon(child.icon)" /></el-icon>
                    <span>{{ child.menuName }}</span>
                  </template>
                  <el-menu-item
                    v-for="grandchild in child.children"
                    :key="grandchild.id"
                    :index="grandchild.path"
                  >
                    <el-icon v-if="grandchild.icon" class="menu-icon"><component :is="getIcon(grandchild.icon)" /></el-icon>
                    <span>{{ grandchild.menuName }}</span>
                  </el-menu-item>
                </el-sub-menu>
                <el-menu-item v-else :index="child.path">
                  <el-icon v-if="child.icon" class="menu-icon"><component :is="getIcon(child.icon)" /></el-icon>
                  <span>{{ child.menuName }}</span>
                </el-menu-item>
              </template>
            </el-sub-menu>
            <el-menu-item v-else :index="menu.path">
              <el-icon class="menu-icon"><component :is="getIcon(menu.icon)" /></el-icon>
              <span>{{ menu.menuName }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>

        <div class="sidebar-footer">
        <div class="collapse-toggle" @click="toggleCollapse">
          <el-icon :size="18"><component :is="isCollapse ? 'Expand' : 'Fold'" /></el-icon>
          <transition name="fade-text">
            <span v-show="!isCollapse" class="collapse-label">收起菜单</span>
          </transition>
        </div>
        <div v-show="!isCollapse" class="version-tag">v1.0.0</div>
      </div>
    </el-aside>
    
    <el-container>
      <el-header>
        <div v-if="userStore.isImpersonating" class="impersonate-bar">
          <span class="impersonate-text">
            <el-icon><Warning /></el-icon>
            正在以「{{ userStore.impersonateTenantName }}」租户管理员身份浏览
          </span>
          <el-button type="danger" size="small" @click="handleReturnImpersonate">返回管理后台</el-button>
        </div>
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">
              {{ route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <div class="user-avatar">{{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U').charAt(0).toUpperCase() }}</div>
              <span class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}</span>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { logout as logoutApi, getUserInfo } from '@/api/auth'
import { returnFromImpersonate } from '@/api/system'
import * as Icons from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const toggleCollapse = () => { isCollapse.value = !isCollapse.value }

const iconMap = {}
for (const [key, component] of Object.entries(Icons)) {
  iconMap[key] = component
}

const getIcon = (name) => {
  return iconMap[name] || iconMap['Menu']
}

const menus = computed(() => {
  const raw = userStore.userInfo?.menus || []
  return raw.filter(m => m.menuType === 0)
})

onMounted(async () => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  try {
    const res = await getUserInfo()
    userStore.setUserInfo(res.data)
  } catch (e) {
    userStore.logout()
    router.push('/login')
  }
})

const activeMenu = computed(() => route.path)

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        type: 'warning'
      })
      await logoutApi()
    } catch (e) {
    }
    userStore.logout()
    router.push('/login')
  }
}

const handleReturnImpersonate = async () => {
  try {
    await returnFromImpersonate()
    userStore.stopImpersonate()
    const res = await getUserInfo()
    userStore.setUserInfo(res.data)
    ElMessage.success('已返回管理后台')
    router.push('/platform/tenant')
  } catch (e) {
    console.error('返回管理后台失败:', e)
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

/* ===== 侧栏 ===== */
.sidebar {
  background: linear-gradient(180deg, #0a1628 0%, #0f2035 40%, #122a45 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-right: 1px solid rgba(64, 158, 255, 0.15);
  transition: width 0.3s ease;
}

.sidebar.is-collapse .logo {
  justify-content: center;
  padding: 0;
}

.sidebar.is-collapse .logo-icon {
  margin: 0;
}

/* ===== Logo文字过渡 ===== */
.fade-text-enter-active,
.fade-text-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
  overflow: hidden;
  white-space: nowrap;
}
.fade-text-enter-from,
.fade-text-leave-to {
  opacity: 0;
  width: 0;
  transform: translateX(-10px);
}
.fade-text-enter-to,
.fade-text-leave-from {
  opacity: 1;
  width: auto;
  transform: translateX(0);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
  border-bottom: 1px solid rgba(64, 158, 255, 0.1);
  flex-shrink: 0;
}

.logo-icon {
  width: 36px;
  height: 36px;
  color: #409eff;
  flex-shrink: 0;
  filter: drop-shadow(0 0 6px rgba(64, 158, 255, 0.5));
}

.logo-icon svg {
  width: 100%;
  height: 100%;
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.logo-title {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
}

.logo-sub {
  color: rgba(128, 163, 204, 0.6);
  font-size: 10px;
  letter-spacing: 0.5px;
  margin-top: 2px;
}

.menu-scroll {
  flex: 1;
  overflow: hidden;
}

/* ===== 菜单 ===== */
.sidebar-menu {
  background: transparent !important;
  border-right: none !important;
  padding: 8px 0;
}

.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  color: #a8b4c8 !important;
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: 8px;
  font-size: 13px;
  transition: all 0.25s ease;
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(64, 158, 255, 0.12) !important;
  color: #c8d6e5 !important;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.25) 0%, rgba(64, 158, 255, 0.1) 100%) !important;
  color: #409eff !important;
  font-weight: 600;
  position: relative;
}

.sidebar-menu :deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: #409eff;
  border-radius: 0 3px 3px 0;
  box-shadow: 0 0 8px rgba(64, 158, 255, 0.6);
}

.menu-icon {
  font-size: 16px;
  margin-right: 8px;
  width: 16px;
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item) {
  padding-left: 52px !important;
  min-width: auto;
}

.sidebar-menu :deep(.el-sub-menu .el-menu) {
  background: transparent !important;
}

.sidebar-menu :deep(.el-sub-menu__icon-arrow) {
  color: #6b7d95;
  font-size: 12px;
}

/* ===== 侧栏底部 ===== */
.sidebar-footer {
  padding: 8px 0 12px;
  border-top: 1px solid rgba(64, 158, 255, 0.1);
  flex-shrink: 0;
}

.collapse-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 20px;
  margin: 0 8px 4px;
  border-radius: 8px;
  color: #6b7d95;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 13px;
}

.collapse-toggle:hover {
  color: #c8d6e5;
  background: rgba(64, 158, 255, 0.12);
}

.sidebar.is-collapse .collapse-toggle {
  padding: 8px 0;
  margin: 0 4px 4px;
  justify-content: center;
}

.version-tag {
  font-size: 11px;
  color: rgba(128, 163, 204, 0.35);
  text-align: center;
  padding: 0 20px;
}

/* ===== 头部 ===== */
.el-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #0f1d2e;
  border-bottom: 1px solid rgba(64, 158, 255, 0.1);
  height: 64px;
  gap: 16px;
}

.impersonate-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.15), rgba(230, 162, 60, 0.05));
  border: 1px solid rgba(230, 162, 60, 0.4);
  border-radius: 8px;
  padding: 4px 12px;
  flex-shrink: 0;
}

.impersonate-text {
  color: #e6a23c;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 4px;
}

.el-main {
  background-color: #0a1628;
  padding: 0;
  overflow: auto;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-left :deep(.el-breadcrumb__inner) {
  color: #8899aa !important;
}
.header-left :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #c8d6e5 !important;
}
.header-left :deep(.el-breadcrumb__separator) {
  color: #4a5568 !important;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-dropdown:hover {
  background-color: rgba(64, 158, 255, 0.1);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  color: #c8d6e5;
}

.dropdown-arrow {
  color: #64748b;
}

/* ===== 侧栏折叠后菜单居中 ===== */
.sidebar.is-collapse .sidebar-menu :deep(.el-menu-item),
.sidebar.is-collapse .sidebar-menu :deep(.el-sub-menu__title) {
  padding: 0 !important;
  justify-content: center;
}

.sidebar.is-collapse .sidebar-menu :deep(.el-menu-item .menu-icon),
.sidebar.is-collapse .sidebar-menu :deep(.el-sub-menu__title .menu-icon) {
  margin-right: 0;
}

.sidebar.is-collapse .sidebar-menu :deep(.el-menu-item.is-active)::before {
  left: 0;
}

/* 折叠后 tooltip 弹出菜单暗色适配 */
.el-menu--popup {
  background-color: #132238 !important;
}

.el-menu--popup .el-menu-item {
  color: #a8b4c8 !important;
}

.el-menu--popup .el-menu-item:hover {
  background: rgba(64, 158, 255, 0.12) !important;
  color: #c8d6e5 !important;
}

.el-menu--popup .el-menu-item.is-active {
  color: #409eff !important;
}
</style>
