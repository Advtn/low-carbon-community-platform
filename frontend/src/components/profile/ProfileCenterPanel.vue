<template>
  <section class="workspace-pane">
    <div class="pane-head">
      <div>
        <span class="workspace-kicker">Profile</span>
        <h2>个人中心</h2>
        <p>{{ descriptionText }}</p>
      </div>
      <button class="btn" @click="emit('save')">保存资料</button>
    </div>

    <div
      v-if="saveMessage"
      class="inline-message"
      :class="saveType === 'success' ? 'success' : 'error'"
    >
      {{ saveMessage }}
    </div>

    <div class="profile-center">
      <section class="profile-card">
        <div class="profile-cover"></div>
        <div class="profile-body">
          <div class="profile-avatar-wrap">
            <div class="profile-avatar profile-avatar-photo">
              <img :src="avatarUrl" alt="个人头像" />
            </div>
            <div class="profile-avatar-upload">
              <input
                :ref="setAvatarInputRef"
                class="profile-avatar-input"
                type="file"
                accept="image/*"
                @change="emit('avatar-change', $event)"
              />
              <button
                class="btn ghost profile-avatar-btn"
                type="button"
                :disabled="avatarUploading"
                @click="emit('trigger-avatar-upload')"
              >
                {{ avatarUploading ? '上传中...' : '上传头像' }}
              </button>
            </div>
          </div>
          <h3 class="profile-name">{{ form.name }}</h3>
          <p class="profile-desc">{{ form.bio }}</p>

          <div class="profile-list">
            <div class="profile-list-item">
              <span>邮</span>
              <strong>{{ form.email }}</strong>
            </div>
            <div class="profile-list-item">
              <span>{{ roleMetaMark }}</span>
              <strong>{{ form.role }}</strong>
            </div>
            <div class="profile-list-item">
              <span>城</span>
              <strong>{{ form.city }}</strong>
            </div>
            <div class="profile-list-item">
              <span>组</span>
              <strong>{{ form.organization }}</strong>
            </div>
          </div>

          <div class="profile-tags">
            <span v-for="tag in form.tags" :key="tag" class="profile-tag">{{ tag }}</span>
          </div>
        </div>
      </section>

      <section class="profile-panel">
        <div class="profile-panel-head">
          <h3>基本设置</h3>
        </div>
        <div class="profile-panel-body">
          <div class="workspace-form">
            <div class="workspace-form-grid">
              <label class="field">
                <span class="field-label">姓名</span>
                <input v-model="form.name" class="input" />
              </label>
              <label class="field">
                <span class="field-label">性别</span>
                <select v-model="form.gender" class="select">
                  <option value="女">女</option>
                  <option value="男">男</option>
                  <option value="保密">保密</option>
                </select>
              </label>
            </div>

            <div class="workspace-form-grid">
              <label class="field">
                <span class="field-label">昵称</span>
                <input v-model="form.nickname" class="input" />
              </label>
              <label class="field">
                <span class="field-label">邮箱</span>
                <input v-model="form.email" class="input" />
              </label>
            </div>

            <div class="workspace-form-grid">
              <label class="field">
                <span class="field-label">手机</span>
                <input v-model="form.phone" class="input" />
              </label>
              <label class="field">
                <span class="field-label">地址</span>
                <input v-model="form.address" class="input" />
              </label>
            </div>

            <label class="field">
              <span class="field-label">个人介绍</span>
              <textarea v-model="form.bio" class="textarea"></textarea>
            </label>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
defineProps({
  descriptionText: {
    type: String,
    required: true
  },
  form: {
    type: Object,
    required: true
  },
  avatarUrl: {
    type: String,
    required: true
  },
  saveMessage: {
    type: String,
    default: ''
  },
  saveType: {
    type: String,
    default: 'success'
  },
  avatarUploading: {
    type: Boolean,
    default: false
  },
  setAvatarInputRef: {
    type: Function,
    default: null
  },
  roleMetaMark: {
    type: String,
    default: '岗'
  }
})

const emit = defineEmits(['save', 'trigger-avatar-upload', 'avatar-change'])
</script>
