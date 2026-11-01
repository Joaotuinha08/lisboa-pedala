package lisboa_pedala.viagens.percursos;

import java.time.Instant;
import java.util.List;
import lisboa_pedala.partilhado.BaseDados;

/** Persistência dos Percursos. Tabelas: percursos_posicoes (só acedidas por este componente). */
class PercursosRepositorio {

    record Ponto(double latitude, double longitude) {
    }

    private final BaseDados bd;

    PercursosRepositorio(BaseDados bd) {
        this.bd = bd;
        bd.criarTabelas("""
                CREATE TABLE IF NOT EXISTS percursos_posicoes (
                    viagem_id TEXT NOT NULL,
                    hora      INTEGER NOT NULL,
                    latitude  REAL NOT NULL,
                    longitude REAL NOT NULL,
                    PRIMARY KEY (viagem_id, hora)
                )""");
    }

    void inserir(String viagemId, Instant hora, double latitude, double longitude) {
        bd.executar("INSERT OR IGNORE INTO percursos_posicoes (viagem_id, hora, latitude, longitude) VALUES (?, ?, ?, ?)",
                viagemId, hora, latitude, longitude);
    }

    List<Ponto> pontos(String viagemId) {
        return bd.consultar("SELECT latitude, longitude FROM percursos_posicoes WHERE viagem_id = ? ORDER BY hora",
                l -> new Ponto(l.real("latitude"), l.real("longitude")), viagemId);
    }

    int apagarAnterioresA(Instant limite) {
        return bd.executar("DELETE FROM percursos_posicoes WHERE hora < ?", limite);
    }
}
