import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(null)
  const permissions = ref([])

  const isLoggedIn = computed(() => !!token.value)

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
    removeToken()
  }

  function hasPermission(permission) {
    return permissions.value.includes(permission)
  }

  return {
    token,
    userInfo,
    permissions,
    isLoggedIn,
    setUserToken,
    setUserInfo,
    logout,
    hasPermission
  }
})
