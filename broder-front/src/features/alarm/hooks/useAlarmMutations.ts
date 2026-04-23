import { useCallback, useState } from 'react'
import { alarmApi } from '../api/alarmApi.ts'

export function useAlarmMutations(onSuccess: () => void) {
  const [error, setError] = useState<string | null>(null)

  const toggleEnabled = useCallback(
    async (id: number, enabled: boolean) => {
      try {
        setError(null)
        await alarmApi.update(id, { enabled })
        onSuccess()
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Erro ao atualizar alarme')
      }
    },
    [onSuccess]
  )

  const remove = useCallback(
    async (id: number) => {
      try {
        setError(null)
        await alarmApi.delete(id)
        onSuccess()
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Erro ao excluir alarme')
      }
    },
    [onSuccess]
  )

  return { toggleEnabled, remove, error }
}
