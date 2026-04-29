import { forwardRef, type ButtonHTMLAttributes, type ReactNode } from 'react'
import './Button.css'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'cta' | 'ghost' | 'danger'
  size?: 'sm' | 'md' | 'lg'
  children: ReactNode
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(function Button(
  { variant = 'primary', size = 'md', children, className = '', ...props },
  ref
) {
  const classes = [`btn`, `btn--${variant}`, `btn--${size}`, className]
    .filter(Boolean)
    .join(' ')
  return (
    <button type="button" className={classes} ref={ref} {...props}>
      {children}
    </button>
  )
})
