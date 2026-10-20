import { useCallback, useEffect, useState } from 'react'
import { PencilSimpleIcon, PlusIcon, TrashIcon } from '@phosphor-icons/react'
import { routes } from '../content'
import { api, type Aluno, type AlunoEdicao } from '../lib/api'
import { maskCpf } from '../lib/documentos'
import { useTitle } from '../lib/useTitle'
import { AlunoForm } from '../forms/AlunoForm'
import { ActionLink, IconButton } from '../components/controls'
import { Dialog } from '../components/Dialog'
import { ConfirmDelete, Detail, ListState, Notice } from '../components/Records'
import { PageTitle, Shell } from '../components/Shell'

const byName = (a: Aluno, b: Aluno) => a.nome.localeCompare(b.nome, 'pt-BR')

// Listagem, edição e exclusão de alunos. O cadastro é a tela pública de "Criar conta".
export function Alunos() {
  useTitle('Alunos')
  const [list, setList] = useState<Aluno[] | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [editing, setEditing] = useState<Aluno | null>(null)
  const [removing, setRemoving] = useState<Aluno | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  const load = useCallback(() => api.alunos.listar().then(setList, (e: Error) => setLoadError(e.message)), [])

  useEffect(() => {
    load()
  }, [load])

  const retry = () => {
    setLoadError(null)
    load()
  }

  const save = async (dados: AlunoEdicao) => {
    if (!editing) return
    const updated = await api.alunos.atualizar(editing.cpf, dados)
    setList((l) => l && l.map((a) => (a.cpf === updated.cpf ? updated : a)).sort(byName))
    setEditing(null)
    setNotice(`Cadastro de ${updated.nome} atualizado.`)
  }

  const remove = async () => {
    if (!removing) return
    await api.alunos.excluir(removing.cpf)
    setList((l) => l && l.filter((a) => a.cpf !== removing.cpf))
    setRemoving(null)
    setNotice(`Cadastro de ${removing.nome} excluído.`)
  }

  return (
    <Shell nav>
      <PageTitle
        eyebrow="Cadastros"
        title="Alunos"
        lead={list?.length ? (list.length === 1 ? '1 aluno cadastrado.' : `${list.length} alunos cadastrados.`) : undefined}
      >
        <ActionLink to={routes.signup}>
          <PlusIcon size={18} weight="bold" />
          Novo aluno
        </ActionLink>
      </PageTitle>

      <div className="mt-10 grid gap-6">
        {notice && <Notice>{notice}</Notice>}

        <ListState
          items={list}
          error={loadError}
          onRetry={retry}
          loadingText="Carregando alunos…"
          emptyText="Nenhum aluno cadastrado ainda."
          emptyAction={{ to: routes.signup, label: 'Cadastrar aluno' }}
        >
          {list?.map((a) => (
            <li key={a.cpf} className="flex flex-col gap-4 px-6 py-6 sm:flex-row sm:items-center md:px-8">
              <div className="min-w-0 flex-1">
                <p className="font-display text-lg font-semibold">{a.nome}</p>
                <p className="mt-1 truncate text-ink-soft">{a.email}</p>
                <dl className="mt-3 flex flex-wrap gap-x-6 gap-y-1 text-sm">
                  <Detail label="CPF">{maskCpf(a.cpf)}</Detail>
                  <Detail label="Instituição">{a.instituicao.nome}</Detail>
                  <Detail label="Curso">{a.curso.nome}</Detail>
                </dl>
              </div>
              <div className="flex gap-2">
                <IconButton label={`Editar ${a.nome}`} icon={PencilSimpleIcon} onClick={() => setEditing(a)} />
                <IconButton label={`Excluir ${a.nome}`} icon={TrashIcon} tone="danger" onClick={() => setRemoving(a)} />
              </div>
            </li>
          ))}
        </ListState>
      </div>

      <Dialog
        open={editing !== null}
        onClose={() => setEditing(null)}
        title="Editar aluno"
        description={editing && `CPF ${maskCpf(editing.cpf)}. O CPF identifica o aluno e não pode ser alterado.`}
      >
        {editing && <AlunoForm initial={editing} submitLabel="Salvar" onSubmit={save} onCancel={() => setEditing(null)} />}
      </Dialog>

      <ConfirmDelete name={removing?.nome ?? null} onCancel={() => setRemoving(null)} onConfirm={remove} />
    </Shell>
  )
}
