package lisboa_pedala.partilhado;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servidor REST mínimo (JDK HttpServer) onde cada componente publica as suas rotas.
 * A camada de interface fica dentro de cada componente (ADR-008); aqui só há o encaminhamento.
 */
public final class Rest {

    private final HttpServer servidor;
    private final ExecutorService executor = Executors.newFixedThreadPool(8);
    private final List<Rota> rotas = new CopyOnWriteArrayList<>();

    private Rest(HttpServer servidor) {
        this.servidor = servidor;
        servidor.createContext("/", this::tratar);
        servidor.setExecutor(executor);
    }

    /** Cria o servidor na porta indicada (0 = porta livre escolhida pelo sistema). */
    public static Rest criar(int porta) {
        try {
            return new Rest(HttpServer.create(new InetSocketAddress("127.0.0.1", porta), 0));
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível abrir a porta " + porta, e);
        }
    }

    public void get(String padrao, Manipulador m) {
        rotas.add(new Rota("GET", padrao.split("/"), m));
    }

    public void post(String padrao, Manipulador m) {
        rotas.add(new Rota("POST", padrao.split("/"), m));
    }

    public void iniciar() {
        servidor.start();
    }

    public void parar() {
        servidor.stop(0);
        executor.shutdownNow();
    }

    public int porta() {
        return servidor.getAddress().getPort();
    }

    @FunctionalInterface
    public interface Manipulador {
        /** Devolve o corpo da resposta (200) ou uma {@link Resposta} com outro estado. */
        Object tratar(Pedido pedido);
    }

    public record Resposta(int estado, Object corpo) {
    }

    public static final class Pedido {
        private final Map<String, String> parametros;
        private final Map<String, String> query;
        private final String corpo;

        private Pedido(Map<String, String> parametros, Map<String, String> query, String corpo) {
            this.parametros = parametros;
            this.query = query;
            this.corpo = corpo;
        }

        public String parametro(String nome) {
            return parametros.get(nome);
        }

        public Optional<String> query(String nome) {
            return Optional.ofNullable(query.get(nome));
        }

        public <T> T corpo(Class<T> tipo) {
            if (corpo == null || corpo.isBlank()) {
                throw new PedidoInvalido("Corpo do pedido vazio");
            }
            return Json.ler(corpo, tipo);
        }
    }

    private record Rota(String metodo, String[] partes, Manipulador manipulador) {
        Optional<Map<String, String>> corresponde(String metodoPedido, String[] caminho) {
            if (!metodo.equals(metodoPedido) || partes.length != caminho.length) {
                return Optional.empty();
            }
            Map<String, String> params = new HashMap<>();
            for (int i = 0; i < partes.length; i++) {
                String p = partes[i];
                if (p.startsWith("{") && p.endsWith("}")) {
                    params.put(p.substring(1, p.length() - 1), URLDecoder.decode(caminho[i], StandardCharsets.UTF_8));
                } else if (!p.equals(caminho[i])) {
                    return Optional.empty();
                }
            }
            return Optional.of(params);
        }
    }

    private void tratar(HttpExchange troca) throws IOException {
        int estado;
        Object corpo;
        try {
            String[] caminho = troca.getRequestURI().getPath().split("/");
            String metodo = troca.getRequestMethod();
            Rota escolhida = null;
            Map<String, String> params = null;
            for (Rota r : rotas) {
                Optional<Map<String, String>> m = r.corresponde(metodo, caminho);
                if (m.isPresent()) {
                    escolhida = r;
                    params = m.get();
                    break;
                }
            }
            if (escolhida == null) {
                estado = 404;
                corpo = Map.of("erro", "Rota inexistente: " + metodo + " " + troca.getRequestURI().getPath());
            } else {
                String texto = new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Object r = escolhida.manipulador().tratar(new Pedido(params, lerQuery(troca), texto));
                if (r instanceof Resposta resposta) {
                    estado = resposta.estado();
                    corpo = resposta.corpo();
                } else {
                    estado = 200;
                    corpo = r;
                }
            }
        } catch (PedidoInvalido | IllegalArgumentException | java.time.DateTimeException e) {
            estado = 400;
            corpo = Map.of("erro", String.valueOf(e.getMessage()));
        } catch (NaoEncontrado e) {
            estado = 404;
            corpo = Map.of("erro", e.getMessage());
        } catch (RuntimeException e) {
            estado = 500;
            corpo = Map.of("erro", String.valueOf(e.getMessage()));
        }
        byte[] bytes = Json.escrever(corpo == null ? Map.of() : corpo).getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        troca.sendResponseHeaders(estado, bytes.length);
        try (OutputStream os = troca.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> lerQuery(HttpExchange troca) {
        Map<String, String> q = new HashMap<>();
        String bruto = troca.getRequestURI().getRawQuery();
        if (bruto == null) {
            return q;
        }
        for (String par : bruto.split("&")) {
            int i = par.indexOf('=');
            if (i > 0) {
                q.put(URLDecoder.decode(par.substring(0, i), StandardCharsets.UTF_8),
                        URLDecoder.decode(par.substring(i + 1), StandardCharsets.UTF_8));
            }
        }
        return q;
    }
}
