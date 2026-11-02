# Prompts usadas para gerar o código a partir do ARCHITECTURE.md

**Ferramenta e modelo:** Claude, modelo Opus 5.5
**Ficheiros gerados:** todo o projeto Maven `lisboa-pedala/`, com:
- `src/main/java/lisboa_pedala/`: os 15 componentes, `partilhado/` e `Main.java`;
- `src/main/java/simuladores/`: docas, bicicletas e CLI da app;
- `src/test/java/`: as fitness functions, os testes das user stories e os testes unitários;
- `scripts/verificar_fitness_functions.sh` e a pasta `prompts/`.

O registo por componente está nos ficheiros `prompts/<dominio>.<componente>.md`. As decisões que a IA tomou sem estarem na especificação estão em `prompts/LACUNAS.md`.

## Prompt 1: De acordo com o architecture markdown desenvolve o software Lisboa Pedala

**Objetivo:** gerar a implementação completa do Lisboa Pedala a partir da especificação para IA.
**Prompt:** "De acordo com o architecture markdown desenvolve o software Lisboa Pedala"
**Ficheiros de contexto fornecidos:**
- `ARCHITECTURE.md`

**Não foram fornecidos** `docs/` nem os ADRs (`docs/adr/`). A IA conheceu os ADRs só pelo número e pelo resumo que o ARCHITECTURE.md faz deles.

**Resultado:** **aceite com reservas**, depois de revisão do grupo.
- Gerou os 15 componentes de uma só vez, em vez de um pedido por componente.
- Gerou 34 testes, todos verdes: 12 fitness functions (RM1–RM6, tabelas por componente e uma interface pública por componente), 11 testes de aceitação (US-1, US-2, US-3 e US-4) e 11 testes unitários.
- O script `verificar_fitness_functions.sh` confirma que as fitness functions apanham 11 de 11 violações plantadas.

Durante a geração, a IA cometeu e corrigiu estes erros:

| # | Erro | Violação da arquitetura? | Detetado por |
|---|---|---|---|
| E1 | SQL partido pela concatenação de um text block sem espaço (`c.comunicada_emFROM`) | Não | Testes unitários (`FaturacaoTest`) |
| E2 | Uso errado da API do ArchUnit (`CompositeArchRule.and(List)`) | Não | Compilador |
| E3 | Mensagem da RM1 ilegível: listava as 15 regras quando só uma falhava | Não | Revisão (ao plantar um mutante) |
| E4 | `mainClass` fixo no `pom.xml` impedia correr o CLI | Não | Revisão |

**Violação da arquitetura:** **sim**. O ARCHITECTURE.md diz "pára e pergunta" quando algo não está definido, e a IA não parou. Tomou 13 decisões sozinha (L1–L13 em `LACUNAS.md`), entre elas:
- criar o pacote `lisboa_pedala.partilhado`, que não está no §1;
- inventar os preços do tarifário;
- decidir como emparelhar eventos de docas diferentes.

**Detetado por:** revisão humana. Nenhuma fitness function apanha este tipo de violação.
