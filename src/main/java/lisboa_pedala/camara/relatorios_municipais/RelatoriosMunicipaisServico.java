package lisboa_pedala.camara.relatorios_municipais;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lisboa_pedala.partilhado.PedidoInvalido;
import lisboa_pedala.partilhado.Rest;
import lisboa_pedala.viagens.percursos.Percursos;
import lisboa_pedala.viagens.registo_viagens.RegistoViagens;
import lisboa_pedala.viagens.registo_viagens.RegistoViagens.Viagem;

class RelatoriosMunicipaisServico implements RelatoriosMunicipais {

    static final ZoneId LISBOA = ZoneId.of("Europe/Lisbon");
    static final String SEM_FREGUESIA = "(sem freguesia)";
    /**
     * PROVISÓRIO: kg de CO₂ evitados por km feito de bicicleta em vez de automóvel. O enunciado não dá
     * o fator; valor assinalado como lacuna em prompts/camara.relatorios_municipais.md.
     */
    static final double KG_CO2_POR_KM = 0.12;

    private final RelatoriosMunicipaisRepositorio repositorio;
    private final RegistoViagens registo;
    private final Percursos percursos;

    RelatoriosMunicipaisServico(RelatoriosMunicipaisRepositorio repositorio, RegistoViagens registo,
                                Percursos percursos) {
        this.repositorio = repositorio;
        this.registo = registo;
        this.percursos = percursos;
    }

    @Override
    public void associarFreguesia(String estacaoId, String freguesia) {
        if (estacaoId == null || freguesia == null || freguesia.isBlank()) {
            throw new PedidoInvalido("estacaoId e freguesia são obrigatórios");
        }
        repositorio.associar(estacaoId, freguesia);
    }

    @Override
    public RelatorioMensal gerar(YearMonth mes) {
        List<Viagem> viagens = registo.viagensFechadasEntre(
                mes.atDay(1).atStartOfDay(LISBOA).toInstant(),
                mes.plusMonths(1).atDay(1).atStartOfDay(LISBOA).toInstant());
        Map<String, String> freguesias = repositorio.freguesiaPorEstacao();

        Map<String, double[]> acumulado = new TreeMap<>(); // freguesia -> [viagens, km]
        for (Viagem v : viagens) {
            String freguesia = freguesias.getOrDefault(v.estacaoOrigem(), SEM_FREGUESIA);
            double km = percursos.distanciaMetros(v.viagemId()) / 1000.0;
            double[] a = acumulado.computeIfAbsent(freguesia, f -> new double[2]);
            a[0]++;
            a[1] += km;
        }
        List<LinhaFreguesia> linhas = new ArrayList<>();
        double totalKm = 0;
        for (Map.Entry<String, double[]> e : acumulado.entrySet()) {
            double km = e.getValue()[1];
            totalKm += km;
            linhas.add(new LinhaFreguesia(e.getKey(), (int) e.getValue()[0], arredondar(km),
                    arredondar(km * KG_CO2_POR_KM)));
        }
        return new RelatorioMensal(mes, viagens.size(), arredondar(totalKm), arredondar(totalKm * KG_CO2_POR_KM),
                linhas);
    }

    private static double arredondar(double v) {
        return Math.round(v * 1000) / 1000.0;
    }

    @Override
    public void publicarRotas(Rest rest) {
        rest.get("/relatorios/{mes}", p -> gerar(YearMonth.parse(p.parametro("mes"))));
    }
}
