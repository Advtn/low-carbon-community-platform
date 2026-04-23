<template>
  <div class="workspace-panel">
    <div class="pane-head">
      <div>
        <span class="workspace-kicker">Route Helper</span>
        <h2>路线辅助选择</h2>
        <p>地图上先选起点再选终点，系统会自动回填路线里程。</p>
      </div>
      <div class="workspace-actions">
        <button class="btn secondary" type="button" @click="emit('locate-current-position')" :disabled="locatingCurrent">
          {{ locatingCurrent ? '定位中...' : '使用当前位置' }}
        </button>
        <button class="btn ghost" type="button" @click="emit('reset-route-planner')">重选路线</button>
      </div>
    </div>

    <div :ref="setMapContainerRef" class="workspace-map"></div>
    <div class="workspace-inline-stats">
      <div class="stat">
        <span>起点</span>
        <strong>{{ routeStartText }}</strong>
      </div>
      <div class="stat">
        <span>终点</span>
        <strong>{{ routeEndText }}</strong>
      </div>
      <div class="stat">
        <span>路线里程</span>
        <strong>{{ routeDistanceKm || '-' }} km</strong>
      </div>
    </div>
    <div v-if="routeLoading" class="inline-message success">路线计算中，请稍候...</div>
    <div v-if="routeError" class="inline-message error">{{ routeError }}</div>
  </div>
</template>

<script setup>
defineProps({
  setMapContainerRef: {
    type: Function,
    default: null
  },
  locatingCurrent: {
    type: Boolean,
    required: true
  },
  routeStartText: {
    type: String,
    default: ''
  },
  routeEndText: {
    type: String,
    default: ''
  },
  routeDistanceKm: {
    type: [Number, String],
    default: ''
  },
  routeLoading: {
    type: Boolean,
    required: true
  },
  routeError: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['locate-current-position', 'reset-route-planner'])
</script>
