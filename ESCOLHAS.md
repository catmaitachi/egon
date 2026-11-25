### 2.1 - Gerenciamento centralizado das configurações do sistema
O sistema deverá disponibilizar um mecanismo centralizado para gerenciamento das configurações utilizadas por seus diferentes módulos.
Entre as configurações que poderão ser administradas est˜ao:
• Quantidade de moedas disponibilizadas aos professores a cada semestre (atualmente, 1.000 moedas).
• Prazo de validade dos cupons gerados ap´os o resgate de vantagens.
• Percentuais ou limites gerais de benef´ıcios permitidos pelo sistema.
• Parˆametros relacionados `as regras de utiliza¸c˜ao das moedas.

As configura¸c˜oes dever˜ao ser disponibilizadas por um componente compartilhado, garantindo que todos os m´odulos do sistema utilizem os mesmos valores. O componente dever´a disponibilizar opera¸c˜oes para consulta e atualiza¸c˜ao das configura¸c˜oes, respeitando as permiss˜oes estabelecidas pelosistema.

Desafio de projeto: como garantir que diferentes m´odulos compartilhem uma ´unica instˆancia respons´avel pelas configura¸c˜oes globais, evitando inconsistˆencias decorrentes da existˆencia de diferentes
instˆancias com valores distintos?

### 2.3 - Controle do ciclo de vida dos resgates
O sistema dever´a controlar as diferentes situa¸c˜oes de um resgate de vantagem realizado pelo aluno.
Um resgate poder´a passar por estados como:
• Solicitado: o aluno realizou a solicita¸c˜ao da vantagem.
• Aguardando utiliza¸c˜ao: o cupom foi gerado e enviado ao aluno.
• Confirmado: a empresa parceira confirmou a utiliza¸c˜ao do cupom.
• Cancelado: o resgate foi cancelado conforme as regras do sistema.
• Expirado: o prazo de utiliza¸c˜ao do cupom terminou.

As transi¸c˜oes entre os estados dever˜ao respeitar regras espec´ıficas. Por exemplo, um resgate confirmado n˜ao poder´a simplesmente retornar ao estado de solicitado. O sistema dever´a controlar as altera¸c˜oes de estado e executar as a¸c˜oes correspondentes a cada transi¸c˜ao.

Desafio de projeto: como organizar os comportamentos e as regras de transi¸c˜ao sem concentrar
diversas condi¸c˜oes em estruturas condicionais extensas?


### 2.5 Composição de vantagens e benefícios adicionais
O sistema dever´a permitir que determinadas vantagens ofere¸cam benef´ıcios adicionais que possam ser
combinados dinamicamente.
Exemplos:
• Uma vantagem que oferece desconto adicional em determinada categoria de produto.
• Uma vantagem que inclui um benef´ıcio de entrega gratuita.
• Uma vantagem que oferece moedas de volta ap´os sua utiliza¸c˜ao.
• Uma vantagem que concede um benef´ıcio adicional em datas comemorativas.

Os benef´ıcios adicionais dever˜ao poder ser combinados sem que seja necess´ario criar uma nova classe para cada combina¸c˜ao poss´ıvel. Cada benef´ıcio dever´a acrescentar uma caracter´ıstica ou comportamento `a vantagem original, preservando suas funcionalidades existentes.

Desafio de projeto: como adicionar responsabilidades e benef´ıcios a uma vantagem existente
sem modificar sua implementa¸c˜ao original ou criar diversas subclasses para representar todas as com-
bina¸c˜oes poss´ıveis?