package lisboa_pedala.partilhado;

/** Recurso inexistente. A API REST responde 404. */
public class NaoEncontrado extends RuntimeException {
    public NaoEncontrado(String mensagem) {
        super(mensagem);
    }
}
