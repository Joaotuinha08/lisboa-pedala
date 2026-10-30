# Dependências entre componentes lógicos

Diagrama dos componentes ([02-componentes.md](02-componentes.md), secção 8).

```mermaid
flowchart LR
  subgraph viagens
    DES[Desbloqueio]
    REC[Receção de Eventos das Docas]
    REG[Registo de Viagens]
    PER[Percursos]
  end
  subgraph pagamentos
    TAR[Tarifação]
    COB[Cobrança]
    FAT[Faturação]
  end
  subgraph clientes
    IDE[Identificação de Utilizadores]
    CON[Consulta de Histórico]
  end
  subgraph estacoes
    MAP[Mapa de Ocupação]
    MON[Monitorização de Disponibilidade]
    RED[Redistribuição]
  end
  subgraph frota
    TEL[Telemetria]
    ORD[Ordens de Manutenção]
  end
  subgraph camara
    REL[Relatórios Municipais]
  end

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
```

## Legenda

| **Elemento** | **Significado**                                                         |
| ------------ | ----------------------------------------------------------------------- |
| Caixa        | Componente lógico (um pacote em `lisboa_pedala.<dominio>.<componente>`) |
| Seta A > B   | A usa B (A depende de B)                                                |
| Grupo        | Domínio a que o componente pertence (provisório)                        |
