package lisboa_pedala.viagens.desbloqueio;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lisboa_pedala.clientes.identificacao.Identificacao;
import lisboa_pedala.clientes.identificacao.Identificacao.Credencial;
import lisboa_pedala.clientes.identificacao.Identificacao.UtilizadorIdentificado;
import lisboa_pedala.partilhado.PedidoInvalido;
import lisboa_pedala.partilhado.Rest;

class DesbloqueioServico implements Desbloqueio {

    private final DesbloqueioRepositorio repositorio;
    private final Identificacao identificacao;
    private final Clock relogio;

    DesbloqueioServico(DesbloqueioRepositorio repositorio, Identificacao identificacao, Clock relogio) {
        this.repositorio = repositorio;
        this.identificacao = identificacao;
        this.relogio = relogio;
    }

    @Override
    public Autorizacao pedir(PedidoDesbloqueio p) {
        if (p == null || p.estacaoId() == null || p.docaId() == null || p.bicicletaId() == null) {
            throw new PedidoInvalido("estacaoId, docaId e bicicletaId são obrigatórios");
        }
        Instant agora = relogio.instant();
        String id = "aut-" + UUID.randomUUID();
        Optional<UtilizadorIdentificado> u = identificacao.identificar(new Credencial(p.meio(), p.credencial()));
        Autorizacao a = u
                .map(ui -> new Autorizacao(true, id, ui.utilizadorId(), null, agora))
                .orElseGet(() -> new Autorizacao(false, id, null, "Utilizador não identificado", agora));
        repositorio.guardar(p, a);
        return a;
    }

    @Override
    public void publicarRotas(Rest rest) {
        rest.post("/desbloqueios", p -> {
            Autorizacao a = pedir(p.corpo(PedidoDesbloqueio.class));
            return new Rest.Resposta(a.autorizado() ? 200 : 403, a);
        });
    }
}
