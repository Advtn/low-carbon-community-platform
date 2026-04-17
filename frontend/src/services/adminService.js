import client from '../api/client'

export const apiOrigin = String(client.defaults.baseURL || '').replace(/\/api$/, '')

export async function fetchAdminData() {
  const [dashboardRes, usersRes, rulesRes, reportsRes, itemsRes, ordersRes] = await Promise.all([
    client.get('/admin/dashboard'),
    client.get('/admin/users'),
    client.get('/admin/rules'),
    client.get('/admin/reports/pending'),
    client.get('/admin/items'),
    client.get('/admin/orders')
  ])

  return {
    dashboard: dashboardRes.data,
    users: usersRes.data,
    rules: rulesRes.data,
    pendingReports: reportsRes.data,
    items: itemsRes.data,
    orders: ordersRes.data
  }
}

export function createUser(payload) {
  return client.post('/admin/users', payload)
}

export function updateUser(id, payload) {
  return client.put(`/admin/users/${id}`, payload)
}

export function deleteUserById(id) {
  return client.delete(`/admin/users/${id}`)
}

export function createRule(payload) {
  return client.post('/admin/rules', payload)
}

export function updateRule(id, payload) {
  return client.put(`/admin/rules/${id}`, payload)
}

export function deleteRuleById(id) {
  return client.delete(`/admin/rules/${id}`)
}

export function auditReport(id, payload) {
  return client.post(`/admin/reports/${id}/audit`, payload)
}

export function createItem(payload) {
  return client.post('/admin/items', payload)
}

export function updateItem(id, payload) {
  return client.put(`/admin/items/${id}`, payload)
}

export function deleteItemById(id) {
  return client.delete(`/admin/items/${id}`)
}

export function updateOrderStatus(id, payload) {
  return client.post(`/admin/orders/${id}/status`, payload)
}

export function logoutAccount() {
  return client.post('/auth/logout')
}
