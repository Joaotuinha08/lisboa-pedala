# 1) Escolha da abordagem 

Para identificar os componentes lógicos usou-se a abordagem **Actor/Action**. O kata tem 5 perfis de utilizador com ações muito diferentes e dois tipos de dispositivo (docas e bicicletas) que enviam informação ao sistema. Os requisitos não se organizam num único grande fluxo, a abordagem Workflow cobriria bem o fluxo desbloquear, devolver e cobrar, mas deixaria de fora R5, R6, R7 e C4. O sistema foi tratado como mais um ator. A decisão está tratada no [ADR-001.md](adr/ADR-001.md).

| **Ator**                       | **Ação**                                                   | **Origem**  |
| ------------------------------ | ---------------------------------------------------------- | ----------- |
| Utilizador (U1 e U2)           | Autenticar-se com a app ou cartão Navegante                | R1, U1 e U2 |
| Utilizador (U1 e U2)           | Desbloquear uma bicicleta                                  | R1          |
| Utilizador (U1 e U2)           | Devolver uma bicicleta numa doca livre                     | R2          |
| Utilizador (U2)                | Pagar a viagem                                             | R3          |
| Utilizador (U1 e U2)           | Ver o mapa de bicicletas e docas livres                    | R4          |
| Utilizador (U1 e U2)           | Reportar uma avaria                                        | R5          |
| Utilizador (U1 e U2)           | Consultar o histórico de viagens e as faturas              | R8          |
| Doca (externo e simulado)      | Confirmar o desbloqueio e o encaixe                        | R1 e R2     |
| Doca (externo e simulado)      | Reenviar eventos guardados                                 | C3a e C3b   |
| Bicicleta (externo e simulado) | Enviar telemetria                                          | C1b         |
| Bicicleta (externo e simulado) | Reportar uma avaria                                        | R5          |
| Técnico (U3)                   | Recebe ordem de manutenção                                 | R5          |
| Carrinha (U4)                  | Receber uma rota de redistribuição                         | R6          |
| Gestor Municipal (U5)          | Obter o relatório mensal                                   | R7          |
| Sistema                        | Calcular o valor de uma viagem                             | R3          |
| Sistema                        | Emitir fatura e comunicar à Autoridade Tributária          | C5a         |
| Sistema                        | Medir, hora a hora, quantas estações conseguem desbloquear | C4          |

___

# 2) Componente iniciais (v1)

Os componentes iniciais são um primeiro palpite, construído a partir das ações principais. Não se tentou acertar à primeira.

| **Componente (v1)** | **Ações que recebe**                                   |
| ------------------- | ------------------------------------------------------ |
| Viagem              | Desbloquear, devolver, confirmar desbloqueio e encaixe |
| Pagamentos          | Calcular o valor e pagar a viagem                      |
| Faturação           | Emitir a fatura e comunicá-la à Autoridade Tributária  |
| Contas              | Autenticar-se                                          |
| Mapa                | Ver o mapa                                             |
| Telemetria          | Enviar telemetria e a avaria reportada pela bicicleta  |
| Manutenção          | Receber a ordem e a avaria reportada pelo utilizador   |
| Redistribuição      | Receber a rota                                         |
| Relatórios          | Obter o relatório mensal                               |
| Histórico           | Consultar o histórico e as faturas                     |
Esta versão está registada no [ADR-002.md](ADR-002.md).

___

# 3) User stories e atribuição aos componentes (v1)

| **Código** | **User story**                                                                             | **Componente (v1)**            |
| ---------- | ------------------------------------------------------------------------------------------ | ------------------------------ |
| US1        | Como assinante, desbloqueio e devolvo uma bicicleta e a viagem fica registada e cobrada    | Viagem, Pagamentos e Faturação |
| US2        | Como técnico, recebo uma ordem de manutenção quando uma bicicleta reporta avaria           | Telemetria e Manutenção        |
| US3        | Como doca sem rede, reenvio os eventos guardados quando a rede volta, sem duplicar viagens | Sem componente (Viagem??)      |
| US4        | Como gestor municipal, obtenho o relatório de utilização por freguesia                     | Relatórios                     |
| US5        | Como utilizador ocasional, pago a viagem com MBWAY ou cartão                               | Pagamentos                     |
| US6        | Como utilizador, vejo no mapa as bicicletas e docas livres de cada estação em tempo real   | Mapa                           |
| US7        | Como utilizador, reporto uma avaria de uma bicicleta                                       | Manutenção                     |
| US8        | Como equipa de redistribuição, recebo uma rota sugerida para equilibrar as estações        | Redistribuição                 |
| US9        | Como utilizador, consulto o histórico de viagens e as faturas                              | Histórico                      |
| US10       | Como sistema, comunico cada fatura à Autoridade Tributária e guardo-a durante 10 anos      | Faturação                      |
| US11       | Como concessionária, sei em cada hora quantas estações conseguem desbloquear               | Sem componente                 |
| US12       | Como utilizador, identifico-me com a app ou cartão Navegante                               | Contas                         |

