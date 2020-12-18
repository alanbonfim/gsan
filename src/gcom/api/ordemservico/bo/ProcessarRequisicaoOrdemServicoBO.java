package gcom.api.ordemservico.bo;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import gcom.api.ordemservico.dto.OrdemServicoDTO;
import gcom.atendimentopublico.bean.IntegracaoComercialHelper;
import gcom.atendimentopublico.ligacaoagua.LigacaoAgua;
import gcom.atendimentopublico.ordemservico.OrdemServico;
import gcom.cadastro.imovel.FiltroImovel;
import gcom.cadastro.imovel.Imovel;
import gcom.fachada.Fachada;
import gcom.gui.ActionServletException;
import gcom.micromedicao.hidrometro.FiltroHidrometro;
import gcom.micromedicao.hidrometro.Hidrometro;
import gcom.micromedicao.hidrometro.HidrometroInstalacaoHistorico;
import gcom.micromedicao.hidrometro.HidrometroLocalInstalacao;
import gcom.micromedicao.hidrometro.HidrometroProtecao;
import gcom.micromedicao.medicao.MedicaoTipo;
import gcom.seguranca.acesso.Operacao;
import gcom.seguranca.acesso.usuario.Usuario;
import gcom.util.ConstantesSistema;
import gcom.util.FachadaException;
import gcom.util.Util;
import gcom.util.filtro.ParametroSimples;

public class ProcessarRequisicaoOrdemServicoBO {

	private Fachada fachada = Fachada.getInstancia();

	private static ProcessarRequisicaoOrdemServicoBO instancia;

	public static ProcessarRequisicaoOrdemServicoBO getInstancia() {
		if (instancia == null) {
			instancia = new ProcessarRequisicaoOrdemServicoBO();
		}
		return instancia;
	}

	public boolean execute(OrdemServicoDTO dto) {
		OrdemServico ordemServico = fachada.recuperaOSPorId(dto.getId());
		Usuario usuario = fachada.pesquisarUsuario(dto.getIdUsuarioEncerramento());
		Imovel imovel = ordemServico.getRegistroAtendimento().getImovel();

		Integer idOperacao = dto.getOperacao();

		if (idOperacao != null) {

			switch (idOperacao) {

			case (Operacao.OPERACAO_RELIGACAO_AGUA_EFETUAR_INT):
				return operacaoReligacaoAguaEfetuar(ordemServico, imovel, usuario, dto);

			case (Operacao.OPERACAO_INSTALACAO_HIDROMETRO_EFETUAR_INT):
				return operacaoInstalacaoHidrometroEfetuar(ordemServico, imovel, usuario, dto);

			case (Operacao.OPERACAO_SUBSTITUICAO_HIDROMETRO_EFETUAR_INT):
				return operacaoSubstituicaoHidrometroEfetuar(ordemServico, imovel, usuario, dto);
				
			case (Operacao.OPERACAO_RESTABELECIMENTO_LIGACAO_AGUA_EFETUAR_INT):
				break;

			case (Operacao.OPERACAO_LIGACAO_AGUA_EFETUAR_INT):
				return operacaoLigacaoAguaEfetuar(ordemServico, imovel, usuario, dto);
			}
		}
		
		return false;
	}

