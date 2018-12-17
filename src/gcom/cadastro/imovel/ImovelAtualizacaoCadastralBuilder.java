package gcom.cadastro.imovel;

import gcom.cadastro.atualizacaocadastral.command.AtualizacaoCadastral;
import gcom.cadastro.atualizacaocadastral.command.AtualizacaoCadastralImovel;
import gcom.cadastro.endereco.FiltroLogradouroTipo;
import gcom.cadastro.endereco.LogradouroTipo;
import gcom.fachada.Fachada;
import gcom.util.Util;
import gcom.util.filtro.ParametroSimples;

import java.math.BigDecimal;

public class ImovelAtualizacaoCadastralBuilder {

	private int matricula;
	private ImovelAtualizacaoCadastral imovelAtualizacaoCadastral;

	private AtualizacaoCadastralImovel atualizacaoCadastralImovel;

	private AtualizacaoCadastral atualizacaoCadastral;

	private int tipoOperacao;

	public ImovelAtualizacaoCadastralBuilder(int matricula, AtualizacaoCadastral atualizacaoCadastral, AtualizacaoCadastralImovel atualizacaoCadastralImovel, int tipoOperacao) {
		this.matricula = matricula;
		this.imovelAtualizacaoCadastral = new ImovelAtualizacaoCadastral();
		this.atualizacaoCadastralImovel = atualizacaoCadastralImovel;
		this.atualizacaoCadastral = atualizacaoCadastral;
		this.tipoOperacao = tipoOperacao;

		buildImovel();
	}

	public ImovelAtualizacaoCadastral getImovelAtualizacaoCadastral() {
		return imovelAtualizacaoCadastral;
	}

