package lisboa_pedala.estacoes.mapa_ocupacao;

import java.util.List;
import lisboa_pedala.partilhado.PedidoInvalido;
import lisboa_pedala.partilhado.Rest;

class MapaOcupacaoServico implements MapaOcupacao {

    private final MapaOcupacaoRepositorio repositorio;

    MapaOcupacaoServico(MapaOcupacaoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void configurarEstacao(Estacao e) {
        if (e == null || e.estacaoId() == null || e.capacidade() <= 0 || e.bicicletas() < 0
                || e.bicicletas() > e.capacidade()) {
            throw new PedidoInvalido("Estação inválida");
        }
        repositorio.guardar(e);
    }

    @Override
    public void registarMovimento(Movimento m) {
        int delta = switch (m.tipo()) {
            case DESBLOQUEIO -> -1; // sai uma bicicleta, fica uma doca livre
            case ENCAIXE -> +1;
        };
        repositorio.ajustarBicicletas(m.estacaoId(), delta, m.hora());
    }

    @Override
    public List<OcupacaoEstacao> ocupacao() {
        return repositorio.todas();
    }

    @Override
    public void publicarRotas(Rest rest) {
        rest.get("/mapa", p -> ocupacao());
    }
}
