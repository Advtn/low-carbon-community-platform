<template>
  <div>
    <div v-if="!hasData" class="workspace-empty">暂无行为数据，提交上报后这里会自动生成行为分布。</div>
    <div v-else :ref="setChartRef" class="workspace-chart resident-chart"></div>
  </div>
</template>

<script setup>
defineProps({
  hasData: {
    type: Boolean,
    required: true
  },
  setChartRef: {
    type: Function,
    default: null
  }
})
</script>
