package lisboa_pedala.viagens.rececao_eventos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lisboa_pedala.clientes.identificacao.Identificacao;
import lisboa_pedala.clientes.identificacao.Identificacao.NovoUtilizador;
import lisboa_pedala.clientes.identificacao.Identificacao.TipoCliente;
import lisboa_pedala.estacoes.mapa_ocupacao.MapaOcupacao;
import lisboa_pedala.estacoes.monitorizacao_disponibilidade.MonitorizacaoDisponibilidade;
import lisboa_pedala.pagamentos.cobranca.Cobranca;
import lisboa_pedala.pagamentos.tarifacao.Tarifacao;
import lisboa_pedala.partilhado.BaseDados;
import lisboa_pedala.partilhado.Rest;
import lisboa_pedala.partilhado.TipoEvento;
import lisboa_pedala.viagens.rececao_eventos.RececaoEventos.EventoDoca;
import lisboa_pedala.viagens.rececao_eventos.RececaoEventos.ResultadoLote;
import lisboa_pedala.viagens.registo_viagens.RegistoViagens;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * §4 "Unidade de trabalho única": marcar o ENCAIXE como processado, fechar a viagem e registar o pedido de
 * cobrança acontecem numa só transação. Se a cobrança falhar a meio, nada fica feito, e o reenvio do mesmo
 * evento fecha e cobra a viagem uma única vez.
 */
class UnidadeDeTrabalhoTest {

    private static final Instant T0 = Instant.parse("2026-11-02T08:00:00Z");

    private final BaseDados bd = BaseDados.abrir(":memory:");
    private final CobrancaQueFalha cobranca = new CobrancaQueFalha();
    private final RegistoViagens registo;
    private final RececaoEventos rececao;

    UnidadeDeTrabalhoTest() {
        Identificacao identificacao = Identificacao.criar(bd);
        identificacao.registar(new NovoUtilizador("u-1", TipoCliente.ASSINANTE, "QR-1", null));
        MapaOcupacao mapa = MapaOcupacao.criar(bd);
        mapa.configurarEstacao(new MapaOcupacao.Estacao("E1", "E1", 10, 5, 0, 0));
        registo = RegistoViagens.criar(bd, identificacao, Tarifacao.criar(), cobranca, "lisboa");
        rececao = RececaoEventos.criar(bd, registo, mapa, MonitorizacaoDisponibilidade.criar(bd, Clock.systemUTC()),
                Clock.systemUTC());
    }

    @AfterEach
    void fechar() {
        bd.close();
    }

    @Test
    void falha_ao_registar_a_cobranca_desfaz_o_fecho_e_o_reenvio_fecha_uma_so_vez() {
        EventoDoca desbloqueio = new EventoDoca("ev-1", TipoEvento.DESBLOQUEIO, "D1", "E1", "B1", "u-1", T0);
        EventoDoca encaixe = new EventoDoca("ev-2", TipoEvento.ENCAIXE, "D1", "E1", "B1", null, T0.plusSeconds(3600));
        rececao.receber(List.of(desbloqueio));

        cobranca.falhar = true;
        ResultadoLote r1 = rececao.receber(List.of(encaixe));
        assertEquals(1, r1.erros().size());
        assertEquals(RegistoViagens.EstadoViagem.ABERTA, registo.viagensDe("u-1").get(0).estado());
        assertEquals(0, cobranca.pedidos.size());

        cobranca.falhar = false;
        ResultadoLote r2 = rececao.receber(List.of(encaixe));          // a doca reenvia
        assertEquals(1, r2.processados());
        ResultadoLote r3 = rececao.receber(List.of(encaixe, encaixe));  // e reenvia outra vez
        assertEquals(2, r3.repetidos());

        assertEquals(RegistoViagens.EstadoViagem.FECHADA, registo.viagensDe("u-1").get(0).estado());
        assertEquals(1, cobranca.pedidos.size());
        assertEquals(50, cobranca.pedidos.get(0).valorCentimos());
    }

    @Test
    void eventos_fora_de_ordem_no_mesmo_lote_sao_entregues_pela_hora_da_doca() {
        EventoDoca encaixe = new EventoDoca("ev-4", TipoEvento.ENCAIXE, "D1", "E1", "B2", null, T0.plusSeconds(600));
        EventoDoca desbloqueio = new EventoDoca("ev-3", TipoEvento.DESBLOQUEIO, "D1", "E1", "B2", "u-1", T0);
        ResultadoLote r = rececao.receber(List.of(encaixe, desbloqueio));
        assertEquals(2, r.processados());
        assertEquals(0, r.pendentes());
        assertEquals(RegistoViagens.EstadoViagem.FECHADA, registo.viagensDe("u-1").get(0).estado());
    }

    /** Cobrança de teste: grava em memória e pode falhar para simular uma falha a meio da transação. */
    private static final class CobrancaQueFalha implements Cobranca {
        boolean falhar;
        final List<PedidoCobranca> pedidos = new ArrayList<>();

        @Override
        public void registarPedido(PedidoCobranca pedido) {
            if (falhar) {
                throw new IllegalStateException("falha simulada a meio da unidade de trabalho");
            }
            pedidos.add(pedido);
        }

        @Override
        public ResumoProcessamento processarPendentes() {
            return new ResumoProcessamento(0, 0, 0);
        }

        @Override
        public Optional<EstadoCobranca> estadoDaViagem(String viagemId) {
            return Optional.empty();
        }

        @Override
        public void publicarRotas(Rest rest) {
        }
    }
}
