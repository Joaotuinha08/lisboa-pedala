package lisboa_pedala.arquitetura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static lisboa_pedala.arquitetura.Componentes.PARTILHADO;
import static lisboa_pedala.arquitetura.Componentes.PODE_USAR;
import static lisboa_pedala.arquitetura.Componentes.RAIZ;
import static lisboa_pedala.arquitetura.Componentes.padrao;
import static lisboa_pedala.arquitetura.Componentes.pacote;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Fitness functions das regras allowed-to-use (ARCHITECTURE.md §2). Correm com {@code mvn test}.
 * Cada teste diz no nome e na mensagem que regra / ADR foi violado.
 */
class RegrasArquiteturaTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importar() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(RAIZ, "simuladores");
    }

    @Test
    void estrutura_todos_os_componentes_existem_e_nao_ha_pacotes_fora_da_tabela() {
        for (String componente : PODE_USAR.keySet()) {
            boolean existe = classes.stream().anyMatch(c -> c.getPackageName().startsWith(pacote(componente)));
            assertTrue(existe, "Componente em falta no esqueleto: " + pacote(componente));
        }
        List<String> permitidos = new ArrayList<>(PODE_USAR.keySet().stream().map(Componentes::padrao).toList());
        permitidos.add(RAIZ);
        permitidos.add(PARTILHADO);
        classes().that().resideInAPackage(RAIZ + "..")
                .should().resideInAnyPackage(permitidos.toArray(String[]::new))
                .because("só existem os 15 componentes do §1, o pacote partilhado e Main (ficheiro 03, §2)")
                .check(classes);
    }

    @Test
    void rm1_cada_componente_so_importa_os_da_sua_linha() {
        List<String> violacoes = new ArrayList<>();
        for (Map.Entry<String, java.util.Set<String>> linha : PODE_USAR.entrySet()) {
            String componente = linha.getKey();
            String[] proibidos = PODE_USAR.keySet().stream()
                    .filter(outro -> !outro.equals(componente) && !linha.getValue().contains(outro))
                    .map(Componentes::padrao)
                    .toArray(String[]::new);
            ArchRule regra = noClasses().that().resideInAPackage(padrao(componente))
                    .should().dependOnClassesThat().resideInAnyPackage(proibidos);
            EvaluationResult r = regra.evaluate(classes);
            if (r.hasViolation()) {
                violacoes.add("RM1: " + componente + " só pode usar " + linha.getValue() + ":");
                r.getFailureReport().getDetails().forEach(d -> violacoes.add("    " + d));
            }
        }
        // Uma só falha que lista todas as violações, componente a componente.
        assertTrue(violacoes.isEmpty(), String.join("\n", violacoes));
    }

    @Test
    void rm2_nao_ha_ciclos_entre_componentes() {
        slices().matching(RAIZ + ".(*).(*)..").namingSlices("$1.$2")
                .should().beFreeOfCycles()
                .because("RM2: não há ciclos entre componentes")
                .check(classes);
    }

    @Test
    void rm3_desbloqueio_nao_importa_pagamentos() {
        noClasses().that().resideInAPackage(padrao("viagens.desbloqueio"))
                .should().dependOnClassesThat().resideInAPackage(RAIZ + ".pagamentos..")
                .because("RM3 / ADR-004: o desbloqueio não pode depender de pagamentos (Disponibilidade, C4)")
                .check(classes);
    }

    @Test
    void rm4_so_telemetria_e_relatorios_importam_percursos() {
        noClasses().that().resideOutsideOfPackages(padrao("viagens.percursos"), padrao("frota.telemetria"),
                        padrao("camara.relatorios_municipais"), RAIZ)
                .should().dependOnClassesThat().resideInAPackage(padrao("viagens.percursos"))
                .because("RM4 / ADR-007: o percurso é dado pessoal (RGPD)")
                .check(classes);
    }

    @Test
    void rm5_nenhum_componente_importa_main() {
        noClasses().that().resideInAnyPackage(componentesEPartilhado())
                .should().dependOnClassesThat().resideInAPackage(RAIZ)
                .because("RM5: nenhum componente importa Main")
                .check(classes);
    }

    @Test
    void rm5_so_main_vive_na_raiz_e_o_partilhado_nao_conhece_componentes() {
        classes().that().resideInAPackage(RAIZ)
                .should().haveNameMatching("lisboa_pedala\\.Main(\\$.*)?")
                .because("RM5: só lisboa_pedala.Main importa todos os componentes")
                .check(classes);
        noClasses().that().resideInAPackage(PARTILHADO)
                .should().dependOnClassesThat().resideInAnyPackage(PODE_USAR.keySet().stream()
                        .map(Componentes::padrao).toArray(String[]::new))
                .because("o pacote partilhado não pode depender de componentes (seria um atalho às regras RM1)")
                .check(classes);
    }

    @Test
    void rm6_nada_em_lisboa_pedala_importa_simuladores() {
        noClasses().that().resideInAPackage(RAIZ + "..")
                .should().dependOnClassesThat().resideInAPackage("simuladores..")
                .because("RM6 / ADR-009")
                .check(classes);
    }

    @Test
    void adr009_simuladores_so_falam_pela_api_rest() {
        noClasses().that().resideInAPackage("simuladores..")
                .should().dependOnClassesThat().resideInAPackage(RAIZ + "..")
                .because("ADR-009: docas, bicicletas e app só falam com a plataforma pela API REST")
                .check(classes);
    }

    @Test
    void adr009_cada_componente_expoe_uma_unica_interface_publica() {
        classes().that().resideInAnyPackage(componentes())
                .and().areTopLevelClasses().and().arePublic()
                .should().beInterfaces()
                .because("§5 / ADR-009: só a interface do componente é pública; o resto é package-private")
                .check(classes);

        Map<String, List<String>> publicasPorComponente = classes.stream()
                .filter(JavaClass::isTopLevelClass)
                .filter(c -> c.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.PUBLIC))
                .filter(c -> PODE_USAR.keySet().stream().anyMatch(k -> c.getPackageName().equals(pacote(k))))
                .collect(Collectors.groupingBy(JavaClass::getPackageName,
                        Collectors.mapping(JavaClass::getSimpleName, Collectors.toList())));
        for (String componente : PODE_USAR.keySet()) {
            List<String> publicas = publicasPorComponente.getOrDefault(pacote(componente), List.of());
            assertEquals(1, publicas.size(),
                    "O componente " + componente + " deve ter exatamente uma interface pública, tem " + publicas);
        }
    }

    private static String[] componentes() {
        return PODE_USAR.keySet().stream().map(Componentes::padrao).toArray(String[]::new);
    }

    private static String[] componentesEPartilhado() {
        List<String> l = new ArrayList<>(List.of(componentes()));
        l.add(PARTILHADO);
        return l.toArray(String[]::new);
    }
}
