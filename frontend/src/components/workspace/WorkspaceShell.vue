<template>
  <main class="workspace-shell">
    <div class="workspace-frame">
      <aside class="workspace-sidebar">
        <div class="workspace-brand">
          <div class="workspace-brand-mark">{{ brandMark }}</div>
          <div>
            <strong>{{ brandTitle }}</strong>
            <span>{{ brandSubtitle }}</span>
          </div>
        </div>

        <nav class="workspace-nav" :aria-label="navAriaLabel">
          <button
            v-for="section in sidebarSections"
            :key="section.id"
            class="workspace-nav-item"
            :class="{ active: activeSection === section.id }"
            @click="emit('open-section', section.id)"
          >
            <span class="workspace-nav-mark">{{ section.short }}</span>
            <span class="workspace-nav-copy">
              <strong>{{ section.label }}</strong>
              <span>{{ section.hint }}</span>
            </span>
          </button>
          <div v-if="!sidebarSections.length" class="workspace-nav-empty">
            没有匹配到模块，试试其他关键词
          </div>
        </nav>

        <div class="workspace-footer">
          <strong>{{ footerTitle }}</strong>
          <p>{{ footerText }}</p>
        </div>
      </aside>

      <section class="workspace-main">
        <header class="workspace-topbar">
          <div>
            <div class="workspace-breadcrumb">
              {{ breadcrumbPrefix }} / {{ activeSectionMeta ? activeSectionMeta.label : inactiveLabel }}
            </div>
            <h1 class="workspace-title">
              {{ activeSectionMeta ? activeSectionMeta.label : emptySelectionTitle }}
            </h1>
            <p class="workspace-subtitle">
              {{ activeSectionMeta ? activeSectionMeta.description : emptySelectionDescription }}
            </p>
          </div>

          <div class="workspace-toolbar">
            <div class="workspace-search">
              <input
                :ref="registerSearchInput"
                :value="searchKeyword"
                class="workspace-search-input"
                type="search"
                :placeholder="searchPlaceholder"
                :aria-label="searchAriaLabel"
                @input="onSearchInput"
                @keydown.enter.prevent="emit('open-first-matched-section')"
                @keydown.esc.prevent="emit('clear-section-search')"
              />
              <kbd>Ctrl K</kbd>
            </div>
            <div class="workspace-actions">
              <button class="btn secondary" @click="emit('refresh')">刷新数据</button>
            </div>
            <div
              class="workspace-avatar-wrap"
              @mouseenter="emit('update:avatarMenuOpen', true)"
              @mouseleave="emit('update:avatarMenuOpen', false)"
            >
              <button class="workspace-avatar" @click="emit('update:avatarMenuOpen', !avatarMenuOpen)">
                <span class="workspace-avatar-image workspace-avatar-photo">
                  <img :src="profileAvatarUrl" alt="头像" />
                </span>
              </button>

              <div v-if="avatarMenuOpen" class="workspace-avatar-menu">
                <div class="workspace-avatar-header">
                  <div class="workspace-avatar-image workspace-avatar-photo">
                    <img :src="profileAvatarUrl" alt="头像" />
                  </div>
                  <div class="workspace-avatar-meta">
                    <strong>{{ profileName }}</strong>
                    <span>{{ profileRole }}</span>
                  </div>
                </div>

                <div class="workspace-avatar-actions">
                  <button class="workspace-avatar-action" @click="emit('open-profile')">
                    <span class="workspace-avatar-icon">人</span>
                    <span>个人中心</span>
                  </button>
                  <button class="workspace-avatar-action danger" @click="emit('logout')">
                    <span class="workspace-avatar-icon">退</span>
                    <span>退出登录</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </header>

        <div class="workspace-tabs">
          <button
            v-for="section in tabSections"
            :key="section.id"
            class="workspace-tab"
            :class="{ active: activeSection === section.id }"
            @click="emit('update:activeSection', section.id)"
          >
            <span class="workspace-tab-label">{{ section.label }}</span>
            <span class="workspace-tab-close" @click.stop="emit('close-section', section.id)">×</span>
          </button>
        </div>

        <div class="workspace-content">
          <slot name="feedback" />

          <div v-if="!activeSection" class="workspace-empty-stage">
            <span class="workspace-kicker">{{ emptyStateKicker }}</span>
            <h2>{{ emptyStateTitle }}</h2>
            <p>{{ emptyStateDescription }}</p>
          </div>

          <slot />
        </div>
      </section>
    </div>
  </main>
</template>

<script setup>
defineProps({
  brandMark: {
    type: String,
    required: true
  },
  brandTitle: {
    type: String,
    required: true
  },
  brandSubtitle: {
    type: String,
    required: true
  },
  navAriaLabel: {
    type: String,
    required: true
  },
  sidebarSections: {
    type: Array,
    default: () => []
  },
  activeSection: {
    type: String,
    default: null
  },
  activeSectionMeta: {
    type: Object,
    default: null
  },
  searchKeyword: {
    type: String,
    default: ''
  },
  registerSearchInput: {
    type: Function,
    default: null
  },
  tabSections: {
    type: Array,
    default: () => []
  },
  avatarMenuOpen: {
    type: Boolean,
    default: false
  },
  profileAvatarUrl: {
    type: String,
    required: true
  },
  profileName: {
    type: String,
    required: true
  },
  profileRole: {
    type: String,
    required: true
  },
  footerTitle: {
    type: String,
    required: true
  },
  footerText: {
    type: String,
    required: true
  },
  breadcrumbPrefix: {
    type: String,
    required: true
  },
  inactiveLabel: {
    type: String,
    default: '未打开模块'
  },
  emptySelectionTitle: {
    type: String,
    required: true
  },
  emptySelectionDescription: {
    type: String,
    required: true
  },
  searchPlaceholder: {
    type: String,
    default: '搜索模块，回车打开首个匹配项'
  },
  searchAriaLabel: {
    type: String,
    required: true
  },
  emptyStateKicker: {
    type: String,
    default: 'Workspace'
  },
  emptyStateTitle: {
    type: String,
    required: true
  },
  emptyStateDescription: {
    type: String,
    required: true
  }
})

const emit = defineEmits([
  'update:searchKeyword',
  'update:activeSection',
  'update:avatarMenuOpen',
  'open-section',
  'close-section',
  'clear-section-search',
  'open-first-matched-section',
  'refresh',
  'open-profile',
  'logout'
])

function onSearchInput(event) {
  emit('update:searchKeyword', event.target.value.trim())
}
</script>
