import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  apiOrigin,
  auditReport,
  createItem,
  createRule,
  createUser,
  deleteItemById,
  deleteRuleById,
  deleteUserById,
  fetchAdminData,
  logoutAccount,
  updateItem,
  updateOrderStatus,
  updateRule,
  updateUser
} from '../services/adminService'
import { badgeClass, fmt, formatStatusLabel, resolveImageUrl } from '../utils/adminViewUtils'

export function useAdminPage() {
  const router = useRouter()
  let messageTimer = null

  const message = ref('')
  const messageType = ref('error')
  const profile = ref({})
  const dashboard = ref({
    residentCount: 0,
    activeRuleCount: 0,
    pendingReportCount: 0,
    pendingOrderCount: 0,
    enabledItemCount: 0,
    enabledItemStock: 0,
    totalResidentPoints: 0,
    totalCarbonReduction: 0,
    todaySubmittedReports: 0,
    todayApprovedReports: 0,
    todayRedeemOrders: 0,
    todayCompletedOrders: 0,
    updatedAt: ''
  })
  const users = ref([])
  const rules = ref([])
  const pendingReports = ref([])
  const items = ref([])
  const orders = ref([])

  const userForm = reactive({
    id: null,
    username: '',
    password: '',
    nickname: '',
    role: 'RESIDENT'
  })

  const ruleForm = reactive({
    id: null,
    name: '',
    description: '',
    pointsPerAction: 10,
    carbonReductionPerAction: 0.5,
    dailyLimit: 3,
    active: true
  })

  const itemForm = reactive({
    id: null,
    name: '',
    description: '',
    pointsCost: 100,
    stock: 10,
    enabled: true
  })

  const enabledItemCount = computed(() => items.value.filter((item) => item.enabled).length)
  const totalItemStock = computed(() =>
    items.value.reduce((sum, item) => sum + Number(item.stock || 0), 0)
  )

  function clearMessageTimer() {
    if (messageTimer !== null) {
      clearTimeout(messageTimer)
      messageTimer = null
    }
  }

  function setMessage(text, type = 'error') {
    clearMessageTimer()
    message.value = text
    messageType.value = type
    if (!text) return

    const duration = type === 'success' ? 3600 : 5000
    messageTimer = setTimeout(() => {
      clearMessage()
    }, duration)
  }

  function clearMessage() {
    clearMessageTimer()
    message.value = ''
    messageType.value = 'error'
  }

  onMounted(loadAll)
  onBeforeUnmount(clearMessageTimer)

  async function loadAll(options = {}) {
    const { keepMessage = false } = options
    if (!keepMessage) {
      clearMessage()
    }

    try {
      const data = await fetchAdminData()
      profile.value = data.profile || profile.value
      dashboard.value = data.dashboard || dashboard.value
      users.value = data.users
      rules.value = data.rules
      pendingReports.value = data.pendingReports
      items.value = data.items
      orders.value = data.orders
      return true
    } catch (e) {
      setMessage(e.message)
      return false
    }
  }

  async function saveUser() {
    const isEditing = Boolean(userForm.id)
    try {
      if (isEditing) {
        await updateUser(userForm.id, userForm)
      } else {
        await createUser(userForm)
      }
      resetUserForm()
      const ok = await loadAll({ keepMessage: true })
      if (ok) {
        setMessage(isEditing ? '用户更新成功' : '用户新增成功', 'success')
        return true
      }
      return false
    } catch (e) {
      setMessage(e.message)
      return false
    }
  }

  function editUser(u) {
    Object.assign(userForm, {
      id: u.id,
      username: u.username,
      password: '',
      nickname: u.nickname,
      role: u.role
    })
  }

  function resetUserForm() {
    Object.assign(userForm, {
      id: null,
      username: '',
      password: '',
      nickname: '',
      role: 'RESIDENT'
    })
  }

  async function deleteUser(id) {
    if (!window.confirm('\u786e\u8ba4\u5220\u9664\u8be5\u7528\u6237\uff1f')) return
    try {
      await deleteUserById(id)
      const ok = await loadAll({ keepMessage: true })
      if (ok) {
        setMessage('用户删除成功', 'success')
      }
    } catch (e) {
      setMessage(e.message)
    }
  }

  async function saveRule() {
    const isEditing = Boolean(ruleForm.id)
    try {
      if (isEditing) {
        await updateRule(ruleForm.id, ruleForm)
      } else {
        await createRule(ruleForm)
      }
      resetRuleForm()
      const ok = await loadAll({ keepMessage: true })
      if (ok) {
        setMessage(isEditing ? '规则更新成功' : '规则新增成功', 'success')
        return true
      }
      return false
    } catch (e) {
      setMessage(e.message)
      return false
    }
  }

  function editRule(r) {
    Object.assign(ruleForm, {
      id: r.id,
      name: r.name,
      description: r.description,
      pointsPerAction: r.pointsPerAction,
      carbonReductionPerAction: r.carbonReductionPerAction,
      dailyLimit: r.dailyLimit,
      active: r.active
    })
  }

  function resetRuleForm() {
    Object.assign(ruleForm, {
      id: null,
      name: '',
      description: '',
      pointsPerAction: 10,
      carbonReductionPerAction: 0.5,
      dailyLimit: 3,
      active: true
    })
  }

  async function deleteRule(id) {
    if (!window.confirm('\u786e\u8ba4\u5220\u9664\u8be5\u89c4\u5219\uff1f')) return
    try {
      await deleteRuleById(id)
      const ok = await loadAll({ keepMessage: true })
      if (ok) {
        setMessage('规则删除成功', 'success')
      }
    } catch (e) {
      setMessage(e.message)
    }
  }

  async function audit(id, approved) {
    try {
      let remark = '\u5ba1\u6838\u901a\u8fc7'
      if (!approved) {
        const input = window.prompt(
          '\u8bf7\u8f93\u5165\u9a73\u56de\u539f\u56e0',
          '\u6750\u6599\u4e0d\u8db3'
        )
        if (input === null) {
          return
        }
        remark = input.trim() || '\u5ba1\u6838\u9a73\u56de'
      }
      await auditReport(id, { approved, remark })
      const ok = await loadAll({ keepMessage: true })
      if (ok) {
        setMessage(approved ? '审核通过成功' : '审核驳回成功', 'success')
      }
    } catch (e) {
      setMessage(e.message)
    }
  }

  async function saveItem() {
    const isEditing = Boolean(itemForm.id)
    try {
      if (isEditing) {
        await updateItem(itemForm.id, itemForm)
      } else {
        await createItem(itemForm)
      }
      resetItemForm()
      const ok = await loadAll({ keepMessage: true })
      if (ok) {
        setMessage(isEditing ? '商品更新成功' : '商品新增成功', 'success')
        return true
      }
      return false
    } catch (e) {
      setMessage(e.message)
      return false
    }
  }

  function editItem(item) {
    Object.assign(itemForm, {
      id: item.id,
      name: item.name,
      description: item.description,
      pointsCost: item.pointsCost,
      stock: item.stock,
      enabled: item.enabled
    })
  }

  function resetItemForm() {
    Object.assign(itemForm, {
      id: null,
      name: '',
      description: '',
      pointsCost: 100,
      stock: 10,
      enabled: true
    })
  }

  async function deleteItem(id) {
    if (!window.confirm('\u786e\u8ba4\u5220\u9664\u8be5\u5546\u54c1\uff1f')) return
    try {
      await deleteItemById(id)
      const ok = await loadAll({ keepMessage: true })
      if (ok) {
        setMessage('商品删除成功', 'success')
      }
    } catch (e) {
      setMessage(e.message)
    }
  }

  async function updateOrder(id, status) {
    const targetOrder = orders.value.find((order) => Number(order.id) === Number(id))
    if (targetOrder && targetOrder.status !== 'PENDING') {
      setMessage('订单已结束，无法再次修改状态')
      return
    }

    try {
      const remark =
        status === 'COMPLETED'
          ? '\u5df2\u53d1\u653e'
          : '\u7ba1\u7406\u5458\u53d6\u6d88\u8ba2\u5355'
      await updateOrderStatus(id, { status, remark })
      const ok = await loadAll({ keepMessage: true })
      if (ok) {
        setMessage(
          status === 'COMPLETED' ? '订单已标记为完成' : '订单已取消并完成退款',
          'success'
        )
      }
    } catch (e) {
      setMessage(e.message)
    }
  }

  function resolveAdminImageUrl(url) {
    return resolveImageUrl(url, apiOrigin)
  }

  function openImage(url) {
    if (!url) return
    window.open(url, '_blank')
  }

  async function logout() {
    try {
      await logoutAccount()
    } catch (e) {
      // Ignore logout request failures and clear local session anyway.
    } finally {
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
      router.push('/login')
    }
  }

  return {
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
    resolveImageUrl: resolveAdminImageUrl,
    openImage,
    logout
  }
}