Possíveis problemas:
- US3 e US11 não têm componente. Não se encaixam em nenhum balde.
- US1 acaba por ficar no Viagem que já tem muitas responsabilidades diferentes.

___

# 4) Análise da coesão (v1)

Para cada componente escreveu-se a sua responsabilidade numa frase e aplicou-se o **teste das conjunções**, se a frase precisar de um "e" ou de um "também" o componente pode estar a fazer demasiado.

| **Componente (v1)** | **Responsabilidade**                                                                                                                           | **Resultado**                                                                      |
| ------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| Viagem              | Valida o utilizador **e** manda a doca libertar a bicicleta, também **recebe** os eventos das docas **e**, além disso, abre e fecha uma viagem | Falha. Junta quatro responsabilidades diferentes com razões diferentes para mudar. |
| Pagamentos          | Calcula o valor segundo o tarifário **e** cobra ao utilizador                                                                                  | Falha. As regras do tarifário e o meio de pagamento mudam por razões diferentes    |
| Telemetria          | Recebe o estado das bicicletas**,** guarda o percurso de cada viagem **e** deteta avarias                                                      | Falha. O percurso é dado pessoal e tem regras próprias                             |
| Restantes           | Uma responsabilidade cada                                                                                                                      | Passa                                                                              |

___

# 5) Reestruturação (v1 para v2)

Com base na coesão (secção 4), nas user stories sem componente (secção 3) e nas características do [ficheiro 01](docs/01-caracteristicas.md), fez-se uma iteração de reestruturação. A decisão está registada no [ADR-003.md](ADR-003.md), que substitui o no [ADR-002.md](ADR-002.md). A separação dos percursos tem um ADR próprio, o  [ADR-007.md](ADR-007.md).

| **Mudança**                                                                                                                                                                                                                     | **Motivo**                                                                                                                                             | **Características**               |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------ | --------------------------------- |
| Viagem dividido em **Desbloqueio** e **Registo de Viagens**                                                                                                                                                                     | Falhava o teste das conjunções e era uma entity trap. O desbloqueio é a operação medida pelo contrato e não pode depender do resto do ciclo da viagem. | Disponibilidade (top 3)           |
| Novo componente **Receção de Eventos das Docas**                                                                                                                                                                                | A US3 não tinha componente. É preciso um único ponto que descarte eventos repetidos e os ordene antes de chegarem às viagens.                          | Recuperabilidade (top 3)          |
| Pagamentos dividido em **Tarifação** e **Cobrança**                                                                                                                                                                             | Os tarifários de cada câmara têm de mudar isolados. O meio de pagamento não muda quando muda o tarifário.                                              | Extensibilidade (top 3)           |
| Telemetria dividido em **Telemetria** e **Percursos**                                                                                                                                                                           | O percurso é dado pessoal, com acesso e retenção próprios.                                                                                             | Privacidade e arquivabilidade     |
| Novo componente **Monitorização de Disponibilidade**                                                                                                                                                                            | A US11 não tinha componente. É preciso medir hora a hora as estações que desbloqueiam.                                                                 | Disponibilidade e suportabilidade |
| Mudança de nomes - Contas para **Identificação de Utilizadores**, Mapa para **Mapa de Ocupação**, Manutenção para **Ordens de Manutenção**, Histórico para **Consulta de Histórico**, Relatórios para **Relatórios Municipais** | Os nomes passam a dizer o que o componente faz e não a entidade de que trata.                                                                          | Evitar a entity trap              |

