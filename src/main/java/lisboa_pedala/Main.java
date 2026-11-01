package lisboa_pedala;

import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lisboa_pedala.camara.relatorios_municipais.RelatoriosMunicipais;
import lisboa_pedala.clientes.consulta_historico.ConsultaHistorico;
import lisboa_pedala.clientes.identificacao.Identificacao;
import lisboa_pedala.clientes.identificacao.Identificacao.NovoUtilizador;
import lisboa_pedala.clientes.identificacao.Identificacao.TipoCliente;
import lisboa_pedala.estacoes.mapa_ocupacao.MapaOcupacao;
import lisboa_pedala.estacoes.monitorizacao_disponibilidade.MonitorizacaoDisponibilidade;
import lisboa_pedala.estacoes.redistribuicao.Redistribuicao;
import lisboa_pedala.frota.ordens_manutencao.OrdensManutencao;
import lisboa_pedala.frota.telemetria.Telemetria;
import lisboa_pedala.pagamentos.cobranca.Cobranca;
import lisboa_pedala.pagamentos.faturacao.Faturacao;
import lisboa_pedala.pagamentos.tarifacao.Tarifacao;
import lisboa_pedala.partilhado.BaseDados;
import lisboa_pedala.partilhado.Rest;
import lisboa_pedala.viagens.desbloqueio.Desbloqueio;
import lisboa_pedala.viagens.percursos.Percursos;
import lisboa_pedala.viagens.rececao_eventos.RececaoEventos;
import lisboa_pedala.viagens.registo_viagens.RegistoViagens;

/**
 * Arranque da plataforma (ADR-009): uma JVM, uma base SQLite, uma API REST.
 * É a única classe que conhece todos os componentes (RM5): cria-os pela ordem das dependências e liga-os.
 */
public final class Main {

    public static final String CAMARA = "lisboa";

    private Main() {
    }

    public static void main(String[] args) {
        int porta = Integer.parseInt(System.getProperty("porta", "8080"));
        String base = System.getProperty("base", "lisboa_pedala.db");
        Aplicacao app = arrancar(new Configuracao(porta, base, true, true, Clock.systemUTC()));
        System.out.println("Lisboa Pedala a correr em http://127.0.0.1:" + app.porta() + " (base: " + base + ")");
        Runtime.getRuntime().addShutdownHook(new Thread(app::parar));
    }

    /**
     * @param dadosDemo              cria 180 estações e alguns utilizadores de demonstração
     * @param processamentoAutomatico cobra e comunica à AT de 5 em 5 segundos (desligado nos testes)
     */
    public record Configuracao(int porta, String caminhoBase, boolean dadosDemo, boolean processamentoAutomatico,
                               Clock relogio) {
    }

    public static Aplicacao arrancar(Configuracao cfg) {
        BaseDados bd = BaseDados.abrir(cfg.caminhoBase());
        Clock relogio = cfg.relogio();

        // Componentes sem dependências.
        Identificacao identificacao = Identificacao.criar(bd);
        Tarifacao tarifacao = Tarifacao.criar();
        Faturacao faturacao = Faturacao.criar(bd, relogio);
        MapaOcupacao mapa = MapaOcupacao.criar(bd);
        MonitorizacaoDisponibilidade monitorizacao = MonitorizacaoDisponibilidade.criar(bd, relogio);
        OrdensManutencao ordens = OrdensManutencao.criar(bd, relogio);
        // Restantes, pela ordem da tabela "Pode usar" (ARCHITECTURE.md §1).
        Cobranca cobranca = Cobranca.criar(bd, faturacao, relogio);
        RegistoViagens registo = RegistoViagens.criar(bd, identificacao, tarifacao, cobranca, CAMARA);
        Desbloqueio desbloqueio = Desbloqueio.criar(bd, identificacao, relogio);
        RececaoEventos rececao = RececaoEventos.criar(bd, registo, mapa, monitorizacao, relogio);
        Percursos percursos = Percursos.criar(bd, registo);
        Telemetria telemetria = Telemetria.criar(bd, percursos, ordens);
        ConsultaHistorico historico = ConsultaHistorico.criar(registo, faturacao);
        Redistribuicao redistribuicao = Redistribuicao.criar(mapa);
        RelatoriosMunicipais relatorios = RelatoriosMunicipais.criar(bd, registo, percursos);

        if (cfg.dadosDemo()) {
            criarDadosDemo(identificacao, mapa, monitorizacao, relatorios);
        }

        Rest rest = Rest.criar(cfg.porta());
        rest.get("/saude", p -> Map.of("estado", "ok"));
        rest.get("/", p -> Map.of(
                "sistema", "Lisboa Pedala (API REST, sem interface gráfica)",
                "rotas", List.of(
                        "GET  /saude",
                        "GET  /mapa",
                        "GET  /disponibilidade?hora=2026-11-02T08:00:00Z",
                        "GET  /redistribuicao?carrinhas=6",
                        "GET  /ordens",
                        "GET  /utilizadores/{utilizadorId}/historico",
                        "GET  /viagens/{viagemId}/cobranca",
                        "GET  /relatorios/{AAAA-MM}",
                        "POST /utilizadores",
                        "POST /desbloqueios",
                        "POST /eventos",
                        "POST /telemetria",
                        "POST /ordens",
                        "POST /ordens/{ordemId}/conclusao"),
                "utilizadoresDemo", List.of("u-assinante-1", "u-assinante-2", "u-ocasional-1")));
        identificacao.publicarRotas(rest);
        desbloqueio.publicarRotas(rest);
        rececao.publicarRotas(rest);
        cobranca.publicarRotas(rest);
        mapa.publicarRotas(rest);
        monitorizacao.publicarRotas(rest);
        redistribuicao.publicarRotas(rest);
        telemetria.publicarRotas(rest);
        ordens.publicarRotas(rest);
        historico.publicarRotas(rest);
        relatorios.publicarRotas(rest);
        rest.iniciar();

        Aplicacao app = new Aplicacao(bd, rest, cobranca, faturacao);
        if (cfg.processamentoAutomatico()) {
            app.agendarProcessamento();
        }
        return app;
    }

