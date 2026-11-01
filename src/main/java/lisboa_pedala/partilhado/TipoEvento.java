package lisboa_pedala.partilhado;

/**
 * Tipo de evento enviado pelas docas. Tipo enumerado partilhado (ARCHITECTURE.md §5, ficheiro 02 §9):
 * nenhum componente interpreta o tipo a partir de texto.
 */
public enum TipoEvento {
    DESBLOQUEIO,
    ENCAIXE
}
