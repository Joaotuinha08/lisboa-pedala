package lisboa_pedala.clientes.consulta_historico;

import lisboa_pedala.pagamentos.faturacao.Faturacao;
import lisboa_pedala.partilhado.Rest;
import lisboa_pedala.viagens.registo_viagens.RegistoViagens;

class ConsultaHistoricoServico implements ConsultaHistorico {

    private final RegistoViagens registo;
    private final Faturacao faturacao;

    ConsultaHistoricoServico(RegistoViagens registo, Faturacao faturacao) {
        this.registo = registo;
        this.faturacao = faturacao;
    }

    @Override
    public Historico consultar(String utilizadorId) {
        return new Historico(utilizadorId, registo.viagensDe(utilizadorId), faturacao.faturasDe(utilizadorId));
    }

    @Override
    public void publicarRotas(Rest rest) {
        rest.get("/utilizadores/{utilizadorId}/historico", p -> consultar(p.parametro("utilizadorId")));
    }
}
