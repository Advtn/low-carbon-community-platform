export function isValidSectionId(sections, sectionId) {
  return sections.some((section) => section.id === sectionId)
}

export function restoreWorkspaceState({
  storageKey,
  sections,
  defaultSectionId = 'overview'
}) {
  const defaultState = {
    openTabs: [defaultSectionId],
    activeSection: defaultSectionId
  }
  const raw = sessionStorage.getItem(storageKey)
  if (!raw) {
    return defaultState
  }

  try {
    const parsed = JSON.parse(raw)
    const openTabs = Array.isArray(parsed.openTabs)
      ? parsed.openTabs.filter((id) => isValidSectionId(sections, id))
      : []
    const activeSection = isValidSectionId(sections, parsed.activeSection)
      ? parsed.activeSection
      : openTabs[0]
    const normalizedTabs = openTabs.length ? openTabs : [defaultSectionId]

    if (activeSection && !normalizedTabs.includes(activeSection)) {
      normalizedTabs.push(activeSection)
    }

    return {
      openTabs: normalizedTabs,
      activeSection: activeSection || normalizedTabs[0] || null
    }
  } catch {
    return defaultState
  }
}

export function persistWorkspaceState({ storageKey, openTabs, activeSection }) {
  sessionStorage.setItem(
    storageKey,
    JSON.stringify({
      openTabs,
      activeSection
    })
  )
}
