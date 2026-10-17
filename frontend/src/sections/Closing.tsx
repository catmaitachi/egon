import { brand, closing, routes } from '../content'
import { Button, Logo, ScrollIn, Section } from '../components/ui'
import { Coin } from '../components/Coin'

export function Closing() {
  return (
    <>
      <Section id="criar-conta">
        <ScrollIn className="relative flex flex-col items-center text-center">
          <Coin size={64} className="mb-10" />
          <h2 className="max-w-4xl font-display text-4xl leading-[1.04] font-extrabold tracking-[-0.02em] uppercase sm:text-6xl lg:text-7xl">
            {closing.title}{' '}
            <span className="text-accent">
              {closing.highlight}
            </span>
          </h2>
          <div className="mt-12">
            <Button href={routes.signup} magnetic>
              Criar conta
            </Button>
          </div>
        </ScrollIn>
      </Section>

      <footer className="px-4 pb-8 sm:px-6">
        <div className="mx-auto flex max-w-5xl flex-col items-center justify-between gap-5 border-t border-line pt-6 sm:flex-row">
          <Logo />
          <nav aria-label="Rodapé" className="flex flex-wrap justify-center gap-x-6 gap-y-2 text-sm text-ink-soft">
            <a className="hover:text-snow" href={routes.login}>Entrar</a>
            <a className="hover:text-snow" href={routes.signup}>Criar conta</a>
            <a className="hover:text-snow" href={routes.partner}>Cadastrar empresa</a>
          </nav>
          <p className="text-sm text-ink-soft">© 2026 {brand.name}</p>
        </div>
      </footer>
    </>
  )
}
