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

    <ResidentOverviewSection
      v-if="activeSection === 'overview'"
      :profile="profile"
      :reports="reports"
      :items="items"
      :paged-leaderboard="pagedLeaderboard"
      :leaderboard-start-index="leaderboardStartIndex"
      :leaderboard="leaderboard"
      :leaderboard-page-size="leaderboardPageSize"
      :leaderboard-page="leaderboardPage"
      @update:leaderboardPage="leaderboardPage = $event"
    />

    <ResidentReportSection
      v-if="activeSection === 'report'"
      :rules="rules"
      :report-form="reportForm"
      :assist-form="assistForm"
      :selected-rule="selectedRule"
      :is-commute-rule="isCommuteRule"
      :is-route-planner-visible="isRoutePlannerVisible"
      :today-used-count="todayUsedCount"
      :remaining-quota="remainingQuota"
      :estimated-points="estimatedPoints"
      :estimated-carbon="estimatedCarbon"
      :quick-quantities="quickQuantities"
      :transport-modes="transportModes"
      :proof-templates="proofTemplates"
      :set-map-container-ref="setMapContainerRef"
      :set-image-input-ref="setImageInputRef"
      :uploading-image="uploadingImage"
      :proof-image-preview-url="proofImagePreviewUrl"
      :route-loading="routeLoading"
      :route-error="routeError"
      :locating-current="locatingCurrent"
      :route-distance-km="routeDistanceKm"
      :route-start-text="routeStartText"
      :route-end-text="routeEndText"
      :quota-percent="quotaPercent"
      :report-message="reportMessage"
      :report-message-type="reportMessageType"
      @reset-form="resetReportForm"
      @change-quantity="changeQuantity"
      @set-quantity="setQuantity"
      @use-template="useTemplate"
      @locate-current-position="locateCurrentPosition"
      @reset-route-planner="resetRoutePlanner"
      @proof-image-change="onProofImageChange"
      @clear-proof-image="clearProofImage"
      @open-image="openImage"
      @submit="submitReport"
    />

    <ResidentReportsSection
      v-if="activeSection === 'reports'"
      :reports="reports"
      :paged-reports="pagedReports"
      :reports-page-size="reportsPageSize"
      :reports-page="reportsPage"
      :badge-class="badgeClass"
      :format-status-label="formatStatusLabel"
      :fmt="fmt"
      :resolve-image-url="resolveImageUrl"
      @update:reportsPage="reportsPage = $event"
      @open-image="openImage"
    />

    <ResidentLedgerSection
      v-if="activeSection === 'ledger'"
      :ledger="ledger"
      :paged-ledger="pagedLedger"
      :ledger-page-size="ledgerPageSize"
      :ledger-page="ledgerPage"
      :format-ledger-type="formatLedgerType"
      :fmt="fmt"
      @update:ledgerPage="ledgerPage = $event"
    />

    <ResidentMallSection
      v-if="activeSection === 'mall'"
      :redeem-message="redeemMessage"
      :redeem-message-type="redeemMessageType"
      :paged-items="pagedItems"
      :items="items"
      :items-page-size="itemsPageSize"
      :items-page="itemsPage"
      @redeem="redeem"
      @update:itemsPage="itemsPage = $event"
    />

    <ResidentOrdersSection
      v-if="activeSection === 'orders'"
      :orders="orders"
      :paged-orders="pagedOrders"
      :orders-page-size="ordersPageSize"
      :orders-page="ordersPage"
      :badge-class="badgeClass"
      :format-status-label="formatStatusLabel"
      :fmt="fmt"
      @update:ordersPage="ordersPage = $event"
    />

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
import { ref } from 'vue'
import ProfileCenterPanel from '../components/profile/ProfileCenterPanel.vue'
import ResidentLedgerSection from '../components/resident/sections/ResidentLedgerSection.vue'
import ResidentMallSection from '../components/resident/sections/ResidentMallSection.vue'
import ResidentOrdersSection from '../components/resident/sections/ResidentOrdersSection.vue'
import ResidentOverviewSection from '../components/resident/sections/ResidentOverviewSection.vue'
import ResidentReportSection from '../components/resident/sections/ResidentReportSection.vue'
import ResidentReportsSection from '../components/resident/sections/ResidentReportsSection.vue'
import { useResidentPage } from '../composables/useResidentPage'
import { usePagination } from '../composables/shared/usePagination'
import { useProfileCenter } from '../composables/shared/useProfileCenter'
import { useWorkspaceState } from '../composables/workspace/useWorkspaceState'
import { updateResidentProfile, uploadImage } from '../services/residentService'
import WorkspaceShell from '../components/workspace/WorkspaceShell.vue'
import residentSections from '../constants/residentSections'

const workspaceStateStorageKey = 'lowcarbon:resident-workspace:v1'
const {
  sectionSearchKeyword,
  setSectionSearchInput,
  filteredSidebarSections,
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

function setImageInputRef(el) {
  imageInputRef.value = el
}

function setMapContainerRef(el) {
  mapContainerRef.value = el
}

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
</script>

<style src="../styles/resident-view.css"></style>
