# Histórias de Usuário

## 1. Atores

- **Aluno:** Realiza seu cadastro no sistema, recebe moedas de professores como reconhecimento de mérito, repassa parte delas a colegas (sob as regras do professor de origem), consulta seu saldo e extrato e troca suas moedas por vantagens de empresas parceiras.
- **Professor:** Docente cadastrado pelo admin e vinculado a uma instituição de ensino. Recebe uma cota semestral de moedas (acumulável), transfere moedas aos alunos com uma justificativa obrigatória e define as regras de repasse das moedas que deu.
- **Empresa Parceira:** Cadastra-se no sistema, cadastra vantagens com foto e custo em moedas, e confere o código de cupom no momento da troca presencial com o aluno.
- **Instituição de Ensino:** Fornece ao admin a lista de docentes e departamentos para pré-cadastro e atua como vínculo acadêmico para alunos e professores.
- **Admin:** Opera o sistema: cadastra instituições, cursos e professores (o pré-cadastro da HU04), gerencia alunos, empresas e outros admins e acompanha onde estão as moedas.
- **Sistema:** Processa transações, calcula acúmulo de cotas semestrais, gera códigos exclusivos de cupom e dispara avisos automáticos no app.

## 2. Especificação das Histórias de Usuário

### Módulo 1: Acesso e Cadastros

#### HU01 — Autenticação no Sistema
- **Declaração:**
  - **Eu como** usuário cadastrado (Aluno, Professor, Empresa Parceira ou Admin),
  - **quero** informar meu login e senha de acesso,
  - **para** entrar no sistema e acessar os recursos disponíveis para o meu perfil.
- **Cenário Explicativo:**
  - Qualquer usuário com conta ativa acessa a tela de autenticação e informa suas credenciais. Se o login e a senha estiverem corretos, o sistema valida a sessão e encaminha o usuário para sua tela inicial correspondente: o Aluno visualiza seu saldo de moedas e o catálogo de vantagens; o Professor visualiza seu saldo para distribuição e a opção de bonificar alunos; a Empresa acessa a gestão de suas vantagens ofertadas; o Admin vê onde estão as moedas e as áreas de cadastro.
  - Caso o usuário digite credenciais incorretas ou inexistentes, o sistema rejeita o acesso e exibe um aviso claro de credenciais inválidas, sem especificar se o erro foi no login ou na senha por segurança.

---

#### HU02 — Cadastro de Aluno
- **Declaração:**
  - **Eu como** aluno interessado no programa de mérito,
  - **quero** realizar meu cadastro informando meus dados pessoais e minha faculdade,
  - **para** poder receber moedas dos meus professores e resgatar vantagens cadastradas pelas empresas.
- **Cenário Explicativo:**
  - O aluno acessa a área de cadastro e preenche o formulário com: Nome completo, E-mail, CPF, RG e Endereço, além de definir seu Login e Senha. A Instituição de Ensino e o Curso são escolhidos em listas suspensas: a de instituições contém as faculdades já parceiras e pré-cadastradas no sistema.
  - O sistema confere os dígitos verificadores do CPF e verifica se o CPF, o e-mail ou o login informados já estão em uso. O login não pode repetir o de nenhum outro usuário, seja aluno, professor ou empresa, porque a tela de autenticação é uma só (HU01). Havendo duplicidade, o cadastro é recusado com aviso específico. Se todos os dados forem válidos, a conta é criada com saldo inicial de 0 (zero) moedas, permitindo que o aluno faça login imediatamente.

---

#### HU03 — Cadastro de Empresa Parceira
- **Declaração:**
  - **Eu como** representante de uma empresa parceira,
  - **quero** cadastrar minha organização no sistema com nossos dados comerciais,
  - **para** disponibilizar produtos, descontos e serviços em troca de moedas virtuais.
- **Cenário Explicativo:**
  - A empresa parceira preenche o cadastro fornecendo Nome da Empresa, CNPJ, E-mail corporativo de contato, Login e Senha de acesso.
  - O sistema valida o formato e os dígitos verificadores do CNPJ (numérico ou alfanumérico) e a sua unicidade no banco de dados. Caso o CNPJ já conste como cadastrado, o sistema impede a criação da conta e exibe a mensagem de erro correspondente. O e-mail e o login também não podem estar em uso, e o login não pode repetir o de nenhum outro usuário (HU01). Após a conclusão bem-sucedida, a empresa tem permissão para autenticar e começar a cadastrar as vantagens que deseja oferecer aos estudantes.

---

#### HU04 — Pré-cadastro de Professores por Instituição
- **Declaração:**
  - **Eu como** instituição de ensino conveniada,
  - **quero** que nossos professores sejam pré-cadastrados no sistema pelo admin, com seus departamentos,
  - **para** que eles tenham contas ativas vinculadas à nossa instituição sem precisarem passar por um autocadastro público.
