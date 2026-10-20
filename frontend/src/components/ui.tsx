import { useRef, type ReactNode } from 'react'
import { motion, useMotionValue, useReducedMotion, useScroll, useSpring, useTransform, type MotionValue } from 'motion/react'
import { ArrowDownIcon, ArrowRightIcon } from '@phosphor-icons/react'
import { brand, sections, type SectionId } from '../content'
import { hoverCta } from '../lib/stage'
import { Coin } from './Coin'

export const ease = [0.16, 1, 0.3, 1] as const

// Logo: EGON com a moeda no lugar do O.
export function Logo({ size = 'md', href = '#topo' }: { size?: 'md' | 'lg'; href?: string }) {
  const lg = size === 'lg'
  const [before, after] = brand.name.toUpperCase().split('O')
  return (
    <a
      href={href}
      aria-label={`${brand.name}, voltar ao início`}
      className={`flex items-center rounded-full font-display leading-none font-extrabold tracking-[0.06em] ${lg ? 'text-3xl' : 'text-xl'}`}
    >
      <span aria-hidden>{before}</span>
      <Coin size={lg ? 26 : 18} className="mx-[0.06em]" />
      <span aria-hidden>{after}</span>
    </a>
  )
}

type ButtonProps = {
  href: string
  children: ReactNode
  variant?: 'primary' | 'outline'
  magnetic?: boolean
  tintsHero?: boolean
  className?: string
}

// Primário: acento roxo com texto claro (5,1:1), o ponto de cor da página. Contorno: branco, acende no hover.
export function Button({ href, children, variant = 'primary', magnetic = false, tintsHero = false, className = '' }: ButtonProps) {
  const ref = useRef<HTMLAnchorElement>(null)
  const reduce = useReducedMotion()
  const x = useMotionValue(0)
  const y = useMotionValue(0)
  const sx = useSpring(x, { stiffness: 220, damping: 18, mass: 0.6 })
  const sy = useSpring(y, { stiffness: 220, damping: 18, mass: 0.6 })

  const onMove = (e: React.PointerEvent) => {
    if (!magnetic || reduce || e.pointerType !== 'mouse' || !ref.current) return
    const r = ref.current.getBoundingClientRect()
    x.set((e.clientX - r.left - r.width / 2) * 0.28)
    y.set((e.clientY - r.top - r.height / 2) * 0.35)
  }
  const onLeave = () => {
    x.set(0)
    y.set(0)
    if (tintsHero) hoverCta(false)
  }

  const look =
    variant === 'primary'
      ? 'bg-accent text-snow font-semibold hover:bg-accent-deep'
      : 'text-snow font-medium bg-night/50 ring-1 ring-snow/40 hover:ring-snow hover:bg-snow hover:text-night'

  return (
    <motion.a
      ref={ref}
      href={href}
      style={{ x: sx, y: sy }}
      onPointerMove={onMove}
      onPointerEnter={() => tintsHero && hoverCta(true)}
      onPointerLeave={onLeave}
      onFocus={() => tintsHero && hoverCta(true)}
      onBlur={onLeave}
      className={`group inline-flex h-13 items-center justify-center gap-2.5 rounded-full px-7 font-display text-[15px] tracking-wide whitespace-nowrap uppercase transition-[box-shadow,color,background-color] duration-300 active:scale-[0.98] ${look} ${className}`}
    >
      {children}
      {variant === 'primary' && (
        <ArrowRightIcon size={18} weight="bold" className="transition-transform duration-500 ease-out-expo group-hover:translate-x-1" />
      )}
    </motion.a>
  )
}

type SectionProps = { id: string; children: ReactNode; className?: string }

// Cada seção ocupa a tela e tem âncora própria.
export function Section({ id, children, className = '' }: SectionProps) {
  return (
    <section
      id={id}
      className={`relative flex min-h-[100dvh] flex-col items-center justify-center px-4 py-24 sm:px-6 ${className}`}
    >
      {children}
    </section>
  )
}

// Título que entra como letreiro: desce contra a rolagem enquanto as letras acendem da esquerda para a direita.
// Tudo preso à posição da rolagem, então rolar para cima desfaz a entrada.
export function Heading({ children }: { children: string }) {
  const ref = useRef<HTMLHeadingElement>(null)
  const reduce = useReducedMotion()
  const { scrollYProgress } = useScroll({ target: ref, offset: ['start end', 'start 0.3'] })
  const y = useTransform(scrollYProgress, [0, 1], [-90, 0])
  const chars = [...children]
  return (
    <motion.h2
      ref={ref}
      aria-label={children}
      style={reduce ? undefined : { y }}
      className="mx-auto max-w-3xl text-center font-display text-[2rem] leading-[1.05] font-extrabold tracking-[-0.02em] uppercase sm:text-4xl md:text-6xl"
    >
      {chars.map((c, i) => (
        <SignChar key={i} char={c} at={i / chars.length} progress={scrollYProgress} still={!!reduce} />
      ))}
    </motion.h2>
  )
}

function SignChar({ char, at, progress, still }: { char: string; at: number; progress: MotionValue<number>; still: boolean }) {
  const start = 0.1 + at * 0.65
  const opacity = useTransform(progress, [start, start + 0.12], [0.1, 1])
  return (
    <motion.span aria-hidden style={still ? undefined : { opacity }}>
      {char}
    </motion.span>
  )
}

// Conteúdo da seção: o mesmo movimento contra a rolagem, mais curto, e um leve ganho de escala.
export function ScrollIn({ children, className = '' }: { children: ReactNode; className?: string }) {
  const ref = useRef<HTMLDivElement>(null)
  const reduce = useReducedMotion()
  const { scrollYProgress } = useScroll({ target: ref, offset: ['start end', 'start 0.45'] })
  const y = useTransform(scrollYProgress, [0, 1], [-50, 0])
  const opacity = useTransform(scrollYProgress, [0, 0.6], [0.15, 1])
  const scale = useTransform(scrollYProgress, [0, 1], [0.97, 1])
  return (
    <motion.div ref={ref} style={reduce ? undefined : { y, opacity, scale }} className={className}>
      {children}
    </motion.div>
  )
}

// Saída no pé da seção: leva para a próxima sem precisar de cabeçalho.
export function NextSign({ to }: { to: SectionId }) {
  const target = sections.find((s) => s.id === to)!
  const Icon = target.icon
  return (
    <a
      href={`#${target.id}`}
      className="group absolute bottom-6 left-1/2 flex -translate-x-1/2 items-center gap-2.5 rounded-full px-4 py-2 text-sm text-ink-soft transition-colors hover:text-snow"
    >
      <Icon size={18} />
      {target.label}
      <ArrowDownIcon size={16} className="transition-transform duration-500 ease-out-expo group-hover:translate-y-1" />
    </a>
  )
}
