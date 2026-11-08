import { useEffect, useState } from 'react'

const CODIGO = ['ArrowUp', 'ArrowUp', 'ArrowDown', 'ArrowDown', 'ArrowLeft', 'ArrowRight', 'ArrowLeft', 'ArrowRight', 'b', 'a']
const CHAVE = 'egon:modo-dev'

// Lembra na aba (sessionStorage) para o modo dev continuar depois de sair; sem storage, só não lembra.
const lembrado = () => {
  try {
    return sessionStorage.getItem(CHAVE) === '1'
  } catch {
    return false
  }
}

// ↑ ↑ ↓ ↓ ← → ← → B A, fora de campos de texto (lá as setas e as letras são do campo).
export function useKonami() {
  const [ativo, setAtivo] = useState(lembrado)
  useEffect(() => {
    if (ativo) return
    let pos = 0
    const tecla = (e: KeyboardEvent) => {
      const alvo = e.target as HTMLElement
      if (alvo.closest('input, textarea, select, [contenteditable="true"]')) return
      const k = e.key.length === 1 ? e.key.toLowerCase() : e.key
      pos = k === CODIGO[pos] ? pos + 1 : k === CODIGO[0] ? 1 : 0
      if (pos === CODIGO.length) {
        try {
          sessionStorage.setItem(CHAVE, '1')
        } catch {
          // sem storage, vale só até recarregar
        }
        setAtivo(true)
      }
    }
    window.addEventListener('keydown', tecla)
    return () => window.removeEventListener('keydown', tecla)
  }, [ativo])
  return ativo
}
