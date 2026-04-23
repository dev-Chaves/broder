import './Badge.css'

interface BadgeProps {
  children: string
  variant?: 'low' | 'medium' | 'high' | 'critical' | 'default'
}

const VARIANT_MAP = {
  low: 'badge--low',
  medium: 'badge--medium',
  high: 'badge--high',
  critical: 'badge--critical',
  default: 'badge--default',
} as const

export function Badge({ children, variant = 'default' }: BadgeProps) {
  return <span className={`badge ${VARIANT_MAP[variant]}`}>{children}</span>
}
