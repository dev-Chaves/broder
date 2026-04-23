export function formatDateTime(iso: string | undefined): string {
  if (!iso) return '-'
  const d = new Date(iso)
  return d.toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function formatNumber(n: number | undefined): string {
  if (n === undefined || n === null) return '-'
  return n.toLocaleString('pt-BR', { maximumFractionDigits: 4 })
}
