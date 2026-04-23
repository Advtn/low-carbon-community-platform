<template>
  <div v-if="open" class="workspace-modal-mask" @click.self="handleClose">
    <section class="workspace-modal-card">
      <header class="workspace-modal-head">
        <div>
          <span class="workspace-kicker">User</span>
          <h3>{{ form.id ? '编辑用户' : '新增用户' }}</h3>
        </div>
        <button class="workspace-modal-close" type="button" aria-label="关闭弹窗" @click="handleClose">×</button>
      </header>

      <div class="workspace-modal-body">
        <div class="workspace-form-grid">
          <label class="field">
            <span class="field-label">登录用户名</span>
            <input
              v-model.trim="form.username"
              class="input"
              :class="{ 'has-error': errors.username }"
              placeholder="例如：resident_01"
              @input="clearFieldError('username')"
            />
            <span v-if="errors.username" class="field-error-text">{{ errors.username }}</span>
          </label>
          <label class="field">
            <span class="field-label">用户昵称</span>
            <input
              v-model.trim="form.nickname"
              class="input"
              :class="{ 'has-error': errors.nickname }"
              placeholder="例如：张三"
              @input="clearFieldError('nickname')"
            />
            <span v-if="errors.nickname" class="field-error-text">{{ errors.nickname }}</span>
          </label>
        </div>

        <div class="workspace-form-grid">
          <label class="field">
            <span class="field-label">登录密码</span>
            <input
              v-model.trim="form.password"
              type="password"
              autocomplete="new-password"
              class="input"
              :class="{ 'has-error': errors.password }"
              :placeholder="form.id ? '留空表示不修改密码' : '至少 6 位'"
              @input="clearFieldError('password')"
            />
            <span v-if="errors.password" class="field-error-text">{{ errors.password }}</span>
          </label>
          <label class="field">
            <span class="field-label">角色</span>
            <select
              v-model="form.role"
              class="select"
              :class="{ 'has-error': errors.role }"
              @change="clearFieldError('role')"
            >
              <option value="RESIDENT">居民（RESIDENT）</option>
              <option value="ADMIN">管理员（ADMIN）</option>
            </select>
            <span v-if="errors.role" class="field-error-text">{{ errors.role }}</span>
          </label>
        </div>
      </div>

      <footer class="workspace-actions workspace-modal-actions">
        <button class="btn" type="button" @click="handleSubmit">
          {{ form.id ? '保存更新' : '确认新增' }}
        </button>
        <button class="btn secondary" type="button" @click="handleClose">取消</button>
      </footer>
    </section>
  </div>
</template>

<script setup>
import { reactive, watch } from 'vue'

const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  form: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'submit'])

const errors = reactive({
  username: '',
  nickname: '',
  password: '',
  role: ''
})

function clearErrors() {
  Object.keys(errors).forEach((key) => {
    errors[key] = ''
  })
}

function clearFieldError(field) {
  errors[field] = ''
}

function validateForm() {
  if (!String(props.form.username || '').trim()) {
    errors.username = '请输入登录用户名'
  }
  if (!String(props.form.nickname || '').trim()) {
    errors.nickname = '请输入用户昵称'
  }
  if (!String(props.form.role || '').trim()) {
    errors.role = '请选择角色'
  }

  const password = String(props.form.password || '').trim()
  const isEditing = Boolean(props.form.id)
  if (!isEditing && !password) {
    errors.password = '请输入登录密码'
  } else if (password && password.length < 6) {
    errors.password = '登录密码至少 6 位'
  }

  return !Object.values(errors).some(Boolean)
}

function handleClose() {
  clearErrors()
  emit('close')
}

function handleSubmit() {
  clearErrors()
  if (!validateForm()) {
    return
  }
  emit('submit')
}

watch(
  () => props.open,
  () => {
    clearErrors()
  }
)

</script>
