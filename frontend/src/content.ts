import {
  BarcodeIcon,
  BellRingingIcon,
  BookOpenTextIcon,
  CalendarCheckIcon,
  ChalkboardTeacherIcon,
  ChatTextIcon,
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
import type { Cupom, EventoTransferencia, Transacao, Transferencia } from './lib/api'
import { dataHora } from './lib/format'

// Nome do site e da moeda. Trocar aqui muda a página inteira.
export const brand = {
  name: 'Egon',
  coin: 'Ego',
}

const count = new Intl.NumberFormat('pt-BR')

// Nome da moeda concordando com a quantia: "Ego" ou "Egos". Para quando o número aparece separado.
export const coinName = (n: number) => `${brand.coin}${n === 1 ? '' : 's'}`

// Quantia na moeda do site: "1 Ego", "0 Egos", "1.350 Egos".
export const coins = (n: number) => `${count.format(n)} ${coinName(n)}`

// Rotas das telas. "Entrar" escolhe o perfil sem senha (o login da HU01 ficou fora do escopo).
export const routes = {
  signup: '/cadastro',
  login: '/entrar',
  partner: '/empresa/cadastro',
  // O dashboard de quem está logado: um por perfil, na mesma rota.
  painel: '/painel',
  admin: {
    instituicoes: '/admin/instituicoes',
    cursos: '/admin/cursos',
    professores: '/admin/professores',
    alunos: '/admin/alunos',
    empresas: '/admin/empresas',
    admins: '/admin/admins',
  },
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
    icon: BellRingingIcon,
    title: 'Você recebe',
    text: 'O saldo cai na hora e o aviso aparece no app.',
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
      { icon: BellRingingIcon, text: 'Aviso por e-mail e no app' },
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
      { icon: BellRingingIcon, text: 'Aviso de cada resgate' },
    ],
    action: { label: 'Cadastrar empresa', href: routes.partner },
  },
]

export const closing = {
  title: 'Sua próxima aula pode valer',
  highlight: 'Egos.',
}

// Aviso ao vivo no app (HU07, junto com o e-mail que o back-end manda). Montado aqui porque o nome da moeda só existe neste arquivo.
export type LiveNotice = { title: string; text: string; message: string }

type Delivered = { transacao: Transacao; saldo: number }
type Repasse = { evento: EventoTransferencia; transferencia: Transferencia; saldo: number }

export const notifications = {
  // Para o aluno: quem enviou, quanto, quando e a mensagem completa.
  aluno: ({ transacao: t }: Delivered): LiveNotice => ({
    title: `Você recebeu ${coins(t.valor)}`,
    text: `De ${t.professor.nome}, em ${dataHora(t.data)}.`,
    message: t.motivo,
  }),
  // Para o professor: para quem, quanto, quando, a mensagem e o saldo que sobrou.
  professor: ({ transacao: t, saldo }: Delivered): LiveNotice => ({
    title: 'Envio confirmado',
    text: `${coins(t.valor)} para ${t.aluno.nome}, em ${dataHora(t.data)}. Saldo restante: ${coins(saldo)}.`,
    message: t.motivo,
  }),
  // Repasse entre alunos, visto por quem pediu (`souRemetente`) ou por quem recebeu.
  repasseAluno: ({ evento, transferencia: t, saldo }: Repasse, souRemetente: boolean): LiveNotice => {
    const origem = `Egos de ${t.professor.nome}`
    if (!souRemetente) {
      return {
        title: `Você recebeu ${coins(t.valor)}`,
        text: `De ${t.remetente.nome}, ${origem}. Seu saldo: ${coins(saldo)}.`,
        message: t.motivo,
      }
    }
    const titulos: Record<EventoTransferencia, string> = {
      SOLICITADA: 'Repasse enviado para aprovação',
      APROVADA: 'Repasse concluído',
      RECUSADA: 'Repasse recusado',
      CANCELADA: 'Repasse cancelado',
    }
    const textos: Record<EventoTransferencia, string> = {
      SOLICITADA: `${coins(t.valor)} para ${t.destinatario.nome} esperam ${t.professor.nome} aprovar. Ficam reservados até lá.`,
      APROVADA: `${coins(t.valor)} foram para ${t.destinatario.nome}. Seu saldo: ${coins(saldo)}.`,
      RECUSADA: `${t.professor.nome} recusou o repasse para ${t.destinatario.nome}. Os ${coinName(2)} voltaram: seu saldo é ${coins(saldo)}.`,
      CANCELADA: `Os ${coinName(2)} voltaram para você. Seu saldo: ${coins(saldo)}.`,
    }
    return { title: titulos[evento], text: textos[evento], message: t.motivo }
  },
  // Cupom (HU10): para o aluno, emitido no resgate e usado na loja; para a empresa, um resgate novo.
  cupomAluno: ({ evento, cupom: c, saldo }: { evento: 'EMITIDO' | 'USADO'; cupom: Cupom; saldo: number }): LiveNotice =>
    evento === 'EMITIDO'
      ? { title: 'Cupom gerado', text: `${c.vantagem.nome}, na ${c.empresa.nome}, por ${coins(c.valor)}. Seu saldo: ${coins(saldo)}.`, message: c.codigo }
      : { title: 'Cupom usado', text: `${c.empresa.nome} confirmou ${c.vantagem.nome} em ${dataHora(c.dataUso ?? c.data)}.`, message: c.codigo },
  cupomEmpresa: ({ cupom: c }: { cupom: Cupom }): LiveNotice => ({
    title: 'Novo resgate',
    text: `${c.aluno.nome} resgatou ${c.vantagem.nome}. Confira o código quando o aluno chegar.`,
    message: c.codigo,
  }),
  // Para o professor de origem: um pedido novo para decidir, ou um pedido que o aluno cancelou.
  repasseProfessor: ({ evento, transferencia: t }: Repasse): LiveNotice => ({
    title: evento === 'CANCELADA' ? 'Pedido de repasse cancelado' : 'Pedido de repasse',
    text:
      evento === 'CANCELADA'
        ? `${t.remetente.nome} desistiu de repassar ${coins(t.valor)} para ${t.destinatario.nome}.`
        : `${t.remetente.nome} quer repassar ${coins(t.valor)} dos seus ${coinName(2)} para ${t.destinatario.nome}.`,
    message: t.motivo,
  }),
}