	public void buildImovel() {
		// Linha 2
		imovelAtualizacaoCadastral.setIdImovel(matricula);
		imovelAtualizacaoCadastral.setTipoOperacao(tipoOperacao);

		String inscricao = atualizacaoCadastralImovel.getLinhaImovel("inscricao");
		imovelAtualizacaoCadastral.setIdLocalidade(Integer.parseInt(inscricao.substring(0, 3)));
		imovelAtualizacaoCadastral.setCodigoSetorComercial(Integer.parseInt(inscricao.substring(3, 6)));
		imovelAtualizacaoCadastral.setNumeroQuadra(Integer.parseInt(inscricao.substring(6, 10)));
		imovelAtualizacaoCadastral.setLote(Short.parseShort(inscricao.substring(10, 14)));
		imovelAtualizacaoCadastral.setSubLote(Short.parseShort(inscricao.substring(14, 17)));
		imovelAtualizacaoCadastral.setIdRota(atualizacaoCadastral.getIdRota());

		imovelAtualizacaoCadastral.setCodigoMunicipio(Integer.parseInt(atualizacaoCadastralImovel.getLinhaImovel("codigoMunicipio")));

		imovelAtualizacaoCadastral.setNumeroIptu(atualizacaoCadastralImovel.getLinhaImovel("numeroIPTU"));
		String contratoEnergia = atualizacaoCadastralImovel.getLinhaImovel("numeroCelpa");

		if (contratoEnergia.equals("")) {
			imovelAtualizacaoCadastral.setNumeroContratoEnergia(null);
		} else {
			try {
				imovelAtualizacaoCadastral.setNumeroContratoEnergia(Long.parseLong(contratoEnergia));
			} catch (NumberFormatException e) {
				imovelAtualizacaoCadastral.setNumeroContratoEnergia(null);
			}
		}

		imovelAtualizacaoCadastral.setNumeroPontosUtilizacao(Util.setValorInteiro(atualizacaoCadastralImovel.getLinhaImovel("numeroPontosUteis")).shortValue());
		imovelAtualizacaoCadastral.setNumeroMorador(Util.setValorInteiro(atualizacaoCadastralImovel.getLinhaImovel("numeroOcupantes")).shortValue());

		imovelAtualizacaoCadastral.setIdLogradouroTipo(Integer.parseInt(atualizacaoCadastralImovel.getLinhaImovel("idTipoLogradouroImovel")));
		imovelAtualizacaoCadastral.setDsLogradouroTipo(getDescricaoLogradouro(Integer.parseInt(atualizacaoCadastralImovel.getLinhaImovel("idTipoLogradouroImovel"))));
		imovelAtualizacaoCadastral.setDescricaoLogradouro(atualizacaoCadastralImovel.getLinhaImovel("logradouroImovel"));
		imovelAtualizacaoCadastral.setNumeroImovel(atualizacaoCadastralImovel.getLinhaImovel("numeroImovel"));
		imovelAtualizacaoCadastral.setComplementoEndereco(atualizacaoCadastralImovel.getLinhaImovel("complementoImovel"));
		imovelAtualizacaoCadastral.setNomeBairro(atualizacaoCadastralImovel.getLinhaImovel("bairro"));
		imovelAtualizacaoCadastral.setCodigoCep(Integer.parseInt(atualizacaoCadastralImovel.getLinhaImovel("cep")));
		imovelAtualizacaoCadastral.setNomeMunicipio(atualizacaoCadastralImovel.getLinhaImovel("municipio"));
		imovelAtualizacaoCadastral.setIdLogradouro(Integer.parseInt(atualizacaoCadastralImovel.getLinhaImovel("codigoLogradouro")));
		imovelAtualizacaoCadastral.setIdFonteAbastecimento(Util.setValorInteiro(atualizacaoCadastralImovel.getLinhaImovel("fonteAbastecimento")));

		if (Util.isPositivo(atualizacaoCadastralImovel.getLinhaImovel("classeSocial"))) {
			imovelAtualizacaoCadastral.setClasseSocial(Short.parseShort(atualizacaoCadastralImovel.getLinhaImovel("classeSocial")));
		}

		if (Util.isPositivo(atualizacaoCadastralImovel.getLinhaImovel("quantidadeAnimaisDomesticos"))) {
			imovelAtualizacaoCadastral.setQuantidadeAnimaisDomesticos(Short.parseShort(atualizacaoCadastralImovel.getLinhaImovel("quantidadeAnimaisDomesticos")));
		}

		if (Util.isBigDecimal(atualizacaoCadastralImovel.getLinhaImovel("areaConstruida"))) {
			imovelAtualizacaoCadastral.setAreaConstruida(new BigDecimal(atualizacaoCadastralImovel.getLinhaImovel("areaConstruida")));
		}
		if (Util.isBigDecimal(atualizacaoCadastralImovel.getLinhaImovel("volPiscina"))) {
			imovelAtualizacaoCadastral.setVolumePiscina(new BigDecimal(atualizacaoCadastralImovel.getLinhaImovel("volPiscina")));
		}

		if (Util.isBigDecimal(atualizacaoCadastralImovel.getLinhaImovel("volCisterna"))) {
			imovelAtualizacaoCadastral.setVolumeCisterna(new BigDecimal(atualizacaoCadastralImovel.getLinhaImovel("volCisterna")));
		}

		if (Util.isBigDecimal(atualizacaoCadastralImovel.getLinhaImovel("volCxDagua"))) {
			imovelAtualizacaoCadastral.setVolumeCaixaDagua(new BigDecimal(atualizacaoCadastralImovel.getLinhaImovel("volCxDagua")));
		}

		if (Util.isPositivo(atualizacaoCadastralImovel.getLinhaImovel("tipoUso"))) {
			imovelAtualizacaoCadastral.setTipoUso(Short.parseShort(atualizacaoCadastralImovel.getLinhaImovel("tipoUso")));
		}

		if (Util.isPositivo(atualizacaoCadastralImovel.getLinhaImovel("acessoHidrometro"))) {
			imovelAtualizacaoCadastral.setAcessoHidrometro(Short.parseShort(atualizacaoCadastralImovel.getLinhaImovel("acessoHidrometro")));
		}

		if (Util.isPositivo(atualizacaoCadastralImovel.getLinhaImovel("quantidadeEconomiasSocial"))) {
			imovelAtualizacaoCadastral.setQuantidadeEconomiasSocial(Integer.parseInt(atualizacaoCadastralImovel.getLinhaImovel("quantidadeEconomiasSocial")));
		}

		if (Util.isPositivo(atualizacaoCadastralImovel.getLinhaImovel("quantidadeEconomiasOutra"))) {
			imovelAtualizacaoCadastral.setQuantidadeEconomiasOutra(Integer.parseInt(atualizacaoCadastralImovel.getLinhaImovel("quantidadeEconomiasOutra")));
		}

		if (Util.isPositivo(atualizacaoCadastralImovel.getLinhaImovel("percentualAbastecimento"))) {
			imovelAtualizacaoCadastral.setPercentualAbastecimento(Short.parseShort(atualizacaoCadastralImovel.getLinhaImovel("percentualAbastecimento")));
		}
		
			imovelAtualizacaoCadastral.setObservacaoCategoria(atualizacaoCadastralImovel.getLinhaImovel("observacaoCategoria"));

		// Linha 4
		imovelAtualizacaoCadastral.setIdLigacaoAguaSituacao(Util.setValorInteiro(atualizacaoCadastralImovel.getLinhaServicos("ligacaoAguaSituacao")));
		imovelAtualizacaoCadastral.setIdLigacaoEsgotoSituacao(Util.setValorInteiro(atualizacaoCadastralImovel.getLinhaServicos("ligacaoEsgotoSituacao")));
		imovelAtualizacaoCadastral.setIdLocalInstalacaoRamal(Util.setValorInteiro(atualizacaoCadastralImovel.getLinhaServicos("localInstalacaoRamal")));

		// Linha 5
		if (atualizacaoCadastralImovel.isExisteMedidor()) {
			imovelAtualizacaoCadastral.setNumeroHidrometro(atualizacaoCadastralImovel.getLinhaMedidor("numeroHidrometro"));
			imovelAtualizacaoCadastral.setIdMarcaHidrometro(Util.setValorInteiro(atualizacaoCadastralImovel.getLinhaMedidor("marcaHidrometro")));
			imovelAtualizacaoCadastral.setIdCapacidadeHidrometro(Util.setValorInteiro(atualizacaoCadastralImovel.getLinhaMedidor("capacidadeHidrometro")));
			imovelAtualizacaoCadastral.setIdProtecaoHidrometro(Util.setValorInteiro(atualizacaoCadastralImovel.getLinhaMedidor("tipoCaixaProtecaoHidrometro")));
		}

		// Linha 6
		imovelAtualizacaoCadastral.setIdCadastroOcorrencia(Util.setValorInteiro(atualizacaoCadastralImovel.getLinhaAnormalidade("codigoAnormalidade")));
		imovelAtualizacaoCadastral.setDescricaoOutrasInformacoes(atualizacaoCadastralImovel.getLinhaAnormalidade("comentario").trim());
		imovelAtualizacaoCadastral.setCoordenadaX(atualizacaoCadastralImovel.getLinhaAnormalidade("latitude"));
		imovelAtualizacaoCadastral.setCoordenadaY(atualizacaoCadastralImovel.getLinhaAnormalidade("longitude"));
		imovelAtualizacaoCadastral.setTipoEntrevistado(atualizacaoCadastralImovel.getLinhaAnormalidade("tipoEntrevistado"));
	}

	public String getDescricaoLogradouro(int idTipoLogradouro) {
		FiltroLogradouroTipo filtro = new FiltroLogradouroTipo();
		filtro.adicionarParametro(new ParametroSimples(FiltroLogradouroTipo.ID, idTipoLogradouro));
		LogradouroTipo logradouroTipo = (LogradouroTipo) (Fachada.getInstancia().pesquisar(filtro, LogradouroTipo.class.getName()).iterator().next());

		return logradouroTipo.getDescricao();
	}
}
