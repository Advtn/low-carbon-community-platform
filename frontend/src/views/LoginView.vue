<template>
  <div class="login-page">
    <div class="card login-card">
      <h1 class="title login-title">社区低碳生活积分平台</h1>
      <p class="subtitle">毕业设计演示系统 · SpringBoot + Vue</p>

      <div class="auth-tabs">
        <button
          class="tab-btn"
          :class="{ active: isLoginMode }"
          @click="switchMode('login')"
          :disabled="loading"
        >
          登录
        </button>
        <button
          class="tab-btn"
          :class="{ active: !isLoginMode }"
          @click="switchMode('register')"
          :disabled="loading"
        >
          注册
        </button>
      </div>

      <template v-if="isLoginMode">
        <div class="login-field">
          <input v-model.trim="loginForm.username" class="input" placeholder="用户名" />
        </div>
        <div class="login-field compact">
          <input v-model.trim="loginForm.password" type="password" class="input" placeholder="密码" />
        </div>
      </template>

      <template v-else>
        <div class="login-field">
          <input v-model.trim="registerForm.username" class="input" placeholder="用户名（3-50位）" />
        </div>
        <div class="login-field compact">
          <input v-model.trim="registerForm.nickname" class="input" placeholder="昵称（2-50位）" />
        </div>
        <div class="login-field compact">
          <input v-model.trim="registerForm.password" type="password" class="input" placeholder="密码（至少6位）" />
        </div>
        <div class="login-field compact">
          <input
            v-model.trim="registerForm.confirmPassword"
            type="password"
            class="input"
            placeholder="确认密码"
          />
        </div>
      </template>

      <div v-if="error" class="message">{{ error }}</div>

      <button v-if="isLoginMode" class="btn login-btn" @click="login" :disabled="loading">
        {{ loading ? '登录中...' : '登录' }}
      </button>
      <button v-else class="btn login-btn" @click="register" :disabled="loading">
        {{ loading ? '注册中...' : '注册并进入居民端' }}
      </button>

      <p class="notice" v-if="isLoginMode">
        演示账号：<br />
        管理员：admin / admin123<br />
        居民：alice / 123456 或 bob / 123456
      </p>
      <p class="notice" v-else>
        注册为居民账号，注册成功后会自动登录并进入居民端。
      </p>
    </div>
  </div>
</template>

<script setup>
import { useLoginPage } from '../composables/useLoginPage'

const {
  loading,
  error,
  isLoginMode,
  loginForm,
  registerForm,
  switchMode,
  login,
  register
} = useLoginPage()
</script>

<style scoped src="../styles/login-view.css"></style>

