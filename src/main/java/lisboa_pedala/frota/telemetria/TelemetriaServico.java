package lisboa_pedala.frota.telemetria;

import java.util.ArrayList;
import java.util.List;
import lisboa_pedala.frota.ordens_manutencao.OrdensManutencao;
import lisboa_pedala.frota.ordens_manutencao.OrdensManutencao.Origem;
import lisboa_pedala.frota.ordens_manutencao.OrdensManutencao.PedidoOrdem;
import lisboa_pedala.frota.ordens_manutencao.OrdensManutencao.TipoAvaria;
import lisboa_pedala.partilhado.PedidoInvalido;
import lisboa_pedala.partilhado.Rest;
import lisboa_pedala.viagens.percursos.Percursos;
import lisboa_pedala.viagens.percursos.Percursos.Posicao;

class TelemetriaServico implements Telemetria {

    private final TelemetriaRepositorio repositorio;
    private final Percursos percursos;
    private final OrdensManutencao ordens;

    TelemetriaServico(TelemetriaRepositorio repositorio, Percursos percursos, OrdensManutencao ordens) {
        this.repositorio = repositorio;
        this.percursos = percursos;
        this.ordens = ordens;
    }

    @Override
    public ResultadoTelemetria receber(EstadoBicicleta e) {
        if (e == null || e.bicicletaId() == null || e.bicicletaId().isBlank() || e.hora() == null
                || e.bateriaPct() < 0 || e.bateriaPct() > 100) {
            throw new PedidoInvalido("Telemetria inválida");
        }
        repositorio.guardar(e.bicicletaId(), e.hora(), e.bateriaPct(), e.travoesOk());

        boolean posicaoGuardada = false;
        if (e.latitude() != null && e.longitude() != null) {
            posicaoGuardada = percursos.registarPosicao(
                    new Posicao(e.bicicletaId(), e.hora(), e.latitude(), e.longitude()));
        }

        List<String> avarias = new ArrayList<>();
        List<String> ordensAbertas = new ArrayList<>();
        if (e.bateriaPct() < BATERIA_MINIMA_PCT) {
            avarias.add(TipoAvaria.BATERIA.name());
            ordensAbertas.add(ordens.abrir(new PedidoOrdem(e.bicicletaId(), TipoAvaria.BATERIA, Origem.TELEMETRIA,
                    "Bateria a " + e.bateriaPct() + "%")).ordemId());
        }
        if (!e.travoesOk()) {
            avarias.add(TipoAvaria.TRAVOES.name());
            ordensAbertas.add(ordens.abrir(new PedidoOrdem(e.bicicletaId(), TipoAvaria.TRAVOES, Origem.TELEMETRIA,
                    "Sensor de travões reporta falha")).ordemId());
        }
        return new ResultadoTelemetria(avarias, ordensAbertas, posicaoGuardada);
    }

    @Override
    public void publicarRotas(Rest rest) {
        rest.post("/telemetria", p -> receber(p.corpo(EstadoBicicleta.class)));
    }
}
