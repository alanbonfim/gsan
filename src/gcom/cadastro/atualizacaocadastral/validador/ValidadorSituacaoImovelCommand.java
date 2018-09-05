package gcom.cadastro.atualizacaocadastral.validador;

import gcom.atualizacaocadastral.ControladorAtualizacaoCadastralLocal;
import gcom.atualizacaocadastral.ImovelControleAtualizacaoCadastral;
import gcom.atualizacaocadastral.Visita;
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
		Integer quantidadeDeVisitas = controlador.obterQuantidadeDeVisitasPorImovelControle(imovelControle);
		
		if (!imovelValidoTransmissao(imovelControle))
			cadastroImovel.addMensagemErro("Im�vel n�o est� em campo, transmitido ou em revisita.");

		if (cadastroImovel.getAtualizacaoArquivo().getArquivoTexto().isArquivoRetornoRevisao() && !imovelValidoRevisao(imovelControle))
			cadastroImovel.addMensagemErro("Im�vel n�o est� em revis�o.");

		if (cadastroImovel.getAtualizacaoArquivo().getArquivoTexto().isArquivoRetornoFiscalizacao() && !imovelValidoFiscalizacao(imovelControle))
			cadastroImovel.addMensagemErro("Im�vel n�o est� em fiscaliza��o.");

		if (imovelSuperouOLimiteDeVisitas(quantidadeDeVisitas))
			cadastroImovel.addMensagemErro(String.format("Im�vel n�o pode ter mais de %d visitas", Visita.QUANTIDADE_MAXIMA_SEM_PRE_AGENDAMENTO));
	}

	private boolean imovelValidoTransmissao(ImovelControleAtualizacaoCadastral imovelControle) {
		return (cadastroImovel.getAtualizacaoArquivo().getArquivoTexto().isArquivoRetornoTransmissao() || cadastroImovel.getAtualizacaoArquivo().getArquivoTexto().isArquivoRetornoRevisita())
				&& (imovelControle == null || situacaoValida(imovelControle));
	}

	private boolean situacaoValida(ImovelControleAtualizacaoCadastral imovelControle) {
		return imovelControle.isImovelNovoOuNaSituacao(SituacaoAtualizacaoCadastral.EM_CAMPO) || 
			   imovelControle.isImovelNovoOuNaSituacao(SituacaoAtualizacaoCadastral.TRANSMITIDO) ||
			   imovelControle.isImovelNovoOuNaSituacao(SituacaoAtualizacaoCadastral.REVISITA);
	}

	private boolean imovelValidoRevisao(ImovelControleAtualizacaoCadastral imovelControle) {
		return cadastroImovel.getAtualizacaoArquivo().getArquivoTexto().isArquivoRetornoRevisao() && (imovelControle == null || imovelControle.isImovelNovoOuNaSituacao(SituacaoAtualizacaoCadastral.EM_REVISAO));
	}

	private boolean imovelValidoFiscalizacao(ImovelControleAtualizacaoCadastral imovelControle) {
		return cadastroImovel.getAtualizacaoArquivo().getArquivoTexto().isArquivoRetornoFiscalizacao() && (imovelControle == null || imovelControle.isImovelNovoOuNaSituacao(SituacaoAtualizacaoCadastral.EM_FISCALIZACAO));
	}

	private boolean imovelSuperouOLimiteDeVisitas(Integer quantidadeDeVisitas) {
		return quantidadeDeVisitas >= Visita.QUANTIDADE_MAXIMA_SEM_PRE_AGENDAMENTO;
	}
}
