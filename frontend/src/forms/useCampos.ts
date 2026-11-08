import { useState, type ChangeEvent, type FormEvent } from 'react'
import { ApiError } from '../lib/api'
import { useFormSubmit } from '../lib/useFormSubmit'
import type { Erros } from './regras'

// O backend guarda o login em minúsculas; mostrar assim evita a surpresa de "Ana" virar "ana".
export const minusculo = (v: string) => v.toLowerCase()

// Valores do formulário (texto, como o DOM entrega) com validação local antes da API.
// Erro local e erro da API chegam do mesmo jeito: aviso embaixo do campo e foco no primeiro inválido.
export function useCampos<K extends string>(inicial: Record<K, string>) {
  const { formRef, sending, errors, alert, submit, clear } = useFormSubmit()
  const [valores, setValores] = useState(inicial)

  const campo = (nome: K, mascara?: (v: string) => string) => ({
    name: nome,
    value: valores[nome],
    error: errors[nome],
    onChange: (e: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
      const valor = mascara ? mascara(e.target.value) : e.target.value
      setValores((v) => ({ ...v, [nome]: valor }) as Record<K, string>)
      clear(nome)
    },
  })

  const enviar = (validar: (v: Record<K, string>) => Erros, acao: (v: Record<K, string>) => Promise<void>) => (e: FormEvent) => {
    e.preventDefault()
    submit(async () => {
      const erros = validar(valores)
      if (Object.keys(erros).length > 0) throw new ApiError('DADOS_INVALIDOS', 'Confira os campos destacados.', erros)
      await acao(valores)
    })
  }

  return { formRef, sending, alert, campo, enviar }
}
