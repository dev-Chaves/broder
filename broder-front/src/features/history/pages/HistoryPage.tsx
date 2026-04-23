import { useHistoryPage } from '../hooks/useHistoryPage.ts'
import { HistoryList } from '../components/HistoryList.tsx'
import './HistoryPage.css'

export default function HistoryPage() {
  const { entries, page, totalPages, loading, error, goToPage } = useHistoryPage()

  return (
    <div className="history-page">
      <h1 className="history-page__title">Histórico</h1>
      <HistoryList
        entries={entries}
        loading={loading}
        error={error}
        page={page}
        totalPages={totalPages}
        onPageChange={goToPage}
      />
    </div>
  )
}
