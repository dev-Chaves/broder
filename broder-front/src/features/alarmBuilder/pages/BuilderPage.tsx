import { useState, useCallback } from 'react'
import { DndContext, type DragEndEvent, useSensor, useSensors, PointerSensor, TouchSensor } from '@dnd-kit/core'
import { useNavigate } from 'react-router-dom'
import { TemplatePalette } from '../components/TemplatePalette.tsx'
import { BuilderCanvas } from '../components/BuilderCanvas.tsx'
import { DropZone } from '../components/DropZone.tsx'
import { useTemplates } from '../hooks/useTemplates.ts'
import type { AlarmTemplate } from '../api/builderTypes.ts'
import { OnboardingTour } from '../../onboarding/components/OnboardingTour.tsx'
import './BuilderPage.css'

export default function BuilderPage() {
  const { templates, loading, error } = useTemplates()
  const [selectedTemplate, setSelectedTemplate] = useState<AlarmTemplate | null>(null)
  const navigate = useNavigate()

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } }),
    useSensor(TouchSensor, { activationConstraint: { delay: 150, tolerance: 5 } })
  )

  const handleDragEnd = useCallback(
    (event: DragEndEvent) => {
      const { active, over } = event
      if (over && over.id === 'builder-canvas') {
        const template = templates.find((t) => t.id === active.id)
        if (template) {
          setSelectedTemplate(template)
        }
      }
    },
    [templates]
  )

  const handleCreated = useCallback(() => {
    setSelectedTemplate(null)
    navigate('/alarms')
  }, [navigate])

  return (
    <div className="builder-page">
      <OnboardingTour />
      <DndContext sensors={sensors} onDragEnd={handleDragEnd}>
        <TemplatePalette templates={templates} loading={loading} error={error} />
        <DropZone>
          <BuilderCanvas template={selectedTemplate} onCreated={handleCreated} />
        </DropZone>
      </DndContext>
    </div>
  )
}
