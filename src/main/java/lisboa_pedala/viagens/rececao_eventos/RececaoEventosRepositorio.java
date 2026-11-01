package lisboa_pedala.viagens.rececao_eventos;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import lisboa_pedala.partilhado.BaseDados;
import lisboa_pedala.partilhado.TipoEvento;
import lisboa_pedala.viagens.rececao_eventos.RececaoEventos.EventoDoca;

/**
 * Persistência da Receção de Eventos. Tabelas: rececao_eventos_recebidos.
 * A chave primária no identificador do evento é a defesa final contra repetidos.
 */
class RececaoEventosRepositorio {

    enum Estado { PROCESSADO, PENDENTE }

    private final BaseDados bd;

    RececaoEventosRepositorio(BaseDados bd) {
        this.bd = bd;
        bd.criarTabelas("""
                CREATE TABLE IF NOT EXISTS rececao_eventos_recebidos (
                    evento_id     TEXT PRIMARY KEY,
                    tipo          TEXT NOT NULL,
                    doca_id       TEXT NOT NULL,
                    estacao_id    TEXT NOT NULL,
                    bicicleta_id  TEXT NOT NULL,
                    utilizador_id TEXT,
                    hora_doca     INTEGER NOT NULL,
                    estado        TEXT NOT NULL,
                    recebido_em   INTEGER NOT NULL
                )""", """
                CREATE INDEX IF NOT EXISTS rececao_eventos_recebidos_pendentes
                ON rececao_eventos_recebidos (estado, bicicleta_id, hora_doca)""");
    }

    BaseDados bd() {
        return bd;
    }

    boolean existe(String eventoId) {
        return bd.contar("SELECT COUNT(*) FROM rececao_eventos_recebidos WHERE evento_id = ?", eventoId) > 0;
    }

    void inserir(EventoDoca e, Estado estado, Instant recebidoEm) {
        bd.executar("""
                INSERT INTO rececao_eventos_recebidos
                    (evento_id, tipo, doca_id, estacao_id, bicicleta_id, utilizador_id, hora_doca, estado, recebido_em)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                e.eventoId(), e.tipo(), e.docaId(), e.estacaoId(), e.bicicletaId(), e.utilizadorId(), e.horaDoca(),
                estado, recebidoEm);
    }

    void marcarProcessado(String eventoId) {
        bd.executar("UPDATE rececao_eventos_recebidos SET estado = 'PROCESSADO' WHERE evento_id = ? AND estado = 'PENDENTE'",
                eventoId);
    }

    List<EventoDoca> pendentesDaBicicleta(String bicicletaId) {
        return bd.consultar("""
                SELECT evento_id, tipo, doca_id, estacao_id, bicicleta_id, utilizador_id, hora_doca
                FROM rececao_eventos_recebidos
                WHERE estado = 'PENDENTE' AND bicicleta_id = ? ORDER BY hora_doca""",
                RececaoEventosRepositorio::mapear, bicicletaId);
    }

    long contarPendentes() {
        return bd.contar("SELECT COUNT(*) FROM rececao_eventos_recebidos WHERE estado = 'PENDENTE'");
    }

    private static EventoDoca mapear(BaseDados.Linha l) throws SQLException {
        return new EventoDoca(l.texto("evento_id"), TipoEvento.valueOf(l.texto("tipo")), l.texto("doca_id"),
                l.texto("estacao_id"), l.texto("bicicleta_id"), l.texto("utilizador_id"), l.instante("hora_doca"));
    }
}
