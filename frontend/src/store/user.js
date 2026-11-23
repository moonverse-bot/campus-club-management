import { defineStore } from 'pinia'
import { login as loginApi, getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null')
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    nickname: (state) => state.userInfo?.nickname || state.userInfo?.realName || '',
    avatar: (state) => state.userInfo?.avatar || '',
    roles: (state) => state.userInfo?.roles || []
  },
  actions: {
    async login(loginForm) {
      const res = await loginApi(loginForm)
      this.token = res.data.token
      localStorage.setItem('token', res.data.token)
      
      // 获取用户信息
      const userRes = await getCurrentUser()
      this.userInfo = userRes.data
      localStorage.setItem('userInfo', JSON.stringify(userRes.data))
      return res.data
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
    async fetchUserInfo() {
      const res = await getCurrentUser()
      this.userInfo = res.data
      localStorage.setItem('userInfo', JSON.stringify(res.data))
    }
  }
})
