<template>
  <div class="login-page">
    <div class="card login-card">
      <h1 class="title login-title">社区低碳生活积分平台</h1>
      <p class="subtitle">毕业设计演示系统 · SpringBoot + Vue</p>

      <div class="login-field">
        <input v-model.trim="form.username" class="input" placeholder="用户名" />
      </div>
      <div class="login-field compact">
        <input v-model.trim="form.password" type="password" class="input" placeholder="密码" />
      </div>

      <div v-if="error" class="message">{{ error }}</div>

      <button class="btn login-btn" @click="login" :disabled="loading">
        {{ loading ? '登录中...' : '登录' }}
      </button>

      <p class="notice">
        演示账号：<br />
        管理员：admin / admin123<br />
        居民：alice / 123456 或 bob / 123456
      </p>
    </div>
  </div>
</template>

<script setup>
import { useLoginPage } from '../composables/useLoginPage'

const { form, loading, error, login } = useLoginPage()
</script>

<style scoped src="../styles/login-view.css"></style>
