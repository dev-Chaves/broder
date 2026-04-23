export interface HistoryEntry {
  id: number
  alarmId: number
  alarmName: string
  status: string
  value: number | null
  createdAt: string
}
