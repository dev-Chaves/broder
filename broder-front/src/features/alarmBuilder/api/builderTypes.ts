export interface FilterInfo {
  key: string
  label: string
  placeholder: string
  description: string
}

export interface AlarmTemplate {
  id: string
  name: string
  description: string
  category: string
  defaultSeverity: string
  defaultComparison: string
  defaultThreshold: string
  filterInfo: FilterInfo[]
  unit: string
  queryTemplate: string
}

export interface AlarmBuilderRequest {
  name: string
  description?: string
  templateId: string
  filters: Record<string, string>
  comparison: string
  threshold: string
  severity?: string
  evaluationIntervalSeconds?: number
}

export interface AlarmPreviewResponse {
  generatedQuery: string
  currentValue: number | null
  threshold: string
  comparison: string
  wouldTrigger: boolean
  severity: string
  category: string
  status: string
  evaluatedAt: string
}
