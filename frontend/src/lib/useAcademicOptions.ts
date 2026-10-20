import { useEffect, useState } from 'react'
import { api, type Curso, type Instituicao } from './api'

type Options = { instituicoes: Instituicao[]; cursos: Curso[] }

// As listas mudam raramente: uma busca serve para a sessão inteira. Se falhar, a próxima
// abertura do formulário tenta de novo.
let cache: Promise<Options> | null = null

export function useAcademicOptions() {
  const [options, setOptions] = useState<Options | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let alive = true
    cache ??= Promise.all([api.instituicoes(), api.cursos()]).then(([instituicoes, cursos]) => ({ instituicoes, cursos }))
    cache
      .then((o) => alive && setOptions(o))
      .catch((e: Error) => {
        cache = null
        if (alive) setError(e.message)
      })
    return () => {
      alive = false
    }
  }, [])

  return { options, error }
}
