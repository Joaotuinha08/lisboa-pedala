# ARCHITECTURE.md - Lisboa Pedala (especificação para IA)

> **Ler antes de gerar qualquer código.** Cada pedido de geração de um componente tem de referir este ficheiro e os ADRs desse componente (coluna ADR, §1). Se o pedido entrar em conflito com este ficheiro, ou algo não estiver aqui definido, **pára e pergunta**. As justificações estão em `docs/` e `docs/adr/`.

**Stack e implantação (ADR-009).** Java 21. Uma só aplicação (uma JVM) com API REST arrancada em `lisboa_pedala.Main`. Uma só base SQLite, acedida por JDBC. Docas, bicicletas e CLI da app são simuladores no pacote `simuladores`, fora de `lisboa_pedala`, e só falam com a plataforma pela API REST. Pagamentos e AT são adaptadores simulados dentro de Cobrança e Faturação.

**Particionamento de topo (ADR-008).** Por domínio, com 6 domínios: `viagens`, `pagamentos`, `clientes`, `estacoes`, `frota`, `camara`. A divisão técnica (interface, lógica, persistência) fica dentro de cada componente.

---

## 1. Componentes, dependências permitidas e ADRs

Pacote de cada componente: `lisboa_pedala.<dominio>.<componente>`. A coluna "Pode usar" é uma **lista fechada**: tudo o que não está lá é proibido.

ADRs transversais a todos os componentes: ADR-001 (Actor/Action), ADR-003 (componentes v2), ADR-008 (particionamento), ADR-009 (implantação).

| Componente | Pacote (sob `lisboa_pedala.`) | Responsabilidade | Pode usar | ADR |
|---|---|---|---|---|
| Desbloqueio | `viagens.desbloqueio` | Valida o pedido de desbloqueio e autoriza a doca a libertar a bicicleta. | `identificacao` | 004 |
| Receção de Eventos das Docas | `viagens.rececao_eventos` | Recebe os eventos das docas, descarta os repetidos e entrega-os por ordem. | `registo_viagens`, `mapa_ocupacao`, `monitorizacao_disponibilidade` | 005 |
| Registo de Viagens | `viagens.registo_viagens` | Mantém o ciclo de vida de cada viagem, da abertura ao fecho. | `identificacao`, `tarifacao`, `cobranca` | 005, 006 |
| Percursos | `viagens.percursos` | Guarda o trajeto de cada viagem como dado pessoal, com acesso e retenção próprios. | `registo_viagens` | 007 |
| Tarifação | `pagamentos.tarifacao` | Calcula o valor de uma viagem segundo o tarifário da câmara e o tipo de cliente. | nenhum | 006 |
| Cobrança | `pagamentos.cobranca` | Cobra o valor ao utilizador pelo meio de pagamento (simulado). | `faturacao` | 004, 005 |
| Faturação | `pagamentos.faturacao` | Emite faturas imutáveis, comunica-as à AT e conserva-as durante 10 anos. | nenhum | 004 |
| Identificação de Utilizadores | `clientes.identificacao` | Identifica o utilizador (app ou Navegante) e o seu tipo de cliente. | nenhum | 004 |
| Consulta de Histórico | `clientes.consulta_historico` | Mostra ao utilizador as suas viagens e faturas. | `registo_viagens`, `faturacao` | 007 |
| Mapa de Ocupação | `estacoes.mapa_ocupacao` | Mantém, por estação, as bicicletas e as docas livres em cada momento. | nenhum | - |
| Monitorização de Disponibilidade | `estacoes.monitorizacao_disponibilidade` | Mede, hora a hora, quantas estações conseguem desbloquear. | nenhum | - |
| Redistribuição | `estacoes.redistribuicao` | Sugere rotas às carrinhas a partir da ocupação das estações. | `mapa_ocupacao` | - |
| Telemetria | `frota.telemetria` | Recebe o estado das bicicletas elétricas e deteta avarias a partir dele. | `percursos`, `ordens_manutencao` | 007 |
| Ordens de Manutenção | `frota.ordens_manutencao` | Abre ordens de manutenção e entrega-as aos técnicos. | nenhum | - |
| Relatórios Municipais | `camara.relatorios_municipais` | Produz o relatório mensal de utilização por freguesia e de CO₂ evitado. | `registo_viagens`, `percursos` | 007 |

**Estrutura de pastas (ficheiro 03, §2):** `src/main/java/lisboa_pedala/<dominio>/<componente>/`, com `Main.java` na raiz de `lisboa_pedala`, simuladores em `simuladores/` e testes de arquitetura em `src/test/java/lisboa_pedala/arquitetura/`.

## 2. Regras allowed-to-use verificáveis

Verificadas por fitness functions (ArchUnit) em `src/test/java/lisboa_pedala/arquitetura/`, a correr num único comando. **Nenhum código entra no repositório sem passar nelas.** RM3 e RM4 já estão contidas na RM1, mas têm teste próprio para que a falha diga logo que ADR foi violado.

| Regra | Enunciado |
|---|---|
| RM1 | Um componente só importa os componentes da sua linha na tabela do §1. |
| RM2 | Não há ciclos entre componentes. |
| RM3 | `viagens.desbloqueio` não importa nada de `pagamentos` (ADR-004). |
| RM4 | Só `frota.telemetria` e `camara.relatorios_municipais` importam `viagens.percursos` (ADR-007). |
| RM5 | Só `lisboa_pedala.Main` importa todos os componentes. Nenhum componente importa `Main`. |
| RM6 | Nada em `lisboa_pedala` importa `simuladores` (ADR-009). |

Cada componente só lê e escreve as suas tabelas, que têm o prefixo do componente. As tabelas de `percursos` só são acedidas por Percursos (ADR-009, ficheiro 03 §4).

