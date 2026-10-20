import { useTitle } from '../lib/useTitle'
import { ActionLink } from '../components/controls'
import { PageTitle, Shell } from '../components/Shell'

// Inclui o /entrar, que a landing já linka e ainda não existe.
export function NaoEncontrada() {
  useTitle('Página não encontrada')
  return (
    <Shell>
      <div className="mx-auto flex max-w-2xl flex-col items-center gap-10 pt-16">
        <PageTitle center eyebrow="404" title="Página não encontrada" lead="Esta tela ainda não existe ou mudou de endereço." />
        <ActionLink to="/" variant="outline">
          Voltar ao início
        </ActionLink>
      </div>
    </Shell>
  )
}
