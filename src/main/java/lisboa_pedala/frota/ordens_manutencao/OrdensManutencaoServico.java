package lisboa_pedala.frota.ordens_manutencao;

import java.time.Clock;
import java.util.List;
import java.util.UUID;
import lisboa_pedala.partilhado.NaoEncontrado;
import lisboa_pedala.partilhado.PedidoInvalido;
import lisboa_pedala.partilhado.Rest;

class OrdensManutencaoServico implements OrdensManutencao {

    private final OrdensManutencaoRepositorio repositorio;
    private final Clock relogio;

    OrdensManutencaoServico(OrdensManutencaoRepositorio repositorio, Clock relogio) {
        this.repositorio = repositorio;
        this.relogio = relogio;
    }

    @Override
    public Ordem abrir(PedidoOrdem p) {
        if (p == null || p.bicicletaId() == null || p.bicicletaId().isBlank() || p.tipo() == null
                || p.origem() == null) {
            throw new PedidoInvalido("bicicletaId, tipo e origem são obrigatórios");
        }
        return repositorio.bd().emTransacao(() -> repositorio.abertaPara(p.bicicletaId(), p.tipo()).orElseGet(() -> {
            Ordem o = new Ordem("om-" + UUID.randomUUID(), p.bicicletaId(), p.tipo(), p.origem(), p.descricao(),
                    EstadoOrdem.ABERTA, relogio.instant(), null);
            repositorio.inserir(o);
            return o;
        }));
    }

    @Override
    public List<Ordem> abertas() {
        return repositorio.abertas();
    }

    @Override
    public Ordem concluir(String ordemId) {
        Ordem o = repositorio.porId(ordemId).orElseThrow(() -> new NaoEncontrado("Ordem inexistente: " + ordemId));
        if (o.estado() == EstadoOrdem.ABERTA) {
            repositorio.concluir(ordemId, relogio.instant());
        }
        return repositorio.porId(ordemId).orElseThrow();
    }

    @Override
    public void publicarRotas(Rest rest) {
        rest.get("/ordens", p -> abertas());
        // Avaria reportada por um utilizador (requisito 5).
        rest.post("/ordens", p -> {
            PedidoOrdem pedido = p.corpo(PedidoOrdem.class);
            return new Rest.Resposta(201, abrir(new PedidoOrdem(pedido.bicicletaId(), pedido.tipo(),
                    Origem.UTILIZADOR, pedido.descricao())));
        });
        rest.post("/ordens/{ordemId}/conclusao", p -> concluir(p.parametro("ordemId")));
    }
}