	private boolean operacaoSubstituicaoHidrometroEfetuar2(OrdemServico ordemServico, Imovel imovel, Usuario usuario, OrdemServicoDTO dto) {
		
		        
        Integer localArmazenagemHidrometro = null;
          
        // caso o hidrometro esteja extraviado, nao pega o local de armazenagem
        //situacaodohidrometro extraviado ???
        if(dto.getHidrometro().getSituacao() != null ){  
        	localArmazenagemHidrometro = dto.getHidrometro().getLocalArmazenagem();        	
        }
        
       HidrometroInstalacaoHistorico hidrometroInstalacaoHistorico = new HidrometroInstalacaoHistorico();
		
       if (dto.getHidrometro().getNumero() != null) {

			//Constr�i o filtro para pesquisa do Hidr�metro
			FiltroHidrometro filtroHidrometro = new FiltroHidrometro();
			filtroHidrometro.adicionarParametro(new ParametroSimples(FiltroHidrometro.NUMERO_HIDROMETRO, dto.getHidrometro().getNumero() ));
	        
			//Realiza a pesquisa do Hidr�metro
			Collection colecaoHidrometro = null;
			colecaoHidrometro = fachada.pesquisar(filtroHidrometro,Hidrometro.class.getName());
			
			//verifica se o n�mero do hidr�metro n�o est� cadastrado
			if (colecaoHidrometro == null || colecaoHidrometro.isEmpty()) {
				throw new ActionServletException("atencao.numero_hidrometro_inexistente", null, dto.getHidrometro().getNumero());
			}
			
			Iterator iteratorHidrometro = colecaoHidrometro.iterator();
			Hidrometro hidrometro = (Hidrometro) iteratorHidrometro.next();
			
			FiltroImovel filtroImovel = new FiltroImovel();
			filtroImovel.adicionarCaminhoParaCarregamentoEntidade("localidade.hidrometroLocalArmazenagem");
			filtroImovel.adicionarParametro(new ParametroSimples(FiltroImovel.ID, imovel.getId()));
			
			Collection colecaoImoveis = fachada.pesquisar(filtroImovel, Imovel.class.getName());
			
			Imovel imovelComLocalidade = (Imovel) Util.retonarObjetoDeColecao(colecaoImoveis);
			
			if (imovelComLocalidade != null && imovelComLocalidade.getLocalidade().getHidrometroLocalArmazenagem() != null &&
					hidrometro.getHidrometroLocalArmazenagem() != null &&
				!hidrometro.getHidrometroLocalArmazenagem().getId().equals(imovelComLocalidade.getLocalidade().getHidrometroLocalArmazenagem().getId())) {
					throw new ActionServletException("atencao.hidrometro_local_armazenagem_imovel_diferente_hidrometro_local_armazenagem_hidrometro");
			}
			
			hidrometroInstalacaoHistorico.setHidrometro(hidrometro);
		}

		//Atualiza a entidade com os valores do formul�rio
        hidrometroInstalacaoHistorico = setFormValues(hidrometroInstalacaoHistorico,dto);
		
		HidrometroInstalacaoHistorico hidrometroSubstituicaoHistorico = new HidrometroInstalacaoHistorico();
		
			// Tipo medi��o - Liga��o �gua
		if (ordemServico.getRegistroAtendimento() == null || ordemServico.getRegistroAtendimento().getSolicitacaoTipoEspecificacao()
				.getIndicadorLigacaoAgua().equals(MedicaoTipo.LIGACAO_AGUA.shortValue())) {
			LigacaoAgua ligacaoAgua = imovel.getLigacaoAgua();
			hidrometroSubstituicaoHistorico = ligacaoAgua.getHidrometroInstalacaoHistorico();
		
			// Tipo medi��o- Po�o
		} else {
			hidrometroSubstituicaoHistorico = imovel.getHidrometroInstalacaoHistorico();
		}

		Date dataRetirada = Util.converteStringParaDate(dto.getHidrometro().getDataRetirada());
		
		hidrometroSubstituicaoHistorico.setDataRetirada(dataRetirada);
		
		if (dto.getHidrometro().getLeituraRetirada() != null){
			hidrometroSubstituicaoHistorico.setNumeroLeituraRetirada(dto.getHidrometro().getLeituraRetirada());
		}
		
		hidrometroSubstituicaoHistorico.setUltimaAlteracao(new Date());
				
		ordemServico.setIndicadorComercialAtualizado(ConstantesSistema.SIM);
		ordemServico.setValorAtual(ordemServico.getValorOriginal());
		ordemServico.setPercentualCobranca(new BigDecimal(100));
		ordemServico.setUltimaAlteracao(new Date());
		
			IntegracaoComercialHelper integracaoComercialHelper = new IntegracaoComercialHelper();
		
			integracaoComercialHelper.setHidrometroInstalacaoHistorico(hidrometroInstalacaoHistorico);
			integracaoComercialHelper.setHidrometroSubstituicaoHistorico(hidrometroSubstituicaoHistorico);
			integracaoComercialHelper.setSituacaoHidrometroSubstituido(dto.getHidrometro().getSituacao().toString());
		if(localArmazenagemHidrometro != null){
			integracaoComercialHelper.setLocalArmazenagemHidrometro(localArmazenagemHidrometro);
		}
			integracaoComercialHelper.setMatriculaImovel(imovel.getMatriculaFormatada());
			integracaoComercialHelper.setOrdemServico(ordemServico);
			integracaoComercialHelper.setQtdParcelas("1");
			integracaoComercialHelper.setUsuarioLogado(usuario);
			integracaoComercialHelper.setVeioEncerrarOS(Boolean.TRUE);	
			
			fachada.validacaoSubstituicaoHidrometro(imovel.getMatriculaFormatada(),hidrometroInstalacaoHistorico.getHidrometro().getNumero(), dto.getHidrometro().getSituacao().toString());

			fachada.atualizarOSViaApp(ordemServico.getServicoTipo().getId(), integracaoComercialHelper, usuario);	
				
		
		return false;
	}

