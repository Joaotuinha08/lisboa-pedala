package lisboa_pedala.pagamentos.tarifacao;

import java.time.Duration;

/** Um tarifário concreto (de uma câmara, para um tipo de cliente). */
interface Tarifario {

    long valorCentimos(Duration duracao);
}
