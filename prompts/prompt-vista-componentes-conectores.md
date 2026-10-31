# Prompts usadas para gerar a vista de componentes e conectores (US1)

**Ferramenta e modelo:** Claude, modelo Opus 5.5 Alto
**Ficheiros gerados:**
- `docs/diagramas/cc-desbloquear-devolver-cobrar.md`
- `docs/03-particionamento-e-vistas.md`, secção 3 (notas e tabela de conectores)

## Prompt 1: diagrama de sequência da US1

**Objetivo:** mostrar o fluxo desbloquear, devolver e cobrar com a ordem das operações e a unidade de trabalho.
**Prompt:** "Cria um diagrama de sequência em Mermaid para a US1 (o assinante desbloqueia e devolve uma bicicleta e a viagem fica registada e cobrada). Usa os componentes e dependências do ficheiro 02. A ordem das operações tem de seguir o acoplamento temporal que descrevemos na secção 9 do ficheiro 02, e os passos fechar viagem, calcular valor e registar o pedido de cobrança têm de aparecer numa só unidade de trabalho, como diz o ADR-005. O desbloqueio só pode depender da Identificação (ADR-004). Numera os passos e acrescenta uma legenda."
**Ficheiros de contexto fornecidos:**
- Enunciado do trabalho (`DS - Trabalho 1.pdf`)
- `docs/02-componentes.md` (dependências e secção 9, conascência e acoplamento temporal)
- `docs/adr/ADR-004.md`
- `docs/adr/ADR-005.md`

## Prompt 2: tabela de conectores

**Objetivo:** descrever os conectores usados no fluxo.
**Prompt:** "A partir do diagrama, faz uma tabela de conectores com as colunas Conector, Onde, Tipo e Porquê. O pedido de cobrança fica pendente na base de dados para o fecho da viagem não esperar pelo fornecedor de pagamentos, e uma falha externa não desfaz a viagem."

## Prompt 3: notas

**Prompt:** "Escreve três notas curtas por baixo: o caso da app em vez da doca, o que ficou de fora do diagrama para não o encher e que o desbloqueio não toca em pagamentos."

## Resultado

**Aceite**, depois de revisão do grupo. A ordem das operações e o que fica na unidade de trabalho foram decididos por nós nos ficheiros 02 e no ADR-005. A IA só os passou para diagrama e tabela.
**Violação da arquitetura:** não se aplica (diagrama, não código).
