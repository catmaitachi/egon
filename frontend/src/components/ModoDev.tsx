import { useState } from 'react'
import { CodeIcon } from '@phosphor-icons/react'
import { api, ApiError, type ContaDev, type PerfilSessao } from '../lib/api'
import { nomeDoPerfil, useSessao } from '../lib/sessao'
import { useLoad } from '../lib/useLoad'
import { bate } from '../painel/regras'
import { TextField, FormAlert } from './fields'
import { Panel } from './Shell'

const ordem: PerfilSessao[] = ['ALUNO', 'PROFESSOR', 'EMPRESA', 'ADMIN']

// Modo dev (código Konami no login): as contas de teste, para entrar com um clique, sem senha.
// Só funciona com a API em dev; fora disso a rota não existe e aqui aparece como ligar.
export function ModoDev({ onEntrou }: { onEntrou: () => void }) {
  const contas = useLoad(api.dev.contas)
  const { entrarComo } = useSessao()
  const [busca, setBusca] = useState('')
  const [entrando, setEntrando] = useState<string | null>(null)
  const [erro, setErro] = useState<string | null>(null)

  const entrar = async (c: ContaDev) => {
    setEntrando(c.id)
    setErro(null)
    try {
      await entrarComo(c.perfil, c.id)
      onEntrou()
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Algo deu errado.')
      setEntrando(null)
    }
  }

  const desligado = contas.error instanceof ApiError && contas.error.codigo === 'RECURSO_NAO_ENCONTRADO'
  const visiveis = contas.data?.filter((c) => bate(c.nome, busca) || bate(c.login, busca)) ?? []

  return (
    <Panel
      title="Modo dev"
      actions={<CodeIcon size={20} aria-hidden className="text-ink-soft" />}
      className="mt-6"
    >
      {desligado ? (
        <p className="text-ink-soft">
          A API está sem o modo dev. Suba com <code className="text-snow">-Dspring-boot.run.profiles=dev</code> ou{' '}
          <code className="text-snow">EGON_DEV=true</code>.
        </p>
      ) : contas.error ? (
        <FormAlert>{contas.error.message}</FormAlert>
      ) : !contas.data ? (
        <p role="status" className="text-ink-soft">
          Carregando contas…
        </p>
      ) : (
        <div className="grid gap-5">
          <p className="text-ink-soft">Entre em qualquer conta de teste com um clique, sem senha.</p>
          <TextField label="Buscar" type="search" autoComplete="off" value={busca} onChange={(e) => setBusca(e.target.value)} />
          {erro && <FormAlert>{erro}</FormAlert>}
          {ordem.map((perfil) => {
            const doPerfil = visiveis.filter((c) => c.perfil === perfil)
            if (doPerfil.length === 0) return null
            return (
              <section key={perfil} aria-label={nomeDoPerfil[perfil]}>
                <h3 className="mb-2 font-display text-xs font-semibold tracking-[0.08em] text-ink-soft uppercase">{nomeDoPerfil[perfil]}</h3>
                <ul className="divide-y divide-line rounded-[20px] ring-1 ring-line">
                  {doPerfil.map((c) => (
                    <li key={c.id}>
                      <button
                        type="button"
                        disabled={entrando !== null}
                        aria-busy={entrando === c.id || undefined}
                        onClick={() => entrar(c)}
                        className="flex w-full flex-col items-start rounded-[20px] px-5 py-3 text-left transition-colors duration-150 hover:bg-snow/[0.05] disabled:opacity-60"
                      >
                        <span className="font-medium break-words">{c.nome}</span>
                        <span className="text-sm break-words text-ink-soft">
                          {c.detalhe} · login {c.login}
                        </span>
                      </button>
                    </li>
                  ))}
                </ul>
              </section>
            )
          })}
          {visiveis.length === 0 && <p className="text-ink-soft">Nenhuma conta com “{busca.trim()}”.</p>}
        </div>
      )}
    </Panel>
  )
}
