import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { AlarmCard } from './AlarmCard.tsx'
import type { Alarm } from '../../api/alarmTypes.ts'

// Mock formatDateTime to return predictable strings
vi.mock('../../../shared/utils/formatters.ts', () => ({
  formatDateTime: (_date: string) => '2024-01-01 12:00',
}))

describe('AlarmCard', () => {
  const mockAlarm: Alarm = {
    id: 1,
    name: 'CPU Usage High',
    description: 'Alert when CPU > 80%',
    query: 'process_cpu_usage',
    comparison: '>',
    threshold: '0.8',
    enabled: true,
    status: 'ACTIVE',
    severity: 'HIGH',
    category: 'JVM',
    templateId: null,
    evaluationIntervalSeconds: 20,
    lastEvaluatedAt: null,
    lastFiredAt: null,
    usages: 0,
    createdAt: '2024-01-01T10:00:00Z',
  }

  const onToggle = vi.fn()
  const onDelete = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders alarm name, severity and status', () => {
    render(<AlarmCard alarm={mockAlarm} onToggle={onToggle} onDelete={onDelete} />)

    expect(screen.getByText('CPU Usage High')).toBeInTheDocument()
    expect(screen.getByText('HIGH')).toBeInTheDocument()
    expect(screen.getByText('ACTIVE')).toBeInTheDocument()
  })

  it('renders disabled state styling when alarm is disabled', () => {
    const disabledAlarm = { ...mockAlarm, enabled: false }
    const { container } = render(
      <AlarmCard alarm={disabledAlarm} onToggle={onToggle} onDelete={onDelete} />
    )

    expect(container.querySelector('.alarm-card--disabled')).toBeInTheDocument()
    expect(screen.getByText('Enable')).toBeInTheDocument()
  })

  it('calls onToggle when toggle button is clicked', () => {
    render(<AlarmCard alarm={mockAlarm} onToggle={onToggle} onDelete={onDelete} />)

    fireEvent.click(screen.getByText('Disable'))
    expect(onToggle).toHaveBeenCalledWith(1, false)
  })

  it('calls onDelete when delete button is clicked', () => {
    render(<AlarmCard alarm={mockAlarm} onToggle={onToggle} onDelete={onDelete} />)

    fireEvent.click(screen.getByText('Delete'))
    expect(onDelete).toHaveBeenCalledWith(1)
  })

  it('renders firing status with pulse animation', () => {
    const firingAlarm = { ...mockAlarm, status: 'FIRING', lastFiredAt: '2024-01-01T12:00:00Z' }
    const { container } = render(
      <AlarmCard alarm={firingAlarm} onToggle={onToggle} onDelete={onDelete} />
    )

    expect(container.querySelector('.alarm-card--firing')).toBeInTheDocument()
    expect(screen.getByText('FIRING')).toBeInTheDocument()
  })
})
