package lisboa_pedala.frota.telemetria;

import java.time.Instant;
import lisboa_pedala.partilhado.BaseDados;

/**
 * Persistência da Telemetria. Tabelas: telemetria_estado_atual (último estado técnico de cada bicicleta,
 * sem posição: a posição é dado pessoal e pertence a Percursos).
 */
class TelemetriaRepositorio {

    private final BaseDados bd;

    TelemetriaRepositorio(BaseDados bd) {
        this.bd = bd;
        bd.criarTabelas("""
                CREATE TABLE IF NOT EXISTS telemetria_estado_atual (
                    bicicleta_id TEXT PRIMARY KEY,
                    hora         INTEGER NOT NULL,
                    bateria_pct  INTEGER NOT NULL,
                    travoes_ok   INTEGER NOT NULL
                )""");
    }

    void guardar(String bicicletaId, Instant hora, int bateriaPct, boolean travoesOk) {
        bd.executar("""
                INSERT INTO telemetria_estado_atual (bicicleta_id, hora, bateria_pct, travoes_ok) VALUES (?, ?, ?, ?)
                ON CONFLICT (bicicleta_id) DO UPDATE SET hora = excluded.hora, bateria_pct = excluded.bateria_pct,
                    travoes_ok = excluded.travoes_ok
                WHERE excluded.hora >= telemetria_estado_atual.hora""",
                bicicletaId, hora, bateriaPct, travoesOk);
    }
}
