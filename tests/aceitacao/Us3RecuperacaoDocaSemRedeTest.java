package aceitacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import simuladores.SimuladorDoca;

/**
 * US-3: como doca sem rede, reenvio os eventos guardados quando a rede volta, sem duplicar viagens.
 * Cenário de Recuperabilidade (C3b): 12 h de eventos, reenviados baralhados, com repetidos e duas vezes.
 * Esperado: 0 viagens duplicadas, 0 cobranças duplicadas, 100% dos eventos processados.
 */
class Us3RecuperacaoDocaSemRedeTest {

    @TempDir
    Path pasta;

    private Plataforma p;
    private final Instant t0 = Instant.parse("2026-11-02T07:00:00Z");

    @BeforeEach
    void arrancar() {
        p = Plataforma.arrancar(pasta);
    }

    @AfterEach
    void parar() {
        p.close();
    }

    @Test
    void doca_reenvia_12h_de_eventos_baralhados_e_repetidos_sem_duplicar_viagens_nem_cobrancas() {
        SimuladorDoca semRede = new SimuladorDoca(p.rest, "E010", "E010-D02", 42);
        SimuladorDoca comRede = new SimuladorDoca(p.rest, "E011", "E011-D01", 7);
        int ocupacaoE010 = p.estacao("E010").path("bicicletas").asInt();
        int ocupacaoE011 = p.estacao("E011").path("bicicletas").asInt();

        semRede.perderRede();

        // Seis viagens de ida e volta na doca sem rede, ao longo de 12 h (assinante).
        int[] minutos = {20, 50, 70, 30, 95, 15};
        long[] valoresEsperados = {0, 50, 100, 0, 200, 0};
        for (int i = 0; i < minutos.length; i++) {
            Instant inicio = t0.plus(Duration.ofHours(2L * i));
            assertEquals(200, semRede.desbloquear("B10" + i, "NAVEGANTE", "NAV-0002", inicio).estado());
            semRede.encaixar("B10" + i, inicio.plus(Duration.ofMinutes(minutos[i])));
        }
        // Viagem que sai da doca sem rede e acaba numa doca com rede: o ENCAIXE chega antes do DESBLOQUEIO.
        semRede.desbloquear("B200", "APP", "QR-O1", t0.plus(Duration.ofHours(1)));
        comRede.encaixar("B200", t0.plus(Duration.ofMinutes(160)));
        // Viagem que sai de uma doca com rede e acaba na doca sem rede.
        comRede.desbloquear("B201", "APP", "QR-O1", t0.plus(Duration.ofHours(3)));
        semRede.encaixar("B201", t0.plus(Duration.ofMinutes(205)));

        assertEquals(14, semRede.eventosGuardados());
        assertEquals(1, p.historico("u-ocasional-1").path("viagens").size(), "só a viagem com DESBLOQUEIO recebido");

        // A rede volta: reenvio baralhado, ~40% repetidos, e o lote inteiro enviado duas vezes.
        List<JsonNode> respostas = semRede.recuperarRedeComFalhas(0.4);
        JsonNode primeira = respostas.get(0);
        JsonNode segunda = respostas.get(1);
        assertEquals(0, primeira.path("erros").size(), primeira.toString());
        assertEquals(14, primeira.path("novos").asInt());
        assertEquals(15, primeira.path("processados").asInt(), "14 novos + o ENCAIXE que estava pendente");
        assertEquals(0, primeira.path("pendentes").asInt());
        assertEquals(0, segunda.path("novos").asInt());
        assertEquals(segunda.path("recebidos").asInt(), segunda.path("repetidos").asInt());

        // 0 viagens duplicadas: exatamente as viagens feitas, todas fechadas, com o valor certo.
        List<JsonNode> assinante = Plataforma.lista(p.historico("u-assinante-2").path("viagens"));
        assertEquals(6, assinante.size());
        for (int i = 0; i < assinante.size(); i++) {
            assertEquals("FECHADA", assinante.get(i).path("estado").asText());
            assertEquals(valoresEsperados[i], assinante.get(i).path("valorCentimos").asLong());
        }
        List<JsonNode> ocasional = Plataforma.lista(p.historico("u-ocasional-1").path("viagens"));
        assertEquals(2, ocasional.size());
        ocasional.forEach(v -> assertEquals("FECHADA", v.path("estado").asText()));
        assertEquals("E011", ocasional.get(0).path("estacaoDestino").asText());

        // 0 cobranças duplicadas: uma cobrança por viagem, e uma fatura por viagem paga.
        p.app.processarPagamentos();
        p.app.processarPagamentos();
        for (JsonNode v : assinante) {
            assertTrue(p.rest.get("/viagens/" + v.path("viagemId").asText() + "/cobranca").ok());
        }
        assertEquals(3, p.historico("u-assinante-2").path("faturas").size());
        assertEquals(2, p.historico("u-ocasional-1").path("faturas").size());

        // O mapa de ocupação não aplicou os repetidos.
        assertEquals(ocupacaoE010, p.estacao("E010").path("bicicletas").asInt());
        assertEquals(ocupacaoE011, p.estacao("E011").path("bicicletas").asInt());
    }

    @Test
    void reenviar_um_lote_ja_processado_nao_altera_nada() {
        SimuladorDoca doca = new SimuladorDoca(p.rest, "E020", "E020-D01", 3);
        doca.perderRede();
        doca.desbloquear("B300", "APP", "QR-A1", t0);
        doca.encaixar("B300", t0.plus(Duration.ofMinutes(80)));
        List<JsonNode> respostas = doca.recuperarRedeComFalhas(1.0); // tudo em duplicado, enviado duas vezes

        assertEquals(2, respostas.get(0).path("novos").asInt());
        assertEquals(2, respostas.get(0).path("repetidos").asInt());
        assertEquals(4, respostas.get(1).path("repetidos").asInt());
        assertEquals(1, p.historico("u-assinante-1").path("viagens").size());
        p.app.processarPagamentos();
        assertEquals(1, p.historico("u-assinante-1").path("faturas").size());
    }
}
