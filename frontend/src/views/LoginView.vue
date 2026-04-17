<template>
  <main class="auth-shell">
    <section class="auth-stage">
      <div class="auth-story">
        <span class="eyebrow">Low Carbon Circle</span>
        <h1 class="auth-title">把社区的每一次低碳行动，变成看得见的公共价值。</h1>
        <p class="auth-copy">
          这是一个围绕绿色出行、社区分类、积分兑换与运营治理构建的低碳生活平台。
          居民端强调行动反馈与参与感，管理端聚焦审核效率与运营洞察。
        </p>

        <div class="auth-highlight-grid">
          <article class="auth-highlight">
            <span>行为闭环</span>
            <strong>上报 · 审核 · 发放 · 兑换</strong>
            <p>把零散记录串成完整的社区激励链路。</p>
          </article>
          <article class="auth-highlight">
            <span>数据感知</span>
            <strong>积分、减碳、排行与订单</strong>
            <p>实时展示每位居民和每个运营节点的变化。</p>
          </article>
          <article class="auth-highlight">
            <span>双端协同</span>
            <strong>居民体验与管理员治理并重</strong>
            <p>统一视觉系统下，兼顾参与动力与管理清晰度。</p>
          </article>
        </div>

        <div class="auth-note-card">
          <div>
            <span class="auth-note-label">演示账号</span>
            <strong>管理员：admin / admin123</strong>
            <p>居民：alice / 123456，bob / 123456</p>
          </div>
          <div class="auth-note-badge">生态社区</div>
        </div>
      </div>

      <section class="auth-panel">
        <div class="auth-panel-head">
          <span class="eyebrow">Access</span>
          <h2>{{ isLoginMode ? '欢迎回来' : '创建居民账号' }}</h2>
          <p>
            {{ isLoginMode ? '登录后进入居民端或管理员端。' : '注册成功后会自动进入居民端开始记录低碳行动。' }}
          </p>
        </div>

        <nav class="auth-tabs" aria-label="认证模式切换">
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
        </nav>

        <form class="field-group" @submit.prevent="isLoginMode ? login() : register()">
          <template v-if="isLoginMode">
            <label class="field">
              <span class="field-label">用户名</span>
              <input
                v-model.trim="loginForm.username"
                class="input"
                placeholder="请输入用户名"
                autocomplete="username"
              />
            </label>
            <label class="field">
              <span class="field-label">密码</span>
              <input
                v-model.trim="loginForm.password"
                type="password"
                class="input"
                placeholder="请输入密码"
                autocomplete="current-password"
              />
            </label>
          </template>

          <template v-else>
            <label class="field">
              <span class="field-label">用户名</span>
              <input
                v-model.trim="registerForm.username"
                class="input"
                placeholder="3-50 位用户名"
                autocomplete="username"
              />
            </label>
            <label class="field">
              <span class="field-label">昵称</span>
              <input
                v-model.trim="registerForm.nickname"
                class="input"
                placeholder="2-50 位昵称"
                autocomplete="nickname"
              />
            </label>
            <label class="field">
              <span class="field-label">密码</span>
              <input
                v-model.trim="registerForm.password"
                type="password"
                class="input"
                placeholder="至少 6 位"
                autocomplete="new-password"
              />
            </label>
            <label class="field">
              <span class="field-label">确认密码</span>
              <input
                v-model.trim="registerForm.confirmPassword"
                type="password"
                class="input"
                placeholder="再次输入密码"
                autocomplete="new-password"
              />
            </label>
          </template>

          <div v-if="error" class="inline-message error">{{ error }}</div>

          <div class="auth-actions">
            <button class="btn" type="submit" :disabled="loading">
              {{ isLoginMode ? (loading ? '登录中...' : '进入平台') : (loading ? '注册中...' : '注册并进入') }}
            </button>
          </div>
        </form>
      </section>
    </section>
  </main>
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
