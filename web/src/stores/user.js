import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(null)
  const permissions = ref([])
  const impersonating = ref(JSON.parse(localStorage.getItem('impersonate') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isSuperAdmin = computed(() => userInfo.value?.userId === '2')
  const isImpersonating = computed(() => !!impersonating.value)
  const impersonateTenantName = computed(() => impersonating.value?.tenantName || '')

  function setUserToken(newToken) {
    token.value = newToken
    setToken(newToken)
  }

  function setUserInfo(info) {
    userInfo.value = info
    permissions.value = info.permissions || []
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    impersonating.value = null
    localStorage.removeItem('impersonate')
    removeToken()
  }

  function startImpersonate(data) {
    impersonating.value = {
      originalToken: token.value,
      ...data
    }
    token.value = data.impersonateToken
    setToken(data.impersonateToken)
    localStorage.setItem('impersonate', JSON.stringify(impersonating.value))
  }

  function stopImpersonate() {
    const orig = impersonating.value?.originalToken
    impersonating.value = null
    localStorage.removeItem('impersonate')
    if (orig) {
      token.value = orig
      setToken(orig)
    }
  }

  function hasPermission(permission) {
    return permissions.value.includes(permission)
  }

  return {
    token,
    userInfo,
    permissions,
    isLoggedIn,
    isSuperAdmin,
    isImpersonating,
    impersonateTenantName,
    impersonating,
    setUserToken,
    setUserInfo,
    logout,
    hasPermission,
    startImpersonate,
    stopImpersonate
  }
})
