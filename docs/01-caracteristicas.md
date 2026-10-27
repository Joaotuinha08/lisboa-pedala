# 1) Separação das frase por códigos 

Para facilitar a organização e identificação das características foi atribuído um código a cada frase: **utilizadores** (U1-U5), **requisitos** (R1-R8) e **contexto adicional** (C1-C8). Quando as frases do contexto apresentavam duas ideias elas eram separadas em código diferentes, por exemplo, C1a e C1b.

### Utilizadores
| **Código** | **Perfil**                            | **Volume**      |
| ---------- | ------------------------------------- | --------------- |
| U1         | Assinantes (passe anual)              | ~60 000         |
| U2         | Utilizadores ocasionais               | ~15 000 por mês |
| U3         | Técnicos de manutenção                | 25              |
| U4         | Equipas de redistribuição (carrinhas) | 6               |
| U5         | Gestores municipais                   | ~10             |

### Requisitos:
| **Código** | **Requisito**                                                                                                                                    |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| R1         | Desbloquear uma bicicleta na estação com a app (código QR) ou com o cartão Navegante                                                             |
| R2         | Devolver em qualquer estação com doca livre; a viagem fecha quando a doca confirma o encaixe                                                     |
| R3         | Tarifário: o passe anual inclui 45 min grátis por viagem, depois cobra-se por bloco de 15 min; os ocasionais pagam por viagem (MB WAY ou cartão) |
| R4         | Mapa em tempo real com bicicletas e docas livres por estação                                                                                     |
| R5         | Abrir uma ordem de manutenção quando uma bicicleta reporta avaria (bateria, travões) ou um utilizador a reporta                                  |
| R6         | Sugerir rotas às carrinhas para equilibrar a ocupação das estações                                                                               |
| R7         | Relatório mensal para a Câmara: utilização por freguesia e CO₂ evitado                                                                           |
| R8         | O utilizador consulta o histórico de viagens e as faturas                                                                                        |

### Contexto adicional:
| **Código** | **Contexto**                                                                                                                 |
| ---------- | ---------------------------------------------------------------------------------------------------------------------------- |
| C1a        | Frota de 1500 bicicletas (70% elétricas) em 180 estações.                                                                    |
| C1b        | As elétricas enviam telemetria a cada 30 s em andamento.                                                                     |
| C2a        | Cerca de 70% dos desbloqueios acontecem entre as 8h00–9h30 e as 17h30–19h00.                                                 |
| C2b        | Em eventos (Santos Populares, maratona) a procura em algumas estações multiplica por 4.                                      |
| C3a        | As docas comunicam por rede móvel e há zonas onde a rede falha com frequência.                                               |
| C3b        | Cada doca guarda até 24 h de eventos localmente.                                                                             |
| C4         | O contrato de concessão aplica uma multa por cada hora em que mais de 10% das estações não conseguem desbloquear bicicletas. |
| C5a        | As faturas são comunicadas à Autoridade Tributária e guardadas durante 10 anos.                                              |
| C5b        | O percurso de cada viagem é dado pessoal (RGPD).                                                                             |
| C6         | Dentro de um ano, a Câmara quer publicar a ocupação das estações como dados abertos.                                         |
| C7         | Duas câmaras vizinhas mostraram interesse em usar a plataforma com tarifários próprios.                                      |
| C8         | Equipa de desenvolvimento de 6 pessoas e orçamento fixo.                                                                     |

___ 

# 2) Identificação de características 

Depois procedeu-se à identificação do **domínio** e das **características**. Para cada característica identificou-se o tipo, se é explícita ou implícita e deu-se uma breve explicação do que levou à sua identificação. A identificação passou por perceber a qual das seguintes perguntas é que as frases respondiam:

- **O que faz?** - O domínio corresponde ao comportamento do sistema.  
- **O que tem de ser para garantir o seu sucesso?** - As características são as capacidades do sistema.

