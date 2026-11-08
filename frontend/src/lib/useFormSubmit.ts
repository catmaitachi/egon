import { useEffect, useRef, useState } from 'react'
import { ApiError } from './api'

// Estado de envio de um formulário ligado à API: erros por campo vêm do backend
// ({ campos }) e o foco vai para o primeiro campo marcado, para quem usa teclado.
export function useFormSubmit() {
  const formRef = useRef<HTMLFormElement>(null)
  const [sending, setSending] = useState(false)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [alert, setAlert] = useState<string | null>(null)
  // Conta os envios que falharam. O foco vai ao primeiro campo marcado só depois de uma falha:
  // se seguisse `errors`, cada clear() tiraria o cursor do campo em que a pessoa está digitando.
  const [falhas, setFalhas] = useState(0)

  useEffect(() => {
    if (falhas === 0) return
    formRef.current?.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus()
  }, [falhas])

  // Devolve true quando a ação deu certo, para quem precisa limpar o formulário depois.
  const submit = async (action: () => Promise<void>) => {
    setSending(true)
    setAlert(null)
    try {
      await action()
      setErrors({})
      return true
    } catch (e) {
      const error = e instanceof ApiError ? e : new ApiError('ERRO', 'Algo deu errado. Tente de novo.')
      setErrors(error.campos)
      setFalhas((n) => n + 1)
      // Erro de um campo só já aparece embaixo dele; o aviso no topo seria a mesma frase duas vezes.
      setAlert(Object.values(error.campos).includes(error.message) ? null : error.message)
      return false
    } finally {
      setSending(false)
    }
  }

  // Ao editar um campo, o aviso dele some: o erro era do valor antigo.
  const clear = (field: string) => {
    if (!(field in errors)) return
    const rest = { ...errors }
    delete rest[field]
    setErrors(rest)
  }

  return { formRef, sending, errors, alert, submit, clear }
}
