import { CoinVerticalIcon } from '@phosphor-icons/react'

// A moeda: um disco no tom de acento com o ícone. É um dos poucos pontos de cor da página.
export function Coin({ size = 20, glow = false, className = '' }: { size?: number; glow?: boolean; className?: string }) {
  return (
    <span
      aria-hidden
      className={`inline-grid shrink-0 place-items-center rounded-full bg-accent text-snow ${
        glow ? 'shadow-[0_0_0_4px_color-mix(in_oklab,var(--accent)_22%,transparent),0_0_28px_-2px_var(--accent)]' : ''
      } ${className}`}
      style={{ width: size, height: size }}
    >
      <CoinVerticalIcon size={Math.round(size * 0.6)} weight="fill" />
    </span>
  )
}
