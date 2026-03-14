export function isToday(datetimeText) {
  if (!datetimeText) return false

  const today = new Date()
  const value = new Date(datetimeText)
  return (
    value.getFullYear() === today.getFullYear() &&
    value.getMonth() === today.getMonth() &&
    value.getDate() === today.getDate()
  )
}

export function resolveImageUrl(url, apiOrigin) {
  if (!url) return ''

  if (/^https?:\/\//.test(url)) {
    try {
      const parsed = new URL(url)
      if (['localhost', '127.0.0.1'].includes(parsed.hostname)) {
        return `${apiOrigin}${parsed.pathname}`
      }
      return url
    } catch {
      return url
    }
  }

  if (url.startsWith('/')) {
    return `${apiOrigin}${url}`
  }

  return `${apiOrigin}/${url}`
}

export function badgeClass(status) {
  if (status === 'APPROVED' || status === 'COMPLETED') return 'badge'
  if (status === 'PENDING') return 'badge warn'
  return 'badge err'
}

export function formatLedgerType(type) {
  const map = {
    REPORT_REWARD: '\u884c\u4e3a\u5956\u52b1',
    REDEMPTION: '\u79ef\u5206\u5151\u6362',
    REFUND: '\u8ba2\u5355\u9000\u6b3e'
  }
  return map[type] || type || '-'
}

export function fmt(v) {
  if (!v) return '-'
  return String(v).replace('T', ' ').slice(0, 19)
}
