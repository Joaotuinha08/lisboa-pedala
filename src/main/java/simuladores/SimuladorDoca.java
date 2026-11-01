package simuladores;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Doca simulada. Gera um identificador único por evento, guarda localmente até 24 h de eventos e,
 * quando a rede volta, reenvia-os (opcionalmente baralhados e com repetidos, como no cenário C3b).
 */
public final class SimuladorDoca {

    public static final Duration RETENCAO_LOCAL = Duration.ofHours(24);

    private final ClienteRest cliente;
    private final String estacaoId;
    private final String docaId;
    private final Random aleatorio;
    private final List<Map<String, Object>> guardados = new ArrayList<>();
    private boolean comRede = true;

    public SimuladorDoca(ClienteRest cliente, String estacaoId, String docaId, long semente) {
        this.cliente = cliente;
        this.estacaoId = estacaoId;
        this.docaId = docaId;
        this.aleatorio = new Random(semente);
    }

    public String estacaoId() {
        return estacaoId;
    }

    /**
     * Pedido de desbloqueio (pela app ou pelo cartão Navegante). Se for autorizado, a doca liberta a
     * bicicleta e regista o evento DESBLOQUEIO. Devolve a resposta da plataforma.
     */
    public ClienteRest.Resposta desbloquear(String bicicletaId, String meio, String credencial, Instant hora) {
        Map<String, Object> pedido = new LinkedHashMap<>();
        pedido.put("estacaoId", estacaoId);
        pedido.put("docaId", docaId);
        pedido.put("bicicletaId", bicicletaId);
        pedido.put("meio", meio);
        pedido.put("credencial", credencial);
        ClienteRest.Resposta r = cliente.post("/desbloqueios", pedido);
        if (r.ok() && r.corpo().path("autorizado").asBoolean()) {
            emitir("DESBLOQUEIO", bicicletaId, r.corpo().path("utilizadorId").asText(), hora);
        }
        return r;
    }

    /** A bicicleta encaixa nesta doca: regista o evento ENCAIXE. */
    public void encaixar(String bicicletaId, Instant hora) {
        emitir("ENCAIXE", bicicletaId, null, hora);
    }

    public void perderRede() {
        comRede = false;
    }

    public int eventosGuardados() {
        return guardados.size();
    }

    /** Recupera a rede e reenvia tudo o que estava guardado, por ordem. */
    public JsonNode recuperarRede() {
        comRede = true;
        return enviarGuardados(new ArrayList<>(guardados));
    }

    /**
     * Recupera a rede numa ligação instável: reenvia os eventos baralhados, com cerca de
     * {@code fracaoRepetidos} deles em duplicado, e repete o envio do lote inteiro (a confirmação perdeu-se).
     */
    public List<JsonNode> recuperarRedeComFalhas(double fracaoRepetidos) {
        comRede = true;
        List<Map<String, Object>> lote = new ArrayList<>(guardados);
        for (Map<String, Object> e : guardados) {
            if (aleatorio.nextDouble() < fracaoRepetidos) {
                lote.add(e);
            }
        }
        Collections.shuffle(lote, aleatorio);
        List<Map<String, Object>> copia = new ArrayList<>(lote);
        Collections.shuffle(copia, aleatorio);
        List<JsonNode> respostas = new ArrayList<>();
        respostas.add(postar(lote));   // a confirmação deste envio "perde-se"...
        respostas.add(postar(copia));  // ...por isso a doca reenvia tudo outra vez
        guardados.clear();
        return respostas;
    }

    private void emitir(String tipo, String bicicletaId, String utilizadorId, Instant hora) {
        Map<String, Object> evento = new LinkedHashMap<>();
        evento.put("eventoId", docaId + "-" + UUID.randomUUID());
        evento.put("tipo", tipo);
        evento.put("docaId", docaId);
        evento.put("estacaoId", estacaoId);
        evento.put("bicicletaId", bicicletaId);
        evento.put("utilizadorId", utilizadorId);
        evento.put("horaDoca", hora.toString());
        guardados.add(evento);
        // Memória local limitada a 24 h de eventos.
        guardados.removeIf(e -> Instant.parse((String) e.get("horaDoca")).isBefore(hora.minus(RETENCAO_LOCAL)));
        if (comRede) {
            enviarGuardados(new ArrayList<>(guardados));
        }
    }

    private JsonNode enviarGuardados(List<Map<String, Object>> lote) {
        if (lote.isEmpty()) {
            return null;
        }
        JsonNode corpo = postar(lote);
        guardados.clear(); // confirmado: pode apagar a cópia local
        return corpo;
    }

    private JsonNode postar(List<Map<String, Object>> lote) {
        ClienteRest.Resposta r = cliente.post("/eventos", Map.of("eventos", lote));
        if (!r.ok()) {
            throw new IllegalStateException("A plataforma recusou o lote: " + r.estado() + " " + r.corpo());
        }
        return r.corpo();
    }
}
