import { HistoryEntryComponent } from './HistoryEntry.tsx'
import { Loading } from '../../../shared/components/Loading.tsx'
import { Button } from '../../../shared/components/Button.tsx'
import type { HistoryEntry } from '../api/historyTypes.ts'
import './HistoryList.css'

interface HistoryListProps {
  entries: HistoryEntry[]
  loading: boolean
  error: string | null
  page: number
  totalPages: number
  onPageChange: (page: number) => void
}

export function HistoryList({ entries, loading, error, page, totalPages, onPageChange }: HistoryListProps) {
  if (loading) return <Loading message="Carregando histórico..." />
  if (error) return <div className="history-list-error">{error}</div>
  if (entries.length === 0) {
    return (
      <div className="history-list-empty">
        <p>Nenhum histórico encontrado.</p>
      </div>
    )
  }

  return (
    <div>
      <div className="history-list">
        {entries.map((e) => (
          <HistoryEntryComponent key={e.id} entry={e} />
        ))}
      </div>
      {totalPages > 1 && (
        <div className="history-pagination">
          <Button
            variant="secondary"
            size="sm"
            onClick={() => onPageChange(page - 1)}
            disabled={page <= 0}
          >
            Anterior
          </Button>
          <span className="history-pagination__info">
            Página {page + 1} de {totalPages}
          </span>
          <Button
            variant="secondary"
            size="sm"
            onClick={() => onPageChange(page + 1)}
            disabled={page >= totalPages - 1}
          >
            Próxima
          </Button>
        </div>
      )}
    </div>
  )
}
