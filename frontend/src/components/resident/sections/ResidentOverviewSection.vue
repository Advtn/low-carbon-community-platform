<template>
  <section class="workspace-pane">
    <div class="pane-head">
      <div>
        <span class="workspace-kicker">Overview</span>
        <h2>我的低碳总览</h2>
        <p>把积分、减碳量、上报活跃度和社区排名放在一个连续工作区里，方便快速扫描今日表现。</p>
      </div>
      <span class="badge">总次数 {{ behaviorTotalCount }}</span>
    </div>

    <div class="metric-strip">
      <div class="metric-unit">
        <span>累计积分</span>
        <strong>{{ profile.totalPoints ?? 0 }}</strong>
        <small>可用于兑换社区商城商品</small>
      </div>
      <div class="metric-unit">
        <span>累计减碳量</span>
        <strong>{{ Number(profile.totalCarbonReduction || 0).toFixed(2) }}</strong>
        <small>单位：kg CO₂e</small>
      </div>
      <div class="metric-unit">
        <span>我的上报</span>
        <strong>{{ reports.length }}</strong>
        <small>含待审核与已审核记录</small>
      </div>
      <div class="metric-unit">
        <span>商城商品</span>
        <strong>{{ items.length }}</strong>
        <small>当前可兑换商品数量</small>
      </div>
    </div>

    <div class="workspace-split">
      <div class="workspace-surface">
        <span class="workspace-kicker">Behavior Mix</span>
        <h3>低碳行为构成</h3>
        <p>优先展示已审核数据，如暂无审核记录则展示全部上报次数。</p>
        <BehaviorPieChart
          :has-data="hasBehaviorRatioData"
          :set-chart-ref="setChartRef"
        />
      </div>

      <div class="workspace-surface">
        <span class="workspace-kicker">Community</span>
        <h3>社区积分排行</h3>
        <p>查看自己在社区中的积分位置与减碳贡献。</p>
        <div class="workspace-list">
          <article v-for="(row, idx) in pagedLeaderboard" :key="row.userId" class="workspace-list-item resident-rank-item">
            <div>
              <strong>{{ leaderboardStartIndex + idx + 1 }} · {{ row.nickname }}</strong>
              <p>{{ row.totalPoints }} 分 · {{ Number(row.totalCarbonReduction || 0).toFixed(2) }} kg</p>
            </div>
            <span
              class="badge"
              :class="leaderboardStartIndex + idx === 0 ? '' : leaderboardStartIndex + idx < 3 ? 'warn' : ''"
            >
              TOP {{ leaderboardStartIndex + idx + 1 }}
            </span>
          </article>
          <div v-if="!pagedLeaderboard.length" class="workspace-empty">排行榜暂无数据</div>
        </div>
        <AppPagination
          v-if="leaderboard.length > leaderboardPageSize"
          :current-page="leaderboardPage"
          :total-items="leaderboard.length"
          :page-size="leaderboardPageSize"
          @change="emit('update:leaderboardPage', $event)"
        />
      </div>
    </div>
  </section>
</template>

<script setup>
import { toRef } from 'vue'
import AppPagination from '../../AppPagination.vue'
import BehaviorPieChart from '../charts/BehaviorPieChart.vue'
import { useBehaviorChart } from '../../../composables/resident/useBehaviorChart'

const props = defineProps({
  profile: {
    type: Object,
    required: true
  },
  reports: {
    type: Array,
    default: () => []
  },
  items: {
    type: Array,
    default: () => []
  },
  pagedLeaderboard: {
    type: Array,
    default: () => []
  },
  leaderboardStartIndex: {
    type: Number,
    required: true
  },
  leaderboard: {
    type: Array,
    default: () => []
  },
  leaderboardPageSize: {
    type: Number,
    required: true
  },
  leaderboardPage: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['update:leaderboardPage'])
const { behaviorTotalCount, hasBehaviorRatioData, setChartRef } = useBehaviorChart(
  toRef(props, 'reports')
)
</script>
