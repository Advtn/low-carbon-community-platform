import axios from 'axios'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api'
})

function resolveErrorMessage(err) {
  const status = err?.response?.status
  const responseMessage = err?.response?.data?.message

  if (responseMessage) {
    return responseMessage
  }

  if (status === 413) {
    return '上传内容过大，请将图片压缩到 5MB 以内后重试'
  }

  if (status === 502 || status === 503 || status === 504) {
    return '服务暂时不可用，请稍后重试'
  }

  if (err?.code === 'ECONNABORTED') {
    return '请求超时，请稍后重试'
  }

  if (!err?.response) {
    return '网络异常，请检查服务是否正常启动'
  }

  return '请求失败'
}

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
    if (err?.response?.status === 401 && window.location.pathname !== '/login') {
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
      window.location.assign('/login')
    }
    const msg = resolveErrorMessage(err)
    return Promise.reject(new Error(msg))
  }
)

export default client
