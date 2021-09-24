package gcom.gui.cadastro.cliente;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;

import gcom.cadastro.cliente.Cliente;
import gcom.cadastro.cliente.ClienteEndereco;
import gcom.cadastro.cliente.ClienteFone;
import gcom.cadastro.cliente.ClienteImovel;
import gcom.cadastro.cliente.ClienteTipo;
import gcom.cadastro.cliente.FiltroClienteEndereco;
import gcom.cadastro.cliente.FiltroClienteImovel;
import gcom.cadastro.cliente.FiltroClienteTipo;
import gcom.cadastro.cliente.OrgaoExpedidorRg;
import gcom.cadastro.cliente.PessoaSexo;
import gcom.cadastro.cliente.Profissao;
import gcom.cadastro.cliente.RamoAtividade;
import gcom.cadastro.descricaogenerica.DescricaoGenerica;
import gcom.cadastro.descricaogenerica.FiltroDescricaoGenerica;
import gcom.cadastro.geografico.UnidadeFederacao;
import gcom.cadastro.imovel.Imovel;
import gcom.fachada.Fachada;
import gcom.gui.ActionServletException;
import gcom.gui.GcomAction;
import gcom.integracao.webservice.spc.ConsultaWebServiceTest;
import gcom.seguranca.AtributoGrupo;
import gcom.seguranca.ConsultaCdl;
import gcom.seguranca.FiltroConsultaCadastroCdl;
import gcom.seguranca.acesso.OperacaoEfetuada;
import gcom.seguranca.acesso.PermissaoEspecial;
import gcom.seguranca.acesso.usuario.FiltroUsuarioPemissaoEspecial;
import gcom.seguranca.acesso.usuario.Usuario;
import gcom.seguranca.acesso.usuario.UsuarioPermissaoEspecial;
import gcom.util.ConstantesSistema;
import gcom.util.Util;
import gcom.util.filtro.ParametroSimples;

/**
 * Description of the Class
 * 
 * @author Rodrigo
 */
public class AtualizarClienteAction extends GcomAction {

