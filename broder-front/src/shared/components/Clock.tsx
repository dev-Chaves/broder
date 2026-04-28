import { useState, useEffect } from 'react'
import './Clock.css'

export function Clock() {
  const [now, setNow] = useState(new Date())

  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()), 1000)
    return () => clearInterval(timer)
  }, [])

  const dateStr = now.toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })

  const timeStr = now.toLocaleTimeString('pt-BR', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })

  return (
    <div className="clock" aria-label="Data e hora atual" role="timer">
      <span className="clock__date">{dateStr}</span>
      <span className="clock__time">{timeStr}</span>
    </div>
  )
}
