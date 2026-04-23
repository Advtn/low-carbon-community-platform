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
      :user-dialog-open="userDialogOpen"
      :user-form="userForm"
      :user-form-errors="userFormErrors"
      @update:usersPage="usersPage = $event"
      @create-user="openCreateUserDialog"
      @edit-user="openEditUserDialog"
      @delete-user="deleteUser"
      @close-user-dialog="closeUserDialog"
      @submit-user-dialog="submitUserDialog"
      @clear-user-field-error="clearUserFieldError"
    />

    <AdminRulesSection
      v-if="activeSection === 'rules'"
      :rules="rules"
      :paged-rules="pagedRules"
      :rules-page-size="rulesPageSize"
      :rules-page="rulesPage"
      :rule-dialog-open="ruleDialogOpen"
      :rule-form="ruleForm"
      :rule-form-errors="ruleFormErrors"
      @update:rulesPage="rulesPage = $event"
      @create-rule="openCreateRuleDialog"
      @edit-rule="openEditRuleDialog"
      @delete-rule="deleteRule"
      @close-rule-dialog="closeRuleDialog"
      @submit-rule-dialog="submitRuleDialog"
      @clear-rule-field-error="clearRuleFieldError"
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
      :item-dialog-open="itemDialogOpen"
      :item-form="itemForm"
      :item-form-errors="itemFormErrors"
      @update:itemsPage="itemsPage = $event"
      @create-item="openCreateItemDialog"
      @edit-item="openEditItemDialog"
      @delete-item="deleteItem"
      @close-item-dialog="closeItemDialog"
      @submit-item-dialog="submitItemDialog"
      @clear-item-field-error="clearItemFieldError"
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
import { reactive, ref, watch } from 'vue'
import AdminAuditsSection from '../components/admin/sections/AdminAuditsSection.vue'
import AdminItemsSection from '../components/admin/sections/AdminItemsSection.vue'
import AdminOrdersSection from '../components/admin/sections/AdminOrdersSection.vue'
import AdminOverviewSection from '../components/admin/sections/AdminOverviewSection.vue'
import AdminRulesSection from '../components/admin/sections/AdminRulesSection.vue'
import AdminUsersSection from '../components/admin/sections/AdminUsersSection.vue'
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
const userDialogOpen = ref(false)
const ruleDialogOpen = ref(false)
const itemDialogOpen = ref(false)
const successDialogVisible = ref(false)
const successDialogMessage = ref('')
const userFormErrors = reactive({
  username: '',
  nickname: '',
  password: '',
  role: ''
})
const ruleFormErrors = reactive({
  name: '',
  pointsPerAction: '',
  carbonReductionPerAction: '',
  dailyLimit: ''
})
const itemFormErrors = reactive({
  name: '',
  pointsCost: '',
  stock: ''
})

function openProfileCenter() {
  openSection('profile')
  avatarMenuOpen.value = false
}

function openCreateUserDialog() {
  resetUserForm()
  clearUserFormErrors()
  userDialogOpen.value = true
}

function openEditUserDialog(user) {
  editUser(user)
  clearUserFormErrors()
  userDialogOpen.value = true
}

function closeUserDialog() {
  userDialogOpen.value = false
  clearUserFormErrors()
  resetUserForm()
}

async function submitUserDialog() {
  clearUserFormErrors()
  if (!validateUserForm()) {
    return
  }
  const ok = await saveUser()
  if (ok) {
    userDialogOpen.value = false
  }
}

function openCreateRuleDialog() {
  resetRuleForm()
  clearRuleFormErrors()
  ruleDialogOpen.value = true
}

function openEditRuleDialog(rule) {
  editRule(rule)
  clearRuleFormErrors()
  ruleDialogOpen.value = true
}

function closeRuleDialog() {
  ruleDialogOpen.value = false
  clearRuleFormErrors()
  resetRuleForm()
}

async function submitRuleDialog() {
  clearRuleFormErrors()
  if (!validateRuleForm()) {
    return
  }
  const ok = await saveRule()
  if (ok) {
    ruleDialogOpen.value = false
  }
}

function openCreateItemDialog() {
  resetItemForm()
  clearItemFormErrors()
  itemDialogOpen.value = true
}

function openEditItemDialog(item) {
  editItem(item)
  clearItemFormErrors()
  itemDialogOpen.value = true
}

function closeItemDialog() {
  itemDialogOpen.value = false
  clearItemFormErrors()
  resetItemForm()
}

function closeSuccessDialog() {
  successDialogVisible.value = false
}

async function submitItemDialog() {
  clearItemFormErrors()
  if (!validateItemForm()) {
    return
  }
  const ok = await saveItem()
  if (ok) {
    itemDialogOpen.value = false
  }
}

function clearErrors(target) {
  Object.keys(target).forEach((key) => {
    target[key] = ''
  })
}

function clearUserFormErrors() {
  clearErrors(userFormErrors)
}

function clearRuleFormErrors() {
  clearErrors(ruleFormErrors)
}

function clearItemFormErrors() {
  clearErrors(itemFormErrors)
}

function clearUserFieldError(field) {
  userFormErrors[field] = ''
}

function clearRuleFieldError(field) {
  ruleFormErrors[field] = ''
}

function clearItemFieldError(field) {
  itemFormErrors[field] = ''
}

function validateUserForm() {
  if (!String(userForm.username || '').trim()) {
    userFormErrors.username = '请输入登录用户名'
  }
  if (!String(userForm.nickname || '').trim()) {
    userFormErrors.nickname = '请输入用户昵称'
  }
  if (!String(userForm.role || '').trim()) {
    userFormErrors.role = '请选择角色'
  }

  const password = String(userForm.password || '').trim()
  const isEditing = Boolean(userForm.id)
  if (!isEditing && !password) {
    userFormErrors.password = '请输入登录密码'
  } else if (password && password.length < 6) {
    userFormErrors.password = '登录密码至少 6 位'
  }

  return !Object.values(userFormErrors).some(Boolean)
}

function validateRuleForm() {
  if (!String(ruleForm.name || '').trim()) {
    ruleFormErrors.name = '请输入规则名称'
  }

  const pointsPerAction = Number(ruleForm.pointsPerAction)
  if (!Number.isFinite(pointsPerAction) || pointsPerAction < 0) {
    ruleFormErrors.pointsPerAction = '积分需为大于等于 0 的数字'
  }

  const carbonReductionPerAction = Number(ruleForm.carbonReductionPerAction)
  if (!Number.isFinite(carbonReductionPerAction) || carbonReductionPerAction < 0) {
    ruleFormErrors.carbonReductionPerAction = '减碳量需为大于等于 0 的数字'
  }

  const dailyLimit = Number(ruleForm.dailyLimit)
  if (!Number.isInteger(dailyLimit) || dailyLimit <= 0) {
    ruleFormErrors.dailyLimit = '每日上报上限需为正整数'
  }

  return !Object.values(ruleFormErrors).some(Boolean)
}

function validateItemForm() {
  if (!String(itemForm.name || '').trim()) {
    itemFormErrors.name = '请输入商品名称'
  }

  const pointsCost = Number(itemForm.pointsCost)
  if (!Number.isFinite(pointsCost) || pointsCost <= 0) {
    itemFormErrors.pointsCost = '积分单价需为大于 0 的数字'
  }

  const stock = Number(itemForm.stock)
  if (!Number.isInteger(stock) || stock < 0) {
    itemFormErrors.stock = '库存需为大于等于 0 的整数'
  }

  return !Object.values(itemFormErrors).some(Boolean)
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
