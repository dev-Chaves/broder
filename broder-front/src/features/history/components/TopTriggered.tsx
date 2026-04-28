import { Zap } from 'lucide-react'
import type { HistoryEntry } from '../api/historyTypes.ts'
import './TopTriggered.css'

interface TopTriggeredProps {
  entries: HistoryEntry[]
}

export function TopTriggered({ entries }: TopTriggeredProps) {
  const counts = entries.reduce<Record<string, { name: string; count: number; lastAt: string }>>((acc, e) => {
    if (!acc[e.alarmName]) {
      acc[e.alarmName] = { name: e.alarmName, count: 0, lastAt: e.createdAt }
    }
    acc[e.alarmName].count += 1
    if (e.createdAt > acc[e.alarmName].lastAt) {
      acc[e.alarmName].lastAt = e.createdAt
    }
    return acc
  }, {})

  const sorted = Object.values(counts)
    .sort((a, b) => b.count - a.count)
    .slice(0, 5)

  if (sorted.length === 0) return null

  const maxCount = sorted[0].count

  return (
    <div className="top-triggered">
      <h2 className="top-triggered__title">
        <Zap className="top-triggered__icon" aria-hidden="true" />
        Mais Acionados
      </h2>
      <div className="top-triggered__list">
        {sorted.map((item) => (
          <div key={item.name} className="top-triggered__item">
            <div className="top-triggered__row">
              <span className="top-triggered__name" title={item.name}>{item.name}</span>
              <span className="top-triggered__count">{item.count}x</span>
            </div>
            <div className="top-triggered__bar-bg">
              <div
                className="top-triggered__bar-fill"
                style={{ width: `${(item.count / maxCount) * 100}%` }}
              />
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
