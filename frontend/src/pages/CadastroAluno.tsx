import { useState } from 'react'
import { coinName, routes } from '../content'
import { api, type Aluno } from '../lib/api'
import { useTitle } from '../lib/useTitle'
import { ActionLink } from '../components/controls'
import { AppShell, PageTitle, Panel } from '../components/Shell'
import { AlunoForm } from '../forms/AlunoForm'
import { Done } from '../forms/parts'

// HU02, destino de "Criar conta" na landing. Concluído, a confirmação toma o lugar do formulário.
export function CadastroAluno() {
  useTitle('Criar conta')
  const [criado, setCriado] = useState<Aluno | null>(null)

  return (
    <AppShell narrow>
      {criado ? (
        <Done
          title="Conta criada"
          text={`Tudo certo, ${criado.nome.split(' ')[0]}. Quando um professor da ${criado.instituicao.nome} te reconhecer, os ${coinName(2)} caem no seu saldo.`}
        >
          <ActionLink to={routes.login}>Entrar</ActionLink>
          <ActionLink to="/" variant="outline">
            Voltar ao início
          </ActionLink>
        </Done>
      ) : (
        <>
          <PageTitle
            center
            title="Criar conta"
            lead={`Com a conta criada, os professores da sua instituição já podem te enviar ${coinName(2)}.`}
          />
          <Panel className="mt-8">
            <AlunoForm
              label="Criar conta"
              busyLabel="Criando conta…"
              onSubmit={async (dados, cpf) => setCriado(await api.alunos.cadastrar({ ...dados, cpf }))}
            />
          </Panel>
        </>
      )}
    </AppShell>
  )
}
