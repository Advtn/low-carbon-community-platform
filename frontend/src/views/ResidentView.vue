<template>
  <div class="page">
    <div class="card header">
      <div>
        <h1 class="title">居民低碳中心</h1>
        <p class="subtitle">欢迎，{{ profile.nickname || user.nickname }}（{{ user.username }}）</p>
      </div>
      <div style="display: flex; gap: 8px">
        <button class="btn secondary" @click="loadAll">刷新数据</button>
        <button class="btn danger" @click="logout">退出登录</button>
      </div>
    </div>

    <div v-if="message" class="card panel" style="margin-bottom: 16px; color: #a12828">{{ message }}</div>

    <div class="grid">
      <section class="card panel" style="grid-column: span 3">
        <h3>个人碳账本概览</h3>
        <div class="kpis">
          <div class="kpi">
            <div class="name">累计积分</div>
            <div class="value">{{ profile.totalPoints ?? '-' }}</div>
          </div>
          <div class="kpi">
            <div class="name">累计减碳(kg)</div>
            <div class="value">{{ Number(profile.totalCarbonReduction || 0).toFixed(2) }}</div>
          </div>
          <div class="kpi">
            <div class="name">我的上报数</div>
            <div class="value">{{ reports.length }}</div>
          </div>
        </div>
        <div class="behavior-chart-panel">
          <div class="behavior-chart-header">
            <span>行为占比（按次数）</span>
            <span class="small-tip">总次数：{{ behaviorTotalCount }}</span>
          </div>
          <div v-if="!hasBehaviorRatioData" class="small-tip">暂无行为数据，提交并审核通过后可查看占比。</div>
          <div ref="behaviorChartRef" class="behavior-chart"></div>
        </div>
      </section>

      <section class="card panel report-panel" style="grid-column: span 5">
        <h3>低碳行为上报</h3>

        <div class="form-row">
          <select v-model="reportForm.ruleId" class="select">
            <option disabled value="">选择行为规则</option>
            <option v-for="r in rules" :key="r.id" :value="r.id">
              {{ r.name }}（+{{ r.pointsPerAction }}分 / {{ r.carbonReductionPerAction }}kg）
            </option>
          </select>

          <div class="qty-wrap">
            <button class="qty-btn" @click="changeQuantity(-1)">-</button>
            <input
              v-model.number="reportForm.quantity"
              type="number"
              min="1"
              class="input qty-input"
              placeholder="次数"
            />
            <button class="qty-btn" @click="changeQuantity(1)">+</button>
          </div>
        </div>

        <div class="report-meta" v-if="selectedRule">
          <div>规则说明：{{ selectedRule.description || '暂无说明' }}</div>
          <div>今日限额：{{ selectedRule.dailyLimit }} 次，已上报 {{ todayUsedCount }} 次，剩余 {{ remainingQuota }} 次</div>
        </div>

        <div class="quick-row">
          <span class="quick-label">快捷次数：</span>
          <button
            v-for="q in quickQuantities"
            :key="q"
            class="chip"
            :class="{ active: Number(reportForm.quantity) === q }"
            @click="setQuantity(q)"
          >
            {{ q }}次
          </button>
        </div>

        <div class="quick-row">
          <span class="quick-label">常用模板：</span>
          <button
            v-for="t in proofTemplates"
            :key="t"
            class="chip"
            @click="useTemplate(t)"
          >
            {{ t }}
          </button>
        </div>

        <template v-if="isCommuteRule">
          <div class="form-row">
            <select v-model="assistForm.mode" class="select">
              <option v-for="m in transportModes" :key="m" :value="m">{{ m }}</option>
            </select>
            <input v-model="assistForm.distance" class="input" placeholder="里程（km，可选）" />
          </div>

          <input v-model.trim="assistForm.location" class="input" placeholder="地点（可选，如：XX社区东门）" />

          <div v-if="isRoutePlannerVisible" class="route-planner">
            <div class="route-header">
              <div class="route-title">步行/骑行路线选择</div>
              <div class="route-header-actions">
                <button class="btn secondary" type="button" @click="locateCurrentPosition" :disabled="locatingCurrent">
                  {{ locatingCurrent ? '定位中...' : '定位到当前位置' }}
                </button>
                <button class="btn secondary" type="button" @click="resetRoutePlanner">重选路线</button>
              </div>
            </div>
            <div class="small-tip">在地图上先点击起点，再点击终点。系统会自动计算路线里程并回填到里程输入框。</div>
            <div ref="mapContainerRef" class="route-map"></div>
            <div class="route-meta">
              <span>起点：{{ routeStartText }}</span>
              <span>终点：{{ routeEndText }}</span>
              <span>路线里程：{{ routeDistanceKm || '-' }} km</span>
            </div>
            <div v-if="routeLoading" class="small-tip">路线计算中...</div>
            <div v-if="routeError" class="small-tip route-error">{{ routeError }}</div>
          </div>
          <div v-else class="small-tip">
            当前出行方式不需要地图选线，仅步行/骑行支持路线选择。
          </div>
        </template>
        <div v-else-if="selectedRule" class="small-tip">
          当前规则无需填写出行方式、里程和地点。
        </div>

        <textarea
          v-model.trim="reportForm.proofText"
          class="textarea"
          placeholder="凭证说明（如：步行2km并拍照记录）"
        />

        <div class="upload-row">
          <input
            ref="imageInputRef"
            type="file"
            class="input"
            accept="image/*"
            @change="onProofImageChange"
          />
          <button class="btn secondary" @click="clearProofImage" :disabled="uploadingImage">
            移除图片
          </button>
        </div>
        <div class="small-tip">凭证图片：支持 jpg/png/webp，单张不超过 5MB。</div>
        <div v-if="uploadingImage" class="small-tip">图片上传中...</div>
        <div v-if="proofImagePreviewUrl" class="proof-preview-box">
          <img :src="proofImagePreviewUrl" alt="凭证预览" class="proof-preview-image" @click="openImage(proofImagePreviewUrl)" />
        </div>

        <div class="preview-box" v-if="selectedRule">
          <div>
            预计奖励：<b>+{{ estimatedPoints }}</b> 分，预计减碳：
            <b>{{ estimatedCarbon }}</b> kg
          </div>
          <div class="quota-line">
            <div class="quota-bar"><div class="quota-fill" :style="{ width: quotaPercent + '%' }" /></div>
            <span>{{ quotaPercent }}%</span>
          </div>
          <div class="small-tip">统计口径：今日“待审核+已通过”上报次数。</div>
        </div>

        <div class="report-actions">
          <button class="btn" @click="submitReport">提交上报</button>
          <button class="btn secondary" @click="resetReportForm">重置</button>
        </div>
      </section>

      <section class="card panel" style="grid-column: span 4">
        <h3>社区积分排行榜 TOP10</h3>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>排名</th>
                <th>居民</th>
                <th>积分</th>
                <th>减碳(kg)</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, idx) in leaderboard" :key="row.userId">
                <td>{{ idx + 1 }}</td>
                <td>{{ row.nickname }}</td>
                <td>{{ row.totalPoints }}</td>
                <td>{{ Number(row.totalCarbonReduction || 0).toFixed(2) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="card panel" style="grid-column: span 8">
        <h3>我的行为上报记录</h3>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>规则</th>
                <th>次数</th>
                <th>状态</th>
                <th>奖励积分</th>
                <th>提交时间</th>
                <th>凭证图片</th>
                <th>审核备注</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in reports" :key="row.id">
                <td>{{ row.id }}</td>
                <td>{{ row.ruleName }}</td>
                <td>{{ row.quantity }}</td>
                <td>
                  <span :class="badgeClass(row.status)">{{ row.status }}</span>
                </td>
                <td>{{ row.grantedPoints ?? '-' }}</td>
                <td>{{ fmt(row.submittedAt) }}</td>
                <td>
                  <img
                    v-if="row.proofImageUrl"
                    :src="resolveImageUrl(row.proofImageUrl)"
                    class="proof-thumb"
                    alt="凭证图片"
                    @click="openImage(resolveImageUrl(row.proofImageUrl))"
                  />
                  <span v-else>-</span>
                </td>
                <td>{{ row.auditRemark || '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="card panel" style="grid-column: span 4">
        <h3>积分流水</h3>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>变动</th>
                <th>类型</th>
                <th>余额</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in ledger" :key="row.id">
                <td>{{ row.changePoints > 0 ? '+' : '' }}{{ row.changePoints }}</td>
                <td>{{ formatLedgerType(row.type) }}</td>
                <td>{{ row.balanceAfter }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="card panel" style="grid-column: span 6">
        <h3>积分商城</h3>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>商品</th>
                <th>单价</th>
                <th>库存</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in items" :key="item.id">
                <td>{{ item.name }}</td>
                <td>{{ item.pointsCost }}</td>
                <td>{{ item.stock }}</td>
                <td>
                  <button class="btn" @click="redeem(item)">兑换</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="card panel" style="grid-column: span 6">
        <h3>我的兑换订单</h3>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>商品</th>
                <th>数量</th>
                <th>积分</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="o in orders" :key="o.id">
                <td>{{ o.id }}</td>
                <td>{{ o.itemName }}</td>
                <td>{{ o.quantity }}</td>
                <td>{{ o.totalPoints }}</td>
                <td><span :class="badgeClass(o.status)">{{ o.status }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { use, init } from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { LabelLayout, UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'
import { useResidentPage } from '../composables/useResidentPage'

use([PieChart, TitleComponent, TooltipComponent, LegendComponent, LabelLayout, UniversalTransition, CanvasRenderer])

const {
  user,
  message,
  profile,
  rules,
  reports,
  ledger,
  items,
  orders,
  leaderboard,
  imageInputRef,
  uploadingImage,
  proofImagePreviewUrl,
  mapContainerRef,
  routeLoading,
  routeError,
  locatingCurrent,
  routeDistanceKm,
  routeStartText,
  routeEndText,
  quickQuantities,
  transportModes,
  proofTemplates,
  reportForm,
  assistForm,
  selectedRule,
  isCommuteRule,
  isRoutePlannerVisible,
  todayUsedCount,
  remainingQuota,
  estimatedPoints,
  estimatedCarbon,
  quotaPercent,
  loadAll,
  submitReport,
  onProofImageChange,
  clearProofImage,
  setQuantity,
  changeQuantity,
  useTemplate,
  resetReportForm,
  resetRoutePlanner,
  locateCurrentPosition,
  redeem,
  resolveImageUrl,
  openImage,
  badgeClass,
  formatLedgerType,
  fmt,
  logout
} = useResidentPage()

const behaviorChartRef = ref(null)
let behaviorChart = null

const behaviorRatioData = computed(() => {
  const approvedRows = reports.value.filter((row) => row.status === 'APPROVED')
  const sourceRows = approvedRows.length ? approvedRows : reports.value
  const counter = new Map()

  sourceRows.forEach((row) => {
    const ruleName = row.ruleName || '其他行为'
    const quantity = Number(row.quantity || 0)
    counter.set(ruleName, (counter.get(ruleName) || 0) + quantity)
  })

  return Array.from(counter.entries())
    .map(([name, value]) => ({ name, value }))
    .filter((item) => item.value > 0)
    .sort((a, b) => b.value - a.value)
})

const hasBehaviorRatioData = computed(() => behaviorRatioData.value.length > 0)
const behaviorTotalCount = computed(() =>
  behaviorRatioData.value.reduce((sum, item) => sum + Number(item.value || 0), 0)
)

function buildBehaviorChartOption() {
  return {
    color: ['#0f9d76', '#38b38b', '#5ec7a2', '#7fd8b8', '#9ae2ca', '#b5ecd9', '#d2f5e9'],
    tooltip: {
      trigger: 'item',
      formatter: '{b}<br/>次数: {c}（{d}%）'
    },
    legend: {
      bottom: 0,
      type: 'scroll',
      icon: 'circle'
    },
    series: [
      {
        name: '行为占比',
        type: 'pie',
        radius: ['36%', '68%'],
        center: ['50%', '42%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{d}%'
        },
        labelLine: {
          show: true
        },
        data: behaviorRatioData.value
      }
    ]
  }
}

function renderBehaviorChart() {
  if (!behaviorChartRef.value) return
  if (!behaviorChart) {
    behaviorChart = init(behaviorChartRef.value)
  }

  if (!hasBehaviorRatioData.value) {
    behaviorChart.clear()
    behaviorChart.setOption({
      title: {
        text: '暂无占比数据',
        left: 'center',
        top: 'middle',
        textStyle: {
          color: '#8b9a9f',
          fontSize: 14,
          fontWeight: 400
        }
      }
    })
    return
  }

  behaviorChart.setOption(buildBehaviorChartOption(), true)
}

function resizeBehaviorChart() {
  if (behaviorChart) {
    behaviorChart.resize()
  }
}

watch(
  behaviorRatioData,
  async () => {
    await nextTick()
    renderBehaviorChart()
    resizeBehaviorChart()
  },
  { deep: true }
)

onMounted(async () => {
  await nextTick()
  renderBehaviorChart()
  window.addEventListener('resize', resizeBehaviorChart)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeBehaviorChart)
  if (behaviorChart) {
    behaviorChart.dispose()
    behaviorChart = null
  }
})
</script>

<style scoped src="../styles/resident-view.css"></style>
