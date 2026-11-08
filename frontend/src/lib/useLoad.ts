import { useCallback, useEffect, useRef, useState } from 'react'
import { ApiError } from './api'

const paraApiError = (e: unknown) => (e instanceof ApiError ? e : new ApiError('ERRO', 'Algo deu errado. Tente de novo.'))

// Busca na montagem e quando `key` muda (ex. o CPF da rota). `data` null é "carregando".
// `reload` busca de novo sem apagar o que está na tela (extrato ao reconectar, "Tentar de novo").
// `setData` recebe uma função, para atualizar a partir do valor atual (aviso ao vivo, edição na lista).
// Com `ativo` falso não busca nada até ele virar verdadeiro (ex. o extrato espera a pessoa existir).
export function useLoad<T>(load: () => Promise<T>, key = '', ativo = true) {
  // Resultado e erro guardam a chave a que pertencem: trocar de CPF nunca mostra dados da pessoa anterior.
  const [result, setResult] = useState<{ key: string; data: T } | null>(null)
  const [failure, setFailure] = useState<{ key: string; error: ApiError } | null>(null)
  const latest = useRef(load)
  const last = useRef(0)

  useEffect(() => {
    latest.current = load
  })

  const fetchNow = useCallback(() => {
    // Só a resposta da última busca vale.
    const n = ++last.current
    latest.current().then(
      (data) => {
        if (n !== last.current) return
        setResult({ key, data })
        setFailure(null)
      },
      (e: unknown) => n === last.current && setFailure({ key, error: paraApiError(e) }),
    )
  }, [key])

  useEffect(() => {
    if (ativo) fetchNow()
  }, [fetchNow, ativo])

  const reload = useCallback(() => {
    setFailure(null)
    fetchNow()
  }, [fetchNow])

  const setData = useCallback(
    (update: (current: T) => T) => setResult((r) => (r && r.key === key ? { key, data: update(r.data) } : r)),
    [key],
  )

  return {
    data: result?.key === key ? result.data : null,
    error: failure?.key === key ? failure.error : null,
    reload,
    setData,
  }
}
