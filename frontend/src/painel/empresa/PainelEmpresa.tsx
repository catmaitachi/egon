import { useRef, useState } from 'react'
import { useSearchParams } from 'react-router'
import { PencilSimpleIcon, TrashIcon } from '@phosphor-icons/react'
import { api, fotoUrl, type Vantagem } from '../../lib/api'
import { maskCnpj } from '../../lib/format'
import { useConta } from '../../lib/sessao'
import { dataHora } from '../../lib/format'
import { useLiveNotices } from '../../lib/useLiveNotices'
import { useLoad } from '../../lib/useLoad'
import { useNotificacoes } from '../../lib/useNotificacoes'
import { Bento, Bloco, Coluna } from '../../components/Bento'
import { ActionButton, IconButton } from '../../components/controls'
import { ConfirmDialog, Dialog } from '../../components/Dialog'
import { LiveNotices } from '../../components/LiveNotices'
import { ListState, Notice, rowClass } from '../../components/states'
import { EmpresaForm } from '../../forms/EmpresaForm'
import { notifications } from '../../content'
import { Preco } from '../Preco'
import { ConferirCupom } from './ConferirCupom'
import { nomeDoTipo } from '../textos'
import { VantagemForm } from './VantagemForm'

const alunos = (n: number) => (n === 0 ? 'Nenhum aluno tem saldo ainda' : n === 1 ? '1 aluno já pode trocar' : `${n} alunos já podem trocar`)

