import { motion } from 'motion/react'
import { hero, routes, sections } from '../content'
import { useMedia } from '../lib/useMedia'
import { Button, Logo, NextSign, Section, ease } from '../components/ui'
import { WavesStage } from '../components/WavesStage'
import { IndexStrip } from '../components/HeroIndex'

const word = {
  hidden: { opacity: 0, y: '0.45em', filter: 'blur(12px)' },
  show: { opacity: 1, y: 0, filter: 'blur(0px)' },
}

export function Hero() {
  const wide = useMedia('(min-width: 1024px)')
  const words = hero.title.join(' ').split(' ')

  return (
    <Section id="topo" className="isolate overflow-hidden !pt-10 !pb-28">
      <WavesStage />

      {/* Nome do site no alto, centralizado. Não é uma barra de navegação. */}
      <motion.div
        initial={{ opacity: 0, y: -12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.9, delay: 0.1, ease }}
        className="absolute inset-x-0 top-8 flex justify-center"
      >
        <Logo size="lg" />
      </motion.div>

      <div className="relative mx-auto flex max-w-5xl flex-col items-center text-center">
        {/* Véu atrás do texto para a leitura não depender das ondas */}
        <div
          aria-hidden
          className="pointer-events-none absolute -inset-x-[12%] -inset-y-[18%] -z-10 bg-[radial-gradient(closest-side,color-mix(in_oklab,var(--night)_72%,transparent),transparent)]"
        />

        <motion.h1
          initial="hidden"
          animate="show"
          transition={{ staggerChildren: 0.06, delayChildren: 0.3 }}
          className="font-display text-[2.4rem] leading-[1.02] font-extrabold tracking-[-0.02em] uppercase sm:text-6xl lg:text-[5.6rem]"
        >
          {words.map((w, i) => (
            <motion.span key={i} variants={word} transition={{ duration: 0.8, ease }} className="inline-block pr-[0.24em]">
              {w}
            </motion.span>
          ))}
          <motion.span
            variants={word}
            transition={{ duration: 0.8, ease }}
            className="inline-block text-accent"
          >
            {hero.highlight}
          </motion.span>
        </motion.h1>

        <motion.p
          initial={{ opacity: 0, y: 14 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.9, delay: 0.9, ease }}
          className="mt-8 max-w-[34rem] text-lg leading-relaxed text-snow md:text-xl"
        >
          {hero.lead}
        </motion.p>

        <motion.div
          initial={{ opacity: 0, y: 14 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.9, delay: 1.05, ease }}
          className="mt-10 flex flex-row items-center gap-3 sm:gap-4"
        >
          <Button href={routes.signup} magnetic tintsHero>
            Criar conta
          </Button>
          <Button href={routes.login} variant="outline">
            Entrar
          </Button>
        </motion.div>

        {/* Atalhos em linha (mobile e tablet) */}
        {!wide && (
          <nav aria-label="Seções" className="mt-12 flex flex-wrap justify-center gap-3">
            {sections.slice(0, 3).map((s) => {
              const Icon = s.icon
              return (
                <a key={s.id} href={`#${s.id}`} className="plate flex h-11 items-center gap-2 rounded-full px-4 text-sm ring-1 ring-snow/30">
                  <Icon size={18} />
                  {s.label}
                </a>
              )
            })}
          </nav>
        )}
      </div>

      {wide ? <IndexStrip /> : <NextSign to="como-funciona" />}
    </Section>
  )
}
