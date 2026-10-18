
1) Para facilitar a organização e identificação das características foi atribuído um código a cada frase: utilizadores (U1-U5), requisitos (R1-R8) e contexto adicional (C1-C8). Quando as frases do contexto apresentavam duas ideias elas eram separadas em código diferentes, por exemplo, C1a e C1b.

Utilizadores: 

Perfil : Volume
U1 - Assinantes (passe anual) : ~60 000
U2 - Utilizadores ocasionais : ~15 000 por mês
U3 - Técnicos de manutenção : 25
U4 - Equipas de redistribuição (carrinhas) : 6
U5 - Gestores municipais : ~10


Requisitos:

R1 - Desbloquear uma bicicleta na estação com a app (código QR) ou com o cartão Navegante.
R2 - Devolver em qualquer estação com doca livre; a viagem fecha quando a doca confirma o encaixe.
R3 - Tarifário: o passe anual inclui 45 min grátis por viagem, depois cobra-se por bloco de 15 min; os ocasionais pagam por viagem (MB WAY ou cartão).
R4 - Mapa em tempo real com bicicletas e docas livres por estação.
R5 - Abrir uma ordem de manutenção quando uma bicicleta reporta avaria (bateria, travões) ou um utilizador a reporta.
R6 - Sugerir rotas às carrinhas para equilibrar a ocupação das estações.
R7 - Relatório mensal para a Câmara: utilização por freguesia e CO₂ evitado.
R8 - O utilizador consulta o histórico de viagens e as faturas.


Contexto adicional:

C1a - Frota de 1500 bicicletas (70% elétricas) em 180 estações. 
C1b - As elétricas enviam telemetria a cada 30 s em andamento.
C2a - Cerca de 70% dos desbloqueios acontecem entre as 8h00–9h30 e as 17h30–19h00. 
C2b - Em eventos (Santos Populares, maratona) a procura em algumas estações multiplica por 4.
C3a - As docas comunicam por rede móvel e há zonas onde a rede falha com frequência.
C3b - Cada doca guarda até 24 h de eventos localmente.
C4 - O contrato de concessão aplica uma multa por cada hora em que mais de 10% das estações não conseguem desbloquear bicicletas.
C5a - As faturas são comunicadas à Autoridade Tributária e guardadas durante 10 anos. 
C5b - O percurso de cada viagem é dado pessoal (RGPD).
C6 - Dentro de um ano, a Câmara quer publicar a ocupação das estações como dados abertos.
C7 - Duas câmaras vizinhas mostraram interesse em usar a plataforma com tarifários próprios.
C8 - Equipa de desenvolvimento de 6 pessoas e orçamento fixo.


___ 


2) Depois procedeu-se à identificação do domínio e das características. Para cada característica identificou-se o tipo, com uma breve explicação do que levou à sua identificação. Nas frases de domínio registaram-se as pistas de características que contêm.

- O domínio corresponde ao comportamento do sistema: o que faz?  
- As características são as capacidades do sistema: o que tem de ser para garantir o seu sucesso?


U1 e U2 - Característica implícita. Havendo 60 000 assinantes e 15 000 ocasionais por mês, com conta e pagamentos, estamos perante uma característica de AUTENTICAÇÃO e SEGURANÇA.
U3, U4 e U5 - Poucos utilizadores profissionais que acabam por não gerar uma característica.


R1 - Domínio. O desbloqueio é a operação central.  
R2 - Domínio. A viagem só fecha quando a doca confirma. 
R3 - Domínio. Já hoje existem dois tarifários com regras diferentes.
R4 - Domínio. 
R5 - Domínio.  
R6 - Domínio.  
R7 - Domínio. O relatório usa dados de viagens.  
R8 - Domínio.


C1a - Dado de dimensão.
C1b - Característica. Bicicletas elétricas a enviar 1 mensagem de telemetria a cada 30s, precisam de ter um bom desempenho. A telemetria contém a posição da bicicleta, ou seja, o percurso, logo estamos perante características de DESEMPENHO e PRIVACIDADE.
C2a - Característica. A carga concentra-se em duas janelas diárias e depois desce. São picos previsíveis que sobem e descem, logo ELASTICIDADE.  
C2b - Característica. Picos pontuais em algumas estações, logo ELASTICIDADE.  
C3a - Característica. A doca perde rede com frequência e o sistema tem de continuar a funcionar, logo ROBUSTEZ. Uma doca sem rede que não desbloqueia conta para a multa, logo DISPONIBILIDADE.  
C3b - Característica. Quando a rede volta, os eventos guardados chegam atrasados e fora de ordem O sistema tem de reconstruir o estado correto, logo RECUPERABILIDADE.  
C4 - Característica. Obrigação contratual com a métrica de em cada hora, no máximo 10% das estações podem ficar sem desbloquear, logo DISPONIBILIDADE. Para saber se a multa se aplica é preciso medir isto hora a hora, logo SUPORTABILIDADE. 
C5a - Característica. Obrigação legal de comunicar à Autoridade Tributária e conservar as faturas durante 10 anos, logo LEGAL e ARQUIVABILIDADE.  
C5b - Característica. O percurso é um dado pessoal e tem acesso restrito e por isso, não pode sair nos relatórios, logo PRIVACIDADE.  
C6 - Característica. Publicar dados para terceiros, logo INTEROPERABILIDADE.  
C7 - Característica. Tarifários próprios por câmara, logo EXTENSIBILIDADE.
C8 - Característica. Equipa pequena e orçamento fixo, logo SIMPLICIDADE e VIABILIDADE. 