| **Código**  | **Classificação**                            | **Característica/s**              | **Explícita/Implícita**                                                                                   | **Comentário**                                                                                                                                                                |
| ----------- | -------------------------------------------- | --------------------------------- | --------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| U1 e U2     | Característica                               | Autenticação + Segurança          | Implícita                                                                                                 | Havendo 60 000 assinantes e 15 000 ocasionais por mês, com conta e pagamentos, é necessário um sistema com autenticação e que funcione de forma segura.                       |
| U3, U4 e U5 | Não é característica                         |                                   |                                                                                                           | Poucos utilizadores profissionais que acabam por não gerar uma característica.                                                                                                |
| R1          | Domínio                                      |                                   |                                                                                                           |                                                                                                                                                                               |
| R2          | Domínio                                      |                                   |                                                                                                           |                                                                                                                                                                               |
| R3          | Domínio                                      |                                   |                                                                                                           |                                                                                                                                                                               |
| R4          | Domínio                                      |                                   |                                                                                                           |                                                                                                                                                                               |
| R5          | Domínio                                      |                                   |                                                                                                           |                                                                                                                                                                               |
| R6          | Domínio                                      |                                   |                                                                                                           |                                                                                                                                                                               |
| R7          | Domínio                                      |                                   |                                                                                                           |                                                                                                                                                                               |
| R8          | Domínio                                      |                                   |                                                                                                           |                                                                                                                                                                               |
| C1a         | Não é característica (é um dado de dimensão) |                                   |                                                                                                           |                                                                                                                                                                               |
| C1b         | Característica                               | Desempenho                        | Explícita                                                                                                 | Bicicletas elétricas a enviar 1 mensagem de telemetria a cada 30s, precisam de ter um bom desempenho.                                                                         |
| C2a         | Característica                               | Elasticidade                      | Explícita                                                                                                 | A carga concentra-se em duas janelas diárias e depois desce. São picos previsíveis que sobem e descem.                                                                        |
| C2b         | Característica                               | Elasticidade                      | Explícita                                                                                                 | Picos pontuais em algumas estações.                                                                                                                                           |
| C3a         | Característica                               | Robustez + Disponibilidade        | Explícita                                                                                                 | A doca perde rede com frequência e o sistema tem de continuar a funcionar. Uma doca sem rede que não desbloqueia conta para a multa.                                          |
| C3b         | Característica                               | Recuperabilidade                  | Implícita                                                                                                 | Quando a rede volta, os eventos guardados chegam atrasados e fora de ordem. O sistema tem de reconstruir o estado correto.                                                    |
| C4          | Característica                               | Disponibilidade + Suportabilidade | Explícita (Disponibilidade) + Implícita (Suportabilidade - Medir as horas de multa e diagnosticar docas.) | Obrigação contratual com a métrica de em cada hora, no máximo 10% das estações podem ficar sem desbloquear. Para saber se a multa se aplica é preciso medir isto hora a hora. |
| C5a         | Característica                               | Legal + Arquivabilidade           | Explícita                                                                                                 | Obrigação legal de comunicar à Autoridade Tributária e conservar as faturas durante 10 anos.                                                                                  |
| C5b         | Característica                               | Privacidade                       | Explícita                                                                                                 | O percurso é um dado pessoal e tem acesso restrito e por isso, não pode sair nos relatórios.                                                                                  |
| C6          | Característica                               | Interoperabilidade                | Explícita                                                                                                 | Publicar dados para terceiros.                                                                                                                                                |
| C7          | Característica                               | Extensibilidade                   | Explícita                                                                                                 | Novos tarifários próprios por câmara.                                                                                                                                         |
| C8          | Característica                               | Simplicidade + Viabilidade        | Explícita                                                                                                 | Equipa pequena e orçamento fixo.                                                                                                                                              |

___

# 3) Característica de Design ou Arquitetura?

 Depois da primeira identificação, faltou perceber se as características candidatas são de design ou de arquitetura. Para isso, procurou-se perceber em qual dos **3 critérios** falha, fazendo as seguintes perguntas: 
 
 1. **Está fora do domínio?**
 2. **Obriga a uma decisão estrutural?**
 3. **É crítica para o sucesso?**

