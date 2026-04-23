import { useState, useEffect, useCallback } from 'react'
import { historyApi } from '../api/historyApi.ts'
import type { HistoryEntry } from '../api/historyTypes.ts'
import type { PageResponse } from '../../../shared/api/types.ts'

export function useHistoryPage(alarmId?: number, size = 20) {
  const [entries, setEntries] = useState<HistoryEntry[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchHistory = useCallback(
    async (p: number) => {
      setLoading(true)
      setError(null)
      try {
        const res: PageResponse<HistoryEntry> = alarmId
          ? await historyApi.listByAlarm(alarmId, p, size)
          : await historyApi.listAll(p, size)
        setEntries(res.content)
        setPage(res.page)
        setTotalPages(res.totalPages)
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Erro ao carregar histórico')
      } finally {
        setLoading(false)
      }
    },
    [alarmId, size]
  )

  useEffect(() => {
    fetchHistory(0)
  }, [fetchHistory])

  const goToPage = useCallback(
    (p: number) => {
      fetchHistory(p)
    },
    [fetchHistory]
  )

  return { entries, page, totalPages, loading, error, goToPage }
}