___

# 6) Componentes lógicos finais (v2)

Os domínios são provisórios e serão justificados no [ficheiro 03](docs/03-particionamento-e-vistas). O pacote de cada componente é `lisboa_pedala.<dominio>.<componente>`.

| **Componente**                   | **Pacote**                      | **Domínio** | **Responsabilidade**                                                               | **Coesão** |
| -------------------------------- | ------------------------------- | ----------- | ---------------------------------------------------------------------------------- | ---------- |
| Desbloqueio                      | `desbloqueio`                   | viagens     | Valida o pedido de desbloqueio e autoriza a doca a libertar a bicicleta.           | Funcional  |
| Receção de Eventos das Docas     | `rececao_eventos`               | viagens     | Recebe os eventos das docas, descarta os repetidos e entrega-os por ordem.         | Sequencial |
| Registo de Viagens               | `registo_viagens`               | viagens     | Mantém o ciclo de vida de cada viagem, da abertura ao fecho.                       | Funcional  |
| Percursos                        | `percursos`                     | viagens     | Guarda o trajeto de cada viagem como dado pessoal, com acesso e retenção próprios. | Funcional  |
| Tarifação                        | `tarifacao`                     | pagamentos  | Calcula o valor de uma viagem segundo o tarifário da câmara e o tipo de cliente.   | Funcional  |
| Cobrança                         | `cobranca`                      | pagamentos  | Cobra o valor ao utilizador pelo meio de pagamento (simulado).                     | Funcional  |
| Faturação                        | `faturacao`                     | pagamentos  | Emite faturas imutáveis, comunica-as à AT e conserva-as durante 10 anos.           | Sequencial |
| Identificação de Utilizadores    | `identificacao`                 | clientes    | Identifica o utilizador (app ou Navegante) e o seu tipo de cliente.                | Funcional  |
| Consulta de Histórico            | `consulta_historico`            | clientes    | Mostra ao utilizador as suas viagens e faturas.                                    | Funcional  |
| Mapa de Ocupação                 | `mapa_ocupacao`                 | estacoes    | Mantém, por estação, as bicicletas e as docas livres em cada momento.              | Funcional  |
| Monitorização de Disponibilidade | `monitorizacao_disponibilidade` | estacoes    | Mede, hora a hora, quantas estações conseguem desbloquear.                         | Funcional  |
| Redistribuição                   | `redistribuicao`                | estacoes    | Sugere rotas às carrinhas a partir da ocupação das estações.                       | Funcional  |
| Telemetria                       | `telemetria`                    | frota       | Recebe o estado das bicicletas elétricas e deteta avarias a partir dele.           | Sequencial |
| Ordens de Manutenção             | `ordens_manutencao`             | frota       | Abre ordens de manutenção e entrega-as aos técnicos.                               | Sequencial |
| Relatórios Municipais            | `relatorios_municipais`         | camara      | Produz o relatório mensal de utilização por freguesia e de CO2 evitado.            |            |

Sobre as conjunções que ficaram - em Receção de Eventos, Faturação, Telemetria e Ordens de Manutenção, o "e" liga passos em que a saída de um é a entrada do seguinte. Isto é a **coesão sequencial** e não justifica dividir o componente.

___

# 7) Mapeamento final user story > componente (v2)

| **User story** | **Componentes envolvidos**                                                                                                   | **Número** |
| -------------- | ---------------------------------------------------------------------------------------------------------------------------- | ---------- |
| US1            | Desbloqueio, Identificação de Utilizadores, Receção de Eventos das Docas, Registo de Viagens, Tarifação, Cobrança, Faturação | 7          |
| US2            | Telemetria, Ordens de Manutenção                                                                                             | 2          |
| US3            | Receção de Eventos das Docas, Registo de Viagens                                                                             | 2          |
| US4            | Relatórios Municipais, Registo de Viagens, Percursos                                                                         | 3          |
| US5            | Cobrança                                                                                                                     | 1          |
| US6            | Mapa de Ocupação                                                                                                             | 1          |
| US7            | Ordens de Manutenção                                                                                                         | 1          |
| US8            | Redistribuição, Mapa de Ocupação                                                                                             | 2          |
| US9            | Consulta de Histórico, Registo de Viagens, Faturação                                                                         | 3          |
| US10           | Faturação                                                                                                                    | 1          |
| US11           | Monitorização de Disponibilidade, Receção de Eventos das Docas                                                               | 2          |
| US12           | Identificação de Utilizadores                                                                                                | 1          |

