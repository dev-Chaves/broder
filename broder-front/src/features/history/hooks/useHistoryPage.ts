import { useState, useEffect } from 'react'
import { historyApi } from '../api/historyApi.ts'
import type { HistoryEntry } from '../api/historyTypes.ts'
import type { PageResponse } from '../../../shared/api/types.ts'

export function useHistoryPage(alarmId?: number, size = 20) {
  const [entries, setEntries] = useState<HistoryEntry[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    const promise: Promise<PageResponse<HistoryEntry>> = alarmId
      ? historyApi.listByAlarm(alarmId, 0, size)
      : historyApi.listAll(0, size)
    promise
      .then((res) => {
        if (!cancelled) {
          setEntries(res.content)
          setPage(res.page)
          setTotalPages(res.totalPages)
        }
      })
      .catch((e) => {
        if (!cancelled) {
          setError(e instanceof Error ? e.message : 'Erro ao carregar histórico')
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false)
        }
      })
    return () => {
      cancelled = true
    }
  }, [alarmId, size])

  const goToPage = (p: number) => {
    setLoading(true)
    setError(null)
    const promise: Promise<PageResponse<HistoryEntry>> = alarmId
      ? historyApi.listByAlarm(alarmId, p, size)
      : historyApi.listAll(p, size)
    promise
      .then((res) => {
        setEntries(res.content)
        setPage(res.page)
        setTotalPages(res.totalPages)
      })
      .catch((e) => setError(e instanceof Error ? e.message : 'Erro ao carregar histórico'))
      .finally(() => setLoading(false))
  }

  return { entries, page, totalPages, loading, error, goToPage }
}