___


3) Depois de uma primeira identificação, voltou a rever-se as características candidatas para perceber se são de design ou de arquitetura. Para isso procurou-se responder a 3 perguntas para filtrar por critério: está fora do domínio? Obriga a uma decisão estrutural? É crítica para o sucesso? A justificação regista a resposta à segunda pergunta.


U1 e U2 - Design. A segurança e autenticação dos pagamentos são necessárias, mas sem impacto na estrututa arquitetural do sistema.  


C1a - Não arquitetural. É apenas um dado usado para avaliar outras características.  
C1b - Arquitetural.  
C2a e C2b - Arquitetural. Obriga a garantir separação entre o caminho do desbloqueio da bicicleta das partes de leitura.
C3a - Arquitetural. Obriga a decidir o que a doca faz sozinha e o que depende do servidor.  
C3b - Arquitetural. Obriga a uma receção de eventos e a uma ordem de processamento definida.  
C4 - Arquitetural. O desbloqueio não pode depender, no momento, de partes que podem falhar.  
C5a - Arquitetural. Faturas com comunicação e separada da cobrança.  
C5b - Arquitetural. O percurso tem de ficar separado dos restantes dados da viagem com acesso próprios.  
C6 - Arquitetural.  
C7 - Arquitetural. As regras dos tarifários têm de estar isoladas, com um ponto onde se acrescentam tarifários novos.  
C8 - Arquitetural. Favorece soluções simples, com poucas partes a implantar e manter.  


___


4) Depois identificou-se, para cada característica, se é explícita ou implícita.

Disponibilidade - Explícita (C4 dá os dados).  
Robustez - Explícita (C3a).  
Recuperabilidade - Implícita. C3b só diz que a doca guarda eventos. A obrigação de não perder nem duplicar viagens e cobranças é deduzida.  
Elasticidade - Explícita (C2a e C2b).  
Extensibilidade - Explícita (C7).  
Arquivabilidade - Explícita (C5a).  
Privacidade - Explícita (C5b).  
Interoperabilidade - Explícita (C6).  
Simplicidade e Viabilidade - Explícita (C8).  
Escalabilidade - Implícita (deduzida do U1, U2, C1a e do possível crescimento em C7).  
Suportabilidade - Implícita (deduzida do C4, medir as horas de multa e diagnosticar docas).  
Segurança e Autenticação - Implícita (U1 e U2).


___


5) Depois juntaram-se as características repetidas e reduziu-se a lista a 7 candidatas. Para cada uma escreveu-se uma definição para este sistema, para evitar ambiguidades.

1.Disponibilidade - Em qualquer hora, pelo menos 90% das 180 estações conseguem desbloquear bicicletas.
2.Robustez - Uma doca sem rede continua a permitir desbloqueios e devoluções, guardando os eventos localmente.
3.Recuperabilidade - Quando a rede volta, os eventos guardados são processados de forma a que cada viagem fique registada e cobrada exatamente uma vez.
4.Elasticidade - O sistema absorve os picos diários e os picos de eventos sem degradar o desbloqueio, e sem pagar a capacidade do pico o dia inteiro.
5.Extensibilidade - É possível acrescentar um tarifário novo de outra câmara sem alterar os componentes existentes.
6.Arquivabilidade - As faturas são comunicadas e conservadas, inalteradas, durante 10 anos.
7.Privacidade- O percurso só é acessível a quem precisa.


___


6) Depois identificou-se uma característica composta e decompôs-se.

A frase C4 pede apenas disponibilidade: "o desbloqueio tem de funcionar sempre". Na realidade isto significa várias capacidades:

