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

    <div v-if="itemDialogOpen" class="workspace-modal-mask" @click.self="emit('close-item-dialog')">
      <section class="workspace-modal-card">
        <header class="workspace-modal-head">
          <div>
            <span class="workspace-kicker">Item</span>
            <h3>{{ itemForm.id ? '编辑商品' : '新增商品' }}</h3>
          </div>
          <button class="workspace-modal-close" type="button" aria-label="关闭弹窗" @click="emit('close-item-dialog')">×</button>
        </header>

        <div class="workspace-modal-body">
          <div class="workspace-form-grid">
            <label class="field">
              <span class="field-label">商品名称</span>
              <input
                v-model.trim="itemForm.name"
                class="input"
                :class="{ 'has-error': itemFormErrors.name }"
                placeholder="例如：环保帆布袋"
                @input="emit('clear-item-field-error', 'name')"
              />
              <span v-if="itemFormErrors.name" class="field-error-text">{{ itemFormErrors.name }}</span>
            </label>
            <label class="field">
              <span class="field-label">兑换积分单价</span>
              <input
                v-model.number="itemForm.pointsCost"
                type="number"
                min="1"
                class="input"
                :class="{ 'has-error': itemFormErrors.pointsCost }"
                @input="emit('clear-item-field-error', 'pointsCost')"
              />
              <span v-if="itemFormErrors.pointsCost" class="field-error-text">{{ itemFormErrors.pointsCost }}</span>
            </label>
          </div>

          <div class="workspace-form-grid">
            <label class="field">
              <span class="field-label">库存数量</span>
              <input
                v-model.number="itemForm.stock"
                type="number"
                min="0"
                class="input"
                :class="{ 'has-error': itemFormErrors.stock }"
                @input="emit('clear-item-field-error', 'stock')"
              />
              <span v-if="itemFormErrors.stock" class="field-error-text">{{ itemFormErrors.stock }}</span>
            </label>
            <label class="field">
              <span class="field-label">上架状态</span>
              <select v-model="itemForm.enabled" class="select">
                <option :value="true">上架</option>
                <option :value="false">下架</option>
              </select>
            </label>
          </div>

          <label class="field">
            <span class="field-label">商品说明</span>
            <textarea
              v-model.trim="itemForm.description"
              class="textarea"
              placeholder="例如：适合居民日常重复使用的环保生活用品。"
            ></textarea>
          </label>
        </div>

        <footer class="workspace-actions workspace-modal-actions">
          <button class="btn" type="button" @click="emit('submit-item-dialog')">
            {{ itemForm.id ? '保存更新' : '确认新增' }}
          </button>
          <button class="btn secondary" type="button" @click="emit('close-item-dialog')">取消</button>
        </footer>
      </section>
    </div>
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
  },
  itemDialogOpen: {
    type: Boolean,
    default: false
  },
  itemForm: {
    type: Object,
    required: true
  },
  itemFormErrors: {
    type: Object,
    required: true
  }
})

const emit = defineEmits([
  'update:itemsPage',
  'create-item',
  'edit-item',
  'delete-item',
  'close-item-dialog',
  'submit-item-dialog',
  'clear-item-field-error'
])
</script>