A US1 envolve 7 componentes, o que cumpre o requisito do enunciado de pelo menos uma user story com 3 ou mais componentes.

___

# 8) Acoplamento aferente, eferente e instabilidade

### Dependências entre componentes

O diagrama de dependências da versão 2 está no [dependencias.componentes-v2.md](dependencias-componentes.md)

A tabela seguinte descreve cada dependência.

| **De**                | **Para**                         | **Porquê**                                        |
| --------------------- | -------------------------------- | ------------------------------------------------- |
| Desbloqueio           | Identificação de Utilizadores    | Validar o utilizador antes de autorizar           |
| Receção de Eventos    | Registo de Viagens               | Entregar desbloqueios e encaixes já sem repetidos |
| Receção de Eventos    | Mapa de Ocupação                 | Atualizar bicicletas e docas livres               |
| Receção de Eventos    | Monitorização de Disponibilidade | Registar o estado de cada estação                 |
| Registo de Viagens    | Identificação de Utilizadores    | Obter o tipo de cliente da viagem                 |
| Registo de Viagens    | Tarifação                        | Calcular o valor ao fechar a viagem               |
| Registo de Viagens    | Cobrança                         | Pedir a cobrança da viagem fechada                |
| Cobrança              | Faturação                        | Emitir a fatura depois da cobrança                |
| Telemetria            | Percursos                        | Enviar a posição de uma bicicleta em viagem       |
| Telemetria            | Ordens de Manutenção             | Abrir uma ordem quando deteta avaria              |
| Percursos             | Registo de Viagens               | Saber a que viagem pertence a posição             |
| Consulta de Histórico | Registo de Viagens               | Obter as viagens do utilizador                    |
| Consulta de Histórico | Faturação                        | Obter as faturas do utilizador                    |
| Redistribuição        | Mapa de Ocupação                 | Obter a ocupação atual das estações               |
| Relatórios Municipais | Registo de Viagens               | Obter as viagens do mês por freguesia             |
| Relatórios Municipais | Percursos                        | Obter a distância de cada viagem (para o CO2)     |

### Métricas por componente

- **Ca (aferente)** - quantos componentes dependem deste.
- **Ce (eferente)** - de quantos componentes este depende.
- **I (instabilidade) = Ce / (Ca + Ce)** - 0 é estável (muitos dependem dele), 1 é instável (depende de outros e ninguém depende dele).

| **Componente**                   | **Ca** | **Ce** | **I** |
| -------------------------------- | ------ | ------ | ----- |
| Desbloqueio                      | 0      | 1      | 1,00  |
| Receção de Eventos das Docas     | 0      | 3      | 1,00  |
| Registo de Viagens               | 4      | 3      | 0,43  |
| Percursos                        | 2      | 1      | 0,33  |
| Tarifação                        | 1      | 0      | 0     |
| Cobrança                         | 1      | 1      | 0,50  |
| Faturação                        | 2      | 0      | 0     |
| Identificação de Utilizadores    | 2      | 0      | 0     |
| Consulta de Histórico            | 0      | 2      | 1,00  |
| Mapa de Ocupação                 | 2      | 0      | 0     |
| Monitorização de Disponibilidade | 1      | 0      | 0,00  |
| Redistribuição                   | 0      | 1      | 1,00  |
| Telemetria                       | 0      | 2      | 1,00  |
| Ordens de Manutenção             | 1      | 0      | 0     |
| Relatórios Municipais            | 0      | 2      | 1,00  |

### Análise

- **Registo de Viagens** é o componente com maior Ca (4). Uma mudança na sua interface afeta 4 componentes, por isso a interface tem de ser estável.
- **Tarifação** é estável (I = 0), mas é o componente que mais vai mudar, por causa da extensibilidade (C7). Para conciliar as duas coisas, a interface "calcular o valor de uma viagem" fica fixa e cada tarifário é uma implementação nova atrás dessa interface (ADR-006).
- Os componentes com I = 1 (Relatórios, Consulta de Histórico, Redistribuição, Telemetria, Receção de Eventos, Desbloqueio) podem mudar sem afetar nenhum outro.
- **Desbloqueio depende de Identificação de Utilizadores.** Se a identificação falhar, o desbloqueio falha, o que é um risco para a disponibilidade (C4). Este risco é tratado no [ADR-004.md](docs/adr/ADR-004.md).

