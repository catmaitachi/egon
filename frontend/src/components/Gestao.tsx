import { useState, type ReactNode } from 'react'
import { PencilSimpleIcon, PlusIcon, TrashIcon } from '@phosphor-icons/react'
import type { ApiError } from '../lib/api'
import { useTitle } from '../lib/useTitle'
import { TelaLogada } from './Conta'
import { ActionButton, IconButton } from './controls'
import { ConfirmDialog, Dialog } from './Dialog'
import { PageTitle, Panel } from './Shell'
import { ListState, Notice, rowClass } from './states'

type Lista<T> = {
  data: T[] | null
  error: ApiError | null
  reload: () => void
  setData: (update: (atual: T[]) => T[]) => void
}

type GestaoProps<T> = {
  titulo: string
  // "3 alunos cadastrados."
  contagem: (n: number) => string
  lista: Lista<T>
  chave: (item: T) => string
  nome: (item: T) => string
  // O conteúdo da linha; editar e excluir são postos à direita.
  linha: (item: T) => ReactNode
  vazio: { text: string; action?: ReactNode }
  novo?: { botao: string; titulo: string; form: (pronto: (criado: T) => void, cancelar: () => void) => ReactNode }
  editar?: { titulo: string; descricao?: (item: T) => ReactNode; form: (item: T, pronto: (salvo: T) => void, cancelar: () => void) => ReactNode }
  excluir?: { titulo: string; texto: (item: T) => ReactNode; acao: (item: T) => Promise<void> }
}

// Uma tela de gestão do admin: listar, criar, editar e excluir com confirmação. Os 409 (registro em uso)
// aparecem dentro da confirmação, com a mensagem da API.
export function Gestao<T>({ titulo, contagem, lista, chave, nome, linha, vazio, novo, editar, excluir }: GestaoProps<T>) {
  useTitle(titulo)
  const [criando, setCriando] = useState(false)
  const [editando, setEditando] = useState<T | null>(null)
  const [excluindo, setExcluindo] = useState<T | null>(null)
  // Some ao abrir outra ação, para o leitor de tela anunciar a próxima mesmo se o texto se repetir.
  const [aviso, setAviso] = useState<string | null>(null)
  const itens = lista.data?.toSorted((a, b) => nome(a).localeCompare(nome(b), 'pt-BR')) ?? null

  const abrir = (acao: () => void) => {
    setAviso(null)
    acao()
  }

  return (
    <TelaLogada>
      <PageTitle title={titulo} lead={itens?.length ? contagem(itens.length) : undefined}>
        {novo && (
          <ActionButton onClick={() => abrir(() => setCriando(true))}>
            <PlusIcon size={18} weight="bold" aria-hidden />
            {novo.botao}
          </ActionButton>
        )}
      </PageTitle>

      <div className="mt-8">
        <Notice>{aviso}</Notice>
      </div>

      <Panel flush className={aviso ? 'mt-5' : ''}>
        <ListState
          items={itens}
          getKey={chave}
          loading={`Carregando ${titulo.toLowerCase()}…`}
          empty={vazio}
          error={lista.error?.message}
          onRetry={lista.reload}
          label={titulo}
        >
          {(item) => (
            <div className={`${rowClass} flex flex-col gap-3 font-normal sm:flex-row sm:items-center sm:gap-6`}>
              <div className="min-w-0 flex-1">{linha(item)}</div>
              {(editar || excluir) && (
                <div className="flex gap-2">
                  {editar && <IconButton label={`Editar ${nome(item)}`} icon={PencilSimpleIcon} onClick={() => abrir(() => setEditando(item))} />}
                  {excluir && (
                    <IconButton label={`Excluir ${nome(item)}`} icon={TrashIcon} tone="danger" onClick={() => abrir(() => setExcluindo(item))} />
                  )}
                </div>
              )}
            </div>
          )}
        </ListState>
      </Panel>

      {novo && (
        <Dialog open={criando} onClose={() => setCriando(false)} title={novo.titulo}>
          {criando &&
            novo.form(
              (criado) => {
                lista.setData((l) => [...l, criado])
                setCriando(false)
                setAviso(`${nome(criado)} cadastrado.`)
              },
              () => setCriando(false),
            )}
        </Dialog>
      )}

      {editar && (
        <Dialog
          open={editando !== null}
          onClose={() => setEditando(null)}
          title={editar.titulo}
          description={editando && editar.descricao?.(editando)}
        >
          {editando &&
            editar.form(
              editando,
              (salvo) => {
                lista.setData((l) => l.map((i) => (chave(i) === chave(salvo) ? salvo : i)))
                setEditando(null)
                setAviso(`${nome(salvo)} atualizado.`)
              },
              () => setEditando(null),
            )}
        </Dialog>
      )}

      {excluir && (
        <ConfirmDialog
          open={excluindo !== null}
          title={excluir.titulo}
          confirmLabel="Excluir"
          busyLabel="Excluindo…"
          onCancel={() => setExcluindo(null)}
          onConfirm={async () => {
            if (!excluindo) return
            await excluir.acao(excluindo)
            lista.setData((l) => l.filter((i) => chave(i) !== chave(excluindo)))
            setExcluindo(null)
            setAviso(`${nome(excluindo)} excluído.`)
          }}
        >
          {excluindo && excluir.texto(excluindo)}
        </ConfirmDialog>
      )}
    </TelaLogada>
  )
}
