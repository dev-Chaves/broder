import { Bell, BarChart3, Clock } from 'lucide-react'
import { Badge } from '../../../shared/components/Badge.tsx'
import { Card } from '../../../shared/components/Card.tsx'
import { formatDateTime } from '../../../shared/utils/formatters.ts'
import type { Alarm } from '../../alarm/api/alarmTypes.ts'
import './HistoryAlarmCard.css'

const SEVERITY_VARIANT: Record<string, 'low' | 'medium' | 'high' | 'critical' | 'default'> = {
  LOW: 'low',
  MEDIUM: 'medium',
  HIGH: 'high',
  CRITICAL: 'critical',
}

interface HistoryAlarmCardProps {
  alarm: Alarm
  onClick: () => void
}

export function HistoryAlarmCard({ alarm, onClick }: HistoryAlarmCardProps) {
  return (
    <Card className={`history-alarm-card ${!alarm.enabled ? 'history-alarm-card--disabled' : ''}`} onClick={onClick}>
      <div className="history-alarm-card__header">
        <div className="history-alarm-card__icon-wrap">
          <Bell className="history-alarm-card__icon" aria-hidden="true" />
        </div>
        <div className="history-alarm-card__meta">
          <h3 className="history-alarm-card__name">{alarm.name}</h3>
          <span className="history-alarm-card__category">{alarm.category}</span>
        </div>
        <Badge variant={SEVERITY_VARIANT[alarm.severity] || 'default'}>{alarm.severity}</Badge>
      </div>

      <div className="history-alarm-card__details">
        <div className="history-alarm-detail">
          <BarChart3 className="history-alarm-detail__icon" aria-hidden="true" />
          <span>{alarm.comparison} {alarm.threshold}</span>
        </div>
        <div className="history-alarm-detail">
          <Clock className="history-alarm-detail__icon" aria-hidden="true" />
          <span>{formatDateTime(alarm.createdAt)}</span>
        </div>
      </div>

      <div className="history-alarm-card__hint">Clique para ver histórico</div>
    </Card>
  )
}
