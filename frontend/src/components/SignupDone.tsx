import { motion } from 'motion/react'
import { ActionLink } from './controls'
import { Coin } from './Coin'
import { Panel } from './Shell'
import { ease } from './ui'

type SignupDoneProps = { title: string; text: string; next: { to: string; label: string } }

// Fecho do cadastro: a moeda acende no lugar do formulário.
export function SignupDone({ title, text, next }: SignupDoneProps) {
  return (
    <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6, ease }}>
      <Panel className="flex flex-col items-center px-6 py-14 text-center md:px-14">
        <Coin size={64} glow />
        <h1 role="status" className="mt-8 font-display text-3xl font-extrabold tracking-[-0.02em] uppercase md:text-4xl">
          {title}
        </h1>
        <p className="mt-4 max-w-[32rem] text-lg leading-relaxed">{text}</p>
        <div className="mt-10 flex flex-wrap justify-center gap-3">
          <ActionLink to="/" variant="outline">
            Voltar ao início
          </ActionLink>
          <ActionLink to={next.to}>{next.label}</ActionLink>
        </div>
      </Panel>
    </motion.div>
  )
}
