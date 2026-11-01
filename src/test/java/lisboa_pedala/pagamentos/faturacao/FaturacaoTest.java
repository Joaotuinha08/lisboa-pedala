package lisboa_pedala.pagamentos.faturacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import lisboa_pedala.pagamentos.faturacao.Faturacao.Fatura;
import lisboa_pedala.pagamentos.faturacao.Faturacao.PedidoFatura;
import lisboa_pedala.partilhado.BaseDados;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Faturas imutáveis, conservadas 10 anos e comunicadas à AT com nova tentativa. */
class FaturacaoTest {

    private final BaseDados bd = BaseDados.abrir(":memory:");
    private final AdaptadorATSimulado at = new AdaptadorATSimulado();
    private final Faturacao faturacao = new FaturacaoServico(new FaturacaoRepositorio(bd), at,
            Clock.fixed(Instant.parse("2026-11-02T12:00:00Z"), ZoneOffset.UTC));

    @AfterEach
    void fechar() {
        bd.close();
    }

    @Test
    void emitir_e_idempotente_pela_referencia_e_conserva_10_anos() {
        Fatura a = faturacao.emitir(new PedidoFatura("cob-1", "u-1", 100, null));
        Fatura b = faturacao.emitir(new PedidoFatura("cob-1", "u-1", 100, null));
        assertEquals(a.numero(), b.numero());
        assertEquals("FT LP/2026/000001", a.numero());
        assertEquals(Instant.parse("2036-11-02T12:00:00Z"), a.conservarAte());
        assertEquals(1, faturacao.faturasDe("u-1").size());
    }

    @Test
    void faturas_nao_podem_ser_alteradas_nem_apagadas_antes_do_prazo() {
        faturacao.emitir(new PedidoFatura("cob-2", "u-2", 300, null));
        assertThrows(BaseDados.ErroBaseDados.class,
                () -> bd.executar("UPDATE faturacao_faturas SET valor_centimos = 1"));
        assertThrows(BaseDados.ErroBaseDados.class, () -> bd.executar("DELETE FROM faturacao_faturas"));
        assertEquals(300, faturacao.faturasDe("u-2").get(0).valorCentimos());
    }

    @Test
    void falha_da_at_deixa_a_fatura_por_comunicar_ate_a_proxima_ronda() {
        faturacao.emitir(new PedidoFatura("cob-3", "u-3", 50, null));
        at.falharProximas(1);
        assertEquals(0, faturacao.comunicarPendentesAT());
        assertFalse(faturacao.faturasDe("u-3").get(0).comunicadaAT());
        assertEquals(1, faturacao.comunicarPendentesAT());
        assertTrue(faturacao.faturasDe("u-3").get(0).comunicadaAT());
        assertEquals(0, faturacao.comunicarPendentesAT());
    }
}
