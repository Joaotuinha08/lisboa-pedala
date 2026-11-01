package lisboa_pedala.viagens.percursos;

import java.time.Instant;
import lisboa_pedala.partilhado.BaseDados;
import lisboa_pedala.viagens.registo_viagens.RegistoViagens;

/**
 * Percursos: guarda o trajeto de cada viagem como dado pessoal (RGPD), com acesso e retenção próprios.
 * Pode usar: registo_viagens. Só Telemetria e Relatórios Municipais o podem usar (ADR-007, RM4).
 *
 * <p>As posições nunca saem deste componente: para fora só sai a distância de cada viagem (§5, regra 4).
 */
public interface Percursos {

    static Percursos criar(BaseDados bd, RegistoViagens registo) {
        return new PercursosServico(new PercursosRepositorio(bd), registo);
    }

    /** Guarda a posição se a bicicleta estiver numa viagem. Devolve false se não estiver (posição descartada). */
    boolean registarPosicao(Posicao posicao);

    /** Distância percorrida na viagem, em metros. */
    double distanciaMetros(String viagemId);

    /** Retenção: apaga as posições registadas antes do limite. Devolve quantas apagou. */
    int apagarPosicoesAnterioresA(Instant limite);

    record Posicao(String bicicletaId, Instant hora, double latitude, double longitude) {
    }
}
