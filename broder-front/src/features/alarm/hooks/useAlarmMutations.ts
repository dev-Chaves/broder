import { useCallback } from 'react'
import { alarmApi } from '../api/alarmApi.ts'

export function useAlarmMutations(onSuccess: () => void) {
  const toggleEnabled = useCallback(
    async (id: number, enabled: boolean) => {
      await alarmApi.update(id, { enabled })
      onSuccess()
    },
    [onSuccess]
  )

  const remove = useCallback(
    async (id: number) => {
      await alarmApi.delete(id)
      onSuccess()
    },
    [onSuccess]
  )

  return { toggleEnabled, remove }
}
