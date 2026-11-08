import { useState } from 'react'
import { brand, coinName } from '../content'
import { api, type Empresa } from '../lib/api'
import { useTitle } from '../lib/useTitle'
import { ActionLink } from '../components/controls'
import { AppShell, PageTitle, Panel } from '../components/Shell'
import { EmpresaForm } from '../forms/EmpresaForm'
import { Done } from '../forms/parts'

// HU03, destino de "Cadastrar empresa" na landing. Concluído, a confirmação toma o lugar do formulário.
export function CadastroEmpresa() {
  useTitle('Cadastrar empresa')
  const [criada, setCriada] = useState<Empresa | null>(null)

  return (
    <AppShell narrow>
      {criada ? (
        <Done title="Empresa cadastrada" text={`Tudo certo. ${criada.nome} já faz parte do ${brand.name}.`}>
          <ActionLink to="/" variant="outline">
            Voltar ao início
          </ActionLink>
        </Done>
      ) : (
        <>
          <PageTitle
            center
            title="Cadastrar empresa"
            lead={`Ofereça vantagens aos alunos em troca de ${coinName(2)}.`}
          />
          <Panel className="mt-8">
            <EmpresaForm
              label="Cadastrar empresa"
              busyLabel="Cadastrando…"
              onSubmit={async (dados, cnpj) => setCriada(await api.empresas.cadastrar({ ...dados, cnpj }))}
            />
          </Panel>
        </>
      )}
    </AppShell>
  )
}
