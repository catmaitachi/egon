import { useId, useState } from 'react'
import { CircleIcon, RadioButtonIcon } from '@phosphor-icons/react'
import type { ApiError, Colega } from '../lib/api'
import { TextField } from '../components/fields'
import { ListState } from '../components/states'
import { bate, porNome } from './regras'

type EscolhaProps = {
  // O nome do campo (para os erros da API) e o rótulo visível.
  campo: string
  label: string
  pessoas: { data: Colega[] | null; error: ApiError | null; reload: () => void }
  valor: string
  onChange: (id: string) => void
  erro?: string
  vazio: string
}

// Busca por nome e uma lista de rádio nativa: acessível por teclado e leitor de tela. A pessoa
// escolhida continua visível mesmo fora da busca.
export function Escolha({ campo, label, pessoas, valor, onChange, erro, vazio }: EscolhaProps) {
  const [busca, setBusca] = useState('')
  const buscaId = useId()
  const todas = pessoas.data && [...pessoas.data].sort(porNome)
  const visiveis = todas ? todas.filter((p) => p.id === valor || bate(p.nome, busca)) : null
  const escolhida = todas?.find((p) => p.id === valor)
  return (
    <div>
      <TextField
        id={buscaId}
        label={label}
        type="search"
        placeholder="Buscar pelo nome"
        autoComplete="off"
        value={busca}
        onChange={(e) => setBusca(e.target.value)}
        error={erro}
        hint={escolhida && `Escolhido: ${escolhida.nome}`}
      />
      <fieldset
        aria-describedby={erro ? `${buscaId}-erro` : undefined}
        className="mt-3 max-h-64 min-w-0 overflow-y-auto rounded-[20px] ring-1 ring-snow/40"
      >
        <legend className="sr-only">{label}</legend>
        <ListState
          items={visiveis}
          getKey={(p) => p.id}
          loading="Carregando…"
          empty={{ text: busca.trim() ? `Ninguém com “${busca.trim()}”.` : vazio }}
          error={pessoas.error?.message}
          onRetry={pessoas.reload}
        >
          {(p) => (
            <label className="relative flex cursor-pointer items-center gap-3 px-5 py-3 transition-colors duration-150 hover:bg-snow/[0.04] has-[:checked]:bg-snow/[0.06] has-[:focus-visible]:rounded-[20px] has-[:focus-visible]:outline-2 has-[:focus-visible]:-outline-offset-2 has-[:focus-visible]:outline-snow">
              <input
                type="radio"
                name={campo}
                value={p.id}
                checked={valor === p.id}
                onChange={() => onChange(p.id)}
                className="sr-only"
              />
              {valor === p.id ? (
                <RadioButtonIcon size={20} weight="fill" aria-hidden className="shrink-0 text-snow" />
              ) : (
                <CircleIcon size={20} aria-hidden className="shrink-0 text-ink-soft" />
              )}
              <span className="min-w-0">
                <span className="block font-medium break-words">{p.nome}</span>
                <span className="block text-sm text-ink-soft">{p.curso}</span>
              </span>
            </label>
          )}
        </ListState>
      </fieldset>
    </div>
  )
}
