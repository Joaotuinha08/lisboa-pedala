package lisboa_pedala.partilhado;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.time.Instant;
import java.time.YearMonth;

/** Conversão JSON usada pela API REST. Instantes em ISO-8601 (UTC). */
public final class Json {

    private static final ObjectMapper MAPEADOR = criar();

    private Json() {
    }

    private static ObjectMapper criar() {
        SimpleModule tempo = new SimpleModule("tempo");
        tempo.addSerializer(Instant.class, new JsonSerializer<>() {
            @Override
            public void serialize(Instant v, JsonGenerator g, SerializerProvider p) throws IOException {
                g.writeString(v.toString());
            }
        });
        tempo.addDeserializer(Instant.class, new JsonDeserializer<>() {
            @Override
            public Instant deserialize(JsonParser p, DeserializationContext c) throws IOException {
                return Instant.parse(p.getValueAsString());
            }
        });
        tempo.addSerializer(YearMonth.class, new JsonSerializer<>() {
            @Override
            public void serialize(YearMonth v, JsonGenerator g, SerializerProvider p) throws IOException {
                g.writeString(v.toString());
            }
        });
        return new ObjectMapper()
                .registerModule(tempo)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String escrever(Object objeto) {
        try {
            return MAPEADOR.writeValueAsString(objeto);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T ler(String json, Class<T> tipo) {
        try {
            return MAPEADOR.readValue(json, tipo);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new PedidoInvalido("JSON inválido: " + e.getOriginalMessage());
        }
    }
}
