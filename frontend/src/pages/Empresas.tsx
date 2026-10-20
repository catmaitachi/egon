import { useCallback, useEffect, useState } from 'react'
import { PencilSimpleIcon, PlusIcon, TrashIcon } from '@phosphor-icons/react'
import { routes } from '../content'
import { api, type Empresa, type EmpresaEdicao } from '../lib/api'
import { maskCnpj } from '../lib/documentos'
import { useTitle } from '../lib/useTitle'
import { EmpresaForm } from '../forms/EmpresaForm'
import { ActionLink, IconButton } from '../components/controls'
import { Dialog } from '../components/Dialog'
import { ConfirmDelete, Detail, ListState, Notice } from '../components/Records'
import { PageTitle, Shell } from '../components/Shell'

const byName = (a: Empresa, b: Empresa) => a.nome.localeCompare(b.nome, 'pt-BR')

// Listagem, edição e exclusão de empresas parceiras. O cadastro é a tela pública.
export function Empresas() {
  useTitle('Empresas')
  const [list, setList] = useState<Empresa[] | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [editing, setEditing] = useState<Empresa | null>(null)
  const [removing, setRemoving] = useState<Empresa | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  const load = useCallback(() => api.empresas.listar().then(setList, (e: Error) => setLoadError(e.message)), [])

  useEffect(() => {
    load()
  }, [load])

  const retry = () => {
    setLoadError(null)
    load()
  }

  const save = async (dados: EmpresaEdicao) => {
    if (!editing) return
    const updated = await api.empresas.atualizar(editing.cnpj, dados)
    setList((l) => l && l.map((e) => (e.cnpj === updated.cnpj ? updated : e)).sort(byName))
    setEditing(null)
    setNotice(`Cadastro de ${updated.nome} atualizado.`)
  }

  const remove = async () => {
    if (!removing) return
    await api.empresas.excluir(removing.cnpj)
    setList((l) => l && l.filter((e) => e.cnpj !== removing.cnpj))
    setRemoving(null)
    setNotice(`Cadastro de ${removing.nome} excluído.`)
  }

  return (
    <Shell nav>
      <PageTitle
        eyebrow="Cadastros"
        title="Empresas"
        lead={list?.length ? (list.length === 1 ? '1 empresa parceira.' : `${list.length} empresas parceiras.`) : undefined}
      >
        <ActionLink to={routes.partner}>
          <PlusIcon size={18} weight="bold" />
          Nova empresa
        </ActionLink>
      </PageTitle>

      <div className="mt-10 grid gap-6">
        {notice && <Notice>{notice}</Notice>}

        <ListState
          items={list}
          error={loadError}
          onRetry={retry}
          loadingText="Carregando empresas…"
          emptyText="Nenhuma empresa cadastrada ainda."
          emptyAction={{ to: routes.partner, label: 'Cadastrar empresa' }}
        >
          {list?.map((e) => (
            <li key={e.cnpj} className="flex flex-col gap-4 px-6 py-6 sm:flex-row sm:items-center md:px-8">
              <div className="min-w-0 flex-1">
                <p className="font-display text-lg font-semibold">{e.nome}</p>
                <p className="mt-1 truncate text-ink-soft">{e.email}</p>
                <dl className="mt-3 flex flex-wrap gap-x-6 gap-y-1 text-sm">
                  <Detail label="CNPJ">{maskCnpj(e.cnpj)}</Detail>
                  <Detail label="Login">{e.login}</Detail>
                </dl>
              </div>
              <div className="flex gap-2">
                <IconButton label={`Editar ${e.nome}`} icon={PencilSimpleIcon} onClick={() => setEditing(e)} />
                <IconButton label={`Excluir ${e.nome}`} icon={TrashIcon} tone="danger" onClick={() => setRemoving(e)} />
              </div>
            </li>
          ))}
        </ListState>
      </div>

      <Dialog
        open={editing !== null}
        onClose={() => setEditing(null)}
        title="Editar empresa"
        description={editing && `CNPJ ${maskCnpj(editing.cnpj)}. O CNPJ identifica a empresa e não pode ser alterado.`}
      >
        {editing && <EmpresaForm initial={editing} submitLabel="Salvar" onSubmit={save} onCancel={() => setEditing(null)} />}
      </Dialog>

      <ConfirmDelete name={removing?.nome ?? null} onCancel={() => setRemoving(null)} onConfirm={remove} />
    </Shell>
  )
}
