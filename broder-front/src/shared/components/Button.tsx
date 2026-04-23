import type { ButtonHTMLAttributes, ReactNode } from 'react'
import './Button.css'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'cta' | 'ghost'
  size?: 'sm' | 'md' | 'lg'
  children: ReactNode
}

export function Button({
  variant = 'primary',
  size = 'md',
  children,
  className = '',
  ...props
}: ButtonProps) {
  const classes = [`btn`, `btn--${variant}`, `btn--${size}`, className]
    .filter(Boolean)
    .join(' ')
  return (
    <button type="button" className={classes} {...props}>
      {children}
    </button>
  )
}
