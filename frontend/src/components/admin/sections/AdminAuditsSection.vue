<template>
  <section class="workspace-pane">
    <div class="pane-head">
      <div>
        <span class="workspace-kicker">Audit</span>
        <h2>待审核上报</h2>
        <p>集中查看居民提交的行为说明、凭证图片与时间，快速完成通过或驳回。</p>
      </div>
      <span class="badge warn">{{ pendingReports.length }} 条待处理</span>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>居民</th>
            <th>规则</th>
            <th>次数</th>
            <th>凭证说明</th>
            <th>凭证图片</th>
            <th>提交时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!pagedPendingReports.length">
            <td colspan="8" class="workspace-table-empty">当前没有待审核上报</td>
          </tr>
          <tr v-for="row in pagedPendingReports" :key="row.id">
            <td>{{ row.id }}</td>
            <td>{{ row.nickname }}</td>
            <td>{{ row.ruleName }}</td>
            <td>{{ row.quantity }}</td>
            <td>{{ row.proofText || '-' }}</td>
            <td>
              <img
                v-if="row.proofImageUrl"
                :src="resolveImageUrl(row.proofImageUrl)"
                class="media-thumb"
                alt="凭证图片"
                @click="emit('open-image', resolveImageUrl(row.proofImageUrl))"
              />
              <span v-else>-</span>
            </td>
            <td>{{ fmt(row.submittedAt) }}</td>
            <td>
              <div class="workspace-actions admin-mini-actions">
                <button class="btn" @click="emit('audit', row.id, true)">通过</button>
                <button class="btn danger" @click="emit('audit', row.id, false)">驳回</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <AppPagination
      v-if="pendingReports.length > pendingReportsPageSize"
      :current-page="pendingReportsPage"
      :total-items="pendingReports.length"
      :page-size="pendingReportsPageSize"
      @change="emit('update:pendingReportsPage', $event)"
    />
  </section>
</template>

<script setup>
import AppPagination from '../../AppPagination.vue'

defineProps({
  pendingReports: {
    type: Array,
    default: () => []
  },
  pagedPendingReports: {
    type: Array,
    default: () => []
  },
  pendingReportsPageSize: {
    type: Number,
    required: true
  },
  pendingReportsPage: {
    type: Number,
    required: true
  },
  resolveImageUrl: {
    type: Function,
    required: true
  },
  fmt: {
    type: Function,
    required: true
  }
})

const emit = defineEmits(['update:pendingReportsPage', 'open-image', 'audit'])
</script>
