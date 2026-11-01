package lisboa_pedala.clientes.consulta_historico;

import java.util.List;
import lisboa_pedala.pagamentos.faturacao.Faturacao;
import lisboa_pedala.pagamentos.faturacao.Faturacao.Fatura;
import lisboa_pedala.partilhado.Rest;
import lisboa_pedala.viagens.registo_viagens.RegistoViagens;
import lisboa_pedala.viagens.registo_viagens.RegistoViagens.Viagem;

/**
 * Consulta de Histórico: mostra ao utilizador as suas viagens e faturas.
 * Pode usar: registo_viagens, faturacao. ADR-007 (não acede a Percursos). Sem tabelas próprias.
 */
public interface ConsultaHistorico {

    static ConsultaHistorico criar(RegistoViagens registo, Faturacao faturacao) {
        return new ConsultaHistoricoServico(registo, faturacao);
    }

    Historico consultar(String utilizadorId);

    void publicarRotas(Rest rest);

    record Historico(String utilizadorId, List<Viagem> viagens, List<Fatura> faturas) {
    }
}
