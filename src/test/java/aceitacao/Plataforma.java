package aceitacao;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import lisboa_pedala.Main;
import simuladores.ClienteRest;

/**
 * Arranca a plataforma completa (Main, REST, SQLite num ficheiro temporário) para os testes de aceitação.
 * Os testes falam com ela como as docas, as bicicletas e a app: só por REST, através dos simuladores.
 */
final class Plataforma implements AutoCloseable {

    final Main.Aplicacao app;
    final ClienteRest rest;

    private Plataforma(Main.Aplicacao app) {
        this.app = app;
        this.rest = new ClienteRest("http://127.0.0.1:" + app.porta());
    }

    static Plataforma arrancar(Path pasta) {
        String base = pasta.resolve("lisboa_pedala_teste.db").toString();
        return new Plataforma(Main.arrancar(new Main.Configuracao(0, base, true, false, Clock.systemUTC())));
    }

    JsonNode get(String caminho) {
        ClienteRest.Resposta r = rest.get(caminho);
        if (!r.ok()) {
            throw new AssertionError("GET " + caminho + " -> " + r.estado() + " " + r.corpo());
        }
        return r.corpo();
    }

    JsonNode historico(String utilizadorId) {
        return get("/utilizadores/" + utilizadorId + "/historico");
    }

    JsonNode estacao(String estacaoId) {
        for (JsonNode e : get("/mapa")) {
            if (e.path("estacaoId").asText().equals(estacaoId)) {
                return e;
            }
        }
        throw new AssertionError("Estação inexistente: " + estacaoId);
    }

    static List<JsonNode> lista(JsonNode array) {
        List<JsonNode> l = new ArrayList<>();
        array.forEach(l::add);
        return l;
    }

    @Override
    public void close() {
        app.parar();
    }
}
