<template>
  <section class="workspace-pane">
    <div class="pane-head">
      <div>
        <span class="workspace-kicker">Orders</span>
        <h2>我的兑换订单</h2>
        <p>查看兑换商品、积分消耗与当前履约状态。</p>
      </div>
      <span class="badge">{{ orders.length }} 笔订单</span>
    </div>
    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>商品</th>
            <th>数量</th>
            <th>积分</th>
            <th>状态</th>
            <th>创建时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!pagedOrders.length">
            <td colspan="6" class="workspace-table-empty">暂无兑换订单</td>
          </tr>
          <tr v-for="o in pagedOrders" :key="o.id">
            <td>{{ o.id }}</td>
            <td>{{ o.itemName }}</td>
            <td>{{ o.quantity }}</td>
            <td>{{ o.totalPoints }}</td>
            <td><span :class="badgeClass(o.status)">{{ formatStatusLabel(o.status) }}</span></td>
            <td>{{ fmt(o.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <AppPagination
      v-if="orders.length > ordersPageSize"
      :current-page="ordersPage"
      :total-items="orders.length"
      :page-size="ordersPageSize"
      @change="emit('update:ordersPage', $event)"
    />
  </section>
</template>

<script setup>
import AppPagination from '../../AppPagination.vue'

defineProps({
  orders: {
    type: Array,
    default: () => []
  },
  pagedOrders: {
    type: Array,
    default: () => []
  },
  ordersPageSize: {
    type: Number,
    required: true
  },
  ordersPage: {
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
  }
})

const emit = defineEmits(['update:ordersPage'])
</script>
