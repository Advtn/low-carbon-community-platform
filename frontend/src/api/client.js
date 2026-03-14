import axios from 'axios'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api'
})

client.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (res) => res,
  (err) => {
    const msg = err?.response?.data?.message || '\u8bf7\u6c42\u5931\u8d25'
    return Promise.reject(new Error(msg))
  }
)

export default client
