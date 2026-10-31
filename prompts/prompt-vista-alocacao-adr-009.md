# Prompts usadas para a vista de alocação e o ADR-009

**Ferramenta e modelo:** Claude, modelo Opus 5.5 Alto
**Ficheiros gerados:**
- `docs/adr/ADR-009.md`
- `docs/03-particionamento-e-vistas.md`, secção 4 (implantação e atribuição de trabalho)

## Prompt 1: redação do ADR-009

**Objetivo:** escrever o ADR da decisão de implantação, que o grupo já tinha tomado.
**Prompt:** "Escreve o ADR-009 no mesmo formato dos ADRs em anexo (estado, data, componentes afetados, contexto, decisão, consequências, alternativas rejeitadas). A decisão do grupo é: os 15 componentes numa só aplicação Java com API REST, uma só base de dados SQLite com tabelas próprias por componente, comunicação entre componentes por chamadas de método à interface pública, simuladores no pacote `simuladores` e pagamentos e AT como adaptadores simulados. Justifica com a equipa de 6 pessoas e orçamento fixo e com a unidade de trabalho do ADR-005. Nas consequências inclui o risco para a disponibilidade. A alternativa rejeitada é separar o desbloqueio numa aplicação própria."
**Ficheiros de contexto fornecidos:**
- Enunciado do trabalho (`DS - Trabalho 1.pdf`)
- `docs/01-caracteristicas.md` (top 3 e restrições)
- `docs/adr/ADR-004.md`, `docs/adr/ADR-005.md`, `docs/adr/ADR-008.md`

## Prompt 2: vista de alocação

**Objetivo:** passar a decisão do ADR-009 e a divisão de trabalho do grupo para tabelas.
**Prompt:** "Com base no ADR-009, faz a tabela de implantação (Elemento, Onde corre, O que contém). Depois faz a tabela de atribuição de trabalho: a divisão por domínios entre os dois elementos é a seguinte [divisão indicada pelo grupo]. Explica numa frase que o CODEOWNERS aplica esta tabela."

## Resultado

**Aceite**, depois de revisão do grupo. A decisão, a alternativa rejeitada e a divisão do trabalho são do grupo. A IA redigiu o texto e as tabelas.
**Violação da arquitetura:** não se aplica (documentação, não código).
