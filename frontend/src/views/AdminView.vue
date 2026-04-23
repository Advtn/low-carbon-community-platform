<template>
  <WorkspaceShell
    brand-mark="AD"
    brand-title="社区运营后台"
    brand-subtitle="规则、审核、商城与订单协同"
    nav-aria-label="管理员模块导航"
    :sections="filteredSidebarSections"
    :search-keyword="sectionSearchKeyword"
    @update:searchKeyword="sectionSearchKeyword = $event"
    :set-search-input-ref="setSectionSearchInput"
    search-aria-label="搜索管理端模块"
    :open-tabs="openTabSections"
    :active-section="activeSection"
    @update:activeSection="activeSection = $event"
    :active-section-meta="activeSectionMeta"
    :avatar-menu-open="avatarMenuOpen"
    @update:avatarMenuOpen="avatarMenuOpen = $event"
    :profile-avatar-url="profileAvatarUrl"
    :profile-name="profileCenterForm.name"
    :profile-role="profileCenterForm.role"
    footer-title="今日待处理"
    :footer-text="`待审核上报 ${dashboard.pendingReportCount} 条，待处理订单 ${dashboard.pendingOrderCount} 笔。`"
    breadcrumb-prefix="管理员端"
    empty-selection-title="请选择一个工作模块"
    empty-selection-description="左侧点击一个模块即可在顶部生成最近打开页签，关闭后会自动回到上一个仍然打开的页签。"
    empty-state-title="从左侧打开一个管理模块"
    empty-state-description="例如先打开“审核工作台”处理待审核上报，或者打开“商品管理”维护积分商城库存。"
    @open-section="openSection"
    @close-section="closeSection"
    @clear-section-search="clearSectionSearch"
    @open-first-matched-section="openFirstMatchedSection"
    @refresh="loadAll"
    @open-profile="openProfileCenter"
    @logout="logout"
  >
    <template #feedback>
      <div
        v-if="message && messageType !== 'success'"
        class="action-toast is-error"
        :style="{ '--toast-duration': '5s' }"
        role="status"
        aria-live="assertive"
      >
        <span class="action-toast-icon" aria-hidden="true">!</span>
        <div class="action-toast-body">
          <p class="action-toast-title">操作失败</p>
          <p class="action-toast-text">{{ message }}</p>
          <span class="action-toast-progress" aria-hidden="true"></span>
        </div>
        <button class="action-toast-close" type="button" aria-label="关闭提示" @click="clearMessage">×</button>
      </div>

      <div v-if="successDialogVisible" class="success-dialog-mask" role="dialog" aria-modal="true" aria-labelledby="success-dialog-title">
        <section class="success-dialog-card">
          <header class="success-dialog-head">
            <h3 id="success-dialog-title">操作成功</h3>
          </header>
          <div class="success-dialog-body">
            <p>{{ successDialogMessage }}</p>
          </div>
          <footer class="success-dialog-actions">
            <button class="btn" type="button" @click="closeSuccessDialog">确定</button>
          </footer>
        </section>
      </div>
    </template>

    <AdminOverviewSection
      v-if="activeSection === 'overview'"
      :dashboard="dashboard"
      :fmt="fmt"
    />

    <AdminUsersSection
      v-if="activeSection === 'users'"
      :users="users"
      :paged-users="pagedUsers"
      :users-page-size="usersPageSize"
      :users-page="usersPage"
      @update:usersPage="usersPage = $event"
      @create-user="openCreateUserDialog"
      @edit-user="openEditUserDialog"
      @delete-user="deleteUser"
    />

    <AdminRulesSection
      v-if="activeSection === 'rules'"
      :rules="rules"
      :paged-rules="pagedRules"
      :rules-page-size="rulesPageSize"
      :rules-page="rulesPage"
      @update:rulesPage="rulesPage = $event"
      @create-rule="openCreateRuleDialog"
      @edit-rule="openEditRuleDialog"
      @delete-rule="deleteRule"
    />

    <AdminAuditsSection
      v-if="activeSection === 'audits'"
      :pending-reports="pendingReports"
      :paged-pending-reports="pagedPendingReports"
      :pending-reports-page-size="pendingReportsPageSize"
      :pending-reports-page="pendingReportsPage"
      :resolve-image-url="resolveImageUrl"
      :fmt="fmt"
      @update:pendingReportsPage="pendingReportsPage = $event"
      @open-image="openImage"
      @audit="audit"
    />

    <AdminItemsSection
      v-if="activeSection === 'items'"
      :items="items"
      :paged-items="pagedItems"
      :items-page-size="itemsPageSize"
      :items-page="itemsPage"
      :enabled-item-count="enabledItemCount"
      :total-item-stock="totalItemStock"
      @update:itemsPage="itemsPage = $event"
      @create-item="openCreateItemDialog"
      @edit-item="openEditItemDialog"
      @delete-item="deleteItem"
    />

    <AdminOrdersSection
      v-if="activeSection === 'orders'"
      :orders="orders"
      :paged-orders="pagedOrders"
      :orders-page-size="ordersPageSize"
      :orders-page="ordersPage"
      :badge-class="badgeClass"
      :format-status-label="formatStatusLabel"
      :fmt="fmt"
      @update:ordersPage="ordersPage = $event"
      @update-order="updateOrder"
    />

    <UserDialog
      :open="userDialogOpen"
      :form="userForm"
      @close="closeUserDialog"
      @submit="submitUserDialog"
    />

    <RuleDialog
      :open="ruleDialogOpen"
      :form="ruleForm"
      @close="closeRuleDialog"
      @submit="submitRuleDialog"
    />

    <ItemDialog
      :open="itemDialogOpen"
      :form="itemForm"
      @close="closeItemDialog"
      @submit="submitItemDialog"
    />

    <ProfileCenterPanel
      v-if="activeSection === 'profile'"
      description-text="集中查看并维护你的基础资料、岗位信息与个人介绍。"
      :form="profileCenterForm"
      :avatar-url="profileAvatarUrl"
      :save-message="profileSaveMessage"
      :save-type="profileSaveType"
      :avatar-uploading="profileAvatarUploading"
      :set-avatar-input-ref="setProfileAvatarInput"
      role-meta-mark="岗"
      @save="saveProfile"
      @trigger-avatar-upload="triggerProfileAvatarUpload"
      @avatar-change="onProfileAvatarChange"
    />
  </WorkspaceShell>
