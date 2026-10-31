# C4 nível 1 - Contexto (IA)

Quem usa o Lisboa Pedala e com que sistemas externos comunica ([03-particionamento-e-vistas.md](../03-particionamento-e-vistas.md), secção 5).

```mermaid

flowchart TB

  UTI["<b>Utilizador</b><br/>Assinante ou ocasional"]:::pessoa

  TEC["<b>Técnico de manutenção</b>"]:::pessoa

  CAR["<b>Equipa de redistribuição</b>"]:::pessoa

  GES["<b>Gestor municipal</b>"]:::pessoa

  

  LP["<b>Lisboa Pedala</b><br/>Desbloqueia e devolve bicicletas,<br/>cobra viagens, mantém a frota<br/>e reporta à Câmara"]:::sistema

  

  DOC["<b>Docas</b><br/>Simuladas"]:::externo

  BIC["<b>Bicicletas elétricas</b><br/>Simuladas"]:::externo

  PAG["<b>Fornecedor de pagamentos</b><br/>MB WAY e cartão, simulado"]:::externo

  AT["<b>Autoridade Tributária</b><br/>Simulada"]:::externo

  

  UTI -->|"Desbloqueia, vê o mapa,<br/>consulta histórico e faturas"| LP

  TEC -->|"Recebe ordens de manutenção"| LP

  CAR -->|"Recebe rotas"| LP

  GES -->|"Obtém o relatório mensal"| LP

  DOC -.->|"Pedidos de desbloqueio e eventos"| LP

  BIC -.->|"Telemetria a cada 30 s"| LP

  LP -.->|"Cobra viagens"| PAG

  LP -.->|"Comunica faturas"| AT

  

  classDef pessoa fill:#08427b,stroke:#052e56,color:#ffffff

  classDef sistema fill:#1168bd,stroke:#0b4884,color:#ffffff

  classDef externo fill:#8a8a8a,stroke:#6b6b6b,color:#ffffff

```

## Legenda

A mesma legenda é usada nos três níveis.

| **Elemento**    | **Significado**                                                 |
| --------------- | --------------------------------------------------------------- |
| Azul escuro     | Pessoa                                                          |
| Azul            | Sistema Lisboa Pedala (nível 1) ou contentor (nível 2)          |
| Azul claro      | Componente (nível 3)                                            |
| Cilindro        | Base de dados                                                   |
| Cinzento        | Sistema externo (simulado neste trabalho)                       |
| Seta contínua   | Uso direto: uma pessoa usa, ou chamada de método na mesma JVM   |
| Seta tracejada  | Pela rede (REST) ou com um sistema externo                      |