package lisboa_pedala.estacoes.mapa_ocupacao;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import lisboa_pedala.estacoes.mapa_ocupacao.MapaOcupacao.Estacao;
import lisboa_pedala.estacoes.mapa_ocupacao.MapaOcupacao.OcupacaoEstacao;
import lisboa_pedala.partilhado.BaseDados;

/** Persistência do Mapa de Ocupação. Tabelas: mapa_ocupacao_estacoes. */
class MapaOcupacaoRepositorio {

    private final BaseDados bd;

    MapaOcupacaoRepositorio(BaseDados bd) {
        this.bd = bd;
        bd.criarTabelas("""
                CREATE TABLE IF NOT EXISTS mapa_ocupacao_estacoes (
                    estacao_id    TEXT PRIMARY KEY,
                    nome          TEXT NOT NULL,
                    capacidade    INTEGER NOT NULL,
                    bicicletas    INTEGER NOT NULL,
                    latitude      REAL NOT NULL,
                    longitude     REAL NOT NULL,
                    atualizada_em INTEGER
                )""");
    }

    void guardar(Estacao e) {
        bd.executar("""
                INSERT INTO mapa_ocupacao_estacoes (estacao_id, nome, capacidade, bicicletas, latitude, longitude)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (estacao_id) DO UPDATE SET nome = excluded.nome, capacidade = excluded.capacidade,
                    bicicletas = excluded.bicicletas, latitude = excluded.latitude, longitude = excluded.longitude""",
                e.estacaoId(), e.nome(), e.capacidade(), e.bicicletas(), e.latitude(), e.longitude());
    }

    /** Soma (ou subtrai) bicicletas, sem sair do intervalo [0, capacidade]. */
    void ajustarBicicletas(String estacaoId, int delta, Instant hora) {
        bd.executar("""
                UPDATE mapa_ocupacao_estacoes
                SET bicicletas = MAX(0, MIN(capacidade, bicicletas + ?)), atualizada_em = ?
                WHERE estacao_id = ?""", delta, hora, estacaoId);
    }

    List<OcupacaoEstacao> todas() {
        return bd.consultar("""
                SELECT estacao_id, nome, capacidade, bicicletas, latitude, longitude, atualizada_em
                FROM mapa_ocupacao_estacoes ORDER BY estacao_id""", MapaOcupacaoRepositorio::mapear);
    }

    private static OcupacaoEstacao mapear(BaseDados.Linha l) throws SQLException {
        int capacidade = l.inteiro("capacidade");
        int bicicletas = l.inteiro("bicicletas");
        return new OcupacaoEstacao(l.texto("estacao_id"), l.texto("nome"), capacidade, bicicletas,
                capacidade - bicicletas, l.real("latitude"), l.real("longitude"), l.instante("atualizada_em"));
    }
}