- **Cenário Explicativo:**
  - No momento em que uma instituição de ensino firma parceria com o sistema, ela fornece ao admin a relação dos docentes autorizados a distribuir moedas, e o admin os cadastra. Cada registro armazena Nome do Professor, CPF, Departamento em que leciona e a Instituição à qual pertence, acompanhado de seu login e senha inicial.
  - O professor cadastrado no meio do semestre recebe a cota daquele semestre na hora (HU05).
  - O sistema garante que cada professor esteja explicitamente associado à sua instituição e com CPF único, habilitando-o a realizar login e a receber sua cota semestral de moedas.

---

### Módulo 2: Distribuição de Moedas e Reconhecimento

#### HU05 — Cota Semestral de Moedas dos Professores
- **Declaração:**
  - **Eu como** professor,
  - **quero** receber automaticamente 1.000 moedas a cada novo semestre letivo e acumular o saldo que não utilizei no período anterior,
  - **para** ter moedas disponíveis para recompensar meus alunos ao longo das aulas.
- **Cenário Explicativo:**
  - A cada virada de semestre letivo, o sistema credita 1.000 moedas na conta de cada professor cadastrado. Caso o professor não tenha distribuído todas as suas moedas no semestre anterior (por exemplo, se restaram 350 moedas), o saldo anterior não expira e não é zerado: as 1.000 novas moedas são somadas ao saldo remanescente, totalizando 1.350 moedas disponíveis.
  - Essa bonificação semestral é registrada no histórico da conta para fins de auditoria e prestação de contas.

---

#### HU06 — Reconhecimento de Aluno e Transferência de Moedas
- **Declaração:**
  - **Eu como** professor com saldo de moedas,
  - **quero** transferir uma quantidade de moedas para um aluno e escrever uma mensagem de justificativa,
  - **para** reconhecer publicamente seu bom comportamento, dedicação ou participação nas aulas.
- **Cenário Explicativo:**
  - O professor acessa a funcionalidade de distribuição de moedas, pesquisa e seleciona o aluno destinatário (pertencente à sua instituição), define a quantidade de moedas a ser transferida e redige obrigatoriamente um texto livre explicando o motivo do reconhecimento.
  - Antes de efetivar a transação, o sistema valida se o valor informado é positivo e se o professor possui saldo suficiente. Se o saldo for insuficiente, a transferência é bloqueada com mensagem informativa. Se a justificativa estiver em branco, o envio não é permitido. Havendo saldo e justificativa preenchida, o sistema debita as moedas do professor e credita na conta do aluno instantaneamente, gerando um registro com data, identificador único (UUID), valor e mensagem.

---

#### HU07 — Notificação de Moedas Recebidas no App
- **Declaração:**
  - **Eu como** aluno bonificado e professor que fez a transferência,
  - **quero** ser avisado no app, ao vivo, assim que a transferência for confirmada,
  - **para** que o aluno saiba na hora o valor recebido, o professor remetente e o motivo do reconhecimento, e o professor tenha a confirmação do envio.
- **Cenário Explicativo:**
  - Imediatamente após a confirmação de uma transferência de moedas feita pelo professor, o sistema avisa o aluno no app, sem que ele precise recarregar a página. O aviso contém o nome completo do professor, a quantidade exata de moedas recebidas, a data da transação e a íntegra da mensagem de justificativa escrita pelo professor. O saldo e o extrato do aluno se atualizam junto.
  - No mesmo momento, o professor recebe no app a confirmação do envio, com o nome do aluno, a quantidade de moedas, a data, a mensagem e o saldo que lhe resta.
  - Se o aluno não estiver com o app aberto, a transferência aparece no seu extrato (HU11) quando ele voltar.

---

#### HU13 — Repasse de Moedas entre Alunos
- **Declaração:**
  - **Eu como** aluno com moedas,
  - **quero** repassar parte das moedas que recebi de um professor a um colega, com uma justificativa,
  - **para** reconhecer a ajuda de um colega dentro das regras que o professor definiu.
- **Cenário Explicativo:**
  - Cada moeda guarda o professor de origem: o saldo do aluno é dividido em carteiras, uma por professor. O aluno escolhe de qual carteira sai o repasse, o colega (da instituição do professor de origem), o valor e a justificativa. No colega, as moedas continuam sendo daquele professor.
  - O professor define as regras das moedas dele: permitir ou não o repasse, exigir ou não a aprovação dele, valor máximo por repasse, limite por aluno no mês e se o colega precisa ser do mesmo curso. As regras valem no momento do pedido.
  - Sem aprovação, o repasse conclui na hora. Com aprovação, as moedas saem da carteira do aluno e ficam reservadas até o professor aprovar (vão para o colega) ou recusar (voltam); o aluno pode cancelar enquanto espera. Os envolvidos recebem os avisos no app, ao vivo.

