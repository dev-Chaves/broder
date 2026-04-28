import { HistoryTable } from './HistoryTable.tsx'
import type { HistoryEntry } from '../api/historyTypes.ts'

interface HistoryListProps {
  entries: HistoryEntry[]
  loading: boolean
  error: string | null
  page: number
  totalPages: number
  onPageChange: (page: number) => void
}

export function HistoryList({ entries, loading, error, page, totalPages, onPageChange }: HistoryListProps) {
  return (
    <HistoryTable
      entries={entries}
      loading={loading}
      error={error}
      page={page}
      totalPages={totalPages}
      onPageChange={onPageChange}
    />
  )
}
