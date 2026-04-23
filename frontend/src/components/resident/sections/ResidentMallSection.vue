<template>
  <section class="workspace-pane">
    <div class="pane-head">
      <div>
        <span class="workspace-kicker">Mall</span>
        <h2>积分商城</h2>
        <p>从上架商品里选择想兑换的社区好物，操作会直接记录到订单与积分流水。</p>
      </div>
    </div>
    <div
      v-if="redeemMessage"
      class="inline-message"
      :class="redeemMessageType === 'success' ? 'success' : 'error'"
    >
      {{ redeemMessage }}
    </div>
    <div class="workspace-list">
      <article v-for="item in pagedItems" :key="item.id" class="workspace-list-item">
        <div>
          <strong>{{ item.name }}</strong>
          <p>{{ item.description || '环保好物，鼓励持续参与。' }}</p>
        </div>
        <div class="workspace-actions">
          <span class="badge">{{ item.pointsCost }} 分</span>
          <span class="badge warn">库存 {{ item.stock }}</span>
          <button class="btn" type="button" @click="emit('redeem', item)">兑换</button>
        </div>
      </article>
      <div v-if="!pagedItems.length" class="workspace-empty">商城暂时没有可兑换商品</div>
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
  redeemMessage: {
    type: String,
    default: ''
  },
  redeemMessageType: {
    type: String,
    default: 'error'
  },
  pagedItems: {
    type: Array,
    default: () => []
  },
  items: {
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
  }
})

const emit = defineEmits(['redeem', 'update:itemsPage'])
</script>
