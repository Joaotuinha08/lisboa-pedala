package lisboa_pedala.viagens.rececao_eventos;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lisboa_pedala.estacoes.mapa_ocupacao.MapaOcupacao;
import lisboa_pedala.estacoes.mapa_ocupacao.MapaOcupacao.Movimento;
import lisboa_pedala.estacoes.monitorizacao_disponibilidade.MonitorizacaoDisponibilidade;
import lisboa_pedala.partilhado.PedidoInvalido;
import lisboa_pedala.partilhado.Rest;
import lisboa_pedala.partilhado.TipoEvento;
import lisboa_pedala.viagens.rececao_eventos.RececaoEventosRepositorio.Estado;
import lisboa_pedala.viagens.registo_viagens.RegistoViagens;
import lisboa_pedala.viagens.registo_viagens.RegistoViagens.AberturaViagem;
import lisboa_pedala.viagens.registo_viagens.RegistoViagens.FechoViagem;
import lisboa_pedala.viagens.registo_viagens.RegistoViagens.ResultadoFecho;

class RececaoEventosServico implements RececaoEventos {

    /** Ordem de entrega: hora da doca; no mesmo instante, o desbloqueio vem antes do encaixe. */
    static final Comparator<EventoDoca> ORDEM = Comparator
            .comparing(EventoDoca::horaDoca)
            .thenComparing(e -> e.tipo() == TipoEvento.DESBLOQUEIO ? 0 : 1)
            .thenComparing(EventoDoca::docaId)
            .thenComparing(EventoDoca::eventoId);

    private final RececaoEventosRepositorio repositorio;
    private final RegistoViagens registo;
    private final MapaOcupacao mapa;
    private final MonitorizacaoDisponibilidade monitorizacao;
    private final Clock relogio;

    RececaoEventosServico(RececaoEventosRepositorio repositorio, RegistoViagens registo, MapaOcupacao mapa,
                          MonitorizacaoDisponibilidade monitorizacao, Clock relogio) {
        this.repositorio = repositorio;
        this.registo = registo;
        this.mapa = mapa;
        this.monitorizacao = monitorizacao;
        this.relogio = relogio;
    }

    @Override
    public synchronized ResultadoLote receber(List<EventoDoca> eventos) {
        if (eventos == null) {
            throw new PedidoInvalido("Lista de eventos em falta");
        }
        eventos.forEach(RececaoEventosServico::validar);

        // 1. Descartar repetidos: dentro do lote e face ao que já foi recebido antes.
        Set<String> vistosNoLote = new HashSet<>();
        List<EventoDoca> novos = new ArrayList<>();
        int repetidos = 0;
        for (EventoDoca e : eventos) {
            if (!vistosNoLote.add(e.eventoId()) || repositorio.existe(e.eventoId())) {
                repetidos++;
            } else {
                novos.add(e);
            }
        }

        // 2. Ordenar pela hora registada na doca antes de entregar ao Registo de Viagens (ADR-005).
        novos.sort(ORDEM);

        // 3. Tratar cada evento novo numa unidade de trabalho própria.
        int processados = 0;
        List<String> erros = new ArrayList<>();
        for (EventoDoca e : novos) {
            try {
                if (tratar(e)) {
                    processados++;
                }
                if (e.tipo() == TipoEvento.DESBLOQUEIO) {
                    processados += resolverPendentes(e.bicicletaId());
                }
            } catch (RuntimeException ex) {
                // A transação foi desfeita: o evento não fica marcado e será tratado se a doca o reenviar.
                erros.add(e.eventoId() + ": " + ex.getMessage());
            }
        }

        // 4. Contacto da estação (Monitorização de Disponibilidade), pela hora de receção.
        eventos.stream().map(EventoDoca::estacaoId).distinct().forEach(monitorizacao::registarContacto);

        return new ResultadoLote(eventos.size(), novos.size(), repetidos, processados,
                (int) repositorio.contarPendentes(), erros);
    }

    /** Devolve true se o evento ficou processado; false se é um encaixe que ficou pendente. */
    private boolean tratar(EventoDoca e) {
        return repositorio.bd().emTransacao(() -> {
            boolean processado;
            if (e.tipo() == TipoEvento.DESBLOQUEIO) {
                registo.abrir(new AberturaViagem(e.bicicletaId(), e.utilizadorId(), e.estacaoId(), e.horaDoca()));
                processado = true;
            } else {
                ResultadoFecho r = registo.fechar(new FechoViagem(e.bicicletaId(), e.estacaoId(), e.horaDoca()));
                processado = r.fechada();
            }
            repositorio.inserir(e, processado ? Estado.PROCESSADO : Estado.PENDENTE, relogio.instant());
            mapa.registarMovimento(new Movimento(e.estacaoId(), e.tipo(), e.horaDoca()));
            return processado;
        });
    }

    /**
     * Encaixes que chegaram antes do desbloqueio da mesma viagem (vindos de outra doca, que esteve sem rede)
     * ficam pendentes; tenta-se fechá-los sempre que chega um desbloqueio da mesma bicicleta.
     */
    private int resolverPendentes(String bicicletaId) {
        int resolvidos = 0;
        for (EventoDoca p : repositorio.pendentesDaBicicleta(bicicletaId)) {
            boolean fechada = repositorio.bd().emTransacao(() -> {
                ResultadoFecho r = registo.fechar(new FechoViagem(p.bicicletaId(), p.estacaoId(), p.horaDoca()));
                if (r.fechada()) {
                    repositorio.marcarProcessado(p.eventoId());
                }
                return r.fechada();
            });
            if (fechada) {
                resolvidos++;
            }
        }
        return resolvidos;
    }

    private static void validar(EventoDoca e) {
        if (e == null || vazio(e.eventoId()) || e.tipo() == null || vazio(e.docaId()) || vazio(e.estacaoId())
                || vazio(e.bicicletaId()) || e.horaDoca() == null) {
            throw new PedidoInvalido("Evento incompleto: " + e);
        }
        if (e.tipo() == TipoEvento.DESBLOQUEIO && vazio(e.utilizadorId())) {
            throw new PedidoInvalido("Evento DESBLOQUEIO sem utilizador: " + e.eventoId());
        }
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }

    @Override
    public void publicarRotas(Rest rest) {
        rest.post("/eventos", p -> receber(p.corpo(LoteEventos.class).eventos()));
    }
}
