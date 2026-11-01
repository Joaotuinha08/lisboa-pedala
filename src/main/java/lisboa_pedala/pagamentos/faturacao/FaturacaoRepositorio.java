package lisboa_pedala.pagamentos.faturacao;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lisboa_pedala.pagamentos.faturacao.Faturacao.Fatura;
import lisboa_pedala.partilhado.BaseDados;

/**
 * Persistência da Faturação. Tabelas: faturacao_faturas (só inserção) e faturacao_comunicacoes_at.
 * A imutabilidade e a conservação por 10 anos são garantidas também pela própria base (triggers).
 */
class FaturacaoRepositorio {

    private static final String COLUNAS = """
            f.numero, f.referencia, f.utilizador_id, f.valor_centimos, f.descricao, f.emitida_em, f.conservar_ate,
            c.comunicada_em""";

    private final BaseDados bd;

    FaturacaoRepositorio(BaseDados bd) {
        this.bd = bd;
        bd.criarTabelas("""
                CREATE TABLE IF NOT EXISTS faturacao_faturas (
                    numero         TEXT PRIMARY KEY,
                    referencia     TEXT NOT NULL UNIQUE,
                    utilizador_id  TEXT NOT NULL,
                    valor_centimos INTEGER NOT NULL,
                    descricao      TEXT NOT NULL,
                    emitida_em     INTEGER NOT NULL,
                    conservar_ate  INTEGER NOT NULL
                )""", """
                CREATE TRIGGER IF NOT EXISTS faturacao_faturas_imutaveis
                BEFORE UPDATE ON faturacao_faturas
                BEGIN SELECT RAISE(ABORT, 'As faturas emitidas são imutáveis'); END""", """
                CREATE TRIGGER IF NOT EXISTS faturacao_faturas_conservacao
                BEFORE DELETE ON faturacao_faturas
                WHEN old.conservar_ate > CAST(strftime('%s', 'now') AS INTEGER) * 1000
                BEGIN SELECT RAISE(ABORT, 'As faturas são conservadas durante 10 anos'); END""", """
                CREATE TABLE IF NOT EXISTS faturacao_comunicacoes_at (
                    numero        TEXT PRIMARY KEY REFERENCES faturacao_faturas (numero),
                    comunicada_em INTEGER,
                    tentativas    INTEGER NOT NULL DEFAULT 0,
                    ultimo_erro   TEXT
                )""");
    }

    BaseDados bd() {
        return bd;
    }

    Optional<Fatura> porReferencia(String referencia) {
        return bd.consultarUm("SELECT " + COLUNAS + " " + """
                 FROM faturacao_faturas f LEFT JOIN faturacao_comunicacoes_at c ON c.numero = f.numero
                 WHERE f.referencia = ?""", FaturacaoRepositorio::mapear, referencia);
    }

    List<Fatura> porUtilizador(String utilizadorId) {
        return bd.consultar("SELECT " + COLUNAS + " " + """
                 FROM faturacao_faturas f LEFT JOIN faturacao_comunicacoes_at c ON c.numero = f.numero
                 WHERE f.utilizador_id = ? ORDER BY f.emitida_em""", FaturacaoRepositorio::mapear, utilizadorId);
    }

    long contarEmitidasComPrefixo(String prefixo) {
        return bd.contar("SELECT COUNT(*) FROM faturacao_faturas WHERE numero LIKE ?", prefixo + "%");
    }

    void inserir(Fatura f) {
        bd.executar("""
                INSERT INTO faturacao_faturas
                    (numero, referencia, utilizador_id, valor_centimos, descricao, emitida_em, conservar_ate)
                VALUES (?, ?, ?, ?, ?, ?, ?)""",
                f.numero(), f.referencia(), f.utilizadorId(), f.valorCentimos(), f.descricao(), f.emitidaEm(),
                f.conservarAte());
        bd.executar("INSERT INTO faturacao_comunicacoes_at (numero) VALUES (?)", f.numero());
    }

    List<Fatura> porComunicar() {
        return bd.consultar("SELECT " + COLUNAS + " " + """
                 FROM faturacao_faturas f JOIN faturacao_comunicacoes_at c ON c.numero = f.numero
                 WHERE c.comunicada_em IS NULL ORDER BY f.emitida_em""", FaturacaoRepositorio::mapear);
    }

    void marcarComunicada(String numero, Instant quando) {
        bd.executar("""
                UPDATE faturacao_comunicacoes_at SET comunicada_em = ?, tentativas = tentativas + 1, ultimo_erro = NULL
                WHERE numero = ?""", quando, numero);
    }

    void registarFalha(String numero, String erro) {
        bd.executar("UPDATE faturacao_comunicacoes_at SET tentativas = tentativas + 1, ultimo_erro = ? WHERE numero = ?",
                erro, numero);
    }

    private static Fatura mapear(BaseDados.Linha l) throws SQLException {
        return new Fatura(l.texto("numero"), l.texto("referencia"), l.texto("utilizador_id"),
                l.longo("valor_centimos"), l.texto("descricao"), l.instante("emitida_em"),
                l.instante("conservar_ate"), l.instante("comunicada_em") != null);
    }
}