	protected boolean operacaoReligacaoAguaEfetuar(OrdemServico ordemServico, Imovel imovel, Usuario usuario, OrdemServicoDTO ordemServicoDTO) {

		try {
			ordemServico.setIndicadorComercialAtualizado(ConstantesSistema.SIM);
			ordemServico.setValorAtual(ordemServico.getValorOriginal());
			ordemServico.setPercentualCobranca(new BigDecimal(100));
	
			Date data = Util.converteStringParaDate(ordemServicoDTO.getDataEncerramento());
			LigacaoAgua ligacaoAgua = new LigacaoAgua();
			ligacaoAgua.setId(imovel.getId());
			ligacaoAgua.setDataReligacao(data);
	
			IntegracaoComercialHelper helper = new IntegracaoComercialHelper();
			helper.setImovel(imovel);
			helper.setLigacaoAgua(ligacaoAgua);
			helper.setOrdemServico(ordemServico);
			helper.setQtdParcelas("1"); // TODO - Verificar
			helper.setUsuarioLogado(usuario);
	
			fachada.atualizarOSViaApp(ordemServico.getServicoTipo().getId(), helper, usuario);
			
			return true;
			
		} catch (FachadaException e) {
			return false;
		}
	}

	protected boolean operacaoInstalacaoHidrometroEfetuar(OrdemServico ordemServico, Imovel imovel, Usuario usuario, OrdemServicoDTO ordemServicoDTO) {

		Map<String, Object> params = new HashMap<String, Object>();
		
		params = validaInstalacaoHidrometro(ordemServicoDTO, imovel);
		
		try {
			
			ordemServico.setIndicadorComercialAtualizado(new Short("1"));
			ordemServico.setValorAtual(ordemServico.getValorOriginal());
			ordemServico.setPercentualCobranca(new BigDecimal(100));
	
			Date data = Util.converteStringParaDate(ordemServicoDTO.getDataEncerramento());
			LigacaoAgua ligacaoAgua = new LigacaoAgua();
			ligacaoAgua.setId(imovel.getId());
			ligacaoAgua.setDataReligacao(data);
	
			IntegracaoComercialHelper helper = new IntegracaoComercialHelper();

			helper.setHidrometroInstalacaoHistorico((HidrometroInstalacaoHistorico)params.get("historico"));
			helper.setVeioEncerrarOS(Boolean.TRUE);
			helper.setImovel(imovel);
			helper.setLigacaoAgua(ligacaoAgua);
			helper.setOrdemServico(ordemServico);
			helper.setQtdParcelas("1"); // TODO - Verificar
			helper.setUsuarioLogado(usuario);
	
			fachada.atualizarOSViaApp(ordemServico.getServicoTipo().getId(), helper, usuario);
			
			params.put("status", true);
			return true;
			
		} catch (FachadaException e) {
			params.put("status", false);
			return false;
		}
	}

