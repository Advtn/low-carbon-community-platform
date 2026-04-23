<template>
  <section class="workspace-pane">
    <div class="pane-head">
      <div>
        <span class="workspace-kicker">Reports</span>
        <h2>我的上报记录</h2>
        <p>集中查看规则、次数、审核状态、奖励积分、凭证图片与审核备注。</p>
      </div>
      <span class="badge">{{ reports.length }} 条记录</span>
    </div>
    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>规则</th>
            <th>次数</th>
            <th>状态</th>
            <th>奖励积分</th>
            <th>提交时间</th>
            <th>凭证</th>
            <th>审核备注</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!pagedReports.length">
            <td colspan="8" class="workspace-table-empty">暂无上报记录</td>
          </tr>
          <tr v-for="row in pagedReports" :key="row.id">
            <td>{{ row.id }}</td>
            <td>{{ row.ruleName }}</td>
            <td>{{ row.quantity }}</td>
            <td><span :class="badgeClass(row.status)">{{ formatStatusLabel(row.status) }}</span></td>
            <td>{{ row.grantedPoints ?? '-' }}</td>
            <td>{{ fmt(row.submittedAt) }}</td>
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
            <td>{{ row.auditRemark || '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <AppPagination
      v-if="reports.length > reportsPageSize"
      :current-page="reportsPage"
      :total-items="reports.length"
      :page-size="reportsPageSize"
      @change="emit('update:reportsPage', $event)"
    />
  </section>
</template>

<script setup>
import AppPagination from '../../AppPagination.vue'

defineProps({
  reports: {
    type: Array,
    default: () => []
  },
  pagedReports: {
    type: Array,
    default: () => []
  },
  reportsPageSize: {
    type: Number,
    required: true
  },
  reportsPage: {
    type: Number,
    required: true
  },
  badgeClass: {
    type: Function,
    required: true
  },
  formatStatusLabel: {
    type: Function,
    required: true
  },
  fmt: {
    type: Function,
    required: true
  },
  resolveImageUrl: {
    type: Function,
    required: true
  }
})

const emit = defineEmits(['update:reportsPage', 'open-image'])
</script>
