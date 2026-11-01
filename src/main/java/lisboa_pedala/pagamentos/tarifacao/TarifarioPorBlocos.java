package lisboa_pedala.pagamentos.tarifacao;

import java.time.Duration;

/**
 * Minutos grátis por viagem e depois um preço por cada bloco de tempo iniciado.
 * Lisboa, assinante: 45 min grátis e blocos de 15 min (enunciado, requisito 3).
 */
record TarifarioPorBlocos(int minutosGratis, int minutosPorBloco, long centimosPorBloco) implements Tarifario {

    TarifarioPorBlocos {
        if (minutosGratis < 0 || minutosPorBloco <= 0 || centimosPorBloco < 0) {
            throw new IllegalArgumentException("Parâmetros de tarifário inválidos");
        }
    }

    @Override
    public long valorCentimos(Duration duracao) {
        long segundosPagos = duracao.toSeconds() - minutosGratis * 60L;
        if (segundosPagos <= 0) {
            return 0;
        }
        long segundosPorBloco = minutosPorBloco * 60L;
        long blocos = (segundosPagos + segundosPorBloco - 1) / segundosPorBloco; // bloco iniciado conta inteiro
        return blocos * centimosPorBloco;
    }
}
