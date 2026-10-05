# Histórias de Usuário

## 1. Atores

- **Aluno:** Realiza seu cadastro no sistema, recebe moedas de professores como reconhecimento de mérito, consulta seu saldo e extrato e troca suas moedas por vantagens de empresas parceiras.
- **Professor:** Docente pré-cadastrado vinculado a uma instituição de ensino. Recebe uma cota semestral de moedas (acumulável) e transfere moedas aos alunos com uma justificativa obrigatória.
- **Empresa Parceira:** Cadastra-se no sistema, cadastra vantagens com foto e custo em moedas, e confere o código de cupom no momento da troca presencial com o aluno.
- **Instituição de Ensino:** Fornece a lista de docentes e departamentos para pré-cadastro e atua como vínculo acadêmico para alunos e professores.
- **Sistema:** Processa transações, calcula acúmulo de cotas semestrais, gera códigos exclusivos de cupom e dispara notificações automáticas por e-mail.

## 2. Especificação das Histórias de Usuário

### Módulo 1: Acesso e Cadastros

#### HU01 — Autenticação no Sistema
- **Declaração:**
  - **Eu como** usuário cadastrado (Aluno, Professor ou Empresa Parceira),
  - **quero** informar meu login e senha de acesso,
  - **para** entrar no sistema e acessar os recursos disponíveis para o meu perfil.
- **Cenário Explicativo:**
  - Qualquer usuário com conta ativa acessa a tela de autenticação e informa suas credenciais. Se o login e a senha estiverem corretos, o sistema valida a sessão e encaminha o usuário para sua tela inicial correspondente: o Aluno visualiza seu saldo de moedas e o catálogo de vantagens; o Professor visualiza seu saldo para distribuição e a opção de bonificar alunos; a Empresa acessa a gestão de suas vantagens ofertadas.
  - Caso o usuário digite credenciais incorretas ou inexistentes, o sistema rejeita o acesso e exibe um aviso claro de credenciais inválidas, sem especificar se o erro foi no login ou na senha por segurança.

---

#### HU02 — Cadastro de Aluno
- **Declaração:**
  - **Eu como** aluno interessado no programa de mérito,
  - **quero** realizar meu cadastro informando meus dados pessoais e minha faculdade,
  - **para** poder receber moedas dos meus professores e resgatar vantagens cadastradas pelas empresas.
- **Cenário Explicativo:**
  - O aluno acessa a área de cadastro e preenche o formulário com: Nome completo, E-mail, CPF, RG, Endereço, Curso, além de definir seu Login e Senha. Para o campo de Instituição de Ensino, o aluno deve selecionar uma faculdade a partir de uma lista suspensa contendo as instituições já parceiras e pré-cadastradas no sistema.
  - O sistema verifica se o CPF ou o e-mail informados já estão em uso. Havendo duplicidade, o cadastro é recusado com aviso específico. Se todos os dados forem válidos, a conta é criada com saldo inicial de 0 (zero) moedas, permitindo que o aluno faça login imediatamente.

---

#### HU03 — Cadastro de Empresa Parceira
- **Declaração:**
  - **Eu como** representante de uma empresa parceira,
  - **quero** cadastrar minha organização no sistema com nossos dados comerciais,
  - **para** disponibilizar produtos, descontos e serviços em troca de moedas virtuais.
- **Cenário Explicativo:**
  - A empresa parceira preenche o cadastro fornecendo Nome da Empresa, CNPJ, E-mail corporativo de contato, Login e Senha de acesso.
  - O sistema valida o formato e a unicidade do CNPJ no banco de dados. Caso o CNPJ já conste como cadastrado, o sistema impede a criação da conta e exibe a mensagem de erro correspondente. Após a conclusão bem-sucedida, a empresa tem permissão para autenticar e começar a cadastrar as vantagens que deseja oferecer aos estudantes.

---

#### HU04 — Pré-cadastro de Professores por Instituição
- **Declaração:**
  - **Eu como** instituição de ensino conveniada,
  - **quero** que nossos professores sejam pré-cadastrados no sistema com seus departamentos,
  - **para** que eles tenham contas ativas vinculadas à nossa instituição sem precisarem passar por um autocadastro público.
- **Cenário Explicativo:**
  - No momento em que uma instituição de ensino firma parceria com o sistema, ela fornece a relação dos docentes autorizados a distribuir moedas. Cada registro armazena Nome do Professor, CPF, Departamento em que leciona e a Instituição à qual pertence, acompanhado de seu login e senha inicial.
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

#### HU07 — Notificação de Moedas Recebidas por E-mail
- **Declaração:**
  - **Eu como** aluno bonificado,
  - **quero** receber um e-mail do sistema assim que um professor me transferir moedas,
  - **para** ser informado na hora sobre o valor recebido, o professor remetente e o motivo do reconhecimento.
- **Cenário Explicativo:**
  - Imediatamente após a confirmação de uma transferência de moedas feita pelo professor, o sistema envia automaticamente um e-mail para o endereço cadastrado do aluno.
  - O e-mail contém o nome completo do professor, a quantidade exata de moedas recebidas, a data da transação e a íntegra da mensagem de justificativa escrita pelo professor.

---

