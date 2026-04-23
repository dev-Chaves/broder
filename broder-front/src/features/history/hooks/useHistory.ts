import { useState, useEffect } from 'react'
import { historyApi } from '../api/historyApi.ts'
import type { HistoryEntry } from '../api/historyTypes.ts'

export function useHistory(alarmId?: number) {
  const [entries, setEntries] = useState<HistoryEntry[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    const promise = alarmId ? historyApi.listByAlarm(alarmId) : historyApi.listAll()
    promise
      .then((page) => {
        if (!cancelled) {
          setEntries(page.content)
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
  }, [alarmId])

  const refetch = () => {
    setLoading(true)
    setError(null)
    const promise = alarmId ? historyApi.listByAlarm(alarmId) : historyApi.listAll()
    promise
      .then((page) => setEntries(page.content))
      .catch((e) => setError(e instanceof Error ? e.message : 'Erro ao carregar histórico'))
      .finally(() => setLoading(false))
  }

  return { entries, loading, error, refetch }
}
