<template>
  <div v-if="open" class="workspace-modal-mask" @click.self="handleClose">
    <section class="workspace-modal-card">
      <header class="workspace-modal-head">
        <div>
          <span class="workspace-kicker">Rule</span>
          <h3>{{ form.id ? '编辑规则' : '新增规则' }}</h3>
        </div>
        <button class="workspace-modal-close" type="button" aria-label="关闭弹窗" @click="handleClose">×</button>
      </header>

      <div class="workspace-modal-body">
        <div class="workspace-form-grid">
          <label class="field">
            <span class="field-label">规则名称</span>
            <input
              v-model.trim="form.name"
              class="input"
              :class="{ 'has-error': errors.name }"
              placeholder="例如：步行或骑行出行"
              @input="clearFieldError('name')"
            />
            <span v-if="errors.name" class="field-error-text">{{ errors.name }}</span>
          </label>
          <label class="field">
            <span class="field-label">单次奖励积分</span>
            <input
              v-model.number="form.pointsPerAction"
              type="number"
              min="0"
              class="input"
              :class="{ 'has-error': errors.pointsPerAction }"
              @input="clearFieldError('pointsPerAction')"
            />
            <span v-if="errors.pointsPerAction" class="field-error-text">{{ errors.pointsPerAction }}</span>
          </label>
        </div>

        <div class="workspace-form-grid">
          <label class="field">
            <span class="field-label">单次减碳量（kg）</span>
            <input
              v-model.number="form.carbonReductionPerAction"
              type="number"
              min="0"
              step="0.1"
              class="input"
              :class="{ 'has-error': errors.carbonReductionPerAction }"
              @input="clearFieldError('carbonReductionPerAction')"
            />
            <span v-if="errors.carbonReductionPerAction" class="field-error-text">{{ errors.carbonReductionPerAction }}</span>
          </label>
          <label class="field">
            <span class="field-label">每日上报上限</span>
            <input
              v-model.number="form.dailyLimit"
              type="number"
              min="1"
              class="input"
              :class="{ 'has-error': errors.dailyLimit }"
              @input="clearFieldError('dailyLimit')"
            />
            <span v-if="errors.dailyLimit" class="field-error-text">{{ errors.dailyLimit }}</span>
          </label>
        </div>

        <div class="workspace-form-grid">
          <label class="field">
            <span class="field-label">规则状态</span>
            <select v-model="form.active" class="select">
              <option :value="true">启用</option>
              <option :value="false">停用</option>
            </select>
          </label>
          <div class="field">
            <span class="field-label">口径提醒</span>
            <span class="field-help">建议写清居民需要提供的凭证与实际行为口径。</span>
          </div>
        </div>

        <label class="field">
          <span class="field-label">规则说明</span>
          <textarea
            v-model.trim="form.description"
            class="textarea"
            placeholder="例如：通勤使用步行或骑行方式，并上传简要说明与凭证图片。"
          ></textarea>
        </label>
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
  name: '',
  pointsPerAction: '',
  carbonReductionPerAction: '',
  dailyLimit: ''
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
  if (!String(props.form.name || '').trim()) {
    errors.name = '请输入规则名称'
  }

  const pointsPerAction = Number(props.form.pointsPerAction)
  if (!Number.isFinite(pointsPerAction) || pointsPerAction < 0) {
    errors.pointsPerAction = '积分需为大于等于 0 的数字'
  }

  const carbonReductionPerAction = Number(props.form.carbonReductionPerAction)
  if (!Number.isFinite(carbonReductionPerAction) || carbonReductionPerAction < 0) {
    errors.carbonReductionPerAction = '减碳量需为大于等于 0 的数字'
  }

  const dailyLimit = Number(props.form.dailyLimit)
  if (!Number.isInteger(dailyLimit) || dailyLimit <= 0) {
    errors.dailyLimit = '每日上报上限需为正整数'
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