    /** Acesso à plataforma em execução (usado pelo arranque e pelos testes de aceitação). */
    public static final class Aplicacao {
        private final BaseDados bd;
        private final Rest rest;
        private final Cobranca cobranca;
        private final Faturacao faturacao;
        private ScheduledExecutorService agendador;

        private Aplicacao(BaseDados bd, Rest rest, Cobranca cobranca, Faturacao faturacao) {
            this.bd = bd;
            this.rest = rest;
            this.cobranca = cobranca;
            this.faturacao = faturacao;
        }

        public int porta() {
            return rest.porta();
        }

        /** Passo 4 do fluxo US-1, fora do caminho do fecho: cobrar pendentes, faturar e comunicar à AT. */
        public void processarPagamentos() {
            cobranca.processarPendentes();
            faturacao.comunicarPendentesAT();
        }

        private void agendarProcessamento() {
            agendador = Executors.newSingleThreadScheduledExecutor();
            agendador.scheduleWithFixedDelay(() -> {
                try {
                    processarPagamentos();
                } catch (RuntimeException e) {
                    System.err.println("Processamento de pagamentos falhou, repete-se: " + e.getMessage());
                }
            }, 5, 5, TimeUnit.SECONDS);
        }

        public void parar() {
            if (agendador != null) {
                agendador.shutdownNow();
            }
            rest.parar();
            bd.close();
        }
    }

    private static final List<String> FREGUESIAS = List.of(
            "Ajuda", "Alcântara", "Alvalade", "Areeiro", "Arroios", "Avenidas Novas", "Beato", "Belém", "Benfica",
            "Campo de Ourique", "Campolide", "Carnide", "Estrela", "Lumiar", "Marvila", "Misericórdia",
            "Olivais", "Parque das Nações", "Penha de França", "Santa Clara", "Santa Maria Maior",
            "Santo António", "São Domingos de Benfica", "São Vicente");

    /** 180 estações (enunciado: frota de 1 500 bicicletas em 180 estações) e três utilizadores de teste. */
    private static void criarDadosDemo(Identificacao identificacao, MapaOcupacao mapa,
                                       MonitorizacaoDisponibilidade monitorizacao, RelatoriosMunicipais relatorios) {
        for (int i = 1; i <= 180; i++) {
            String id = String.format(Locale.ROOT, "E%03d", i);
            int capacidade = 12 + (i % 3) * 4;
            double lat = 38.70 + (i % 15) * 0.0055;
            double lon = -9.21 + (i / 15) * 0.0095;
            mapa.configurarEstacao(new MapaOcupacao.Estacao(id, "Estação " + i, capacidade, capacidade / 2, lat, lon));
            monitorizacao.registarEstacao(id);
            relatorios.associarFreguesia(id, FREGUESIAS.get(i % FREGUESIAS.size()));
        }
        identificacao.registar(new NovoUtilizador("u-assinante-1", TipoCliente.ASSINANTE, "QR-A1", "NAV-0001"));
        identificacao.registar(new NovoUtilizador("u-assinante-2", TipoCliente.ASSINANTE, "QR-A2", "NAV-0002"));
        identificacao.registar(new NovoUtilizador("u-ocasional-1", TipoCliente.OCASIONAL, "QR-O1", null));
    }
}
