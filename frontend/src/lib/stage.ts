import { useSyncExternalStore } from 'react'

// Hover da ação principal, compartilhado com o fundo do hero fora da árvore do React
// para que o hover não re-renderize a página inteira.

let cta = false
const listeners = new Set<() => void>()

export const hoverCta = (on: boolean) => {
  cta = on
  listeners.forEach((l) => l())
}

const subscribe = (l: () => void) => {
  listeners.add(l)
  return () => listeners.delete(l)
}

export const useCtaHover = () => useSyncExternalStore(subscribe, () => cta)
