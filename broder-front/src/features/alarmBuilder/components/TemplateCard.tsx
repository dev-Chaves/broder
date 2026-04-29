import { useDraggable } from '@dnd-kit/core'
import type { ElementType } from 'react'
import { Activity, Cpu, Database, Globe, Layers, Timer } from 'lucide-react'
import { Card } from '../../../shared/components/Card.tsx'
import type { AlarmTemplate } from '../api/builderTypes.ts'
import './TemplateCard.css'

const ICON_MAP: Record<string, ElementType> = {
  cpu_usage_high: Cpu,
  memory_heap_high: Layers,
  http_error_rate: Globe,
  http_client_error_rate: Globe,
  http_p50_latency: Timer,
  http_p90_latency: Timer,
  http_p99_latency: Timer,
  http_avg_response_time: Timer,
  http_rps: Activity,
  gc_pause_p95: Timer,
  db_connections_high: Database,
  thread_count_high: Cpu,
}

interface TemplateCardProps {
  template: AlarmTemplate
  onClick?: () => void
}

export function TemplateCard({ template, onClick }: TemplateCardProps) {
  const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({
    id: template.id,
    data: { template },
  })

  const style = transform
    ? { transform: `translate3d(${transform.x}px, ${transform.y}px, 0)` }
    : undefined

  const Icon = ICON_MAP[template.id] || Activity

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...listeners}
      {...attributes}
      className={`template-card ${isDragging ? 'template-card--dragging' : ''}`}
    >
      <Card className="template-card__inner" onClick={onClick}>
        <div className="template-card__icon-wrap">
          <Icon className="template-card__icon" aria-hidden="true" />
        </div>
        <h3 className="template-card__title">{template.name}</h3>
        <p className="template-card__desc">{template.description}</p>
        <div className="template-card__meta">
          <span className="template-card__category">{template.category}</span>
          <span className="template-card__unit">{template.unit}</span>
        </div>
      </Card>
    </div>
  )
}