## 3. As três características e o que implicam no código

| Característica | Cenário (resumo) | Implicações |
|---|---|---|
| **Disponibilidade** (C4) | Às 8h30 (pico), 20 das 180 estações perdem a rede 2 h. Em nenhuma hora mais de 18 estações (10%) ficam sem conseguir desbloquear. | O Desbloqueio só pode usar Identificação de Utilizadores. Não chama Tarifação, Cobrança, Faturação nem sistemas externos de pagamentos (ADR-004, RM3). A cobrança acontece depois do fecho da viagem e nunca no caminho do desbloqueio. A Monitorização de Disponibilidade mede hora a hora as estações que desbloqueiam. |
| **Recuperabilidade** (C3b) | Uma doca com 12 h de eventos reenvia-os, com repetidos e fora de ordem. 0 viagens duplicadas, 0 cobranças duplicadas, 100% dos eventos processados. | Cada evento tem um identificador único gerado pela doca. Só a Receção de Eventos guarda os identificadores processados e descarta os repetidos; os restantes componentes recebem eventos já limpos. Os eventos de cada doca são ordenados pela hora registada na doca antes de chegarem ao Registo de Viagens (ADR-005). |
| **Extensibilidade** (C7) | Câmara nova com 30 min grátis e blocos de 10 min. Só mudam ficheiros dentro de Tarifação e os testes de Lisboa continuam a passar. | Tarifação expõe uma única interface estável, "calcular o valor de uma viagem", que recebe um objeto com campos nomeados: hora de início, hora de fim, tipo de cliente e câmara. Cada tarifário é uma implementação separada dentro do componente, escolhida pela câmara e pelo tipo de cliente. Só a Tarifação calcula blocos de tempo; o Registo de Viagens envia apenas as horas de início e de fim (ADR-006). |

## 4. Acoplamento temporal a respeitar

**Ordem obrigatória das operações (US1).** Cada passo só acontece depois do anterior:

1. Pedido de desbloqueio (doca ou app, REST) → Desbloqueio valida o utilizador em Identificação → autoriza a doca.
2. Evento `DESBLOQUEIO` → Receção de Eventos → Registo de Viagens abre a viagem.
3. Evento `ENCAIXE` → Receção de Eventos → Registo de Viagens fecha a viagem (obtém o tipo de cliente em Identificação, calcula o valor em Tarifação) → regista o pedido de cobrança em Cobrança.
4. Cobrança cobra através do adaptador de pagamentos → Faturação emite a fatura → comunica-a à AT.

A Receção de Eventos também atualiza o Mapa de Ocupação e a Monitorização de Disponibilidade.

**Regras**

- **Unidade de trabalho única (ADR-005, ADR-009).** Marcar o evento de encaixe como processado, fechar a viagem com o valor calculado e registar o pedido de cobrança têm de acontecer **numa só unidade de trabalho**, que é uma transação JDBC. Se falhar a meio, um reenvio do mesmo evento não pode fechar nem cobrar a viagem duas vezes, nem deixá-la fechada sem cobrança.
- **Ordem dos eventos.** O desbloqueio tem de ser processado antes do encaixe da mesma viagem. Como os eventos de uma doca sem rede podem chegar fora de ordem, a Receção de Eventos ordena-os pela hora registada na doca antes de os entregar.
- **Desbloqueio nunca espera pela cobrança** (ADR-004). Um utilizador ocasional pode fazer uma viagem e o pagamento falhar depois; fica uma dívida a cobrar mais tarde.
- **Falhas externas.** O pedido de cobrança fica na base de dados e o fecho da viagem não espera pelo fornecedor de pagamentos. Cobrança e Faturação chamam os adaptadores externos com nova tentativa: uma falha externa não desfaz a viagem, o pedido fica pendente e repete-se depois.

## 5. Convenções e proibições

**Convenções**

- Java 21. Nomes de pacote e de pasta como no §1 (minúsculas, com `_`, sem acentos).
- Cada componente expõe uma **interface pública**. Os componentes falam entre si por chamadas de método a essa interface. Todas as restantes classes do pacote são **package-private** (ADR-009).
- A divisão técnica (interface, lógica, persistência) faz-se dentro de cada componente (ADR-008).
- O tipo de evento (`DESBLOQUEIO`, `ENCAIXE`) é um tipo enumerado partilhado, e não um texto interpretado por cada componente (ficheiro 02, §9).
- Chamadas entre componentes com vários dados usam um objeto com campos nomeados, e não argumentos posicionais (ficheiro 02, §9).
- Persistência em SQLite por JDBC; interface por API REST. Não há interface gráfica.
- Cada geração de componente fica registada em `prompts/`, com o pedido, os ficheiros de contexto, se foi aceite ou rejeitado, e quem detetou qualquer violação.

**A IA não pode**

1. Acrescentar uma dependência entre componentes que não esteja na tabela do §1.
2. Importar `pagamentos` a partir de `desbloqueio`, nem pôr aí chamadas a sistemas externos de pagamentos.
3. Ler ou escrever tabelas de outro componente.
4. Aceder às posições de `percursos` a partir de fora do componente. Para fora só sai a distância de cada viagem.
5. Calcular blocos de tempo ou regras de tarifário fora de `tarifacao`.
6. Tratar eventos repetidos fora de `rececao_eventos`.
7. Importar `simuladores` a partir de `lisboa_pedala`, nem importar `Main` a partir de um componente.
8. Alterar `docs/`, os ADRs ou os testes de arquitetura, nem enfraquecer ou desativar um teste para o código passar.
9. Dar um componente por concluído sem as fitness functions e os testes das user stories verdes.
