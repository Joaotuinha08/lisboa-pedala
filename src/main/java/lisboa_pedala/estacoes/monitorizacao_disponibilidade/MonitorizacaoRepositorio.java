package lisboa_pedala.estacoes.monitorizacao_disponibilidade;

import java.time.Instant;
import java.util.List;
import lisboa_pedala.partilhado.BaseDados;

/** Persistência da Monitorização. Tabelas: monitorizacao_disponibilidade_estacoes e _contactos. */
class MonitorizacaoRepositorio {

    private final BaseDados bd;

    MonitorizacaoRepositorio(BaseDados bd) {
        this.bd = bd;
        bd.criarTabelas("""
                CREATE TABLE IF NOT EXISTS monitorizacao_disponibilidade_estacoes (
                    estacao_id TEXT PRIMARY KEY
                )""", """
                CREATE TABLE IF NOT EXISTS monitorizacao_disponibilidade_contactos (
                    estacao_id  TEXT NOT NULL,
                    inicio_hora INTEGER NOT NULL,
                    contactos   INTEGER NOT NULL,
                    PRIMARY KEY (estacao_id, inicio_hora)
                )""");
    }

    void registarEstacao(String estacaoId) {
        bd.executar("INSERT OR IGNORE INTO monitorizacao_disponibilidade_estacoes (estacao_id) VALUES (?)", estacaoId);
    }

    void registarContacto(String estacaoId, Instant inicioHora) {
        bd.executar("""
                INSERT INTO monitorizacao_disponibilidade_contactos (estacao_id, inicio_hora, contactos) VALUES (?, ?, 1)
                ON CONFLICT (estacao_id, inicio_hora) DO UPDATE SET contactos = contactos + 1""",
                estacaoId, inicioHora);
    }

    List<String> estacoes() {
        return bd.consultar("SELECT estacao_id FROM monitorizacao_disponibilidade_estacoes ORDER BY estacao_id",
                l -> l.texto("estacao_id"));
    }

    List<String> comContacto(Instant inicioHora) {
        return bd.consultar("SELECT estacao_id FROM monitorizacao_disponibilidade_contactos WHERE inicio_hora = ?",
                l -> l.texto("estacao_id"), inicioHora);
    }
}
