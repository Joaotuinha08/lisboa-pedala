package lisboa_pedala.pagamentos.tarifacao;

import java.time.Instant;

/**
 * Tarifação: calcula o valor de uma viagem segundo o tarifário da câmara e o tipo de cliente.
 * Pode usar: nenhum componente. ADR-006.
 *
 * <p>Interface estável única (§3, Extensibilidade): quem chama só envia horas de início e fim, tipo de
 * cliente e câmara. Blocos de tempo e minutos grátis existem apenas dentro deste pacote. Para acrescentar
 * uma câmara nova cria-se uma implementação de {@code Tarifario} e regista-se em {@code CatalogoTarifarios}.
 */
public interface Tarifacao {

    static Tarifacao criar() {
        return new TarifacaoServico(CatalogoTarifarios.padrao());
    }

    /** Valor da viagem em cêntimos de euro. */
    long calcularValor(PedidoCalculo pedido);

    enum TipoCliente { ASSINANTE, OCASIONAL }

    record PedidoCalculo(Instant inicio, Instant fim, TipoCliente tipoCliente, String camara) {
    }
}
