import { useEffect, useRef, useState } from 'react'
import { AnimatePresence, motion, type Variants } from 'motion/react'
import { roles, type RoleId } from '../content'
import { Button, Heading, NextSign, ScrollIn, Section, ease } from '../components/ui'

// O conteúdo sai para um lado e o novo entra pelo outro, conforme a aba fica à esquerda ou à direita.
const panel: Variants = {
  enter: (dir: number) => ({ x: dir * 90, opacity: 0, filter: 'blur(8px)' }),
  center: {
    x: 0,
    opacity: 1,
    filter: 'blur(0px)',
    transition: { duration: 0.55, ease, staggerChildren: 0.06, delayChildren: 0.08 },
  },
  exit: (dir: number) => ({
    x: dir * -90,
    opacity: 0,
    filter: 'blur(8px)',
    transition: { duration: 0.28, ease: [0.4, 0, 1, 1] },
  }),
}

const item: Variants = {
  enter: { opacity: 0, y: 14 },
  center: { opacity: 1, y: 0, transition: { duration: 0.5, ease } },
}

// Três lados da troca. Passar o mouse (com uma pausa curta), focar ou tocar troca o papel.
export function Roles() {
  const [current, setCurrent] = useState<RoleId>('aluno')
  const [dir, setDir] = useState(1)
  const hoverTimer = useRef(0)
  const role = roles.find((r) => r.id === current)!
  const Icon = role.icon
  const indexOf = (id: RoleId) => roles.findIndex((r) => r.id === id)

  const pick = (id: RoleId) => {
    window.clearTimeout(hoverTimer.current)
    if (id === current) return
    setDir(indexOf(id) > indexOf(current) ? 1 : -1)
    setCurrent(id)
  }
  // Atravessar a barra com o mouse não deve trocar de perfil a cada aba.
  const pickOnHover = (id: RoleId) => {
    window.clearTimeout(hoverTimer.current)
    hoverTimer.current = window.setTimeout(() => pick(id), 140)
  }
  useEffect(() => () => window.clearTimeout(hoverTimer.current), [])

  // Teclado: setas, Home e End movem o foco entre as abas, e o foco seleciona o perfil.
  const tabs = useRef<(HTMLButtonElement | null)[]>([])
  const onKeyDown = (e: React.KeyboardEvent) => {
    const i = indexOf(current)
    const last = roles.length - 1
    const next =
      e.key === 'ArrowRight' ? (i === last ? 0 : i + 1)
      : e.key === 'ArrowLeft' ? (i === 0 ? last : i - 1)
      : e.key === 'Home' ? 0
      : e.key === 'End' ? last
      : null
    if (next === null) return
    e.preventDefault()
    tabs.current[next]?.focus()
  }

  return (
    <Section id="quem-participa" className="!pb-32">
      <Heading>Quem participa</Heading>

      <ScrollIn className="mx-auto mt-12 flex w-full max-w-3xl flex-col items-center md:mt-14">
        <div role="tablist" aria-label="Perfis" onKeyDown={onKeyDown} className="flex rounded-full bg-coal p-1.5 ring-1 ring-line">
          {roles.map((r, i) => {
            const selected = r.id === current
            const TabIcon = r.icon
            return (
              <button
                key={r.id}
                ref={(el) => {
                  tabs.current[i] = el
                }}
                role="tab"
                tabIndex={selected ? 0 : -1}
                id={`tab-${r.id}`}
                aria-selected={selected}
                aria-controls="painel-perfil"
                onPointerEnter={(e) => e.pointerType === 'mouse' && pickOnHover(r.id)}
                onPointerLeave={() => window.clearTimeout(hoverTimer.current)}
                onFocus={() => pick(r.id)}
                onClick={() => pick(r.id)}
                className={`relative flex h-11 items-center gap-2 rounded-full px-3.5 font-display text-xs font-semibold tracking-wide uppercase sm:text-sm transition-colors duration-300 sm:px-5 ${
                  selected ? 'text-night' : 'text-ink-soft hover:text-snow'
                }`}
              >
                {selected && (
                  <motion.span
                    layoutId="role-pill"
                    className="absolute inset-0 rounded-full bg-snow"
                    transition={{ type: 'spring', stiffness: 380, damping: 32 }}
                  />
                )}
                <TabIcon size={18} weight={selected ? 'fill' : 'regular'} className="relative hidden sm:block" />
                <span className="relative">{r.label}</span>
              </button>
            )
          })}
        </div>

        {/* A altura do painel acompanha o conteúdo de cada perfil */}
        <motion.div
          layout
          transition={{ layout: { duration: 0.5, ease } }}
          style={{ borderRadius: 20 }}
          id="painel-perfil"
          role="tabpanel"
          aria-labelledby={`tab-${current}`}
          className="relative mt-8 w-full overflow-hidden bg-coal px-6 py-12 ring-1 ring-line md:px-14"
        >
          <AnimatePresence mode="popLayout" initial={false} custom={dir}>
            <motion.div
              key={current}
              custom={dir}
              variants={panel}
              initial="enter"
              animate="center"
              exit="exit"
              className="flex flex-col items-center text-center"
            >
              <motion.span
                variants={{
                  enter: { opacity: 0, scale: 0.7, rotate: dir * 18 },
                  center: { opacity: 1, scale: 1, rotate: 0, transition: { type: 'spring', stiffness: 260, damping: 18 } },
                }}
              >
                <Icon size={60} weight="light" className="text-snow" />
              </motion.span>

              <motion.p variants={item} className="mt-7 max-w-[30ch] font-display text-xl leading-snug font-semibold md:text-2xl">
                {role.line}
              </motion.p>

              <ul className="mt-8 flex flex-wrap justify-center gap-x-8 gap-y-3 text-base">
                {role.points.map((p) => {
                  const PIcon = p.icon
                  return (
                    <motion.li variants={item} key={p.text} className="flex items-center gap-2.5 text-snow">
                      <PIcon size={22} className="text-ink-soft" />
                      {p.text}
                    </motion.li>
                  )
                })}
              </ul>

              <motion.div variants={item} className="mt-10">
                <Button href={role.action.href} variant={role.id === 'aluno' ? 'primary' : 'outline'} magnetic={role.id === 'aluno'}>
                  {role.action.label}
                </Button>
              </motion.div>
            </motion.div>
          </AnimatePresence>
        </motion.div>
      </ScrollIn>

      <NextSign to="criar-conta" />
    </Section>
  )
}
