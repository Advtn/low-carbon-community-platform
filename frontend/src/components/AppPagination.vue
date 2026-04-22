<template>
  <nav class="pagination" aria-label="分页导航">
    <span class="pagination-meta">显示 {{ rangeStart }}-{{ rangeEnd }} / {{ totalItems }}</span>

    <button
      class="pagination-btn pagination-nav"
      :disabled="safeCurrentPage <= 1"
      aria-label="上一页"
      @click="$emit('change', safeCurrentPage - 1)"
    >
      上一页
    </button>

    <span v-for="item in pageItems" :key="item.key" class="pagination-item">
      <button
        v-if="item.type === 'page'"
        class="pagination-btn"
        :class="{ active: item.value === safeCurrentPage }"
        :aria-current="item.value === safeCurrentPage ? 'page' : undefined"
        :aria-label="`第 ${item.value} 页`"
        @click="$emit('change', item.value)"
      >
        {{ item.value }}
      </button>
      <span v-else class="pagination-ellipsis" aria-hidden="true">…</span>
    </span>

    <button
      class="pagination-btn pagination-nav"
      :disabled="safeCurrentPage >= totalPages"
      aria-label="下一页"
      @click="$emit('change', safeCurrentPage + 1)"
    >
      下一页
    </button>
  </nav>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  currentPage: {
    type: Number,
    required: true
  },
  totalItems: {
    type: Number,
    required: true
  },
  pageSize: {
    type: Number,
    required: true
  },
  maxVisible: {
    type: Number,
    default: 7
  }
})

defineEmits(['change'])

const totalPages = computed(() => Math.max(1, Math.ceil(props.totalItems / props.pageSize)))
const safeCurrentPage = computed(() =>
  Math.min(Math.max(props.currentPage || 1, 1), totalPages.value)
)
const rangeStart = computed(() =>
  props.totalItems === 0 ? 0 : (safeCurrentPage.value - 1) * props.pageSize + 1
)
const rangeEnd = computed(() =>
  props.totalItems === 0 ? 0 : Math.min(props.totalItems, safeCurrentPage.value * props.pageSize)
)

function createPageItem(page) {
  return {
    key: `page-${page}`,
    type: 'page',
    value: page
  }
}

function createEllipsisItem(position) {
  return {
    key: `ellipsis-${position}`,
    type: 'ellipsis'
  }
}

const pageItems = computed(() => {
  const pages = totalPages.value
  const current = safeCurrentPage.value
  const visibleCount = Math.max(5, props.maxVisible)

  if (pages <= visibleCount) {
    return Array.from({ length: pages }, (_, index) => createPageItem(index + 1))
  }

  const items = [createPageItem(1)]
  let start = Math.max(2, current - 1)
  let end = Math.min(pages - 1, current + 1)

  if (current <= 3) {
    start = 2
    end = 4
  }

  if (current >= pages - 2) {
    start = pages - 3
    end = pages - 1
  }

  if (start > 2) {
    items.push(createEllipsisItem('left'))
  }

  for (let page = start; page <= end; page += 1) {
    items.push(createPageItem(page))
  }

  if (end < pages - 1) {
    items.push(createEllipsisItem('right'))
  }

  items.push(createPageItem(pages))
  return items
})
</script>

<style scoped>
.pagination {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 18px;
}

.pagination-meta {
  margin-right: auto;
  color: #5d6f66;
  font-size: 12px;
}

.pagination-btn {
  min-width: 38px;
  height: 38px;
  padding: 0 12px;
  border: 1px solid rgba(23, 53, 45, 0.1);
  border-radius: 999px;
  background: rgba(255, 253, 247, 0.9);
  color: #18322a;
  font-size: 13px;
  transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease;
}

.pagination-btn:hover:not(:disabled) {
  background: rgba(29, 122, 82, 0.1);
  border-color: rgba(29, 122, 82, 0.18);
  color: #13593b;
}

.pagination-btn.active {
  background: linear-gradient(135deg, #1d7a52, #2b9568);
  border-color: transparent;
  color: #f7f5ef;
}

.pagination-ellipsis {
  min-width: 24px;
  text-align: center;
  color: #8a9a92;
}

.pagination-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.pagination-nav {
  min-width: auto;
}

.pagination-item {
  display: contents;
}
</style>
