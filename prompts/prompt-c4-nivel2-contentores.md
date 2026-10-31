# Prompts usadas para gerar o C4 nível 2 - Contentores

**Ferramenta e modelo:** Claude, modelo Opus 5.5 Alto
**Ficheiro gerado:** `docs/diagramas/c4-nivel2-contentores.md`

## Prompt 1: diagrama de contentores

**Objetivo:** gerar o diagrama C4 de nível 2 coerente com a vista de alocação e com o ADR-009.
**Prompt:** "Cria o C4 de nível 2 (contentores) em Mermaid, com a mesma notação e legenda do nível 1 que está em anexo. Decidimos que os 15 componentes correm numa só aplicação Java com API REST, com uma base de dados SQLite, e que a CLI da app, as docas e as bicicletas são simuladores que só falam com o servidor por REST. Pagamentos e AT são adaptadores simulados. Põe a fronteira do sistema como grupo e indica a tecnologia em cada contentor."
**Ficheiros de contexto fornecidos:**
- Enunciado do trabalho (`DS - Trabalho 1.pdf`)
- `docs/diagramas/c4-nivel1-contexto.md`
- `docs/adr/ADR-009.md`
- `docs/03-particionamento-e-vistas.md` (secção 4, vista de alocação)

## Prompt 2: ajuste da legenda

**Prompt:** "Adapta a legenda ao nível 2: o azul passa a ser contentor e acrescenta uma linha para o grupo que marca a fronteira do sistema."

## Resultado

**Aceite**, depois de revisão do grupo. Confirmámos que cada contentor corresponde a uma linha da tabela de implantação da secção 4.
**Violação da arquitetura:** não se aplica (diagrama, não código).