---

### Módulo 3: Vantagens, Resgate e Troca

#### HU08 — Cadastro de Vantagens pela Empresa Parceira
- **Declaração:**
  - **Eu como** empresa parceira autenticada,
  - **quero** cadastrar ofertas de produtos, serviços e descontos informando descrição, foto e valor em moedas,
  - **para** que fiquem visíveis para os alunos no catálogo de benefícios.
- **Cenário Explicativo:**
  - A empresa acessa seu painel e cadastra uma nova vantagem (exemplos: desconto em restaurante universitário, desconto em mensalidade, materiais escolares, etc.). No cadastro, a empresa preenche o nome da vantagem, uma descrição detalhada do que está sendo oferecido, anexa uma foto do produto/benefício e define o custo em moedas virtuais.
  - O sistema valida se todos os campos obrigatórios estão preenchidos, se o custo em moedas é um valor inteiro maior que zero e se a foto é uma imagem JPEG, PNG ou WebP de até 2 MB (conferida pelo conteúdo do arquivo, não pelo nome). Ao salvar, a vantagem é publicada no catálogo de vantagens do sistema para consulta dos alunos.
  - A empresa também edita (sem foto nova, a atual continua) e exclui as próprias vantagens, e vê quantos alunos já têm saldo para cada uma.

---

#### HU09 — Consulta e Resgate de Vantagens
- **Declaração:**
  - **Eu como** aluno com saldo de moedas,
  - **quero** navegar pelo catálogo de vantagens e trocar minhas moedas por um item disponível,
  - **para** usufruir do produto ou desconto junto à empresa parceira.
- **Cenário Explicativo:**
  - O aluno acessa a área de vantagens do sistema, onde visualiza os cards de cada item contendo foto, descrição, empresa parceira e o custo em moedas. O sistema permite identificar quais vantagens estão ao alcance do saldo atual do aluno.
  - Ao escolher um item e clicar em resgatar, o sistema checa se o aluno possui saldo igual ou superior ao custo da vantagem. Se não possuir saldo suficiente, o resgate é cancelado com aviso na tela. Se tiver saldo suficiente, o sistema debita imediatamente as moedas da conta do aluno, cria um registro de troca e dispara a emissão do cupom.

---

#### HU10 — Emissão do Cupom de Troca e Aviso no App
- **Declaração:**
  - **Eu como** aluno e empresa parceira envolvidos no resgate de uma vantagem,
  - **quero** receber um aviso no app com os detalhes da troca e um código validador exclusivo gerado pelo sistema,
  - **para** que o aluno possa apresentar o cupom presencialmente e a empresa possa conferir e validar a troca.
- **Cenário Explicativo:**
  - No exato momento em que um resgate de vantagem é confirmado com sucesso, o sistema gera um código de cupom único (alfanumérico) e dispara simultaneamente dois avisos no app:
    1. **Aviso para o Aluno:** Contém o código validador, o nome da vantagem, o valor em moedas debitado e a identificação da empresa parceira, servindo como comprovante para uso presencial.
    2. **Aviso para a Empresa Parceira:** Contém o mesmo código validador, os dados do aluno beneficiado e a descrição da vantagem trocada.
  - No momento do atendimento presencial, o parceiro digita o código informado pelo aluno (ou lê o QR code do cupom, que abre a conferência com o código preenchido), vê quem resgatou o quê e confirma a troca antes de entregar o produto ou conceder o desconto. O aluno recebe o aviso de que o cupom foi usado. Um cupom já usado não pode ser confirmado de novo, e a empresa só confere cupons das próprias vantagens.
  - O código tem o formato `EGON-XXXX-XXXX`, sorteado de forma segura (impossível de adivinhar); a conferência aceita minúsculas, espaços e o código sem hífens.

---

### Módulo 4: Extratos e Acompanhamento

#### HU11 — Extrato de Moedas do Aluno
- **Declaração:**
  - **Eu como** aluno autenticado,
  - **quero** consultar meu saldo atualizado e o histórico de todas as transações da minha conta,
  - **para** acompanhar de quem recebi moedas, os motivos informados e quais vantagens já resgatei.
- **Cenário Explicativo:**
  - O aluno entra na seção de extrato de sua conta. No topo, o sistema mostra seu saldo total de moedas disponível no momento.
  - Logo abaixo, o extrato lista todas as transações, da mais recente para a mais antiga:
    - **Entradas:** Mostram a data da transação, o nome do professor que transferiu as moedas, a quantidade recebida e a justificativa redigida pelo professor.
    - **Resgates (HU09):** Mostram a data, a vantagem, a empresa, as moedas gastas e o código do cupom.
    - **Repasses (HU13):** Os enviados a colegas, com o status (esperando aprovação, concluído, recusado, cancelado) e o professor de origem; os recebidos de colegas, quando aprovados.
    - **Saídas (Trocas):** Mostram a data do resgate, o nome da vantagem, o parceiro ofertante, a quantidade de moedas gastas e o código do cupom gerado.

