import { useState } from 'react'

// Uma ação de botão (aprovar, cancelar...) com estado de ocupado e a mensagem de erro da API.
export function useAcao(acao: () => Promise<unknown>) {
  const [busy, setBusy] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const run = async () => {
    setBusy(true)
    setErro(null)
    try {
      await acao()
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Algo deu errado. Tente de novo.')
    } finally {
      setBusy(false)
    }
  }
  return { busy, erro, run }
}
