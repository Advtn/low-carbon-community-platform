<template>
  <div v-if="selectedRule" class="workspace-panel">
    <span class="workspace-kicker">Rule Summary</span>
    <h3>{{ selectedRule.name }}</h3>
    <p>{{ selectedRule.description || '当前规则暂无补充说明。' }}</p>
    <div class="workspace-inline-stats">
      <div class="stat">
        <span>今日已用</span>
        <strong>{{ todayUsedCount }} 次</strong>
      </div>
      <div class="stat">
        <span>剩余额度</span>
        <strong>{{ remainingQuota }} 次</strong>
      </div>
      <div class="stat">
        <span>预计奖励</span>
        <strong>+{{ estimatedPoints }} 分</strong>
      </div>
      <div class="stat">
        <span>预计减碳</span>
        <strong>{{ estimatedCarbon }} kg</strong>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  selectedRule: {
    type: Object,
    default: null
  },
  todayUsedCount: {
    type: Number,
    required: true
  },
  remainingQuota: {
    type: Number,
    required: true
  },
  estimatedPoints: {
    type: Number,
    required: true
  },
  estimatedCarbon: {
    type: [Number, String],
    required: true
  }
})
</script>
