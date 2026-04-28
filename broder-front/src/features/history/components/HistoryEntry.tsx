import { AlertTriangle, CheckCircle2, Clock } from 'lucide-react'
import { formatDateTime, formatNumber } from '../../../shared/utils/formatters.ts'
import type { HistoryEntry } from '../api/historyTypes.ts'
import './HistoryEntry.css'

const STATUS_ICON: Record<string, React.ElementType> = {
  FIRING: AlertTriangle,
  RESOLVED: CheckCircle2,
}

const STATUS_CLASS: Record<string, string> = {
  FIRING: 'history-entry__status--firing',
  RESOLVED: 'history-entry__status--resolved',
}

interface HistoryEntryProps {
  entry: HistoryEntry
}

export function HistoryEntryComponent({ entry }: HistoryEntryProps) {
  const Icon = STATUS_ICON[entry.status] || Clock
  const statusClass = STATUS_CLASS[entry.status] || ''

  return (
    <tr className="history-entry">
      <td className="history-entry__datetime">{formatDateTime(entry.createdAt)}</td>
      <td className="history-entry__alarm">{entry.alarmName}</td>
      <td className="history-entry__status">
        <span className={`history-entry__status-badge ${statusClass}`}>
          <Icon className="history-entry__status-icon" aria-hidden="true" />
          {entry.status}
        </span>
      </td>
      <td className="history-entry__value">{formatNumber(entry.value ?? undefined)}</td>
    </tr>
  )
}