	protected boolean operacaoSubstituicaoHidrometroEfetuar(OrdemServico ordemServico, Imovel imovel, Usuario usuario, OrdemServicoDTO ordemServicoDTO) {

		try {
			ordemServico.setIndicadorComercialAtualizado(new Short("1"));
			ordemServico.setValorAtual(ordemServico.getValorOriginal());
			ordemServico.setPercentualCobranca(new BigDecimal(100));
	
			Date data = Util.converteStringParaDate(ordemServicoDTO.getDataEncerramento());
			LigacaoAgua ligacaoAgua = new LigacaoAgua();
			ligacaoAgua.setId(imovel.getId());
			ligacaoAgua.setDataReligacao(data);
	
			IntegracaoComercialHelper helper = new IntegracaoComercialHelper();
			helper.setImovel(imovel);
			helper.setLigacaoAgua(ligacaoAgua);
			helper.setOrdemServico(ordemServico);
			helper.setQtdParcelas("1"); // TODO - Verificar
			helper.setUsuarioLogado(usuario);
	
			fachada.atualizarOSViaApp(ordemServico.getServicoTipo().getId(), helper, usuario);
			
			return true;
			
		} catch (FachadaException e) {
			return false;
		}
	}

	protected boolean operacaoLigacaoAguaEfetuar(OrdemServico ordemServico, Imovel imovel, Usuario usuario, OrdemServicoDTO ordemServicoDTO) {
			
		return true;
		
	}
	

	@SuppressWarnings("rawtypes")
	private Map<String, Object> validaInstalacaoHidrometro(OrdemServicoDTO ordemServicoDTO, Imovel imovel) {
		
		HidrometroInstalacaoHistorico hidrometroInstalacaoHistorico = new HidrometroInstalacaoHistorico();
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		if (ordemServicoDTO.getHidrometroInstalacao().getNumero() != null) {
			FiltroHidrometro filtroHidrometro = new FiltroHidrometro();
			filtroHidrometro.adicionarParametro(new ParametroSimples(FiltroHidrometro.NUMERO_HIDROMETRO, ordemServicoDTO.getHidrometroInstalacao().getNumero()));
			Collection colecaoHidrometro = fachada.pesquisar(filtroHidrometro,Hidrometro.class.getName());
			
			//verificar se o n�mero do hidr�metro n�o est� cadastrado
			if (colecaoHidrometro == null || colecaoHidrometro.isEmpty()) {
				params.put("mensagem", "Hidrometro inexistente");
				return params;
			}		
			
			Iterator iteratorHidrometro = colecaoHidrometro.iterator();
			Hidrometro hidrometro = (Hidrometro) iteratorHidrometro.next();
			
			FiltroImovel filtroImovel = new FiltroImovel();
			filtroImovel.adicionarCaminhoParaCarregamentoEntidade("localidade.hidrometroLocalArmazenagem");
			filtroImovel.adicionarParametro(new ParametroSimples(FiltroImovel.ID, imovel.getId()));
			
			Collection colecaoImoveis = fachada.pesquisar(filtroImovel, Imovel.class.getName());
			
			Imovel imovelComLocalidade = (Imovel) Util.retonarObjetoDeColecao(colecaoImoveis);
			
			if (imovelComLocalidade != null && imovelComLocalidade.getLocalidade().getHidrometroLocalArmazenagem() != null && 
				hidrometro.getHidrometroLocalArmazenagem() != null &&
				!hidrometro.getHidrometroLocalArmazenagem().getId().equals(imovelComLocalidade.getLocalidade().getHidrometroLocalArmazenagem().getId())) {
					
				params.put("mensagem", "O local de armazenagem do hidr\\u00F4metro n\\u00E3o \\u00E9 compat\\u00EDvel com a localidade do im\\u00F3vel.");
			}
			
            hidrometroInstalacaoHistorico.setHidrometro(hidrometro);
            
            params.put("historico", hidrometroInstalacaoHistorico);
		}
		
		return params;
	}
	
	private boolean isIdValido(String idCampo) {
		return idCampo != null && !idCampo.equals("") &&!idCampo.trim().equalsIgnoreCase(""+ConstantesSistema.NUMERO_NAO_INFORMADO); 
	}
	
