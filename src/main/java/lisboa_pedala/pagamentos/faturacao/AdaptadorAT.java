package lisboa_pedala.pagamentos.faturacao;

/** Fronteira com a Autoridade Tributária (sistema externo). */
interface AdaptadorAT {

    /** Comunica a fatura. Lança exceção se a AT não estiver disponível. */
    void comunicar(Faturacao.Fatura fatura);
}
