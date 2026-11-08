import { AnimatePresence, motion } from 'motion/react'
import { coinName, coins } from '../content'
import { Coin } from './Coin'
import { ease } from './ui'

const fmt = new Intl.NumberFormat('pt-BR')

// Saldo com a moeda. Quando muda por um aviso ao vivo, o número novo desce no lugar do antigo;
// na primeira exibição ele só aparece.
export function Balance({ value, label = 'Saldo' }: { value: number; label?: string }) {
  return (
    <div>
      <p className="font-display text-xs font-semibold tracking-[0.08em] text-ink-soft uppercase">{label}</p>
      <p className="mt-2 flex items-center gap-3">
        <span className="sr-only">{coins(value)}</span>
        <Coin size={34} />
        <span aria-hidden className="inline-flex overflow-hidden">
          <AnimatePresence initial={false} mode="wait">
            <motion.span
              key={value}
              initial={{ y: '-0.5em', opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              transition={{ duration: 0.25, ease }}
              className="font-display text-4xl leading-none font-extrabold tracking-[-0.02em] tabular-nums sm:text-5xl"
            >
              {fmt.format(value)}
            </motion.span>
          </AnimatePresence>
        </span>
        <span aria-hidden className="self-end pb-1 font-display text-sm font-semibold tracking-[0.08em] text-ink-soft uppercase">
          {coinName(value)}
        </span>
      </p>
    </div>
  )
}
