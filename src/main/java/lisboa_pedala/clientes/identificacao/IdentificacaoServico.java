package lisboa_pedala.clientes.identificacao;

import java.util.Map;
import java.util.Optional;
import lisboa_pedala.partilhado.NaoEncontrado;
import lisboa_pedala.partilhado.PedidoInvalido;
import lisboa_pedala.partilhado.Rest;

class IdentificacaoServico implements Identificacao {

    private final IdentificacaoRepositorio repositorio;

    IdentificacaoServico(IdentificacaoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void registar(NovoUtilizador u) {
        if (u == null || vazio(u.utilizadorId()) || u.tipo() == null) {
            throw new PedidoInvalido("utilizadorId e tipo são obrigatórios");
        }
        if (vazio(u.codigoApp()) && vazio(u.cartaoNavegante())) {
            throw new PedidoInvalido("É preciso pelo menos uma credencial (código da app ou cartão Navegante)");
        }
        repositorio.guardar(u);
    }

    @Override
    public Optional<UtilizadorIdentificado> identificar(Credencial credencial) {
        if (credencial == null || credencial.meio() == null || vazio(credencial.valor())) {
            return Optional.empty();
        }
        return switch (credencial.meio()) {
            case APP -> repositorio.porCodigoApp(credencial.valor());
            case NAVEGANTE -> repositorio.porNavegante(credencial.valor());
        };
    }

    @Override
    public TipoCliente tipoCliente(String utilizadorId) {
        return repositorio.porId(utilizadorId)
                .map(UtilizadorIdentificado::tipo)
                .orElseThrow(() -> new NaoEncontrado("Utilizador desconhecido: " + utilizadorId));
    }

    @Override
    public void publicarRotas(Rest rest) {
        rest.post("/utilizadores", p -> {
            NovoUtilizador u = p.corpo(NovoUtilizador.class);
            registar(u);
            return new Rest.Resposta(201, Map.of("utilizadorId", u.utilizadorId()));
        });
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}
