package lisboa_pedala.estacoes.redistribuicao;

import java.util.List;
import lisboa_pedala.estacoes.mapa_ocupacao.MapaOcupacao;
import lisboa_pedala.partilhado.Rest;

/**
 * Redistribuição: sugere rotas às carrinhas a partir da ocupação das estações.
 * Pode usar: mapa_ocupacao. Sem estado próprio (não tem tabelas).
 */
public interface Redistribuicao {

    static Redistribuicao criar(MapaOcupacao mapa) {
        return new RedistribuicaoServico(mapa);
    }

    /** Sugestões para o número de carrinhas indicado (até {@code bicicletasPorCarrinha} por viagem). */
    List<Rota> sugerirRotas(int carrinhas, int bicicletasPorCarrinha);

    void publicarRotas(Rest rest);

    record Rota(int carrinha, String estacaoOrigem, String estacaoDestino, int bicicletas) {
    }
}
