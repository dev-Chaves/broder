import { AlertTriangle, CheckCircle2, Clock } from 'lucide-react'
import { Badge } from '../../../shared/components/Badge.tsx'
import { Card } from '../../../shared/components/Card.tsx'
import { formatDateTime, formatNumber } from '../../../shared/utils/formatters.ts'
import type { HistoryEntry } from '../api/historyTypes.ts'
import './HistoryEntry.css'

const STATUS_ICON: Record<string, React.ElementType> = {
  FIRING: AlertTriangle,
  RESOLVED: CheckCircle2,
}

const STATUS_VARIANT: Record<string, 'high' | 'default'> = {
  FIRING: 'high',
  RESOLVED: 'default',
}

interface HistoryEntryProps {
  entry: HistoryEntry
}

export function HistoryEntryComponent({ entry }: HistoryEntryProps) {
  const Icon = STATUS_ICON[entry.status] || Clock
  const variant = STATUS_VARIANT[entry.status] || 'default'

  return (
    <Card className="history-entry">
      <div className="history-entry__header">
        <div className="history-entry__icon-wrap">
          <Icon className="history-entry__icon" aria-hidden="true" />
        </div>
        <div className="history-entry__meta">
          <h3 className="history-entry__alarm">{entry.alarmName}</h3>
          <span className="history-entry__time">{formatDateTime(entry.createdAt)}</span>
        </div>
        <Badge variant={variant}>{entry.status}</Badge>
      </div>
      <div className="history-entry__value">
        <span className="history-entry__label">Valor medido:</span>
        <span className="history-entry__number">{formatNumber(entry.value ?? undefined)}</span>
      </div>
    </Card>
  )
}
