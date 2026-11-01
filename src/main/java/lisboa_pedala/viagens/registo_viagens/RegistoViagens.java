package lisboa_pedala.viagens.registo_viagens;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lisboa_pedala.clientes.identificacao.Identificacao;
import lisboa_pedala.pagamentos.cobranca.Cobranca;
import lisboa_pedala.pagamentos.tarifacao.Tarifacao;
import lisboa_pedala.partilhado.BaseDados;

/**
 * Registo de Viagens: mantém o ciclo de vida de cada viagem, da abertura ao fecho.
 * Pode usar: identificacao, tarifacao, cobranca. ADR-005, ADR-006.
 *
 * <p>Recebe eventos já limpos (sem repetidos) da Receção de Eventos: não guarda identificadores de
 * eventos. Não calcula blocos de tempo: envia as horas de início e fim à Tarifação.
 */
public interface RegistoViagens {

    /**
     * @param camara câmara cujo tarifário se aplica às viagens desta instalação (hoje só "lisboa").
     */
    static RegistoViagens criar(BaseDados bd, Identificacao identificacao, Tarifacao tarifacao, Cobranca cobranca,
                                String camara) {
        return new RegistoViagensServico(new RegistoViagensRepositorio(bd), identificacao, tarifacao, cobranca, camara);
    }

    /** Abre uma viagem (evento DESBLOQUEIO). */
    String abrir(AberturaViagem abertura);

    /**
     * Fecha a viagem da bicicleta (evento ENCAIXE): obtém o tipo de cliente, calcula o valor e regista o
     * pedido de cobrança, tudo na unidade de trabalho de quem chama. Se ainda não houver uma viagem aberta
     * que possa terminar a essa hora, devolve {@code fechada = false} e não altera nada.
     */
    ResultadoFecho fechar(FechoViagem fecho);

    /** Viagem aberta da bicicleta a essa hora, se houver. */
    Optional<String> viagemEmCurso(String bicicletaId, Instant hora);

    List<Viagem> viagensDe(String utilizadorId);

    /** Viagens fechadas cujo início está em [de, ate). */
    List<Viagem> viagensFechadasEntre(Instant de, Instant ate);

    record AberturaViagem(String bicicletaId, String utilizadorId, String estacaoId, Instant inicio) {
    }

    record FechoViagem(String bicicletaId, String estacaoId, Instant fim) {
    }

    record ResultadoFecho(boolean fechada, String viagemId, long valorCentimos) {
        public static ResultadoFecho semViagemAberta() {
            return new ResultadoFecho(false, null, 0);
        }
    }

    enum EstadoViagem { ABERTA, FECHADA }

    record Viagem(String viagemId, String utilizadorId, String bicicletaId, String estacaoOrigem,
                  String estacaoDestino, Instant inicio, Instant fim, Long valorCentimos, EstadoViagem estado) {
    }
}
