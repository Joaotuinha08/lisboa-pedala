package lisboa_pedala.pagamentos.cobranca;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import lisboa_pedala.pagamentos.faturacao.Faturacao;
import lisboa_pedala.pagamentos.faturacao.Faturacao.PedidoFatura;
import lisboa_pedala.partilhado.NaoEncontrado;
import lisboa_pedala.partilhado.PedidoInvalido;
import lisboa_pedala.partilhado.Rest;

class CobrancaServico implements Cobranca {

    private final CobrancaRepositorio repositorio;
    private final AdaptadorPagamentos pagamentos;
    private final Faturacao faturacao;
    private final Clock relogio;

    CobrancaServico(CobrancaRepositorio repositorio, AdaptadorPagamentos pagamentos, Faturacao faturacao,
                    Clock relogio) {
        this.repositorio = repositorio;
        this.pagamentos = pagamentos;
        this.faturacao = faturacao;
        this.relogio = relogio;
    }

    @Override
    public void registarPedido(PedidoCobranca p) {
        if (p == null || p.viagemId() == null || p.utilizadorId() == null || p.valorCentimos() < 0) {
            throw new PedidoInvalido("Pedido de cobrança incompleto");
        }
        // Junta-se à unidade de trabalho de quem chama (fecho da viagem). Não contacta o fornecedor.
        repositorio.bd().emTransacao(() -> {
            if (repositorio.porViagem(p.viagemId()).isPresent()) {
                return; // segunda linha de defesa contra cobranças duplicadas (UNIQUE em viagem_id)
            }
            Estado estado = p.valorCentimos() == 0 ? Estado.ISENTO : Estado.PENDENTE;
            repositorio.inserir(new EstadoCobranca("cob-" + UUID.randomUUID(), p.viagemId(), p.utilizadorId(),
                    p.valorCentimos(), estado, 0, null, relogio.instant()));
        });
    }

    @Override
    public synchronized ResumoProcessamento processarPendentes() {
        int pagas = 0;
        int faturadas = 0;
        int falhadas = 0;
        for (EstadoCobranca c : repositorio.porEstado(Estado.PENDENTE)) {
            try {
                // Chamada externa fora de qualquer transação: não prende a base enquanto espera.
                pagamentos.cobrar(c.cobrancaId(), c.utilizadorId(), c.valorCentimos());
                repositorio.mudarEstado(c.cobrancaId(), Estado.PENDENTE, Estado.PAGO);
                pagas++;
            } catch (RuntimeException e) {
                repositorio.registarFalha(c.cobrancaId(), e.getMessage()); // fica pendente: dívida a repetir
                falhadas++;
            }
        }
        for (EstadoCobranca c : repositorio.porEstado(Estado.PAGO)) {
            try {
                repositorio.bd().emTransacao(() -> {
                    faturacao.emitir(new PedidoFatura(c.cobrancaId(), c.utilizadorId(), c.valorCentimos(),
                            "Viagem " + c.viagemId()));
                    repositorio.mudarEstado(c.cobrancaId(), Estado.PAGO, Estado.FATURADO);
                });
                faturadas++;
            } catch (RuntimeException e) {
                repositorio.registarFalha(c.cobrancaId(), e.getMessage());
                falhadas++;
            }
        }
        return new ResumoProcessamento(pagas, faturadas, falhadas);
    }

    @Override
    public Optional<EstadoCobranca> estadoDaViagem(String viagemId) {
        return repositorio.porViagem(viagemId);
    }

    @Override
    public void publicarRotas(Rest rest) {
        rest.get("/viagens/{viagemId}/cobranca", p -> estadoDaViagem(p.parametro("viagemId"))
                .orElseThrow(() -> new NaoEncontrado("Sem cobrança para a viagem " + p.parametro("viagemId"))));
    }
}
