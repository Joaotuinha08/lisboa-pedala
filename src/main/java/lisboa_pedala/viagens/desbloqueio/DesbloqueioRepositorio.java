package lisboa_pedala.viagens.desbloqueio;

import lisboa_pedala.partilhado.BaseDados;
import lisboa_pedala.viagens.desbloqueio.Desbloqueio.Autorizacao;
import lisboa_pedala.viagens.desbloqueio.Desbloqueio.PedidoDesbloqueio;

/** Persistência do Desbloqueio. Tabelas: desbloqueio_autorizacoes (registo de pedidos e decisões). */
class DesbloqueioRepositorio {

    private final BaseDados bd;

    DesbloqueioRepositorio(BaseDados bd) {
        this.bd = bd;
        bd.criarTabelas("""
                CREATE TABLE IF NOT EXISTS desbloqueio_autorizacoes (
                    autorizacao_id TEXT PRIMARY KEY,
                    estacao_id     TEXT NOT NULL,
                    doca_id        TEXT NOT NULL,
                    bicicleta_id   TEXT NOT NULL,
                    meio           TEXT,
                    utilizador_id  TEXT,
                    autorizado     INTEGER NOT NULL,
                    motivo         TEXT,
                    hora           INTEGER NOT NULL
                )""");
    }

    void guardar(PedidoDesbloqueio p, Autorizacao a) {
        bd.executar("""
                INSERT INTO desbloqueio_autorizacoes
                    (autorizacao_id, estacao_id, doca_id, bicicleta_id, meio, utilizador_id, autorizado, motivo, hora)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                a.autorizacaoId(), p.estacaoId(), p.docaId(), p.bicicletaId(), p.meio(), a.utilizadorId(),
                a.autorizado(), a.motivo(), a.hora());
    }
}
