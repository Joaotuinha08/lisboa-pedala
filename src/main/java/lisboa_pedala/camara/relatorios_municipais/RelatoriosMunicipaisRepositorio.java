package lisboa_pedala.camara.relatorios_municipais;

import java.util.HashMap;
import java.util.Map;
import lisboa_pedala.partilhado.BaseDados;

/** Persistência dos Relatórios Municipais. Tabelas: relatorios_municipais_freguesias. */
class RelatoriosMunicipaisRepositorio {

    private final BaseDados bd;

    RelatoriosMunicipaisRepositorio(BaseDados bd) {
        this.bd = bd;
        bd.criarTabelas("""
                CREATE TABLE IF NOT EXISTS relatorios_municipais_freguesias (
                    estacao_id TEXT PRIMARY KEY,
                    freguesia  TEXT NOT NULL
                )""");
    }

    void associar(String estacaoId, String freguesia) {
        bd.executar("""
                INSERT INTO relatorios_municipais_freguesias (estacao_id, freguesia) VALUES (?, ?)
                ON CONFLICT (estacao_id) DO UPDATE SET freguesia = excluded.freguesia""", estacaoId, freguesia);
    }

    Map<String, String> freguesiaPorEstacao() {
        Map<String, String> mapa = new HashMap<>();
        bd.consultar("SELECT estacao_id, freguesia FROM relatorios_municipais_freguesias",
                l -> mapa.put(l.texto("estacao_id"), l.texto("freguesia")));
        return mapa;
    }
}
