import { useMemo, useEffect } from 'react'
import { useAlarms } from '../hooks/useAlarms.ts'
import { useAlarmMutations } from '../hooks/useAlarmMutations.ts'
import { AlarmList } from '../components/AlarmList.tsx'
import './AlarmsPage.css'

export default function AlarmsPage() {
  const { alarms, loading, error, refetch } = useAlarms()
  const { toggleEnabled, remove, error: mutationError } = useAlarmMutations(refetch)

  useEffect(() => {
    const interval = setInterval(() => {
      refetch()
    }, 30000)
    return () => clearInterval(interval)
  }, [refetch])

  const firingAlarms = useMemo(() => {
    return alarms
      .filter((a) => a.status === 'FIRING')
      .sort((a, b) => {
        const sevOrder: Record<string, number> = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 }
        const sa = sevOrder[a.severity] ?? 99
        const sb = sevOrder[b.severity] ?? 99
        if (sa !== sb) return sa - sb
        return new Date(b.lastFiredAt || b.createdAt).getTime() - new Date(a.lastFiredAt || a.createdAt).getTime()
      })
  }, [alarms])

  const count = firingAlarms.length

  return (
    <div className="alarms-page">
      <div className="alarms-page__header">
        <div>
          <h1 className="alarms-page__title">Alarmes</h1>
          <p className="alarms-page__subtitle">
            {count > 0
              ? `${count} alarme${count > 1 ? 's' : ''} disparado${count > 1 ? 's' : ''}`
              : 'Nenhum alarme disparado'}
          </p>
        </div>
      </div>

      {mutationError && <div className="alarms-page__error" role="alert">{mutationError}</div>}

      <AlarmList
        alarms={firingAlarms}
        loading={loading}
        error={error}
        onToggle={toggleEnabled}
        onDelete={remove}
      />
    </div>
  )
}