___

# 9) Conascência

Dois componentes são conascentes quando uma mudança num obriga a mudar o outro. Analisaram-se dois pares.

### Receção de Eventos das Docas e Registo de Viagens

| **Tipo**               | **Onde aparece**                                                                                               | **Decisão**                                                                 |
| ---------------------- | -------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------- |
| Nome e tipo (estática) | Ambos conhecem os campos do evento - doca, bicicleta, tipo, hora, identificador.                               | Aceitável.                                                                  |
| Significado (estática) | O tipo de evento é um texto ("DESBLOQUEIO", "ENCAIXE") que os dois têm de interpretar igual.                   | Enfraquecer para conascência de nome com um tipo enumerado partilhado.      |
| Identidade (dinâmica)  | Os dois usam o identificador do evento gerado pela doca para garantir que cada evento é processado uma só vez. | Necessária para a recuperabilidade. Fica limitada a estes dois componentes. |
| Execução (dinâmica)    | O desbloqueio tem de ser processado antes do encaixe da mesma viagem.                                          | Ver acoplamento temporal (secção 10).                                       |

### Registo de Viagens e Tarifação

| **Tipo**             | **Onde aparece**                                                                                                         | **Decisão**                                                                                      |
| -------------------- | ------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------ |
| Posição (estática)   | Uma chamada como `calcular(inicio, fim, tipo, camara)` obriga a respeitar a ordem dos argumentos.                        | Enfraquecer para conascência de nome - passar um objeto com campos nomeados (dados da viagem).   |
| Algoritmo (estática) | Se o Registo de Viagens também arredondasse a duração em blocos de 15 minutos, os dois teriam de usar o mesmo algoritmo. | Evitar - só a Tarifação calcula blocos. O Registo envia apenas a hora de início e a hora de fim. |

___

# 10) Acoplamento temporal

### Exemplo 1 - ordem obrigatória das operações

Desbloqueio, encaixe, fecho da viagem, cálculo do valor, pedido de cobrança, emissão da fatura e comunicação à Autoridade Tributária. Cada passo só pode acontecer depois do anterior. Como os eventos de uma doca sem rede podem chegar fora de ordem (C3b), a Receção de Eventos ordena-os pela hora registada na doca antes de os entregar ao Registo de Viagens.

### Exemplo 2 - unidade de trabalho

Marcar o evento de encaixe como processado, fechar a viagem com o valor calculado e registar o pedido de cobrança têm de acontecer **numa só unidade de trabalho**. Se o sistema falhar a meio, um reenvio do mesmo evento pela doca voltaria a fechar e a cobrar a viagem (cobrança duplicada) ou deixaria a viagem fechada sem cobrança. A decisão está registada no [ADR-005.md](docs/adr/ADR-005.md).

### Desacoplamento intencional

O desbloqueio **não** espera pela cobrança. Isto corresponde ao trade-off entre disponibilidade e cobrança imediata do ficheiro 01 - a cobrança acontece depois do fecho da viagem e nunca no caminho do desbloqueio ([ADR-004.md](docs/adr/ADR-004.md)).

---

# 11) Lei de Deméter

Um componente deve ter conhecimento limitado dos outros.

- **Antes** - os Relatórios Municipais liam o trajeto completo de cada viagem a partir de Percursos e calculavam eles próprios a distância para o CO₂. Conheciam a estrutura interna de Percursos e tinham acesso a dados pessoais de que não precisavam.
- **Depois** - Percursos expõe apenas a distância de cada viagem. Os Relatórios Municipais deixam de conhecer o trajeto.

Os Relatórios ficam menos acoplados e Percursos ganha uma responsabilidade. A Lei de Deméter redistribui o acoplamento em vez de o eliminar. A mudança também reforça a privacidade (C5b), porque o trajeto deixa de sair do componente Percursos ([ADR-007.md](docs/adr/ADR-007.md)).