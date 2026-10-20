import { useState } from 'react'
import { coins, routes } from '../content'
import { api, type Aluno, type AlunoCadastro } from '../lib/api'
import { useTitle } from '../lib/useTitle'
import { AlunoForm } from '../forms/AlunoForm'
import { PageTitle, Panel, Shell } from '../components/Shell'
import { SignupDone } from '../components/SignupDone'

// HU02. A landing manda para cá pelo "Criar conta".
export function CadastroAluno() {
  useTitle('Criar conta')
  const [created, setCreated] = useState<Aluno | null>(null)

  const create = async (dados: AlunoCadastro) => {
    setCreated(await api.alunos.cadastrar(dados))
    window.scrollTo({ top: 0 })
  }

  return (
    <Shell>
      <div className="mx-auto max-w-2xl">
        {created ? (
          <SignupDone
            title="Conta criada"
            text={`Tudo certo, ${created.nome.split(' ')[0]}. Sua conta começa com ${coins(created.saldo)} e você já pode entrar.`}
            next={{ to: routes.login, label: 'Entrar' }}
          />
        ) : (
          <>
            <PageTitle
              center
              eyebrow="Aluno"
              title="Criar conta"
              lead="Seus dados, sua instituição e um login. Os professores da sua instituição passam a poder te reconhecer."
            />
            <Panel className="mt-10 px-6 py-8 md:px-10 md:py-10">
              <AlunoForm submitLabel="Criar conta" onSubmit={create} />
            </Panel>
          </>
        )}
      </div>
    </Shell>
  )
}
