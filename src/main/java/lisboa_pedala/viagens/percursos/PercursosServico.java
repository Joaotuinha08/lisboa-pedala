package lisboa_pedala.viagens.percursos;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lisboa_pedala.viagens.percursos.PercursosRepositorio.Ponto;
import lisboa_pedala.viagens.registo_viagens.RegistoViagens;

class PercursosServico implements Percursos {

    private static final double RAIO_TERRA_M = 6_371_000;

    private final PercursosRepositorio repositorio;
    private final RegistoViagens registo;

    PercursosServico(PercursosRepositorio repositorio, RegistoViagens registo) {
        this.repositorio = repositorio;
        this.registo = registo;
    }

    @Override
    public boolean registarPosicao(Posicao p) {
        if (p == null || p.bicicletaId() == null || p.hora() == null) {
            return false;
        }
        Optional<String> viagem = registo.viagemEmCurso(p.bicicletaId(), p.hora());
        viagem.ifPresent(id -> repositorio.inserir(id, p.hora(), p.latitude(), p.longitude()));
        return viagem.isPresent();
    }

    @Override
    public double distanciaMetros(String viagemId) {
        List<Ponto> pontos = repositorio.pontos(viagemId);
        double total = 0;
        for (int i = 1; i < pontos.size(); i++) {
            total += haversine(pontos.get(i - 1), pontos.get(i));
        }
        return total;
    }

    @Override
    public int apagarPosicoesAnterioresA(Instant limite) {
        return repositorio.apagarAnterioresA(limite);
    }

    private static double haversine(Ponto a, Ponto b) {
        double dLat = Math.toRadians(b.latitude() - a.latitude());
        double dLon = Math.toRadians(b.longitude() - a.longitude());
        double h = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(a.latitude())) * Math.cos(Math.toRadians(b.latitude()))
                * Math.pow(Math.sin(dLon / 2), 2);
        return 2 * RAIO_TERRA_M * Math.asin(Math.sqrt(h));
    }
}
