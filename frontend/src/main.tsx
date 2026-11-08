import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Route, Routes } from 'react-router'
import { MotionConfig } from 'motion/react'
import '@fontsource/chakra-petch/300.css'
import '@fontsource/chakra-petch/400.css'
import '@fontsource/chakra-petch/500.css'
import '@fontsource/tomorrow/600.css'
import '@fontsource/tomorrow/700.css'
import '@fontsource/tomorrow/800.css'
import './index.css'
import App from './App.tsx'
import { routes } from './content'
import { RotaProtegida, SessaoProvider } from './components/Sessao'
import { Entrar } from './pages/Entrar'
import { Painel } from './pages/Painel'
import { CadastroAluno } from './pages/CadastroAluno'
import { CadastroEmpresa } from './pages/CadastroEmpresa'
import { Admins, Alunos, Cursos, Empresas, Instituicoes, Professores } from './pages/admin/Gestoes'
import { NaoEncontrada } from './pages/NaoEncontrada'

const admin = (pagina: React.ReactNode) => <RotaProtegida perfis={['ADMIN']}>{pagina}</RotaProtegida>

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <MotionConfig reducedMotion="user">
      <BrowserRouter>
        <SessaoProvider>
          <Routes>
            <Route path="/" element={<App />} />
            <Route path={routes.login} element={<Entrar />} />
            <Route path={routes.signup} element={<CadastroAluno />} />
            <Route path={routes.partner} element={<CadastroEmpresa />} />
            <Route
              path={routes.painel}
              element={
                <RotaProtegida>
                  <Painel />
                </RotaProtegida>
              }
            />
            <Route path={routes.admin.instituicoes} element={admin(<Instituicoes />)} />
            <Route path={routes.admin.cursos} element={admin(<Cursos />)} />
            <Route path={routes.admin.professores} element={admin(<Professores />)} />
            <Route path={routes.admin.alunos} element={admin(<Alunos />)} />
            <Route path={routes.admin.empresas} element={admin(<Empresas />)} />
            <Route path={routes.admin.admins} element={admin(<Admins />)} />
            <Route path="*" element={<NaoEncontrada />} />
          </Routes>
        </SessaoProvider>
      </BrowserRouter>
    </MotionConfig>
  </StrictMode>,
)