</template>

<script setup>
import { ref, watch } from 'vue'
import AdminAuditsSection from '../components/admin/sections/AdminAuditsSection.vue'
import ItemDialog from '../components/admin/dialogs/ItemDialog.vue'
import AdminItemsSection from '../components/admin/sections/AdminItemsSection.vue'
import AdminOrdersSection from '../components/admin/sections/AdminOrdersSection.vue'
import AdminOverviewSection from '../components/admin/sections/AdminOverviewSection.vue'
import AdminRulesSection from '../components/admin/sections/AdminRulesSection.vue'
import AdminUsersSection from '../components/admin/sections/AdminUsersSection.vue'
import RuleDialog from '../components/admin/dialogs/RuleDialog.vue'
import UserDialog from '../components/admin/dialogs/UserDialog.vue'
import { useAdminDialogs } from '../composables/admin/useAdminDialogs'
import { useAdminPage } from '../composables/useAdminPage'
import { usePagination } from '../composables/shared/usePagination'
import { useProfileCenter } from '../composables/shared/useProfileCenter'
import { useWorkspaceState } from '../composables/workspace/useWorkspaceState'
import { updateAdminProfile, uploadImage } from '../services/adminService'
import ProfileCenterPanel from '../components/profile/ProfileCenterPanel.vue'
import WorkspaceShell from '../components/workspace/WorkspaceShell.vue'
import adminSections from '../constants/adminSections'

const workspaceStateStorageKey = 'lowcarbon:admin-workspace:v1'
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
  sections: adminSections
})
const avatarMenuOpen = ref(false)
const successDialogVisible = ref(false)
const successDialogMessage = ref('')

function openProfileCenter() {
  openSection('profile')
  avatarMenuOpen.value = false
}

function closeSuccessDialog() {
  successDialogVisible.value = false
}

const {
  message,
  messageType,
  clearMessage,
  profile,
  dashboard,
  users,
  rules,
  pendingReports,
  items,
  orders,
  userForm,
  ruleForm,
  itemForm,
  enabledItemCount,
  totalItemStock,
  loadAll,
  saveUser,
  editUser,
  resetUserForm,
  deleteUser,
  saveRule,
  editRule,
  resetRuleForm,
  deleteRule,
  audit,
  saveItem,
  editItem,
  resetItemForm,
  deleteItem,
  updateOrder,
  badgeClass,
  formatStatusLabel,
  fmt,
  resolveImageUrl,
  openImage,
  logout
} = useAdminPage()

watch([message, messageType], ([nextMessage, nextType]) => {
  if (nextType !== 'success' || !nextMessage) {
    return
  }
  successDialogMessage.value = nextMessage
  successDialogVisible.value = true
  clearMessage()
})

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
  storageKey: 'lowcarbon:admin-avatar:v1',
  defaults: {
    name: '社区管理员',
    gender: '保密',
    nickname: '运营管理员',
    email: 'admin@lowcarbon.local',
    phone: '13800009999',
    address: '广东省深圳市南山区低碳社区运营中心',
    bio: '负责社区低碳积分平台的规则配置、审核治理、商城履约与整体运营推进。',
    role: '社区运营管理员',
    city: '广东省深圳市',
    organization: '低碳社区运营中心',
    avatarUrl: '',
    tags: ['规则治理', '审核管理', '运营协同', '社区激励']
  },
  sourceWatcher: () => profile.value,
  resolveImageUrl,
  uploadImage,
  updateProfile: updateAdminProfile,
  applyProfileData: (data) => {
    profile.value = data
  },
  resolveDefaultEmail: () => 'admin@lowcarbon.local'
})

const {
  userDialogOpen,
  ruleDialogOpen,
  itemDialogOpen,
  openCreateUserDialog,
  openEditUserDialog,
  closeUserDialog,
  submitUserDialog,
  openCreateRuleDialog,
  openEditRuleDialog,
  closeRuleDialog,
  submitRuleDialog,
  openCreateItemDialog,
  openEditItemDialog,
  closeItemDialog,
  submitItemDialog
} = useAdminDialogs({
  userForm,
  ruleForm,
  itemForm,
  editUser,
  resetUserForm,
  saveUser,
  editRule,
  resetRuleForm,
  saveRule,
  editItem,
  resetItemForm,
  saveItem
})

const usersPageSize = 5
const rulesPageSize = 5
const pendingReportsPageSize = 5
const itemsPageSize = 5
const ordersPageSize = 5

const { currentPage: usersPage, pagedItems: pagedUsers } = usePagination(users, usersPageSize)
const { currentPage: rulesPage, pagedItems: pagedRules } = usePagination(rules, rulesPageSize)
const { currentPage: pendingReportsPage, pagedItems: pagedPendingReports } = usePagination(
  pendingReports,
  pendingReportsPageSize
)
const { currentPage: itemsPage, pagedItems: pagedItems } = usePagination(items, itemsPageSize)
const { currentPage: ordersPage, pagedItems: pagedOrders } = usePagination(orders, ordersPageSize)
</script>

<style src="../styles/admin-view.css"></style>
