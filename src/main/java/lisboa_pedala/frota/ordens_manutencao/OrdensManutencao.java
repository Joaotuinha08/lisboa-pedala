package lisboa_pedala.frota.ordens_manutencao;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lisboa_pedala.partilhado.BaseDados;
import lisboa_pedala.partilhado.Rest;

/**
 * Ordens de Manutenção: abre ordens de manutenção e entrega-as aos técnicos.
 * Pode usar: nenhum componente.
 */
public interface OrdensManutencao {

    static OrdensManutencao criar(BaseDados bd, Clock relogio) {
        return new OrdensManutencaoServico(new OrdensManutencaoRepositorio(bd), relogio);
    }

    /**
     * Abre uma ordem. Se a bicicleta já tiver uma ordem aberta para o mesmo tipo de avaria, devolve essa
     * (a telemetria repete o estado a cada 30 s e não pode abrir uma ordem por mensagem).
     */
    Ordem abrir(PedidoOrdem pedido);

    /** Ordens por resolver, as mais antigas primeiro: é a lista que os técnicos recebem. */
    List<Ordem> abertas();

    Ordem concluir(String ordemId);

    void publicarRotas(Rest rest);

    enum TipoAvaria { BATERIA, TRAVOES, OUTRA }

    enum Origem { TELEMETRIA, UTILIZADOR }

    enum EstadoOrdem { ABERTA, CONCLUIDA }

    record PedidoOrdem(String bicicletaId, TipoAvaria tipo, Origem origem, String descricao) {
    }

    record Ordem(String ordemId, String bicicletaId, TipoAvaria tipo, Origem origem, String descricao,
                 EstadoOrdem estado, Instant abertaEm, Instant concluidaEm) {
    }
}