---

#### HU12 — Extrato de Distribuições do Professor
- **Declaração:**
  - **Eu como** professor autenticado,
  - **quero** consultar meu saldo disponível no semestre e o histórico das doações que já realizei,
  - **para** acompanhar quantas moedas ainda posso distribuir e verificar os reconhecimentos concedidos aos meus alunos.
- **Cenário Explicativo:**
  - O professor acessa a tela de extrato e visualiza o montante total de moedas que ainda possui para uso no semestre corrente (incluindo o saldo acumulado).
  - O sistema exibe o histórico detalhado de todas as transferências realizadas: data de cada envio, nome do aluno beneficiado, quantidade de moedas doadas e a justificativa registrada na ocasião.

---

#### HU14 — Administração do Sistema
- **Declaração:**
  - **Eu como** admin do Egon,
  - **quero** cadastrar instituições, cursos e professores, gerenciar alunos, empresas e admins e acompanhar onde estão as moedas,
  - **para** manter o sistema em ordem e saber se o reconhecimento está acontecendo.
- **Cenário Explicativo:**
  - O admin entra pelo mesmo login (HU01). No painel vê quantas moedas existem e onde estão (com professores, nas carteiras dos alunos e reservadas em repasses; a soma tem de bater com o que a cota emitiu), os envios dos últimos seis meses, os números por instituição e quantos cadastros existem.
  - Instituição com alunos ou professores, curso com alunos, professor que já enviou moedas e aluno com histórico não podem ser excluídos. Ninguém exclui a si mesmo nem o último admin.

---

## 3. Matriz de Rastreabilidade

Mapeamento entre as Histórias de Usuário, os casos de uso do [Diagrama de Casos de Uso](Casos%20de%20Uso.png) e as classes do [Diagrama de Classes](Diagrama%20de%20Classes.png). As tabelas correspondentes estão no [Modelo de Dados](../Sprint%202/Modelo%20de%20Dados.md).

| ID | Título da História | Ator Principal | Caso de Uso Vinculado | Classes UML Envolvidas |
| :---: | :--- | :--- | :--- | :--- |
| **HU01** | Autenticação no Sistema | Usuário (Todos) | Realizar login | `Usuario`, `Aluno`, `Professor`, `Empresa`, `Admin` |
| **HU02** | Cadastro de Aluno | Aluno | Realizar cadastro | `Aluno`, `Instituicao`, `Curso`, `Usuario` |
| **HU03** | Cadastro de Empresa | Empresa | Realizar cadastro de empresa | `Empresa`, `Usuario` |
| **HU04** | Pré-cadastro de Professores | Instituição / Admin | Sem caso de uso no diagrama: o admin cadastra a partir da lista da instituição | `Professor`, `Instituicao`, `Usuario` |
| **HU05** | Cota Semestral de Moedas | Sistema / Professor | Sem caso de uso: regra executada pelo sistema a cada semestre | `Professor`, `CotaSemestral` |
| **HU06** | Envio de Moedas por Mérito | Professor | Distribuir moedas | `Professor`, `Aluno`, `Transacao` |
| **HU07** | Notificação de Moedas Recebidas | Aluno e Professor | Distribuir moedas (aviso no app ao aluno e ao professor) | `Aluno`, `Professor`, `Transacao` |
| **HU08** | Cadastro de Vantagens | Empresa | Cadastrar vantagens | `Empresa`, `Vantagem` |
| **HU09** | Consulta e Resgate de Vantagens | Aluno | Trocar moedas | `Aluno`, `Vantagem`, `Troca` |
| **HU10** | Emissão e Aviso do Cupom | Aluno e Empresa | Trocar moedas (avisos no app com o cupom) | `Cupom`, `Troca`, `Notificacao`, `Empresa`, `Aluno` |
| **HU11** | Extrato do Aluno | Aluno | Visualizar saldo / Visualizar transações | `Aluno`, `Transacao`, `Troca` |
| **HU12** | Extrato do Professor | Professor | Visualizar saldo / Visualizar transações | `Professor`, `Transacao`, `CotaSemestral` |
| **HU13** | Repasse de Moedas entre Alunos | Aluno e Professor | Sem caso de uso no diagrama (pendente): repassar moedas / aprovar repasse | `Aluno`, `Professor`, `Carteira`, `Transferencia` |
| **HU14** | Administração do Sistema | Admin | Sem caso de uso no diagrama (pendente): cadastros e acompanhamento | `Admin`, `Instituicao`, `Curso`, `Professor`, `Aluno`, `Empresa` |
