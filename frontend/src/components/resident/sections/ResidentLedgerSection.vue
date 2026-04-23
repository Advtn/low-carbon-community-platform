<template>
  <section class="workspace-pane">
    <div class="pane-head">
      <div>
        <span class="workspace-kicker">Ledger</span>
        <h2>积分流水</h2>
        <p>查看每一笔奖励、兑换与退款带来的积分变化。</p>
      </div>
    </div>
    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>变动</th>
            <th>类型</th>
            <th>余额</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!pagedLedger.length">
            <td colspan="4" class="workspace-table-empty">暂无积分流水</td>
          </tr>
          <tr v-for="row in pagedLedger" :key="row.id">
            <td>{{ row.changePoints > 0 ? '+' : '' }}{{ row.changePoints }}</td>
            <td>{{ formatLedgerType(row.type) }}</td>
            <td>{{ row.balanceAfter }}</td>
            <td>{{ fmt(row.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <AppPagination
      v-if="ledger.length > ledgerPageSize"
      :current-page="ledgerPage"
      :total-items="ledger.length"
      :page-size="ledgerPageSize"
      @change="emit('update:ledgerPage', $event)"
    />
  </section>
</template>

<script setup>
import AppPagination from '../../AppPagination.vue'

defineProps({
  ledger: {
    type: Array,
    default: () => []
  },
  pagedLedger: {
    type: Array,
    default: () => []
  },
  ledgerPageSize: {
    type: Number,
    required: true
  },
  ledgerPage: {
    type: Number,
    required: true
  },
  formatLedgerType: {
    type: Function,
    required: true
  },
  fmt: {
    type: Function,
    required: true
  }
})

const emit = defineEmits(['update:ledgerPage'])
</script>
