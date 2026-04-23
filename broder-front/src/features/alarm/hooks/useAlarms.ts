import { useState, useEffect } from 'react'
import { alarmApi } from '../api/alarmApi.ts'
import type { Alarm } from '../api/alarmTypes.ts'

export function useAlarms() {
  const [alarms, setAlarms] = useState<Alarm[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    alarmApi
      .listAll()
      .then((page) => {
        if (!cancelled) {
          setAlarms(page.content)
        }
      })
      .catch((e) => {
        if (!cancelled) {
          setError(e instanceof Error ? e.message : 'Erro ao carregar alarmes')
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
  }, [])

  const refetch = () => {
    setLoading(true)
    setError(null)
    alarmApi
      .listAll()
      .then((page) => setAlarms(page.content))
      .catch((e) => setError(e instanceof Error ? e.message : 'Erro ao carregar alarmes'))
      .finally(() => setLoading(false))
  }

  return { alarms, loading, error, refetch }
}
