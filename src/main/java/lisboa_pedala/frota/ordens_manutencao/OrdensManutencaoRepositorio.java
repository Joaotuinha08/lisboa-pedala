package lisboa_pedala.frota.ordens_manutencao;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lisboa_pedala.frota.ordens_manutencao.OrdensManutencao.EstadoOrdem;
import lisboa_pedala.frota.ordens_manutencao.OrdensManutencao.Ordem;
import lisboa_pedala.frota.ordens_manutencao.OrdensManutencao.Origem;
import lisboa_pedala.frota.ordens_manutencao.OrdensManutencao.TipoAvaria;
import lisboa_pedala.partilhado.BaseDados;

/** Persistência das Ordens de Manutenção. Tabelas: ordens_manutencao_ordens. */
class OrdensManutencaoRepositorio {

    private static final String COLUNAS =
            "ordem_id, bicicleta_id, tipo, origem, descricao, estado, aberta_em, concluida_em";

    private final BaseDados bd;

    OrdensManutencaoRepositorio(BaseDados bd) {
        this.bd = bd;
        bd.criarTabelas("""
                CREATE TABLE IF NOT EXISTS ordens_manutencao_ordens (
                    ordem_id     TEXT PRIMARY KEY,
                    bicicleta_id TEXT NOT NULL,
                    tipo         TEXT NOT NULL,
                    origem       TEXT NOT NULL,
                    descricao    TEXT,
                    estado       TEXT NOT NULL,
                    aberta_em    INTEGER NOT NULL,
                    concluida_em INTEGER
                )""");
    }

    BaseDados bd() {
        return bd;
    }

    Optional<Ordem> abertaPara(String bicicletaId, TipoAvaria tipo) {
        return bd.consultarUm("SELECT " + COLUNAS
                        + " FROM ordens_manutencao_ordens WHERE bicicleta_id = ? AND tipo = ? AND estado = 'ABERTA'",
                OrdensManutencaoRepositorio::mapear, bicicletaId, tipo);
    }

    Optional<Ordem> porId(String ordemId) {
        return bd.consultarUm("SELECT " + COLUNAS + " FROM ordens_manutencao_ordens WHERE ordem_id = ?",
                OrdensManutencaoRepositorio::mapear, ordemId);
    }

    void inserir(Ordem o) {
        bd.executar("INSERT INTO ordens_manutencao_ordens (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                o.ordemId(), o.bicicletaId(), o.tipo(), o.origem(), o.descricao(), o.estado(), o.abertaEm(),
                o.concluidaEm());
    }

    void concluir(String ordemId, Instant quando) {
        bd.executar("UPDATE ordens_manutencao_ordens SET estado = 'CONCLUIDA', concluida_em = ? WHERE ordem_id = ?",
                quando, ordemId);
    }

    List<Ordem> abertas() {
        return bd.consultar("SELECT " + COLUNAS + " FROM ordens_manutencao_ordens WHERE estado = 'ABERTA' ORDER BY aberta_em",
                OrdensManutencaoRepositorio::mapear);
    }

    private static Ordem mapear(BaseDados.Linha l) throws SQLException {
        return new Ordem(l.texto("ordem_id"), l.texto("bicicleta_id"), TipoAvaria.valueOf(l.texto("tipo")),
                Origem.valueOf(l.texto("origem")), l.texto("descricao"), EstadoOrdem.valueOf(l.texto("estado")),
                l.instante("aberta_em"), l.instante("concluida_em"));
    }
}
