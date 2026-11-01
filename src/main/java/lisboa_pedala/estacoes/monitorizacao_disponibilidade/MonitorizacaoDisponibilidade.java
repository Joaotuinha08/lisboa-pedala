package lisboa_pedala.estacoes.monitorizacao_disponibilidade;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lisboa_pedala.partilhado.BaseDados;
import lisboa_pedala.partilhado.Rest;

/**
 * Monitorização de Disponibilidade: mede, hora a hora, quantas estações conseguem desbloquear.
 * Pode usar: nenhum componente. Serve o cenário de Disponibilidade (C4) e a multa do contrato (&gt; 10%).
 *
 * <p>Critério usado: uma estação "consegue desbloquear" numa hora se a plataforma recebeu dela algum
 * contacto nessa hora (hora de receção, não a hora da doca). O ARCHITECTURE.md não define o critério;
 * ver prompts/estacoes.monitorizacao_disponibilidade.md.
 */
public interface MonitorizacaoDisponibilidade {

    double LIMITE_MULTA = 0.10;

    static MonitorizacaoDisponibilidade criar(BaseDados bd, Clock relogio) {
        return new MonitorizacaoServico(new MonitorizacaoRepositorio(bd), relogio);
    }

    void registarEstacao(String estacaoId);

    void registarContacto(String estacaoId);

    /** Relatório da hora que contém o instante indicado. */
    RelatorioHora relatorio(Instant hora);

    void publicarRotas(Rest rest);

    record RelatorioHora(Instant inicioHora, int totalEstacoes, int estacoesComContacto,
                         List<String> estacoesSemContacto, double fracaoSemDesbloqueio, boolean multa) {
    }
}
