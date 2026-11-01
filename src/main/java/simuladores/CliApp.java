package simuladores;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * CLI da app (simulada). Fala com a plataforma apenas por REST.
 *
 * <pre>
 *   java -cp ... simuladores.CliApp demo                 # US-1, US-3 e US-2 contra uma plataforma a correr
 *   java -cp ... simuladores.CliApp historico u-assinante-1
 *   java -cp ... simuladores.CliApp mapa | ordens | disponibilidade | relatorio 2026-11
 * </pre>
 * URL da plataforma: -Dplataforma=http://127.0.0.1:8080 (por omissão).
 */
public final class CliApp {

    private CliApp() {
    }

    public static void main(String[] args) {
        ClienteRest cliente = new ClienteRest(System.getProperty("plataforma", "http://127.0.0.1:8080"));
        String comando = args.length == 0 ? "ajuda" : args[0];
        switch (comando) {
            case "demo" -> demo(cliente);
            case "historico" -> imprimir(cliente.get("/utilizadores/" + args[1] + "/historico"));
            case "mapa" -> imprimir(cliente.get("/mapa"));
            case "ordens" -> imprimir(cliente.get("/ordens"));
            case "disponibilidade" -> imprimir(cliente.get("/disponibilidade"));
            case "relatorio" -> imprimir(cliente.get("/relatorios/" + args[1]));
            default -> System.out.println("Comandos: demo | historico <utilizador> | mapa | ordens | disponibilidade"
                    + " | relatorio <AAAA-MM>");
        }
    }

    private static void demo(ClienteRest cliente) {
        Instant agora = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        SimuladorDoca origem = new SimuladorDoca(cliente, "E001", "E001-D01", 1);
        SimuladorDoca destino = new SimuladorDoca(cliente, "E002", "E002-D03", 2);

        System.out.println("== US-1: assinante desbloqueia (QR) e devolve 62 min depois");
        Instant inicio = agora.minus(Duration.ofMinutes(62));
        imprimir(origem.desbloquear("B0001", "APP", "QR-A1", inicio));
        new SimuladorBicicleta(cliente, "B0001")
                .pedalar(inicio, Duration.ofMinutes(60), 38.7139, -9.1394, 38.7369, -9.1427);
        destino.encaixar("B0001", agora);

        System.out.println("== US-3: doca sem rede guarda eventos e reenvia-os com repetidos e fora de ordem");
        destino.perderRede();
        Instant t = agora.minus(Duration.ofHours(12));
        for (int i = 0; i < 4; i++) {
            String bicicleta = "B01" + i;
            destino.desbloquear(bicicleta, "NAVEGANTE", "NAV-0002", t.plus(Duration.ofHours(i * 3)));
            destino.encaixar(bicicleta, t.plus(Duration.ofHours(i * 3)).plus(Duration.ofMinutes(20 + i * 10)));
        }
        System.out.println("Eventos guardados na doca: " + destino.eventosGuardados());
        List<JsonNode> respostas = destino.recuperarRedeComFalhas(0.3);
        respostas.forEach(r -> System.out.println("  " + r));

        System.out.println("== US-2: bicicleta reporta bateria fraca e travões avariados");
        SimuladorBicicleta b = new SimuladorBicicleta(cliente, "B0420");
        b.bateria(8);
        b.avariarTravoes();
        imprimir(b.enviar(agora, null, null));
        imprimir(cliente.get("/ordens"));

        System.out.println("A cobrança corre em segundo plano (5 s). Consulte depois: historico u-assinante-1");
    }

    private static void imprimir(ClienteRest.Resposta r) {
        System.out.println(r.estado() + " " + r.corpo().toPrettyString());
    }
}
