package lisboa_pedala.partilhado;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Acesso à única base SQLite (ADR-009), partilhado por todos os componentes.
 *
 * <p>Não conhece nenhuma tabela: cada componente cria e usa apenas as suas (prefixo do componente).
 * Oferece a <b>unidade de trabalho</b> do §4 do ARCHITECTURE.md: {@link #emTransacao} é reentrante,
 * por isso quando a Receção de Eventos abre uma transação e chama o Registo de Viagens, que chama a
 * Cobrança, as escritas dos três componentes ficam na mesma transação JDBC.
 *
 * <p>SQLite só admite um escritor de cada vez, por isso há uma só ligação, protegida por um trinco.
 */
public final class BaseDados implements AutoCloseable {

    private final Connection ligacao;
    private final ReentrantLock trinco = new ReentrantLock();
    private int profundidade = 0; // só lido/escrito com o trinco na mão

    private BaseDados(Connection ligacao) {
        this.ligacao = ligacao;
    }

    /** Abre (ou cria) a base no ficheiro indicado. Use ":memory:" para uma base temporária. */
    public static BaseDados abrir(String caminho) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection("jdbc:sqlite:" + caminho);
            try (Statement s = c.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON");
                if (!":memory:".equals(caminho)) {
                    s.execute("PRAGMA journal_mode = WAL");
                }
            }
            return new BaseDados(c);
        } catch (ClassNotFoundException | SQLException e) {
            throw new IllegalStateException("Não foi possível abrir a base de dados " + caminho, e);
        }
    }

    /** Executa o trabalho numa transação. Se já houver uma transação aberta nesta thread, junta-se a ela. */
    public <T> T emTransacao(Supplier<T> trabalho) {
        trinco.lock();
        try {
            boolean exterior = profundidade == 0;
            if (exterior) {
                ligacao.setAutoCommit(false);
            }
            profundidade++;
            try {
                T resultado = trabalho.get();
                if (exterior) {
                    ligacao.commit();
                }
                return resultado;
            } catch (RuntimeException | Error e) {
                if (exterior) {
                    ligacao.rollback();
                }
                throw e;
            } finally {
                profundidade--;
                if (exterior) {
                    ligacao.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            throw new ErroBaseDados(e);
        } finally {
            trinco.unlock();
        }
    }

    public void emTransacao(Runnable trabalho) {
        emTransacao(() -> {
            trabalho.run();
            return null;
        });
    }

    /** Executa DDL (CREATE TABLE IF NOT EXISTS ...). */
    public void criarTabelas(String... ddl) {
        emTransacao(() -> {
            for (String sql : ddl) {
                executar(sql);
            }
        });
    }

    public int executar(String sql, Object... parametros) {
        trinco.lock();
        try (PreparedStatement ps = ligacao.prepareStatement(sql)) {
            ligar(ps, parametros);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroBaseDados(e);
        } finally {
            trinco.unlock();
        }
    }

    public <T> List<T> consultar(String sql, Mapeador<T> mapeador, Object... parametros) {
        trinco.lock();
        try (PreparedStatement ps = ligacao.prepareStatement(sql)) {
            ligar(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> lista = new ArrayList<>();
                Linha linha = new Linha(rs);
                while (rs.next()) {
                    lista.add(mapeador.mapear(linha));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new ErroBaseDados(e);
        } finally {
            trinco.unlock();
        }
    }

    public <T> Optional<T> consultarUm(String sql, Mapeador<T> mapeador, Object... parametros) {
        List<T> lista = consultar(sql, mapeador, parametros);
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    public long contar(String sql, Object... parametros) {
        return consultarUm(sql, l -> l.longo(1), parametros).orElse(0L);
    }

    private static void ligar(PreparedStatement ps, Object[] parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            Object p = parametros[i];
            int pos = i + 1;
            if (p == null) {
                ps.setObject(pos, null);
            } else if (p instanceof Instant instante) {
                ps.setLong(pos, instante.toEpochMilli());
            } else if (p instanceof Enum<?> e) {
                ps.setString(pos, e.name());
            } else if (p instanceof Boolean b) {
                ps.setInt(pos, b ? 1 : 0);
            } else {
                ps.setObject(pos, p);
            }
        }
    }

    @Override
    public void close() {
        try {
            ligacao.close();
        } catch (SQLException e) {
            throw new ErroBaseDados(e);
        }
    }

    @FunctionalInterface
    public interface Mapeador<T> {
        T mapear(Linha linha) throws SQLException;
    }

    /** Leitura tipada da linha atual de um resultado. */
    public static final class Linha {
        private final ResultSet rs;

        private Linha(ResultSet rs) {
            this.rs = rs;
        }

        public String texto(String coluna) throws SQLException {
            return rs.getString(coluna);
        }

        public long longo(String coluna) throws SQLException {
            return rs.getLong(coluna);
        }

        public long longo(int coluna) throws SQLException {
            return rs.getLong(coluna);
        }

        public Long longoOuNulo(String coluna) throws SQLException {
            long v = rs.getLong(coluna);
            return rs.wasNull() ? null : v;
        }

        public int inteiro(String coluna) throws SQLException {
            return rs.getInt(coluna);
        }

        public double real(String coluna) throws SQLException {
            return rs.getDouble(coluna);
        }

        public boolean logico(String coluna) throws SQLException {
            return rs.getInt(coluna) != 0;
        }

        public Instant instante(String coluna) throws SQLException {
            long v = rs.getLong(coluna);
            return rs.wasNull() ? null : Instant.ofEpochMilli(v);
        }
    }

    public static final class ErroBaseDados extends RuntimeException {
        public ErroBaseDados(SQLException causa) {
            super(causa.getMessage(), causa);
        }
    }
}
