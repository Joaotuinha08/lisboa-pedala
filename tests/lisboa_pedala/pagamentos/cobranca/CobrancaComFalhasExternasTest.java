package lisboa_pedala.pagamentos.cobranca;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import lisboa_pedala.pagamentos.cobranca.Cobranca.Estado;
import lisboa_pedala.pagamentos.cobranca.Cobranca.PedidoCobranca;
import lisboa_pedala.pagamentos.faturacao.Faturacao;
import lisboa_pedala.partilhado.BaseDados;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** §4 "Falhas externas": a falha do fornecedor não desfaz nada; o pedido fica pendente e repete-se. */
class CobrancaComFalhasExternasTest {

    private final BaseDados bd = BaseDados.abrir(":memory:");
    private final Faturacao faturacao = Faturacao.criar(bd, Clock.systemUTC());
    private final PagamentosSimulados fornecedor = new PagamentosSimulados();
    private final Cobranca cobranca =
            new CobrancaServico(new CobrancaRepositorio(bd), fornecedor, faturacao, Clock.systemUTC());

    @AfterEach
    void fechar() {
        bd.close();
    }

    @Test
    void pagamento_que_falha_fica_pendente_e_e_cobrado_uma_so_vez_quando_o_fornecedor_volta() {
        cobranca.registarPedido(new PedidoCobranca("via-1", "u-1", 150));
        fornecedor.falharProximas(2);

        assertEquals(1, cobranca.processarPendentes().falhadas());
        assertEquals(1, cobranca.processarPendentes().falhadas());
        Cobranca.EstadoCobranca estado = cobranca.estadoDaViagem("via-1").orElseThrow();
        assertEquals(Estado.PENDENTE, estado.estado());
        assertEquals(2, estado.tentativas());

        Cobranca.ResumoProcessamento r = cobranca.processarPendentes();
        assertEquals(1, r.pagas());
        assertEquals(1, r.faturadas());
        assertEquals(Estado.FATURADO, cobranca.estadoDaViagem("via-1").orElseThrow().estado());
        assertEquals(1, fornecedor.movimentos().size());
        assertEquals(1, faturacao.faturasDe("u-1").size());
    }

    @Test
    void registar_duas_vezes_o_mesmo_pedido_nao_duplica_a_cobranca() {
        cobranca.registarPedido(new PedidoCobranca("via-2", "u-2", 200));
        cobranca.registarPedido(new PedidoCobranca("via-2", "u-2", 200));
        cobranca.processarPendentes();
        cobranca.processarPendentes();
        assertEquals(1, fornecedor.movimentos().size());
        assertEquals(1, faturacao.faturasDe("u-2").size());
    }
}
