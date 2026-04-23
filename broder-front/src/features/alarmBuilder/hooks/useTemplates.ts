import { useState, useEffect, useCallback } from 'react'
import { builderApi } from '../api/builderApi.ts'
import type { AlarmTemplate } from '../api/builderTypes.ts'

export function useTemplates() {
  const [templates, setTemplates] = useState<AlarmTemplate[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchTemplates = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await builderApi.listTemplates()
      setTemplates(data)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erro ao carregar templates')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchTemplates()
  }, [fetchTemplates])

  return { templates, loading, error, refetch: fetchTemplates }
}
