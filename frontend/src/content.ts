import {
  BarcodeIcon,
  BookOpenTextIcon,
  CalendarCheckIcon,
  ChalkboardTeacherIcon,
  ChatTextIcon,
  EnvelopeSimpleIcon,
  FilmSlateIcon,
  ForkKnifeIcon,
  GiftIcon,
  ImagesIcon,
  NotePencilIcon,
  PathIcon,
  StorefrontIcon,
  StudentIcon,
  TicketIcon,
  TranslateIcon,
  UserPlusIcon,
  UsersThreeIcon,
  WalletIcon,
  type Icon,
} from '@phosphor-icons/react'

// Nome do site e da moeda. Trocar aqui muda a página inteira.
export const brand = {
  name: 'Egon',
  coin: 'Ego',
}

// Rotas das telas. O login ainda não existe; a landing já aponta para ele.
export const routes = {
  signup: '/cadastro',
  login: '/entrar',
  partner: '/empresa/cadastro',
  students: '/alunos',
  partners: '/empresas',
}

export type SectionId = 'como-funciona' | 'vantagens' | 'quem-participa' | 'criar-conta'

// Sem cabeçalho: cada seção tem seu letreiro, que serve de link no hero e de saída na seção anterior.
export const sections: { id: SectionId; label: string; icon: Icon }[] = [
  { id: 'como-funciona', label: 'Como funciona', icon: PathIcon },
  { id: 'vantagens', label: 'Vantagens', icon: GiftIcon },
  { id: 'quem-participa', label: 'Quem participa', icon: UsersThreeIcon },
  { id: 'criar-conta', label: 'Criar conta', icon: UserPlusIcon },
]

export const hero = {
  title: ['Seu esforço em aula', 'vira'],
  highlight: 'Ego.',
  lead: 'Professores reconhecem sua dedicação com Egos. Você troca por descontos e benefícios de empresas parceiras.',
}

export type Step = { icon: Icon; title: string; text: string }

export const steps: Step[] = [
  {
    icon: ChalkboardTeacherIcon,
    title: 'O professor reconhece',
    text: 'Participou, ajudou um colega, mandou bem na prova. Os Egos vêm com uma mensagem dele.',
  },
  {
    icon: EnvelopeSimpleIcon,
    title: 'Você recebe',
    text: 'O saldo cai na hora e o aviso chega no seu e-mail.',
  },
  {
    icon: TicketIcon,
    title: 'Você troca',
    text: 'Escolha uma vantagem e receba um cupom para apresentar na loja.',
  },
]

export type Perk = { icon: Icon; name: string; partner: string; cost: number }

// Exemplos ilustrativos. Não são parceiros nem preços reais.
export const perks: Perk[] = [
  { icon: ForkKnifeIcon, name: 'Almoço com desconto', partner: 'Restaurante universitário', cost: 120 },
  { icon: NotePencilIcon, name: 'Material de estudo', partner: 'Papelaria', cost: 80 },
  { icon: FilmSlateIcon, name: 'Ingresso de cinema', partner: 'Cinema', cost: 150 },
  { icon: BookOpenTextIcon, name: 'Livro da bibliografia', partner: 'Livraria', cost: 300 },
  { icon: TranslateIcon, name: 'Mês de curso de idiomas', partner: 'Escola de idiomas', cost: 400 },
]

export type RoleId = 'aluno' | 'professor' | 'empresa'

export type Role = {
  id: RoleId
  label: string
  icon: Icon
  line: string
  points: { icon: Icon; text: string }[]
  action: { label: string; href: string }
}

export const roles: Role[] = [
  {
    id: 'aluno',
    label: 'Aluno',
    icon: StudentIcon,
    line: 'Crie sua conta, acompanhe o saldo e troque quando quiser.',
    points: [
      { icon: WalletIcon, text: 'Saldo e extrato' },
      { icon: EnvelopeSimpleIcon, text: 'Aviso por e-mail' },
      { icon: TicketIcon, text: 'Cupom na hora' },
    ],
    action: { label: 'Criar conta', href: routes.signup },
  },
  {
    id: 'professor',
    label: 'Professor',
    icon: ChalkboardTeacherIcon,
    line: 'Você recebe 1.000 Egos por semestre. O que sobra acumula para o próximo.',
    points: [
      { icon: CalendarCheckIcon, text: 'Cota semestral' },
      { icon: ChatTextIcon, text: 'Mensagem por envio' },
      { icon: WalletIcon, text: 'Histórico completo' },
    ],
    action: { label: 'Entrar', href: routes.login },
  },
  {
    id: 'empresa',
    label: 'Empresa',
    icon: StorefrontIcon,
    line: 'Publique vantagens com foto e preço em Egos. Confira o cupom na troca.',
    points: [
      { icon: ImagesIcon, text: 'Catálogo próprio' },
      { icon: BarcodeIcon, text: 'Código de conferência' },
      { icon: EnvelopeSimpleIcon, text: 'Aviso de cada resgate' },
    ],
    action: { label: 'Cadastrar empresa', href: routes.partner },
  },
]

export const closing = {
  title: 'Sua próxima aula pode valer',
  highlight: 'Egos.',
}
