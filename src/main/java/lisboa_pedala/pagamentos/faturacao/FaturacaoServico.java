package lisboa_pedala.pagamentos.faturacao;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import lisboa_pedala.partilhado.BaseDados;
import lisboa_pedala.partilhado.PedidoInvalido;

class FaturacaoServico implements Faturacao {

    static final ZoneId LISBOA = ZoneId.of("Europe/Lisbon");
    static final int ANOS_CONSERVACAO = 10;

    private final FaturacaoRepositorio repositorio;
    private final AdaptadorAT at;
    private final Clock relogio;

    FaturacaoServico(FaturacaoRepositorio repositorio, AdaptadorAT at, Clock relogio) {
        this.repositorio = repositorio;
        this.at = at;
        this.relogio = relogio;
    }

    @Override
    public Fatura emitir(PedidoFatura p) {
        if (p == null || p.referencia() == null || p.utilizadorId() == null || p.valorCentimos() <= 0) {
            throw new PedidoInvalido("Fatura sem referência, utilizador ou valor positivo");
        }
        return transacao().emTransacao(() -> repositorio.porReferencia(p.referencia()).orElseGet(() -> {
            Instant agora = relogio.instant();
            ZonedDateTime local = agora.atZone(LISBOA);
            String prefixo = "FT LP/" + local.getYear() + "/";
            long sequencia = repositorio.contarEmitidasComPrefixo(prefixo) + 1;
            Fatura f = new Fatura(prefixo + String.format("%06d", sequencia), p.referencia(), p.utilizadorId(),
                    p.valorCentimos(), p.descricao() == null ? "Viagem Lisboa Pedala" : p.descricao(), agora,
                    local.plusYears(ANOS_CONSERVACAO).toInstant(), false);
            repositorio.inserir(f);
            return f;
        }));
    }

    @Override
    public int comunicarPendentesAT() {
        int comunicadas = 0;
        for (Fatura f : repositorio.porComunicar()) {
            try {
                at.comunicar(f);
                repositorio.marcarComunicada(f.numero(), relogio.instant());
                comunicadas++;
            } catch (RuntimeException e) {
                // Falha externa: a fatura fica por comunicar e tenta-se de novo na próxima ronda (§4).
                repositorio.registarFalha(f.numero(), e.getMessage());
            }
        }
        return comunicadas;
    }

    @Override
    public List<Fatura> faturasDe(String utilizadorId) {
        return repositorio.porUtilizador(utilizadorId);
    }

    private BaseDados transacao() {
        return repositorio.bd();
    }
}
