import { useCallback, useState } from 'react'
import type { LiveNotice } from '../content'
import { useMedia } from './useMedia'

export type ShownNotice = LiveNotice & { id: string }

// Três avisos na tela no máximo; o mais antigo sai primeiro. No celular, um por vez: três cobririam
// quase a tela toda, e o extrato já mostra todas as entradas.
const MAX = 3

// Fila dos avisos ao vivo. O id (o da transação) evita mostrar o mesmo aviso duas vezes.
export function useLiveNotices() {
  const max = useMedia('(max-width: 639px)') ? 1 : MAX
  const [items, setItems] = useState<ShownNotice[]>([])
  const push = useCallback(
    (id: string, notice: LiveNotice) => setItems((l) => [...l.filter((n) => n.id !== id), { id, ...notice }].slice(-max)),
    [max],
  )
  const close = useCallback((id: string) => setItems((l) => l.filter((n) => n.id !== id)), [])
  return { items, push, close }
}
