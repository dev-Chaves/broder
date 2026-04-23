export interface Alarm {
  id: number
  name: string
  description: string | null
  query: string
  comparison: string
  threshold: string
  enabled: boolean
  status: string
  severity: string
  category: string
  templateId: string | null
  evaluationIntervalSeconds: number | null
  lastEvaluatedAt: string | null
  lastFiredAt: string | null
  usages: number | null
  createdAt: string
}

export interface AlarmRequest {
  name: string
  description?: string
  query: string
  comparison: string
  threshold: string
  evaluationIntervalSeconds?: number
  severity?: string
  category?: string
  templateId?: string
}

export interface AlarmUpdate {
  name?: string
  description?: string
  query?: string
  comparison?: string
  threshold?: string
  evaluationIntervalSeconds?: number
  severity?: string
  category?: string
  enabled?: boolean
}
