package lisboa_pedala.pagamentos.tarifacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.Instant;
import lisboa_pedala.pagamentos.tarifacao.Tarifacao.PedidoCalculo;
import lisboa_pedala.pagamentos.tarifacao.Tarifacao.TipoCliente;
import lisboa_pedala.partilhado.PedidoInvalido;
import org.junit.jupiter.api.Test;

/** Tarifário de Lisboa (requisito 3) e cenário de Extensibilidade (C7, ADR-006). */
class TarifacaoTest {

    private static final Instant INICIO = Instant.parse("2026-11-02T08:00:00Z");
    private final Tarifacao tarifacao = Tarifacao.criar();

    private long assinanteLisboa(Duration d) {
        return tarifacao.calcularValor(new PedidoCalculo(INICIO, INICIO.plus(d), TipoCliente.ASSINANTE, "lisboa"));
    }

    @Test
    void assinante_tem_45_minutos_gratis_e_paga_por_bloco_de_15_minutos_iniciado() {
        assertEquals(0, assinanteLisboa(Duration.ofMinutes(10)));
        assertEquals(0, assinanteLisboa(Duration.ofMinutes(45)));
        assertEquals(50, assinanteLisboa(Duration.ofMinutes(45).plusSeconds(1)));
        assertEquals(50, assinanteLisboa(Duration.ofMinutes(60)));
        assertEquals(100, assinanteLisboa(Duration.ofMinutes(61)));
        assertEquals(200, assinanteLisboa(Duration.ofMinutes(105)));
    }

    @Test
    void ocasional_paga_por_viagem() {
        long curta = tarifacao.calcularValor(
                new PedidoCalculo(INICIO, INICIO.plusSeconds(60), TipoCliente.OCASIONAL, "lisboa"));
        long longa = tarifacao.calcularValor(
                new PedidoCalculo(INICIO, INICIO.plus(Duration.ofHours(3)), TipoCliente.OCASIONAL, "Lisboa"));
        assertEquals(200, curta);
        assertEquals(curta, longa);
    }

    @Test
    void camara_sem_tarifario_e_viagem_invertida_sao_recusadas() {
        assertThrows(PedidoInvalido.class, () -> tarifacao.calcularValor(
                new PedidoCalculo(INICIO, INICIO.plusSeconds(60), TipoCliente.ASSINANTE, "almada")));
        assertThrows(PedidoInvalido.class, () -> tarifacao.calcularValor(
                new PedidoCalculo(INICIO, INICIO.minusSeconds(60), TipoCliente.ASSINANTE, "lisboa")));
    }

    @Test
    void camara_nova_com_30_minutos_e_blocos_de_10_so_mexe_dentro_da_tarifacao() {
        // C7: a câmara nova entra só com um registo no catálogo; Lisboa não muda.
        CatalogoTarifarios catalogo = CatalogoTarifarios.padrao();
        catalogo.registar("almada", TipoCliente.ASSINANTE, new TarifarioPorBlocos(30, 10, 40));
        Tarifacao comAlmada = new TarifacaoServico(catalogo);

        assertEquals(0, comAlmada.calcularValor(pedido("almada", 30)));
        assertEquals(40, comAlmada.calcularValor(pedido("almada", 31)));
        assertEquals(80, comAlmada.calcularValor(pedido("almada", 41)));
        assertEquals(0, comAlmada.calcularValor(pedido("lisboa", 45)));
        assertEquals(50, comAlmada.calcularValor(pedido("lisboa", 46)));
    }

    private static PedidoCalculo pedido(String camara, int minutos) {
        return new PedidoCalculo(INICIO, INICIO.plus(Duration.ofMinutes(minutos)), TipoCliente.ASSINANTE, camara);
    }
}