| **Código** | **Design/Arquitetura** | **Justificação**                                                                                                                                                                                                                                                                                        |
| ---------- | ---------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| U1 e U2    | Design                 | A segurança e autenticação dos pagamentos são necessárias, mas sem impacto na estrututa arquitetural do sistema                                                                                                                                                                                         |
| C1a        | Não arquitetura        | É apenas um dado usado para avaliar outras características.                                                                                                                                                                                                                                             |
| C1b        | Arquitetural           | A quantidade de mensagens a serem enviadas por segundo não exigem uma estrutura própria completa. Cerca de 1050 bicicletas a enviar 1 mensagem a cada 30 segundos dá no máximo 35 mensagens por segundo, que é uma carga baixa. De qualquer, maneira vamos considerar como característica arquitetural. |
| C2a e C2b  | Arquitetural           | Obriga a garantir separação entre o caminho do desbloqueio da bicicleta das partes de leitura.                                                                                                                                                                                                          |
| C3a        | Arquitetural           | Obriga a decidir o que a doca faz sozinha e o que depende do servidor.                                                                                                                                                                                                                                  |
| C3b        | Arquitetural           | Obriga a uma receção de eventos e a uma ordem de processamento definida.                                                                                                                                                                                                                                |
| C4         | Arquitetural           | O desbloqueio não pode depender, no momento, de partes que podem falhar.                                                                                                                                                                                                                                |
| C5a        | Arquitetural           | Faturas com comunicação e separada da cobrança.                                                                                                                                                                                                                                                         |
| C5b        | Arquitetural           | O percurso tem de ficar separado dos restantes dados da viagem com acesso próprios.                                                                                                                                                                                                                     |
| C6         | Arquitetural           |                                                                                                                                                                                                                                                                                                         |
| C7         | Arquitetural           | As regras dos tarifários têm de estar isoladas, com um ponto onde se acrescentam tarifários novos.                                                                                                                                                                                                      |
| C8         | Arquitetural           | Favorece soluções simples, com poucas partes a implantar e manter.                                                                                                                                                                                                                                      |

___

# 4) Redução da lista de característica a 7 candidatas

Depois juntaram-se as características repetidas e reduziu-se a lista a **7 candidatas** (não ordenadas). Para cada uma escreveu-se uma definição para este sistema, para evitar ambiguidades.

1. **Disponibilidade** - Em qualquer hora, pelo menos 90% das 180 estações conseguem desbloquear bicicletas.
2. **Robustez** - Uma doca sem rede continua a permitir desbloqueios e devoluções, guardando os eventos localmente.
3. **Recuperabilidade** - Quando a rede volta, os eventos guardados são processados de forma a que cada viagem fique registada e cobrada exatamente uma vez.
4. **Elasticidade** - O sistema absorve os picos diários e os picos de eventos sem degradar o desbloqueio, e sem pagar a capacidade do pico o dia inteiro.
5. **Extensibilidade** - É possível acrescentar um tarifário novo de outra câmara sem alterar os componentes existentes.
6. **Arquivabilidade** - As faturas são comunicadas e conservadas, inalteradas, durante 10 anos.
7. **Privacidade** - O percurso só é acessível a quem precisa.

___

# 5) Identificação de uma característica composta e decomposição da mesma

Depois identificou-se uma característica composta e decompôs-se tal como pedido no enunciado.

A frase C4 pede apenas **disponibilidade**: "o desbloqueio tem de funcionar sempre". Na realidade isto significa várias capacidades:

- **Disponibilidade** - Em cada hora, pelo menos 90% estações conseguem desbloquear. (C4)
- **Robustez** - Uma doca sem rede continua a operar. (C3a) 
- **Recuperabilidade** - Quando a rede volta, a sincronização não perde nem duplica viagens. (C3b)
- **Elasticidade** - Os picos não impedem desbloqueios.
- **Suportabilidade** - Saber, hora a hora, quantas estações conseguem desbloquear. Sem isso não se consegue provar nem contestar a multa. (C4)

___

# 6) As características condutoras da arquitetura

Depois escolheram-se as 3 características que conduzem a arquitetura, sem as ordenar. Cada candidata foi avaliada por três perguntas: 

- **Qual o custo da falha?**
- **Qual o impacto na estrutura?**
- **É específica deste sistema?**

Criou-se uma tabela para as 7 características:

