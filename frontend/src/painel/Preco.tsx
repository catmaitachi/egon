import { coins } from '../content'
import { Coin } from '../components/Coin'

const fmt = new Intl.NumberFormat('pt-BR')

// O chip de preço da landing: fundo Neve, número tabular e a moeda colada à esquerda.
export function Preco({ valor }: { valor: number }) {
  return (
    <span className="inline-flex shrink-0 items-center gap-1.5 rounded-full bg-snow py-1 pr-3 pl-1 font-display text-sm font-bold text-night tabular-nums">
      <Coin size={18} />
      <span className="sr-only">{coins(valor)}</span>
      <span aria-hidden>{fmt.format(valor)}</span>
    </span>
  )
}
