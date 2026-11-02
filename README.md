# Lisboa Pedala

Trabalho Prático 1 de Design de Software (FCUL, 2026/27): especificação de arquitetura do sistema de bicicletas partilhadas Lisboa Pedala e um esqueleto funcional gerado com IA a partir dessa especificação.

## Equipa

| **Elemento**    |
| --------------- |
| @Joaotuinha08   |
| @TomasECarvalho |
A atribuição completa está em `CODEOWNERS` e na secção 4 de `docs/03-particionamento-e-vistas.md`.

## Uso de IA

- **Ferramenta:** Claude
- **Modelo:** Opus 5.5 Alto
- **Onde:** código em `src/` e `tests/`, diagramas Mermaid, ajuda na redação das partes técnicas e revisão crítica.
- A análise de características, os componentes lógicos e as decisões dos ADR-001 a ADR-008 são do grupo.
- O detalhe está em `prompts/uso-de-ia.md` e em `prompts/`. As secções feitas com ajuda de IA têm `(IA)` no título.

## Estrutura

```
ARCHITECTURE.md          especificação lida pela IA antes de gerar código
docs/
  01-caracteristicas.md
  02-componentes.md
  03-particionamento-e-vistas.md
  adr/                   ADR-001 a ADR-009
  diagramas/             C4 níveis 1 a 3, dependências, fluxo da US-1 (Mermaid)
  reflexao.md
src/main/java/
  lisboa_pedala/         plataforma: Main, partilhado e os 15 componentes por domínio
  simuladores/           docas, bicicletas e CLI da app (só falam por REST)
tests/
  architecture/          fitness functions (ArchUnit)
  aceitacao/             testes das user stories, de ponta a ponta por REST
  lisboa_pedala/         testes de componentes (tarifação, cobrança, faturação, unidade de trabalho)
prompts/                 registo de uso de IA
CODEOWNERS               vista de atribuição de trabalho
```

## Requisitos

- Java 21
- Maven 3.9+

## Correr os testes

Todos os testes (fitness functions e user stories), num só comando:

```
mvn test
```

Só as fitness functions:

```
mvn test -Dtest="RegrasArquiteturaTest,TabelasPorComponenteTest"
```

| Teste                        | O que verifica                                                         |
| ---------------------------- | ---------------------------------------------------------------------- |
| `RegrasArquiteturaTest`      | RM1 a RM6, ausência de ciclos, uma só interface pública por componente |
| `TabelasPorComponenteTest`   | Cada componente só usa as tabelas com o seu prefixo                    |
| `Us1...` a `Us4...`          | As user stories de ponta a ponta, pela API REST                        |
Os testes de aceitação usam uma base SQLite temporária, apagada no fim de cada teste. Não escrevem no `lisboa_pedala.db`.

## Correr a plataforma

Terminal 1, arrancar a plataforma (porta 8080, base `lisboa_pedala.db`, com 180 estações e 3 utilizadores de demonstração):

```
mvn compile exec:java
```

Terminal 2, usar a CLI da app:

```
mvn exec:java -Dexec.mainClass=simuladores.CliApp -Dexec.args="demo"
mvn exec:java -Dexec.mainClass=simuladores.CliApp -Dexec.args="historico u-assinante-1"
mvn exec:java -Dexec.mainClass=simuladores.CliApp -Dexec.args="relatorio 2026-11"
```

Outros comandos: `mapa`, `ordens`, `disponibilidade`. O `demo` corre a US-1, a US-3 e a US-2. A cobrança corre em segundo plano de 5 em 5 segundos, por isso o histórico mostra as faturas pouco depois.

No PowerShell, os argumentos `-D` levam aspas: `mvn exec:java "-Dexec.mainClass=simuladores.CliApp" "-Dexec.args=demo"`.

Utilizadores de demonstração:

| Utilizador      | Tipo      | Código da app | Cartão Navegante |
| --------------- | --------- | ------------- | ---------------- |
| `u-assinante-1` | Assinante | `QR-A1`       | `NAV-0001`       |
| `u-assinante-2` | Assinante | `QR-A2`       | `NAV-0002`       |
| `u-ocasional-1` | Ocasional | `QR-O1`       |                  |

## API REST

| Método | Rota                                   | Componente                       |
| ------ | -------------------------------------- | -------------------------------- |
| POST   | `/desbloqueios`                        | Desbloqueio                      |
| POST   | `/eventos`                             | Receção de Eventos das Docas     |
| GET    | `/viagens/{viagemId}/cobranca`         | Cobrança                         |
| GET    | `/utilizadores/{utilizadorId}/historico` | Consulta de Histórico          |
| POST   | `/utilizadores`                        | Identificação de Utilizadores    |
| GET    | `/mapa`                                | Mapa de Ocupação                 |
| GET    | `/disponibilidade?hora=...`            | Monitorização de Disponibilidade |
| GET    | `/redistribuicao?carrinhas=6`          | Redistribuição                   |
| POST   | `/telemetria`                          | Telemetria                       |
| GET    | `/ordens`                              | Ordens de Manutenção             |
| POST   | `/ordens`                              | Ordens de Manutenção             |
| POST   | `/ordens/{ordemId}/conclusao`          | Ordens de Manutenção             |
| GET    | `/relatorios/{AAAA-MM}`                | Relatórios Municipais            |
| GET    | `/saude`                               | Main                             |

## User stories implementadas

| User story | Descrição                                                                    | Teste                              |
| ---------- | ---------------------------------------------------------------------------- | ---------------------------------- |
| US-1       | Como assinante, desbloqueio e devolvo uma bicicleta e a viagem fica registada e cobrada | `Us1DesbloquearDevolverCobrarTest` |
| US-2       | Como técnico, recebo uma ordem de manutenção quando uma bicicleta reporta avaria | `Us2OrdemManutencaoTest`        |
| US-3       | Como doca sem rede, reenvio os eventos guardados quando a rede volta, sem duplicar viagens | `Us3RecuperacaoDocaSemRedeTest` |
| US-4       | Como gestor municipal, obtenho o relatório de utilização por freguesia        | `Us4RelatorioMunicipalTest`        |

Docas, bicicletas, pagamentos e Autoridade Tributária são simulados.
