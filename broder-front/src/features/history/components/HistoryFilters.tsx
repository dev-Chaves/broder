import { Search } from 'lucide-react'
import './HistoryFilters.css'

const PERIODS = [
  { key: 'all', label: 'Todos' },
  { key: 'today', label: 'Hoje' },
  { key: 'yesterday', label: 'Ontem' },
  { key: '7d', label: '7 dias' },
  { key: '30d', label: '30 dias' },
] as const

type PeriodKey = (typeof PERIODS)[number]['key']

interface HistoryFiltersProps {
  period: PeriodKey
  onPeriodChange: (period: PeriodKey) => void
  search: string
  onSearchChange: (value: string) => void
}

export function HistoryFilters({ period, onPeriodChange, search, onSearchChange }: HistoryFiltersProps) {
  return (
    <div className="history-filters">
      <div className="history-filters__periods" role="tablist" aria-label="Filtrar por período">
        {PERIODS.map((p) => (
          <button
            key={p.key}
            type="button"
            role="tab"
            aria-selected={period === p.key}
            className={`history-filters__chip ${period === p.key ? 'history-filters__chip--active' : ''}`}
            onClick={() => onPeriodChange(p.key)}
          >
            {p.label}
          </button>
        ))}
      </div>
      <div className="history-filters__search">
        <Search className="history-filters__search-icon" aria-hidden="true" />
        <input
          type="text"
          className="history-filters__search-input"
          placeholder="Buscar alarme..."
          value={search}
          onChange={(e) => onSearchChange(e.target.value)}
        />
      </div>
    </div>
  )
}

export type { PeriodKey }
