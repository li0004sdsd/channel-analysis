import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth.js'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue'), meta: { public: true } },
  { path: '/register', name: 'Register', component: () => import('@/views/RegisterView.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/components/AppLayout.vue'),
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/DashboardView.vue') },
      { path: 'channels', name: 'Channels', component: () => import('@/views/ChannelListView.vue') },
      { path: 'channels/create', name: 'ChannelCreate', component: () => import('@/views/ChannelFormView.vue') },
      { path: 'channels/:id/edit', name: 'ChannelEdit', component: () => import('@/views/ChannelFormView.vue') },
      { path: 'stats', name: 'Stats', component: () => import('@/views/StatsView.vue') },
      { path: 'analysis', name: 'Analysis', component: () => import('@/views/AnalysisView.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (!to.meta.public && !authStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
