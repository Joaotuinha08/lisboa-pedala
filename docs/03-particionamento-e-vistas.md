# 1) Particionamento de topo

O sistema é particionado **por domínio**. Os 15 componentes do [ficheiro 02](02-componentes.md) ficam agrupados em 6 domínios e cada componente vive `lisboa_pedala.<dominio>.<componente>`. A divisão técnica (interface, lógica, persistência) fica dentro de cada componente. A decisão está no [ADR-008.md](ADR-008.md).

| **Domínio** | **Componentes**                                                          | **Porquê juntos**                                   | **Características**                            |
| ----------- | ------------------------------------------------------------------------ | --------------------------------------------------- | ---------------------------------------------- |
| viagens     | Desbloqueio, receção de eventos das docas, registo de viagens, percursos | Tudo o que acontece entre o desbloqueio e o encaixe | Disponibilidade, recuperabilidade, privacidade |
| pagamentos  | Tarifação, cobrança, faturação                                           | O que acontece depois de a viagem fechar            | Extensibilidade, arquivabilidade               |
| clientes    | Identificação de utilizadores, consulta de histórico                     | O que o utilizador é e o que vê de si próprio       |                                                |
| estacoes    | Mapa de cupação, Monitorização de Disponibilidade, Redistribuição        | Estado de cada estação                              | Disponibilidade (medição)                      |
| frota       | Telemetria, Ordens de Manutenção                                         | Estado de cada bicicleta                            |                                                |
| camara      | Relatórios Municipais                                                    | Reporte à Câmara, só com dados agregados            | Privacidade                                    |

### Trade-off

| **Por domínio**                                                                                  | **Técnico**                                                                 |
| ------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------- |
| Um tarifário novo só mexe em `pagamentos/tarifacao` (cenário de extensibilidade do ficheiro 01). | Um tarifário novo mexe nas camadas de apresentação, negócio e persistência. |
| O caminho do desbloqueio fica isolado em `viagens/desbloqueio`.                                  | O desbloqueio fica espalhado pelas camadas, junto com tudo o resto.         |
| Cada elemento do grupo fica com domínios inteiros (secção 4).                                    | Cada elemento ficaria com uma camada e todas as US passariam por todos.     |
| Regenerar um componente com IA toca um só diretório.                                             | Regenerar um componente obriga a mexer em várias camadas.                   |
| **Custo:** a persistência SQLite repete-se em cada componente.                                   | A persistência ficaria num só sítio.                                        |
| **Custo:** a US1 atravessa 3 domínios (viagens, clientes, pagamentos).                           |                                                                             |

___

# 2) Vista de módulos

### Estrutura dos pacotes

Projeto Java.

```
src/
  main/java/
    lisboa_pedala/
      Main.java
      viagens/
        desbloqueio/
        rececao_eventos/
        registo_viagens/
        percursos/
      pagamentos/
        tarifacao/
        cobranca/
        faturacao/
      clientes/
        identificacao/
        consulta_historico/
      estacoes/
        mapa_ocupacao/
        monitorizacao_disponibilidade/
        redistribuicao/
      frota/
        telemetria/
        ordens_manutencao/
      camara/
        relatorios_municipais/
    simuladores/
  test/java/
    lisboa_pedala/
      arquitetura/
```


### Regras allowed-to-use

A coluna "Pode usar" é uma lista fechada. Tudo o que não está lá é proibido.

| **Componente**                   | **Pode usar**                                                 | **ADR** |
| -------------------------------- | ------------------------------------------------------------- | ------- |
| Desbloqueio                      | identificacao                                                 | ADR-004 |
| Receção de Eventos das Docas     | registo_viagens, mapa_ocupacao, monitorizacao_disponibilidade | ADR-005 |
| Registo de Viagens               | identificacao, tarifacao, cobranca                            | ADR-005 |
| Percursos                        | registo_viagens                                               | ADR-007 |
| Tarifação                        | nenhum                                                        | ADR-006 |
| Cobrança                         | faturacao                                                     | ADR-004 |
| Faturação                        | nenhum                                                        |         |
| Identificação de Utilizadores    | nenhum                                                        |         |
| Consulta de Histórico            | registo_viagens, faturacao                                    |         |
| Mapa de Ocupação                 | nenhum                                                        |         |
| Monitorização de Disponibilidade | nenhum                                                        |         |
| Redistribuição                   | mapa_ocupacao                                                 |         |
| Telemetria                       | percursos, ordens_manutencao                                  | ADR-007 |
| Ordens de Manutenção             | nenhum                                                        |         |
| Relatórios Municipais            | registo_viagens, percursos                                    | ADR-007 |

O diagrama destas dependências está em [dependencias-componentes.md](diagramas/dependencias-componentes.md).

### Regras verificáveis

