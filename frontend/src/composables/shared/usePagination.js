import { computed, ref, unref, watch } from 'vue'

export function usePagination(itemsSource, pageSize, initialPage = 1) {
  const currentPage = ref(initialPage)

  const totalItems = computed(() => {
    const items = unref(itemsSource)
    return Array.isArray(items) ? items.length : 0
  })

  const totalPages = computed(() => {
    return Math.max(1, Math.ceil(totalItems.value / pageSize))
  })

  const startIndex = computed(() => {
    return (currentPage.value - 1) * pageSize
  })

  const pagedItems = computed(() => {
    const items = unref(itemsSource)
    if (!Array.isArray(items)) {
      return []
    }

    return items.slice(startIndex.value, startIndex.value + pageSize)
  })

  watch(
    totalItems,
    () => {
      if (currentPage.value > totalPages.value) {
        currentPage.value = totalPages.value
      }
    },
    { immediate: true }
  )

  return {
    currentPage,
    pageSize,
    totalItems,
    totalPages,
    startIndex,
    pagedItems
  }
}
