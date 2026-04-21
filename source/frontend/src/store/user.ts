import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const id = ref('')
  const username = ref('')
  const nickname = ref('')
  const avatar = ref('')
  const deptId = ref('')
  const deptName = ref('')
  const position = ref('')
  const permissions = ref<string[]>([])

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info: any) => {
    id.value = info.id
    username.value = info.username
    nickname.value = info.nickname
    avatar.value = info.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
    deptId.value = info.deptId
    deptName.value = info.deptName
    position.value = info.position
    permissions.value = info.permissions || []
  }

  const logout = () => {
    token.value = ''
    id.value = ''
    username.value = ''
    nickname.value = ''
    avatar.value = ''
    permissions.value = []
    localStorage.removeItem('token')
  }

  const hasPermission = (permission: string) => {
    if (permissions.value.includes('*:*:*')) return true
    return permissions.value.includes(permission)
  }

  return {
    token,
    id,
    username,
    nickname,
    avatar,
    deptId,
    deptName,
    position,
    permissions,
    setToken,
    setUserInfo,
    logout,
    hasPermission
  }
})
