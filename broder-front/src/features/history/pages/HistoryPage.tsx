import { useState, useCallback, useMemo } from 'react'
import { ArrowLeft } from 'lucide-react'
import { useAlarms } from '../../alarm/hooks/useAlarms.ts'
import { useHistoryPage } from '../hooks/useHistoryPage.ts'
import { HistoryAlarmCard } from '../components/HistoryAlarmCard.tsx'
import { HistoryList } from '../components/HistoryList.tsx'
import { TopTriggered } from '../components/TopTriggered.tsx'
import { HistoryFilters, type PeriodKey } from '../components/HistoryFilters.tsx'
import { Button } from '../../../shared/components/Button.tsx'
import type { Alarm } from '../../alarm/api/alarmTypes.ts'
import type { HistoryEntry } from '../api/historyTypes.ts'
import './HistoryPage.css'

function matchesPeriod(entry: HistoryEntry, period: PeriodKey): boolean {
  if (period === 'all') return true
  const now = new Date()
  const d = new Date(entry.createdAt)
  const diffMs = now.getTime() - d.getTime()
  const diffDays = diffMs / (1000 * 60 * 60 * 24)

  if (period === 'today') {
    return d.getDate() === now.getDate() && d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear()
  }
  if (period === 'yesterday') {
    const yest = new Date(now)
    yest.setDate(yest.getDate() - 1)
    return d.getDate() === yest.getDate() && d.getMonth() === yest.getMonth() && d.getFullYear() === yest.getFullYear()
  }
  if (period === '7d') return diffDays <= 7
  if (period === '30d') return diffDays <= 30
  return true
}

export default function HistoryPage() {
  const { alarms, loading: alarmsLoading, error: alarmsError } = useAlarms()
  const [selectedAlarm, setSelectedAlarm] = useState<Alarm | null>(null)
  const [period, setPeriod] = useState<PeriodKey>('all')
  const [search, setSearch] = useState('')

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
    setPeriod('all')
    setSearch('')
  }, [])

  const filteredEntries = useMemo(() => {
    return entries
      .filter((e) => matchesPeriod(e, period))
      .filter((e) => {
        if (!search.trim()) return true
        return e.alarmName.toLowerCase().includes(search.trim().toLowerCase())
      })
  }, [entries, period, search])

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

        <TopTriggered entries={entries} />

        <HistoryFilters
          period={period}
          onPeriodChange={setPeriod}
          search={search}
          onSearchChange={setSearch}
        />

        <HistoryList
          entries={filteredEntries}
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
