import { routes } from '../content'
import { useTitle } from '../lib/useTitle'
import { ActionLink } from '../components/controls'
import { AppShell, PageTitle } from '../components/Shell'

export function NaoEncontrada() {
  useTitle('Página não encontrada')
  return (
    <AppShell narrow>
      <div className="pt-16">
        <PageTitle center title="Página não encontrada" lead="Este endereço não existe ou mudou. Volte ao início ou entre com seu perfil.">
          <ActionLink to="/">Voltar ao início</ActionLink>
          <ActionLink to={routes.login} variant="outline">
            Entrar
          </ActionLink>
        </PageTitle>
      </div>
    </AppShell>
  )
}
