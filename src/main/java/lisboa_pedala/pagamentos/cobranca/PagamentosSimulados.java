package lisboa_pedala.pagamentos.cobranca;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Fornecedor de pagamentos simulado (ADR-009). Pode ser configurado para falhar as próximas N cobranças. */
class PagamentosSimulados implements AdaptadorPagamentos {

    private final AtomicInteger falhasPorProvocar = new AtomicInteger();
    private final Map<String, Long> cobrado = new ConcurrentHashMap<>();

    void falharProximas(int n) {
        falhasPorProvocar.set(n);
    }

    /** Movimentos efetivamente feitos no fornecedor, por chave de idempotência. */
    Map<String, Long> movimentos() {
        return cobrado;
    }

    @Override
    public void cobrar(String chaveIdempotencia, String utilizadorId, long valorCentimos) {
        if (falhasPorProvocar.getAndUpdate(n -> Math.max(0, n - 1)) > 0) {
            throw new IllegalStateException("Pagamento recusado pelo fornecedor (simulado)");
        }
        cobrado.putIfAbsent(chaveIdempotencia, valorCentimos);
    }
}