	/**
	 * Description of the Method
	 * 
	 * @param actionMapping
	 *            Description of the Parameter
	 * @param actionForm
	 *            Description of the Parameter
	 * @param httpServletRequest
	 *            Description of the Parameter
	 * @param httpServletResponse
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public ActionForward execute(ActionMapping actionMapping,
			ActionForm actionForm, HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {

		// localiza o action no objeto actionmapping
		ActionForward retorno = actionMapping.findForward("telaSucesso");
		HttpSession sessao = httpServletRequest.getSession(false);

		// Pega o form do cliente
		DynaValidatorForm form = (DynaValidatorForm) actionForm;

		Usuario usuario = (Usuario) sessao.getAttribute("usuarioLogado");
		       
		Fachada fachada = Fachada.getInstancia();

		Short tipoPessoa = (Short) form.get("tipoPessoa");
		
		String tipoPessoaForm = tipoPessoa.toString();

		FiltroClienteTipo filtroClienteTipo = new FiltroClienteTipo();

		filtroClienteTipo.adicionarParametro(new ParametroSimples(
				FiltroClienteTipo.ID, tipoPessoaForm));
		tipoPessoa = ((ClienteTipo) fachada.pesquisar(filtroClienteTipo,
				ClienteTipo.class.getName()).iterator().next())
				.getIndicadorPessoaFisicaJuridica();

		Short indicadorUsoNomeFantasiaConta = ConstantesSistema.NAO;

		if (form.get("indicadorExibicaoNomeConta") != null) {
			
			String indicadorExibicaoNomeConta = null;
			indicadorExibicaoNomeConta = (String) form.get(
					"indicadorExibicaoNomeConta").toString();

			if (indicadorExibicaoNomeConta
					.equals(Cliente.INDICADOR_NOME_FANTASIA.toString())) {

				indicadorUsoNomeFantasiaConta = ConstantesSistema.SIM;
			}
		}

		// Verifica o destino porque se o usu�rio tentar concluir o processo
		// nesta p�gina, n�o � necess�rio verificar a tela de confirma��o
		// if (destinoPagina != null && !destinoPagina.trim().equals("")) {
		if (tipoPessoa != null
				&& tipoPessoa.equals(ClienteTipo.INDICADOR_PESSOA_JURIDICA)) {
			// Vai para Pessoa Juridica mas tem dados existentes em pessoa fisica
			String cpf = (String) form.get("cpf");
			String rg = (String) form.get("rg");
			String dataEmissao = (String) form.get("dataEmissao");
			Integer idOrgaoExpedidor = (Integer) form.get("idOrgaoExpedidor");
			Integer idUnidadeFederacao = (Integer) form.get("idUnidadeFederacao");
			String dataNascimento = (String) form.get("dataNascimento");
			Integer idProfissao = (Integer) form.get("idProfissao");
			Integer idPessoaSexo = (Integer) form.get("idPessoaSexo");

			if( ( idPessoaSexo != null && idPessoaSexo != ConstantesSistema.NUMERO_NAO_INFORMADO )
				|| ( cpf != null && !cpf.trim().equalsIgnoreCase("") )
					|| ( rg != null && !rg.trim().equalsIgnoreCase("") )
						|| ( dataEmissao != null && !dataEmissao.trim().equalsIgnoreCase("") )
							|| ( dataNascimento != null && !dataNascimento.trim().equalsIgnoreCase("") )
								|| ( idOrgaoExpedidor != null && idOrgaoExpedidor != ConstantesSistema.NUMERO_NAO_INFORMADO )
									|| ( idUnidadeFederacao != null && idUnidadeFederacao != ConstantesSistema.NUMERO_NAO_INFORMADO )
										|| ( idProfissao != null && idProfissao != ConstantesSistema.NUMERO_NAO_INFORMADO ) ){

				// Limpar todo o conte�do da p�gina de pessoa f�sica
				form.set("cpf", "");
				form.set("rg", "");
				form.set("dataEmissao", "");
				form.set("idOrgaoExpedidor", new Integer(ConstantesSistema.NUMERO_NAO_INFORMADO));
				form.set("idUnidadeFederacao", new Integer(ConstantesSistema.NUMERO_NAO_INFORMADO));
				form.set("dataNascimento", "");
				form.set("idProfissao", new Integer(ConstantesSistema.NUMERO_NAO_INFORMADO));
				form.set("idPessoaSexo", new Integer(ConstantesSistema.NUMERO_NAO_INFORMADO));
			}
		}else if (tipoPessoa != null
			&& tipoPessoa.equals(ClienteTipo.INDICADOR_PESSOA_FISICA)) {
			// Vai para Pessoa Fisica mas tem dados existentes em pessoa juridica

			String cnpj = (String) form.get("cnpj");
			Integer idRamoAtividade = (Integer) form.get("idRamoAtividade");
			String codigoClienteResponsavel = (String) form.get("codigoClienteResponsavel");

			if( (cnpj != null && !cnpj.trim().equalsIgnoreCase("") )
					|| (idRamoAtividade != null && idRamoAtividade != ConstantesSistema.NUMERO_NAO_INFORMADO)
						|| (codigoClienteResponsavel != null && !codigoClienteResponsavel.trim().equalsIgnoreCase(""))) {
				// Limpa os dados da p�gina de pessoa jur�dica
				form.set("cnpj", "");
				form.set("idRamoAtividade", new Integer(ConstantesSistema.NUMERO_NAO_INFORMADO));
				form.set("codigoClienteResponsavel", "");
				form.set("nomeClienteResponsavel", "");
			}
		}

		// Pega o cliente que foi selecionado para atualiza��o
		Cliente clienteAtualizacao = (Cliente) sessao
				.getAttribute("clienteAtualizacao");

		// Pega a cole��o de endere�os do cliente
		Collection colecaoEnderecos = (Collection) sessao
				.getAttribute("colecaoEnderecos");

		// Pega a cole��o de telefones do cliente
		Collection colecaoFones = (Collection) sessao
				.getAttribute("colecaoClienteFone");

		// Cria o objeto do cliente para ser inserido
		String nome = ((String) form.get("nome")).toUpperCase();
		
		/**
		 * Autor: Paulo Diniz
		 * Data: 11/07/2011
		 * [RR2011061059]
		 * [UC0009]
		 */
		if(clienteAtualizacao.getIndicadorUso() != null && clienteAtualizacao.getIndicadorUso().intValue() == 2){
			//[FS0013] Verificar permissão especial alterar cliente inativo
			FiltroUsuarioPemissaoEspecial filtroUsuarioPemissaoEspecial = new FiltroUsuarioPemissaoEspecial();
			filtroUsuarioPemissaoEspecial.adicionarParametro(new ParametroSimples(FiltroUsuarioPemissaoEspecial.USUARIO_ID, usuario.getId()));
			filtroUsuarioPemissaoEspecial.adicionarParametro(new ParametroSimples(FiltroUsuarioPemissaoEspecial.PERMISSAO_ESPECIAL_ID, PermissaoEspecial.ALTERAR_CLIENTE_INATIVO));
			
			Collection colecaoUsuarioPermisao = fachada.pesquisar(filtroUsuarioPemissaoEspecial, UsuarioPermissaoEspecial.class.getName());
			if (colecaoUsuarioPermisao == null || colecaoUsuarioPermisao.isEmpty()) {
				throw new ActionServletException(
					"atencao.usuario.sem.permissao.para.alteracao.cliente.inativo");
			}
			
		}
		

