package lisboa_pedala.pagamentos.cobranca;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import lisboa_pedala.pagamentos.cobranca.Cobranca.Estado;
import lisboa_pedala.pagamentos.cobranca.Cobranca.EstadoCobranca;
import lisboa_pedala.partilhado.BaseDados;

/** Persistência da Cobrança. Tabelas: cobranca_pedidos. */
class CobrancaRepositorio {

    private static final String COLUNAS =
            "cobranca_id, viagem_id, utilizador_id, valor_centimos, estado, tentativas, ultimo_erro, registada_em";

    private final BaseDados bd;

    CobrancaRepositorio(BaseDados bd) {
        this.bd = bd;
        bd.criarTabelas("""
                CREATE TABLE IF NOT EXISTS cobranca_pedidos (
                    cobranca_id    TEXT PRIMARY KEY,
                    viagem_id      TEXT NOT NULL UNIQUE,
                    utilizador_id  TEXT NOT NULL,
                    valor_centimos INTEGER NOT NULL,
                    estado         TEXT NOT NULL,
                    tentativas     INTEGER NOT NULL DEFAULT 0,
                    ultimo_erro    TEXT,
                    registada_em   INTEGER NOT NULL
                )""");
    }

    BaseDados bd() {
        return bd;
    }

    Optional<EstadoCobranca> porViagem(String viagemId) {
        return bd.consultarUm("SELECT " + COLUNAS + " FROM cobranca_pedidos WHERE viagem_id = ?",
                CobrancaRepositorio::mapear, viagemId);
    }

    void inserir(EstadoCobranca c) {
        bd.executar("INSERT INTO cobranca_pedidos (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                c.cobrancaId(), c.viagemId(), c.utilizadorId(), c.valorCentimos(), c.estado(), c.tentativas(),
                c.ultimoErro(), c.registadaEm());
    }

    List<EstadoCobranca> porEstado(Estado estado) {
        return bd.consultar("SELECT " + COLUNAS + " FROM cobranca_pedidos WHERE estado = ? ORDER BY registada_em",
                CobrancaRepositorio::mapear, estado);
    }

    void mudarEstado(String cobrancaId, Estado de, Estado para) {
        bd.executar("UPDATE cobranca_pedidos SET estado = ?, tentativas = tentativas + 1, ultimo_erro = NULL"
                + " WHERE cobranca_id = ? AND estado = ?", para, cobrancaId, de);
    }

    void registarFalha(String cobrancaId, String erro) {
        bd.executar("UPDATE cobranca_pedidos SET tentativas = tentativas + 1, ultimo_erro = ? WHERE cobranca_id = ?",
                erro, cobrancaId);
    }

    private static EstadoCobranca mapear(BaseDados.Linha l) throws SQLException {
        return new EstadoCobranca(l.texto("cobranca_id"), l.texto("viagem_id"), l.texto("utilizador_id"),
                l.longo("valor_centimos"), Estado.valueOf(l.texto("estado")), l.inteiro("tentativas"),
                l.texto("ultimo_erro"), l.instante("registada_em"));
    }
}
