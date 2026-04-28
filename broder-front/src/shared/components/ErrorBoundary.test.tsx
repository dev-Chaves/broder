import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { ErrorBoundary } from './ErrorBoundary.tsx'

const ThrowError = ({ shouldThrow }: { shouldThrow: boolean }) => {
  if (shouldThrow) {
    throw new Error('Test error')
  }
  return <div>Normal content</div>
}

describe('ErrorBoundary', () => {
  const consoleError = console.error

  beforeEach(() => {
    // Suppress React error boundary console errors during test
    console.error = vi.fn()
  })

  afterEach(() => {
    console.error = consoleError
  })

  it('renders children when no error occurs', () => {
    render(
      <ErrorBoundary>
        <div>Hello World</div>
      </ErrorBoundary>
    )

    expect(screen.getByText('Hello World')).toBeInTheDocument()
  })

  it('renders fallback UI when child throws', () => {
    render(
      <ErrorBoundary>
        <ThrowError shouldThrow={true} />
      </ErrorBoundary>
    )

    expect(screen.getByText('Something went wrong')).toBeInTheDocument()
    expect(screen.getByText('An unexpected error occurred. Please reload the page to continue.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /reload page/i })).toBeInTheDocument()
  })

  it('does not crash the entire app when one component fails', () => {
    render(
      <div>
        <ErrorBoundary>
          <ThrowError shouldThrow={true} />
        </ErrorBoundary>
        <div data-testid="sibling">Sibling content</div>
      </div>
    )

    expect(screen.getByText('Something went wrong')).toBeInTheDocument()
    expect(screen.getByTestId('sibling')).toBeInTheDocument()
  })
})
