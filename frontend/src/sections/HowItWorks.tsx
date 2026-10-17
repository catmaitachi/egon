import { useLayoutEffect, useRef, useState, type CSSProperties } from 'react'
import { steps } from '../content'
import { useMedia } from '../lib/useMedia'
import { Heading, NextSign, ScrollIn, Section } from '../components/ui'
import { Coin } from '../components/Coin'

// Fluxo contínuo: moedas nascem no professor, correm pela trilha e somem na troca.
// Três moedas defasadas em um terço do ciclo; cada parada pulsa quando uma moeda passa por ela.
const CYCLE = 4.5
const COINS = 3
const GAP = CYCLE / COINS

// Atraso do pulso de uma parada que está na fração `f` da trilha.
const pulseDelay = (f: number) => `${(f * CYCLE) % GAP}s`

function Stop({ index, at }: { index: number; at: number }) {
  const step = steps[index]
  const Icon = step.icon
  return (
    <li className="group relative flex gap-5 lg:flex-col lg:items-center lg:gap-0 lg:text-center">
      <span className="relative z-10 grid size-20 shrink-0 place-items-center rounded-full bg-night text-snow ring-1 ring-snow/40 lg:size-24">
        {/* Camada própria para o pulso: o anel continua desenhado por baixo */}
        <span
          aria-hidden
          style={{ animationDelay: pulseDelay(at), animationDuration: `${GAP}s` } as CSSProperties}
          className="stop-pulse absolute inset-0 rounded-full"
        />
        <Icon size={34} weight="regular" />
      </span>
      <div className="pt-3 lg:pt-7">
        <h3 className="font-display text-xl leading-[1.15] font-bold tracking-tight uppercase md:text-2xl lg:flex lg:min-h-[2.3em] lg:items-end lg:justify-center">{step.title}</h3>
        <p className="mt-2 max-w-[30ch] text-base leading-relaxed text-ink-soft transition-colors duration-300 group-hover:text-snow lg:mx-auto">
          {step.text}
        </p>
      </div>
    </li>
  )
}

// As moedas viajam dentro de uma faixa do tamanho da trilha: o translate em % usa o tamanho da faixa.
// Passam por trás das paradas, que acendem quando elas chegam.
function Flow({ axis }: { axis: 'x' | 'y' }) {
  return (
    <>
      {Array.from({ length: COINS }, (_, i) => (
        <span
          key={i}
          aria-hidden
          className={`absolute inset-0 ${axis === 'x' ? 'flow-x' : 'flow-y'}`}
          style={{ animationDelay: `${-i * GAP}s`, animationDuration: `${CYCLE}s` }}
        >
          <span className={`absolute ${axis === 'x' ? 'top-1/2 left-0' : 'top-0 left-1/2'} -translate-x-1/2 -translate-y-1/2`}>
            <Coin size={axis === 'x' ? 30 : 24} glow />
          </span>
        </span>
      ))}
    </>
  )
}

function Track() {
  return (
    <div className="relative mx-auto mt-20 w-full max-w-5xl">
      <div aria-hidden className="absolute top-12 right-[16.66%] left-[16.66%] h-[3px] -translate-y-1/2">
        <div className="absolute inset-0 rounded-full bg-[linear-gradient(90deg,color-mix(in_oklab,var(--snow)_40%,transparent),var(--accent))]" />
        <Flow axis="x" />
      </div>
      <ol className="grid grid-cols-3 gap-8">
        {steps.map((s, i) => (
          <Stop key={s.title} index={i} at={i / (steps.length - 1)} />
        ))}
      </ol>
    </div>
  )
}

function VerticalTrack() {
  const list = useRef<HTMLOListElement>(null)
  const [tops, setTops] = useState<number[]>([0])
  const span = tops[tops.length - 1] || 1

  // A trilha vai do centro da primeira parada ao centro da última; as paradas têm alturas diferentes.
  useLayoutEffect(() => {
    const ol = list.current
    if (!ol) return
    const measure = () => setTops([...ol.children].map((li) => (li as HTMLElement).offsetTop))
    measure()
    const ro = new ResizeObserver(measure)
    ro.observe(ol)
    return () => ro.disconnect()
  }, [])

  return (
    <div className="relative mx-auto mt-14 w-full max-w-md">
      <div aria-hidden style={{ height: span }} className="absolute top-10 left-10 w-[3px] -translate-x-1/2">
        <div className="absolute inset-0 rounded-full bg-[linear-gradient(180deg,color-mix(in_oklab,var(--snow)_40%,transparent),var(--accent))]" />
        <Flow axis="y" />
      </div>
      <ol ref={list} className="flex flex-col gap-14">
        {steps.map((s, i) => (
          <Stop key={s.title} index={i} at={(tops[i] ?? 0) / span} />
        ))}
      </ol>
    </div>
  )
}

export function HowItWorks() {
  const wide = useMedia('(min-width: 1024px)')
  return (
    <Section id="como-funciona" className="!pb-32">
      <Heading>Como o Ego circula</Heading>
      <ScrollIn className="w-full">{wide ? <Track /> : <VerticalTrack />}</ScrollIn>
      <NextSign to="vantagens" />
    </Section>
  )
}
