# Reflexão

## Sobre o trabalho

Gostámos bastante do que foi pedido. Foi divertido perceber o processo inicial de arquitetar um sistema - partir das frases do enunciado, descobrir características que não estavam escritas e chegar a componentes que fizessem sentido.

O tempo foi o fator que mais pesou na qualidade do trabalho. As primeiras partes (características, componentes e particionamento) foram feitas com calma e conseguimos justificar cada escolha. Nas partes finais, sobretudo nas mais ligadas à implementação, tivemos de recorrer bastante à IA e às vezes sentimo-nos um bocado perdidos, o que não é bom - código que não percebemos bem é código que não conseguimos defender. Mesmo assim, foi uma forma de programar que nunca tínhamos experimentado - escrever primeiro uma especificação precisa e deixar que seja ela a guiar quem implementa.

## O que a IA fez bem

- Respeitou a tabela allowed-to-use - nenhum componente importa um componente fora da sua linha e não há ciclos.
- Cada componente tem uma só interface pública e o resto é package-private, como pede o 5 do ARCHITECTURE.md.
- A unidade de trabalho do 4 ficou numa transação JDBC reentrante (`BaseDados.emTransacao`). Marcar o evento, fechar a viagem e registar a cobrança são desfeitos em conjunto se algo falhar.
- A recuperabilidade ficou toda na Receção de Eventos - identificador único por evento, ordenação pela hora da doca e encaixes guardados como pendentes quando chegam antes do desbloqueio.
- O desbloqueio não depende de pagamentos e a cobrança corre depois do fecho, com nova tentativa.
- Cada tarifário é uma implementação separada dentro da Tarifação, e há um teste para o cenário da câmara nova.
- Os testes das user stories usam cenários realistas: 12 h de eventos baralhados e repetidos, e falhas do fornecedor de pagamentos.

## O que a IA errou

Todos os casos abaixo passaram nas fitness functions. Foram detetados numa revisão do código feita depois da geração.

1. **Pacote `partilhado` fora da especificação.** A IA criou `lisboa_pedala.partilhado` (BaseDados, Rest, Json, TipoEvento e exceções), que não está no 1, e escreveu a fitness function de forma a abrir-lhe uma exceção. Qualquer componente pode usar este pacote sem regra nenhuma.
2. **Rota `POST /utilizadores`.** Não faz parte de nenhuma user story nem do ARCHITECTURE.md. A IA acrescentou-a para facilitar os testes.
3. **Preços inventados.** Nem o enunciado nem a especificação dão preços. A IA escolheu valores (50 cêntimos por bloco de 15 min e 200 cêntimos por viagem ocasional) e marcou-os como provisórios, mas o comentário diz que a lacuna ficou registada em `prompts/` e nesta reflexão, o que não tinha acontecido.
4. **`TipoCliente` duplicado.** Existe em Identificação e em Tarifação, e o Registo de Viagens converte um no outro com `valueOf(name())`. É uma conascência de nome entre três componentes que não está no ficheiro 02.
5. **Nova tentativa agendada no `Main`.** O processamento das cobranças pendentes é agendado no `Main`, de 5 em 5 segundos. A especificação diz que há nova tentativa, mas não diz quem a dispara.

## O que a especificação não previu

- **Doca sem rede.** No simulador, uma doca sem rede continua a chamar `POST /desbloqueios` para autorizar o utilizador. Sem rede isso não seria possível, e a especificação não diz como a doca autoriza alguém offline.
- **Utilizador não verificado.** A Receção de Eventos aceita o `utilizadorId` que vem no evento sem confirmar que houve uma autorização do Desbloqueio.
- **Contratos dos componentes.** O ARCHITECTURE.md diz o que cada componente faz, mas não as operações, os estados da cobrança (`PENDENTE`, `PAGO`, `FATURADO`, `ISENTO`) nem as rotas REST de que os testes dependem.
- **Localização dos testes.** A especificação pôs as fitness functions em `src/test/java`, seguindo a convenção do Maven, e não em `tests/architecture/` como pede o enunciado. Corrigimos isto passando os testes para `tests/`.

## Fitness functions: o que apanham e o que deixam passar

- **Apanham:** imports entre componentes (RM1 a RM6), ciclos, mais de uma classe pública por componente e tabelas sem o prefixo do componente.
- **Deixam passar:** tudo o que não é um import. Nenhum dos cinco erros acima é visível para o ArchUnit: um pacote novo autorizado na própria regra, uma rota a mais, um valor inventado, um tipo duplicado ou uma responsabilidade no sítio errado. A verificação das tabelas procura nomes depois de `FROM`, `INTO`, `UPDATE` e `JOIN` com uma expressão regular, por isso SQL com palavras-chave em minúsculas ou com o nome da tabela montado por concatenação escapa.

## Riscos para o teste de regeneração

- **Tarifação.** Os testes da US-1 esperam exatamente 100 e 200 cêntimos, mas os preços só estão no código. Uma regeneração a partir do ARCHITECTURE.md escolheria outros valores e os testes falhariam. Os preços têm de passar para o ARCHITECTURE.md ou para o ADR-006.
- **Cobrança e Receção de Eventos.** Sem contratos, a IA pode gerar operações e estados com outros nomes. Uma secção curta de contratos por componente reduz a variação entre regenerações.
- **Pacote `partilhado`.** Como não está na especificação, a IA só descobre que tem de usar a `BaseDados` e o `Rest` se ler o resto do código. Uma regeneração pode criar versões próprias dessas classes dentro do componente.