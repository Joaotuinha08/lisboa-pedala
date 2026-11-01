package lisboa_pedala.frota.telemetria;

import java.time.Instant;
import java.util.List;
import lisboa_pedala.frota.ordens_manutencao.OrdensManutencao;
import lisboa_pedala.partilhado.BaseDados;
import lisboa_pedala.partilhado.Rest;
import lisboa_pedala.viagens.percursos.Percursos;

/**
 * Telemetria: recebe o estado das bicicletas elétricas e deteta avarias a partir dele.
 * Pode usar: percursos, ordens_manutencao. ADR-007.
 *
 * <p>A posição recebida é dado pessoal: é entregue logo a Percursos e não fica guardada aqui.
 */
public interface Telemetria {

    /** Abaixo desta percentagem de bateria considera-se avaria de bateria. */
    int BATERIA_MINIMA_PCT = 15;

    static Telemetria criar(BaseDados bd, Percursos percursos, OrdensManutencao ordens) {
        return new TelemetriaServico(new TelemetriaRepositorio(bd), percursos, ordens);
    }

    ResultadoTelemetria receber(EstadoBicicleta estado);

    void publicarRotas(Rest rest);

    /** Mensagem enviada pela bicicleta (a cada 30 s em andamento). Latitude e longitude podem faltar. */
    record EstadoBicicleta(String bicicletaId, Instant hora, Double latitude, Double longitude, int bateriaPct,
                           boolean travoesOk) {
    }

    record ResultadoTelemetria(List<String> avariasDetetadas, List<String> ordens, boolean posicaoGuardada) {
    }
}
