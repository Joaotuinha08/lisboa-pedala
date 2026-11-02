package aceitacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import simuladores.ClienteRest;
import simuladores.SimuladorBicicleta;

/**
 * US-2: como técnico, recebo uma ordem de manutenção quando uma bicicleta reporta avaria.
 * Envolve Telemetria, Ordens de Manutenção e (para a posição) Percursos.
 */
class Us2OrdemManutencaoTest {

    @TempDir
    Path pasta;

    private Plataforma p;
    private final Instant t0 = Instant.parse("2026-11-02T10:00:00Z");

    @BeforeEach
    void arrancar() {
        p = Plataforma.arrancar(pasta);
    }

    @AfterEach
    void parar() {
        p.close();
    }

    @Test
    void bateria_fraca_reportada_pela_bicicleta_abre_uma_so_ordem_para_o_tecnico() {
        SimuladorBicicleta bicicleta = new SimuladorBicicleta(p.rest, "B0420");
        assertEquals(0, p.get("/ordens").size());

        bicicleta.bateria(8);
        ClienteRest.Resposta r = bicicleta.enviar(t0, 38.72, -9.14);
        assertEquals(200, r.estado());
        assertEquals("BATERIA", r.corpo().path("avariasDetetadas").get(0).asText());

        // A bicicleta continua a reportar a cada 30 s: não pode abrir uma ordem por mensagem.
        bicicleta.enviar(t0.plusSeconds(30), 38.721, -9.141);
        bicicleta.enviar(t0.plusSeconds(60), 38.722, -9.142);

        List<JsonNode> ordens = Plataforma.lista(p.get("/ordens"));
        assertEquals(1, ordens.size());
        assertEquals("B0420", ordens.get(0).path("bicicletaId").asText());
        assertEquals("BATERIA", ordens.get(0).path("tipo").asText());
        assertEquals("TELEMETRIA", ordens.get(0).path("origem").asText());
        assertEquals("ABERTA", ordens.get(0).path("estado").asText());
    }

    @Test
    void travoes_avariados_abrem_outra_ordem_e_o_tecnico_conclui() {
        SimuladorBicicleta bicicleta = new SimuladorBicicleta(p.rest, "B0777");
        bicicleta.bateria(10);
        bicicleta.avariarTravoes();
        JsonNode r = bicicleta.enviar(t0, null, null).corpo();
        assertEquals(2, r.path("ordens").size());
        assertFalse(r.path("posicaoGuardada").asBoolean());
        assertEquals(2, p.get("/ordens").size());

        String ordemId = r.path("ordens").get(0).asText();
        ClienteRest.Resposta concluida = p.rest.post("/ordens/" + ordemId + "/conclusao", Map.of());
        assertEquals("CONCLUIDA", concluida.corpo().path("estado").asText());
        assertEquals(1, p.get("/ordens").size());
    }

    @Test
    void utilizador_tambem_pode_reportar_avaria() {
        ClienteRest.Resposta r = p.rest.post("/ordens",
                Map.of("bicicletaId", "B0099", "tipo", "OUTRA", "descricao", "Corrente solta"));
        assertEquals(201, r.estado());
        assertEquals("UTILIZADOR", r.corpo().path("origem").asText());
        assertTrue(Plataforma.lista(p.get("/ordens")).stream()
                .anyMatch(o -> o.path("bicicletaId").asText().equals("B0099")));
    }

    @Test
    void bicicleta_sem_avaria_nao_abre_ordem() {
        JsonNode r = new SimuladorBicicleta(p.rest, "B0500").enviar(t0, 38.7, -9.1).corpo();
        assertEquals(0, r.path("avariasDetetadas").size());
        assertEquals(0, p.get("/ordens").size());
    }
}
