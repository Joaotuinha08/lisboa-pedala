package lisboa_pedala.estacoes.mapa_ocupacao;

import java.time.Instant;
import java.util.List;
import lisboa_pedala.partilhado.BaseDados;
import lisboa_pedala.partilhado.Rest;
import lisboa_pedala.partilhado.TipoEvento;

/**
 * Mapa de Ocupação: mantém, por estação, as bicicletas e as docas livres em cada momento.
 * Pode usar: nenhum componente.
 */
public interface MapaOcupacao {

    static MapaOcupacao criar(BaseDados bd) {
        return new MapaOcupacaoServico(new MapaOcupacaoRepositorio(bd));
    }

    void configurarEstacao(Estacao estacao);

    /** Aplica o efeito de um evento de doca já limpo (sem repetidos). */
    void registarMovimento(Movimento movimento);

    List<OcupacaoEstacao> ocupacao();

    void publicarRotas(Rest rest);

    record Estacao(String estacaoId, String nome, int capacidade, int bicicletas, double latitude, double longitude) {
    }

    record Movimento(String estacaoId, TipoEvento tipo, Instant hora) {
    }

    record OcupacaoEstacao(String estacaoId, String nome, int capacidade, int bicicletas, int docasLivres,
                           double latitude, double longitude, Instant atualizadaEm) {
    }
}
