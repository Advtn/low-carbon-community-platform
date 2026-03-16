import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png'
import markerIcon from 'leaflet/dist/images/marker-icon.png'
import markerShadow from 'leaflet/dist/images/marker-shadow.png'
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

delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow
})

const WALK_MODE = '\u6b65\u884c'
const CYCLE_MODE = '\u9a91\u884c'
const ROUTE_PROFILES = {
  [WALK_MODE]: 'walking',
  [CYCLE_MODE]: 'cycling'
}

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
  const mapContainerRef = ref(null)
  const map = ref(null)
  const routeLine = ref(null)
  const routeStartMarker = ref(null)
  const routeEndMarker = ref(null)
  const routeStartPoint = ref(null)
  const routeEndPoint = ref(null)
  const routeLoading = ref(false)
  const routeError = ref('')
  const locatingCurrent = ref(false)
  const currentLocationMarker = ref(null)

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
  const isRoutePlannerVisible = computed(
    () => isCommuteRule.value && [WALK_MODE, CYCLE_MODE].includes(assistForm.mode)
  )
  const routeDistanceKm = computed(() => {
    if (!routeStartPoint.value || !routeEndPoint.value) return ''
    return assistForm.distance ? String(assistForm.distance).trim() : ''
  })
  const routeStartText = computed(() =>
    routeStartPoint.value
      ? `${routeStartPoint.value.lat.toFixed(6)}, ${routeStartPoint.value.lng.toFixed(6)}`
      : '未选择'
  )
  const routeEndText = computed(() =>
    routeEndPoint.value
      ? `${routeEndPoint.value.lat.toFixed(6)}, ${routeEndPoint.value.lng.toFixed(6)}`
      : '未选择'
  )

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

  onMounted(() => {
    loadAll()
  })
  onBeforeUnmount(() => {
    destroyMap()
  })

  watch(
    isRoutePlannerVisible,
    async (visible) => {
      if (visible) {
        await nextTick()
        initMap()
        if (map.value) {
          map.value.invalidateSize()
        }
      } else {
        clearRouteSelection()
        destroyMap()
      }
    },
    { immediate: true }
  )

  watch(
    () => assistForm.mode,
    () => {
      if (!isRoutePlannerVisible.value) return
      if (routeStartPoint.value && routeEndPoint.value) {
        drawRouteAndDistance()
      }
    }
  )

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
      if (routeStartPoint.value) {
        fragments.push(
          `\u8d77\u70b9\u5750\u6807:${routeStartPoint.value.lat.toFixed(6)},${routeStartPoint.value.lng.toFixed(6)}`
        )
      }
      if (routeEndPoint.value) {
        fragments.push(
          `\u7ec8\u70b9\u5750\u6807:${routeEndPoint.value.lat.toFixed(6)},${routeEndPoint.value.lng.toFixed(6)}`
        )
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
    clearRouteSelection()
    if (map.value) {
      map.value.setView([31.2304, 121.4737], 12)
    }
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

  function initMap() {
    if (map.value || !mapContainerRef.value) return

    const mapInstance = L.map(mapContainerRef.value, {
      zoomControl: true
    }).setView([31.2304, 121.4737], 12)

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(mapInstance)

    mapInstance.on('click', handleMapClick)
    map.value = mapInstance
  }

  function destroyMap() {
    if (!map.value) return
    map.value.off('click', handleMapClick)
    map.value.remove()
    map.value = null
    routeLine.value = null
    routeStartMarker.value = null
    routeEndMarker.value = null
    currentLocationMarker.value = null
  }

  function clearRouteSelection() {
    routeError.value = ''
    routeLoading.value = false
    routeStartPoint.value = null
    routeEndPoint.value = null
    assistForm.distance = ''
    if (routeStartMarker.value) {
      routeStartMarker.value.remove()
      routeStartMarker.value = null
    }
    if (routeEndMarker.value) {
      routeEndMarker.value.remove()
      routeEndMarker.value = null
    }
    if (routeLine.value) {
      routeLine.value.remove()
      routeLine.value = null
    }
  }

  function locateCurrentPosition() {
    routeError.value = ''
    if (!map.value) {
      routeError.value = '地图未初始化，请稍后重试。'
      return
    }
    if (!navigator.geolocation) {
      routeError.value = '当前浏览器不支持定位功能。'
      return
    }

    locatingCurrent.value = true
    navigator.geolocation.getCurrentPosition(
      (position) => {
        locatingCurrent.value = false
        const point = {
          lat: position.coords.latitude,
          lng: position.coords.longitude
        }

        map.value.setView([point.lat, point.lng], 16)
        if (currentLocationMarker.value) {
          currentLocationMarker.value.remove()
        }
        currentLocationMarker.value = L.circleMarker([point.lat, point.lng], {
          radius: 7,
          color: '#1677ff',
          weight: 2,
          fillColor: '#4fa5ff',
          fillOpacity: 0.9
        }).addTo(map.value)

        if (!routeStartPoint.value) {
          routeStartPoint.value = point
          if (routeStartMarker.value) {
            routeStartMarker.value.remove()
          }
          routeStartMarker.value = L.marker([point.lat, point.lng]).addTo(map.value)
        }
      },
      (error) => {
        locatingCurrent.value = false
        if (error.code === 1) {
          routeError.value = '定位权限被拒绝，请在浏览器中允许位置权限。'
          return
        }
        if (error.code === 2) {
          routeError.value = '无法获取位置信息，请检查定位服务。'
          return
        }
        if (error.code === 3) {
          routeError.value = '定位超时，请重试。'
          return
        }
        routeError.value = '定位失败，请稍后重试。'
      },
      {
        enableHighAccuracy: true,
        timeout: 12000,
        maximumAge: 0
      }
    )
  }

  function handleMapClick(event) {
    routeError.value = ''
    const point = {
      lat: event.latlng.lat,
      lng: event.latlng.lng
    }

    if (!routeStartPoint.value || (routeStartPoint.value && routeEndPoint.value)) {
      clearRouteSelection()
      routeStartPoint.value = point
      routeStartMarker.value = L.marker([point.lat, point.lng]).addTo(map.value)
      return
    }

    routeEndPoint.value = point
    routeEndMarker.value = L.marker([point.lat, point.lng]).addTo(map.value)
    drawRouteAndDistance()
  }

  async function drawRouteAndDistance() {
    if (!routeStartPoint.value || !routeEndPoint.value || !map.value) return

    routeLoading.value = true
    routeError.value = ''
    const profile = ROUTE_PROFILES[assistForm.mode] || 'walking'
    const routeUrl = `https://router.project-osrm.org/route/v1/${profile}/${routeStartPoint.value.lng},${routeStartPoint.value.lat};${routeEndPoint.value.lng},${routeEndPoint.value.lat}?overview=full&geometries=geojson`

    try {
      const response = await fetch(routeUrl)
      if (!response.ok) {
        throw new Error('\u8def\u7ebf\u670d\u52a1\u8fd4\u56de\u5f02\u5e38')
      }

      const data = await response.json()
      const route = data?.routes?.[0]
      if (!route?.geometry?.coordinates?.length) {
        throw new Error('\u672a\u8ba1\u7b97\u5230\u6709\u6548\u8def\u7ebf')
      }

      const latLngs = route.geometry.coordinates.map(([lng, lat]) => [lat, lng])
      if (routeLine.value) {
        routeLine.value.remove()
      }
      routeLine.value = L.polyline(latLngs, {
        color: '#0f9d76',
        weight: 5,
        opacity: 0.85
      }).addTo(map.value)
      map.value.fitBounds(routeLine.value.getBounds(), { padding: [24, 24] })

      const distanceKm = (Number(route.distance || 0) / 1000).toFixed(2)
      assistForm.distance = distanceKm
    } catch (error) {
      const fallbackDistance = calcLineDistanceKm(routeStartPoint.value, routeEndPoint.value)
      assistForm.distance = fallbackDistance.toFixed(2)
      routeError.value =
        '\u8def\u7ebf\u670d\u52a1\u6682\u4e0d\u53ef\u7528\uff0c\u5df2\u4f7f\u7528\u76f4\u7ebf\u8ddd\u79bb\u4f30\u7b97\u3002'
    } finally {
      routeLoading.value = false
    }
  }

  function calcLineDistanceKm(a, b) {
    const toRad = (deg) => (deg * Math.PI) / 180
    const earthRadiusKm = 6371
    const dLat = toRad(b.lat - a.lat)
    const dLng = toRad(b.lng - a.lng)
    const lat1 = toRad(a.lat)
    const lat2 = toRad(b.lat)
    const haversine =
      Math.sin(dLat / 2) ** 2 +
      Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) ** 2
    return 2 * earthRadiusKm * Math.asin(Math.sqrt(haversine))
  }

  function resetRoutePlanner() {
    clearRouteSelection()
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
    resolveImageUrl: resolveResidentImageUrl,
    openImage,
    badgeClass,
    formatLedgerType,
    fmt,
    logout
  }
}
