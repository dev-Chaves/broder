import { useAlarms } from '../hooks/useAlarms.ts'
import { useAlarmMutations } from '../hooks/useAlarmMutations.ts'
import { AlarmList } from '../components/AlarmList.tsx'
import './AlarmsPage.css'

export default function AlarmsPage() {
  const { alarms, loading, error, refetch } = useAlarms()
  const { toggleEnabled, remove } = useAlarmMutations(refetch)

  return (
    <div className="alarms-page">
      <h1 className="alarms-page__title">Seus Alarmes</h1>
      <AlarmList
        alarms={alarms}
        loading={loading}
        error={error}
        onToggle={toggleEnabled}
        onDelete={remove}
      />
    </div>
  )
}
