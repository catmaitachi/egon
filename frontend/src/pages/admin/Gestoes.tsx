import { coins, routes } from '../../content'
import { api } from '../../lib/api'
import { maskCnpj, maskCpf } from '../../lib/format'
import { useConta } from '../../lib/sessao'
import { useLoad } from '../../lib/useLoad'
import { Coin } from '../../components/Coin'
import { ActionLink } from '../../components/controls'
import { Gestao } from '../../components/Gestao'
import { AdminForm, CursoForm, InstituicaoForm, ProfessorForm } from '../../forms/CadastrosAdmin'
import { AlunoForm } from '../../forms/AlunoForm'
import { EmpresaForm } from '../../forms/EmpresaForm'

const n = (qtd: number, um: string, varios: string) => `${qtd} ${qtd === 1 ? um : varios}.`
const detalhe = 'mt-0.5 text-sm break-words text-ink-soft'

// HU04: as instituições conveniadas, que entregam a lista de professores.
export function Instituicoes() {
  const lista = useLoad(api.instituicoes.listar)
  return (
    <Gestao
      titulo="Instituições"
      contagem={(q) => n(q, 'instituição conveniada', 'instituições conveniadas')}
      lista={lista}
      chave={(i) => i.id}
      nome={(i) => i.nome}
      vazio={{ text: 'Nenhuma instituição ainda. Cadastre a primeira para os alunos poderem escolher no cadastro.' }}
      linha={(i) => (
        <>
          <p className="font-medium break-words">{i.nome}</p>
          <p className={detalhe}>
            {i.tipo} · <span className="tabular-nums">CNPJ {maskCnpj(i.cnpj)}</span>
          </p>
        </>
      )}
      novo={{
        botao: 'Nova instituição',
        titulo: 'Nova instituição',
        form: (pronto, cancelar) => (
          <InstituicaoForm label="Cadastrar" busyLabel="Cadastrando…" onCancel={cancelar} onSubmit={async (d) => pronto(await api.instituicoes.cadastrar(d))} />
        ),
      }}
      editar={{
        titulo: 'Editar instituição',
        descricao: (i) => `CNPJ ${maskCnpj(i.cnpj)}. O CNPJ identifica a instituição e não muda.`,
        form: (i, pronto, cancelar) => (
          <InstituicaoForm item={i} label="Salvar alterações" busyLabel="Salvando…" onCancel={cancelar} onSubmit={async (d) => pronto(await api.instituicoes.atualizar(i.id, d))} />
        ),
      }}
      excluir={{
        titulo: 'Excluir instituição?',
        texto: (i) => `${i.nome} sai da lista do cadastro. Quem tem alunos ou professores não pode sair.`,
        acao: (i) => api.instituicoes.excluir(i.id),
      }}
    />
  )
}

export function Cursos() {
  const lista = useLoad(api.cursos.listar)
  return (
    <Gestao
      titulo="Cursos"
      contagem={(q) => n(q, 'curso na lista do cadastro', 'cursos na lista do cadastro')}
      lista={lista}
      chave={(c) => String(c.id)}
      nome={(c) => c.nome}
      vazio={{ text: 'Nenhum curso ainda. Os alunos escolhem o curso numa lista no cadastro.' }}
      linha={(c) => <p className="font-medium break-words">{c.nome}</p>}
      novo={{
        botao: 'Novo curso',
        titulo: 'Novo curso',
        form: (pronto, cancelar) => (
          <CursoForm label="Cadastrar" busyLabel="Cadastrando…" onCancel={cancelar} onSubmit={async (nome) => pronto(await api.cursos.cadastrar(nome))} />
        ),
      }}
      editar={{
        titulo: 'Renomear curso',
        form: (c, pronto, cancelar) => (
          <CursoForm item={c} label="Salvar" busyLabel="Salvando…" onCancel={cancelar} onSubmit={async (nome) => pronto(await api.cursos.atualizar(c.id, nome))} />
        ),
      }}
      excluir={{
        titulo: 'Excluir curso?',
        texto: (c) => `${c.nome} sai da lista do cadastro. Curso com alunos não pode sair.`,
        acao: (c) => api.cursos.excluir(c.id),
      }}
    />
  )
}

// HU04: o admin cadastra o professor com a senha inicial; ele já recebe a cota do semestre.
export function Professores() {
  const lista = useLoad(api.professores.listar)
  return (
    <Gestao
      titulo="Professores"
      contagem={(q) => n(q, 'professor cadastrado', 'professores cadastrados')}
      lista={lista}
      chave={(p) => p.id}
      nome={(p) => p.nome}
      vazio={{ text: 'Nenhum professor ainda. Cadastre os professores que a instituição informou.' }}
      linha={(p) => (
        <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:gap-6">
          <div className="min-w-0 flex-1">
            <p className="font-medium break-words">{p.nome}</p>
            <p className={detalhe}>
              {p.departamento} · {p.instituicao.nome}
            </p>
            <p className={detalhe}>
              <span className="tabular-nums">CPF {maskCpf(p.cpf)}</span> · login {p.login}
            </p>
          </div>
          <p className="flex items-center gap-2 tabular-nums whitespace-nowrap">
            <span className="sr-only">Saldo:</span>
            <Coin size={18} />
            {coins(p.saldo)}
          </p>
        </div>
      )}
      novo={{
        botao: 'Novo professor',
        titulo: 'Novo professor',
        form: (pronto, cancelar) => (
          <ProfessorForm label="Cadastrar" busyLabel="Cadastrando…" onCancel={cancelar} onSubmit={async (d) => pronto(await api.professores.cadastrar(d))} />
        ),
      }}
      editar={{
        titulo: 'Editar professor',
        descricao: (p) => `CPF ${maskCpf(p.cpf)}. O CPF identifica o professor e não muda.`,
        form: (p, pronto, cancelar) => (
          <ProfessorForm item={p} label="Salvar alterações" busyLabel="Salvando…" onCancel={cancelar} onSubmit={async (d) => pronto(await api.professores.atualizar(p.id, d))} />
        ),
      }}
      excluir={{
        titulo: 'Excluir professor?',
        texto: (p) => `O cadastro de ${p.nome} será apagado. Quem já enviou não pode sair: o histórico dos alunos depende dele.`,
        acao: (p) => api.professores.excluir(p.id),
      }}
    />
  )
}

