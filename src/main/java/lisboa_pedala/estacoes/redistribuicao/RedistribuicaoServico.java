package lisboa_pedala.estacoes.redistribuicao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lisboa_pedala.estacoes.mapa_ocupacao.MapaOcupacao;
import lisboa_pedala.estacoes.mapa_ocupacao.MapaOcupacao.OcupacaoEstacao;

/**
 * Heurística gulosa: tira bicicletas das estações mais cheias (acima de 80%) e leva-as às mais vazias
 * (abaixo de 20%), até cada uma ficar perto de metade da capacidade.
 */
class RedistribuicaoServico implements Redistribuicao {

    static final double CHEIA = 0.8;
    static final double VAZIA = 0.2;

    private final MapaOcupacao mapa;

    RedistribuicaoServico(MapaOcupacao mapa) {
        this.mapa = mapa;
    }

    @Override
    public List<Rota> sugerirRotas(int carrinhas, int bicicletasPorCarrinha) {
        List<int[]> excessos = new ArrayList<>(); // [indice, quantidade]
        List<int[]> faltas = new ArrayList<>();
        List<OcupacaoEstacao> estacoes = mapa.ocupacao();
        for (int i = 0; i < estacoes.size(); i++) {
            OcupacaoEstacao e = estacoes.get(i);
            double ocupacao = (double) e.bicicletas() / e.capacidade();
            int alvo = e.capacidade() / 2;
            if (ocupacao > CHEIA) {
                excessos.add(new int[] {i, e.bicicletas() - alvo});
            } else if (ocupacao < VAZIA) {
                faltas.add(new int[] {i, alvo - e.bicicletas()});
            }
        }
        excessos.sort(Comparator.comparingInt((int[] a) -> a[1]).reversed());
        faltas.sort(Comparator.comparingInt((int[] a) -> a[1]).reversed());

        List<Rota> rotas = new ArrayList<>();
        int o = 0;
        int d = 0;
        while (rotas.size() < carrinhas && o < excessos.size() && d < faltas.size()) {
            int[] origem = excessos.get(o);
            int[] destino = faltas.get(d);
            int n = Math.min(bicicletasPorCarrinha, Math.min(origem[1], destino[1]));
            if (n <= 0) {
                break;
            }
            rotas.add(new Rota(rotas.size() + 1, estacoes.get(origem[0]).estacaoId(),
                    estacoes.get(destino[0]).estacaoId(), n));
            origem[1] -= n;
            destino[1] -= n;
            if (origem[1] == 0) {
                o++;
            }
            if (destino[1] == 0) {
                d++;
            }
        }
        return rotas;
    }

    @Override
    public void publicarRotas(lisboa_pedala.partilhado.Rest rest) {
        rest.get("/redistribuicao", p -> sugerirRotas(
                p.query("carrinhas").map(Integer::parseInt).orElse(6),
                p.query("capacidade").map(Integer::parseInt).orElse(20)));
    }
}
