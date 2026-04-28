import { Loader2 } from 'lucide-react'
import './Loading.css'

export function Loading({ message = 'Loading...' }: { message?: string }) {
  return (
    <div className="loading" role="status" aria-live="polite">
      <Loader2 className="loading__spinner" aria-hidden="true" />
      <span className="loading__text">{message}</span>
    </div>
  )
}
