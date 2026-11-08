import { useConta } from '../lib/sessao'
import { useTitle } from '../lib/useTitle'
import { TelaLogada } from '../components/Conta'
import { PageTitle } from '../components/Shell'
import { PainelAdmin } from '../painel/admin/PainelAdmin'
import { PainelAluno } from '../painel/aluno/PainelAluno'
import { PainelEmpresa } from '../painel/empresa/PainelEmpresa'
import { PainelProfessor } from '../painel/professor/PainelProfessor'

const paineis = { ALUNO: PainelAluno, PROFESSOR: PainelProfessor, EMPRESA: PainelEmpresa, ADMIN: PainelAdmin }

// /painel: o dashboard do perfil de quem está logado. O h1 é o nome da pessoa.
export function Painel() {
  const conta = useConta()
  useTitle('Painel')
  const Dashboard = paineis[conta.perfil]
  return (
    <TelaLogada>
      <PageTitle title={conta.nome} />
      <div className="mt-8">
        <Dashboard />
      </div>
    </TelaLogada>
  )
}
