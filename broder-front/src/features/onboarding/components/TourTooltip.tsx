import { useEffect, useRef, useState } from 'react'
import { Button } from '../../../shared/components/Button.tsx'
import './TourTooltip.css'

interface TourTooltipProps {
  step: number
  total: number
  title: string
  text: string
  targetSelector: string | null
  onNext: () => void
  onPrev: () => void
  onSkip: () => void
  isFirst: boolean
  isLast: boolean
}

export function TourTooltip({
  step,
  total,
  title,
  text,
  targetSelector,
  onNext,
  onPrev,
  onSkip,
  isFirst,
  isLast,
}: TourTooltipProps) {
  const tooltipRef = useRef<HTMLDivElement>(null)
  const [style, setStyle] = useState<React.CSSProperties>({
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
  })

  useEffect(() => {
    if (!targetSelector) return
    const target = document.querySelector(targetSelector) as HTMLElement | null
    if (!target || !tooltipRef.current) return

    const targetRect = target.getBoundingClientRect()
    const tooltipRect = tooltipRef.current.getBoundingClientRect()
    const margin = 12

    let top = targetRect.bottom + margin
    let left = targetRect.left + targetRect.width / 2 - tooltipRect.width / 2

    if (left < margin) left = margin
    if (left + tooltipRect.width > window.innerWidth - margin) {
      left = window.innerWidth - tooltipRect.width - margin
    }
    if (top + tooltipRect.height > window.innerHeight - margin) {
      top = targetRect.top - tooltipRect.height - margin
    }

    setStyle({ top, left, transform: 'none' })

    const originalPosition = target.style.position
    const originalZIndex = target.style.zIndex
    target.style.position = 'relative'
    target.style.zIndex = '201'
    target.scrollIntoView({ behavior: 'smooth', block: 'center' })

    return () => {
      target.style.position = originalPosition
      target.style.zIndex = originalZIndex
    }
  }, [targetSelector, step])

  return (
    <div
      ref={tooltipRef}
      className="tour-tooltip"
      style={style}
      role="dialog"
      aria-modal="true"
      aria-labelledby="tour-title"
    >
      <div className="tour-tooltip__header">
        <span className="tour-tooltip__badge">
          {step} / {total}
        </span>
        <button type="button" className="tour-tooltip__skip" onClick={onSkip}>
          Skip
        </button>
      </div>
      <h3 id="tour-title" className="tour-tooltip__title">
        {title}
      </h3>
      <p className="tour-tooltip__text">{text}</p>
      <div className="tour-tooltip__actions">
        {!isFirst && (
          <Button variant="ghost" size="sm" onClick={onPrev}>
            Back
          </Button>
        )}
        <Button variant="primary" size="sm" onClick={onNext}>
          {isLast ? 'Start!' : 'Next'}
        </Button>
      </div>
    </div>
  )
}
