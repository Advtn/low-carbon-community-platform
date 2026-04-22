import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import {
  isValidSectionId,
  persistWorkspaceState,
  restoreWorkspaceState
} from '../../utils/workspaceState'

export function useWorkspaceState({
  sections,
  storageKey,
  defaultSectionId = 'overview'
}) {
  const restoredWorkspaceState = restoreWorkspaceState({
    storageKey,
    sections,
    defaultSectionId
  })

  const sectionSearchKeyword = ref('')
  const sectionSearchInputRef = ref(null)
  const sidebarSections = computed(() =>
    sections.filter((section) => section.visibleInSidebar)
  )
  const filteredSidebarSections = computed(() => {
    const keyword = sectionSearchKeyword.value.trim().toLowerCase()
    if (!keyword) {
      return sidebarSections.value
    }

    return sidebarSections.value.filter((section) => {
      const searchableText = `${section.label} ${section.hint} ${section.description}`.toLowerCase()
      return searchableText.includes(keyword)
    })
  })

  const openTabs = ref(restoredWorkspaceState.openTabs)
  const activeSection = ref(restoredWorkspaceState.activeSection)

  const openTabSections = computed(() =>
    openTabs.value
      .map((id) => sections.find((section) => section.id === id))
      .filter(Boolean)
  )

  const activeSectionMeta = computed(
    () => sections.find((section) => section.id === activeSection.value) || null
  )

  function openSection(sectionId) {
    if (!isValidSectionId(sections, sectionId)) {
      return
    }
    if (!openTabs.value.includes(sectionId)) {
      openTabs.value.push(sectionId)
    }
    activeSection.value = sectionId
  }

  function closeSection(sectionId) {
    const index = openTabs.value.indexOf(sectionId)
    if (index === -1) return

    const nextTabs = openTabs.value.filter((id) => id !== sectionId)
    openTabs.value = nextTabs

    if (activeSection.value === sectionId) {
      if (nextTabs.length === 0) {
        activeSection.value = null
        return
      }

      const fallbackIndex = index > 0 ? index - 1 : 0
      activeSection.value = nextTabs[fallbackIndex] || nextTabs[0]
    }
  }

  function clearSectionSearch() {
    sectionSearchKeyword.value = ''
  }

  function openFirstMatchedSection() {
    const firstMatch = filteredSidebarSections.value[0]
    if (firstMatch) {
      openSection(firstMatch.id)
    }
  }

  function focusSectionSearch() {
    if (!sectionSearchInputRef.value) {
      return
    }
    sectionSearchInputRef.value.focus()
    sectionSearchInputRef.value.select()
  }

  function setSectionSearchInput(element) {
    sectionSearchInputRef.value = element
  }

  function handleWorkspaceShortcut(event) {
    if ((event.ctrlKey || event.metaKey) && String(event.key).toLowerCase() === 'k') {
      event.preventDefault()
      focusSectionSearch()
    }
  }

  watch(
    [openTabs, activeSection],
    () => {
      persistWorkspaceState({
        storageKey,
        openTabs: openTabs.value,
        activeSection: activeSection.value
      })
    },
    { deep: true }
  )

  onMounted(() => {
    window.addEventListener('keydown', handleWorkspaceShortcut)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('keydown', handleWorkspaceShortcut)
  })

  return {
    sectionSearchKeyword,
    setSectionSearchInput,
    sidebarSections,
    filteredSidebarSections,
    openTabs,
    activeSection,
    openTabSections,
    activeSectionMeta,
    openSection,
    closeSection,
    clearSectionSearch,
    openFirstMatchedSection,
    focusSectionSearch
  }
}
