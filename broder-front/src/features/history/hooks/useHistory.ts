import { useState, useEffect, useCallback } from 'react'
import { historyApi } from '../api/historyApi.ts'
import type { HistoryEntry } from '../api/historyTypes.ts'

export function useHistory(alarmId?: number) {
  const [entries, setEntries] = useState<HistoryEntry[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchHistory = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const page = alarmId ? await historyApi.listByAlarm(alarmId) : await historyApi.listAll()
      setEntries(page.content)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erro ao carregar histórico')
    } finally {
      setLoading(false)
    }
  }, [alarmId])

  useEffect(() => {
    fetchHistory()
  }, [fetchHistory])

  return { entries, loading, error, refetch: fetchHistory }
}
