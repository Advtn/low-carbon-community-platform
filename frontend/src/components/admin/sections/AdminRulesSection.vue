<template>
  <section class="workspace-pane">
    <div class="pane-head">
      <div>
        <span class="workspace-kicker">Rules</span>
        <h2>行为规则配置</h2>
        <p>维护积分、减碳量、日上限与启停状态，形成居民端的激励逻辑。</p>
      </div>
      <div class="workspace-actions">
        <span class="badge">{{ rules.length }} 条规则</span>
        <button class="btn" @click="emit('create-rule')">新增规则</button>
      </div>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>积分</th>
            <th>减碳</th>
            <th>日限</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!pagedRules.length">
            <td colspan="7" class="workspace-table-empty">暂无规则数据</td>
          </tr>
          <tr v-for="rule in pagedRules" :key="rule.id">
            <td>{{ rule.id }}</td>
            <td>{{ rule.name }}</td>
            <td>{{ rule.pointsPerAction }}</td>
            <td>{{ rule.carbonReductionPerAction }}</td>
            <td>{{ rule.dailyLimit }}</td>
            <td><span :class="rule.active ? 'badge' : 'badge err'">{{ rule.active ? '启用' : '停用' }}</span></td>
            <td>
              <div class="workspace-actions admin-mini-actions">
                <button class="btn secondary" @click="emit('edit-rule', rule)">编辑</button>
                <button class="btn danger" @click="emit('delete-rule', rule.id)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <AppPagination
      v-if="rules.length > rulesPageSize"
      :current-page="rulesPage"
      :total-items="rules.length"
      :page-size="rulesPageSize"
      @change="emit('update:rulesPage', $event)"
    />
  </section>
</template>

<script setup>
import AppPagination from '../../AppPagination.vue'

defineProps({
  rules: {
    type: Array,
    default: () => []
  },
  pagedRules: {
    type: Array,
    default: () => []
  },
  rulesPageSize: {
    type: Number,
    required: true
  },
  rulesPage: {
    type: Number,
    required: true
  }
})

const emit = defineEmits([
  'update:rulesPage',
  'create-rule',
  'edit-rule',
  'delete-rule'
])
</script>
