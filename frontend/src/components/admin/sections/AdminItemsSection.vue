<template>
  <section class="workspace-pane">
    <div class="pane-head">
      <div>
        <span class="workspace-kicker">Mall</span>
        <h2>商城商品管理</h2>
        <p>在统一工作区里维护商品、库存和上下架状态，让兑换端与库存治理同步更新。</p>
      </div>
      <div class="workspace-actions">
        <div class="workspace-inline-stats admin-items-summary">
          <div class="stat">
            <span>商品总数</span>
            <strong>{{ items.length }}</strong>
          </div>
          <div class="stat">
            <span>上架商品</span>
            <strong>{{ enabledItemCount }}</strong>
          </div>
          <div class="stat">
            <span>总库存</span>
            <strong>{{ totalItemStock }}</strong>
          </div>
        </div>
        <button class="btn" @click="emit('create-item')">新增商品</button>
      </div>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>单价</th>
            <th>库存</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!pagedItems.length">
            <td colspan="6" class="workspace-table-empty">暂无商品数据</td>
          </tr>
          <tr v-for="item in pagedItems" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.pointsCost }}</td>
            <td>{{ item.stock }}</td>
            <td><span :class="item.enabled ? 'badge' : 'badge err'">{{ item.enabled ? '上架' : '下架' }}</span></td>
            <td>
              <div class="workspace-actions admin-mini-actions">
                <button class="btn secondary" @click="emit('edit-item', item)">编辑</button>
                <button class="btn danger" @click="emit('delete-item', item.id)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <AppPagination
      v-if="items.length > itemsPageSize"
      :current-page="itemsPage"
      :total-items="items.length"
      :page-size="itemsPageSize"
      @change="emit('update:itemsPage', $event)"
    />
  </section>
</template>

<script setup>
import AppPagination from '../../AppPagination.vue'

defineProps({
  items: {
    type: Array,
    default: () => []
  },
  pagedItems: {
    type: Array,
    default: () => []
  },
  itemsPageSize: {
    type: Number,
    required: true
  },
  itemsPage: {
    type: Number,
    required: true
  },
  enabledItemCount: {
    type: Number,
    required: true
  },
  totalItemStock: {
    type: Number,
    required: true
  }
})

const emit = defineEmits([
  'update:itemsPage',
  'create-item',
  'edit-item',
  'delete-item'
])
</script>
