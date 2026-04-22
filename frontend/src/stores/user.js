import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUser(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  async function login(loginForm) {
    const res = await request.post('/auth/login', loginForm)
    const data = res.data
    setToken(data.token)
    setUser({
      id: data.userId,
      username: data.username,
      role: data.role,
      avatar: data.avatar,
      staffRole: data.staffRole,
      shopName: data.shopName,
      baseSalary: data.baseSalary,
      commissionRate: data.commissionRate,
      realName: data.realName,
      idCard: data.idCard,
      parentProviderId: data.parentProviderId
    })
    return res
  }

  async function register(regForm) {
    return await request.post('/auth/register', regForm)
  }

  async function fetchCurrentUser() {
    try {
      const res = await request.get('/auth/current')
      setUser(res.data)
      return res.data
    } catch (e) {
      console.error('获取用户信息失败', e)
      return null
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 权限判断
  const isAdmin = () => userInfo.value.role === 2
  const isProvider = () => userInfo.value.role === 1
  const isUser = () => userInfo.value.role === 0
  const isShopOwner = () => userInfo.value.role === 1 && (!userInfo.value.staffRole || userInfo.value.staffRole === 1)
  const isShopStaff = () => userInfo.value.role === 1 && userInfo.value.staffRole === 2

  return {
    token, userInfo, login, register, logout,
    fetchCurrentUser, setToken, setUser,
    isAdmin, isProvider, isUser, isShopOwner, isShopStaff
  }
})
