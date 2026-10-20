import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router'
import { painelDe, routes } from '../content'
import { api } from '../lib/api'
import { guardarSessao, useSessao } from '../lib/sessao'
import { useFormSubmit } from '../lib/useFormSubmit'
import { useTitle } from '../lib/useTitle'
import { ActionButton } from '../components/controls'
import { FormAlert, TextField } from '../components/form'
import { PageTitle, Panel, Shell } from '../components/Shell'

// HU01: um login para aluno, professor e empresa. Cada perfil cai no próprio painel.
export function Entrar() {
  useTitle('Entrar')
  const sessao = useSessao()
  const navigate = useNavigate()
  const location = useLocation()
  const [login, setLogin] = useState('')
  const [senha, setSenha] = useState('')
  const { formRef, sending, errors, alert, submit, clear } = useFormSubmit()

  // Quem já está logado vai direto para o painel.
  if (sessao) return <Navigate to={painelDe[sessao.perfil]} replace />

  const onSubmit = (e: FormEvent) => {
    e.preventDefault()
    submit(async () => {
      const nova = await api.auth.entrar(login, senha)
      guardarSessao(nova)
      // Volta para a área que pediu login, se for do mesmo perfil.
      const de = (location.state as { de?: string } | null)?.de
      navigate(de && de === painelDe[nova.perfil] ? de : painelDe[nova.perfil], { replace: true })
    })
  }

  return (
    <Shell>
      <div className="mx-auto max-w-md">
        <PageTitle center eyebrow="Aluno, professor ou empresa" title="Entrar" />
        <Panel className="mt-10 px-6 py-8 md:px-10 md:py-10">
          <form ref={formRef} noValidate onSubmit={onSubmit} className="grid gap-6">
            {alert && <FormAlert>{alert}</FormAlert>}
            <TextField
              label="Login"
              name="login"
              autoComplete="username"
              autoCapitalize="none"
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
              value={senha}
              error={errors.senha}
              onChange={(e) => {
                setSenha(e.target.value)
                clear('senha')
              }}
            />
            <ActionButton type="submit" disabled={sending} className="mt-2 w-full">
              {sending ? 'Entrando…' : 'Entrar'}
            </ActionButton>
          </form>
        </Panel>
        <p className="mt-8 text-center text-ink-soft">
          Ainda não tem conta?{' '}
          <Link to={routes.signup} className="text-snow underline underline-offset-4 hover:text-accent">
            Criar conta de aluno
          </Link>{' '}
          ou{' '}
          <Link to={routes.partner} className="text-snow underline underline-offset-4 hover:text-accent">
            cadastrar empresa
          </Link>
          .
        </p>
      </div>
    </Shell>
  )
}
