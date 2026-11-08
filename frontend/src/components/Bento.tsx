import type { ReactNode } from 'react'
import { Panel } from './Shell'

// Grade bento dos dashboards: 12 colunas a partir de 1024px, 6 no tablet, 1 no celular (na ordem
// do código, que é a ordem de importância). Cada bloco responde a uma pergunta de quem olha; o
// tamanho vem da importância dela, nunca todos iguais.
export function Bento({ children }: { children: ReactNode }) {
  return <div className="grid grid-cols-1 items-start gap-6 md:grid-cols-6 lg:grid-cols-12 lg:items-stretch">{children}</div>
}

// Uma coluna da grade a partir de 1024px: os blocos dela se empilham sem herdar a altura dos
// blocos da outra coluna. As duas colunas têm a mesma altura; um bloco `elastico` em cada uma
// ocupa a sobra, para nenhuma terminar antes da outra. Abaixo disso some (display: contents) e cada bloco volta para a
// grade única, na ordem que o `className` dele dá (order-1, order-2...).
export function Coluna({ className = '', children }: { className?: string; children: ReactNode }) {
  return <div className={`contents lg:flex lg:min-w-0 lg:flex-col lg:gap-6 ${className}`}>{children}</div>
}

type BlocoProps = {
  titulo: ReactNode
  acoes?: ReactNode
  // Spans e ordem por breakpoint, ex. "order-2 md:col-span-6 lg:order-none".
  className?: string
  flush?: boolean
  // O bloco do número principal: o único com o contorno no tom da moeda.
  destaque?: boolean
  // A partir de 1024px, para as duas colunas terminarem juntas (sem vão embaixo de nenhuma):
  // "esticar" ocupa a sobra da coluna; "rolar" também, mas não empurra a coluna (lista que cresce,
  // como o extrato): fica com a altura que a outra coluna deixa e rola por dentro.
  elastico?: 'esticar' | 'rolar'
  children: ReactNode
}

const elasticidade = {
  esticar: { bloco: 'lg:flex-1', corpo: '' },
  rolar: { bloco: 'lg:flex lg:h-0 lg:min-h-[22rem] lg:flex-1 lg:flex-col', corpo: 'lg:min-h-0 lg:flex-1 lg:overflow-y-auto' },
}

// Um painel por bloco, nunca um dentro do outro. `@container` deixa o conteúdo se ajustar à
// largura do bloco, não à da tela.
export function Bloco({ titulo, acoes, className = '', flush, destaque = false, elastico, children }: BlocoProps) {
  const e = elastico ? elasticidade[elastico] : { bloco: '', corpo: '' }
  return (
    <Panel
      title={titulo}
      actions={acoes}
      flush={flush}
      bodyClassName={e.corpo}
      className={`@container min-w-0 ${e.bloco} ${destaque ? '!ring-accent/50' : ''} ${className}`}
    >
      {children}
    </Panel>
  )
}