- **RM1** - Um componente só importa os componentes da sua linha na tabela.
- **RM2** - Não há ciclos entre componentes.
- **RM3** - `viagens.desbloqueio` não importa nada de `pagamentos` (ADR-004).
- **RM4** - Só `frota.telemetria` e `camara.relatorios_municipais` importam `viagens.percursos` (ADR-007).
- **RM5** - Só `lisboa_pedala.Main` importa todos os componentes. Nenhum componente importa `Main`.

A RM3 e a RM4 já estão contidas na RM1, mas têm um teste próprio para que a falha diga logo que ADR foi violado.

___

# 3) Vista de componentes e conectores (IA)

Fluxo desbloquear, devolver e cobrar (US1). O diagrama também está em [cc-desbloquear-devolver-cobrar.md](cc-desbloquear-devolver-cobrar.md).

Notas:
- Com a app, o pedido de desbloqueio chega pela app em vez da doca. O resto é igual.
- A Receção de Eventos também atualiza o Mapa de Ocupação e a Monitorização de Disponibilidade. Ficou fora do diagrama para não o encher.
- O desbloqueio (passos 1 a 5) não toca em nenhum componente de `pagamentos`.

### Conectores

| **Conector**                | **Onde**                                  | **Tipo**                     | **Porquê**                                                                        |
| --------------------------- | ----------------------------------------- | ---------------------------- | --------------------------------------------------------------------------------- |
| Pedido REST                 | Doca e app > Desbloqueio, Doca > Receção  | Síncrono, pela rede móvel    | Dispositivos externos. Sem confirmação, a doca volta a enviar.                    |
| Chamada de método           | Entre componentes, pela interface pública | Síncrono                     | Todos os componentes correm na mesma JVM.                                         |
| Pedido de cobrança pendente | Registo de Viagens > Cobrança             | Assíncrono (fica na BD)      | O fecho da viagem não espera pelo fornecedor de pagamentos.                       |
| Adaptador externo           | Cobrança > Pagamentos, Faturação > AT     | Síncrono, com nova tentativa | Uma falha externa não desfaz a viagem. O pedido fica pendente e repete-se depois. |

___

# 4) Vista de alocação (IA)
 
### Implantação

O sistema corre numa só aplicação Java com uma base de dados SQLite. A decisão está no [ADR-009.md](ADR-009.md) e corresponde ao [C4 nível 2](c4-nivel2-contentores.md).

| **Elemento**                     | **Onde corre**                                   | **O que contém**                                              |
| -------------------------------- | ------------------------------------------------ | ------------------------------------------------------------- |
| Servidor Lisboa Pedala           | Uma JVM com API REST (`lisboa_pedala.Main`)      | Os 15 componentes de `lisboa_pedala`                          |
| Base de dados                    | Ficheiro SQLite na mesma máquina (JDBC)          | Tabelas de cada componente, com o prefixo do componente       |
| Simulador de docas e bicicletas  | JVM à parte (pacote `simuladores`)               | Docas que guardam até 24 h de eventos e reenviam, telemetria  |
| CLI da app                       | JVM à parte (pacote `simuladores`)               | Pedidos dos utilizadores, técnicos, carrinhas e gestores      |
| Pagamentos e AT                  | Adaptadores simulados dentro de Cobrança e Faturação | Respostas simuladas, incluindo falhas                     |

Cada componente só lê e escreve as suas tabelas. As tabelas de `percursos` só são acedidas por Percursos.

### Atribuição de trabalho

A atribuição segue o particionamento por domínio. O ficheiro [CODEOWNERS](../CODEOWNERS) aplica esta tabela.

| **Elemento**    | **Domínios**                  | **Componentes** | **Porquê**                                                                                                      |
| --------------- | ----------------------------- | --------------- | --------------------------------------------------------------------------------------------------------------- |
| @Joaotuinha08   | viagens, pagamentos, clientes | 9               | Caminho crítico do contrato (disponibilidade e recuperabilidade)<br>Extensibilidade da tarifação e o fim da US1 |
| @TomasECarvalho | estacoes, frota, camara       | 6               | Componentes fora da US1 (US2 e US4)                                                                             |

`ARCHITECTURE.md`, `docs/`, `pom.xml`, `Main.java` e os testes de arquitetura pertencem aos dois, porque uma mudança nas regras afeta todos.

___

# 5) Diagramas C4 (IA)

Todos usam a mesma notação e legenda, e batem certo com as vistas acima.

| **Diagrama**                                         | **Corresponde a**                                                            |
| ---------------------------------------------------- | ---------------------------------------------------------------------------- |
| [C4 nível 1 - Contexto](c4-nivel1-contexto.md)       | Atores e sistemas externos ([ficheiro 02](docs/02-componentes.md), secção 1) |
| [C4 nível 2 - Contentores](c4-nivel2-contentores.md) | Vista de alocação, implantação (secção 4)                                    |
| [C4 nível 3 - Componentes](c4-nivel3-componentes.md) | Vista de módulos e allowed-to-use (secção 2)                                 |
