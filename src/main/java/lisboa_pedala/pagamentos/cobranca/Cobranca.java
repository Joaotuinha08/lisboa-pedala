package lisboa_pedala.pagamentos.cobranca;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import lisboa_pedala.pagamentos.faturacao.Faturacao;
import lisboa_pedala.partilhado.BaseDados;
import lisboa_pedala.partilhado.Rest;

/**
 * Cobrança: cobra o valor ao utilizador pelo meio de pagamento (simulado).
 * Pode usar: faturacao. ADR-004, ADR-005.
 *
 * <p>{@link #registarPedido} só grava o pedido (corre dentro da unidade de trabalho do fecho da viagem
 * e nunca fala com o fornecedor de pagamentos). A cobrança propriamente dita acontece depois, em
 * {@link #processarPendentes}, com nova tentativa em caso de falha externa (§4).
 */
public interface Cobranca {

    static Cobranca criar(BaseDados bd, Faturacao faturacao, Clock relogio) {
        return new CobrancaServico(new CobrancaRepositorio(bd), new PagamentosSimulados(), faturacao, relogio);
    }

    /** Regista o pedido de cobrança de uma viagem. Idempotente pela viagem: nunca há dois pedidos. */
    void registarPedido(PedidoCobranca pedido);

    /** Cobra os pedidos pendentes e pede as faturas das cobranças pagas. */
    ResumoProcessamento processarPendentes();

    Optional<EstadoCobranca> estadoDaViagem(String viagemId);

    void publicarRotas(Rest rest);

    record PedidoCobranca(String viagemId, String utilizadorId, long valorCentimos) {
    }

    enum Estado {
        /** Ainda não cobrado (ou cobrança falhada, a repetir: dívida do utilizador). */
        PENDENTE,
        /** Pago no fornecedor, fatura ainda por emitir. */
        PAGO,
        /** Pago e faturado. */
        FATURADO,
        /** Viagem sem custo: nada a cobrar nem a faturar. */
        ISENTO
    }

    record EstadoCobranca(String cobrancaId, String viagemId, String utilizadorId, long valorCentimos, Estado estado,
                          int tentativas, String ultimoErro, Instant registadaEm) {
    }

    record ResumoProcessamento(int pagas, int faturadas, int falhadas) {
    }
}
