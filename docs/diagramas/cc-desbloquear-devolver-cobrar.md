# Vista de componentes e conectores - desbloquear, devolver e cobrar (IA)

Fluxo da US1 ([03-particionamento-e-vistas.md](../03-particionamento-e-vistas.md), secção 3).
 

```mermaid

sequenceDiagram

  autonumber

  actor U as Assinante

  participant DOC as Doca (simulada)

  participant DES as Desbloqueio

  participant IDE as Identificação

  participant REC as Receção de Eventos

  participant REG as Registo de Viagens

  participant TAR as Tarifação

  participant COB as Cobrança

  participant FAT as Faturação

  participant EXT as Pagamentos e AT (simulados)

  

  U->>DOC: Identifica-se (Navegante ou QR na app)

  DOC-->>DES: Pedido de desbloqueio (REST)

  DES->>IDE: Validar utilizador

  IDE-->>DES: Válido

  DES-->>DOC: Autorizado

  DOC-->>REC: Evento DESBLOQUEIO (REST)

  REC->>REG: Abrir viagem

  U->>DOC: Encaixa a bicicleta noutra estação

  DOC-->>REC: Evento ENCAIXE (REST)

  rect rgba(150, 150, 150, 0.15)

    Note over REC,COB: Uma só unidade de trabalho (ADR-005)

    REC->>REG: Fechar viagem

    REG->>IDE: Tipo de cliente

    REG->>TAR: Calcular valor (dados da viagem)

    TAR-->>REG: Valor

    REG->>COB: Registar pedido de cobrança

  end

  COB-->>EXT: Cobrar

  COB->>FAT: Emitir fatura

  FAT-->>EXT: Comunicar à AT

```

## Legenda

| **Elemento**           | **Significado**                                                     |
| ---------------------- | ------------------------------------------------------------------- |
| Seta contínua          | Chamada de método na mesma JVM                                      |
| Seta tracejada         | Pela rede (REST), resposta, ou comunicação com um sistema externo   |
| Caixa sombreada        | Passos que acontecem numa só unidade de trabalho (ADR-005)          |
| Número                 | Ordem obrigatória das operações (acoplamento temporal, ficheiro 02) |