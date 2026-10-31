# Prompts usadas para gerar o C4 nível 1 - Contexto

**Ferramenta e modelo:** Claude, modelo Opus 5.5 Alto
**Ficheiro gerado:** `docs/diagramas/c4-nivel1-contexto.md`

## Prompt 1: diagrama de contexto

**Objetivo:** gerar o diagrama C4 de nível 1 em Mermaid a partir dos atores e sistemas externos que o grupo identificou.
**Prompt:** "Com base nos ficheiros em anexo, cria um diagrama C4 de nível 1 (contexto) em Mermaid para o sistema Lisboa Pedala. Usa os atores e os sistemas externos que identificámos no ficheiro 02, secção 1. Os sistemas externos (docas, bicicletas, pagamentos e Autoridade Tributária) são simulados neste trabalho e isso deve aparecer no diagrama. Usa setas contínuas para uso direto e tracejadas para comunicação pela rede ou com sistemas externos."
**Ficheiros de contexto fornecidos:**
- Enunciado do trabalho (`DS - Trabalho 1.pdf`)
- `docs/01-caracteristicas.md`
- `docs/02-componentes.md` (secção 1, atores e ações)

## Prompt 2: cores e legenda

**Objetivo:** dar ao diagrama uma notação consistente que servisse para os três níveis.
**Prompt:** "Acrescenta classes de cor ao estilo C4 (pessoa, sistema, externo) e escreve uma legenda em tabela Markdown que possa ser reutilizada nos níveis 2 e 3."

## Resultado

**Aceite**, depois de revisão do grupo. Os atores e sistemas externos já estavam definidos por nós, a IA só os passou para Mermaid e propôs a legenda.
**Violação da arquitetura:** não se aplica (diagrama, não código).
