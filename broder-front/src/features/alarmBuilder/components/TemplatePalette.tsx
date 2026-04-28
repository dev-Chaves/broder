import { TemplateCard } from './TemplateCard.tsx'
import { Loading } from '../../../shared/components/Loading.tsx'
import type { AlarmTemplate } from '../api/builderTypes.ts'
import './TemplatePalette.css'

interface TemplatePaletteProps {
  templates: AlarmTemplate[]
  loading: boolean
  error: string | null
  onSelect: (template: AlarmTemplate) => void
}

export function TemplatePalette({ templates, loading, error, onSelect }: TemplatePaletteProps) {
  if (loading) return <Loading message="Loading templates..." />
  if (error) return <div className="palette-error" role="alert">{error}</div>

  return (
    <section className="template-palette" aria-label="Alarm templates">
      <h2 className="palette-title">Choose a template</h2>
      <p className="palette-subtitle">Drag a card to the build area, or click to select</p>
      <div className="palette-grid">
        {templates.map((t) => (
          <TemplateCard key={t.id} template={t} onClick={() => onSelect(t)} />
        ))}
      </div>
    </section>
  )
}
