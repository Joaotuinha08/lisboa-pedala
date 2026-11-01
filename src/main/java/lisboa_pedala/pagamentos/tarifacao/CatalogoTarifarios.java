package lisboa_pedala.pagamentos.tarifacao;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lisboa_pedala.pagamentos.tarifacao.Tarifacao.TipoCliente;

/**
 * Escolhe o tarifário pela câmara e pelo tipo de cliente (ADR-006).
 *
 * <p>Os preços em euros não constam do enunciado nem do ARCHITECTURE.md: os valores abaixo são
 * provisórios e estão assinalados como lacuna em prompts/ e no relatório de reflexão.
 */
final class CatalogoTarifarios {

    static final String LISBOA = "lisboa";

    /** PROVISÓRIO: preço de cada bloco de 15 min do assinante, depois dos 45 min grátis. */
    static final long LISBOA_ASSINANTE_CENTIMOS_POR_BLOCO = 50;
    /** PROVISÓRIO: preço fixo de uma viagem de utilizador ocasional. */
    static final long LISBOA_OCASIONAL_CENTIMOS_POR_VIAGEM = 200;

    private final Map<String, Tarifario> porChave = new HashMap<>();

    static CatalogoTarifarios padrao() {
        CatalogoTarifarios c = new CatalogoTarifarios();
        c.registar(LISBOA, TipoCliente.ASSINANTE, new TarifarioPorBlocos(45, 15, LISBOA_ASSINANTE_CENTIMOS_POR_BLOCO));
        c.registar(LISBOA, TipoCliente.OCASIONAL, new TarifarioPorViagem(LISBOA_OCASIONAL_CENTIMOS_POR_VIAGEM));
        // Câmara nova (cenário C7): acrescentar aqui, p.ex.
        // c.registar("almada", TipoCliente.ASSINANTE, new TarifarioPorBlocos(30, 10, ...));
        return c;
    }

    void registar(String camara, TipoCliente tipo, Tarifario tarifario) {
        porChave.put(chave(camara, tipo), tarifario);
    }

    Optional<Tarifario> procurar(String camara, TipoCliente tipo) {
        return Optional.ofNullable(porChave.get(chave(camara, tipo)));
    }

    private static String chave(String camara, TipoCliente tipo) {
        return camara.toLowerCase() + "/" + tipo.name();
    }
}
