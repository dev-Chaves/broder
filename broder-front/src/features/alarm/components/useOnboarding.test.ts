import { describe, it, expect, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useOnboarding } from '../../onboarding/hooks/useOnboarding.ts'

describe('useOnboarding', () => {
  let storage: Record<string, string> = {}

  beforeEach(() => {
    storage = {}
    Object.defineProperty(window, 'localStorage', {
      value: {
        getItem: (key: string) => storage[key] || null,
        setItem: (key: string, value: string) => { storage[key] = value },
        removeItem: (key: string) => { delete storage[key] },
        clear: () => { storage = {} },
      },
      writable: true,
    })
  })

  it('returns active=false initially before delay', () => {
    const { result } = renderHook(() => useOnboarding())

    expect(result.current.active).toBe(false)
    expect(result.current.step).toBe(0)
    expect(result.current.total).toBe(4)
  })

  it('advances to next step', () => {
    const { result } = renderHook(() => useOnboarding())

    act(() => {
      result.current.next()
    })

    expect(result.current.step).toBe(1)
  })

  it('completes tour after last step', () => {
    const { result } = renderHook(() => useOnboarding())

    act(() => {
      result.current.next() // step 1
      result.current.next() // step 2
      result.current.next() // step 3
      result.current.next() // complete
    })

    expect(result.current.active).toBe(false)
    expect(storage['broder_onboarding_completed']).toBe('true')
  })

  it('skip completes the tour immediately', () => {
    const { result } = renderHook(() => useOnboarding())

    act(() => {
      result.current.skip()
    })

    expect(result.current.active).toBe(false)
    expect(storage['broder_onboarding_completed']).toBe('true')
  })

  it('restart resets state and clears localStorage', () => {
    const { result } = renderHook(() => useOnboarding())

    act(() => {
      result.current.skip()
    })

    expect(result.current.active).toBe(false)

    act(() => {
      result.current.restart()
    })

    expect(result.current.active).toBe(true)
    expect(result.current.step).toBe(0)
    expect(storage['broder_onboarding_completed']).toBeUndefined()
  })

  it('does not go below step 0 when prev is called', () => {
    const { result } = renderHook(() => useOnboarding())

    act(() => {
      result.current.prev()
    })

    expect(result.current.step).toBe(0)
  })
})
