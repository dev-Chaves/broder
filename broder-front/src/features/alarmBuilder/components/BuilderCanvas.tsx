import { useState, useCallback } from 'react'
import { Activity } from 'lucide-react'
import { Button } from '../../../shared/components/Button.tsx'
import { Card } from '../../../shared/components/Card.tsx'
import { builderApi } from '../api/builderApi.ts'
import type { AlarmTemplate, AlarmBuilderRequest, AlarmPreviewResponse } from '../api/builderTypes.ts'
import './BuilderCanvas.css'

const COMPARISONS = ['>', '<', '>=', '<=', '==']
const SEVERITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

interface BuilderCanvasProps {
  template: AlarmTemplate | null
  onCreated: () => void
}

export function BuilderCanvas({ template, onCreated }: BuilderCanvasProps) {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [threshold, setThreshold] = useState('')
  const [comparison, setComparison] = useState(template?.defaultComparison || '>')
  const [severity, setSeverity] = useState(template?.defaultSeverity || 'MEDIUM')
  const [filters, setFilters] = useState<Record<string, string>>({})
  const [preview, setPreview] = useState<AlarmPreviewResponse | null>(null)
  const [previewLoading, setPreviewLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handlePreview = useCallback(async () => {
    if (!template) return
    setPreviewLoading(true)
    setError(null)
    try {
      const dto: AlarmBuilderRequest = {
        name: name || template.name,
        description,
        templateId: template.id,
        filters,
        comparison,
        threshold: threshold !== '' ? threshold : template.defaultThreshold,
        severity,
      }
      const res = await builderApi.preview(dto)
      setPreview(res)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erro no preview')
    } finally {
      setPreviewLoading(false)
    }
  }, [template, name, description, filters, comparison, threshold, severity])

  const handleCreate = useCallback(async () => {
    if (!template) return
    setSubmitting(true)
    setError(null)
    try {
      const dto: AlarmBuilderRequest = {
        name: name || template.name,
        description,
        templateId: template.id,
        filters,
        comparison,
        threshold: threshold !== '' ? threshold : template.defaultThreshold,
        severity,
      }
      await builderApi.build(dto)
      onCreated()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erro ao criar alarme')
    } finally {
      setSubmitting(false)
    }
  }, [template, name, description, filters, comparison, threshold, severity, onCreated])

  if (!template) {
    return (
      <div className="builder-canvas builder-canvas--empty">
        <Activity className="builder-canvas__placeholder-icon" aria-hidden="true" />
        <p className="builder-canvas__placeholder-text">
          Arraste um template aqui para montar seu alarme
        </p>
      </div>
    )
  }

  return (
    <div className="builder-canvas">
      <h2 className="builder-canvas__title">Montando: {template.name}</h2>

      <div className="builder-canvas__form">
        <label className="field">
          <span className="field__label">Nome do alarme</span>
          <input
            className="field__input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder={template.name}
          />
        </label>

        <label className="field">
          <span className="field__label">Descrição</span>
          <input
            className="field__input"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Opcional"
          />
        </label>

        {template.supportedFilters.length > 0 && (
          <div className="field">
            <span className="field__label">Filtros</span>
            <div className="filters-grid">
              {template.supportedFilters.map((f) => (
                <label key={f} className="filter-field">
                  <span className="filter-field__name">{f}</span>
                  <input
                    className="field__input"
                    value={filters[f] || ''}
                    onChange={(e) =>
                      setFilters((prev) => ({ ...prev, [f]: e.target.value }))
                    }
                    placeholder={`ex: valor`}
                  />
                </label>
              ))}
            </div>
          </div>
        )}

        <div className="field">
          <span className="field__label">Comparação</span>
          <div className="comparison-group" role="group" aria-label="Operador de comparação">
            {COMPARISONS.map((c) => (
              <button
                key={c}
                type="button"
                className={`comparison-chip ${comparison === c ? 'comparison-chip--active' : ''}`}
                onClick={() => setComparison(c)}
                aria-pressed={comparison === c}
              >
                {c}
              </button>
            ))}
          </div>
        </div>

        <label className="field">
          <span className="field__label">
            Limite ({template.unit})
          </span>
          <input
            className="field__input"
            type="text"
            value={threshold}
            onChange={(e) => setThreshold(e.target.value)}
            placeholder={template.defaultThreshold}
          />
        </label>

        <div className="field">
          <span className="field__label">Severidade</span>
          <div className="severity-group" role="group" aria-label="Nível de severidade">
            {SEVERITIES.map((s) => (
              <button
                key={s}
                type="button"
                className={`severity-chip severity-chip--${s.toLowerCase()} ${severity === s ? 'severity-chip--active' : ''}`}
                onClick={() => setSeverity(s)}
                aria-pressed={severity === s}
              >
                {s}
              </button>
            ))}
          </div>
        </div>
      </div>

      {error && <div className="builder-error" role="alert">{error}</div>}

      <div className="builder-actions">
        <Button variant="secondary" onClick={handlePreview} disabled={previewLoading}>
          {previewLoading ? 'Analisando...' : 'Preview'}
        </Button>
        <Button variant="cta" onClick={handleCreate} disabled={submitting}>
          {submitting ? 'Criando...' : 'Criar Alarme'}
        </Button>
      </div>

      {preview && (
        <Card className="preview-card">
          <h3 className="preview-title">Resultado do Preview</h3>
          <div className="preview-grid">
            <div className="preview-item">
              <span className="preview-label">Valor atual</span>
              <span className="preview-value">{preview.currentValue ?? 'N/A'}</span>
            </div>
            <div className="preview-item">
              <span className="preview-label">Limite</span>
              <span className="preview-value">{preview.threshold}</span>
            </div>
            <div className="preview-item">
              <span className="preview-label">Comparação</span>
              <span className="preview-value">{preview.comparison}</span>
            </div>
            <div className="preview-item">
              <span className="preview-label">Dispararia?</span>
              <span className={`preview-value preview-value--${preview.wouldTrigger ? 'yes' : 'no'}`}>
                {preview.wouldTrigger ? 'Sim' : 'Não'}
              </span>
            </div>
          </div>
          <div className="preview-query">
            <span className="preview-label">Query gerada</span>
            <code className="preview-query__code">{preview.generatedQuery}</code>
          </div>
        </Card>
      )}
    </div>
  )
}
