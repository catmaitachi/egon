import { motion } from 'motion/react'
import { ArrowDownIcon } from '@phosphor-icons/react'
import { sections } from '../content'
import { ease } from './ui'

const links = sections.slice(0, 3)


// Índice do hero: faixa de três atalhos no pé,, separados por linhas finas. Uma barra acende no topo do item em hover.
export function IndexStrip() {
  return (
    <motion.nav
      aria-label="Seções"
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.9, delay: 1.3, ease }}
      className="absolute inset-x-0 bottom-10 mx-auto grid w-full max-w-4xl grid-cols-3 px-6"
    >
      {links.map((s, i) => {
        const Icon = s.icon
        return (
          <a
            key={s.id}
            href={`#${s.id}`}
            className={`group relative flex items-center justify-center gap-3 py-5 text-ink-soft transition-colors duration-300 hover:text-snow ${
              i > 0 ? 'border-l border-line' : ''
            }`}
          >
            <span
              aria-hidden
              className="absolute inset-x-6 top-0 h-px origin-center scale-x-0 bg-snow transition-transform duration-500 ease-out-expo group-hover:scale-x-100"
            />
            <Icon size={22} className="transition-transform duration-500 ease-out-expo group-hover:-translate-y-0.5" />
            <span className="font-display text-sm font-bold tracking-[0.14em] uppercase">{s.label}</span>
            <ArrowDownIcon
              size={14}
              className="-translate-y-1 opacity-0 transition-[opacity,transform] duration-500 ease-out-expo group-hover:translate-y-0 group-hover:opacity-100"
            />
          </a>
        )
      })}
    </motion.nav>
  )
}