		/**
		 * Autor: Mariana Victor
		 * Data:  28/12/2010
		 * RM_3320 - [FS0010] Verificar Duplicidade de cliente
		 */
		if (this.getSistemaParametro().getIndicadorDuplicidadeCliente().toString()
				.equals(ConstantesSistema.SIM.toString())) {
			
			FiltroUsuarioPemissaoEspecial filtroUsuarioPemissaoEspecial = new FiltroUsuarioPemissaoEspecial();
			filtroUsuarioPemissaoEspecial.adicionarParametro(new ParametroSimples(FiltroUsuarioPemissaoEspecial.USUARIO_ID, usuario.getId()));
			filtroUsuarioPemissaoEspecial.adicionarParametro(new ParametroSimples(FiltroUsuarioPemissaoEspecial.PERMISSAO_ESPECIAL_ID, PermissaoEspecial.INSERIR_CLIENTE_COM_MESMO_NOME_E_ENDERECO));
									
			Collection colecaoUsuarioPermisao = fachada.pesquisar(filtroUsuarioPemissaoEspecial, UsuarioPermissaoEspecial.class.getName());
			
			if (colecaoUsuarioPermisao == null || colecaoUsuarioPermisao.isEmpty()) {
				FiltroClienteEndereco filtroClienteEndereco = new FiltroClienteEndereco();
				filtroClienteEndereco.adicionarParametro(new ParametroSimples(FiltroClienteEndereco.NOME, nome.toUpperCase()));

				filtroClienteEndereco.adicionarCaminhoParaCarregamentoEntidade("logradouroCep.logradouro.logradouroTipo");
				filtroClienteEndereco.adicionarCaminhoParaCarregamentoEntidade("logradouroCep.logradouro.logradouroTitulo");
				filtroClienteEndereco.adicionarCaminhoParaCarregamentoEntidade("enderecoReferencia");
				filtroClienteEndereco.adicionarCaminhoParaCarregamentoEntidade("logradouroBairro.bairro.municipio.unidadeFederacao");
				filtroClienteEndereco.adicionarCaminhoParaCarregamentoEntidade("logradouroCep.cep");
				filtroClienteEndereco.adicionarCaminhoParaCarregamentoEntidade("perimetroInicial.logradouroTipo");
				filtroClienteEndereco.adicionarCaminhoParaCarregamentoEntidade("perimetroInicial.logradouroTitulo");
				filtroClienteEndereco.adicionarCaminhoParaCarregamentoEntidade("perimetroFinal.logradouroTipo");
				filtroClienteEndereco.adicionarCaminhoParaCarregamentoEntidade("perimetroFinal.logradouroTitulo");
				filtroClienteEndereco.adicionarCaminhoParaCarregamentoEntidade("enderecoTipo");
				filtroClienteEndereco.adicionarCaminhoParaCarregamentoEntidade("cliente");
				
				Collection<ClienteEndereco> colecaoClienteEndereco = fachada.pesquisar(filtroClienteEndereco, ClienteEndereco.class.getName());
				
				if (colecaoClienteEndereco != null && !colecaoClienteEndereco.isEmpty()){
					Iterator iterator = colecaoClienteEndereco.iterator();
					
					while (iterator.hasNext()) {
						ClienteEndereco clienteEnderecoIterator = (ClienteEndereco) iterator.next();
						
						Iterator iteratorEnderecos = colecaoEnderecos.iterator();
						while (iteratorEnderecos.hasNext()) {
							ClienteEndereco clienteEndereco = (ClienteEndereco) iteratorEnderecos
									.next();
							
							if (clienteEndereco.getEnderecoFormatado().equals(
									clienteEnderecoIterator.getEnderecoFormatado())
									&& !clienteAtualizacao.getId().equals(
											clienteEnderecoIterator.getCliente().getId())) {
								throw new ActionServletException("atencao.duplicidade.cliente", null,
									"Cliente");
							}
						}
					}
				}	
				
			}
			
		}
		
		/**
		 * Autor: Mariana Victor
		 * Data:  28/12/2010
		 * RM_3320 - [FS0011] Verificar Nome de Cliente com menos de 10 posi��es
		 */
		if (this.getSistemaParametro().getIndicadorNomeMenorDez().toString()
				.equals(ConstantesSistema.NAO.toString())) {
			
			FiltroUsuarioPemissaoEspecial filtroUsuarioPemissaoEspecial = new FiltroUsuarioPemissaoEspecial();
			filtroUsuarioPemissaoEspecial.adicionarParametro(new ParametroSimples(FiltroUsuarioPemissaoEspecial.USUARIO_ID, usuario.getId()));
			filtroUsuarioPemissaoEspecial.adicionarParametro(new ParametroSimples(FiltroUsuarioPemissaoEspecial.PERMISSAO_ESPECIAL_ID, PermissaoEspecial.INSERIR_NOMES_COM_MENOS_DE_10_CARACTERES));
									
			Collection colecaoUsuarioPermisao = fachada.pesquisar(filtroUsuarioPemissaoEspecial, UsuarioPermissaoEspecial.class.getName());
			
			if (colecaoUsuarioPermisao == null || colecaoUsuarioPermisao.isEmpty()) {
				String nomeFormatado= nome.replaceAll(" ", "");
				if (nomeFormatado.length() < 10) {
					throw new ActionServletException("atencao.nome.sobrenome.cliente.menos.dez.posicoes",
							null, nome);
				}
			}
			
		}

		/**
		 * Autor: Mariana Victor
		 * Data:  28/12/2010
		 * RM_3320 - [FS0012] Verificar Nome de Cliente com Descri��o Gen�rica
		 */
		if (this.getSistemaParametro().getIndicadorNomeClienteGenerico().toString()
				.equals(ConstantesSistema.NAO.toString())) {
			
			FiltroUsuarioPemissaoEspecial filtroUsuarioPemissaoEspecial = new FiltroUsuarioPemissaoEspecial();
			filtroUsuarioPemissaoEspecial.adicionarParametro(new ParametroSimples(FiltroUsuarioPemissaoEspecial.USUARIO_ID, usuario.getId()));
			filtroUsuarioPemissaoEspecial.adicionarParametro(new ParametroSimples(FiltroUsuarioPemissaoEspecial.PERMISSAO_ESPECIAL_ID, PermissaoEspecial.INSERIR_NOME_CLIENTE_GENERICO));
									
			Collection colecaoUsuarioPermisao = fachada.pesquisar(filtroUsuarioPemissaoEspecial, UsuarioPermissaoEspecial.class.getName());
			
			if (colecaoUsuarioPermisao == null || colecaoUsuarioPermisao.isEmpty()) {
				FiltroDescricaoGenerica filtroDescricaoGenerica = new FiltroDescricaoGenerica();
				Collection colecaoDescricaoGenerica = fachada.pesquisar(filtroDescricaoGenerica, DescricaoGenerica.class.getName());
				
				if (colecaoDescricaoGenerica != null || !colecaoDescricaoGenerica.isEmpty()) {
					String nomeFormatado= nome.replaceAll(" ", "");
					Iterator iteratorDescricaoGenerica = colecaoDescricaoGenerica.iterator();
					
					while (iteratorDescricaoGenerica.hasNext()) {
						DescricaoGenerica descricaoGenerica = (DescricaoGenerica) iteratorDescricaoGenerica.next();
						String nomeGenerico = descricaoGenerica.getNomeGenerico();
						String nomeGenericoFormatado = nomeGenerico.replaceAll(" ", "");
						
						if (nomeGenerico.equalsIgnoreCase(nome)
								|| nomeGenericoFormatado.equalsIgnoreCase(nome)
								|| nomeGenerico.equalsIgnoreCase(nomeFormatado)
								|| nomeGenericoFormatado.equalsIgnoreCase(nomeFormatado)) {
							throw new ActionServletException("atencao.nome.cliente.descricao.generica",
									null, "Nome do Cliente");		
						}
					}
					
				}
				
			}
			
		}
		
