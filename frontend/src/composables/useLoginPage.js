import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { loginByPassword } from '../services/loginService'

export function useLoginPage() {
  const router = useRouter()
  const loading = ref(false)
  const error = ref('')

  const form = reactive({
    username: 'alice',
    password: '123456'
  })

  async function login() {
    if (!form.username || !form.password) {
      error.value = '\u8bf7\u8f93\u5165\u7528\u6237\u540d\u548c\u5bc6\u7801'
      return
    }

    loading.value = true
    error.value = ''
    try {
      const { data } = await loginByPassword(form)
      sessionStorage.setItem('token', data.token)
      sessionStorage.setItem('user', JSON.stringify(data))
      if (data.role === 'ADMIN') {
        router.push('/admin')
      } else {
        router.push('/resident')
      }
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    error,
    form,
    login
  }
}
