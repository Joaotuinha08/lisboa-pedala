package simuladores;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Cliente HTTP dos simuladores. Os simuladores só falam com a plataforma pela API REST (ADR-009):
 * não importam nada de {@code lisboa_pedala}.
 */
public final class ClienteRest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final String base;
    private final HttpClient http = HttpClient.newBuilder()
            .proxy(HttpClient.Builder.NO_PROXY)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public ClienteRest(String base) {
        this.base = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    public record Resposta(int estado, JsonNode corpo) {
        public boolean ok() {
            return estado >= 200 && estado < 300;
        }
    }

    public Resposta get(String caminho) {
        return enviar(HttpRequest.newBuilder(URI.create(base + caminho)).GET());
    }

    public Resposta post(String caminho, Object corpo) {
        try {
            String texto = JSON.writeValueAsString(corpo);
            return enviar(HttpRequest.newBuilder(URI.create(base + caminho))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(texto)));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Resposta enviar(HttpRequest.Builder pedido) {
        try {
            HttpResponse<String> r = http.send(pedido.timeout(Duration.ofSeconds(30)).build(),
                    HttpResponse.BodyHandlers.ofString());
            return new Resposta(r.statusCode(), r.body().isBlank() ? null : JSON.readTree(r.body()));
        } catch (IOException e) {
            throw new IllegalStateException("Plataforma inacessível: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
