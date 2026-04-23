<template>
  <section class="workspace-pane">
    <div class="pane-head">
      <div>
        <span class="workspace-kicker">Users</span>
        <h2>用户管理</h2>
        <p>创建居民与管理员账号，并对昵称、角色与密码进行维护。</p>
      </div>
      <div class="workspace-actions">
        <span class="badge">{{ users.length }} 个账号</span>
        <button class="btn" @click="emit('create-user')">新增用户</button>
      </div>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>昵称</th>
            <th>角色</th>
            <th>积分</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!pagedUsers.length">
            <td colspan="6" class="workspace-table-empty">暂无用户数据</td>
          </tr>
          <tr v-for="user in pagedUsers" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.nickname }}</td>
            <td>{{ user.role }}</td>
            <td>{{ user.totalPoints }}</td>
            <td>
              <div class="workspace-actions admin-mini-actions">
                <button class="btn secondary" @click="emit('edit-user', user)">编辑</button>
                <button class="btn danger" @click="emit('delete-user', user.id)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <AppPagination
      v-if="users.length > usersPageSize"
      :current-page="usersPage"
      :total-items="users.length"
      :page-size="usersPageSize"
      @change="emit('update:usersPage', $event)"
    />

    <div v-if="userDialogOpen" class="workspace-modal-mask" @click.self="emit('close-user-dialog')">
      <section class="workspace-modal-card">
        <header class="workspace-modal-head">
          <div>
            <span class="workspace-kicker">User</span>
            <h3>{{ userForm.id ? '编辑用户' : '新增用户' }}</h3>
          </div>
          <button class="workspace-modal-close" type="button" aria-label="关闭弹窗" @click="emit('close-user-dialog')">×</button>
        </header>

        <div class="workspace-modal-body">
          <div class="workspace-form-grid">
            <label class="field">
              <span class="field-label">登录用户名</span>
              <input
                v-model.trim="userForm.username"
                class="input"
                :class="{ 'has-error': userFormErrors.username }"
                placeholder="例如：resident_01"
                @input="emit('clear-user-field-error', 'username')"
              />
              <span v-if="userFormErrors.username" class="field-error-text">{{ userFormErrors.username }}</span>
            </label>
            <label class="field">
              <span class="field-label">用户昵称</span>
              <input
                v-model.trim="userForm.nickname"
                class="input"
                :class="{ 'has-error': userFormErrors.nickname }"
                placeholder="例如：张三"
                @input="emit('clear-user-field-error', 'nickname')"
              />
              <span v-if="userFormErrors.nickname" class="field-error-text">{{ userFormErrors.nickname }}</span>
            </label>
          </div>

          <div class="workspace-form-grid">
            <label class="field">
              <span class="field-label">登录密码</span>
              <input
                v-model.trim="userForm.password"
                class="input"
                :class="{ 'has-error': userFormErrors.password }"
                :placeholder="userForm.id ? '留空表示不修改密码' : '至少 6 位'"
                @input="emit('clear-user-field-error', 'password')"
              />
              <span v-if="userFormErrors.password" class="field-error-text">{{ userFormErrors.password }}</span>
            </label>
            <label class="field">
              <span class="field-label">角色</span>
              <select
                v-model="userForm.role"
                class="select"
                :class="{ 'has-error': userFormErrors.role }"
                @change="emit('clear-user-field-error', 'role')"
              >
                <option value="RESIDENT">居民（RESIDENT）</option>
                <option value="ADMIN">管理员（ADMIN）</option>
              </select>
              <span v-if="userFormErrors.role" class="field-error-text">{{ userFormErrors.role }}</span>
            </label>
          </div>
        </div>

        <footer class="workspace-actions workspace-modal-actions">
          <button class="btn" type="button" @click="emit('submit-user-dialog')">
            {{ userForm.id ? '保存更新' : '确认新增' }}
          </button>
          <button class="btn secondary" type="button" @click="emit('close-user-dialog')">取消</button>
        </footer>
      </section>
    </div>
  </section>
</template>

<script setup>
import AppPagination from '../../AppPagination.vue'

defineProps({
  users: {
    type: Array,
    default: () => []
  },
  pagedUsers: {
    type: Array,
    default: () => []
  },
  usersPageSize: {
    type: Number,
    required: true
  },
  usersPage: {
    type: Number,
    required: true
  },
  userDialogOpen: {
    type: Boolean,
    default: false
  },
  userForm: {
    type: Object,
    required: true
  },
  userFormErrors: {
    type: Object,
    required: true
  }
})

const emit = defineEmits([
  'update:usersPage',
  'create-user',
  'edit-user',
  'delete-user',
  'close-user-dialog',
  'submit-user-dialog',
  'clear-user-field-error'
])
</script>