	public HidrometroInstalacaoHistorico setFormValues(HidrometroInstalacaoHistorico hidrometroInstalacaoHistorico, OrdemServicoDTO dto) {
		
		/*
		 * Campos obrigat�rios
		 */
		
		//data instala��o
		hidrometroInstalacaoHistorico.setDataInstalacao(Util.converteStringParaDate(dto.getHidrometroInstalacao().getData()));
		
		if (dto.getHidrometro().getTipoMedicao().equals(""+MedicaoTipo.POCO)) {

		  Imovel imovel = new Imovel();
		  imovel.setId(new Integer(dto.getImovel().getMatricula()));
						
		  hidrometroInstalacaoHistorico.setImovel(imovel);
		  hidrometroInstalacaoHistorico.setLigacaoAgua(null);
					
		} else if (dto.getHidrometro().getTipoMedicao().equals(""+MedicaoTipo.LIGACAO_AGUA)) {

		  LigacaoAgua ligacaoAgua = new LigacaoAgua();
		  ligacaoAgua.setId(new Integer(dto.getImovel().getMatricula()));
						
		  hidrometroInstalacaoHistorico.setLigacaoAgua(ligacaoAgua);
		  hidrometroInstalacaoHistorico.setImovel(null);
	    }
		//medi��o tipo
		MedicaoTipo medicaoTipo = new MedicaoTipo();
		medicaoTipo.setId(Integer.parseInt(dto.getHidrometro().getTipoMedicao()));
		hidrometroInstalacaoHistorico.setMedicaoTipo(medicaoTipo);
		
		//hidr�metro local instala��o
		HidrometroLocalInstalacao hidrometroLocalInstalacao = new HidrometroLocalInstalacao();
		hidrometroLocalInstalacao.setId(dto.getHidrometroInstalacao().getLocal());		
		hidrometroInstalacaoHistorico.setHidrometroLocalInstalacao(hidrometroLocalInstalacao);
		
		//prote��o
		HidrometroProtecao hidrometroProtecao = new HidrometroProtecao();
		hidrometroProtecao.setId(dto.getHidrometroInstalacao().getProtecao());
		hidrometroInstalacaoHistorico.setHidrometroProtecao(hidrometroProtecao);
		
		//leitura instala��o
		if(dto.getHidrometroInstalacao().getLeitura() != null && !dto.getHidrometroInstalacao().getLeitura().equals("")){
		    hidrometroInstalacaoHistorico.setNumeroLeituraInstalacao(dto.getHidrometroInstalacao().getLeitura());
		}else{
			hidrometroInstalacaoHistorico.setNumeroLeituraInstalacao(0);	
		}
		
		//cavalete
		hidrometroInstalacaoHistorico.setIndicadorExistenciaCavalete(dto.getHidrometroInstalacao().getCavalete().shortValue());
		
		/*
		 * Campos opcionais 
		 */
		//leitura corte
		hidrometroInstalacaoHistorico.setNumeroLeituraCorte(null);
		
		//leitura supress�o
		hidrometroInstalacaoHistorico.setNumeroLeituraSupressao(null);
		
		//numero selo
		if (dto.getHidrometroInstalacao().getSelo() != null && !dto.getHidrometroInstalacao().getSelo().equals("")){
			hidrometroInstalacaoHistorico.setNumeroSelo(dto.getHidrometroInstalacao().getSelo().toString());
		} else {
			hidrometroInstalacaoHistorico.setNumeroSelo(null);
		}
		
		//numero lacre
		if (dto.getHidrometroInstalacao().getLacre() != null && !dto.getHidrometroInstalacao().getLacre().equals("")){
			hidrometroInstalacaoHistorico.setNumeroLacre(dto.getHidrometroInstalacao().getLacre().toString());
		} else {
			hidrometroInstalacaoHistorico.setNumeroLacre(null);
		}
		
		//tipo de rateio
		hidrometroInstalacaoHistorico.setRateioTipo(null);
		hidrometroInstalacaoHistorico.setDataImplantacaoSistema(new Date());

		//indicador instala��o substitui��o
		hidrometroInstalacaoHistorico.setIndicadorInstalcaoSubstituicao(new Short("1"));		
		
		//data �ltima altera��o
		hidrometroInstalacaoHistorico.setUltimaAlteracao(new Date());
        
        if(dto.getHidrometroInstalacao().getTrocaProtecao() != null){
            hidrometroInstalacaoHistorico.setIndicadorTrocaProtecao(dto.getHidrometroInstalacao().getTrocaProtecao().shortValue());
        }
        if(dto.getHidrometroInstalacao().getTrocaRegistro() != null){
            hidrometroInstalacaoHistorico.setIndicadorTrocaRegistro(dto.getHidrometroInstalacao().getTrocaRegistro().shortValue());
        }
		
		return hidrometroInstalacaoHistorico;
	}
}
