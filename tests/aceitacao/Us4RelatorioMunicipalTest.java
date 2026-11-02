package aceitacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import simuladores.SimuladorBicicleta;
import simuladores.SimuladorDoca;

/**
 * US-4 (extra): como gestor municipal, obtenho o relatório de utilização por freguesia.
 * Envolve Relatórios Municipais, Registo de Viagens e Percursos (só a distância sai de Percursos).
 */
class Us4RelatorioMunicipalTest {

    @TempDir
    Path pasta;

    @Test
    void relatorio_mensal_conta_viagens_e_km_por_freguesia_de_origem() {
        try (Plataforma p = Plataforma.arrancar(pasta)) {
            Instant inicio = Instant.parse("2026-11-03T08:00:00Z");
            SimuladorDoca doca = new SimuladorDoca(p.rest, "E001", "E001-D02", 5);
            doca.desbloquear("B0050", "APP", "QR-A1", inicio);
            // ~2,5 km para norte durante 20 min de viagem.
            new SimuladorBicicleta(p.rest, "B0050")
                    .pedalar(inicio.plusSeconds(30), Duration.ofMinutes(20), 38.7100, -9.1400, 38.7325, -9.1400);
            new SimuladorDoca(p.rest, "E002", "E002-D02", 6).encaixar("B0050", inicio.plus(Duration.ofMinutes(21)));

            JsonNode relatorio = p.get("/relatorios/2026-11");
            assertEquals(1, relatorio.path("totalViagens").asInt());
            JsonNode linha = relatorio.path("freguesias").get(0);
            assertEquals("Alcântara", linha.path("freguesia").asText()); // freguesia de E001 nos dados demo
            double km = linha.path("quilometros").asDouble();
            assertTrue(km > 2.4 && km < 2.6, "km = " + km);
            assertTrue(linha.path("kgCo2Evitado").asDouble() > 0);
            assertEquals(0, p.get("/relatorios/2026-10").path("totalViagens").asInt());
        }
    }
}
