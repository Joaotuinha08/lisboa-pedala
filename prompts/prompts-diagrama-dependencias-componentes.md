# Prompts usadas para gerar o diagrama de componentes

- **Prompt 1: diagrama de dependências**
    
    - Objetivo: gerar o diagrama Mermaid com as dependências entre componentes
        
    - Prompt:
        
        > Tendo em conta a seguinte tabela de componentes, cria o diagrama em formato Mermaid que corresponda às dependências dos mesmos:
        
    - Tabela fornecida (De, Para, Porquê):
        
        |De|Para|Porquê|
        |---|---|---|
        |Desbloqueio|Identificação de Utilizadores|Validar o utilizador antes de autorizar|
        |Receção de Eventos|Registo de Viagens|Entregar desbloqueios e encaixes já sem repetidos|
        |Receção de Eventos|Mapa de Ocupação|Atualizar bicicletas e docas livres|
        |Receção de Eventos|Monitorização de Disponibilidade|Registar o estado de cada estação|
        |Registo de Viagens|Identificação de Utilizadores|Obter o tipo de cliente da viagem|
        |Registo de Viagens|Tarifação|Calcular o valor ao fechar a viagem|
        |Registo de Viagens|Cobrança|Pedir a cobrança da viagem fechada|
        |Cobrança|Faturação|Emitir a fatura depois da cobrança|
        |Telemetria|Percursos|Enviar a posição de uma bicicleta em viagem|
        |Telemetria|Ordens de Manutenção|Abrir uma ordem quando deteta avaria|
        |Percursos|Registo de Viagens|Saber a que viagem pertence a posição|
        |Consulta de Histórico|Registo de Viagens|Obter as viagens do utilizador|
        |Consulta de Histórico|Faturação|Obter as faturas do utilizador|
        |Redistribuição|Mapa de Ocupação|Obter a ocupação atual das estações|
        |Relatórios Municipais|Registo de Viagens|Obter as viagens do mês por freguesia|
        |Relatórios Municipais|Percursos|Obter a distância de cada viagem (para o CO2)|
        
- **Prompt 2: agrupamento por domínios**
    
    - Objetivo: acrescentar ao diagrama subgraphs com o domínio de cada componente
        
    - Prompt:
        
        > Acrescenta grupos com os domínios a que cada componente pertence. A seguinte tabela contém essa informação:
        
    - Tabela fornecida (Componente, Pacote, Domínio, Responsabilidade, Coesão):
        
        |Componente|Pacote|Domínio|Responsabilidade|Coesão|
        |---|---|---|---|---|
        |Desbloqueio|`desbloqueio`|viagens|Valida o pedido de desbloqueio e autoriza a doca a libertar a bicicleta.|Funcional|
        |Receção de Eventos das Docas|`rececao_eventos`|viagens|Recebe os eventos das docas, descarta os repetidos e entrega-os por ordem.|Sequencial|
        |Registo de Viagens|`registo_viagens`|viagens|Mantém o ciclo de vida de cada viagem, da abertura ao fecho.|Funcional|
        |Percursos|`percursos`|viagens|Guarda o trajeto de cada viagem como dado pessoal, com acesso e retenção próprios.|Funcional|
        |Tarifação|`tarifacao`|pagamentos|Calcula o valor de uma viagem segundo o tarifário da câmara e o tipo de cliente.|Funcional|
        |Cobrança|`cobranca`|pagamentos|Cobra o valor ao utilizador pelo meio de pagamento (simulado).|Funcional|
        |Faturação|`faturacao`|pagamentos|Emite faturas imutáveis, comunica-as à AT e conserva-as durante 10 anos.|Sequencial|
        |Identificação de Utilizadores|`identificacao`|clientes|Identifica o utilizador (app ou Navegante) e o seu tipo de cliente.|Funcional|
        |Consulta de Histórico|`consulta_historico`|clientes|Mostra ao utilizador as suas viagens e faturas.|Funcional|
        |Mapa de Ocupação|`mapa_ocupacao`|estacoes|Mantém, por estação, as bicicletas e as docas livres em cada momento.|Funcional|
        |Monitorização de Disponibilidade|`monitorizacao_disponibilidade`|estacoes|Mede, hora a hora, quantas estações conseguem desbloquear.|Funcional|
        |Redistribuição|`redistribuicao`|estacoes|Sugere rotas às carrinhas a partir da ocupação das estações.|Funcional|
        |Telemetria|`telemetria`|frota|Recebe o estado das bicicletas elétricas e deteta avarias a partir dele.|Sequencial|
        |Ordens de Manutenção|`ordens_manutencao`|frota|Abre ordens de manutenção e entrega-as aos técnicos.|Sequencial|
        |Relatórios Municipais|`relatorios_municipais`|camara|Produz o relatório mensal de utilização por freguesia e de CO2 evitado.||
        