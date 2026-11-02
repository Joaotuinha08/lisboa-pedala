package architecture;

import static architecture.Componentes.PODE_USAR;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Fitness function da regra de dados (ARCHITECTURE.md §2, último parágrafo, e §5 regra 3):
 * cada componente só lê e escreve as suas tabelas, que têm o prefixo do componente.
 *
 * <p>Não é ArchUnit (o SQL vive em texto): procura no código-fonte os nomes que aparecem depois de
 * FROM, INTO, UPDATE, JOIN, TABLE, EXISTS e ON. Convenção que torna isto verificável: palavras-chave SQL
 * em maiúsculas e nomes de tabelas em minúsculas.
 */
class TabelasPorComponenteTest {

    private static final Path FONTES = Path.of("src", "main", "java");
    private static final Pattern REFERENCIA =
            Pattern.compile("\\b(FROM|INTO|UPDATE|JOIN|TABLE|EXISTS|ON)\\s+([a-z][a-z0-9_]*)\\b(?!\\.)");

    @Test
    void cada_componente_so_usa_tabelas_com_o_seu_prefixo() throws IOException {
        assertTrue(Files.isDirectory(FONTES), "Correr a partir da raiz do projeto (falta " + FONTES + ")");
        List<String> violacoes = new ArrayList<>();
        int referencias = 0;
        for (String componente : PODE_USAR.keySet()) {
            Path pasta = FONTES.resolve(Componentes.pacote(componente).replace('.', '/'));
            String prefixo = Componentes.nomeCurto(componente) + "_";
            for (Referencia r : referencias(pasta)) {
                referencias++;
                if (!r.tabela().startsWith(prefixo)) {
                    violacoes.add(componente + " usa a tabela '" + r.tabela() + "' em " + r.ficheiro());
                }
            }
        }
        assertTrue(referencias > 0, "Nenhuma referência SQL encontrada: o teste não está a ver o código");
        assertTrue(violacoes.isEmpty(), "Acesso a tabelas de outro componente:\n" + String.join("\n", violacoes));
    }

    @Test
    void fora_dos_componentes_nao_ha_sql_de_tabelas() throws IOException {
        List<String> violacoes = new ArrayList<>();
        for (Path pasta : List.of(FONTES.resolve("lisboa_pedala/partilhado"), FONTES.resolve("simuladores"))) {
            for (Referencia r : referencias(pasta)) {
                violacoes.add(r.tabela() + " em " + r.ficheiro());
            }
        }
        assertTrue(violacoes.isEmpty(), "Tabelas usadas fora de um componente:\n" + String.join("\n", violacoes));
    }

    private record Referencia(String tabela, Path ficheiro) {
    }

    private static List<Referencia> referencias(Path pasta) throws IOException {
        List<Referencia> lista = new ArrayList<>();
        if (!Files.isDirectory(pasta)) {
            return lista;
        }
        try (Stream<Path> ficheiros = Files.walk(pasta)) {
            for (Path f : ficheiros.filter(p -> p.toString().endsWith(".java")).toList()) {
                Matcher m = REFERENCIA.matcher(Files.readString(f));
                while (m.find()) {
                    lista.add(new Referencia(m.group(2), f));
                }
            }
        }
        return lista;
    }
}
