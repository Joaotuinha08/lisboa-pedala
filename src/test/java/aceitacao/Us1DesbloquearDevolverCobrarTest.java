package aceitacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import simuladores.ClienteRest;
import simuladores.SimuladorDoca;

/**
 * US-1 (obrigatória): como assinante, desbloqueio e devolvo uma bicicleta e a viagem fica registada e cobrada.
 * Envolve Desbloqueio, Identificação, Receção de Eventos, Registo de Viagens, Tarifação, Cobrança,
 * Faturação, Mapa de Ocupação e Consulta de Histórico.
 */
class Us1DesbloquearDevolverCobrarTest {

    @TempDir
    Path pasta;

    private Plataforma p;
    private SimuladorDoca origem;
    private SimuladorDoca destino;
    private final Instant t0 = Instant.parse("2026-11-02T08:30:00Z");

    @BeforeEach
    void arrancar() {
        p = Plataforma.arrancar(pasta);
        origem = new SimuladorDoca(p.rest, "E001", "E001-D01", 1);
        destino = new SimuladorDoca(p.rest, "E002", "E002-D05", 2);
    }

    @AfterEach
    void parar() {
        p.close();
    }

    @Test
    void assinante_desbloqueia_devolve_e_a_viagem_fica_registada_cobrada_e_faturada() {
        int bicicletasOrigem = p.estacao("E001").path("bicicletas").asInt();
        int bicicletasDestino = p.estacao("E002").path("bicicletas").asInt();

        // 1. Desbloqueio com a app (QR). Só passa por Identificação.
        ClienteRest.Resposta autorizacao = origem.desbloquear("B0001", "APP", "QR-A1", t0);
        assertEquals(200, autorizacao.estado());
        assertEquals("u-assinante-1", autorizacao.corpo().path("utilizadorId").asText());

        // 2. Evento DESBLOQUEIO -> viagem aberta.
        JsonNode viagem = p.historico("u-assinante-1").path("viagens").get(0);
        assertEquals("ABERTA", viagem.path("estado").asText());

        // 3. Evento ENCAIXE 62 min depois -> viagem fechada com valor (45 grátis + 2 blocos de 15 min).
        destino.encaixar("B0001", t0.plus(Duration.ofMinutes(62)));
        viagem = p.historico("u-assinante-1").path("viagens").get(0);
        assertEquals("FECHADA", viagem.path("estado").asText());
        assertEquals("E002", viagem.path("estacaoDestino").asText());
        assertEquals(100, viagem.path("valorCentimos").asLong());

        // O fecho só regista o pedido de cobrança: ainda não falou com o fornecedor de pagamentos.
        String viagemId = viagem.path("viagemId").asText();
        assertEquals("PENDENTE", p.get("/viagens/" + viagemId + "/cobranca").path("estado").asText());

        // 4. Cobrança -> Faturação -> AT.
        p.app.processarPagamentos();
        assertEquals("FATURADO", p.get("/viagens/" + viagemId + "/cobranca").path("estado").asText());
        List<JsonNode> faturas = Plataforma.lista(p.historico("u-assinante-1").path("faturas"));
        assertEquals(1, faturas.size());
        assertEquals(100, faturas.get(0).path("valorCentimos").asLong());
        assertTrue(faturas.get(0).path("comunicadaAT").asBoolean());

        // Reprocessar não cobra nem fatura outra vez.
        p.app.processarPagamentos();
        assertEquals(1, p.historico("u-assinante-1").path("faturas").size());

        // Mapa de ocupação acompanhou a viagem.
        assertEquals(bicicletasOrigem - 1, p.estacao("E001").path("bicicletas").asInt());
        assertEquals(bicicletasDestino + 1, p.estacao("E002").path("bicicletas").asInt());
    }

    @Test
    void viagem_de_assinante_dentro_dos_45_minutos_fica_isenta_e_sem_fatura() {
        origem.desbloquear("B0002", "NAVEGANTE", "NAV-0001", t0);
        destino.encaixar("B0002", t0.plus(Duration.ofMinutes(45)));
        p.app.processarPagamentos();

        JsonNode historico = p.historico("u-assinante-1");
        assertEquals(0, historico.path("viagens").get(0).path("valorCentimos").asLong());
        String viagemId = historico.path("viagens").get(0).path("viagemId").asText();
        assertEquals("ISENTO", p.get("/viagens/" + viagemId + "/cobranca").path("estado").asText());
        assertEquals(0, historico.path("faturas").size());
    }

    @Test
    void ocasional_paga_por_viagem() {
        origem.desbloquear("B0003", "APP", "QR-O1", t0);
        destino.encaixar("B0003", t0.plus(Duration.ofMinutes(10)));
        p.app.processarPagamentos();

        JsonNode historico = p.historico("u-ocasional-1");
        assertEquals(200, historico.path("viagens").get(0).path("valorCentimos").asLong());
        assertEquals(1, historico.path("faturas").size());
    }

    @Test
    void credencial_desconhecida_e_recusada_e_nao_abre_viagem() {
        ClienteRest.Resposta r = origem.desbloquear("B0004", "APP", "QR-NAO-EXISTE", t0);
        assertEquals(403, r.estado());
        assertFalse(r.corpo().path("autorizado").asBoolean());
        assertEquals(0, origem.eventosGuardados());
    }
}
