package lisboa_pedala.viagens.registo_viagens;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lisboa_pedala.partilhado.BaseDados;
import lisboa_pedala.viagens.registo_viagens.RegistoViagens.EstadoViagem;
import lisboa_pedala.viagens.registo_viagens.RegistoViagens.Viagem;

/** Persistência do Registo de Viagens. Tabelas: registo_viagens_viagens. */
class RegistoViagensRepositorio {

    private static final String COLUNAS = """
            viagem_id, utilizador_id, bicicleta_id, estacao_origem, estacao_destino, inicio, fim, valor_centimos, estado""";

    private final BaseDados bd;

    RegistoViagensRepositorio(BaseDados bd) {
        this.bd = bd;
        bd.criarTabelas("""
                CREATE TABLE IF NOT EXISTS registo_viagens_viagens (
                    viagem_id       TEXT PRIMARY KEY,
                    utilizador_id   TEXT NOT NULL,
                    bicicleta_id    TEXT NOT NULL,
                    estacao_origem  TEXT NOT NULL,
                    estacao_destino TEXT,
                    inicio          INTEGER NOT NULL,
                    fim             INTEGER,
                    valor_centimos  INTEGER,
                    estado          TEXT NOT NULL
                )""", """
                CREATE INDEX IF NOT EXISTS registo_viagens_viagens_bicicleta
                ON registo_viagens_viagens (bicicleta_id, estado, inicio)""");
    }

    BaseDados bd() {
        return bd;
    }

    void inserir(Viagem v) {
        bd.executar("INSERT INTO registo_viagens_viagens (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                v.viagemId(), v.utilizadorId(), v.bicicletaId(), v.estacaoOrigem(), v.estacaoDestino(), v.inicio(),
                v.fim(), v.valorCentimos(), v.estado());
    }

    /** A viagem aberta mais recente da bicicleta que começou até à hora indicada. */
    Optional<Viagem> abertaMaisRecenteAte(String bicicletaId, Instant hora) {
        return bd.consultarUm("SELECT " + COLUNAS + " " + """
                 FROM registo_viagens_viagens
                 WHERE bicicleta_id = ? AND estado = 'ABERTA' AND inicio <= ?
                 ORDER BY inicio DESC LIMIT 1""", RegistoViagensRepositorio::mapear, bicicletaId, hora);
    }

    void fechar(String viagemId, String estacaoDestino, Instant fim, long valorCentimos) {
        bd.executar("""
                UPDATE registo_viagens_viagens
                SET estado = 'FECHADA', estacao_destino = ?, fim = ?, valor_centimos = ?
                WHERE viagem_id = ? AND estado = 'ABERTA'""", estacaoDestino, fim, valorCentimos, viagemId);
    }

    List<Viagem> porUtilizador(String utilizadorId) {
        return bd.consultar("SELECT " + COLUNAS + " FROM registo_viagens_viagens WHERE utilizador_id = ? ORDER BY inicio",
                RegistoViagensRepositorio::mapear, utilizadorId);
    }

    List<Viagem> fechadasEntre(Instant de, Instant ate) {
        return bd.consultar("SELECT " + COLUNAS + " " + """
                 FROM registo_viagens_viagens
                 WHERE estado = 'FECHADA' AND inicio >= ? AND inicio < ? ORDER BY inicio""",
                RegistoViagensRepositorio::mapear, de, ate);
    }

    private static Viagem mapear(BaseDados.Linha l) throws SQLException {
        return new Viagem(l.texto("viagem_id"), l.texto("utilizador_id"), l.texto("bicicleta_id"),
                l.texto("estacao_origem"), l.texto("estacao_destino"), l.instante("inicio"), l.instante("fim"),
                l.longoOuNulo("valor_centimos"), EstadoViagem.valueOf(l.texto("estado")));
    }
}
