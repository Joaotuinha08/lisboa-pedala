# C4 nível 3 - Componentes

Componentes dentro do Servidor Lisboa Pedala, agrupados por domínio ([ADR-008](../adr/ADR-008.md)). As setas internas são exatamente as regras allowed-to-use da vista de módulos ([03-particionamento-e-vistas.md](../03-particionamento-e-vistas.md), secção 2).

```mermaid

flowchart LR

  CLI["<b>CLI da app</b>"]:::contentor

  DOC["<b>Docas</b>"]:::externo

  BIC["<b>Bicicletas</b>"]:::externo

  

  subgraph SRV["Servidor Lisboa Pedala"]

    subgraph viagens

      DES["Desbloqueio"]:::componente

      REC["Receção de Eventos das Docas"]:::componente

      REG["Registo de Viagens"]:::componente

      PER["Percursos"]:::componente

    end

    subgraph pagamentos

      TAR["Tarifação"]:::componente

      COB["Cobrança"]:::componente

      FAT["Faturação"]:::componente

    end

    subgraph clientes

      IDE["Identificação de Utilizadores"]:::componente

      CON["Consulta de Histórico"]:::componente

    end

    subgraph estacoes

      MAP["Mapa de Ocupação"]:::componente

      MON["Monitorização de Disponibilidade"]:::componente

      RED["Redistribuição"]:::componente

    end

    subgraph frota

      TEL["Telemetria"]:::componente

      ORD["Ordens de Manutenção"]:::componente

    end

    subgraph camara

      REL["Relatórios Municipais"]:::componente

    end

  end

  

  BD[("<b>Base de dados</b><br/>SQLite")]:::contentor

  PAG["<b>Fornecedor de pagamentos</b>"]:::externo

  AT["<b>Autoridade Tributária</b>"]:::externo

  

  DOC -.-> DES

  DOC -.-> REC

  BIC -.-> TEL

  CLI -.-> DES

  CLI -.-> MAP

  CLI -.-> CON

  CLI -.-> ORD

  CLI -.-> RED

  CLI -.-> REL

  CLI -.-> MON

  

  DES --> IDE

  REC --> REG

  REC --> MAP

  REC --> MON

  REG --> IDE

  REG --> TAR

  REG --> COB

  COB --> FAT

  TEL --> PER

  TEL --> ORD

  PER --> REG

  CON --> REG

  CON --> FAT

  RED --> MAP

  REL --> REG

  REL --> PER

  

  COB -.-> PAG

  FAT -.-> AT

  

  classDef contentor fill:#1168bd,stroke:#0b4884,color:#ffffff

  classDef componente fill:#85bbf0,stroke:#5d82a8,color:#000000

  classDef externo fill:#8a8a8a,stroke:#6b6b6b,color:#ffffff

```

Todos os componentes usam a base de dados, cada um só nas suas tabelas. Essas setas foram omitidas para o diagrama se ler.

## Legenda

Igual à do [nível 1](c4-nivel1-contexto.md).

| **Elemento**    | **Significado**                                               |
| --------------- | ------------------------------------------------------------- |
| Azul            | Contentor                                                     |
| Azul claro      | Componente (um pacote `lisboa_pedala.<dominio>.<componente>`) |
| Cilindro        | Base de dados                                                 |
| Cinzento        | Sistema externo (simulado neste trabalho)                     |
| Grupo interno   | Domínio                                                       |
| Seta contínua   | Chamada de método na mesma JVM (A usa B)                      |
| Seta tracejada  | Pela rede (REST) ou com um sistema externo                    |