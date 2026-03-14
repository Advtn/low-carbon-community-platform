import client from '../api/client'

export function loginByPassword(payload) {
  return client.post('/auth/login', payload)
}
