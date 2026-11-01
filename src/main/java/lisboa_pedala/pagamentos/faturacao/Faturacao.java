package lisboa_pedala.pagamentos.faturacao;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lisboa_pedala.partilhado.BaseDados;

/**
 * Faturação: emite faturas imutáveis, comunica-as à AT e conserva-as durante 10 anos.
 * Pode usar: nenhum componente. ADR-004 (fora do caminho do desbloqueio).
 */
public interface Faturacao {

    static Faturacao criar(BaseDados bd, Clock relogio) {
        return new FaturacaoServico(new FaturacaoRepositorio(bd), new AdaptadorATSimulado(), relogio);
    }

    /**
     * Emite a fatura de uma cobrança. Idempotente pela referência: emitir duas vezes a mesma
     * referência devolve a fatura já emitida.
     */
    Fatura emitir(PedidoFatura pedido);

    /** Comunica à AT (simulada) as faturas ainda não comunicadas. Devolve quantas foram comunicadas. */
    int comunicarPendentesAT();

    List<Fatura> faturasDe(String utilizadorId);

    record PedidoFatura(String referencia, String utilizadorId, long valorCentimos, String descricao) {
    }

    record Fatura(String numero, String referencia, String utilizadorId, long valorCentimos, String descricao,
                  Instant emitidaEm, Instant conservarAte, boolean comunicadaAT) {
    }
}
