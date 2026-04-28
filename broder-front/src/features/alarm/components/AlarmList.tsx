import { AlarmCard } from './AlarmCard.tsx'
import { Loading } from '../../../shared/components/Loading.tsx'
import type { Alarm } from '../api/alarmTypes.ts'
import './AlarmList.css'

interface AlarmListProps {
  alarms: Alarm[]
  loading: boolean
  error: string | null
  onToggle: (id: number, enabled: boolean) => void
  onDelete: (id: number) => void
}

export function AlarmList({ alarms, loading, error, onToggle, onDelete }: AlarmListProps) {
  if (loading) return <Loading message="Loading alarms..." />
  if (error) return <div className="alarm-list-error" role="alert">{error}</div>
  if (alarms.length === 0) {
    return (
      <div className="alarm-list-empty">
        <p>No alarms found with this filter.</p>
        <p className="alarm-list-empty__hint">Create a new alarm in the "Create Alarm" tab.</p>
      </div>
    )
  }

  return (
    <div className="alarm-list">
      {alarms.map((a) => (
        <AlarmCard key={a.id} alarm={a} onToggle={onToggle} onDelete={onDelete} />
      ))}
    </div>
  )
}
