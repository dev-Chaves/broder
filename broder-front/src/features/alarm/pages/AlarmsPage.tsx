import { useState, useMemo } from 'react'
import { useAlarms } from '../hooks/useAlarms.ts'
import { useAlarmMutations } from '../hooks/useAlarmMutations.ts'
import { AlarmList } from '../components/AlarmList.tsx'
import './AlarmsPage.css'

type StatusFilter = 'ALL' | 'FIRING' | 'ACTIVE' | 'RESOLVED' | 'ERROR'

const FILTERS: { key: StatusFilter; label: string }[] = [
  { key: 'ALL', label: 'Todos' },
  { key: 'FIRING', label: 'Disparados' },
  { key: 'ACTIVE', label: 'Ativos' },
  { key: 'RESOLVED', label: 'Resolvidos' },
  { key: 'ERROR', label: 'Erros' },
]

export default function AlarmsPage() {
  const { alarms, loading, error, refetch } = useAlarms()
  const { toggleEnabled, remove, error: mutationError } = useAlarmMutations(refetch)
  const [activeFilter, setActiveFilter] = useState<StatusFilter>('ALL')

  const filteredAlarms = useMemo(() => {
    let result = activeFilter === 'ALL' ? alarms : alarms.filter((a) => a.status === activeFilter)

    // Ordenar: FIRING primeiro, depois ERROR, ACTIVE, RESOLVED
    const statusPriority: Record<string, number> = { FIRING: 0, ERROR: 1, ACTIVE: 2, RESOLVED: 3 }
    result = [...result].sort((a, b) => {
      const pa = statusPriority[a.status] ?? 99
      const pb = statusPriority[b.status] ?? 99
      return pa - pb
    })

    return result
  }, [alarms, activeFilter])

  const counts = useMemo(() => {
    return {
      ALL: alarms.length,
      FIRING: alarms.filter((a) => a.status === 'FIRING').length,
      ACTIVE: alarms.filter((a) => a.status === 'ACTIVE').length,
      RESOLVED: alarms.filter((a) => a.status === 'RESOLVED').length,
      ERROR: alarms.filter((a) => a.status === 'ERROR').length,
    }
  }, [alarms])

  return (
    <div className="alarms-page">
      <h1 className="alarms-page__title">Seus Alarmes</h1>
      <p className="alarms-page__subtitle">Monitore o status dos seus alarmes em tempo real</p>

      {mutationError && <div className="alarms-page__error" role="alert">{mutationError}</div>}

      <div className="alarms-filter" role="tablist" aria-label="Filtrar alarmes por status">
        {FILTERS.map((f) => (
          <button
            key={f.key}
            type="button"
            role="tab"
            aria-selected={activeFilter === f.key}
            className={`alarms-filter__chip ${activeFilter === f.key ? 'alarms-filter__chip--active' : ''}`}
            onClick={() => setActiveFilter(f.key)}
          >
            {f.label}
            <span className="alarms-filter__count">{counts[f.key]}</span>
          </button>
        ))}
      </div>

      <AlarmList
        alarms={filteredAlarms}
        loading={loading}
        error={error}
        onToggle={toggleEnabled}
        onDelete={remove}
      />
    </div>
  )
}
