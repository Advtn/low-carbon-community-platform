import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { loginByPassword, registerAccount } from '../services/loginService'

export function useLoginPage() {
  const router = useRouter()
  const loading = ref(false)
  const error = ref('')
  const authMode = ref('login')

  const loginForm = reactive({
    username: 'alice',
    password: '123456'
  })

  const registerForm = reactive({
    username: '',
    nickname: '',
    password: '',
    confirmPassword: ''
  })

  const isLoginMode = computed(() => authMode.value === 'login')

  function switchMode(mode) {
    authMode.value = mode
    error.value = ''
  }

  async function login() {
    if (!loginForm.username || !loginForm.password) {
      error.value = '请输入用户名和密码'
      return
    }

    loading.value = true
    error.value = ''
    try {
      const { data } = await loginByPassword({
        username: loginForm.username.trim(),
        password: loginForm.password
      })
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

  async function register() {
    const username = registerForm.username.trim()
    const nickname = registerForm.nickname.trim()
    const password = registerForm.password
    const confirmPassword = registerForm.confirmPassword

    if (!username || !nickname || !password || !confirmPassword) {
      error.value = '请完整填写注册信息'
      return
    }
    if (username.length < 3 || username.length > 50) {
      error.value = '用户名长度需在 3-50 个字符之间'
      return
    }
    if (nickname.length < 2 || nickname.length > 50) {
      error.value = '昵称长度需在 2-50 个字符之间'
      return
    }
    if (password.length < 6) {
      error.value = '密码至少 6 位'
      return
    }
    if (password !== confirmPassword) {
      error.value = '两次输入的密码不一致'
      return
    }

    loading.value = true
    error.value = ''
    try {
      const { data } = await registerAccount({
        username,
        nickname,
        password
      })
      sessionStorage.setItem('token', data.token)
      sessionStorage.setItem('user', JSON.stringify(data))
      router.push('/resident')
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    error,
    authMode,
    isLoginMode,
    loginForm,
    registerForm,
    switchMode,
    login,
    register
  }
}