		String nomeAbreviado = ((String) form.get("nomeAbreviado")).toUpperCase();
		String rg = (String) form.get("rg");
		String cpf = (String) form.get("cpf");
		if(cpf != null && cpf.trim().equals("")){
			cpf = null;
		}
		String dataEmissao = (String) form.get("dataEmissao");
		String dataNascimento = (String) form.get("dataNascimento");
		String cnpj = (String) form.get("cnpj");
		if(cnpj != null && cnpj.trim().equals("")){
			cnpj = null;
		}
		String indicadorAcaoCobranca =  (String)form.get("indicadorAcaoCobranca");

		String email = (String) form.get("email");
		
		Short indicadorUso = null;
		
		if(form.get("indicadorUso") != null){
			indicadorUso = new Short((String) form
					.get("indicadorUso"));
		}else{
			indicadorUso = new Short("1");	
		}
		
		Short indicadorAcrescimos = null;
		if(form.get("indicadorAcrescimos") != null){
			indicadorAcrescimos = new Short((String)form
					.get("indicadorAcrescimos"));
		} else {
			indicadorAcrescimos = new Short("1");
		}

		// Verificar se o usu�rio digitou os 4 campos relacionados com o RG de
		// pessoa f�sica ou se ele n�o digitou nenhum

		Integer idOrgaoExpedidor = (Integer) form
				.get("idOrgaoExpedidor");
		Integer idUnidadeFederacao = (Integer) form
				.get("idUnidadeFederacao");

		if( ! ( ( (rg != null && !rg.trim().equalsIgnoreCase(""))
					&& (idOrgaoExpedidor != null && !idOrgaoExpedidor.equals(ConstantesSistema.NUMERO_NAO_INFORMADO))) 
						&& (idUnidadeFederacao != null && !idUnidadeFederacao.equals(ConstantesSistema.NUMERO_NAO_INFORMADO)) 
							|| ((rg != null && rg.trim().equalsIgnoreCase(""))
									&& (idOrgaoExpedidor != null && idOrgaoExpedidor.equals(ConstantesSistema.NUMERO_NAO_INFORMADO)) 
										&& (idUnidadeFederacao != null && idUnidadeFederacao.equals(ConstantesSistema.NUMERO_NAO_INFORMADO)))) ){
			throw new ActionServletException(
					"atencao.rg_campos_relacionados.nao_preenchidos");
		}

		OrgaoExpedidorRg orgaoExpedidorRg = null;
		if (form.get("idOrgaoExpedidor") != null
				&& ((Integer) form.get("idOrgaoExpedidor")).intValue() > 0) {
			orgaoExpedidorRg = new OrgaoExpedidorRg();
			orgaoExpedidorRg.setId((Integer) form
					.get("idOrgaoExpedidor"));
		}

		PessoaSexo pessoaSexo = null;
		if (form.get("idPessoaSexo") != null
				&& ((Integer) form.get("idPessoaSexo")).intValue() > 0) {
			pessoaSexo = new PessoaSexo();
			pessoaSexo.setId((Integer) form.get("idPessoaSexo"));
		}

		Profissao profissao = null;
		if (form.get("idProfissao") != null
				&& ((Integer) form.get("idProfissao")).intValue() > 0) {
			profissao = new Profissao();
			profissao.setId((Integer) form.get("idProfissao"));
		}

		UnidadeFederacao unidadeFederacao = null;
		if (form.get("idUnidadeFederacao") != null
				&& ((Integer) form.get("idUnidadeFederacao")).intValue() > 0) {
			unidadeFederacao = new UnidadeFederacao();
			unidadeFederacao.setId((Integer) form.get("idUnidadeFederacao"));
		}

		ClienteTipo clienteTipo = new ClienteTipo();
		clienteTipo.setId(new Integer(((Short) form.get("tipoPessoa")).intValue()));

		RamoAtividade ramoAtividade = null;
		if (form.get("idRamoAtividade") != null
				&& ((Integer) form.get("idRamoAtividade")).intValue() > 0) {
			ramoAtividade = new RamoAtividade();
			ramoAtividade.setId((Integer) form
					.get("idRamoAtividade"));
		}
 
		Cliente clienteResponsavel = null;
		if (form.get("codigoClienteResponsavel") != null
				&& !((String) form.get("codigoClienteResponsavel")).trim().equalsIgnoreCase("")) {
			// Cria o objeto do cliente respons�vel
			clienteResponsavel = new Cliente();
			clienteResponsavel.setId(new Integer((String) form
					.get("codigoClienteResponsavel")));
		}

		// Verifica se o usu�rio adicionou um endere�o de correspond�ncia
		Long enderecoCorrespondenciaSelecao = (Long) form
				.get("enderecoCorrespondenciaSelecao");

