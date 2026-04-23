<template>
  <section class="workspace-pane">
    <div class="pane-head">
      <div>
        <span class="workspace-kicker">Orders</span>
        <h2>兑换订单管理</h2>
        <p>统一处理履约与退款，避免订单状态长时间停留在待处理。</p>
      </div>
      <span class="badge">{{ orders.length }} 笔订单</span>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>居民</th>
            <th>商品</th>
            <th>数量</th>
            <th>积分</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!pagedOrders.length">
            <td colspan="8" class="workspace-table-empty">暂无订单数据</td>
          </tr>
          <tr v-for="order in pagedOrders" :key="order.id">
            <td>{{ order.id }}</td>
            <td>{{ order.nickname }}</td>
            <td>{{ order.itemName }}</td>
            <td>{{ order.quantity }}</td>
            <td>{{ order.totalPoints }}</td>
            <td><span :class="badgeClass(order.status)">{{ formatStatusLabel(order.status) }}</span></td>
            <td>{{ fmt(order.createdAt) }}</td>
            <td>
              <div v-if="order.status === 'PENDING'" class="workspace-actions admin-mini-actions">
                <button class="btn" @click="emit('update-order', order.id, 'COMPLETED')">完成</button>
                <button class="btn danger" @click="emit('update-order', order.id, 'CANCELLED')">取消并退款</button>
              </div>
              <span v-else class="admin-order-locked">已结束</span>
            </td>
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

const emit = defineEmits(['update:ordersPage', 'update-order'])
</script>
