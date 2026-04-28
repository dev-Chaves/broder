import { useOnboarding } from '../hooks/useOnboarding.ts'
import { TourTooltip } from './TourTooltip.tsx'
import './OnboardingTour.css'

const STEPS = [
  {
    title: 'Welcome to Broder!',
    text: 'Here you can create alarms in a super simple way. Let\'s get started?',
    target: null,
  },
  {
    title: 'Choose a template',
    text: 'Each card is a ready-made alarm type. Drag one to the area below.',
    target: '.template-palette',
  },
  {
    title: 'Configure your alarm',
    text: 'Adjust the threshold, comparison, and severity however you need.',
    target: '.builder-canvas',
  },
  {
    title: 'Create and done!',
    text: 'Click "Create Alarm" and monitor everything in the "Alarms" tab. Enjoy!',
    target: '.builder-actions',
  },
]

export function OnboardingTour() {
  const { active, step, total, next, prev, skip } = useOnboarding()

  if (!active) return null

  const current = STEPS[step]

  return (
    <div className="onboarding-overlay">
      <TourTooltip
        step={step + 1}
        total={total}
        title={current.title}
        text={current.text}
        targetSelector={current.target}
        onNext={next}
        onPrev={prev}
        onSkip={skip}
        isFirst={step === 0}
        isLast={step === total - 1}
      />
    </div>
  )
}
