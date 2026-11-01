package lisboa_pedala.viagens.rececao_eventos;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lisboa_pedala.estacoes.mapa_ocupacao.MapaOcupacao;
import lisboa_pedala.estacoes.monitorizacao_disponibilidade.MonitorizacaoDisponibilidade;
import lisboa_pedala.partilhado.BaseDados;
import lisboa_pedala.partilhado.Rest;
import lisboa_pedala.partilhado.TipoEvento;
import lisboa_pedala.viagens.registo_viagens.RegistoViagens;

/**
 * Receção de Eventos das Docas: recebe os eventos das docas, descarta os repetidos e entrega-os por ordem.
 * Pode usar: registo_viagens, mapa_ocupacao, monitorizacao_disponibilidade. ADR-005.
 *
 * <p>É o único componente que guarda identificadores de eventos (Recuperabilidade, C3b). Cada evento novo
 * é tratado numa só transação: marcar como processado + abrir/fechar a viagem (+ pedido de cobrança).
 */
public interface RececaoEventos {

    static RececaoEventos criar(BaseDados bd, RegistoViagens registo, MapaOcupacao mapa,
                                MonitorizacaoDisponibilidade monitorizacao, Clock relogio) {
        return new RececaoEventosServico(new RececaoEventosRepositorio(bd), registo, mapa, monitorizacao, relogio);
    }

    /** Recebe um lote (em tempo real ou reenvio de uma doca que recuperou a rede). */
    ResultadoLote receber(List<EventoDoca> eventos);

    void publicarRotas(Rest rest);

    /**
     * Evento gerado pela doca. {@code eventoId} é único e gerado pela doca; {@code horaDoca} é a hora do
     * relógio da doca. {@code utilizadorId} só é obrigatório no DESBLOQUEIO.
     */
    record EventoDoca(String eventoId, TipoEvento tipo, String docaId, String estacaoId, String bicicletaId,
                      String utilizadorId, Instant horaDoca) {
    }

    record LoteEventos(List<EventoDoca> eventos) {
    }

    /**
     * @param processados eventos tratados até ao fim neste pedido (incluindo encaixes pendentes resolvidos)
     * @param pendentes   encaixes à espera do desbloqueio da mesma viagem (total, depois deste lote)
     */
    record ResultadoLote(int recebidos, int novos, int repetidos, int processados, int pendentes,
                         List<String> erros) {
    }
}
