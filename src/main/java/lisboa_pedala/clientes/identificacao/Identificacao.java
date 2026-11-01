package lisboa_pedala.clientes.identificacao;

import java.util.Optional;
import lisboa_pedala.partilhado.BaseDados;
import lisboa_pedala.partilhado.Rest;

/**
 * Identificação de Utilizadores: identifica o utilizador (app ou Navegante) e o seu tipo de cliente.
 * Pode usar: nenhum componente. ADR-004 (é a única dependência do Desbloqueio).
 */
public interface Identificacao {

    static Identificacao criar(BaseDados bd) {
        return new IdentificacaoServico(new IdentificacaoRepositorio(bd));
    }

    /** Regista um utilizador (assinante ou ocasional) com as suas credenciais. */
    void registar(NovoUtilizador utilizador);

    /** Identifica o utilizador pela credencial apresentada na doca ou na app. */
    Optional<UtilizadorIdentificado> identificar(Credencial credencial);

    /** Tipo de cliente de um utilizador conhecido. */
    TipoCliente tipoCliente(String utilizadorId);

    void publicarRotas(Rest rest);

    enum TipoCliente { ASSINANTE, OCASIONAL }

    enum MeioIdentificacao { APP, NAVEGANTE }

    record Credencial(MeioIdentificacao meio, String valor) {
    }

    record NovoUtilizador(String utilizadorId, TipoCliente tipo, String codigoApp, String cartaoNavegante) {
    }

    record UtilizadorIdentificado(String utilizadorId, TipoCliente tipo) {
    }
}