- Disponibilidade (C4): em cada hora, pelo menos 90% estações conseguem desbloquear.
- Robustez (C3a): uma doca sem rede continua a operar.
- Recuperabilidade (C3b): quando a rede volta, a sincronização não perde nem duplica viagens.
- Elasticidade (C2a e C2b): os picos não impedem desbloqueios.
- Suportabilidade (C4): saber, hora a hora, quantas estações conseguem desbloquear. Sem isso não se consegue provar nem contestar a multa.


___


7) Depois escolheram-se as 3 características que conduzem a arquitetura, sem as ordenar. Cada candidata foi avaliada por três perguntas: Qual o custo da falha? Qual o impacto na estrutura? É específica deste sistema?


Disponibilidade
- Custo da falha: Alto (multa)
- Impacto estrutural: Alto    
- Específica do kata: Alta (métrica no contrato)

Robustez
- Custo da falha: Alto (multa)
- Impacto estrutural: Alto
- Específica do kata: Alta

Recuperabilidade
- Custo da falha: Alto (cobranças duplicadas ou perdidas, viagens abertas)
- Impacto estrutural: Alto
- Específica do kata: Alta

Elasticidade
- Custo da falha: Médio (a frota limita os desbloqueios)
- Impacto estrutural: Médio
- Específica do kata: Média

Extensibilidade
- Custo da falha: Médio (perder novas câmaras)
- Impacto estrutural: Alto
- Específica do kata: Alta (já tem 2 tarifários)

Arquivabilidade
- Custo da falha: Alto (legal)
- Impacto estrutural: Médio
- Específica do kata: Baixa (qualquer sistema que fatura)

Privacidade
- Custo da falha: Alto (RGPD)
- Impacto estrutural: Médio
- Específica do kata: Média


Top 3: Disponibilidade, Recuperabilidade, Extensibilidade.


Outras consideradas e motivos:

- Robustez: parte dela vive na doca, que está fora da plataforma. A parte que conta para a multa já está na disponibilidade, e a consequência do lado do servidor é a recuperabilidade.
- Elasticidade: a frota física limita os desbloqueios simultâneos, por isso o pico pesa sobretudo nas consultas ao mapa.
- Arquivabilidade e privacidade: são obrigatórias, mas resolvem-se com decisões sobre os dados (separação, retenção e agregação) sem conduzir a estrutura geral.
- Desempenho, escalabilidade e interoperabilidade: cortadas pelos motivos dos pontos 2 e 3.
- Simplicidade e viabilidade: tratada como restrição presente em todos os trade-offs.
- Segurança, autenticação e suportabilidade: implícitas e necessárias, mas com menos impacto estrutural do que as top 3. A suportabilidade entra na decomposição do ponto 6.


Trade-offs entre as top 3:

- Disponibilidade vs recuperabilidade: permitir desbloqueios com a doca sem rede aumenta a disponibilidade, mas cria eventos que é preciso reconciliar mais tarde. Mais de uma exige mais da outra.
- Disponibilidade vs cobrança imediata: o desbloqueio não pode esperar pela cobrança, por isso a cobrança acontece depois.
- Extensibilidade vs simplicidade: pontos de extensão custam código e testes a uma equipa de 6. A extensibilidade fica limitada ao tarifário, sem generalizar tudo.


___


8) Por fim, escreveu-se um cenário concreto para cada uma das top 3.


Disponibilidade

- Situação: dia útil às 8h30 (pico, C2a), uma zona da cidade com 20 estações perde a rede móvel durante 2 horas.
- Condições: as restantes 160 estações funcionam normalmente.
- Resposta: as estações sem rede continuam a desbloquear bicicletas.
- Medida: em nenhuma das horas mais de 18 estações (10% de 180) ficam sem conseguir desbloquear.

Recuperabilidade

- Situação: uma doca numa zona sem rede regista 12 horas de desbloqueios e devoluções. Quando a rede volta, reenvia todos os eventos, e alguns chegam duas vezes porque o primeiro envio não teve confirmação.
- Condições: eventos fora de ordem e repetidos, dentro do limite de 24 horas.
- Resposta: cada viagem fica registada e cobrada uma única vez. Um evento repetido não altera o estado.
- Medida: 0 viagens duplicadas, 0 cobranças duplicadas e 100% dos eventos processados.

Extensibilidade

- Situação: uma câmara vizinha adere com tarifário próprio, por exemplo 30 minutos grátis e blocos de 10 minutos.
- Condições: a plataforma já está em produção em Lisboa.
- Resposta: o novo tarifário é acrescentado sem alterar os componentes de desbloqueio, viagem e cobrança, e os tarifários de Lisboa continuam iguais.
- Medida: só mudam ficheiros dentro do componente de tarifação, e os testes existentes de Lisboa continuam a passar.


