package lisboa_pedala.pagamentos.faturacao;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** AT simulada (ADR-009). Pode ser configurada para falhar as próximas N comunicações. */
class AdaptadorATSimulado implements AdaptadorAT {

    private final AtomicInteger falhasPorProvocar = new AtomicInteger();
    private final Set<String> recebidas = ConcurrentHashMap.newKeySet();

    void falharProximas(int n) {
        falhasPorProvocar.set(n);
    }

    Set<String> recebidas() {
        return recebidas;
    }

    @Override
    public void comunicar(Faturacao.Fatura fatura) {
        if (falhasPorProvocar.getAndUpdate(n -> Math.max(0, n - 1)) > 0) {
            throw new IllegalStateException("AT indisponível (simulado)");
        }
        recebidas.add(fatura.numero()); // a AT aceita reenvios da mesma fatura sem a duplicar
    }
}
