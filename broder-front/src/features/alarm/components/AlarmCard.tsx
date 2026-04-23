import { Bell, Trash2, Power, Clock, BarChart3 } from 'lucide-react'
import { Badge } from '../../../shared/components/Badge.tsx'
import { Button } from '../../../shared/components/Button.tsx'
import { Card } from '../../../shared/components/Card.tsx'
import { formatDateTime } from '../../../shared/utils/formatters.ts'
import type { Alarm } from '../api/alarmTypes.ts'
import './AlarmCard.css'

const SEVERITY_VARIANT: Record<string, 'low' | 'medium' | 'high' | 'critical' | 'default'> = {
  LOW: 'low',
  MEDIUM: 'medium',
  HIGH: 'high',
  CRITICAL: 'critical',
}

interface AlarmCardProps {
  alarm: Alarm
  onToggle: (id: number, enabled: boolean) => void
  onDelete: (id: number) => void
}

export function AlarmCard({ alarm, onToggle, onDelete }: AlarmCardProps) {
  return (
    <Card className={`alarm-card ${!alarm.enabled ? 'alarm-card--disabled' : ''}`}>
      <div className="alarm-card__header">
        <div className="alarm-card__icon-wrap">
          <Bell className="alarm-card__icon" aria-hidden="true" />
        </div>
        <div className="alarm-card__meta">
          <h3 className="alarm-card__name">{alarm.name}</h3>
          <span className="alarm-card__category">{alarm.category}</span>
        </div>
        <Badge variant={SEVERITY_VARIANT[alarm.severity] || 'default'}>{alarm.severity}</Badge>
      </div>

      {alarm.description && <p className="alarm-card__desc">{alarm.description}</p>}

      <div className="alarm-card__details">
        <div className="alarm-detail">
          <BarChart3 className="alarm-detail__icon" aria-hidden="true" />
          <span>{alarm.comparison} {alarm.threshold}</span>
        </div>
        <div className="alarm-detail">
          <Clock className="alarm-detail__icon" aria-hidden="true" />
          <span>{formatDateTime(alarm.createdAt)}</span>
        </div>
      </div>

      <div className="alarm-card__actions">
        <Button
          variant={alarm.enabled ? 'secondary' : 'primary'}
          size="sm"
          onClick={() => onToggle(alarm.id, !alarm.enabled)}
        >
          <Power className="btn-icon" aria-hidden="true" />
          {alarm.enabled ? 'Desativar' : 'Ativar'}
        </Button>
        <Button variant="ghost" size="sm" onClick={() => onDelete(alarm.id)}>
          <Trash2 className="btn-icon" aria-hidden="true" />
          Excluir
        </Button>
      </div>
    </Card>
  )
}