		if (enderecoCorrespondenciaSelecao == null
				|| enderecoCorrespondenciaSelecao == 0) {
			throw new ActionServletException(
					"atencao.endereco_correspondencia.nao_selecionado");
		}

		SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");

		// Verifica se o nome do Cliente � o mesmo encontrado na R. Federal de acordo com o CPF digitado
		ConsultaCdl clienteCadastradoNaReceita = new ConsultaCdl();
		//String mensagemRetornoReceita = null;
		try {
			
			if(cpf != null && cpf.equals("")){
				cpf = null;
			}

			if(cnpj != null && cnpj.equals("")){
				cnpj = null;
			}
			
			Cliente cliente = new Cliente(
					// Nome
					nome,
					
					// Nome Abreviado
					nomeAbreviado,
					
					// CPF
					cpf,
					
					// RG
					rg,
					
					// Data de Emiss�o do RG
					dataEmissao != null && !dataEmissao.trim().equalsIgnoreCase("") 
						? formatoData.parse(dataEmissao): null,
								
					// Data de Nascimento
					dataNascimento != null && !dataNascimento.trim().equalsIgnoreCase("") 
						? formatoData.parse(dataNascimento) : null, 
					
					// CNPJ
					cnpj, 
					
					// Email
					email, 
					
					// Indicador Uso
					indicadorUso,
					
					// Indicador Acrescimos
					indicadorAcrescimos,
					
					// Data da �ltima Altera��o
					clienteAtualizacao.getUltimaAlteracao(),
					
					// �rg�o Expedidor RG
					orgaoExpedidorRg,
					
					// Cliente Respons�vel
					clienteResponsavel, 
					
					// Sexo
					pessoaSexo,
					
					// Profiss�o
					profissao,
					
					// Unidade Federa��o
					unidadeFederacao, 
					
					// Tipo do Cliente
					clienteTipo,
					
					// Ramo de Atividade
					ramoAtividade,
					indicadorUsoNomeFantasiaConta);

			// Seta o id do cliente atualizado para ser identificado no BD na atualiza��o
			cliente.setId(clienteAtualizacao.getId());
			
			// Numero do NIS
			String numeroNIS = (String) form.get("numeroNIS");
			if (numeroNIS != null && !numeroNIS.trim().equals("")) {
				cliente.setNumeroNIS(Integer.valueOf(numeroNIS));
			}
			
			// Indicador Bolsa Familia
			if (form.get("indicadorBolsaFamilia") != null && !form.get("indicadorBolsaFamilia").equals("")) {
				cliente.setIndicadorBolsaFamilia(new Short((String) form.get("indicadorBolsaFamilia")));
			} else {
				cliente.setIndicadorBolsaFamilia(ConstantesSistema.NAO);
			}
			
			cliente.setIndicadorAcaoCobranca(new Integer (indicadorAcaoCobranca).shortValue());
			
			cliente.setIndicadorGeraArquivoTexto(clienteAtualizacao.getIndicadorGeraArquivoTexto());
			
			cliente.setDiaVencimento(clienteAtualizacao.getDiaVencimento());
			
//			 Permissao Especial Validar Acrescimos Impontualidade
			boolean validarAcrescimoImpontualidade = Fachada.getInstancia().verificarPermissaoValAcrescimosImpontualidade(usuario);
			
			httpServletRequest.setAttribute("validarAcrescimoImpontualidade",validarAcrescimoImpontualidade);

            
            if (form.get("diaVencimento") != null
                    && !(form.get("diaVencimento").equals(""))){
                String diaVencimento = (String)form.get("diaVencimento"); 
                cliente.setDataVencimento( new Short(diaVencimento));
            }else{
                cliente.setDataVencimento(null);
            }
        
        	//Nome da M�e	
            if (form.get("nomeMae") != null
                        && (!(form.get("nomeMae").equals("")))) {
            	cliente.setNomeMae(((String)form.get("nomeMae")).toUpperCase());
             }
            
			if (form.get("indicadorGeraFaturaAntecipada") != null && !form.get("indicadorGeraFaturaAntecipada").equals("")) {
				cliente.setIndicadorGeraFaturaAntecipada(new Short((String) form.get("indicadorGeraFaturaAntecipada")));
			} else {
				cliente.setIndicadorGeraFaturaAntecipada(ConstantesSistema.NAO);
			}
			
			if (form.get("diaVencimento") != null && !(form.get("diaVencimento").equals("")) && 
			   (form.get("indicadorVencimentoMesSeguinte") != null && !form.get("indicadorVencimentoMesSeguinte").equals(""))) {
				cliente.setIndicadorVencimentoMesSeguinte(new Short((String) form.get("indicadorVencimentoMesSeguinte")));
			} else {
				cliente.setIndicadorVencimentoMesSeguinte(ConstantesSistema.NAO);
			}
			
			 if (form.get("indicadorAcaoCobranca") != null
	                    && !(form.get("indicadorAcaoCobranca").equals(""))){
				 cliente.setIndicadorAcaoCobranca(new Integer ((String)form.get("indicadorAcaoCobranca")).shortValue());
			 }

			 if (form.get("indicadorPermiteNegativacao") != null
						&& form.get("indicadorPermiteNegativacao").equals(ConstantesSistema.SIM.toString())){
					
					cliente.setIndicadorPermiteNegativacao(ConstantesSistema.NAO);
				} else {
					cliente.setIndicadorPermiteNegativacao(ConstantesSistema.SIM);
				}
			
			//*************************************************************************
			// Autor: Ivan Sergio
			// Data: 06/08/2009
			// CRC2103
			// Verifica se a funcionalidade esta sendo executada dentro de um popup
			//*************************************************************************
			if (sessao.getAttribute("POPUP") != null) {
				if (sessao.getAttribute("POPUP").equals("true")) {
					Integer idImovel = null;
					if (sessao.getAttribute("idImovel") != null && 
							!sessao.getAttribute("idImovel").equals("")) {
						idImovel = new Integer(sessao.getAttribute("idImovel").toString());
					}else if (sessao.getAttribute("imovelAtualizacao") != null) {
						Imovel imovel = (Imovel) sessao.getAttribute("imovelAtualizacao");
						idImovel = new Integer(imovel.getId());
					}
					
					if (idImovel == null) {
						cliente.setId2(-1);
						colecaoEnderecos = this.setaId2ClienteEnderecos(colecaoEnderecos, -1);
						colecaoFones = this.setaId2ClienteFones(colecaoFones, -1);
					} else {
						//Integer idImovel = new Integer(sessao.getAttribute("idImovel").toString());
						cliente.setId2(idImovel);
						colecaoEnderecos = this.setaId2ClienteEnderecos(colecaoEnderecos, idImovel);
						colecaoFones = this.setaId2ClienteFones(colecaoFones, idImovel);
						
						// Recupera o Tipo de Relacao do Cliente
						FiltroClienteImovel filtro = new FiltroClienteImovel();
						filtro.adicionarCaminhoParaCarregamentoEntidade(
								FiltroClienteImovel.CLIENTE_RELACAO_TIPO);
						filtro.adicionarParametro(new ParametroSimples(
								FiltroClienteImovel.CLIENTE_ID, cliente.getId()));
						filtro.adicionarParametro(new ParametroSimples(
								FiltroClienteImovel.IMOVEL_ID, idImovel));
						
						ClienteImovel clienteImovel = (ClienteImovel) Util.retonarObjetoDeColecao(
								fachada.pesquisar(filtro, ClienteImovel.class.getName()));
						
						if (clienteImovel != null) {
							if (clienteImovel.getClienteRelacaoTipo() != null) {
								Integer idAtributoGrupo = null;
								switch (clienteImovel.getClienteRelacaoTipo().getId()) {
								case 1:
									idAtributoGrupo = AtributoGrupo.ATRIBUTOS_DO_PROPRIETARIO;
									break;
								case 2:
									idAtributoGrupo = AtributoGrupo.ATRIBUTOS_DO_USUARIO;
									break;
								}
								
								if (idAtributoGrupo != null) {
									AtributoGrupo atributoGrupo = new AtributoGrupo();
									atributoGrupo.setId(idAtributoGrupo);
									
									OperacaoEfetuada operacaoEfetuada = new OperacaoEfetuada();
									operacaoEfetuada.setAtributoGrupo(atributoGrupo);
									
									cliente.setOperacaoEfetuada(operacaoEfetuada);
								}
							}
						}
					}
				}
			}
			
			/**
			 * Autor: Rodrigo Cabral
			 * Data: 20/10/2010
			 * CRC4476
			 */
			String confirmado = null;
			if ( httpServletRequest.getParameter("confirmado") != null  ) {
				confirmado = httpServletRequest.getParameter("confirmado");
			}
			
			ConsultaCdl consultaCdl = null;
			FiltroConsultaCadastroCdl filtroConsultaCadastroCdl = new FiltroConsultaCadastroCdl();
			
			Short indicadorConsultaDocumentoReceita = this.getSistemaParametro().getIndicadorConsultaDocumentoReceita();
			
			if(cpf != null || cnpj != null){
				
				if (cpf != null){
					filtroConsultaCadastroCdl.adicionarParametro(
						new ParametroSimples(FiltroConsultaCadastroCdl.CPF_CLIENTE, cpf));
				}
				
				if (cnpj != null){
					filtroConsultaCadastroCdl.adicionarParametro(
						new ParametroSimples(FiltroConsultaCadastroCdl.CNPJ_CLIENTE, cnpj));
				}
				
				Collection colecaoConsultaCadastroCdl = 
					fachada.pesquisar(filtroConsultaCadastroCdl, ConsultaCdl.class.getName());
				
				consultaCdl = (ConsultaCdl)Util.retonarObjetoDeColecao(colecaoConsultaCadastroCdl);
			}else{
				indicadorConsultaDocumentoReceita = ConstantesSistema.NAO;
			}
			
			if (confirmado == null && 
				consultaCdl == null &&
				indicadorConsultaDocumentoReceita.toString().equals(ConstantesSistema.SIM.toString())){
				
				ConsultaWebServiceTest consultaWebService = new ConsultaWebServiceTest();
				
				try {
					if (cpf != null){
						clienteCadastradoNaReceita = consultaWebService.consultarPessoaFisica(nome,cpf);
						System.out.println("CONSULTA SPC ATUALIZAR CLIENTE CPF: "+cpf);
					}else if (cnpj != null){
						clienteCadastradoNaReceita = consultaWebService.consultaPessoaJuridica(nome,cnpj);
						System.out.println("CONSULTA SPC ATUALIZAR CLIENTE CNPJ: "+cnpj);
					}
				} catch (Exception e) {
					e.printStackTrace();
					clienteCadastradoNaReceita.setMensagemRetorno("Erro ao consultar o CDL.");
				}
					
				if(clienteCadastradoNaReceita.getNomeCliente() != null && 
					!clienteCadastradoNaReceita.getNomeCliente().equals("NOME NAO CADASTRADO") &&
					!clienteCadastradoNaReceita.getNomeCliente().equals("EMPRESA NAO CADASTRADA") ){
					
					System.out.println("NOME RETORNADO CDL "+cpf+":"+clienteCadastradoNaReceita.getNomeCliente());
					
					form.set("nomeClienteReceitaFederal" , clienteCadastradoNaReceita.getNomeCliente());
				
				}else{
					clienteCadastradoNaReceita.setNomeCliente(null);
					clienteCadastradoNaReceita.setMensagemRetorno("Erro ao consultar o CDL.");
				}
				
				sessao.setAttribute("clienteCadastradoNaReceita", clienteCadastradoNaReceita);
			
			}else if (confirmado == null && 
					consultaCdl != null && 
					indicadorConsultaDocumentoReceita.toString().equals(ConstantesSistema.SIM.toString()) ){
				
				clienteCadastradoNaReceita.setNomeCliente(consultaCdl.getNomeCliente());
				
				sessao.setAttribute("clienteCadastradoNaReceita", clienteCadastradoNaReceita);
			}

			short codigoAcao = ConstantesSistema.NUMERO_NAO_INFORMADO;
			boolean atualizaImovel = true;
			
			//Caso o spc esteja fora, n�o realizar acao de atualizacao do cliente e dos dados do spc
			if(clienteCadastradoNaReceita != null &&
				clienteCadastradoNaReceita.getMensagemRetorno() != null){
				
				atualizaImovel = false;
				retorno = this.montaTelaAtencao(actionMapping,
						httpServletRequest,
						"atencao.cliente_nao_foi_atualizado_spc_fora",
						false);
				
			}
			
			if ( confirmado == null && 
				clienteCadastradoNaReceita.getNomeCliente() != null &&
				!clienteCadastradoNaReceita.getNomeCliente().equals(nome) ) {
				
				httpServletRequest.setAttribute("nomeBotao1", "Aceitar");
				httpServletRequest.setAttribute("nomeBotao3", "Rejeitar");
		
				
				return montarPaginaConfirmacaoWizard("atencao.confirmacao_nome_receita_federal",
							httpServletRequest, 
							actionMapping, 
							nome, 
							clienteCadastradoNaReceita.getNomeCliente());
					
			}else if(confirmado == null && 
				clienteCadastradoNaReceita.getNomeCliente() != null &&
				clienteCadastradoNaReceita.getNomeCliente().equals(nome)){
				
				
				clienteCadastradoNaReceita = 
					(ConsultaCdl) sessao.getAttribute("clienteCadastradoNaReceita");

				codigoAcao = 3;
				
				clienteCadastradoNaReceita.setCodigoAcaoOperador(codigoAcao);
				
			
			}else if ( confirmado != null && confirmado.trim().equalsIgnoreCase("ok") ) {
				
				clienteCadastradoNaReceita = 
					(ConsultaCdl) sessao.getAttribute("clienteCadastradoNaReceita");
				
				cliente.setNome(clienteCadastradoNaReceita.getNomeCliente());
				
				if (clienteCadastradoNaReceita.getNomeMae() != null){
					cliente.setNomeMae(clienteCadastradoNaReceita.getNomeMae());
				}
				
				if (clienteCadastradoNaReceita.getDataNascimento() != null){
					cliente.setDataNascimento(clienteCadastradoNaReceita.getDataNascimento());
				}
				
				codigoAcao = 1;
				clienteCadastradoNaReceita.setCodigoAcaoOperador(codigoAcao);
				
			} else if((clienteCadastradoNaReceita.getMensagemRetorno() == null || 
				clienteCadastradoNaReceita.getMensagemRetorno().equals("")) && 
				(confirmado != null)){
				
				clienteCadastradoNaReceita = 
					(ConsultaCdl) sessao.getAttribute("clienteCadastradoNaReceita");
				
				codigoAcao = 2;
				clienteCadastradoNaReceita.setCodigoAcaoOperador(codigoAcao);
				
				atualizaImovel = false;
				
				retorno = this.montaTelaAtencao(actionMapping,
					httpServletRequest,
					"atencao.cliente_nao_foi_atualizado",
					true);
			} 

			/**
			 * fim
			 */
			
			if(atualizaImovel){
				this.getFachada().atualizarCliente(cliente, colecaoFones, colecaoEnderecos, usuario);
			}
			
			if ((confirmado != null) ||
					(clienteCadastradoNaReceita.getCodigoAcaoOperador() != null &&
							clienteCadastradoNaReceita.getCodigoAcaoOperador() == 3)){
				
				if(consultaCdl == null){
					ConsultaCdl clienteCadastradoNaReceitaAtualiza = 
						(ConsultaCdl) sessao.getAttribute("clienteCadastradoNaReceita");
					
					clienteCadastradoNaReceitaAtualiza.setCodigoAcaoOperador(codigoAcao);
					clienteCadastradoNaReceitaAtualiza.setCodigoCliente(cliente);
					clienteCadastradoNaReceitaAtualiza.setUsuario(usuario);
					clienteCadastradoNaReceitaAtualiza.setCpfUsuario(usuario.getCpf());
					clienteCadastradoNaReceitaAtualiza.setLoginUsuario(usuario.getLogin());
					clienteCadastradoNaReceitaAtualiza.setUltimaAlteracao(new Date());

					this.getFachada().inserir(clienteCadastradoNaReceitaAtualiza);
				}
			}

			// limpa a sess�o
			sessao.removeAttribute("clienteCadastradoNaReceita");
			sessao.removeAttribute("colecaoClienteFone");
			sessao.removeAttribute("colecaoEnderecos");
			sessao.removeAttribute("foneTipos");
			sessao.removeAttribute("municipios");
			sessao.removeAttribute("colecaoResponsavelSuperiores");
			sessao.removeAttribute("InserirEnderecoActionForm");
			sessao.removeAttribute("ClienteActionForm");
			sessao.removeAttribute("tipoPesquisaRetorno");
			sessao.removeAttribute("clienteAtualizacao");

		} catch (ParseException ex) {
			// Erro no hibernate
			reportarErros(httpServletRequest, "erro.sistema", ex);
			// Atribui o mapeamento de retorno para a tela de erro
			retorno = actionMapping.findForward("telaErro");
		}

