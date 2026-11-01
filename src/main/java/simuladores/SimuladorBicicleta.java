package simuladores;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Bicicleta elétrica simulada: envia telemetria (estado da bateria, travões e posição) pela API REST. */
public final class SimuladorBicicleta {

    public static final Duration PERIODO = Duration.ofSeconds(30);

    private final ClienteRest cliente;
    private final String bicicletaId;
    private int bateriaPct = 90;
    private boolean travoesOk = true;

    public SimuladorBicicleta(ClienteRest cliente, String bicicletaId) {
        this.cliente = cliente;
        this.bicicletaId = bicicletaId;
    }

    public void bateria(int pct) {
        bateriaPct = pct;
    }

    public void avariarTravoes() {
        travoesOk = false;
    }

    public ClienteRest.Resposta enviar(Instant hora, Double latitude, Double longitude) {
        Map<String, Object> estado = new LinkedHashMap<>();
        estado.put("bicicletaId", bicicletaId);
        estado.put("hora", hora.toString());
        estado.put("latitude", latitude);
        estado.put("longitude", longitude);
        estado.put("bateriaPct", bateriaPct);
        estado.put("travoesOk", travoesOk);
        return cliente.post("/telemetria", estado);
    }

    /** Percorre em linha reta de (lat0,lon0) a (lat1,lon1), uma mensagem a cada 30 s. */
    public void pedalar(Instant inicio, Duration duracao, double lat0, double lon0, double lat1, double lon1) {
        long passos = Math.max(1, duracao.dividedBy(PERIODO));
        for (long i = 0; i <= passos; i++) {
            double f = (double) i / passos;
            enviar(inicio.plus(PERIODO.multipliedBy(i)), lat0 + (lat1 - lat0) * f, lon0 + (lon1 - lon0) * f);
        }
    }
}
