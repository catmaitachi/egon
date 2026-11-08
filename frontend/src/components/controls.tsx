import type { ComponentProps, ReactNode } from 'react'
import { Link } from 'react-router'
import { CircleNotchIcon, type Icon } from '@phosphor-icons/react'

// Os botões da landing em tamanho de app (44px). Roxo só no primário: a ação que a pessoa veio fazer.
export type Variant = 'primary' | 'outline' | 'danger' | 'quiet'

const base =
  'inline-flex h-11 items-center justify-center gap-2 rounded-full font-display text-sm tracking-wide whitespace-nowrap uppercase transition-[color,background-color,box-shadow,transform] duration-200 ease-out-expo active:scale-[0.98] disabled:pointer-events-none disabled:opacity-60 aria-disabled:pointer-events-none aria-disabled:opacity-60'

const looks: Record<Variant, string> = {
  primary: 'px-6 bg-accent font-semibold text-snow hover:bg-accent-deep',
  outline: 'px-6 font-medium text-snow ring-1 ring-snow/40 hover:bg-snow hover:text-night hover:ring-snow',
  danger: 'px-6 font-semibold text-danger ring-1 ring-danger/60 hover:bg-danger hover:text-night hover:ring-danger',
  quiet: 'px-3 font-medium text-ink-soft hover:text-snow',
}

type ActionButtonProps = ComponentProps<'button'> & { variant?: Variant; busy?: boolean }

// `busy` trava o botão durante o envio e mostra um anel girando; o texto ("Enviando…") fica com quem chama.
// Ocupado não usa `disabled`: o botão em foco perderia o foco para o <body> no meio do envio. Continua
// focável, com aria-disabled, e o clique (inclusive o Enter que envia o formulário) é cancelado.
export function ActionButton({ variant = 'primary', busy = false, className = '', type = 'button', disabled, onClick, children, ...props }: ActionButtonProps) {
  return (
    <button
      type={type}
      disabled={disabled}
      aria-disabled={busy || undefined}
      aria-busy={busy || undefined}
      onClick={busy ? (e) => e.preventDefault() : onClick}
      className={`${base} ${looks[variant]} ${className}`}
      {...props}
    >
      {busy && <CircleNotchIcon size={18} weight="bold" aria-hidden className="motion-safe:animate-spin" />}
      {children}
    </button>
  )
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

// Ação de linha (editar, excluir, fechar). O nome vai no aria-label e no title, já que só o ícone aparece.
export function IconButton({ label, icon: Glyph, onClick, tone = 'neutral' }: IconButtonProps) {
  const hover = tone === 'danger' ? 'hover:text-danger hover:ring-danger/60' : 'hover:text-snow hover:ring-snow/60'
  return (
    <button
      type="button"
      aria-label={label}
      title={label}
      onClick={onClick}
      className={`grid size-11 shrink-0 place-items-center rounded-full text-ink-soft ring-1 ring-line transition-colors duration-200 ${hover}`}
    >
      <Glyph size={20} />
    </button>
  )
}