export function Alunos() {
  const lista = useLoad(api.alunos.listar)
  return (
    <Gestao
      titulo="Alunos"
      contagem={(q) => n(q, 'aluno cadastrado', 'alunos cadastrados')}
      lista={lista}
      chave={(a) => a.id}
      nome={(a) => a.nome}
      vazio={{
        text: 'Nenhum aluno ainda. Cada aluno cria a própria conta.',
        action: (
          <ActionLink to={routes.signup} variant="outline">
            Abrir o cadastro
          </ActionLink>
        ),
      }}
      linha={(a) => (
        <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:gap-6">
          <div className="min-w-0 flex-1">
            <p className="font-medium break-words">{a.nome}</p>
            <p className={detalhe}>{a.email}</p>
            <p className={detalhe}>
              <span className="tabular-nums">CPF {maskCpf(a.cpf)}</span> · {a.curso.nome} · {a.instituicao.nome}
            </p>
          </div>
          <p className="flex items-center gap-2 tabular-nums whitespace-nowrap">
            <span className="sr-only">Saldo:</span>
            <Coin size={18} />
            {coins(a.saldo)}
          </p>
        </div>
      )}
      editar={{
        titulo: 'Editar aluno',
        descricao: (a) => `CPF ${maskCpf(a.cpf)}. O CPF identifica o aluno e não muda.`,
        form: (a, pronto, cancelar) => (
          <AlunoForm aluno={a} label="Salvar alterações" busyLabel="Salvando…" onCancel={cancelar} onSubmit={async (d) => pronto(await api.alunos.atualizar(a.id, d))} />
        ),
      }}
      excluir={{
        titulo: 'Excluir aluno?',
        texto: (a) => `O cadastro e o login de ${a.nome} serão apagados. Quem já recebeu ou repassou não pode sair.`,
        acao: (a) => api.alunos.excluir(a.id),
      }}
    />
  )
}

export function Empresas() {
  const lista = useLoad(api.empresas.listar)
  return (
    <Gestao
      titulo="Empresas"
      contagem={(q) => n(q, 'empresa parceira', 'empresas parceiras')}
      lista={lista}
      chave={(e) => e.id}
      nome={(e) => e.nome}
      vazio={{
        text: 'Nenhuma empresa ainda. A empresa parceira faz o próprio cadastro.',
        action: (
          <ActionLink to={routes.partner} variant="outline">
            Abrir o cadastro
          </ActionLink>
        ),
      }}
      linha={(e) => (
        <>
          <p className="font-medium break-words">{e.nome}</p>
          <p className={detalhe}>{e.email}</p>
          <p className={detalhe}>
            <span className="tabular-nums">CNPJ {maskCnpj(e.cnpj)}</span> · login {e.login}
          </p>
        </>
      )}
      editar={{
        titulo: 'Editar empresa',
        descricao: (e) => `CNPJ ${maskCnpj(e.cnpj)}. O CNPJ identifica a empresa e não muda.`,
        form: (e, pronto, cancelar) => (
          <EmpresaForm empresa={e} label="Salvar alterações" busyLabel="Salvando…" onCancel={cancelar} onSubmit={async (d) => pronto(await api.empresas.atualizar(e.id, d))} />
        ),
      }}
      excluir={{
        titulo: 'Excluir empresa?',
        texto: (e) => `O cadastro de ${e.nome} será apagado, com as vantagens e as fotos dela. Não dá para desfazer.`,
        acao: (e) => api.empresas.excluir(e.id),
      }}
    />
  )
}

export function Admins() {
  const conta = useConta()
  const lista = useLoad(api.admins.listar)
  return (
    <Gestao
      titulo="Admins"
      contagem={(q) => n(q, 'admin', 'admins')}
      lista={lista}
      chave={(a) => a.id}
      nome={(a) => a.nome}
      vazio={{ text: 'Nenhum admin.' }}
      linha={(a) => (
        <>
          <p className="font-medium break-words">
            {a.nome}
            {a.id === conta.id && <span className="text-ink-soft"> (você)</span>}
          </p>
          <p className={detalhe}>login {a.login}</p>
        </>
      )}
      novo={{
        botao: 'Novo admin',
        titulo: 'Novo admin',
        form: (pronto, cancelar) => (
          <AdminForm label="Cadastrar" busyLabel="Cadastrando…" onCancel={cancelar} onSubmit={async (d) => pronto(await api.admins.cadastrar(d))} />
        ),
      }}
      editar={{
        titulo: 'Editar admin',
        form: (a, pronto, cancelar) => (
          <AdminForm item={a} label="Salvar alterações" busyLabel="Salvando…" onCancel={cancelar} onSubmit={async (d) => pronto(await api.admins.atualizar(a.id, d))} />
        ),
      }}
      excluir={{
        titulo: 'Excluir admin?',
        texto: (a) => `${a.nome} perde o acesso. Ninguém exclui a si mesmo nem o último admin.`,
        acao: (a) => api.admins.excluir(a.id),
      }}
    />
  )
}
