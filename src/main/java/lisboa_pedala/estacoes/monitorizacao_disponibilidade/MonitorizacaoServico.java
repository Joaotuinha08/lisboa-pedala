package lisboa_pedala.estacoes.monitorizacao_disponibilidade;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lisboa_pedala.partilhado.Rest;

class MonitorizacaoServico implements MonitorizacaoDisponibilidade {

    private final MonitorizacaoRepositorio repositorio;
    private final Clock relogio;

    MonitorizacaoServico(MonitorizacaoRepositorio repositorio, Clock relogio) {
        this.repositorio = repositorio;
        this.relogio = relogio;
    }

    @Override
    public void registarEstacao(String estacaoId) {
        repositorio.registarEstacao(estacaoId);
    }

    @Override
    public void registarContacto(String estacaoId) {
        repositorio.registarContacto(estacaoId, relogio.instant().truncatedTo(ChronoUnit.HOURS));
    }

    @Override
    public RelatorioHora relatorio(Instant hora) {
        Instant inicio = hora.truncatedTo(ChronoUnit.HOURS);
        List<String> todas = repositorio.estacoes();
        Set<String> comContacto = new HashSet<>(repositorio.comContacto(inicio));
        List<String> semContacto = todas.stream().filter(e -> !comContacto.contains(e)).toList();
        int com = todas.size() - semContacto.size();
        double fracao = todas.isEmpty() ? 0 : (double) semContacto.size() / todas.size();
        return new RelatorioHora(inicio, todas.size(), com, semContacto, fracao, fracao > LIMITE_MULTA);
    }

    @Override
    public void publicarRotas(Rest rest) {
        rest.get("/disponibilidade", p -> relatorio(p.query("hora").map(Instant::parse).orElse(relogio.instant())));
    }
}
