import client from '../api/client'

export const apiOrigin = String(client.defaults.baseURL || '').replace(/\/api$/, '')

export async function fetchResidentData() {
  const [profileRes, rulesRes, reportsRes, ledgerRes, itemsRes, ordersRes, leaderboardRes] =
    await Promise.all([
      client.get('/user/profile'),
      client.get('/user/rules'),
      client.get('/user/reports'),
      client.get('/user/ledger'),
      client.get('/user/mall/items'),
      client.get('/user/mall/orders'),
      client.get('/user/leaderboard')
    ])

  return {
    profile: profileRes.data,
    rules: rulesRes.data,
    reports: reportsRes.data,
    ledger: ledgerRes.data,
    items: itemsRes.data,
    orders: ordersRes.data,
    leaderboard: leaderboardRes.data
  }
}

export function createReport(payload) {
  return client.post('/user/reports', payload)
}

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return client.post('/common/upload-image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function redeemItem(payload) {
  return client.post('/user/mall/redeem', payload)
}

export function logoutAccount() {
  return client.post('/auth/logout')
}
