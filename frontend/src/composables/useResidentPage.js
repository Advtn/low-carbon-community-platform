import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  apiOrigin,
  createReport,
  fetchResidentData,
  redeemItem,
  uploadImage
} from '../services/residentService'
import {
  badgeClass,
  fmt,
  formatLedgerType,
  isToday,
  resolveImageUrl
} from '../utils/residentViewUtils'

export function useResidentPage() {
  const router = useRouter()
  const user = JSON.parse(sessionStorage.getItem('user') || '{}')

  const message = ref('')
  const profile = reactive({})
  const rules = ref([])
  const reports = ref([])
  const ledger = ref([])
  const items = ref([])
  const orders = ref([])
  const leaderboard = ref([])
  const imageInputRef = ref(null)
  const uploadingImage = ref(false)
  const proofImagePreviewUrl = ref('')

  const quickQuantities = [1, 2, 3, 5]
  const transportModes = [
    '\u6b65\u884c',
    '\u9a91\u884c',
    '\u516c\u4ea4',
    '\u5730\u94c1',
    '\u5176\u4ed6'
  ]
  const proofTemplates = [
    '\u4f4e\u78b3\u901a\u52e4',
    '\u5783\u573e\u5206\u7c7b\u6295\u653e',
    '\u81ea\u5e26\u6c34\u676f\u6d88\u8d39',
    '\u7a7a\u8c0326\u5ea6\u8bbe\u7f6e'
  ]

  const reportForm = reactive({
    ruleId: '',
    quantity: 1,
    proofText: '',
    proofImageUrl: ''
  })

  const assistForm = reactive({
    mode: '\u6b65\u884c',
    distance: '',
    location: ''
  })

  const selectedRule = computed(() =>
    rules.value.find((rule) => String(rule.id) === String(reportForm.ruleId))
  )

  const isCommuteRule = computed(() => {
    if (!selectedRule.value) return false
    return /\u6b65\u884c|\u9a91\u884c|\u51fa\u884c|\u901a\u52e4/.test(selectedRule.value.name || '')
  })

  const todayUsedCount = computed(() => {
    if (!selectedRule.value) return 0

    return reports.value
      .filter((row) => String(row.ruleId) === String(selectedRule.value.id))
      .filter((row) => ['PENDING', 'APPROVED'].includes(row.status))
      .filter((row) => isToday(row.submittedAt))
      .reduce((sum, row) => sum + Number(row.quantity || 0), 0)
  })

  const remainingQuota = computed(() => {
    if (!selectedRule.value) return 0
    return Math.max(Number(selectedRule.value.dailyLimit || 0) - todayUsedCount.value, 0)
  })

  const estimatedPoints = computed(() => {
    if (!selectedRule.value) return 0
    return Number(selectedRule.value.pointsPerAction || 0) * Number(reportForm.quantity || 0)
  })

  const estimatedCarbon = computed(() => {
    if (!selectedRule.value) return '0.00'
    const value =
      Number(selectedRule.value.carbonReductionPerAction || 0) * Number(reportForm.quantity || 0)
    return value.toFixed(2)
  })

  const quotaPercent = computed(() => {
    if (!selectedRule.value || Number(selectedRule.value.dailyLimit || 0) <= 0) return 0
    const percent = Math.round((todayUsedCount.value / Number(selectedRule.value.dailyLimit)) * 100)
    return Math.min(percent, 100)
  })

  onMounted(loadAll)

  async function loadAll() {
    message.value = ''
    try {
      const data = await fetchResidentData()
      Object.assign(profile, data.profile)
      rules.value = data.rules
      reports.value = data.reports
      ledger.value = data.ledger
      items.value = data.items
      orders.value = data.orders
      leaderboard.value = data.leaderboard
    } catch (e) {
      message.value = e.message
    }
  }

  async function submitReport() {
    message.value = ''

    if (uploadingImage.value) {
      message.value = '\u56fe\u7247\u4e0a\u4f20\u4e2d\uff0c\u8bf7\u7a0d\u5019\u518d\u63d0\u4ea4'
      return
    }

    if (!reportForm.ruleId) {
      message.value = '\u8bf7\u5148\u9009\u62e9\u884c\u4e3a\u89c4\u5219'
      return
    }

    const quantity = Number(reportForm.quantity)
    if (!Number.isInteger(quantity) || quantity <= 0) {
      message.value = '\u4e0a\u62a5\u6b21\u6570\u5fc5\u987b\u4e3a\u6b63\u6574\u6570'
      return
    }

    if (selectedRule.value && quantity > remainingQuota.value) {
      message.value = `\u8d85\u8fc7\u4eca\u65e5\u4e0a\u62a5\u4e0a\u9650\uff0c\u5f53\u524d\u5269\u4f59 ${remainingQuota.value} \u6b21`
      return
    }

    if (!reportForm.proofText.trim()) {
      message.value = '\u8bf7\u586b\u5199\u51ed\u8bc1\u6587\u5b57\u8bf4\u660e'
      return
    }

    if (!reportForm.proofImageUrl) {
      message.value = '\u8bf7\u5148\u4e0a\u4f20\u51ed\u8bc1\u56fe\u7247'
      return
    }

    const proofText = buildProofText()
    if (!proofText) {
      message.value = '\u8bf7\u586b\u5199\u51ed\u8bc1\u8bf4\u660e'
      return
    }

    try {
      await createReport({
        ruleId: Number(reportForm.ruleId),
        quantity,
        proofText,
        proofImageUrl: reportForm.proofImageUrl
      })
      resetReportForm()
      await loadAll()
    } catch (e) {
      message.value = e.message
    }
  }

  function buildProofText() {
    const fragments = []
    const base = reportForm.proofText.trim()
    if (base) fragments.push(base)

    if (isCommuteRule.value) {
      if (assistForm.mode) {
        fragments.push(`\u65b9\u5f0f:${assistForm.mode}`)
      }
      if (assistForm.distance && String(assistForm.distance).trim()) {
        fragments.push(`\u91cc\u7a0b:${String(assistForm.distance).trim()}km`)
      }
      if (assistForm.location) {
        fragments.push(`\u5730\u70b9:${assistForm.location.trim()}`)
      }
    }

    return fragments.join('\uff1b').slice(0, 500)
  }

  async function onProofImageChange(event) {
    message.value = ''
    const file = event.target?.files?.[0]
    if (!file) return
    await uploadProofImage(file)
  }

  async function uploadProofImage(file) {
    if (!file.type.startsWith('image/')) {
      message.value = '\u4ec5\u652f\u6301\u56fe\u7247\u683c\u5f0f\u6587\u4ef6'
      return
    }

    if (file.size > 5 * 1024 * 1024) {
      message.value = '\u56fe\u7247\u5927\u5c0f\u4e0d\u80fd\u8d85\u8fc7 5MB'
      return
    }

    uploadingImage.value = true
    try {
      const { data } = await uploadImage(file)
      reportForm.proofImageUrl = data.url
      proofImagePreviewUrl.value = resolveResidentImageUrl(data.url)
    } catch (e) {
      message.value = e.message
    } finally {
      uploadingImage.value = false
    }
  }

  function clearProofImage() {
    reportForm.proofImageUrl = ''
    proofImagePreviewUrl.value = ''
    if (imageInputRef.value) {
      imageInputRef.value.value = ''
    }
  }

  function setQuantity(value) {
    reportForm.quantity = value
  }

  function changeQuantity(step) {
    const current = Number(reportForm.quantity || 1)
    reportForm.quantity = Math.max(1, current + step)
  }

  function useTemplate(template) {
    reportForm.proofText = template
  }

  function resetReportForm() {
    reportForm.ruleId = ''
    reportForm.quantity = 1
    reportForm.proofText = ''
    reportForm.proofImageUrl = ''
    assistForm.mode = '\u6b65\u884c'
    assistForm.distance = ''
    assistForm.location = ''
    proofImagePreviewUrl.value = ''
    if (imageInputRef.value) {
      imageInputRef.value.value = ''
    }
  }

  async function redeem(item) {
    const input = window.prompt(
      `\u5151\u6362 ${item.name}\uff0c\u8bf7\u8f93\u5165\u6570\u91cf`,
      '1'
    )
    if (!input) return

    const quantity = Number(input)
    if (!Number.isInteger(quantity) || quantity <= 0) {
      message.value = '\u6570\u91cf\u5fc5\u987b\u4e3a\u6b63\u6574\u6570'
      return
    }

    try {
      await redeemItem({ itemId: item.id, quantity })
      await loadAll()
    } catch (e) {
      message.value = e.message
    }
  }

  function resolveResidentImageUrl(url) {
    return resolveImageUrl(url, apiOrigin)
  }

  function openImage(url) {
    if (!url) return
    window.open(url, '_blank')
  }

  function logout() {
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
    router.push('/login')
  }

  return {
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
    quickQuantities,
    transportModes,
    proofTemplates,
    reportForm,
    assistForm,
    selectedRule,
    isCommuteRule,
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
    redeem,
    resolveImageUrl: resolveResidentImageUrl,
    openImage,
    badgeClass,
    formatLedgerType,
    fmt,
    logout
  }
}
