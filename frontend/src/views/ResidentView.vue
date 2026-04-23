<template>
  <WorkspaceShell
    brand-mark="LC"
    brand-title="居民低碳中心"
    :brand-subtitle="`${profile.nickname || user.nickname} · ${user.username}`"
    nav-aria-label="居民端模块导航"
    :sections="filteredSidebarSections"
    :search-keyword="sectionSearchKeyword"
    @update:searchKeyword="sectionSearchKeyword = $event"
    :set-search-input-ref="setSectionSearchInput"
    search-aria-label="搜索居民端模块"
    :open-tabs="openTabSections"
    :active-section="activeSection"
    @update:activeSection="activeSection = $event"
    :active-section-meta="activeSectionMeta"
    :avatar-menu-open="avatarMenuOpen"
    @update:avatarMenuOpen="avatarMenuOpen = $event"
    :profile-avatar-url="profileAvatarUrl"
    :profile-name="profileCenterForm.name"
    :profile-role="profileCenterForm.role"
    footer-title="今日状态"
    :footer-text="selectedRule ? `当前规则剩余 ${remainingQuota} 次额度` : '点击左侧模块后会在顶部生成最近打开页签。'"
    breadcrumb-prefix="居民端"
    empty-selection-title="请选择一个工作模块"
    empty-selection-description="左侧菜单用于打开模块，顶部仅保留最近打开的页签，并支持随时关闭。"
    empty-state-title="从左侧打开一个模块开始工作"
    empty-state-description="例如先打开“行为上报”提交低碳记录，或者打开“积分商城”查看当前可兑换商品。"
    @open-section="openSection"
    @close-section="closeSection"
    @clear-section-search="clearSectionSearch"
    @open-first-matched-section="openFirstMatchedSection"
    @refresh="loadAll"
    @open-profile="openProfileCenter"
    @logout="logout"
  >
    <template #feedback>
      <div v-if="message" class="inline-message error">{{ message }}</div>
    </template>

    <section v-if="activeSection === 'overview'" class="workspace-pane">
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
                <div v-if="!hasBehaviorRatioData" class="workspace-empty">暂无行为数据，提交上报后这里会自动生成行为分布。</div>
                <div v-else ref="behaviorChartRef" class="workspace-chart resident-chart"></div>
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
                  @change="leaderboardPage = $event"
                />
              </div>
            </div>
          </section>

    <section v-if="activeSection === 'report'" class="workspace-pane">
            <div class="pane-head">
              <div>
                <span class="workspace-kicker">Submit</span>
                <h2>低碳行为上报</h2>
                <p>从规则选择、里程辅助、凭证说明到图片上传，所有提交动作都在同一个工作台里完成。</p>
              </div>
              <div class="workspace-actions">
                <button class="btn ghost" @click="resetReportForm">重置表单</button>
              </div>
            </div>

            <div class="workspace-form">
              <div class="workspace-form-grid">
                <label class="field">
                  <span class="field-label">行为规则</span>
                  <select v-model="reportForm.ruleId" class="select">
                    <option disabled value="">请选择行为规则</option>
                    <option v-for="r in rules" :key="r.id" :value="r.id">
                      {{ r.name }} · +{{ r.pointsPerAction }} 分 / {{ r.carbonReductionPerAction }} kg
                    </option>
                  </select>
                </label>

                <label class="field">
                  <span class="field-label">上报次数</span>
                  <div class="resident-qty-wrap">
                    <button class="workspace-pill" @click="changeQuantity(-1)">-1</button>
                    <input v-model.number="reportForm.quantity" type="number" min="1" class="input resident-qty-input" />
                    <button class="workspace-pill" @click="changeQuantity(1)">+1</button>
                  </div>
                </label>
              </div>

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

              <div class="field">
                <span class="field-label">快捷次数</span>
                <div class="workspace-pills">
                  <button
                    v-for="q in quickQuantities"
                    :key="q"
                    class="workspace-pill"
                    :class="{ active: Number(reportForm.quantity) === q }"
                    @click="setQuantity(q)"
                  >
                    {{ q }} 次
                  </button>
                </div>
              </div>

              <div class="field">
                <span class="field-label">凭证模板</span>
                <div class="workspace-pills">
                  <button
                    v-for="t in proofTemplates"
                    :key="t"
                    class="workspace-pill"
                    @click="useTemplate(t)"
                  >
                    {{ t }}
                  </button>
                </div>
              </div>

              <template v-if="isCommuteRule">
                <div class="workspace-form-grid">
                  <label class="field">
                    <span class="field-label">出行方式</span>
                    <select v-model="assistForm.mode" class="select">
                      <option v-for="m in transportModes" :key="m" :value="m">{{ m }}</option>
                    </select>
                  </label>

                  <label class="field">
                    <span class="field-label">里程（km）</span>
                    <input v-model="assistForm.distance" class="input" placeholder="可手动填写或自动计算" />
                  </label>
                </div>

                <label class="field">
                  <span class="field-label">地点补充</span>
                  <input v-model.trim="assistForm.location" class="input" placeholder="例如：社区东门到地铁站" />
                </label>

                <div v-if="isRoutePlannerVisible" class="workspace-panel">
                  <div class="pane-head">
                    <div>
                      <span class="workspace-kicker">Route Helper</span>
                      <h2>路线辅助选择</h2>
                      <p>地图上先选起点再选终点，系统会自动回填路线里程。</p>
                    </div>
                    <div class="workspace-actions">
                      <button class="btn secondary" type="button" @click="locateCurrentPosition" :disabled="locatingCurrent">
                        {{ locatingCurrent ? '定位中...' : '使用当前位置' }}
                      </button>
                      <button class="btn ghost" type="button" @click="resetRoutePlanner">重选路线</button>
                    </div>
                  </div>

                  <div ref="mapContainerRef" class="workspace-map"></div>
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

              <label class="field">
                <span class="field-label">凭证说明</span>
                <textarea
                  v-model.trim="reportForm.proofText"
                  class="textarea"
                  placeholder="例如：今天步行通勤 2 公里，并上传现场图片作为凭证。"
                />
              </label>

              <div class="workspace-grid-2">
                <label class="field">
                  <span class="field-label">上传凭证图片</span>
                  <input
                    ref="imageInputRef"
                    type="file"
                    class="input proof-image-input"
                    accept="image/*"
                    @change="onProofImageChange"
                  />
                  <span class="field-help">支持 jpg / png / webp，单张建议控制在 5MB 以内。</span>
                </label>

                <div class="field">
                  <span class="field-label">图片预览</span>
                  <div class="workspace-upload-preview">
                    <img
                      v-if="proofImagePreviewUrl"
                      :src="proofImagePreviewUrl"
                      alt="凭证预览"
                      @click="openImage(proofImagePreviewUrl)"
                    />
                    <div v-else class="workspace-empty">上传后会在这里显示预览</div>
                  </div>
                  <div class="workspace-actions">
                    <button class="btn secondary" @click="clearProofImage" :disabled="uploadingImage">移除图片</button>
                  </div>
                </div>
              </div>

              <div v-if="uploadingImage" class="inline-message success">图片上传中，请稍候后再提交。</div>

              <div v-if="selectedRule" class="workspace-panel">
                <span class="workspace-kicker">Quota</span>
                <h3>今日额度进度</h3>
                <p>系统会把待审核与已通过的次数一起计入今日限额。</p>
                <div class="progress">
                  <span :style="{ width: quotaPercent + '%' }"></span>
                </div>
                <div class="workspace-legend">
                  <span>当前进度 {{ quotaPercent }}%</span>
                  <span>预计本次 +{{ estimatedPoints }} 分 / {{ estimatedCarbon }} kg</span>
                </div>
              </div>

              <div
                v-if="reportMessage"
                class="inline-message"
                :class="reportMessageType === 'success' ? 'success' : 'error'"
              >
                {{ reportMessage }}
              </div>

              <div class="workspace-actions">
                <button class="btn" @click="submitReport" :disabled="uploadingImage">提交上报</button>
              </div>
            </div>
          </section>

    <section v-if="activeSection === 'reports'" class="workspace-pane">
            <div class="pane-head">
              <div>
                <span class="workspace-kicker">Reports</span>
                <h2>我的上报记录</h2>
                <p>集中查看规则、次数、审核状态、奖励积分、凭证图片与审核备注。</p>
              </div>
              <span class="badge">{{ reports.length }} 条记录</span>
            </div>
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
                    <th>凭证</th>
                    <th>审核备注</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="!pagedReports.length">
                    <td colspan="8" class="workspace-table-empty">暂无上报记录</td>
                  </tr>
                  <tr v-for="row in pagedReports" :key="row.id">
                    <td>{{ row.id }}</td>
                    <td>{{ row.ruleName }}</td>
                    <td>{{ row.quantity }}</td>
                    <td><span :class="badgeClass(row.status)">{{ formatStatusLabel(row.status) }}</span></td>
                    <td>{{ row.grantedPoints ?? '-' }}</td>
                    <td>{{ fmt(row.submittedAt) }}</td>
                    <td>
                      <img
                        v-if="row.proofImageUrl"
                        :src="resolveImageUrl(row.proofImageUrl)"
                        class="media-thumb"
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
            <AppPagination
              v-if="reports.length > reportsPageSize"
              :current-page="reportsPage"
              :total-items="reports.length"
              :page-size="reportsPageSize"
              @change="reportsPage = $event"
            />
          </section>

    <section v-if="activeSection === 'ledger'" class="workspace-pane">
            <div class="pane-head">
              <div>
                <span class="workspace-kicker">Ledger</span>
                <h2>积分流水</h2>
                <p>查看每一笔奖励、兑换与退款带来的积分变化。</p>
              </div>
            </div>
            <div class="table-wrap">
              <table class="table">
                <thead>
                  <tr>
                    <th>变动</th>
                    <th>类型</th>
                    <th>余额</th>
                    <th>时间</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="!pagedLedger.length">
                    <td colspan="4" class="workspace-table-empty">暂无积分流水</td>
                  </tr>
                  <tr v-for="row in pagedLedger" :key="row.id">
                    <td>{{ row.changePoints > 0 ? '+' : '' }}{{ row.changePoints }}</td>
                    <td>{{ formatLedgerType(row.type) }}</td>
                    <td>{{ row.balanceAfter }}</td>
                    <td>{{ fmt(row.createdAt) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <AppPagination
              v-if="ledger.length > ledgerPageSize"
              :current-page="ledgerPage"
              :total-items="ledger.length"
              :page-size="ledgerPageSize"
              @change="ledgerPage = $event"
            />
          </section>

    <section v-if="activeSection === 'mall'" class="workspace-pane">
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
                  <button class="btn" @click="redeem(item)">兑换</button>
                </div>
              </article>
              <div v-if="!pagedItems.length" class="workspace-empty">商城暂时没有可兑换商品</div>
            </div>
            <AppPagination
              v-if="items.length > itemsPageSize"
              :current-page="itemsPage"
              :total-items="items.length"
              :page-size="itemsPageSize"
              @change="itemsPage = $event"
            />
          </section>

    <section v-if="activeSection === 'orders'" class="workspace-pane">
            <div class="pane-head">
              <div>
                <span class="workspace-kicker">Orders</span>
                <h2>我的兑换订单</h2>
                <p>查看兑换商品、积分消耗与当前履约状态。</p>
              </div>
              <span class="badge">{{ orders.length }} 笔订单</span>
            </div>
            <div class="table-wrap">
              <table class="table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>商品</th>
                    <th>数量</th>
                    <th>积分</th>
                    <th>状态</th>
                    <th>创建时间</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="!pagedOrders.length">
                    <td colspan="6" class="workspace-table-empty">暂无兑换订单</td>
                  </tr>
                  <tr v-for="o in pagedOrders" :key="o.id">
                    <td>{{ o.id }}</td>
                    <td>{{ o.itemName }}</td>
                    <td>{{ o.quantity }}</td>
                    <td>{{ o.totalPoints }}</td>
                    <td><span :class="badgeClass(o.status)">{{ formatStatusLabel(o.status) }}</span></td>
                    <td>{{ fmt(o.createdAt) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <AppPagination
              v-if="orders.length > ordersPageSize"
              :current-page="ordersPage"
              :total-items="orders.length"
              :page-size="ordersPageSize"
              @change="ordersPage = $event"
            />
          </section>

    <ProfileCenterPanel
      v-if="activeSection === 'profile'"
      description-text="集中查看并维护你的基础资料、个人介绍与常用标签。"
      :form="profileCenterForm"
      :avatar-url="profileAvatarUrl"
      :save-message="profileSaveMessage"
      :save-type="profileSaveType"
      :avatar-uploading="profileAvatarUploading"
      :set-avatar-input-ref="setProfileAvatarInput"
      role-meta-mark="角"
      @save="saveProfile"
      @trigger-avatar-upload="triggerProfileAvatarUpload"
      @avatar-change="onProfileAvatarChange"
    />
  </WorkspaceShell>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { use, init } from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import { LabelLayout, UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'
import ProfileCenterPanel from '../components/profile/ProfileCenterPanel.vue'
import { useResidentPage } from '../composables/useResidentPage'
import { usePagination } from '../composables/shared/usePagination'
import { useProfileCenter } from '../composables/shared/useProfileCenter'
import { useWorkspaceState } from '../composables/workspace/useWorkspaceState'
import { updateResidentProfile, uploadImage } from '../services/residentService'
import AppPagination from '../components/AppPagination.vue'
import WorkspaceShell from '../components/workspace/WorkspaceShell.vue'
import residentSections from '../constants/residentSections'

use([PieChart, TooltipComponent, LegendComponent, LabelLayout, UniversalTransition, CanvasRenderer])

const workspaceStateStorageKey = 'lowcarbon:resident-workspace:v1'
const {
  sectionSearchKeyword,
  setSectionSearchInput,
  filteredSidebarSections,
  openTabs,
  activeSection,
  openTabSections,
  activeSectionMeta,
  openSection,
  closeSection,
  clearSectionSearch,
  openFirstMatchedSection
} = useWorkspaceState({
  storageKey: workspaceStateStorageKey,
  sections: residentSections
})
const avatarMenuOpen = ref(false)

function openProfileCenter() {
  openSection('profile')
  avatarMenuOpen.value = false
}

const {
  user,
  message,
  reportMessage,
  reportMessageType,
  redeemMessage,
  redeemMessageType,
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
  formatStatusLabel,
  formatLedgerType,
  fmt,
  logout
} = useResidentPage()

const {
  profileCenterForm,
  profileSaveMessage,
  profileSaveType,
  profileAvatarUploading,
  profileAvatarUrl,
  setProfileAvatarInput,
  triggerProfileAvatarUpload,
  onProfileAvatarChange,
  saveProfile
} = useProfileCenter({
  storageKey: 'lowcarbon:resident-avatar:v1',
  defaults: {
    name: profile.fullName || user.nickname || user.username || '低碳居民',
    gender: '保密',
    nickname: user.nickname || '',
    email: `${user.username || 'resident'}@lowcarbon.local`,
    phone: '13800001234',
    address: '广东省深圳市南山区低碳社区 8 栋 201',
    bio: '我正在通过步行通勤、垃圾分类和自带水杯，把低碳行动变成社区里的长期习惯。',
    role: '社区低碳居民',
    city: '广东省深圳市',
    organization: '低碳社区志愿网络',
    avatarUrl: user.avatarUrl || user.avatar || '',
    tags: ['绿色出行', '社区分类', '持续打卡', '低碳生活']
  },
  sourceWatcher: () => ({
    username: user.username,
    nickname: profile.nickname,
    fullName: profile.fullName,
    gender: profile.gender,
    email: profile.email,
    phone: profile.phone,
    address: profile.address,
    bio: profile.bio,
    city: profile.city,
    organization: profile.organization,
    avatarUrl: profile.avatarUrl,
    avatar: profile.avatar,
    tags: profile.tags,
    role: profile.role
  }),
  resolveImageUrl,
  uploadImage,
  updateProfile: updateResidentProfile,
  applyProfileData: (data) => {
    Object.assign(profile, data)
  },
  username: user.username,
  resolveDefaultEmail: ({ source, username: sourceUsername }) =>
    `${source.username || sourceUsername || 'resident'}@lowcarbon.local`
})

const leaderboardPageSize = 5
const reportsPageSize = 5
const ledgerPageSize = 5
const itemsPageSize = 4
const ordersPageSize = 5

const {
  currentPage: leaderboardPage,
  startIndex: leaderboardStartIndex,
  pagedItems: pagedLeaderboard
} = usePagination(leaderboard, leaderboardPageSize)
const { currentPage: reportsPage, pagedItems: pagedReports } = usePagination(reports, reportsPageSize)
const { currentPage: ledgerPage, pagedItems: pagedLedger } = usePagination(ledger, ledgerPageSize)
const { currentPage: itemsPage, pagedItems: pagedItems } = usePagination(items, itemsPageSize)
const { currentPage: ordersPage, pagedItems: pagedOrders } = usePagination(orders, ordersPageSize)

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
    color: ['#1d7a52', '#5db07f', '#9dd0a7', '#c7713f', '#e4b793', '#7aa7b0'],
    tooltip: {
      trigger: 'item',
      formatter: '{b}<br/>次数：{c}（{d}%）'
    },
    legend: {
      bottom: 0,
      icon: 'circle',
      textStyle: {
        color: '#5d6f66'
      }
    },
    series: [
      {
        name: '行为占比',
        type: 'pie',
        radius: ['40%', '68%'],
        center: ['50%', '42%'],
        itemStyle: {
          borderColor: '#fffdf7',
          borderWidth: 3
        },
        label: {
          show: true,
          formatter: '{d}%',
          color: '#18322a',
          fontWeight: 600
        },
        data: behaviorRatioData.value
      }
    ]
  }
}

function renderBehaviorChart() {
  if (!behaviorChartRef.value) return

  if (behaviorChart && behaviorChart.getDom() !== behaviorChartRef.value) {
    behaviorChart.dispose()
    behaviorChart = null
  }

  if (!behaviorChart) {
    behaviorChart = init(behaviorChartRef.value)
  }

  if (!hasBehaviorRatioData.value) {
    behaviorChart.clear()
    behaviorChart.setOption({
      title: {
        text: '暂无行为占比数据',
        left: 'center',
        top: 'middle',
        textStyle: {
          color: '#8a9a92',
          fontSize: 14,
          fontWeight: 500
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
  [behaviorRatioData, activeSection],
  async () => {
    if (activeSection.value !== 'overview') {
      if (behaviorChart) {
        behaviorChart.dispose()
        behaviorChart = null
      }
      return
    }
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
