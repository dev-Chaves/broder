import { Bell, Trash2, Power, Clock, BarChart3, Zap, Activity, CheckCircle2, AlertTriangle } from 'lucide-react'
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

const STATUS_CONFIG: Record<string, { label: string; color: string; bg: string; icon: typeof Zap }> = {
  FIRING: { label: 'DISPARADO', color: '#EF4444', bg: 'rgba(220,38,38,0.15)', icon: Zap },
  ACTIVE: { label: 'ATIVO', color: '#22C55E', bg: 'rgba(34,197,94,0.12)', icon: Activity },
  RESOLVED: { label: 'RESOLVIDO', color: '#6B7280', bg: 'rgba(107,114,128,0.1)', icon: CheckCircle2 },
  ERROR: { label: 'ERRO', color: '#F59E0B', bg: 'rgba(245,158,11,0.12)', icon: AlertTriangle },
}

interface AlarmCardProps {
  alarm: Alarm
  onToggle: (id: number, enabled: boolean) => void
  onDelete: (id: number) => void
}

export function AlarmCard({ alarm, onToggle, onDelete }: AlarmCardProps) {
  const statusConfig = STATUS_CONFIG[alarm.status] || STATUS_CONFIG.ACTIVE
  const StatusIcon = statusConfig.icon
  const isFiring = alarm.status === 'FIRING'
  const isResolved = alarm.status === 'RESOLVED'

  return (
    <Card
      className={`alarm-card ${!alarm.enabled ? 'alarm-card--disabled' : ''} ${isFiring ? 'alarm-card--firing' : ''} ${isResolved ? 'alarm-card--resolved' : ''}`}
    >
      <div className="alarm-card__status-bar" style={{ background: statusConfig.color }} />

      <div className="alarm-card__header">
        <div className="alarm-card__icon-wrap" style={{ background: statusConfig.bg, color: statusConfig.color }}>
          <Bell className="alarm-card__icon" aria-hidden="true" />
        </div>
        <div className="alarm-card__meta">
          <h3 className="alarm-card__name">{alarm.name}</h3>
          {!isResolved && <span className="alarm-card__category">{alarm.category}</span>}
        </div>
        <div className="alarm-card__badges">
          <span
            className="alarm-card__status-badge"
            style={{ color: statusConfig.color, background: statusConfig.bg }}
          >
            <StatusIcon className="status-icon" aria-hidden="true" />
            {statusConfig.label}
          </span>
          {!isResolved && <Badge variant={SEVERITY_VARIANT[alarm.severity] || 'default'}>{alarm.severity}</Badge>}
        </div>
      </div>

      {alarm.description && !isResolved && <p className="alarm-card__desc">{alarm.description}</p>}

      {!isResolved && (
        <div className="alarm-card__details">
          <div className="alarm-detail">
            <BarChart3 className="alarm-detail__icon" aria-hidden="true" />
            <span>{alarm.comparison} {alarm.threshold}</span>
          </div>
          <div className="alarm-detail">
            <Clock className="alarm-detail__icon" aria-hidden="true" />
            <span>Criado: {formatDateTime(alarm.createdAt)}</span>
          </div>
          {alarm.lastEvaluatedAt && (
            <div className="alarm-detail">
              <Activity className="alarm-detail__icon" aria-hidden="true" />
              <span>Avaliado: {formatDateTime(alarm.lastEvaluatedAt)}</span>
            </div>
          )}
          {alarm.lastFiredAt && (
            <div className="alarm-detail alarm-detail--firing">
              <Zap className="alarm-detail__icon" aria-hidden="true" />
              <span>Disparou: {formatDateTime(alarm.lastFiredAt)}</span>
            </div>
          )}
          {alarm.usages !== null && alarm.usages > 0 && (
            <div className="alarm-detail">
              <span className="alarm-detail__count">{alarm.usages}x disparos</span>
            </div>
          )}
        </div>
      )}

      {!isResolved && (
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
      )}
    </Card>
  )
}
