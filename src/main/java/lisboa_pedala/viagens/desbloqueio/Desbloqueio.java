package lisboa_pedala.viagens.desbloqueio;

import java.time.Clock;
import java.time.Instant;
import lisboa_pedala.clientes.identificacao.Identificacao;
import lisboa_pedala.clientes.identificacao.Identificacao.MeioIdentificacao;
import lisboa_pedala.partilhado.BaseDados;
import lisboa_pedala.partilhado.Rest;

/**
 * Desbloqueio: valida o pedido de desbloqueio e autoriza a doca a libertar a bicicleta.
 * Pode usar: identificacao. ADR-004.
 *
 * <p>Disponibilidade (C4): não chama Tarifação, Cobrança, Faturação nem sistemas de pagamentos, e não
 * espera por nenhum deles. A viagem é aberta mais tarde, pelo evento DESBLOQUEIO que a doca envia.
 */
public interface Desbloqueio {

    static Desbloqueio criar(BaseDados bd, Identificacao identificacao, Clock relogio) {
        return new DesbloqueioServico(new DesbloqueioRepositorio(bd), identificacao, relogio);
    }

    Autorizacao pedir(PedidoDesbloqueio pedido);

    void publicarRotas(Rest rest);

    record PedidoDesbloqueio(String estacaoId, String docaId, String bicicletaId, MeioIdentificacao meio,
                             String credencial) {
    }

    record Autorizacao(boolean autorizado, String autorizacaoId, String utilizadorId, String motivo,
                       Instant hora) {
    }
}
