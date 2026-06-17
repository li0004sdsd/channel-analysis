import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth.js'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const email = ref(localStorage.getItem('email') || '')

  const isAuthenticated = computed(() => !!token.value)

  async function login(credentials) {
    const res = await authApi.login(credentials)
    token.value = res.data.data.token
    username.value = res.data.data.username
    email.value = res.data.data.email
    localStorage.setItem('token', token.value)
    localStorage.setItem('username', username.value)
    localStorage.setItem('email', email.value)
  }

  async function register(data) {
    const res = await authApi.register(data)
    token.value = res.data.data.token
    username.value = res.data.data.username
    email.value = res.data.data.email
    localStorage.setItem('token', token.value)
    localStorage.setItem('username', username.value)
    localStorage.setItem('email', email.value)
  }

  function logout() {
    token.value = ''
    username.value = ''
    email.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('email')
  }

  return { token, username, email, isAuthenticated, login, register, logout }
})
