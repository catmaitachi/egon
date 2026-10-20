import { useState } from 'react'
import { brand, routes } from '../content'
import { api, type Empresa, type EmpresaCadastro } from '../lib/api'
import { useTitle } from '../lib/useTitle'
import { EmpresaForm } from '../forms/EmpresaForm'
import { PageTitle, Panel, Shell } from '../components/Shell'
import { SignupDone } from '../components/SignupDone'

// HU03. A landing manda para cá pelo "Cadastrar empresa".
export function CadastroEmpresa() {
  useTitle('Cadastrar empresa')
  const [created, setCreated] = useState<Empresa | null>(null)

  const create = async (dados: EmpresaCadastro) => {
    setCreated(await api.empresas.cadastrar(dados))
    window.scrollTo({ top: 0 })
  }

  return (
    <Shell>
      <div className="mx-auto max-w-2xl">
        {created ? (
          <SignupDone
            title="Empresa cadastrada"
            text={`${created.nome} já faz parte do ${brand.name}. Entre com o seu login para publicar vantagens para os alunos.`}
            next={{ to: routes.login, label: 'Entrar' }}
          />
        ) : (
          <>
            <PageTitle
              center
              eyebrow="Empresa parceira"
              title="Cadastrar empresa"
              lead={`Com a conta criada, sua empresa publica vantagens que os alunos trocam por ${brand.coin}s.`}
            />
            <Panel className="mt-10 px-6 py-8 md:px-10 md:py-10">
              <EmpresaForm submitLabel="Cadastrar empresa" onSubmit={create} />
            </Panel>
          </>
        )}
      </div>
    </Shell>
  )
}
