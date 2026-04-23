import { useDroppable } from '@dnd-kit/core'
import type { ReactNode } from 'react'

export function DropZone({ children }: { children: ReactNode }) {
  const { setNodeRef, isOver } = useDroppable({ id: 'builder-canvas' })

  return (
    <div
      ref={setNodeRef}
      className={`builder-drop-zone ${isOver ? 'builder-drop-zone--over' : ''}`}
    >
      {children}
    </div>
  )
}
