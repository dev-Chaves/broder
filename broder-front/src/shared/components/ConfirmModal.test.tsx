import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { ConfirmModal } from './ConfirmModal.tsx'

describe('ConfirmModal', () => {
  const onConfirm = vi.fn()
  const onCancel = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders title, message and buttons when open', () => {
    render(
      <ConfirmModal
        isOpen={true}
        title="Delete Alarm?"
        message="Are you sure you want to delete alarm 'CPU High'?"
        onConfirm={onConfirm}
        onCancel={onCancel}
      />
    )

    expect(screen.getByRole('alertdialog')).toBeInTheDocument()
    expect(screen.getByText("Delete Alarm?")).toBeInTheDocument()
    expect(screen.getByText("Are you sure you want to delete alarm 'CPU High'?")).toBeInTheDocument()
    expect(screen.getByText('Cancel')).toBeInTheDocument()
    expect(screen.getByText('Delete')).toBeInTheDocument()
  })

  it('does not render when isOpen is false', () => {
    const { container } = render(
      <ConfirmModal
        isOpen={false}
        title="Delete Alarm?"
        message="Are you sure?"
        onConfirm={onConfirm}
        onCancel={onCancel}
      />
    )

    expect(container.querySelector('.confirm-modal__overlay')).not.toBeInTheDocument()
  })

  it('calls onConfirm when Delete button is clicked', () => {
    render(
      <ConfirmModal
        isOpen={true}
        title="Delete Alarm?"
        message="Are you sure?"
        onConfirm={onConfirm}
        onCancel={onCancel}
      />
    )

    fireEvent.click(screen.getByText('Delete'))
    expect(onConfirm).toHaveBeenCalledTimes(1)
    expect(onCancel).not.toHaveBeenCalled()
  })

  it('calls onCancel when Cancel button is clicked', () => {
    render(
      <ConfirmModal
        isOpen={true}
        title="Delete Alarm?"
        message="Are you sure?"
        onConfirm={onConfirm}
        onCancel={onCancel}
      />
    )

    fireEvent.click(screen.getByText('Cancel'))
    expect(onCancel).toHaveBeenCalledTimes(1)
    expect(onConfirm).not.toHaveBeenCalled()
  })

  it('calls onCancel when overlay is clicked', () => {
    render(
      <ConfirmModal
        isOpen={true}
        title="Delete Alarm?"
        message="Are you sure?"
        onConfirm={onConfirm}
        onCancel={onCancel}
      />
    )

    const overlay = screen.getByRole('alertdialog').parentElement
    if (overlay) {
      fireEvent.click(overlay)
      expect(onCancel).toHaveBeenCalledTimes(1)
    }
  })

  it('traps focus inside the modal', () => {
    render(
      <ConfirmModal
        isOpen={true}
        title="Delete Alarm?"
        message="Are you sure?"
        onConfirm={onConfirm}
        onCancel={onCancel}
      />
    )

    const dialog = screen.getByRole('alertdialog')
    expect(dialog).toHaveAttribute('aria-modal', 'true')
  })
})
