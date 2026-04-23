import { client } from '../../../shared/api/client.ts'
import type { AlarmTemplate, AlarmBuilderRequest, AlarmPreviewResponse } from './builderTypes.ts'

export const builderApi = {
  listTemplates: () => client.get<AlarmTemplate[]>('/alarms/builder/templates'),
  listCategories: () => client.get<string[]>('/alarms/builder/categories'),
  listMetrics: () => client.get<unknown[]>('/alarms/builder/metrics'),
  preview: (dto: AlarmBuilderRequest) => client.post<AlarmPreviewResponse>('/alarms/builder/preview', dto),
  validate: (dto: AlarmBuilderRequest) => client.post<unknown>('/alarms/builder/validate', dto),
  build: (dto: AlarmBuilderRequest) => client.post<unknown>('/alarms/builder', dto),
}
