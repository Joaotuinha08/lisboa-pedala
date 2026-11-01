package lisboa_pedala.pagamentos.tarifacao;

import java.time.Duration;
import lisboa_pedala.partilhado.PedidoInvalido;

class TarifacaoServico implements Tarifacao {

    private final CatalogoTarifarios catalogo;

    TarifacaoServico(CatalogoTarifarios catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public long calcularValor(PedidoCalculo p) {
        if (p == null || p.inicio() == null || p.fim() == null || p.tipoCliente() == null || p.camara() == null) {
            throw new PedidoInvalido("inicio, fim, tipoCliente e camara são obrigatórios");
        }
        if (p.fim().isBefore(p.inicio())) {
            throw new PedidoInvalido("A viagem termina antes de começar");
        }
        Tarifario tarifario = catalogo.procurar(p.camara(), p.tipoCliente())
                .orElseThrow(() -> new PedidoInvalido(
                        "Sem tarifário para a câmara " + p.camara() + " e cliente " + p.tipoCliente()));
        return tarifario.valorCentimos(Duration.between(p.inicio(), p.fim()));
    }
}
