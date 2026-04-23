import { useState, useEffect } from 'react'
import { builderApi } from '../api/builderApi.ts'
import type { AlarmTemplate } from '../api/builderTypes.ts'

export function useTemplates() {
  const [templates, setTemplates] = useState<AlarmTemplate[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    builderApi
      .listTemplates()
      .then((data) => {
        if (!cancelled) {
          setTemplates(data)
        }
      })
      .catch((e) => {
        if (!cancelled) {
          setError(e instanceof Error ? e.message : 'Erro ao carregar templates')
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
    builderApi
      .listTemplates()
      .then((data) => setTemplates(data))
      .catch((e) => setError(e instanceof Error ? e.message : 'Erro ao carregar templates'))
      .finally(() => setLoading(false))
  }

  return { templates, loading, error, refetch }
}
