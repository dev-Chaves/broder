import type { PageResponse } from '../../../shared/api/types.ts'
import { client } from '../../../shared/api/client.ts'
import type { Alarm, AlarmRequest, AlarmUpdate } from './alarmTypes.ts'

export const alarmApi = {
  listAll: (page = 0, size = 20) => client.get<PageResponse<Alarm>>(`/alarms?page=${page}&size=${size}`),
  findById: (id: number) => client.get<Alarm>(`/alarms/${id}`),
  save: (dto: AlarmRequest) => client.post<Alarm>('/alarms', dto),
  update: (id: number, dto: AlarmUpdate) => client.put<Alarm>(`/alarms/${id}`, dto),
  delete: (id: number) => client.delete<void>(`/alarms/${id}`),
}
