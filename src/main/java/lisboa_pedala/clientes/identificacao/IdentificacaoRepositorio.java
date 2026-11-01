package lisboa_pedala.clientes.identificacao;

import java.util.Optional;
import lisboa_pedala.clientes.identificacao.Identificacao.TipoCliente;
import lisboa_pedala.clientes.identificacao.Identificacao.UtilizadorIdentificado;
import lisboa_pedala.partilhado.BaseDados;

/** Persistência da Identificação. Tabelas: identificacao_utilizadores. */
class IdentificacaoRepositorio {

    private final BaseDados bd;

    IdentificacaoRepositorio(BaseDados bd) {
        this.bd = bd;
        bd.criarTabelas("""
                CREATE TABLE IF NOT EXISTS identificacao_utilizadores (
                    utilizador_id    TEXT PRIMARY KEY,
                    tipo             TEXT NOT NULL,
                    codigo_app       TEXT UNIQUE,
                    cartao_navegante TEXT UNIQUE
                )""");
    }

    void guardar(Identificacao.NovoUtilizador u) {
        bd.executar("""
                INSERT INTO identificacao_utilizadores (utilizador_id, tipo, codigo_app, cartao_navegante)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (utilizador_id) DO UPDATE SET
                    tipo = excluded.tipo, codigo_app = excluded.codigo_app, cartao_navegante = excluded.cartao_navegante""",
                u.utilizadorId(), u.tipo(), u.codigoApp(), u.cartaoNavegante());
    }

    Optional<UtilizadorIdentificado> porCodigoApp(String codigo) {
        return bd.consultarUm("SELECT utilizador_id, tipo FROM identificacao_utilizadores WHERE codigo_app = ?",
                IdentificacaoRepositorio::mapear, codigo);
    }

    Optional<UtilizadorIdentificado> porNavegante(String cartao) {
        return bd.consultarUm("SELECT utilizador_id, tipo FROM identificacao_utilizadores WHERE cartao_navegante = ?",
                IdentificacaoRepositorio::mapear, cartao);
    }

    Optional<UtilizadorIdentificado> porId(String utilizadorId) {
        return bd.consultarUm("SELECT utilizador_id, tipo FROM identificacao_utilizadores WHERE utilizador_id = ?",
                IdentificacaoRepositorio::mapear, utilizadorId);
    }

    private static UtilizadorIdentificado mapear(BaseDados.Linha l) throws java.sql.SQLException {
        return new UtilizadorIdentificado(l.texto("utilizador_id"), TipoCliente.valueOf(l.texto("tipo")));
    }
}
