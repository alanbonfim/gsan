package gcom.cadastro.atualizacaocadastral.validador;

import gcom.atualizacaocadastral.ControladorAtualizacaoCadastralLocal;
import gcom.atualizacaocadastral.ImovelControleAtualizacaoCadastral;
import gcom.cadastro.SituacaoAtualizacaoCadastral;
import gcom.cadastro.atualizacaocadastral.command.AtualizacaoCadastralImovel;

public class ValidadorSituacaoImovelCommand extends ValidadorCommand {

	private ControladorAtualizacaoCadastralLocal controlador;

	public ValidadorSituacaoImovelCommand(AtualizacaoCadastralImovel cadastroImovel, ControladorAtualizacaoCadastralLocal controlador) {
		super(cadastroImovel);

		this.controlador = controlador;
	}

	@Override
	public void execute() throws Exception {

		ImovelControleAtualizacaoCadastral imovelControle = controlador.obterImovelControle(cadastroImovel.getMatricula());

		if ((cadastroImovel.getAtualizacaoArquivo().getArquivoTexto().isArquivoRetornoTransmissao() || cadastroImovel.getAtualizacaoArquivo().getArquivoTexto().isArquivoRetornoRevisita())
				&& !imovelValidoTransmissao(imovelControle)) {
			cadastroImovel.addMensagemErro("Tipo de retorno inv�lido. Im�vel n�o est� em campo, transmitido ou em revisita.");
		}

		if (cadastroImovel.getAtualizacaoArquivo().getArquivoTexto().isArquivoRetornoRevisao() && !imovelValidoRevisao(imovelControle)) {
			cadastroImovel.addMensagemErro("Tipo de retorno inv�lido. Im�vel n�o est� em revis�o.");
		}

		if (cadastroImovel.getAtualizacaoArquivo().getArquivoTexto().isArquivoRetornoFiscalizacao() && !imovelValidoFiscalizacao(imovelControle)) {
			cadastroImovel.addMensagemErro("Tipo de retorno inv�lido. Im�vel n�o est� em fiscaliza��o.");
		}
	}

	private boolean imovelValidoTransmissao(ImovelControleAtualizacaoCadastral imovelControle) {
		return (cadastroImovel.getAtualizacaoArquivo().getArquivoTexto().isArquivoRetornoTransmissao() || cadastroImovel.getAtualizacaoArquivo().getArquivoTexto().isArquivoRetornoRevisita())
				&& (imovelControle == null || (imovelControle.isImovelNovoOuNaSituacao(SituacaoAtualizacaoCadastral.TRANSMITIDO) || imovelControle.isImovelNovoOuNaSituacao(SituacaoAtualizacaoCadastral.EM_CAMPO)));
	}

	private boolean imovelValidoRevisao(ImovelControleAtualizacaoCadastral imovelControle) {
		return cadastroImovel.getAtualizacaoArquivo().getArquivoTexto().isArquivoRetornoRevisao() && (imovelControle == null || imovelControle.isImovelNovoOuNaSituacao(SituacaoAtualizacaoCadastral.EM_REVISAO));
	}

	private boolean imovelValidoFiscalizacao(ImovelControleAtualizacaoCadastral imovelControle) {
		return cadastroImovel.getAtualizacaoArquivo().getArquivoTexto().isArquivoRetornoFiscalizacao() && (imovelControle == null || imovelControle.isImovelNovoOuNaSituacao(SituacaoAtualizacaoCadastral.EM_FISCALIZACAO));
	}
}
