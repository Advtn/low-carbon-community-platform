export function badgeClass(status) {
  if (status === 'APPROVED' || status === 'COMPLETED') return 'badge'
  if (status === 'PENDING') return 'badge warn'
  return 'badge err'
}

export function formatStatusLabel(status) {
  const map = {
    PENDING: '待处理',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status || '-'
}

export function fmt(v) {
  if (!v) return '-'
  return String(v).replace('T', ' ').slice(0, 19)
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
