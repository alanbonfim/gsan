package gcom.cadastro.atualizacaocadastral.validador;

import gcom.cadastro.atualizacaocadastral.command.AtualizacaoCadastralImovel;
import gcom.cadastro.endereco.Logradouro;
import gcom.cadastro.endereco.LogradouroBairro;
import gcom.cadastro.endereco.LogradouroCep;
import gcom.cadastro.imovel.IRepositorioImovel;
import gcom.seguranca.transacao.AlteracaoTipo;

import java.util.Map;

public class ValidadorTipoOperacaoCommand extends ValidadorCommand {
	
	private IRepositorioImovel repositorioImovel;

	public ValidadorTipoOperacaoCommand(
			AtualizacaoCadastralImovel cadastroImovel,
			Map<String, String> linha, IRepositorioImovel repositorioImovel) {
		super(cadastroImovel, linha);
		
		this.repositorioImovel = repositorioImovel;
	}

	@Override
	public void execute() throws Exception {

		String tipoOperacao = linha.get("tipoOperacao");

		if (campoNumericoInvalido(tipoOperacao)) {
			cadastroImovel.addMensagemErro("Tipo da opera��o inv�lida");
		} else {
			Integer tipo = Integer.valueOf(tipoOperacao);
			
			if (tipo == AlteracaoTipo.INCLUSAO) {
				String codigoLogradouro = linha.get("codigoLogradouro");
				String codigoMunicipio = linha.get("codigoMunicipio");
				String nomeBairro = linha.get("bairro");
				String municipio = linha.get("municipio");
				String cep = linha.get("cep");

				boolean codigosInvalidos = false;
				
				if (campoNumericoInvalido(codigoLogradouro)) {
					cadastroImovel.addMensagemErro("C�digo do logradouro inv�lido");
					codigosInvalidos = true;
				}
				
				if (campoNumericoInvalido(codigoMunicipio)) {
					cadastroImovel.addMensagemErro("C�digo do munic�pio inv�lido");
					codigosInvalidos = true;
				}

				if (campoNumericoInvalido(cep)) {
					cadastroImovel.addMensagemErro("CEP inv�lido");
					codigosInvalidos = true;
				}

				if (!codigosInvalidos) {
					Integer codigo = Integer.valueOf(codigoLogradouro);
					
					Logradouro logradouro = repositorioImovel.pesquisarLogradouro(codigo);
					
					boolean bairroInvalido = true;
					
					for (LogradouroBairro logrBairro : logradouro.getLogradouroBairros()) {
						if (logrBairro.getBairro().getNome().equalsIgnoreCase(nomeBairro)) {
							bairroInvalido = false;
						}
					}

					if (bairroInvalido) {
						cadastroImovel.addMensagemErro("Bairro inv�lido");
					}

					if (!logradouro.getMunicipio().getNome().equalsIgnoreCase(municipio)) {
						cadastroImovel.addMensagemErro("Munic�pio inv�lido");
					}

					boolean cepInvalido = true;
					
					for (LogradouroCep logrCep : logradouro.getLogradouroCeps()) {
						if (logrCep.getCep().getCodigo() == Integer.valueOf(cep)) {
							cepInvalido = false;
						}
					}

					if (cepInvalido) {
						cadastroImovel.addMensagemErro("CEP inv�lido");
					}
				}

				String inscricao = linha.get("inscricao");
				String lote = inscricao.substring(10, 14);
				String sublote = inscricao.substring(14);

				if (campoNumericoInvalido(lote)) {
					cadastroImovel.addMensagemErro("Lote inv�lido");
				}
				if (campoNumericoInvalido(sublote)) {
					cadastroImovel.addMensagemErro("Sublote inv�lido");
				}

				int qtdInscricao = 0;
				for (AtualizacaoCadastralImovel itemAtualizacao : 
					cadastroImovel.getAtualizacaoArquivo().getImoveisComInconsistencia()) {
					
					if (itemAtualizacao.getLinhaImovel("inscricao").equals(inscricao)) {
						qtdInscricao++;
					}
				}

				if (qtdInscricao > 1) {
					cadastroImovel.addMensagemErro("N�mero de inscri��o repetido");
				}

				cadastroImovel.limparDadosProprietario();
				cadastroImovel.limparDadosResponsavel();
			}
		}
	}
}
