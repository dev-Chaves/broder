import { useState, useEffect, useCallback } from 'react'
import { alarmApi } from '../api/alarmApi.ts'
import type { Alarm } from '../api/alarmTypes.ts'

export function useAlarms() {
  const [alarms, setAlarms] = useState<Alarm[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchAlarms = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const page = await alarmApi.listAll()
      setAlarms(page.content)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erro ao carregar alarmes')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchAlarms()
  }, [fetchAlarms])

  return { alarms, loading, error, refetch: fetchAlarms }
}