| **Característica** | **Custo da falha**                                       | **Impacto estrutural** | **Específica do kata**              |
| ------------------ | -------------------------------------------------------- | ---------------------- | ----------------------------------- |
| Disponibilidade    | Alto (multa)                                             | Alto                   | Alta (métrica no contrato)          |
| Robustez           | Alto (multa)                                             | Alto                   | Alta                                |
| Recuperabilidade   | Alto (cobranças duplicadas ou perdidas, viagens abertas) | Alto                   | Alta                                |
| Elasticidade       | Médio (a frota limita os desbloqueios)                   | Médio                  | Média                               |
| Extensibilidade    | Médio (perder novas câmaras)                             | Alto                   | Alta (já tem 2 tarifários)          |
| Arquivabilidade    | Alto (legal)                                             | Médio                  | Baixa (qualquer sistema que fatura) |
| Privacidade        | Alto (RGPD - legal)                                      | Médio                  | Média                               |

Fazendo uma análise da tabela, as 3 características mais críticas (top 3) são:

>**Disponibilidade**
>**Recuperabilidade**
>**Extensibilidade**

Outras consideradas e motivos:

1. **Robustez** - Parte dela vive na doca, que está fora da plataforma. A parte que conta para a multa já está na disponibilidade, e a consequência do lado do servidor é a recuperabilidade.
2. **Elasticidade** - A frota física limita os desbloqueios simultâneos, por isso o pico pesa sobretudo nas consultas ao mapa.
3. **Arquivabilidade** e **Privacidade** - São obrigatórias, mas resolvem-se com decisões sobre os dados (separação e agregação) sem conduzir a estrutura geral.
4. **Desempenho**, **Escalabilidade** e **Interoperabilidade** - Cortadas pelos motivos dos pontos 2 e 3.
5. **Simplicidade** e **Viabilidade** - Tratada como restrição presente em todos os trade-offs.
6. **Segurança**, **Autenticação** e **Suportabilidade** - Implícitas e necessárias, mas com menos impacto estrutural do que as top 3. A suportabilidade entra na decomposição do ponto 6.

Como pedido no enunciado, os **trade-offs** entre as top 3:

- **Disponibilidade** vs **Recuperabilidade** - Permitir desbloqueios com a doca sem rede aumenta a disponibilidade, mas cria eventos que é preciso reconciliar mais tarde. Mais de uma exige mais da outra.
- **Disponibilidade** vs **Cobrança Imediata** - O desbloqueio não pode esperar pela cobrança, por isso a cobrança acontece depois.
- **Extensibilidade** vs **Simplicidade** - Pontos de extensão custam código e testes a uma equipa de 6. A extensibilidade fica limitada ao tarifário, sem generalizar tudo.

___

# 7) Cenários para cada característica

Por fim, escreveu-se um cenário concreto para cada uma das top 3.
### Disponibilidade
| **Situação**                                                                                         | **Condições**                                    | **Resposta**                                             | **Medida**                                                                             |
| ---------------------------------------------------------------------------------------------------- | ------------------------------------------------ | -------------------------------------------------------- | -------------------------------------------------------------------------------------- |
| Dia útil às 8h30 (pico, C2a), uma zona da cidade com 20 estações perde a rede móvel durante 2 horas. | As restantes 160 estações funcionam normalmente. | As estações sem rede continuam a desbloquear bicicletas. | Em nenhuma das horas mais de 18 estações (10% de 180) ficam sem conseguir desbloquear. |

### Recuperabilidade
| **Situação**                                                                                                                                                                                       | **Condições**                                                    | **Resposta**                                                                                | **Medida**                                                                   |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------- | ------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------- |
| Uma doca numa zona sem rede regista 12 horas de desbloqueios e devoluções. Quando a rede volta, reenvia todos os eventos, e alguns chegam duas vezes porque o primeiro envio não teve confirmação. | Eventos fora de ordem e repetidos, dentro do limite de 24 horas. | Cada viagem fica registada e cobrada uma única vez. Um evento repetido não altera o estado. | 0 viagens duplicadas, 0 cobranças duplicadas e 100% dos eventos processados. |

### Extensibilidade
| **Situação**                                                                                          | **Condições**                               | **Resposta**                                                                                                                              | **Medida**                                                                                                 |
| ----------------------------------------------------------------------------------------------------- | ------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| Uma câmara vizinha adere com tarifário próprio, por exemplo 30 minutos grátis e blocos de 10 minutos. | A plataforma já está em produção em Lisboa. | O novo tarifário é acrescentado sem alterar os componentes de desbloqueio, viagem e cobrança, e os tarifários de Lisboa continuam iguais. | Só mudam ficheiros dentro do componente de tarifação, e os testes existentes de Lisboa continuam a passar. |
