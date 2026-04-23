import { useOnboarding } from '../hooks/useOnboarding.ts'
import { TourTooltip } from './TourTooltip.tsx'
import './OnboardingTour.css'

const STEPS = [
  {
    title: 'Bem-vindo ao Broder!',
    text: 'Aqui você cria alarmes de forma super simples. Vamos começar?',
    target: null,
  },
  {
    title: 'Escolha um template',
    text: 'Cada card é um tipo de alarme pronto. Arraste um para a área de baixo.',
    target: '.template-palette',
  },
  {
    title: 'Configure seu alarme',
    text: 'Ajuste o limite, a comparação e a severidade do jeito que precisar.',
    target: '.builder-canvas',
  },
  {
    title: 'Crie e pronto!',
    text: 'Clique em "Criar Alarme" e acompanhe tudo na aba "Alarmes". Divirta-se!',
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
