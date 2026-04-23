<template>
  <div v-if="open" class="workspace-modal-mask" @click.self="handleClose">
    <section class="workspace-modal-card">
      <header class="workspace-modal-head">
        <div>
          <span class="workspace-kicker">Item</span>
          <h3>{{ form.id ? '编辑商品' : '新增商品' }}</h3>
        </div>
        <button class="workspace-modal-close" type="button" aria-label="关闭弹窗" @click="handleClose">×</button>
      </header>

      <div class="workspace-modal-body">
        <div class="workspace-form-grid">
          <label class="field">
            <span class="field-label">商品名称</span>
            <input
              v-model.trim="form.name"
              class="input"
              :class="{ 'has-error': errors.name }"
              placeholder="例如：环保帆布袋"
              @input="clearFieldError('name')"
            />
            <span v-if="errors.name" class="field-error-text">{{ errors.name }}</span>
          </label>
          <label class="field">
            <span class="field-label">兑换积分单价</span>
            <input
              v-model.number="form.pointsCost"
              type="number"
              min="1"
              class="input"
              :class="{ 'has-error': errors.pointsCost }"
              @input="clearFieldError('pointsCost')"
            />
            <span v-if="errors.pointsCost" class="field-error-text">{{ errors.pointsCost }}</span>
          </label>
        </div>

        <div class="workspace-form-grid">
          <label class="field">
            <span class="field-label">库存数量</span>
            <input
              v-model.number="form.stock"
              type="number"
              min="0"
              class="input"
              :class="{ 'has-error': errors.stock }"
              @input="clearFieldError('stock')"
            />
            <span v-if="errors.stock" class="field-error-text">{{ errors.stock }}</span>
          </label>
          <label class="field">
            <span class="field-label">上架状态</span>
            <select v-model="form.enabled" class="select">
              <option :value="true">上架</option>
              <option :value="false">下架</option>
            </select>
          </label>
        </div>

        <label class="field">
          <span class="field-label">商品说明</span>
          <textarea
            v-model.trim="form.description"
            class="textarea"
            placeholder="例如：适合居民日常重复使用的环保生活用品。"
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
  pointsCost: '',
  stock: ''
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
    errors.name = '请输入商品名称'
  }

  const pointsCost = Number(props.form.pointsCost)
  if (!Number.isFinite(pointsCost) || pointsCost <= 0) {
    errors.pointsCost = '积分单价需为大于 0 的数字'
  }

  const stock = Number(props.form.stock)
  if (!Number.isInteger(stock) || stock < 0) {
    errors.stock = '库存需为大于等于 0 的整数'
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
