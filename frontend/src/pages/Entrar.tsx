import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router'
import { routes } from '../content'
import { useFormSubmit } from '../lib/useFormSubmit'
import { useSessao } from '../lib/sessao'
import { useTitle } from '../lib/useTitle'
import { ActionButton } from '../components/controls'
import { FormAlert, TextField } from '../components/fields'
import { AppShell, PageTitle, Panel } from '../components/Shell'
import { Notice } from '../components/states'
import { ModoDev } from '../components/ModoDev'
import { useKonami } from '../lib/useKonami'

// HU01: uma tela de login para aluno, professor, empresa e admin. Depois, o painel do perfil
// (ou a página que pediu o login).
export function Entrar() {
  useTitle('Entrar')
  const { sessao, expirou, entrar } = useSessao()
  const [busca] = useSearchParams()
  const navigate = useNavigate()
  const { formRef, sending, errors, alert, submit, clear } = useFormSubmit()
  const [login, setLogin] = useState('')
  const [senha, setSenha] = useState('')
  // ↑ ↑ ↓ ↓ ← → ← → B A: mostra as contas de teste (só funciona com a API em dev).
  const modoDev = useKonami()
  // Só caminhos internos: um ?volta= para fora do site viraria redirecionamento aberto.
  const pedido = busca.get('volta') ?? ''
  const volta = /^\/(?![/\\])/.test(pedido) && !pedido.includes('\\') ? pedido : routes.painel

  if (sessao && !sending) return <Navigate to={volta} replace />

  const enviar = (e: FormEvent) => {
    e.preventDefault()
    submit(async () => {
      await entrar(login, senha)
      navigate(volta, { replace: true })
    })
  }

  return (
    <AppShell narrow>
      <div className="mx-auto max-w-md pt-6 sm:pt-10">
        <PageTitle center title="Entrar" lead="Aluno, professor, empresa ou admin: o mesmo login para todos." />
        <Panel className="mt-8">
          <form ref={formRef} onSubmit={enviar} noValidate className="grid gap-5">
            {expirou && <Notice>Sua sessão terminou. Entre de novo para continuar.</Notice>}
            <TextField
              label="Login"
              name="login"
              autoComplete="username"
              autoCapitalize="none"
              spellCheck={false}
              required
              value={login}
              error={errors.login}
              onChange={(e) => {
                setLogin(e.target.value)
                clear('login')
              }}
            />
            <TextField
              label="Senha"
              name="senha"
              type="password"
              autoComplete="current-password"
              required
              value={senha}
              error={errors.senha}
              onChange={(e) => {
                setSenha(e.target.value)
                clear('senha')
              }}
            />
            {alert && <FormAlert>{alert}</FormAlert>}
            <ActionButton type="submit" busy={sending} className="w-full">
              {sending ? 'Entrando…' : 'Entrar'}
            </ActionButton>
          </form>
        </Panel>
        {modoDev && <ModoDev onEntrou={() => navigate(volta, { replace: true })} />}
        <p className="mt-8 text-center text-ink-soft">
          Aluno sem conta?{' '}
          <Link className="rounded-sm text-snow underline underline-offset-4 hover:text-accent" to={routes.signup}>
            Criar conta
          </Link>
          <br />
          Empresa parceira?{' '}
          <Link className="rounded-sm text-snow underline underline-offset-4 hover:text-accent" to={routes.partner}>
            Cadastrar empresa
          </Link>
        </p>
      </div>
    </AppShell>
  )
}
