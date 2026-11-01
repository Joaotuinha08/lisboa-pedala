package lisboa_pedala.pagamentos.tarifacao;

import java.time.Duration;

/** Preço fixo por viagem. Lisboa, ocasional: paga por viagem (enunciado, requisito 3). */
record TarifarioPorViagem(long centimosPorViagem) implements Tarifario {

    @Override
    public long valorCentimos(Duration duracao) {
        return centimosPorViagem;
    }
}
