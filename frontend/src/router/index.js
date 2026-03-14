import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import ResidentView from '../views/ResidentView.vue'
import AdminView from '../views/AdminView.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: LoginView },
  { path: '/resident', component: ResidentView, meta: { auth: true, role: 'RESIDENT' } },
  { path: '/admin', component: AdminView, meta: { auth: true, role: 'ADMIN' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (!to.meta.auth) {
    return true
  }

  const token = sessionStorage.getItem('token')
  const userRaw = sessionStorage.getItem('user')
  if (!token || !userRaw) {
    return '/login'
  }

  const user = JSON.parse(userRaw)
  if (to.meta.role && user.role !== to.meta.role) {
    return user.role === 'ADMIN' ? '/admin' : '/resident'
  }

  return true
})

export default router