		// Verifica se a funcionalidade esta sendo executada dentro de um popup
		boolean exibirTelaSucesso = true;
		if (sessao.getAttribute("POPUP") != null) {
			if (sessao.getAttribute("POPUP").equals("true")) {
				// Verifica o action de retorno
				// action = inserirClienteNomeTipo
				retorno = actionMapping.findForward("atualizarClientePopUp");
				sessao.setAttribute("codigoCliente", clienteAtualizacao.getId());
				sessao.setAttribute("nomeCliente", nome);
				if (cpf != null) {
					sessao.setAttribute("cpfCnpjCliente", Util.formatarCpf(cpf));
				}else if (cnpj != null) {
					sessao.setAttribute("cpfCnpjCliente", Util.formatarCnpj(cnpj));
				}
				
				httpServletRequest.setAttribute("colecaoTipoPessoa", null);
				exibirTelaSucesso = false;
			}
		}
		
		if (exibirTelaSucesso) {
			
			// Monta a p�gina de sucesso
			if (retorno.getName().equalsIgnoreCase("telaSucesso")) {
				
				String linkSucesso = (String)sessao.getAttribute("linkSucesso");
				String mensagemSucesso = "Cliente de c�digo " + clienteAtualizacao.getId() + " atualizado com sucesso.";	
				
//				if(mensagemRetornoReceita != null && !mensagemRetornoReceita.equals("")){
//					mensagemSucesso = mensagemSucesso +"\n"+ mensagemRetornoReceita;
//				}
				
				if(linkSucesso != null && !linkSucesso.equals("")){
					
					montarPaginaSucesso(httpServletRequest, 
						mensagemSucesso,
						"Realizar outra Manuten��o de Cliente", "exibirManterClienteAction.do?menu=sim",
						linkSucesso,
						"Retornar ao Consultar Im�vel.");
					
					sessao.removeAttribute("linkSucesso");
					
				}else if(sessao.getAttribute("caminhoVoltarPromais")!=null){
					
					montarPaginaSucesso(httpServletRequest, 
						mensagemSucesso,
						"Realizar outra Manuten��o de Cliente", "exibirManterClienteAction.do?menu=sim",
						(String)sessao.getAttribute("caminhoVoltarPromais")+".do?promais=nao","Retornar ao Consultar Im�vel.");
					
					sessao.setAttribute("promaisExecutado", "sim");
					sessao.setAttribute("idImovelPromaisExecutado", Integer.parseInt((String)sessao.getAttribute("idImovel")));
					sessao.removeAttribute("idImovel");
					
					sessao.removeAttribute("caminhoVoltarPromais");
					
				}else{
					montarPaginaSucesso(httpServletRequest, 
						mensagemSucesso,
						"Realizar outra Manuten��o de Cliente", 
						"exibirManterClienteAction.do?menu=sim");
				}
			}
		}

