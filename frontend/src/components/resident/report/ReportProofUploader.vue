<template>
  <div class="workspace-grid-2">
    <label class="field">
      <span class="field-label">上传凭证图片</span>
      <input
        :ref="setImageInputRef"
        type="file"
        class="input proof-image-input"
        accept="image/*"
        @change="emit('proof-image-change', $event)"
      />
      <span class="field-help">支持 jpg / png / webp，单张建议控制在 5MB 以内。</span>
    </label>

    <div class="field">
      <span class="field-label">图片预览</span>
      <div class="workspace-upload-preview">
        <img
          v-if="proofImagePreviewUrl"
          :src="proofImagePreviewUrl"
          alt="凭证预览"
          @click="emit('open-image', proofImagePreviewUrl)"
        />
        <div v-else class="workspace-empty">上传后会在这里显示预览</div>
      </div>
      <div class="workspace-actions">
        <button class="btn secondary" type="button" @click="emit('clear-proof-image')" :disabled="uploadingImage">移除图片</button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  setImageInputRef: {
    type: Function,
    default: null
  },
  proofImagePreviewUrl: {
    type: String,
    default: ''
  },
  uploadingImage: {
    type: Boolean,
    required: true
  }
})

const emit = defineEmits(['proof-image-change', 'clear-proof-image', 'open-image'])
</script>
