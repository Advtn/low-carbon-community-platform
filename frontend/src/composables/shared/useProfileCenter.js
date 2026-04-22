import { computed, reactive, ref, watch } from 'vue'

const defaultProfileAvatarUrl = `data:image/svg+xml;utf8,${encodeURIComponent(
  `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 128 128"><defs><linearGradient id="g" x1="0" x2="1" y1="0" y2="1"><stop offset="0" stop-color="#9cc9b2"/><stop offset="1" stop-color="#e0c39f"/></linearGradient></defs><rect width="128" height="128" rx="28" fill="url(#g)"/><circle cx="64" cy="50" r="22" fill="#f7f5ef"/><path d="M24 118c4-22 19-34 40-34s36 12 40 34" fill="#f7f5ef"/></svg>`
)}`

function readCachedAvatarUrl(storageKey) {
  try {
    return localStorage.getItem(storageKey) || ''
  } catch {
    return ''
  }
}

function writeCachedAvatarUrl(storageKey, url) {
  try {
    if (url) {
      localStorage.setItem(storageKey, url)
      return
    }
    localStorage.removeItem(storageKey)
  } catch {
    // Ignore storage failures.
  }
}

function parseTags(value) {
  if (!value) {
    return []
  }

  return String(value)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function updateSessionUser(patch) {
  const raw = sessionStorage.getItem('user')
  if (!raw) {
    return
  }

  try {
    const parsed = JSON.parse(raw)
    sessionStorage.setItem('user', JSON.stringify({ ...parsed, ...patch }))
  } catch {
    // Ignore malformed session cache.
  }
}

function buildDefaultSavePayload(form) {
  return {
    fullName: form.name,
    nickname: form.nickname,
    gender: form.gender,
    email: form.email,
    phone: form.phone,
    address: form.address,
    bio: form.bio,
    city: form.city,
    organization: form.organization,
    tags: form.tags.join(','),
    avatarUrl: form.avatarUrl,
    avatar: form.avatarUrl
  }
}

export function useProfileCenter({
  storageKey,
  defaults,
  sourceWatcher,
  resolveImageUrl,
  uploadImage,
  updateProfile,
  applyProfileData,
  username = '',
  resolveDefaultEmail,
  buildSavePayload = buildDefaultSavePayload,
  roleLabels = {
    admin: '社区运营管理员',
    resident: '社区低碳居民'
  }
}) {
  const cachedAvatarUrl = readCachedAvatarUrl(storageKey)
  const profileCenterForm = reactive({
    ...defaults,
    avatarUrl: defaults.avatarUrl || cachedAvatarUrl,
    tags: Array.isArray(defaults.tags) ? [...defaults.tags] : []
  })
  const profileSaveMessage = ref('')
  const profileSaveType = ref('success')
  const profileAvatarInputRef = ref(null)
  const profileAvatarUploading = ref(false)

  function syncProfileCenterForm(source = {}) {
    const latestCachedAvatarUrl = readCachedAvatarUrl(storageKey)
    const sourceUsername = source.username || username || ''

    profileCenterForm.name =
      source.fullName ||
      source.nickname ||
      source.username ||
      profileCenterForm.name ||
      defaults.name
    profileCenterForm.gender = source.gender || defaults.gender || profileCenterForm.gender || '保密'
    profileCenterForm.nickname =
      source.nickname || profileCenterForm.nickname || defaults.nickname || ''
    profileCenterForm.email =
      source.email ||
      (typeof resolveDefaultEmail === 'function'
        ? resolveDefaultEmail({
            source,
            username: sourceUsername,
            defaults,
            currentForm: profileCenterForm
          })
        : defaults.email || profileCenterForm.email || '')
    profileCenterForm.phone = source.phone || defaults.phone || profileCenterForm.phone || ''
    profileCenterForm.address = source.address || defaults.address || profileCenterForm.address || ''
    profileCenterForm.bio = source.bio || defaults.bio || profileCenterForm.bio || ''
    profileCenterForm.role =
      source.role === 'ADMIN' ? roleLabels.admin : roleLabels.resident
    profileCenterForm.city = source.city || defaults.city || profileCenterForm.city || ''
    profileCenterForm.organization =
      source.organization || defaults.organization || profileCenterForm.organization || ''
    profileCenterForm.avatarUrl =
      source.avatarUrl ||
      source.avatar ||
      latestCachedAvatarUrl ||
      profileCenterForm.avatarUrl ||
      defaults.avatarUrl ||
      ''

    const nextTags = parseTags(source.tags)
    profileCenterForm.tags = nextTags.length
      ? nextTags
      : Array.isArray(defaults.tags)
        ? [...defaults.tags]
        : []

    writeCachedAvatarUrl(storageKey, profileCenterForm.avatarUrl)
  }

  function setProfileAvatarInput(element) {
    profileAvatarInputRef.value = element
  }

  function triggerProfileAvatarUpload() {
    if (profileAvatarUploading.value) return
    profileAvatarInputRef.value?.click()
  }

  async function onProfileAvatarChange(event) {
    const file = event?.target?.files?.[0]
    if (event?.target) {
      event.target.value = ''
    }
    if (!file) return

    if (!String(file.type || '').startsWith('image/')) {
      profileSaveType.value = 'error'
      profileSaveMessage.value = '请选择图片文件'
      return
    }
    if (Number(file.size || 0) > 5 * 1024 * 1024) {
      profileSaveType.value = 'error'
      profileSaveMessage.value = '头像文件不能超过 5MB'
      return
    }

    profileAvatarUploading.value = true
    profileSaveMessage.value = ''

    try {
      const { data } = await uploadImage(file)
      const avatarUrl = data?.url || data?.path || ''
      if (!avatarUrl) {
        throw new Error('头像地址为空')
      }

      profileCenterForm.avatarUrl = avatarUrl
      writeCachedAvatarUrl(storageKey, avatarUrl)
      profileSaveType.value = 'success'
      profileSaveMessage.value = '头像上传成功，请点击“保存资料”持久化'
    } catch (error) {
      profileSaveType.value = 'error'
      profileSaveMessage.value = error.message || '头像上传失败，请稍后重试'
    } finally {
      profileAvatarUploading.value = false
    }
  }

  async function saveProfile() {
    profileSaveMessage.value = ''

    try {
      const { data } = await updateProfile(buildSavePayload(profileCenterForm))
      syncProfileCenterForm(data)
      applyProfileData?.(data)

      const savedAvatarUrl = data?.avatarUrl || data?.avatar || profileCenterForm.avatarUrl || ''
      writeCachedAvatarUrl(storageKey, savedAvatarUrl)
      updateSessionUser({
        nickname: data?.nickname,
        avatarUrl: savedAvatarUrl
      })
      profileSaveType.value = 'success'
      profileSaveMessage.value = '个人资料已保存'
      return true
    } catch (error) {
      profileSaveType.value = 'error'
      profileSaveMessage.value = error.message || '保存失败，请稍后重试'
      return false
    }
  }

  watch(
    sourceWatcher,
    (value) => {
      syncProfileCenterForm(value || {})
    },
    { immediate: true, deep: true }
  )

  const profileAvatarUrl = computed(() => {
    const candidate = profileCenterForm.avatarUrl || ''
    if (!candidate) return defaultProfileAvatarUrl
    const resolved = resolveImageUrl(candidate)
    return resolved || defaultProfileAvatarUrl
  })

  return {
    profileCenterForm,
    profileSaveMessage,
    profileSaveType,
    profileAvatarUploading,
    profileAvatarUrl,
    setProfileAvatarInput,
    triggerProfileAvatarUpload,
    onProfileAvatarChange,
    saveProfile
  }
}
