package lisboa_pedala.viagens.registo_viagens;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lisboa_pedala.clientes.identificacao.Identificacao;
import lisboa_pedala.pagamentos.cobranca.Cobranca;
import lisboa_pedala.pagamentos.cobranca.Cobranca.PedidoCobranca;
import lisboa_pedala.pagamentos.tarifacao.Tarifacao;
import lisboa_pedala.pagamentos.tarifacao.Tarifacao.PedidoCalculo;
import lisboa_pedala.partilhado.PedidoInvalido;

class RegistoViagensServico implements RegistoViagens {

    private final RegistoViagensRepositorio repositorio;
    private final Identificacao identificacao;
    private final Tarifacao tarifacao;
    private final Cobranca cobranca;
    private final String camara;

    RegistoViagensServico(RegistoViagensRepositorio repositorio, Identificacao identificacao, Tarifacao tarifacao,
                          Cobranca cobranca, String camara) {
        this.repositorio = repositorio;
        this.identificacao = identificacao;
        this.tarifacao = tarifacao;
        this.cobranca = cobranca;
        this.camara = camara;
    }

    @Override
    public String abrir(AberturaViagem a) {
        if (a == null || a.bicicletaId() == null || a.utilizadorId() == null || a.estacaoId() == null
                || a.inicio() == null) {
            throw new PedidoInvalido("Abertura de viagem incompleta");
        }
        String viagemId = "via-" + UUID.randomUUID();
        repositorio.inserir(new Viagem(viagemId, a.utilizadorId(), a.bicicletaId(), a.estacaoId(), null,
                a.inicio(), null, null, EstadoViagem.ABERTA));
        return viagemId;
    }

    @Override
    public ResultadoFecho fechar(FechoViagem f) {
        if (f == null || f.bicicletaId() == null || f.estacaoId() == null || f.fim() == null) {
            throw new PedidoInvalido("Fecho de viagem incompleto");
        }
        // Unidade de trabalho (§4): fecho + pedido de cobrança juntam-se à transação de quem chama.
        return repositorio.bd().emTransacao(() -> {
            Optional<Viagem> aberta = repositorio.abertaMaisRecenteAte(f.bicicletaId(), f.fim());
            if (aberta.isEmpty()) {
                return ResultadoFecho.semViagemAberta();
            }
            Viagem v = aberta.get();
            Tarifacao.TipoCliente tipo =
                    Tarifacao.TipoCliente.valueOf(identificacao.tipoCliente(v.utilizadorId()).name());
            long valor = tarifacao.calcularValor(new PedidoCalculo(v.inicio(), f.fim(), tipo, camara));
            repositorio.fechar(v.viagemId(), f.estacaoId(), f.fim(), valor);
            cobranca.registarPedido(new PedidoCobranca(v.viagemId(), v.utilizadorId(), valor));
            return new ResultadoFecho(true, v.viagemId(), valor);
        });
    }

    @Override
    public Optional<String> viagemEmCurso(String bicicletaId, Instant hora) {
        return repositorio.abertaMaisRecenteAte(bicicletaId, hora).map(Viagem::viagemId);
    }

    @Override
    public List<Viagem> viagensDe(String utilizadorId) {
        return repositorio.porUtilizador(utilizadorId);
    }

    @Override
    public List<Viagem> viagensFechadasEntre(Instant de, Instant ate) {
        return repositorio.fechadasEntre(de, ate);
    }
}
