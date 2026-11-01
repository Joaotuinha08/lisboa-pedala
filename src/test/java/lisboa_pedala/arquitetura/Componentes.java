package lisboa_pedala.arquitetura;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Transcrição literal da tabela do §1 do ARCHITECTURE.md (coluna "Pode usar").
 * Se a tabela mudar, muda-se aqui e em mais lado nenhum.
 */
final class Componentes {

    static final String RAIZ = "lisboa_pedala";
    static final String PARTILHADO = RAIZ + ".partilhado";

    /** componente (dominio.componente) -> componentes que pode usar. */
    static final Map<String, Set<String>> PODE_USAR = new LinkedHashMap<>();

    static {
        PODE_USAR.put("viagens.desbloqueio", Set.of("clientes.identificacao"));
        PODE_USAR.put("viagens.rececao_eventos", Set.of("viagens.registo_viagens", "estacoes.mapa_ocupacao",
                "estacoes.monitorizacao_disponibilidade"));
        PODE_USAR.put("viagens.registo_viagens", Set.of("clientes.identificacao", "pagamentos.tarifacao",
                "pagamentos.cobranca"));
        PODE_USAR.put("viagens.percursos", Set.of("viagens.registo_viagens"));
        PODE_USAR.put("pagamentos.tarifacao", Set.of());
        PODE_USAR.put("pagamentos.cobranca", Set.of("pagamentos.faturacao"));
        PODE_USAR.put("pagamentos.faturacao", Set.of());
        PODE_USAR.put("clientes.identificacao", Set.of());
        PODE_USAR.put("clientes.consulta_historico", Set.of("viagens.registo_viagens", "pagamentos.faturacao"));
        PODE_USAR.put("estacoes.mapa_ocupacao", Set.of());
        PODE_USAR.put("estacoes.monitorizacao_disponibilidade", Set.of());
        PODE_USAR.put("estacoes.redistribuicao", Set.of("estacoes.mapa_ocupacao"));
        PODE_USAR.put("frota.telemetria", Set.of("viagens.percursos", "frota.ordens_manutencao"));
        PODE_USAR.put("frota.ordens_manutencao", Set.of());
        PODE_USAR.put("camara.relatorios_municipais", Set.of("viagens.registo_viagens", "viagens.percursos"));
    }

    static final List<String> DOMINIOS = List.of("viagens", "pagamentos", "clientes", "estacoes", "frota", "camara");

    private Componentes() {
    }

    static String pacote(String componente) {
        return RAIZ + "." + componente;
    }

    /** Padrão ArchUnit do pacote e subpacotes do componente. */
    static String padrao(String componente) {
        return pacote(componente) + "..";
    }

    /** Nome curto (prefixo das tabelas), p.ex. "registo_viagens". */
    static String nomeCurto(String componente) {
        return componente.substring(componente.indexOf('.') + 1);
    }
}
