package lisboa_pedala.pagamentos.cobranca;

/** Fronteira com o fornecedor de pagamentos (MB WAY / cartão). Sistema externo. */
interface AdaptadorPagamentos {

    /**
     * Cobra o valor. A chave de idempotência garante que um reenvio da mesma cobrança não cobra
     * duas vezes no fornecedor. Lança exceção se o pagamento falhar.
     */
    void cobrar(String chaveIdempotencia, String utilizadorId, long valorCentimos);
}
