# Prompts usadas para gerar o C4 nível 3 - Componentes

**Ferramenta e modelo:** Claude, modelo Opus 5.5 Alto
**Ficheiro gerado:** `docs/diagramas/c4-nivel3-componentes.md`

## Prompt 1: diagrama de componentes

**Objetivo:** gerar o C4 de nível 3 com os componentes dentro do servidor, agrupados por domínio, com as setas iguais às regras allowed-to-use.
**Prompt:** "Cria o C4 de nível 3 (componentes) do Servidor Lisboa Pedala em Mermaid. Parte do diagrama de dependências em anexo: os grupos são os domínios do ADR-008 e as setas internas têm de ser exatamente as regras allowed-to-use da secção 2 do ficheiro 03, sem acrescentar nenhuma. Acrescenta as entradas externas (CLI, docas, bicicletas) e as saídas para pagamentos e AT. Mantém a notação e a legenda dos níveis 1 e 2."
**Ficheiros de contexto fornecidos:**
- Enunciado do trabalho (`DS - Trabalho 1.pdf`)
- `docs/diagramas/dependencias-componentes.md`
- `docs/diagramas/c4-nivel2-contentores.md`
- `docs/adr/ADR-008.md`
- `docs/03-particionamento-e-vistas.md` (secção 2, vista de módulos e regras RM1 a RM5)

## Prompt 2: legibilidade

**Prompt:** "O diagrama fica ilegível com as setas de todos os componentes para a base de dados. Tira-as e explica numa frase por baixo do diagrama que todos os componentes usam a base de dados, cada um só nas suas tabelas."

## Resultado

**Aceite**, depois de revisão do grupo. Comparámos seta a seta com a tabela de dependências do ficheiro 02 para garantir que não havia dependências a mais.
**Violação da arquitetura:** não se aplica (diagrama, não código).
