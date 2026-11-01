package lisboa_pedala.partilhado;

/** Dados de entrada inválidos. A API REST responde 400. */
public class PedidoInvalido extends RuntimeException {
    public PedidoInvalido(String mensagem) {
        super(mensagem);
    }
}
