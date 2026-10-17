import { motion } from 'motion/react'
import { ArrowUpRightIcon } from '@phosphor-icons/react'
import { perks, routes, type Perk } from '../content'
import { useMedia } from '../lib/useMedia'
import { Heading, NextSign, ScrollIn, Section } from '../components/ui'
import { Coin } from '../components/Coin'

const fmt = new Intl.NumberFormat('pt-BR')

// Cada vantagem leva ao cadastro: é preciso conta para trocar Egos.
function Price({ cost }: { cost: number }) {
  return (
    <span className="flex shrink-0 items-center gap-1.5 rounded-full bg-snow py-1 pr-3.5 pl-1 font-display text-sm font-bold text-night tabular-nums">
      <Coin size={18} />
      {fmt.format(cost)}
    </span>
  )
}

// Desktop: cinco placas em carvão; a primeira é maior e leva uma trama de linhas. O roxo fica só na moeda do preço.
const cells = [
  'lg:col-span-3 lg:row-span-2 bg-[repeating-linear-gradient(135deg,transparent_0_14px,color-mix(in_oklab,var(--snow)_6%,transparent)_14px_15px)]',
  'lg:col-span-3',
  'lg:col-span-3',
  'lg:col-span-4',
  'lg:col-span-2',
]

function Tile({ perk, index }: { perk: Perk; index: number }) {
  const big = index === 0
  const Icon = perk.icon

  // Luz que segue o cursor, escrita direto em variáveis CSS para não re-renderizar.
  const onMove = (e: React.PointerEvent<HTMLAnchorElement>) => {
    const r = e.currentTarget.getBoundingClientRect()
    e.currentTarget.style.setProperty('--mx', `${e.clientX - r.left}px`)
    e.currentTarget.style.setProperty('--my', `${e.clientY - r.top}px`)
  }

  return (
    <li className={cells[index]}>
      <motion.a
        href={routes.signup}
        aria-label={`${perk.name}, ${perk.partner}, ${fmt.format(perk.cost)} Egos. Criar conta para trocar`}
        whileHover={{ y: -6 }}
        transition={{ type: 'spring', stiffness: 300, damping: 22 }}
        onPointerMove={onMove}
        className="group relative flex h-full min-h-48 flex-col justify-between overflow-hidden rounded-[20px] bg-coal p-7 text-snow ring-1 ring-line transition-shadow duration-300 hover:ring-snow/60"
      >
        <span
          aria-hidden
          className="pointer-events-none absolute inset-0 opacity-0 transition-opacity duration-500 group-hover:opacity-100"
          style={{
            background:
              'radial-gradient(300px circle at var(--mx, 50%) var(--my, 50%), color-mix(in oklab, var(--snow) 10%, transparent), transparent 70%)',
          }}
        />
        <span className="relative flex items-start justify-between">
          <Icon size={big ? 64 : 40} weight="light" className="transition-transform duration-700 ease-out-expo group-hover:scale-110" />
          <ArrowUpRightIcon
            size={20}
            className="text-ink-soft opacity-0 transition-[opacity,transform] duration-500 ease-out-expo group-hover:translate-x-0.5 group-hover:-translate-y-0.5 group-hover:opacity-100"
          />
        </span>
        <span className="relative mt-10 flex items-end justify-between gap-4">
          <span>
            <span className={`block font-display font-bold tracking-tight uppercase ${big ? 'text-3xl leading-none md:text-5xl' : 'text-xl md:text-2xl'}`}>
              {perk.name}
            </span>
            <span className="mt-2 block text-sm font-normal text-ink-soft">{perk.partner}</span>
          </span>
          <Price cost={perk.cost} />
        </span>
      </motion.a>
    </li>
  )
}

// Mobile e tablet: lista de preços, uma linha por vantagem.
function PriceList() {
  return (
    <ul className="mx-auto mt-10 w-full max-w-xl divide-y divide-line overflow-hidden rounded-[20px] bg-coal ring-1 ring-line">
      {perks.map((perk) => {
        const Icon = perk.icon
        return (
          <li key={perk.name}>
            <a
              href={routes.signup}
              aria-label={`${perk.name}, ${perk.partner}, ${fmt.format(perk.cost)} Egos. Criar conta para trocar`}
              className="flex items-center gap-4 px-5 py-4 transition-colors active:bg-snow/5"
            >
              <Icon size={26} weight="light" className="shrink-0 text-ink-soft" />
              <span className="min-w-0 flex-1">
                <span className="block font-display text-base font-bold tracking-tight uppercase">{perk.name}</span>
                <span className="block text-sm font-normal text-ink-soft">{perk.partner}</span>
              </span>
              <Price cost={perk.cost} />
            </a>
          </li>
        )
      })}
    </ul>
  )
}

export function Perks() {
  const wide = useMedia('(min-width: 1024px)')
  return (
    <Section id="vantagens" className="!pb-32">
      <Heading>Onde seus Egos valem</Heading>
      <ScrollIn className="w-full">
        <p className="mt-5 text-center text-base text-ink-soft">Exemplos do que uma empresa parceira pode oferecer.</p>
        {wide ? (
          <ul className="mx-auto mt-16 grid w-full max-w-5xl auto-rows-[minmax(190px,auto)] grid-cols-6 gap-5">
            {perks.map((perk, i) => (
              <Tile key={perk.name} perk={perk} index={i} />
            ))}
          </ul>
        ) : (
          <PriceList />
        )}
      </ScrollIn>

      <NextSign to="quem-participa" />
    </Section>
  )
}