### Módulo 3: Vantagens, Resgate e Troca

#### HU08 — Cadastro de Vantagens pela Empresa Parceira
- **Declaração:**
  - **Eu como** empresa parceira autenticada,
  - **quero** cadastrar ofertas de produtos, serviços e descontos informando descrição, foto e valor em moedas,
  - **para** que fiquem visíveis para os alunos no catálogo de benefícios.
- **Cenário Explicativo:**
  - A empresa acessa seu painel e cadastra uma nova vantagem (exemplos: desconto em restaurante universitário, desconto em mensalidade, materiais escolares, etc.). No cadastro, a empresa preenche o nome da vantagem, uma descrição detalhada do que está sendo oferecido, anexa uma foto do produto/benefício e define o custo em moedas virtuais.
  - O sistema valida se todos os campos obrigatórios estão preenchidos e se o custo em moedas é um valor inteiro maior que zero. Ao salvar, a vantagem é publicada no catálogo de vantagens do sistema para consulta dos alunos.

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

#### HU10 — Emissão e Envio do Cupom de Troca por E-mail
- **Declaração:**
  - **Eu como** aluno e empresa parceira envolvidos no resgate de uma vantagem,
  - **quero** receber um e-mail com os detalhes da troca e um código validador exclusivo gerado pelo sistema,
  - **para** que o aluno possa apresentar o cupom presencialmente e a empresa possa conferir e validar a troca.
- **Cenário Explicativo:**
  - No exato momento em que um resgate de vantagem é confirmado com sucesso, o sistema gera um código de cupom único (alfanumérico) e dispara simultaneamente dois e-mails:
    1. **E-mail para o Aluno:** Contém o código validador, o nome da vantagem, o valor em moedas debitado e a identificação da empresa parceira, servindo como comprovante para uso presencial.
    2. **E-mail para a Empresa Parceira:** Contém o mesmo código validador, os dados do aluno beneficiado e a descrição da vantagem trocada.
  - No momento do atendimento presencial, o parceiro confere o código informado pelo aluno com o código recebido em seu e-mail, garantindo a autenticidade da troca antes de entregar o produto ou conceder o desconto.

---

### Módulo 4: Extratos e Acompanhamento

#### HU11 — Extrato de Moedas do Aluno
- **Declaração:**
  - **Eu como** aluno autenticado,
  - **quero** consultar meu saldo atualizado e o histórico de todas as transações da minha conta,
  - **para** acompanhar de quem recebi moedas, os motivos informados e quais vantagens já resgatei.
- **Cenário Explicativo:**
  - O aluno entra na seção de extrato de sua conta. No topo, o sistema mostra seu saldo total de moedas disponível no momento.
  - Logo abaixo, o extrato lista todas as transações em ordem cronológica:
    - **Entradas:** Mostram a data da transação, o nome do professor que transferiu as moedas, a quantidade recebida e a justificativa redigida pelo professor.
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

## 4. Matriz de Rastreabilidade

Mapeamento entre as Histórias de Usuário, os casos de uso do [Diagrama de Casos de Uso](file:///home/luuspz/Wonderland/Projetos/egon/docs/Casos%20de%20Uso.png) e as entidades do [Diagrama de Classes](file:///home/luuspz/Wonderland/Projetos/egon/docs/Diagrama%20de%20Classe.png):

| ID | Título da História | Ator Principal | Caso de Uso Vinculado | Classes UML Envolvidas |
| :---: | :--- | :--- | :--- | :--- |
| **HU01** | Autenticação no Sistema | Usuário (Todos) | Realizar login | `Usuario`, `Aluno`, `Professor`, `Empresa` |
| **HU02** | Cadastro de Aluno | Aluno | Realizar cadastro | `Aluno`, `Instituição`, `Usuario` |
| **HU03** | Cadastro de Empresa | Empresa | Realizar cadstro de empresa | `Empresa`, `Usuario` |
| **HU04** | Pré-cadastro de Professores | Instituição / Sistema | Pré-cadastro (Admin) | `Professor`, `Instituição`, `Usuario` |
| **HU05** | Cota Semestral de Moedas | Sistema / Professor | Carga Semestral (Regra) | `Professor`, `Transação` |
| **HU06** | Envio de Moedas por Mérito | Professor | Distribuir moedas | `Professor`, `Aluno`, `Transação` |
| **HU07** | Notificação de Moedas Recebidas | Aluno | Distribuir moedas (Email) | `Notificação`, `Aluno`, `Transação` |
| **HU08** | Cadastro de Vantagens | Empresa | Cadastrar vantagens | `Empresa`, `Vantagens` |
| **HU09** | Consulta e Resgate de Vantagens | Aluno | Trocar moedas | `Aluno`, `Vantagens`, `Troca` |
| **HU10** | Emissão e Envio do Cupom | Aluno e Empresa | Trocar moedas (Email) | `Cupom`, `Troca`, `Notificação`, `Empresa`, `Aluno` |
| **HU11** | Extrato do Aluno | Aluno | Visualizar saldo / Visualizar transações | `Aluno`, `Transação`, `Troca` |
| **HU12** | Extrato do Professor | Professor | Visualizar saldo / Visualizar transações | `Professor`, `Transação` |
