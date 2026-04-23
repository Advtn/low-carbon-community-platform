<template>
  <section class="workspace-pane">
    <div class="pane-head">
      <div>
        <span class="workspace-kicker">Overview</span>
        <h2>运营总览</h2>
        <p>从居民活跃、规则启用、待处理任务到当日订单履约，把关键运营指标集中在同一工作区。</p>
      </div>
      <span class="badge">更新时间 {{ fmt(dashboard.updatedAt) }}</span>
    </div>

    <div class="metric-strip">
      <div class="metric-unit">
        <span>居民用户</span>
        <strong>{{ dashboard.residentCount }}</strong>
        <small>当前系统中的居民账号总数</small>
      </div>
      <div class="metric-unit">
        <span>启用规则</span>
        <strong>{{ dashboard.activeRuleCount }}</strong>
        <small>居民端当前可上报的低碳行为</small>
      </div>
      <div class="metric-unit">
        <span>待审核上报</span>
        <strong>{{ dashboard.pendingReportCount }}</strong>
        <small>需要人工确认的行为记录</small>
      </div>
      <div class="metric-unit">
        <span>待处理订单</span>
        <strong>{{ dashboard.pendingOrderCount }}</strong>
        <small>待履约或退款的兑换订单</small>
      </div>
    </div>

    <div class="workspace-grid-2">
      <div class="workspace-surface">
        <span class="workspace-kicker">Today</span>
        <h3>今日运营脉搏</h3>
        <div class="workspace-list">
          <article class="workspace-list-item">
            <div>
              <strong>今日提交上报</strong>
              <p>衡量社区参与热度的最直接指标。</p>
            </div>
            <span class="badge">{{ dashboard.todaySubmittedReports }}</span>
          </article>
          <article class="workspace-list-item">
            <div>
              <strong>今日通过上报</strong>
              <p>反映审核效率与激励发放节奏。</p>
            </div>
            <span class="badge">{{ dashboard.todayApprovedReports }}</span>
          </article>
          <article class="workspace-list-item">
            <div>
              <strong>今日兑换订单</strong>
              <p>衡量积分体系是否形成持续激励。</p>
            </div>
            <span class="badge warn">{{ dashboard.todayRedeemOrders }}</span>
          </article>
          <article class="workspace-list-item">
            <div>
              <strong>今日完成订单</strong>
              <p>体现后端履约处理能力。</p>
            </div>
            <span class="badge">{{ dashboard.todayCompletedOrders }}</span>
          </article>
        </div>
      </div>

      <div class="workspace-surface">
        <span class="workspace-kicker">Resources</span>
        <h3>资源概况</h3>
        <p>从库存与积分角度快速判断运营是否需要补货或调整激励策略。</p>
        <div class="workspace-inline-stats">
          <div class="stat">
            <span>居民总积分</span>
            <strong>{{ dashboard.totalResidentPoints }}</strong>
          </div>
          <div class="stat">
            <span>累计减碳量</span>
            <strong>{{ Number(dashboard.totalCarbonReduction || 0).toFixed(2) }}</strong>
          </div>
          <div class="stat">
            <span>上架商品</span>
            <strong>{{ dashboard.enabledItemCount }}</strong>
          </div>
          <div class="stat">
            <span>总库存</span>
            <strong>{{ dashboard.enabledItemStock }}</strong>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  dashboard: {
    type: Object,
    required: true
  },
  fmt: {
    type: Function,
    required: true
  }
})
</script>
