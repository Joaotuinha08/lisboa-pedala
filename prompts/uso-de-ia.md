# Declaração de uso de IA

**Ferramentas e modelos usados:** Claude, modelo Opus 5.5 Alto

## O que é do grupo

A análise e as decisões de arquitetura são nossas:

- Identificação das características, redução a 7 candidatas e escolha das top 3 (disponibilidade, recuperabilidade e extensibilidade), com trade-offs e cenários (`01-caracteristicas.md`)
- Componentes lógicos v1 e v2, atribuição das user stories, dependências, coesão, conascência e acoplamento temporal (`02-componentes.md`)
- Particionamento de topo por domínio e regras allowed-to-use (`03-particionamento-e-vistas.md`, secções 1 e 2)
- Decisões e justificações dos ADR-001 a ADR-008, incluindo as alternativas rejeitadas  

## Onde a IA foi mais útil

**Diagramas.** Passou para Mermaid aquilo que já tínhamos decidido em tabelas e texto: dependências entre componentes, C4 níveis 1 a 3 e o diagrama de sequência da US1. Também propôs a notação de cores e a legenda comum.

**Partes técnicas e tecnologias.** Ajudou a redigir as partes mais técnicas: a vista de componentes e conectores, a vista de alocação e o texto do ADR-009 (Java, REST, JDBC, SQLite, pacotes e visibilidade). A decisão de implantação foi nossa; a IA ajudou a escrevê-la e a confirmar que era coerente com o enunciado.

**Indicações.** Serviu de revisor crítico: apontou partes confusas ou mal justificadas. Nós decidimos o que mudar e fizemos as alterações.

**Formatação.** Ajeitou tabelas, títulos e listas em Markdown quando pedido (raramente).

**Erros ortográficos.** Corrigiu ortografia e gramática sem mudar o conteúdo (raramente).

**Código.** O código em `src/` e os testes em `tests/` são gerados com IA a partir do `ARCHITECTURE.md` e do ADR relevante, como pede o enunciado. Cada pedido fica registado em `prompts/`, um ficheiro por componente.

## Secções marcadas

As secções e ficheiros feitos com ajuda de IA têm `(IA)` no título.