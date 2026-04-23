import type { PageResponse } from '../../../shared/api/types.ts'
import { client } from '../../../shared/api/client.ts'
import type { HistoryEntry } from './historyTypes.ts'

export const historyApi = {
  listAll: (page = 0, size = 20) => client.get<PageResponse<HistoryEntry>>(`/history?page=${page}&size=${size}`),
  listByAlarm: (alarmId: number, page = 0, size = 20) =>
    client.get<PageResponse<HistoryEntry>>(`/history/alarm/${alarmId}?page=${page}&size=${size}`),
  findLatestByAlarm: (alarmId: number) => client.get<HistoryEntry>(`/history/alarm/${alarmId}/latest`),
}
