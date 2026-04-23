import { useState, useEffect, useCallback } from 'react'

const ONBOARDING_KEY = 'broder_onboarding_completed'
const TOTAL_STEPS = 4

export function useOnboarding() {
  const [completed, setCompleted] = useState(() => {
    try {
      return localStorage.getItem(ONBOARDING_KEY) === 'true'
    } catch {
      return false
    }
  })
  const [active, setActive] = useState(false)
  const [step, setStep] = useState(0)

  useEffect(() => {
    if (!completed) {
      const timer = setTimeout(() => setActive(true), 600)
      return () => clearTimeout(timer)
    }
  }, [completed])

  const next = useCallback(() => {
    setStep((s) => {
      if (s >= TOTAL_STEPS - 1) {
        setActive(false)
        setCompleted(true)
        localStorage.setItem(ONBOARDING_KEY, 'true')
        return s
      }
      return s + 1
    })
  }, [])

  const prev = useCallback(() => {
    setStep((s) => Math.max(0, s - 1))
  }, [])

  const skip = useCallback(() => {
    setActive(false)
    setCompleted(true)
    localStorage.setItem(ONBOARDING_KEY, 'true')
  }, [])

  const restart = useCallback(() => {
    setStep(0)
    setCompleted(false)
    setActive(true)
    localStorage.removeItem(ONBOARDING_KEY)
  }, [])

  return { active, step, total: TOTAL_STEPS, next, prev, skip, restart }
}
