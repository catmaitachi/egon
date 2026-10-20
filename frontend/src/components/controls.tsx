import type { ButtonHTMLAttributes, ReactNode } from 'react'
import { Link } from 'react-router'
import type { Icon } from '@phosphor-icons/react'

// Os mesmos botões da landing, em versão <button> e <Link> para as telas de cadastro.
// Roxo só no primário: é a ação que a pessoa veio fazer.
type Variant = 'primary' | 'outline' | 'danger'

const base =
  'inline-flex h-12 items-center justify-center gap-2.5 rounded-full px-6 font-display text-sm tracking-wide whitespace-nowrap uppercase transition-[box-shadow,color,background-color,opacity] duration-300 active:scale-[0.98] disabled:pointer-events-none disabled:opacity-60'

const looks: Record<Variant, string> = {
  primary: 'bg-accent font-semibold text-snow hover:bg-accent-deep',
  outline: 'font-medium text-snow ring-1 ring-snow/40 hover:bg-snow hover:text-night hover:ring-snow',
  danger: 'font-semibold text-danger ring-1 ring-danger/60 hover:bg-danger hover:text-night hover:ring-danger',
}

type ActionButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & { variant?: Variant }

export function ActionButton({ variant = 'primary', className = '', type = 'button', ...props }: ActionButtonProps) {
  return <button type={type} className={`${base} ${looks[variant]} ${className}`} {...props} />
}

type ActionLinkProps = { to: string; variant?: Variant; children: ReactNode; className?: string }

export function ActionLink({ to, variant = 'primary', children, className = '' }: ActionLinkProps) {
  return (
    <Link to={to} className={`${base} ${looks[variant]} ${className}`}>
      {children}
    </Link>
  )
}

type IconButtonProps = { label: string; icon: Icon; onClick: () => void; tone?: 'neutral' | 'danger' }

// Ação de linha (editar, excluir). O nome vai no aria-label e no title, já que só o ícone aparece.
export function IconButton({ label, icon: Glyph, onClick, tone = 'neutral' }: IconButtonProps) {
  const hover = tone === 'danger' ? 'hover:text-danger hover:ring-danger/60' : 'hover:text-snow hover:ring-snow/60'
  return (
    <button
      type="button"
      aria-label={label}
      title={label}
      onClick={onClick}
      className={`grid size-11 shrink-0 place-items-center rounded-full text-ink-soft ring-1 ring-line transition-colors duration-300 ${hover}`}
    >
      <Glyph size={20} />
    </button>
  )
}
