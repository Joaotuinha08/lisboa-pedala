# C4 nível 2 - Contentores (IA)

Onde corre cada parte do sistema ([03-particionamento-e-vistas.md](../03-particionamento-e-vistas.md), secção 4, e [ADR-009](../adr/ADR-009.md)).

```mermaid

flowchart TB

  UTI["<b>Utilizador</b><br/>Assinante ou ocasional"]:::pessoa

  EQU["<b>Equipa</b><br/>Técnicos, carrinhas e gestores"]:::pessoa

  

  subgraph LP["Lisboa Pedala"]

    CLI["<b>CLI da app</b><br/>Java, simulada<br/>Pedidos dos utilizadores e da equipa"]:::contentor

    SRV["<b>Servidor Lisboa Pedala</b><br/>Java, API REST<br/>Os 15 componentes numa só JVM"]:::contentor

    BD[("<b>Base de dados</b><br/>SQLite<br/>Tabelas próprias de cada componente")]:::contentor

  end

  

  DOC["<b>Docas</b><br/>Simulador, guarda 24 h de eventos"]:::externo

  BIC["<b>Bicicletas elétricas</b><br/>Simulador de telemetria"]:::externo

  PAG["<b>Fornecedor de pagamentos</b><br/>Adaptador simulado"]:::externo

  AT["<b>Autoridade Tributária</b><br/>Adaptador simulado"]:::externo

  

  UTI --> CLI

  EQU --> CLI

  CLI -.->|"REST/JSON"| SRV

  DOC -.->|"REST/JSON<br/>desbloqueios e eventos"| SRV

  BIC -.->|"REST/JSON<br/>telemetria"| SRV

  SRV -->|"JDBC"| BD

  SRV -.->|"Cobrar"| PAG

  SRV -.->|"Comunicar faturas"| AT

  

  classDef pessoa fill:#08427b,stroke:#052e56,color:#ffffff

  classDef contentor fill:#1168bd,stroke:#0b4884,color:#ffffff

  classDef externo fill:#8a8a8a,stroke:#6b6b6b,color:#ffffff

```

## Legenda

Igual à do [nível 1](c4-nivel1-contexto.md).

| **Elemento**    | **Significado**                                                 |
| --------------- | --------------------------------------------------------------- |
| Azul escuro     | Pessoa                                                          |
| Azul            | Contentor (algo que corre ou guarda dados)                      |
| Cilindro        | Base de dados                                                   |
| Cinzento        | Sistema externo (simulado neste trabalho)                       |
| Grupo           | Fronteira do sistema Lisboa Pedala                              |
| Seta contínua   | Uso direto: uma pessoa usa, ou acesso local                     |
| Seta tracejada  | Pela rede (REST) ou com um sistema externo                      |