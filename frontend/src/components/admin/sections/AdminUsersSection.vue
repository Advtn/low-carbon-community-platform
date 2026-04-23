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
  }
})

const emit = defineEmits([
  'update:usersPage',
  'create-user',
  'edit-user',
  'delete-user'
])
</script>