// HU08. Blocos: que vantagens tenho publicadas (e quantos alunos já podem pagar cada uma);
// cadastrar uma nova; meus dados.
export function PainelEmpresa() {
  const conta = useConta()
  const vantagens = useLoad(() => api.empresas.vantagens(conta.id), conta.id)
  const empresa = useLoad(() => api.empresas.buscar(conta.id), conta.id)
  const [editando, setEditando] = useState<Vantagem | null>(null)
  const [excluindo, setExcluindo] = useState<Vantagem | null>(null)
  const [editandoDados, setEditandoDados] = useState(false)
  const [aviso, setAviso] = useState<string | null>(null)
  const [formulario, setFormulario] = useState(0)
  const cupons = useLoad(() => api.empresas.cupons(conta.id), conta.id)
  const [busca] = useSearchParams()
  const avisos = useLiveNotices()
  const vistos = useRef(new Set<string>())

  // Resgate novo de uma vantagem desta empresa: a lista de resgates atualiza e aparece o aviso.
  useNotificacoes({
    onMensagem: (n) => {
      if (n.tipo !== 'CUPOM' || n.evento !== 'EMITIDO' || vistos.current.has(n.cupom.codigo)) return
      vistos.current.add(n.cupom.codigo)
      cupons.reload()
      vantagens.reload()
      avisos.push(n.cupom.codigo, notifications.cupomEmpresa(n))
    },
    onConectar: cupons.reload,
  })

  return (
    <>
      <Bento>
        <Coluna className="lg:col-span-7">
          <Bloco titulo="Conferir cupom" className="order-1 md:col-span-6 lg:order-none">
            <ConferirCupom empresa={conta.id} inicial={busca.get('cupom') ?? ''} onConfirmado={() => cupons.reload()} />
          </Bloco>

          <Bloco titulo="Minhas vantagens" flush destaque elastico="rolar" className="order-3 md:col-span-6 lg:order-none">
            <div className="px-5 pt-4 sm:px-7">
              <Notice>{aviso}</Notice>
            </div>
            <ListState
              items={vantagens.data}
              getKey={(v) => v.id}
              loading="Carregando vantagens…"
              empty={{ text: 'Você ainda não publicou vantagens. Cadastre a primeira ao lado: ela aparece na hora no catálogo dos alunos.' }}
              error={vantagens.error?.message}
              onRetry={vantagens.reload}
            >
              {(v) => (
                <div className={`${rowClass} flex gap-4`}>
                  <img src={fotoUrl(v)} alt="" loading="lazy" className="size-20 shrink-0 rounded-[20px] object-cover ring-1 ring-line" />
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-start justify-between gap-3">
                      <p className="min-w-0 font-medium break-words">{v.nome}</p>
                      <Preco valor={v.custo} />
                    </div>
                    <p className="text-sm text-ink-soft">
                      {nomeDoTipo[v.tipo]} · {alunos(v.alunosComSaldo)}
                    </p>
                    <p className="mt-2 line-clamp-2 leading-relaxed break-words">{v.descricao}</p>
                    <div className="mt-3 flex gap-2">
                      <IconButton label={`Editar ${v.nome}`} icon={PencilSimpleIcon} onClick={() => setEditando(v)} />
                      <IconButton label={`Excluir ${v.nome}`} icon={TrashIcon} tone="danger" onClick={() => setExcluindo(v)} />
                    </div>
                  </div>
                </div>
              )}
            </ListState>
          </Bloco>
        </Coluna>

        <Coluna className="lg:col-span-5">
          <Bloco titulo="Resgates" flush className="order-2 md:col-span-6 lg:order-none">
            <div className="max-h-96 overflow-y-auto">
              <ListState
                items={cupons.data}
                getKey={(c) => c.codigo}
                loading="Carregando resgates…"
                empty={{ text: 'Nenhum resgate ainda. Quando um aluno resgatar uma vantagem sua, o cupom aparece aqui.' }}
                error={cupons.error?.message}
                onRetry={cupons.reload}
              >
                {(c) => (
                  <div className={`${rowClass} min-w-0`}>
                    <p className="font-medium break-words">
                      {c.vantagem.nome} · {c.aluno.nome}
                    </p>
                    <p className="text-sm text-ink-soft">
                      <span className="tabular-nums">{c.codigo}</span> ·{' '}
                      {c.status === 'EMITIDO' ? `resgatado em ${dataHora(c.data)}` : `trocado em ${dataHora(c.dataUso ?? c.data)}`}
                    </p>
                  </div>
                )}
              </ListState>
            </div>
          </Bloco>

          <Bloco titulo="Cadastrar vantagem" className="order-4 md:col-span-6 lg:order-none">
            <VantagemForm
              key={formulario}
              label="Publicar"
              busyLabel="Publicando…"
              onSubmit={async (dados) => {
                const nova = await api.empresas.cadastrarVantagem(conta.id, dados)
                vantagens.setData((l) => [...l, nova].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR')))
                setAviso(`${nova.nome} publicada no catálogo.`)
                setFormulario((n) => n + 1)
              }}
            />
          </Bloco>

          <Bloco titulo="Meus dados" elastico="esticar" className="order-5 md:col-span-6 lg:order-none">
            {empresa.data ? (
              <>
                <dl className="grid gap-3">
                  {[
                    ['Empresa', empresa.data.nome],
                    ['CNPJ', maskCnpj(empresa.data.cnpj)],
                    ['E-mail', empresa.data.email],
                    ['Login', empresa.data.login],
                  ].map(([rotulo, valor]) => (
                    <div key={rotulo}>
                      <dt className="text-sm text-ink-soft">{rotulo}</dt>
                      <dd className="break-words">{valor}</dd>
                    </div>
                  ))}
                </dl>
                <ActionButton variant="outline" className="mt-6" onClick={() => setEditandoDados(true)}>
                  Editar dados
                </ActionButton>
              </>
            ) : (
              <p role="status" className="text-ink-soft">
                {empresa.error ? empresa.error.message : 'Carregando…'}
              </p>
            )}
          </Bloco>
        </Coluna>
      </Bento>

      <LiveNotices items={avisos.items} onClose={avisos.close} />

      <Dialog open={editando !== null} onClose={() => setEditando(null)} title="Editar vantagem">
        {editando && (
          <div className="@container">
            <VantagemForm
              vantagem={editando}
              label="Salvar alterações"
              busyLabel="Salvando…"
              onCancel={() => setEditando(null)}
              onSubmit={async (dados) => {
                const nova = await api.empresas.atualizarVantagem(conta.id, editando.id, dados)
                vantagens.setData((l) => l.map((v) => (v.id === nova.id ? nova : v)))
                setEditando(null)
                setAviso(`${nova.nome} atualizada.`)
              }}
            />
          </div>
        )}
      </Dialog>

      <ConfirmDialog
        open={excluindo !== null}
        title="Excluir vantagem"
        confirmLabel="Excluir"
        busyLabel="Excluindo…"
        onCancel={() => setExcluindo(null)}
        onConfirm={async () => {
          if (!excluindo) return
          await api.empresas.excluirVantagem(conta.id, excluindo.id)
          vantagens.setData((l) => l.filter((v) => v.id !== excluindo.id))
          setAviso(`${excluindo.nome} saiu do catálogo.`)
          setExcluindo(null)
        }}
      >
        <strong className="font-medium text-snow">{excluindo?.nome}</strong> sai do catálogo dos alunos. Não dá para desfazer.
      </ConfirmDialog>

      <Dialog open={editandoDados} onClose={() => setEditandoDados(false)} title="Editar dados" description="O CNPJ identifica a empresa e não muda.">
        {empresa.data && (
          <EmpresaForm
            empresa={empresa.data}
            label="Salvar alterações"
            busyLabel="Salvando…"
            onCancel={() => setEditandoDados(false)}
            onSubmit={async (dados) => {
              const nova = await api.empresas.atualizar(conta.id, dados)
              empresa.setData(() => nova)
              setEditandoDados(false)
              setAviso('Dados atualizados.')
            }}
          />
        )}
      </Dialog>
    </>
  )
}
