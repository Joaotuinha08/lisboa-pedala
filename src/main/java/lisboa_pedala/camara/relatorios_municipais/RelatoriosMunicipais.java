package lisboa_pedala.camara.relatorios_municipais;

import java.time.YearMonth;
import java.util.List;
import lisboa_pedala.partilhado.BaseDados;
import lisboa_pedala.partilhado.Rest;
import lisboa_pedala.viagens.percursos.Percursos;
import lisboa_pedala.viagens.registo_viagens.RegistoViagens;

/**
 * Relatórios Municipais: produz o relatório mensal de utilização por freguesia e de CO₂ evitado.
 * Pode usar: registo_viagens, percursos. ADR-007 (de Percursos só usa a distância).
 */
public interface RelatoriosMunicipais {

    static RelatoriosMunicipais criar(BaseDados bd, RegistoViagens registo, Percursos percursos) {
        return new RelatoriosMunicipaisServico(new RelatoriosMunicipaisRepositorio(bd), registo, percursos);
    }

    /** Freguesia de cada estação (a viagem conta na freguesia da estação de origem). */
    void associarFreguesia(String estacaoId, String freguesia);

    RelatorioMensal gerar(YearMonth mes);

    void publicarRotas(Rest rest);

    record LinhaFreguesia(String freguesia, int viagens, double quilometros, double kgCo2Evitado) {
    }

    record RelatorioMensal(YearMonth mes, int totalViagens, double totalQuilometros, double totalKgCo2Evitado,
                           List<LinhaFreguesia> freguesias) {
    }
}
