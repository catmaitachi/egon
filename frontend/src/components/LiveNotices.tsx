import { useEffect, useState } from 'react'
import { AnimatePresence, motion } from 'motion/react'
import { XIcon } from '@phosphor-icons/react'
import type { ShownNotice } from '../lib/useLiveNotices'
import type { EstadoConexao } from '../lib/useNotificacoes'
import { Coin } from './Coin'
import { IconButton } from './controls'
import { ease } from './ui'

// Some sozinho depois disso; ponteiro ou foco em cima seguram o aviso na tela.
const VISIVEL_MS = 10_000

type LiveNoticesProps = { items: ShownNotice[]; onClose: (id: string) => void }

// Avisos ao vivo (moedas recebidas, envio confirmado), presos ao canto da tela.
// A lista é região aria-live desde a montagem, então cada aviso novo é lido assim que entra.
export function LiveNotices({ items, onClose }: LiveNoticesProps) {
  return (
    <section aria-label="Avisos" className="pointer-events-none fixed inset-x-4 bottom-4 z-40 sm:inset-x-auto sm:right-6 sm:bottom-6 sm:w-[26rem]">
      <ol aria-live="polite" aria-relevant="additions" className="flex flex-col gap-3">
        <AnimatePresence initial={false}>
          {items.map((n) => (
            <Toast key={n.id} notice={n} onClose={onClose} />
          ))}
        </AnimatePresence>
      </ol>
    </section>
  )
}

function Toast({ notice, onClose }: { notice: ShownNotice; onClose: (id: string) => void }) {
  const [held, setHeld] = useState(false)

  useEffect(() => {
    if (held) return
    const t = window.setTimeout(() => onClose(notice.id), VISIVEL_MS)
    return () => window.clearTimeout(t)
  }, [held, notice.id, onClose])

  return (
    <motion.li
      layout
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0, transition: { duration: 0.2, ease } }}
      exit={{ opacity: 0, transition: { duration: 0.15 } }}
      onPointerEnter={() => setHeld(true)}
      onPointerLeave={() => setHeld(false)}
      onFocus={() => setHeld(true)}
      onBlur={() => setHeld(false)}
      className="pointer-events-auto flex gap-4 rounded-[20px] bg-coal py-4 pr-3 pl-4 ring-1 ring-snow/25"
    >
      <Coin size={30} glow className="mt-1" />
      <div className="min-w-0 flex-1">
        <p className="font-display text-base leading-snug font-bold tracking-[-0.01em] uppercase">{notice.title}</p>
        <p className="mt-1 text-sm text-ink-soft">{notice.text}</p>
        {notice.message && <p className="mt-2 text-sm break-words text-snow">“{notice.message}”</p>}
      </div>
      <IconButton label="Fechar aviso" icon={XIcon} onClick={() => onClose(notice.id)} />
    </motion.li>
  )
}

const status: Record<EstadoConexao, { label: string; dot: string }> = {
  'ao-vivo': { label: 'Ao vivo', dot: 'bg-snow' },
  conectando: { label: 'Conectando…', dot: 'ring-1 ring-ink-soft' },
  reconectando: { label: 'Reconectando…', dot: 'ring-1 ring-ink-soft' },
}

// Estado da conexão dos avisos, para a pessoa saber se a tela está se atualizando sozinha.
export function LiveStatus({ estado }: { estado: EstadoConexao }) {
  const s = status[estado]
  return (
    <span className="inline-flex items-center gap-2 font-display text-xs font-semibold tracking-[0.08em] text-ink-soft uppercase">
      <span aria-hidden className={`size-2 rounded-full ${s.dot}`} />
      {s.label}
    </span>
  )
}