		return retorno;
	}
	
	/***
	 * @author Ivan Sergio
	 * @date: 11/08/2009
	 * 
	 * @param colecaoEnderecos
	 * @param id2
	 * @return
	 */
	private Collection setaId2ClienteEnderecos(Collection colecaoEnderecos, Integer id2) {
		Collection retorno = null;
		
		if (colecaoEnderecos != null && !colecaoEnderecos.isEmpty()) {
			retorno = new ArrayList();
			Iterator iColecaoEnderecos = colecaoEnderecos.iterator();
			
			while (iColecaoEnderecos.hasNext()) {
				ClienteEndereco endereco = (ClienteEndereco) iColecaoEnderecos.next();
				endereco.setId2(id2);
				retorno.add(endereco);
			}
		}else {
			retorno = colecaoEnderecos;
		}
		
		return retorno;
	}
	
	/**
	 * @author Ivan Sergio
	 * @date: 11/08/2009
	 * 
	 * @param colecaoFones
	 * @param id2
	 * @return
	 */
	private Collection setaId2ClienteFones(Collection colecaoFones, Integer id2) {
		Collection retorno = null;
		
		if (colecaoFones != null && !colecaoFones.isEmpty()) {
			retorno = new ArrayList();
			Iterator iColecaoFones = colecaoFones.iterator();
			
			while (iColecaoFones.hasNext()) {
				ClienteFone fone = (ClienteFone) iColecaoFones.next();
				fone.setId2(id2);
				retorno.add(fone);
			}
		}else {
			retorno = colecaoFones;
		}
		
		return retorno;
	}
	
	/**
	 * @author Rafael Pinto
	 * @date: 09/01/2011
	 * 
	 * @param actionMapping ActionMapping
	 * @param httpServletRequest httpServletRequest
	 * @return ActionForward
	 */
	private ActionForward montaTelaAtencao(ActionMapping actionMapping, 
		HttpServletRequest httpServletRequest,String chave,
		boolean naoExibirBotaoVoltarTelaAtencao){
				
		httpServletRequest.setAttribute("naoExibirBotaoVoltarTelaAtencao",naoExibirBotaoVoltarTelaAtencao);
		reportarErros(httpServletRequest, chave);
		
		return actionMapping.findForward("telaAtencao");
	} 
	

}
