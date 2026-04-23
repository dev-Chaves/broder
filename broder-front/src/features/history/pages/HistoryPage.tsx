import { useState, useCallback } from 'react'
import { ArrowLeft } from 'lucide-react'
import { useAlarms } from '../../alarm/hooks/useAlarms.ts'
import { useHistoryPage } from '../hooks/useHistoryPage.ts'
import { HistoryAlarmCard } from '../components/HistoryAlarmCard.tsx'
import { HistoryList } from '../components/HistoryList.tsx'
import { Button } from '../../../shared/components/Button.tsx'
import type { Alarm } from '../../alarm/api/alarmTypes.ts'
import './HistoryPage.css'

export default function HistoryPage() {
  const { alarms, loading: alarmsLoading, error: alarmsError } = useAlarms()
  const [selectedAlarm, setSelectedAlarm] = useState<Alarm | null>(null)

  const {
    entries,
    page,
    totalPages,
    loading: historyLoading,
    error: historyError,
    goToPage,
  } = useHistoryPage(selectedAlarm?.id)

  const handleBack = useCallback(() => {
    setSelectedAlarm(null)
  }, [])

  if (selectedAlarm) {
    return (
      <div className="history-page">
        <div className="history-page__header">
          <Button variant="ghost" size="sm" onClick={handleBack}>
            <ArrowLeft className="btn-icon" aria-hidden="true" />
            Voltar
          </Button>
          <h1 className="history-page__title">Histórico: {selectedAlarm.name}</h1>
        </div>
        <HistoryList
          entries={entries}
          loading={historyLoading}
          error={historyError}
          page={page}
          totalPages={totalPages}
          onPageChange={goToPage}
        />
      </div>
    )
  }

  return (
    <div className="history-page">
      <h1 className="history-page__title">Histórico</h1>
      <p className="history-page__subtitle">Selecione um alarme para ver seu histórico</p>
      {alarmsError && <div className="history-page__error" role="alert">{alarmsError}</div>}
      {alarmsLoading ? (
        <p className="history-page__loading">Carregando alarmes...</p>
      ) : alarms.length === 0 ? (
        <div className="history-page__empty">
          <p>Nenhum alarme criado ainda.</p>
        </div>
      ) : (
        <div className="history-alarm-grid">
          {alarms.map((a) => (
            <HistoryAlarmCard key={a.id} alarm={a} onClick={() => setSelectedAlarm(a)} />
          ))}
        </div>
      )}
    </div>
  )
}
