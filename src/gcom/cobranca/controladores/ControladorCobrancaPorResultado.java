package gcom.cobranca.controladores;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.EJBException;

import org.apache.log4j.Logger;

import gcom.arrecadacao.pagamento.GuiaPagamento;
import gcom.arrecadacao.pagamento.Pagamento;
import gcom.atendimentopublico.registroatendimento.AtendimentoMotivoEncerramento;
import gcom.batch.Processo;
import gcom.batch.UnidadeProcessamento;
import gcom.cadastro.EnvioEmail;
import gcom.cadastro.cliente.ClienteImovel;
import gcom.cadastro.cliente.ClienteRelacaoTipo;
import gcom.cadastro.cliente.FiltroClienteImovel;
import gcom.cadastro.empresa.Empresa;
import gcom.cadastro.empresa.EmpresaCobranca;
import gcom.cadastro.empresa.FiltroEmpresaCobranca;
import gcom.cadastro.endereco.FiltroLogradouroTipo;
import gcom.cadastro.endereco.LogradouroTipo;
import gcom.cadastro.imovel.Categoria;
import gcom.cadastro.imovel.FiltroImovel;
import gcom.cadastro.imovel.FiltroImovelCobrancaSituacao;
import gcom.cadastro.imovel.FiltroImovelSubCategoria;
import gcom.cadastro.imovel.Imovel;
import gcom.cadastro.imovel.ImovelCobrancaSituacao;
import gcom.cadastro.imovel.ImovelSubcategoria;
import gcom.cadastro.imovel.Subcategoria;
import gcom.cadastro.sistemaparametro.SistemaParametro;
import gcom.cobranca.CmdEmpresaCobrancaContaLigacaoAguaSituacao;
import gcom.cobranca.CmdEmpresaCobrancaContaLigacaoAguaSituacaoPK;
import gcom.cobranca.CobrancaDocumento;
import gcom.cobranca.CobrancaSituacao;
import gcom.cobranca.CobrancaSituacaoHistorico;
import gcom.cobranca.CobrancaSituacaoTipo;
import gcom.cobranca.ComandoEmpresaCobrancaConta;
import gcom.cobranca.ComandoEmpresaCobrancaContaGerencia;
import gcom.cobranca.ComandoEmpresaCobrancaContaGerenciaPK;
import gcom.cobranca.ComandoEmpresaCobrancaContaHelper;
import gcom.cobranca.ComandoEmpresaCobrancaContaImovelPerfil;
import gcom.cobranca.ComandoEmpresaCobrancaContaImovelPerfilPK;
import gcom.cobranca.ComandoEmpresaCobrancaContaUnidadeNegocio;
import gcom.cobranca.ComandoEmpresaCobrancaContaUnidadeNegocioPK;
import gcom.cobranca.EmpresaCobrancaConta;
import gcom.cobranca.EmpresaCobrancaContaPagamentos;
import gcom.cobranca.ExtensaoComandoContasCobrancaEmpresaHelper;
import gcom.cobranca.FiltroCobrancaAcaoAtividadeComando;
import gcom.cobranca.FiltroCobrancaSituacaoHistorico;
import gcom.cobranca.FiltroComandoEmpresaCobrancaConta;
import gcom.cobranca.FiltroEmpresaCobrancaConta;
import gcom.cobranca.GerarArquivoTextoContasCobrancaEmpresaHelper;
import gcom.cobranca.GerarExtensaoComandoContasCobrancaEmpresaHelper;
import gcom.cobranca.IRepositorioCobranca;
import gcom.cobranca.NegativacaoImoveis;
import gcom.cobranca.RelatorioPagamentosContasCobrancaEmpresaHelper;
import gcom.cobranca.RepositorioCobrancaHBM;
import gcom.cobranca.UC0870GerarMovimentoContasEmCobrancaPorEmpresa;
import gcom.cobranca.cobrancaporresultado.ArquivoTextoNegociacaoCobrancaEmpresaBuilder;
import gcom.cobranca.cobrancaporresultado.ArquivoTextoNegociacaoCobrancaEmpresaHelper;
import gcom.cobranca.cobrancaporresultado.ArquivoTextoPagamentoContasCobrancaEmpresaHelper;
import gcom.cobranca.cobrancaporresultado.ArquivoTextoParagentosCobancaEmpresaBuilder;
import gcom.cobranca.cobrancaporresultado.ConsultarComandosContasCobrancaEmpresaHelper;
import gcom.cobranca.cobrancaporresultado.NegociacaoCobrancaEmpresa;
import gcom.cobranca.cobrancaporresultado.NegociacaoContaCobrancaEmpresa;
import gcom.cobranca.parcelamento.Parcelamento;
import gcom.cobranca.parcelamento.ParcelamentoItem;
import gcom.cobranca.repositorios.IRepositorioCobrancaPorResultadoHBM;
import gcom.cobranca.repositorios.RepositorioCobrancaPorResultadoHBM;
import gcom.faturamento.GuiaPagamentoGeral;
import gcom.faturamento.conta.Conta;
import gcom.faturamento.conta.ContaGeral;
import gcom.faturamento.debito.DebitoACobrar;
import gcom.faturamento.debito.DebitoACobrarGeral;
import gcom.faturamento.debito.DebitoCobrado;
import gcom.faturamento.debito.DebitoTipo;
import gcom.financeiro.FinanciamentoTipo;
import gcom.interceptor.RegistradorOperacao;
import gcom.micromedicao.IRepositorioMicromedicao;
import gcom.micromedicao.RepositorioMicromedicaoHBM;
import gcom.relatorio.cobranca.RelatorioPagamentosContasCobrancaEmpresaBean;
import gcom.seguranca.acesso.Operacao;
import gcom.seguranca.acesso.OperacaoEfetuada;
import gcom.seguranca.acesso.usuario.Usuario;
import gcom.seguranca.acesso.usuario.UsuarioAcao;
import gcom.seguranca.acesso.usuario.UsuarioAcaoUsuarioHelper;
import gcom.spcserasa.FiltroNegativacaoImoveis;
import gcom.util.ConstantesSistema;
import gcom.util.ControladorComum;
import gcom.util.ControladorException;
import gcom.util.ErroRepositorioException;
import gcom.util.Util;
import gcom.util.email.ServicosEmail;
import gcom.util.filtro.Filtro;
import gcom.util.filtro.MaiorQue;
import gcom.util.filtro.ParametroNaoNulo;
import gcom.util.filtro.ParametroNulo;
import gcom.util.filtro.ParametroSimples;
import gcom.util.filtro.ParametroSimplesIn;


public class ControladorCobrancaPorResultado extends ControladorComum {
	private static final long serialVersionUID = 4498794060506412760L;
	
	private static Logger logger = Logger.getLogger(ControladorCobrancaPorResultado.class);

	protected IRepositorioCobrancaPorResultadoHBM repositorio;
	protected IRepositorioCobranca repositorioCobranca;
	protected IRepositorioMicromedicao repositorioMicromedicao;

	public void ejbCreate() throws CreateException {
		repositorio = RepositorioCobrancaPorResultadoHBM.getInstancia();
		repositorioCobranca = RepositorioCobrancaHBM.getInstancia();
		repositorioMicromedicao = RepositorioMicromedicaoHBM.getInstancia();
	}

	public List<Object[]> pesquisarQuantidadeContas(ComandoEmpresaCobrancaContaHelper helper, boolean percentualInformado) throws ControladorException {
		try {
			List<Object[]> retorno = new ArrayList<Object[]>();
			Integer anoMesFaturamento = getControladorUtil().pesquisarParametrosDoSistema().getAnoMesFaturamento();
			ComandoEmpresaCobrancaConta comando = helper.getComando();
			
			List<Integer> imoveis = pesquisarImoveis(helper, percentualInformado, anoMesFaturamento);
			
			for (Integer idImovel : imoveis) {
				List<Object[]> contas = repositorio.pesquisarContas(idImovel, helper, percentualInformado, anoMesFaturamento);
				
				int qtdContas = 0;
				BigDecimal valorTotal = new BigDecimal(0.0);
				boolean adicionar = true;
				
				if (isImovelValido(comando, idImovel, contas.size(), percentualInformado)) {
					for (Object[] conta : contas) {
						if (isContaValida(comando, conta)) {
							qtdContas++;
							valorTotal = valorTotal.add((BigDecimal) conta[1]);
						} else {
							adicionar = false;
							break;
						}
					}

					if (adicionar) {
						Object[] dados = new Object[3];
						dados[0] = idImovel;
						dados[1] = valorTotal;
						dados[2] = qtdContas;

						retorno.add(dados);
					}
					
					if (comando.isQtdMaximaInformada() && retorno.size() == comando.getQtdMaximaClientes()) {
						break;
					}
				}
				
			}
			
			return retorno;
		} catch (ErroRepositorioException e) {
			throw new ControladorException("erro.sistema", e);
		}
	}

	public List<Object[]> pesquisarContas(Integer idImovel, ComandoEmpresaCobrancaContaHelper helper, boolean percentualInformado) throws ControladorException {
		try {
			List<Object[]> retorno = new ArrayList<Object[]>();
			Integer anoMesFaturamento = getControladorUtil().pesquisarParametrosDoSistema().getAnoMesFaturamento();
			ComandoEmpresaCobrancaConta comando = helper.getComando();
			
			List<Object[]> contas = repositorio.pesquisarContas(idImovel, helper, percentualInformado, anoMesFaturamento);

			if (isImovelValido(comando, idImovel, contas.size(), percentualInformado)) {
				boolean adicionar = true;
				for (Object[] conta : contas) {
					if (!isContaValida(comando, conta)) {
						adicionar = false;
						break;
					}
				}

				if (adicionar) {
					retorno.addAll(contas);
				}
			}
			
			return retorno;
		} catch (ErroRepositorioException e) {
			throw new ControladorException("erro.sistema", e);
		}
	}
	
	private boolean isContaValida(ComandoEmpresaCobrancaConta comando, Object[] conta) {
		return comando.isValorValido(((BigDecimal) conta[1]).doubleValue()) 
				&& comando.isReferenciaValida((Integer) conta[2]) 
				&& comando.isVencimentoValido((Date) conta[3]) 
				&& comando.isDiasVencimentoValido((Date) conta[3]);
	}
	
	private boolean isImovelValido(ComandoEmpresaCobrancaConta comando, Integer idImovel, int qtdContas, boolean percentualInformado) throws ErroRepositorioException {
		return !repositorio.isImovelEmCobranca(idImovel) 
				&& comando.isQuantidadeContasValida(qtdContas, pesquisarQtdMenorFaixa(percentualInformado, comando.getEmpresa()))
				&& comando.isIndicadorCpfCnpjValido(repositorio.isClienteComCpfOuCnpj(idImovel));
	}

	private Integer pesquisarQtdMenorFaixa(boolean percentualInformado, Empresa empresa) throws ErroRepositorioException {
		int qtdMenorFaixa = 0;
		if (!percentualInformado && empresa != null) {
			qtdMenorFaixa = repositorio.pesquisarMenorFaixa(empresa.getId());
		}
		return qtdMenorFaixa;
	}
	
	public void gerarMovimentoContas(ComandoEmpresaCobrancaConta comando, int idFuncionalidadeIniciada) throws ControladorException {
		int idUnidadeIniciada = getControladorBatch().iniciarUnidadeProcessamentoBatch(idFuncionalidadeIniciada, UnidadeProcessamento.COMANDO_EMPRESA_COBRANCA_CONTA, comando.getId());

		try {
			logger.info("Cobran�a por Resultado - Iniciou Comando: " + comando.getId());

			UC0870GerarMovimentoContasEmCobrancaPorEmpresa movimento = UC0870GerarMovimentoContasEmCobrancaPorEmpresa.getInstancia(repositorioCobranca);
			movimento.gerarMovimentoContasEmCobranca(comando);

			comando.setDataExecucao(new Date());
			comando.setUltimaAlteracao(new Date());
			this.montarDatasCiclo(comando);
			
			getControladorUtil().atualizar(comando);

			getControladorBatch().encerrarUnidadeProcessamentoBatch(null, idUnidadeIniciada, false);

			logger.info("Cobran�a por Resultado - Finalizou Comando: " + comando.getId());
		} catch (Exception e) {
			getControladorBatch().encerrarUnidadeProcessamentoBatch(e, idUnidadeIniciada, true);
			throw new EJBException(e);
		}
	}
	
	private void montarDatasCiclo(ComandoEmpresaCobrancaConta comando) {
		
		if (comando.getDataInicioCiclo() == null) {
			comando.setDataInicioCiclo(new Date());
			comando.setDataFimCiclo(Util.adicionarNumeroDiasDeUmaData(comando.getDataInicioCiclo(), 90));
		}
		
	}
	
	
	public List<Integer> pesquisarImoveis(ComandoEmpresaCobrancaContaHelper helper, boolean percentualInformado, Integer anoMesFaturamento) throws ControladorException {
		try {
			return repositorio.pesquisarImoveis(helper, percentualInformado, anoMesFaturamento);
		} catch (ErroRepositorioException e) {
			throw new ControladorException("erro.sistema", e);
		}
	}
	
	public void gerarArquivoContas(Collection<Integer> comandos, Integer idEmpresa, int idFuncionalidadeIniciada) throws ControladorException {
		int idUnidadeIniciada = getControladorBatch().iniciarUnidadeProcessamentoBatch(idFuncionalidadeIniciada, UnidadeProcessamento.FUNCIONALIDADE, 0);

		try {
			SistemaParametro sistemaParametro = getControladorUtil().pesquisarParametrosDoSistema();
			Integer idPerfilProgramaEspecial = sistemaParametro.getPerfilProgramaEspecial() != null ? sistemaParametro.getPerfilProgramaEspecial().getId() : null;

			Collection<Object[]> colecao = repositorioCobranca.pesquisarDadosArquivoTextoContasCobrancaEmpresa(comandos, idPerfilProgramaEspecial);

			StringBuilder registros = new StringBuilder();
			for (Iterator<Object[]> iterator = colecao.iterator(); iterator.hasNext();) {
				Object[] dados = (Object[]) iterator.next();

				if (dados[30] == null || ((Short) dados[30]).equals(ConstantesSistema.SIM)) {
					registros = gerarArquivoLayoutPadrao(comandos, dados, registros);
				} else {
					registros = gerarArquivoLayoutTipo02(comandos, dados, registros);
				}
			}
			
			enviarEmailArquivoContas(idEmpresa, comandos, registros);
			repositorioCobranca.atualizarIndicadorGeracaoTxt(comandos);

			getControladorBatch().encerrarUnidadeProcessamentoBatch(null, idUnidadeIniciada, false);
		} catch (Exception e) {
			e.printStackTrace();
			getControladorBatch().encerrarUnidadeProcessamentoBatch(e, idUnidadeIniciada, true);
		}
	}
	
	private void enviarEmailArquivoContas(Integer idEmpresa, Collection<Integer> comandos, StringBuilder registros) throws ControladorException {
		String dataHora = Util.formatarDataAAAAMMDD(new Date()) + "-" + Util.formatarDataHHMM(new Date());
		String nome = "contas_cobranca_empresa-" + idEmpresa + "_" + dataHora;
		
		EnvioEmail envioEmail = getControladorCadastro().pesquisarEnvioEmail(EnvioEmail.COBRANCA_EMPRESA);
		
		String corpo = envioEmail.getCorpoMensagem() + " - PROCESSO: " + Processo.GERAR_ARQUIVO_TEXTO_CONTAS_COBRANCA_EMPRESA 
				+ "\nEmpresa: " + idEmpresa 
				+ "\nComando(s): " + comandos.toString().replace("[", "").replace("]", ".");
		
		ServicosEmail.enviarArquivoCompactado(nome, registros, envioEmail.getEmailReceptor(), envioEmail.getEmailRemetente(), envioEmail.getTituloMensagem(), corpo);
	}
	
	private StringBuilder gerarArquivoLayoutPadrao(Collection<Integer> comandos, Object[] dados, StringBuilder registros) {
		GerarArquivoTextoContasCobrancaEmpresaHelper helper = buildDadosArquivoContasLayoutPadrao(dados, comandos);
		
		return helper.buildArquivoContasLayoutPadrao(registros);
	}
	
	private GerarArquivoTextoContasCobrancaEmpresaHelper buildDadosArquivoContasLayoutPadrao(Object[] dados, Collection<Integer> comandos) {
		GerarArquivoTextoContasCobrancaEmpresaHelper helper = new GerarArquivoTextoContasCobrancaEmpresaHelper();
		
		helper.setIdCobrancaConta((Integer) dados[0]);
		helper.setIdUnidadeNegocio((Integer) dados[1]);
		helper.setNomeUnidadeNegocio((String) dados[2]);
		helper.setIdFaturamentoGrupo((Integer) dados[3]);
		helper.setIdLocalidade((Integer) dados[4]);
		helper.setNomeLocalidade((String) dados[5]);
		helper.setCodigoRota((Short) dados[6]);
		helper.setNumeroSequencialRota((Integer) dados[7]);
		helper.setIdImovel((Integer) dados[8]);
		helper.setNomeClienteConta((String) dados[9]);
		helper.setNomeAbreviadoCliente((String) dados[17]);
		helper.setIdClienteTipo((Integer) dados[10]);
		helper.setCpf((String) dados[11]);
		helper.setCnpj((String) dados[12]);
		helper.setRg((String) dados[13]);
		helper.setNumeroQuadra((Integer) dados[14]);
		helper.setCodigoSetorComercial((Integer) dados[24]);
		helper.setNumeroLote((Short) dados[25]);
		helper.setNumeroSublote((Short) dados[26]);
		helper.setIdCliente((Integer) dados[27]);
		helper.setIdGerenciaRegional((Integer) dados[28]);
		helper.setNomeGerenciaRegional((String) dados[29]);
		helper.setCodigoLayoutTxt((Short) dados[30]);
		helper.setDataInicioCiclo((Date) dados[33]);
		helper.setDataFimCiclo((Date) dados[34]);
		
		if (dados[15] != null) {
			Conta conta = new Conta();
			conta.setId((Integer) dados[15]);
			conta.setReferencia((Integer) dados[18]);
			conta.setDataVencimentoConta((Date) dados[19]);
			conta.setValorAgua((BigDecimal) dados[20]);
			conta.setValorEsgoto((BigDecimal) dados[21]);
			conta.setDebitos((BigDecimal) dados[22]);
			conta.setValorCreditos((BigDecimal) dados[23]);
			helper.setConta(conta);
		}

		if (dados[32] != null && dados[16] != null) {
			String ddd = (String) dados[32];
			String telefone = (String) dados[16];
			helper.setTelefone(ddd + telefone);
		}

		try {
			helper = buildDadosEnderecoArquivoContasLayoutPadrao(helper);
			helper = buildDadosQuantidadeArquivoContas(helper, comandos);
			helper = buildDadosImovelArquivoContas(helper);
			helper = buildDadosSubcategoriasArquivoContas(helper);
			helper = buildDadosClientesArquivoContas(helper);
			helper = buildDadosNegativacaoArquivoContas(helper);
		} catch (ControladorException e) {
			e.printStackTrace();
		} catch (ErroRepositorioException e) {
			e.printStackTrace();
		}
		
		return helper;
	}
	
	@SuppressWarnings("unchecked")
	private GerarArquivoTextoContasCobrancaEmpresaHelper buildDadosNegativacaoArquivoContas(GerarArquivoTextoContasCobrancaEmpresaHelper helper) throws ControladorException {
		FiltroNegativacaoImoveis filtro = new FiltroNegativacaoImoveis();
		filtro.adicionarParametro(new ParametroSimples(FiltroNegativacaoImoveis.IMOVEL_ID, helper.getIdImovel()));
		filtro.adicionarParametro(new ParametroSimples(FiltroNegativacaoImoveis.INDICADOR_EXCLUIDO, ConstantesSistema.NAO));
		filtro.adicionarParametro(new ParametroNulo(FiltroNegativacaoImoveis.DATA_EXCLUSAO));
		filtro.adicionarParametro(new ParametroNaoNulo(FiltroNegativacaoImoveis.DATA_CONFIRMACAO));
		
		Collection<NegativacaoImoveis> colecao = getControladorUtil().pesquisar(filtro, NegativacaoImoveis.class.getName());
		
		if (colecao != null && !colecao.isEmpty()) {
			NegativacaoImoveis negativacao = (NegativacaoImoveis) Util.retonarObjetoDeColecao(colecao);
			
			helper.setIndicadorImovelNegativado(ConstantesSistema.SIM);
			helper.setDataNegativacao(negativacao.getDataConfirmacao());
		} else {
			helper.setIndicadorImovelNegativado(ConstantesSistema.NAO);
		}
		
		return helper;
	}

	@SuppressWarnings("unchecked")
	private GerarArquivoTextoContasCobrancaEmpresaHelper buildDadosClientesArquivoContas(GerarArquivoTextoContasCobrancaEmpresaHelper helper) throws ControladorException {
		FiltroClienteImovel filtro = new FiltroClienteImovel();
		filtro.adicionarParametro(new ParametroSimples(FiltroClienteImovel.IMOVEL_ID, helper.getIdImovel()));
		filtro.adicionarParametro(new ParametroNulo(FiltroClienteImovel.DATA_FIM_RELACAO));
		filtro.adicionarCaminhoParaCarregamentoEntidade(FiltroClienteImovel.CLIENTE);
		filtro.adicionarCaminhoParaCarregamentoEntidade(FiltroClienteImovel.CLIENTE_FONE);
		
		Collection<ClienteImovel> colecao = getControladorUtil().pesquisar(filtro, ClienteImovel.class.getName());
		
		for (Iterator<ClienteImovel> iterator = colecao.iterator(); iterator.hasNext();) {
			ClienteImovel clienteImovel = (ClienteImovel) iterator.next();
			
			if (clienteImovel.getClienteRelacaoTipo().getId().shortValue() == ClienteRelacaoTipo.USUARIO.shortValue()) {
				helper.montarDadosClienteUsuario(clienteImovel);
			} else if (clienteImovel.getClienteRelacaoTipo().getId().shortValue() == ClienteRelacaoTipo.RESPONSAVEL.shortValue()) {
				helper.montarDadosClienteResponsavel(clienteImovel);
			} else {
				helper.montarDadosClienteProprietario(clienteImovel);
			}
		}
		
		return helper;
	}

	private GerarArquivoTextoContasCobrancaEmpresaHelper buildDadosQuantidadeArquivoContas(
			GerarArquivoTextoContasCobrancaEmpresaHelper helper, Collection<Integer> comandos) throws ErroRepositorioException {
		
		Integer quantidadeContas = repositorioCobranca.pesquisarQuantidadeContasArquivoTextoContasCobrancaEmpresa(comandos, helper.getIdImovel());
		if (quantidadeContas != null) {
			helper.setQuantidadeContas(quantidadeContas);
		}
		
		return helper;
	}

	private GerarArquivoTextoContasCobrancaEmpresaHelper buildDadosEnderecoArquivoContasLayoutPadrao(GerarArquivoTextoContasCobrancaEmpresaHelper helper) throws ControladorException {
		Collection<Object[]> enderecos = getControladorEndereco().pesquisarDadosClienteEnderecoArquivoTextoContasCobrancaEmpresa(helper.getIdCliente());

		return buildDadosEnderecoArquivoContas(enderecos, helper);
	}
	
	@SuppressWarnings("unchecked")
	private GerarArquivoTextoContasCobrancaEmpresaHelper buildDadosEnderecoArquivoContas(Collection<Object[]> enderecos, 
			GerarArquivoTextoContasCobrancaEmpresaHelper helper) throws ControladorException {
		
		for (Iterator<Object[]> iterator = enderecos.iterator(); iterator.hasNext();) {
			Object[] endereco = (Object[]) iterator.next();

			helper.setNomeLogradouro((String) endereco[0]);
			helper.setComplementoEndereco((String) endereco[1]);
			helper.setCodigoCep((Integer) endereco[2]);
			helper.setNomeBairro((String) endereco[3]);
			helper.setNumeroImovel((String) endereco[4]);

			if (endereco[5] != null) {
				FiltroLogradouroTipo filtro = new FiltroLogradouroTipo();
				filtro.adicionarParametro(new ParametroSimples(FiltroLogradouroTipo.ID, (Integer) endereco[5]));
				Collection<LogradouroTipo> logradouroTipos = getControladorUtil().pesquisar(filtro, LogradouroTipo.class.getName());
				
				LogradouroTipo logradouroTipo = (LogradouroTipo) Util.retonarObjetoDeColecao(logradouroTipos);
				helper.setTipoLogradouro(logradouroTipo.getDescricao());
			}
		}
		
		return helper;
	}

	@SuppressWarnings("unchecked")
	private GerarArquivoTextoContasCobrancaEmpresaHelper buildDadosImovelArquivoContas(GerarArquivoTextoContasCobrancaEmpresaHelper helper) throws ControladorException, ErroRepositorioException {
		FiltroImovel filtroImovel = new FiltroImovel();
		filtroImovel.adicionarParametro(new ParametroSimples(FiltroImovel.ID, helper.getIdImovel()));
		filtroImovel.adicionarCaminhoParaCarregamentoEntidade(FiltroImovel.LIGACAO_AGUA_SITUACAO);
		filtroImovel.adicionarCaminhoParaCarregamentoEntidade(FiltroImovel.LIGACAO_ESGOTO_SITUACAO);
		filtroImovel.adicionarCaminhoParaCarregamentoEntidade(FiltroImovel.FONTE_ABASTECIMENTO);
		
		Imovel imovel = (Imovel) Util.retonarObjetoDeColecao(getControladorUtil().pesquisar(filtroImovel, Imovel.class.getName()));

		helper.setLigacaoAguaSituacao(imovel.getLigacaoAguaSituacao().getDescricao());
		helper.setLigacaoEsgotoSituacao(imovel.getLigacaoEsgotoSituacao().getDescricao());
		helper.setFonteAbastecimento(imovel.getFonteAbastecimento() != null ? imovel.getFonteAbastecimento().getDescricao() : null);
		helper.setImovelHidrometrado(repositorioMicromedicao.verificaExistenciaHidrometro(helper.getIdImovel()));
		helper.setQuantidadeParcelamentos(imovel.getNumeroParcelamento());
		helper.setQuantidadeReparcelamentos(imovel.getNumeroReparcelamento());
		helper.setQuantidadeReparcelamentosConsecutivos(imovel.getNumeroReparcelamentoConsecutivos());
		
		return helper;
	}

	@SuppressWarnings("unchecked")
	private GerarArquivoTextoContasCobrancaEmpresaHelper buildDadosSubcategoriasArquivoContas(GerarArquivoTextoContasCobrancaEmpresaHelper helper) throws ControladorException {
		FiltroImovelSubCategoria filtro = new FiltroImovelSubCategoria(FiltroImovelSubCategoria.SUBCATEGORIA);
		filtro.adicionarParametro(new ParametroSimples(FiltroImovelSubCategoria.IMOVEL_ID, helper.getIdImovel()));
		filtro.adicionarCaminhoParaCarregamentoEntidade(FiltroImovelSubCategoria.SUBCATEGORIA);
		
		Collection<ImovelSubcategoria> colecao = getControladorUtil().pesquisar(filtro, ImovelSubcategoria.class.getName());
		
		for (Iterator<ImovelSubcategoria> iterator = colecao.iterator(); iterator.hasNext();) {
			ImovelSubcategoria imovelSubcategoria = (ImovelSubcategoria) iterator.next();
			
			switch (imovelSubcategoria.getSubcategoria().getId().intValue()) {
			case Subcategoria.RESIDENCIAL_R1:
				helper.setQtdEconomiasR1(imovelSubcategoria.getQuantidadeEconomias());
				break;
			case Subcategoria.RESIDENCIAL_R2:
				helper.setQtdEconomiasR2(imovelSubcategoria.getQuantidadeEconomias());
				break;
			case Subcategoria.RESIDENCIAL_R3:
				helper.setQtdEconomiasR3(imovelSubcategoria.getQuantidadeEconomias());
				break;
			case Subcategoria.RESIDENCIAL_R4:
				helper.setQtdEconomiasR4(imovelSubcategoria.getQuantidadeEconomias());
				break;
			case Subcategoria.COMERCIAL_C1:
				helper.setQtdEconomiasC1(imovelSubcategoria.getQuantidadeEconomias());
				break;
			case Subcategoria.COMERCIAL_C2:
				helper.setQtdEconomiasC2(imovelSubcategoria.getQuantidadeEconomias());
				break;
			case Subcategoria.COMERCIAL_C3:
				helper.setQtdEconomiasC3(imovelSubcategoria.getQuantidadeEconomias());
				break;
			case Subcategoria.COMERCIAL_C4:
				helper.setQtdEconomiasC4(imovelSubcategoria.getQuantidadeEconomias());
				break;
			case Subcategoria.INDUSTRIAL_I1:
				helper.setQtdEconomiasI1(imovelSubcategoria.getQuantidadeEconomias());
				break;
			case Subcategoria.INDUSTRIAL_I2:
				helper.setQtdEconomiasI2(imovelSubcategoria.getQuantidadeEconomias());
				break;
			case Subcategoria.INDUSTRIAL_I3:
				helper.setQtdEconomiasI3(imovelSubcategoria.getQuantidadeEconomias());
				break;
			case Subcategoria.INDUSTRIAL_I4:
				helper.setQtdEconomiasI4(imovelSubcategoria.getQuantidadeEconomias());
				break;
			case Subcategoria.PUBLICA_P1:
				helper.setQtdEconomiasP1(imovelSubcategoria.getQuantidadeEconomias());
				break;
			case Subcategoria.PUBLICA_P2:
				helper.setQtdEconomiasP2(imovelSubcategoria.getQuantidadeEconomias());
				break;
			case Subcategoria.PUBLICA_P3:
				helper.setQtdEconomiasP3(imovelSubcategoria.getQuantidadeEconomias());
				break;
			case Subcategoria.PUBLICA_P4:
				helper.setQtdEconomiasP4(imovelSubcategoria.getQuantidadeEconomias());
				break;
			default:
				break;
			}
		}
		
		return helper;
	}
	
	private StringBuilder gerarArquivoLayoutTipo02(Collection<Integer> comandos, Object[] dados, StringBuilder registros) {
		GerarArquivoTextoContasCobrancaEmpresaHelper helper = new GerarArquivoTextoContasCobrancaEmpresaHelper();
		Map<Integer, GerarArquivoTextoContasCobrancaEmpresaHelper> map = new HashMap<Integer, GerarArquivoTextoContasCobrancaEmpresaHelper>();
		
		buildDadosArquivoLayout02(dados, map, helper, comandos);
		
		if (map != null && !map.isEmpty() && !map.values().isEmpty()) {
			Collection<GerarArquivoTextoContasCobrancaEmpresaHelper> colecao = map.values();
			Iterator<GerarArquivoTextoContasCobrancaEmpresaHelper> iterator = colecao.iterator();

			while (iterator.hasNext()) {
				GerarArquivoTextoContasCobrancaEmpresaHelper novoHelper = (GerarArquivoTextoContasCobrancaEmpresaHelper) iterator.next();
				registros = novoHelper.buildArquivoContasLayoutTipo02(registros);
			}
		}
		
		return registros;
	}
	
	private void buildDadosArquivoLayout02(Object[] dados, Map<Integer, GerarArquivoTextoContasCobrancaEmpresaHelper> mapHelper, 
			GerarArquivoTextoContasCobrancaEmpresaHelper helper, Collection<Integer> comandos) {

		List<Conta> colecaoConta = new ArrayList<Conta>();

		if ((Integer) (dados[8]) != null && mapHelper.containsKey((Integer) (dados[8]))) {
			GerarArquivoTextoContasCobrancaEmpresaHelper helperImovel = mapHelper.get((Integer) (dados[8]));
			colecaoConta = helperImovel.getColecaoConta();

			helperImovel.setNomeClienteConta((String) (dados[9]));
			helperImovel.setIdClienteTipo((Integer) (dados[10]));
			helperImovel.setCpf((String) (dados[11]));
			helperImovel.setCnpj((String) (dados[12]));
			helperImovel.setRg((String) (dados[13]));

			Conta conta = new Conta();
			conta.setId((Integer) (dados[15]));
			conta.setReferencia(((Integer) (dados[18])).intValue());
			conta.setDataVencimentoConta((Date) (dados[19]));
			conta.setValorAgua((BigDecimal) (dados[20]));
			conta.setValorEsgoto((BigDecimal) (dados[21]));
			conta.setDebitos((BigDecimal) (dados[22]));
			conta.setValorCreditos((BigDecimal) (dados[23]));

			colecaoConta.add(conta);
			helperImovel.setColecaoConta(colecaoConta);

			mapHelper.remove((Integer) (dados[8]));
			mapHelper.put((Integer) (dados[8]), helperImovel);
		} else {
			helper.setIdCobrancaConta((Integer) (dados[0]));
//			helper.setIdUnidadeNegocio((Integer) (dados[1]));
//			helper.setNomeUnidadeNegocio((String) (dados[2]));
			helper.setIdFaturamentoGrupo((Integer) (dados[3]));
			helper.setIdLocalidade((Integer) (dados[4]));
			helper.setNomeLocalidade((String) (dados[5]));
			helper.setCodigoRota((Short) (dados[6]));
			helper.setNumeroSequencialRota((Integer) (dados[7]));
			helper.setIdImovel((Integer) (dados[8]));
			helper.setNomeClienteConta((String) (dados[9]));
			helper.setIdClienteTipo((Integer) (dados[10]));
			helper.setCpf((String) (dados[11]));
			helper.setCnpj((String) (dados[12]));
			helper.setRg((String) (dados[13]));
			helper.setTelefone((String) (dados[16]));
			helper.setNomeAbreviadoCliente((String) (dados[17]));
			helper.setCodigoSetorComercial(((Integer) (dados[24])));
			helper.setNumeroLote((Short) (dados[25]));
			helper.setNumeroSublote((Short) (dados[26]));
			helper.setNumeroQuadra((((Integer) (dados[14])).intValue()));
			helper.setIdCliente((Integer) (dados[27]));
			helper.setIdGerenciaRegional((Integer) (dados[28]));
			helper.setNomeGerenciaRegional((String) (dados[29]));
			helper.setCodigoLayoutTxt((Short) (dados[30]));
			helper.setIdOrdemServico((Integer) (dados[31]));

			Conta conta = new Conta();
			conta.setId((Integer) (dados[15]));
			conta.setReferencia(((Integer) (dados[18])).intValue());
			conta.setDataVencimentoConta((Date) (dados[19]));
			conta.setValorAgua((BigDecimal) (dados[20]));
			conta.setValorEsgoto((BigDecimal) (dados[21]));
			conta.setDebitos((BigDecimal) (dados[22]));
			conta.setValorCreditos((BigDecimal) (dados[23]));

			colecaoConta.add(conta);
			helper.setColecaoConta(colecaoConta);

			try {
				buildDadosEnderecoArquivoLayoutTipo02(helper);
				buildDadosQuantidadeArquivoContas(helper, comandos);
				
				mapHelper.put(helper.getIdImovel(), helper);
			} catch (ControladorException e) {
				e.printStackTrace();
			} catch (ErroRepositorioException e) {
				e.printStackTrace();
			}
		}
	}

	private void buildDadosEnderecoArquivoLayoutTipo02(GerarArquivoTextoContasCobrancaEmpresaHelper helper) throws ControladorException {
		Collection<Object[]> enderecos = getControladorEndereco().pesquisarDadosClienteEnderecoArquivoTextoContasCobrancaEmpresaLayout02(helper.getIdCliente());
		buildDadosEnderecoArquivoContas(enderecos, helper);
	}
	
	public void gerarNegociacoesCobrancaEmpresa(int idFuncionalidadeIniciada, Integer idEmpresa) throws ControladorException {
		int idUnidadeIniciada = 0;

		try {
			List<Integer> negociacoes = new ArrayList<Integer>();

			idUnidadeIniciada = getControladorBatch().iniciarUnidadeProcessamentoBatch(idFuncionalidadeIniciada, UnidadeProcessamento.EMPRESA, idEmpresa);

			gerarNegociacoesParcelamentos(idEmpresa, negociacoes);
			gerarNegociacoesExtrato(idEmpresa, negociacoes);
			gerarNegociacoesGuias(idEmpresa, negociacoes);

			gerarArquivoTextoNegociacoesCobrancaEmpresa(idEmpresa, negociacoes);

			getControladorBatch().encerrarUnidadeProcessamentoBatch(null, idUnidadeIniciada, false);

		} catch (Exception e) {
			e.printStackTrace();
			getControladorBatch().encerrarUnidadeProcessamentoBatch(e, idUnidadeIniciada, true);
		}
	}

	private void gerarNegociacoesParcelamentos(Integer idEmpresa, List<Integer> negociacoes) throws ErroRepositorioException, ControladorException {
		List<Parcelamento> parcelamentos = repositorioCobranca.obterParcelamentosCobrancaEmpresa(idEmpresa);

		for (Parcelamento parcelamento : parcelamentos) {
			NegociacaoCobrancaEmpresa negociacao = new NegociacaoCobrancaEmpresa(parcelamento, new Empresa(idEmpresa), new Date());

			Date dataVencimento = getControladorCobranca().obterDataVencimentoEntradaParcelamento(parcelamento.getId());
			negociacao.setDataVencimento(dataVencimento);
			
			Integer idNegociacao = (Integer) getControladorUtil().inserir(negociacao);
			negociacao.setId(idNegociacao);
			negociacoes.add(idNegociacao);

			List<ContaGeral> contas = repositorioCobranca.obterContasParcelamentosCobrancaEmpresa(parcelamento.getId(), idEmpresa);
			gerarNegociacoesContas(contas, negociacao);
		}
	}

	private void gerarNegociacoesExtrato(Integer idEmpresa, List<Integer> negociacoes) throws ErroRepositorioException, ControladorException {
		List<CobrancaDocumento> documentos = repositorioCobranca.obterExtratosCobrancaEmpresa(idEmpresa);

		for (CobrancaDocumento documento : documentos) {
			NegociacaoCobrancaEmpresa negociacao = new NegociacaoCobrancaEmpresa(documento, new Empresa(idEmpresa), new Date());

			Integer idNegociacao = (Integer) getControladorUtil().inserir(negociacao);
			negociacao.setId(idNegociacao);
			negociacoes.add(idNegociacao);

			List<ContaGeral> contas = repositorioCobranca.obterContasExtratosCobrancaEmpresa(documento.getId(), idEmpresa);
			gerarNegociacoesContas(contas, negociacao);
		}
	}

	private void gerarNegociacoesGuias(Integer idEmpresa, List<Integer> negociacoes) throws ErroRepositorioException, ControladorException {
		List<GuiaPagamentoGeral> guias = repositorioCobranca.obterGuiasCobrancaEmpresa(idEmpresa);

		for (GuiaPagamentoGeral guia : guias) {
			NegociacaoCobrancaEmpresa negociacao = new NegociacaoCobrancaEmpresa(guia, new Empresa(idEmpresa), new Date());

			Integer idNegociacao = (Integer) getControladorUtil().inserir(negociacao);
			negociacao.setId(idNegociacao);
			negociacoes.add(idNegociacao);

			List<ContaGeral> contas = repositorioCobranca.obterContasGuiaCobrancaEmpresa(guia.getId(), idEmpresa);
			gerarNegociacoesContas(contas, negociacao);
		}
	}

	private void gerarNegociacoesContas(List<ContaGeral> contas, NegociacaoCobrancaEmpresa negociacao) {
		try {
			for (ContaGeral contaGeral : contas) {
				NegociacaoContaCobrancaEmpresa contaCobranca = new NegociacaoContaCobrancaEmpresa(negociacao, contaGeral, new Date());
				getControladorUtil().inserir(contaCobranca);
			}
		} catch (ControladorException e) {
			e.printStackTrace();
		}
	}

	public void gerarArquivoTextoNegociacoesCobrancaEmpresa(Integer idEmpresa, List<Integer> idNegociacoes) throws ControladorException {
		if (!idNegociacoes.isEmpty()) {
			try {
				List<NegociacaoCobrancaEmpresa> negociacoes = obterNegociacoesEmpresa(idNegociacoes);

				StringBuilder arquivoTxt = new StringBuilder();
				for (NegociacaoCobrancaEmpresa negociacao : negociacoes) {
					ArquivoTextoNegociacaoCobrancaEmpresaHelper helper = new ArquivoTextoNegociacaoCobrancaEmpresaBuilder(negociacao).buildHelper();
					arquivoTxt.append(helper.getArquivoTextoNegociacoes());
				}

				enviarEmailArquivoNegociacoes(idEmpresa, arquivoTxt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private List<NegociacaoCobrancaEmpresa> obterNegociacoesEmpresa(List<Integer> idNegociacoes) throws ControladorException {
		List<NegociacaoCobrancaEmpresa> retorno = new ArrayList<NegociacaoCobrancaEmpresa>();

		try {
			retorno = repositorioCobranca.obterNegociacoesEmpresa(idNegociacoes);
		} catch (ErroRepositorioException e) {
			e.printStackTrace();
		}
		return retorno;
	}
	
	
	public void atualizarPagamentosContasCobranca(int idFuncionalidadeIniciada, Integer idLocalidade, Integer anoMesArrecadacao) throws ControladorException {
		int idUnidadeIniciada = getControladorBatch().iniciarUnidadeProcessamentoBatch(idFuncionalidadeIniciada, UnidadeProcessamento.LOCALIDADE, idLocalidade);
		
		Map<String, EmpresaCobrancaContaPagamentos> mapPagamentos = new HashMap<String, EmpresaCobrancaContaPagamentos>();
		try {
			Map<Integer, List<Integer>> mapImovelPagamento = new HashMap<Integer, List<Integer>>();
			
			Collection<EmpresaCobrancaContaPagamentos> pagamentos = obterPagamentosEmpresa(idLocalidade, anoMesArrecadacao);
			
			Map<Integer, List<EmpresaCobrancaContaPagamentos>> mapPagamentosEmpresa = obterHashPagamentosPorEmpresa(pagamentos);
			
			List<EmpresaCobrancaContaPagamentos> pagamentosParaInserir = new ArrayList<EmpresaCobrancaContaPagamentos>();
			for (Integer idEmpresa : mapPagamentosEmpresa.keySet()) {
				List<EmpresaCobrancaContaPagamentos> pagamentosPorEmpresa = mapPagamentosEmpresa.get(idEmpresa);
				
				for (EmpresaCobrancaContaPagamentos pagamento : pagamentos) {
					System.out.println("Imovel:" + pagamento.getIdImovel());
					if (!isPagamentoDuplicado(pagamento, mapPagamentos)) {
						getControladorUtil().inserir(pagamento);
						pagamentosParaInserir.add(pagamento);
					}
				}
				
			atualizarSituacaoCobranca(pagamentosParaInserir, idEmpresa);

			}
			
			getControladorBatch().encerrarUnidadeProcessamentoBatch(null, idUnidadeIniciada, false);
		} catch (Exception ex) {
			ex.printStackTrace();
			getControladorBatch().encerrarUnidadeProcessamentoBatch(ex, idUnidadeIniciada, true);
		}
	}
	
	private boolean isPagamentoDuplicado(EmpresaCobrancaContaPagamentos pagamento, Map<String, EmpresaCobrancaContaPagamentos> mapPagamentos) {
		if (mapPagamentos.containsKey(obterHashPagamento(pagamento))) {
			return true;
		} else {
			mapPagamentos.put(obterHashPagamento(pagamento),pagamento);
			return false;
		}
	}
	
	private String obterHashPagamento(EmpresaCobrancaContaPagamentos pagamento) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(pagamento.getIdImovel()) 
				.append(pagamento.getEmpresaCobrancaConta().getId()) 
				.append(pagamento.getValorPagamentoMes())
				.append(pagamento.getAnoMesReferenciaPagamento()) 
				.append(pagamento.getNumeroParcelaAtual());
		
		if (pagamento.getDebitoTipo() != null)
			builder.append(pagamento.getDebitoTipo().getId());
		
		return builder.toString(); 
	}
	
	

	private void atualizarSituacaoCobranca(List<EmpresaCobrancaContaPagamentos> pagamentos, Integer idComando) throws ErroRepositorioException {
		List<Integer> idsImoveis = obterImoveisPagamentos(pagamentos);
		
		List<Integer> imoveisContasPagas = repositorio.obterContasPagas(idsImoveis, idComando); 
		try {
			SistemaParametro parametros = getControladorUtil().pesquisarParametrosDoSistema();
			atualizarSituacaoCobrancaImovel(imoveisContasPagas);
			atualizarImovelCobrancaSituacao(imoveisContasPagas, new Date());
			atualizarCobrancaSituacaoHistorico(imoveisContasPagas, parametros.getAnoMesFaturamento(), "TODAS AS CONTAS FORAM PAGAS", new Date(), null);
		} catch (ControladorException e) {
			e.printStackTrace();
		}
	}
	
	private List<Integer> obterImoveisPagamentos(List<EmpresaCobrancaContaPagamentos> pagamentos) {
		List<Integer> ids = new ArrayList<Integer>();
		
		for (EmpresaCobrancaContaPagamentos pagamento : pagamentos) {
			ids.add(pagamento.getIdImovel());
		}
		
		return ids;
	}

	@SuppressWarnings("unchecked")
	private void atualizarImovelCobrancaSituacao(List<Integer> idsImoveis, Date dataRetirada) throws ControladorException {
		Filtro filtro = new FiltroImovelCobrancaSituacao();
		
		filtro.adicionarParametro(new ParametroSimplesIn(FiltroImovelCobrancaSituacao.IMOVEL_ID,
				idsImoveis));
		filtro.adicionarParametro(new ParametroSimples(FiltroImovelCobrancaSituacao.ID_COBRANCA_SITUACAO, CobrancaSituacao.COBRANCA_EMPRESA_TERCEIRIZADA));
		filtro.adicionarParametro(new ParametroNulo(FiltroImovelCobrancaSituacao.DATA_RETIRADA_COBRANCA));
		
		List<ImovelCobrancaSituacao> imoveis = new ArrayList<ImovelCobrancaSituacao>();
		
		Collection<ImovelCobrancaSituacao> colecao = getControladorUtil().pesquisar(filtro, ImovelCobrancaSituacao.class.getName());
		
		if (colecao != null && !colecao.isEmpty()) {
			for (ImovelCobrancaSituacao imovelCobranca : colecao) {
				imovelCobranca.setDataRetiradaCobranca(new Date());
				imovelCobranca.setUltimaAlteracao(new Date());

				imoveis.add(imovelCobranca);
			}
			
			getControladorBatch().atualizarColecaoObjetoParaBatch(imoveis);
		}
	}
	
	private void atualizarSituacaoCobrancaImovel(List<Integer> idImoveis) throws ControladorException {
		List<Imovel> imoveis = new ArrayList<Imovel>();
		
		for (Integer idImovel : idImoveis) {
			Imovel imovel = getControladorImovel().pesquisarImovel(idImovel);
			if (imovel != null ) {
				imovel.setCobrancaSituacao(null);
				imovel.setCobrancaSituacaoTipo(null);
				imovel.setUltimaAlteracao(new Date());
				
				imoveis.add(imovel);
			}
		}
		getControladorBatch().atualizarColecaoObjetoParaBatch(imoveis);
	}
	
	
	@SuppressWarnings("unchecked")
	private void atualizarCobrancaSituacaoHistorico(List<Integer> idsImoveis, Integer anoMesRetirada, String observacaoRetirada, Date dataFim, Usuario usuario) throws ControladorException {
		Filtro filtro = new FiltroCobrancaSituacaoHistorico();
		
		filtro.adicionarParametro(new ParametroSimplesIn(FiltroCobrancaSituacaoHistorico.IMOVEL_ID,
				idsImoveis));
		filtro.adicionarParametro(new ParametroSimples(FiltroCobrancaSituacaoHistorico.COBRANCA_TIPO_ID, CobrancaSituacaoTipo.COBRANCA_EMPRESA_TERCEIRIZADA));
		filtro.adicionarParametro(new ParametroNulo(FiltroCobrancaSituacaoHistorico.ANO_MES_COBRANCA_RETIRADA));
		
		Collection<CobrancaSituacaoHistorico> colecao = getControladorUtil().pesquisar(filtro, CobrancaSituacaoHistorico.class.getName());

		List<CobrancaSituacaoHistorico> historicos = new ArrayList<CobrancaSituacaoHistorico>();
		
		if (colecao != null && !colecao.isEmpty()) {
			for (CobrancaSituacaoHistorico historico : colecao) {
				historico.setAnoMesCobrancaRetirada(Util.formataAnoMes(new Date()));
				historico.setObservacaoRetira(observacaoRetirada);
				historico.setUsuarioRetira(usuario);
				historico.setDataFimSituacao(new Date());
				historico.setUltimaAlteracao(new Date());
				
				historicos.add(historico);
			}
			
			getControladorBatch().atualizarColecaoObjetoParaBatch(historicos);
		}
	}
	
	private Collection<EmpresaCobrancaContaPagamentos> obterPagamentosEmpresa(Integer idLocalidade, Integer anoMesArrecadacao) throws ErroRepositorioException {
		Collection<EmpresaCobrancaContaPagamentos> pagamentosEmpresa = new ArrayList<EmpresaCobrancaContaPagamentos>();

		boolean flagTerminou = false;
		final int quantidadeRegistros = 500;
		int numeroPaginas = 0;
		try {
			while (!flagTerminou) {
				Collection<Pagamento> pagamentos = getControladorArrecadacao().obterPagamentosClassificadosNaoRegistradosCobrancaPorEmpresa(idLocalidade, anoMesArrecadacao, numeroPaginas, quantidadeRegistros);

				if (pagamentos != null && !pagamentos.isEmpty()) {
					for (Pagamento pagamento : pagamentos) {
						if (categoriaPermiteGerarPagamento(pagamento)) {
							pagamentosEmpresa.addAll(gerarPagamentoCobrancaDeContas(pagamento));
							pagamentosEmpresa.addAll(gerarPagamentosCobrancaDeGuias(pagamento));
							pagamentosEmpresa.addAll(gerarPagamentosCobrandaDeDebitos(pagamento));
						}
					}
				}

				if (pagamentos == null || pagamentos.size() < quantidadeRegistros) {

					flagTerminou = true;
				}

				if (pagamentos != null) {
					pagamentos.clear();
					pagamentos = null;
				}

				numeroPaginas += quantidadeRegistros;
			}
		} catch (ControladorException e) {
			e.printStackTrace();
		}
		return pagamentosEmpresa;
	}
	
	private Map<Integer, List<EmpresaCobrancaContaPagamentos>> obterHashPagamentosPorEmpresa(Collection<EmpresaCobrancaContaPagamentos> pagamentosEmpresa) {
		Map<Integer, List<EmpresaCobrancaContaPagamentos>> mapPagamentos = new HashMap<Integer, List<EmpresaCobrancaContaPagamentos>>();
		
		for (EmpresaCobrancaContaPagamentos pagamento : pagamentosEmpresa) {
			if (mapPagamentos.containsKey(pagamento.getEmpresaCobrancaConta().getComandoEmpresaCobrancaConta().getId())) {
				List<EmpresaCobrancaContaPagamentos> lista = mapPagamentos.get(pagamento.getEmpresaCobrancaConta().getComandoEmpresaCobrancaConta().getId());
				lista.add(pagamento);
			} else {
				List<EmpresaCobrancaContaPagamentos> lista = new ArrayList<EmpresaCobrancaContaPagamentos>();
				lista.add(pagamento);
				mapPagamentos.put(pagamento.getEmpresaCobrancaConta().getComandoEmpresaCobrancaConta().getId(), lista);
			}
		}
		
		return mapPagamentos;
		
	}
	
	private boolean categoriaPermiteGerarPagamento(Pagamento pagamento) throws ControladorException {
		boolean permite = false;
		
		if (pagamento.getImovel() != null) {
			Categoria categoria = getControladorImovel().obterPrincipalCategoriaImovel(pagamento.getImovel().getId());
			
			if (!categoria.getId().equals(Categoria.PUBLICO)) {
				permite =  true;
			}
			
		} else {
			permite =  true;
		}
		return permite;
	}

	private Collection<EmpresaCobrancaContaPagamentos> gerarPagamentoCobrancaDeContas(Pagamento pagamento)
			throws ErroRepositorioException, ControladorException {
		
		Collection<EmpresaCobrancaContaPagamentos> pagamentosEmpresa = new ArrayList<EmpresaCobrancaContaPagamentos>();
		
		if (pagamento.getContaGeral() != null) {
			if (isContaEmCobranca(pagamento)) {
				pagamentosEmpresa.addAll(criaColecaoEmpresaContaCobrancaPagamento(pagamento.getContaGeral().getId(), pagamento.getValorPagamento(),
						pagamento, null, null, false, null, ConstantesSistema.INDICADOR_PAGAMENTO_A_VISTA, null));
			} else {
				List<DebitoCobrado> debitosCobrados = obterDebitosDePagamentoDeParcelamento(pagamento);
				for (DebitoCobrado debitoCobrado : debitosCobrados) {
					Parcelamento parcelamento =  null;
							
					if (debitoCobrado.getDebitoACobrarGeral() != null
							&& debitoCobrado.getDebitoACobrarGeral().getDebitoACobrar() != null
							&& debitoCobrado.getDebitoACobrarGeral().getDebitoACobrar().getParcelamento() != null ) {
						parcelamento = debitoCobrado.getDebitoACobrarGeral().getDebitoACobrar().getParcelamento();
					}
					
					pagamentosEmpresa.addAll(verificarItensParcelamentos(parcelamento, null, null, pagamento, debitoCobrado, pagamento.getAnoMesReferenciaArrecadacao()));
				}
			}
		}
		return pagamentosEmpresa;
	}

	private Boolean isContaEmCobranca(Pagamento pagamento) throws ErroRepositorioException {
		EmpresaCobrancaConta contaCobranca = repositorio.pesquisarEmpresaCobrancaConta(pagamento.getContaGeral().getId());
		
		if (contaCobranca != null && isContaPrazoCobranca(pagamento, contaCobranca.getComandoEmpresaCobrancaConta())) 
			return true;
		else return false;
	}
	
	private boolean isContaPrazoCobranca(Pagamento pagamento, ComandoEmpresaCobrancaConta comando) {
		if (comando.getDataExecucao() != null && comando.getDataFimCiclo() != null) {
			if (pagamento.getDataPagamento().before(comando.getDataFimCiclo()))
				return true;
			else return false;
		} else return false;
	}

	private Collection<EmpresaCobrancaContaPagamentos> gerarPagamentosCobrandaDeDebitos(Pagamento pagamento) throws ControladorException {
		Collection<EmpresaCobrancaContaPagamentos> pagamentosEmpresa = new ArrayList<EmpresaCobrancaContaPagamentos>();

		if (pagamento.getDebitoACobrarGeral() != null
				&& (pagamento.getDebitoACobrarGeral().getDebitoACobrar().getParcelamento() != null)) {

			pagamentosEmpresa.addAll(verificarItensParcelamentos(pagamento.getDebitoACobrarGeral().getDebitoACobrar().getParcelamento(), null,
					pagamento.getDebitoACobrarGeral().getDebitoACobrar(), pagamento, null, pagamento.getAnoMesReferenciaArrecadacao()));
		}
		return pagamentosEmpresa;
	}

	private Collection<EmpresaCobrancaContaPagamentos> gerarPagamentosCobrancaDeGuias(Pagamento pagamento) throws ControladorException {
		Collection<EmpresaCobrancaContaPagamentos> pagamentosEmpresa = new ArrayList<EmpresaCobrancaContaPagamentos>();

		if (pagamento.getGuiaPagamento() != null && pagamento.getGuiaPagamento().getGuiaPagamento().getParcelamento() != null) {
			
			pagamentosEmpresa.addAll(verificarItensParcelamentos(pagamento.getGuiaPagamento().getGuiaPagamento().getParcelamento(), pagamento.getGuiaPagamento().getGuiaPagamento(), null, 
					pagamento, null, pagamento.getAnoMesReferenciaArrecadacao()));
		}
		return pagamentosEmpresa;
	}

	private List<DebitoCobrado> obterDebitosDePagamentoDeParcelamento(Pagamento pagamento) throws ControladorException {
		
		Collection<Integer> financiamentos = obterFinanciamentoTipoParcelamento();

		Collection<Object[]> colecaoDadosDebitoCobrado = getControladorFaturamento().pesquisaridDebitoTipoDoDebitoCobradoDeParcelamento(
															pagamento.getContaGeral().getId(), financiamentos);
		
		List<DebitoCobrado> debitos = new ArrayList<DebitoCobrado>();

		if (colecaoDadosDebitoCobrado != null && !colecaoDadosDebitoCobrado.isEmpty()) {

			for (Object[] dadosDebitoCobrado : colecaoDadosDebitoCobrado) {
				if (dadosDebitoCobrado != null) {
					DebitoTipo debitoTipo = null;
					Parcelamento parcelamento = null;
					DebitoCobrado debitoCobrado = null;

					if (dadosDebitoCobrado[3] != null) {
						debitoCobrado = new DebitoCobrado();
						debitoCobrado.setValorPrestacao((BigDecimal) dadosDebitoCobrado[3]);
						debitoCobrado.setNumeroPrestacaoDebito((Short) dadosDebitoCobrado[4]);
						debitoCobrado.setNumeroPrestacao((Short) dadosDebitoCobrado[5]);
						if (dadosDebitoCobrado[0] != null) {
							debitoTipo = new DebitoTipo();
							debitoTipo.setId((Integer) dadosDebitoCobrado[0]);
							debitoCobrado.setDebitoTipo(debitoTipo);
						}

						if (dadosDebitoCobrado[1] != null) {
							parcelamento = new Parcelamento();
							parcelamento.setId((Integer) dadosDebitoCobrado[1]);
							if (dadosDebitoCobrado[2] != null) {
								parcelamento.setValorDebitoAtualizado((BigDecimal) dadosDebitoCobrado[2]);
							}
							if (dadosDebitoCobrado[6] != null) {
								parcelamento.setValorConta((BigDecimal) dadosDebitoCobrado[6]);
							}
							DebitoACobrar debitoACobrar = new DebitoACobrar();
							debitoACobrar.setParcelamento(parcelamento);
							
							debitoCobrado.setDebitoACobrarGeral(new DebitoACobrarGeral(debitoACobrar));
						}
					}
					
					debitos.add(debitoCobrado);
				}
			}
		}
		
		return debitos;
	}

	private Collection<Integer> obterFinanciamentoTipoParcelamento() {
		Collection<Integer> collIdsFincanciamentoTipo = new ArrayList<Integer>();
		
		collIdsFincanciamentoTipo.add(FinanciamentoTipo.PARCELAMENTO_AGUA);
		collIdsFincanciamentoTipo.add(FinanciamentoTipo.PARCELAMENTO_ESGOTO);
		collIdsFincanciamentoTipo.add(FinanciamentoTipo.PARCELAMENTO_SERVICO);
		collIdsFincanciamentoTipo.add(FinanciamentoTipo.JUROS_PARCELAMENTO);
		collIdsFincanciamentoTipo.add(FinanciamentoTipo.ENTRADA_PARCELAMENTO);
		
		return collIdsFincanciamentoTipo;
	}
	
	private Collection<EmpresaCobrancaContaPagamentos> criaColecaoEmpresaContaCobrancaPagamento(Integer idConta, BigDecimal valorConta, Pagamento pagamento,
			DebitoTipo debitoTipo, Parcelamento parcelamento, boolean nivel2, BigDecimal valorPagamentoSemPercentual,
			Short indicadorTipoPagamento, DebitoCobrado debitoCobrado) throws ControladorException {

		Collection<EmpresaCobrancaContaPagamentos> pagamentosEmpresa = new ArrayList<EmpresaCobrancaContaPagamentos>();
		try {

			EmpresaCobrancaConta empresaCobrancaConta = repositorio.pesquisarEmpresaCobrancaConta(idConta);

			if (empresaCobrancaConta != null) {
				// caso seja um re-parcelamento
				BigDecimal percentualContaParcelada = null;
				BigDecimal valorPagamentoMes = null;

				if (nivel2) {
					percentualContaParcelada = Util.dividirArredondando(valorConta, parcelamento.getValorConta());
					valorPagamentoMes = (valorPagamentoSemPercentual.multiply(percentualContaParcelada)).setScale(2,
							BigDecimal.ROUND_HALF_DOWN);
				} else {
					// caso exista parcelamento, calcular o percentual da conta paga
					if (parcelamento != null) {
						percentualContaParcelada = Util.dividirArredondando(valorConta, parcelamento.getValorConta());
						valorPagamentoMes = (valorPagamentoSemPercentual.multiply(percentualContaParcelada)).setScale(2,
								BigDecimal.ROUND_HALF_DOWN);
					} else {
						// caso n�o tenha parcelamento, o pagamento refere-se a uma conta e esteja em cobran�a por alguma empresa.
						valorPagamentoMes = valorConta;
					}
				}

				EmpresaCobrancaContaPagamentos pagamentoEmpresa = new EmpresaCobrancaContaPagamentos();
				pagamentoEmpresa.setDebitoTipo(debitoTipo);
				pagamentoEmpresa.setEmpresaCobrancaConta(empresaCobrancaConta);
				pagamentoEmpresa.setAnoMesPagamentoArrecadacao(pagamento.getAnoMesReferenciaArrecadacao());
				pagamentoEmpresa.setValorPagamentoMes(valorPagamentoMes);
				pagamentoEmpresa.setIndicadorTipoPagamento(obterPagamentoTipoDoPagamento(debitoTipo));
				pagamentoEmpresa.setNumeroParcelaAtual(debitoCobrado != null ? new Integer(debitoCobrado.getNumeroPrestacaoDebito()) : new Integer("0"));
				pagamentoEmpresa.setNumeroTotalParcelas(debitoCobrado != null ? new Integer(debitoCobrado.getNumeroPrestacao()) : new Integer("0"));
				pagamentoEmpresa.setUltimaAlteracao(new Date());
				pagamentoEmpresa.setIndicadorGeracaoArquivo(ConstantesSistema.NAO);
				pagamentoEmpresa.setIdPagamento(pagamento.getId());
				
				if (pagamento.getAnoMesReferenciaPagamento() != null) {
					pagamentoEmpresa.setAnoMesReferenciaPagamento(pagamento.getAnoMesReferenciaPagamento());
				}
				
				pagamentoEmpresa.setDataPagamento(pagamento.getDataPagamento());
				
				if (pagamento.getImovel() != null) {
					pagamentoEmpresa.setIdImovel(pagamento.getImovel().getId());
				}
				
				if (pagamento.getAvisoBancario() != null) {
					pagamentoEmpresa.setIdArrecadador(pagamento.getAvisoBancario().getArrecadador().getId());
				}
				
				if (pagamento.getValorDesconto() != null) {
					pagamentoEmpresa.setValorDesconto(this.calcularValorDesconto(pagamento, valorConta));
				}
				
				pagamentosEmpresa.add(pagamentoEmpresa);
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new EJBException(ex);
		}
		return pagamentosEmpresa;
	}
	
	private BigDecimal calcularValorDesconto(Pagamento pagamento, BigDecimal valorConta) {
		BigDecimal desconto = new BigDecimal(0);
		
		if (pagamento.getValorDesconto() != null)
			desconto = (valorConta.multiply(pagamento.getValorDesconto())).divide(pagamento.getValorPagamento());
		return desconto;
	}
	
	private Short obterPagamentoTipoDoPagamento(DebitoTipo debitoTipo) {
		if (debitoTipo == null) {
			return ConstantesSistema.INDICADOR_PAGAMENTO_A_VISTA;
		} else if (debitoTipo.getId().intValue() == DebitoTipo.ENTRADA_PARCELAMENTO.intValue()) {
			return ConstantesSistema.INDICADOR_PAGAMENTO_ENTRADA_PARCELAMENTO;
		} else {
			return ConstantesSistema.INDICADOR_PAGAMENTO_PARCELADO;
		}
	}
	
	private Collection<EmpresaCobrancaContaPagamentos> verificarItensParcelamentos(Parcelamento parcelamento, GuiaPagamento guiaPagamento, DebitoACobrar debitoACobrar,
			Pagamento pagamento, DebitoCobrado debitoCobrado, Integer anoMesArrecadacao)
			throws ControladorException {
		
		Collection<EmpresaCobrancaContaPagamentos> pagamentosEmpresa = new ArrayList<EmpresaCobrancaContaPagamentos>();
		try {

			if (parcelamento != null) {

				List<ParcelamentoItem> itens = repositorioCobranca.pesquisarItensParcelamentos(parcelamento.getId());
				
				if (itens != null && !itens.isEmpty()) {
					
					for (ParcelamentoItem item : itens) {

						if (item.getContaGeral() != null) {
							
							BigDecimal valorConta = item.getContaGeral().obterValorConta();

							// caso n�o seja guia de pagamento nem debito a cobrar
							if (guiaPagamento == null && debitoACobrar == null) {
								
								// [SB0003] - Atualizar pagamento de conta parcelada a partir do debito cobrado
								pagamentosEmpresa.addAll(criaColecaoEmpresaContaCobrancaPagamento(item.getContaGeral().getId(), valorConta, pagamento,
										debitoCobrado.getDebitoTipo(), parcelamento, false, debitoCobrado.getValorPrestacao(),
										ConstantesSistema.INDICADOR_PAGAMENTO_PARCELADO,debitoCobrado));
							} else {
								if (guiaPagamento != null) {
									// [SB0007] - Atualizar pagamento de conta parcelada a partir da guia de pagamento
									pagamentosEmpresa.addAll(criaColecaoEmpresaContaCobrancaPagamento(item.getContaGeral().getId(), valorConta, pagamento,
											guiaPagamento.getDebitoTipo(), parcelamento, false, guiaPagamento.getValorDebito(),
											ConstantesSistema.INDICADOR_PAGAMENTO_A_VISTA, null));
								} else {
									// [SB0011] - Atualizar pagamento de conta parcelada a partir do debito a cobrar
									pagamentosEmpresa.addAll(criaColecaoEmpresaContaCobrancaPagamento(item.getContaGeral().getId(), valorConta, pagamento,
											debitoACobrar.getDebitoTipo(), parcelamento, false, debitoACobrar.getValorTotalComBonus(),
											ConstantesSistema.INDICADOR_PAGAMENTO_PARCELADO, null));
								}
							}
						}
						
						// verifica se existe o id do debito a cobrar geral, refeere-se a um re-parcelamento
						if (item.getDebitoACobrarGeral() != null) {
							pagamentosEmpresa.addAll(verificarItensParcelamentosNivel2(parcelamento, guiaPagamento, debitoACobrar, pagamento, debitoCobrado,
									anoMesArrecadacao, item.getDebitoACobrarGeral().getId()));
						}

					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new EJBException(ex);
		}
		
		return pagamentosEmpresa;
	}
	
	@SuppressWarnings("unused")
	private Collection<EmpresaCobrancaContaPagamentos> verificarItensParcelamentosNivel2(Parcelamento parcelamento, GuiaPagamento guiaPagamento, DebitoACobrar debitoACobrar,
			Pagamento pagamento, DebitoCobrado debitoCobrado, Integer anoMesArrecadacao, Integer idDebitoACobrarNivel2) throws ControladorException {
		
		Collection<EmpresaCobrancaContaPagamentos> pagamentosEmpresa = new ArrayList<EmpresaCobrancaContaPagamentos>();
		
		try {

			Integer idParcelamento = null;

			if (idParcelamento != null) {

				Collection<Object[]> collItensParcelamentosNivel2 = repositorioCobranca.pesquisarItensParcelamentosNivel2(idParcelamento);

				Integer idContaGeralNivel2 = null;
				BigDecimal valorContaNivel2 = null;

				if (collItensParcelamentosNivel2 != null && !collItensParcelamentosNivel2.isEmpty()) {
					for (Object[] dadosItensParcelamento : collItensParcelamentosNivel2) {

						if (dadosItensParcelamento != null) {

							if (dadosItensParcelamento[2] != null) {
								valorContaNivel2 = (BigDecimal) dadosItensParcelamento[2];
							}

							if (dadosItensParcelamento[0] != null) {
								idContaGeralNivel2 = (Integer) dadosItensParcelamento[0];

								if (guiaPagamento == null && debitoACobrar == null) {
									// [SB0005] - Atualizar pagamento de conta parcelada a partir do debito cobrado - nivel 2
									
									pagamentosEmpresa.addAll(criaColecaoEmpresaContaCobrancaPagamento(idContaGeralNivel2, valorContaNivel2, pagamento,
											debitoCobrado.getDebitoTipo(), parcelamento, true, debitoCobrado.getValorPrestacao(),
											ConstantesSistema.INDICADOR_PAGAMENTO_PARCELADO, debitoCobrado));
								} else {
									if (guiaPagamento != null) {
										// [SB0008] - Atualizar pagamento de conta parcelada a partir da guia de - pagamento nivel 2
										pagamentosEmpresa.addAll(criaColecaoEmpresaContaCobrancaPagamento(idContaGeralNivel2, valorContaNivel2, pagamento,
												guiaPagamento.getDebitoTipo(), parcelamento, true, guiaPagamento.getValorDebito(),
												ConstantesSistema.INDICADOR_PAGAMENTO_A_VISTA, null));
									} else {
										// [SB0013] - Atualizar pagamento de conta parcelada a partir do debito a cobrar nivel 2
										pagamentosEmpresa.addAll(criaColecaoEmpresaContaCobrancaPagamento(idContaGeralNivel2, valorContaNivel2, pagamento,
												debitoACobrar.getDebitoTipo(), parcelamento, true, debitoACobrar.getValorTotalComBonus(),
												ConstantesSistema.INDICADOR_PAGAMENTO_PARCELADO, null));
									}
								}
							}
						}
						idContaGeralNivel2 = null;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new EJBException(ex);
		}
		return pagamentosEmpresa;
	}

	
	@SuppressWarnings("rawtypes")
	public void gerarArquivoTextoPagamentosCobrancaEmpresa(Integer idFuncionalidadeIniciada, Integer idEmpresa) throws ControladorException {
		
		int idUnidadeIniciada = getControladorBatch().iniciarUnidadeProcessamentoBatch(idFuncionalidadeIniciada, UnidadeProcessamento.EMPRESA, idEmpresa);

		try {

			StringBuilder arquivoCompleto = new StringBuilder();
			
			Collection colecaoDadosTxt = repositorioCobranca.pesquisarDadosArquivoTextoPagamentosContasCobrancaEmpresa(idEmpresa);
			
			if (colecaoDadosTxt != null && !colecaoDadosTxt.isEmpty()) {

				Iterator colecaoDadosTxtIterator = colecaoDadosTxt.iterator();

				List<Integer> idsCobrancaPagamentos = new ArrayList<Integer>();
				
				while (colecaoDadosTxtIterator.hasNext()) {

					Object[] dados = (Object[]) colecaoDadosTxtIterator.next();

					ArquivoTextoPagamentoContasCobrancaEmpresaHelper helper = new ArquivoTextoParagentosCobancaEmpresaBuilder(dados).buildHelper(); 
					
					arquivoCompleto.append(helper.getArquivoTexto());
					
					idsCobrancaPagamentos.add(helper.getIdEmpresaCobrancaContaPagamento());
				}
				enviarEmailArquivoPagamentos(idEmpresa, arquivoCompleto);
				
				atualizarPagamentosGerados(idsCobrancaPagamentos);
			}
			
			getControladorBatch().encerrarUnidadeProcessamentoBatch(null, idUnidadeIniciada, false);
		} catch (Exception ex) {
			getControladorBatch().encerrarUnidadeProcessamentoBatch(ex, idUnidadeIniciada, true);
			ex.printStackTrace();

			throw new EJBException(ex);
		}
	}
	
	private void atualizarPagamentosGerados(List<Integer> idsPagamentos) {
		try {
			repositorioCobranca.atualizarPagamentosCobrancaPorEmpresaGerados(idsPagamentos);
		} catch (ErroRepositorioException e) {
			e.printStackTrace();
		}
	}
	
	private void enviarEmailArquivoPagamentos(Integer idEmpresa, StringBuilder arquivo) throws ControladorException {
		String dataHora = Util.formatarDataAAAAMMDD(new Date()) + "-" + Util.formatarDataHHMM(new Date());

		String nomeArquivo = "pagamentos_contas_cobranca_empresa_" + idEmpresa + "_" + dataHora;
		
		EnvioEmail envioEmail = getControladorCadastro().pesquisarEnvioEmail(EnvioEmail.COBRANCA_EMPRESA);
		
		String titulo = "Pagamentos contas da Empresa de Cobran�a - " + idEmpresa ;
		String corpo = "Pagamentos contas da Empresa de Cobran�a : " + idEmpresa ;
		
		ServicosEmail.enviarArquivoCompactado(nomeArquivo, arquivo, envioEmail.getEmailReceptor(), envioEmail.getEmailRemetente(), titulo, corpo);
	}
	
	private void enviarEmailArquivoNegociacoes(Integer idEmpresa, StringBuilder arquivoTxt) throws ControladorException {
		String dataHora = Util.formatarDataAAAAMMDD(new Date()) + "-" + Util.formatarDataHHMM(new Date());

		String nomeArquivo = "movimento_contas_cobranca_empresa_" + idEmpresa + "_" + dataHora;

		EnvioEmail envioEmail = getControladorCadastro().pesquisarEnvioEmail(EnvioEmail.COBRANCA_EMPRESA);

		String titulo = "Negocia��es da Empresa de Cobran�a - " + idEmpresa;
		String corpo = "Negocia��es da Empresa de Cobran�a : " + idEmpresa;

		ServicosEmail.enviarArquivoCompactado(nomeArquivo, arquivoTxt, envioEmail.getEmailReceptor(), envioEmail.getEmailRemetente(), titulo, corpo);
	}

	@SuppressWarnings("rawtypes")
	public Integer pesquisarDadosGerarRelatorioPagamentosContasCobrancaEmpresaCount(Integer idEmpresa, String dataPagamentoInicial, String dataPagamentoFinal) throws ControladorException {
		Integer retorno = 0;

		try {
			Collection colecao = repositorio.pesquisarDadosGerarRelatorioPagamentosContasCobrancaEmpresaCount(idEmpresa, dataPagamentoInicial, dataPagamentoFinal);

			if (colecao != null && !colecao.isEmpty()) {
				Iterator iterator = colecao.iterator();
				while (iterator.hasNext()) {
					retorno += (Integer) iterator.next();
				}
			}
		} catch (ErroRepositorioException e) {
			throw new ControladorException("erro.sistema", e);
		}
		return retorno;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection pesquisarDadosGerarRelatorioPagamentosContasCobrancaEmpresa(RelatorioPagamentosContasCobrancaEmpresaHelper helper) throws ControladorException {
		Collection<RelatorioPagamentosContasCobrancaEmpresaBean> retorno = new ArrayList<RelatorioPagamentosContasCobrancaEmpresaBean>();
		Collection<Object[]> colecaoDados = null;

		try {
			colecaoDados = repositorio.pesquisarDadosGerarRelatorioPagamentosContasCobrancaEmpresaOpcaoTotalizacao(helper);

			if (colecaoDados != null && !colecaoDados.isEmpty()) {
				for (Object[] dados : colecaoDados) {
					RelatorioPagamentosContasCobrancaEmpresaBean bean = new RelatorioPagamentosContasCobrancaEmpresaBean();

					// idImovel
					if (dados[0] != null) {
						Integer idImovel = (Integer) dados[0];
						bean.setMatricula(Util.retornaMatriculaImovelFormatada(idImovel));
					}
					// nomeCliente
					if (dados[1] != null) {
						String nomeCliente = (String) dados[1];
						bean.setNomeCliente(nomeCliente);
					}
					// anoMesConta
					if (dados[2] != null) {
						Integer anoMesConta = (Integer) dados[2];
						bean.setAnoMesConta(Util.formatarAnoMesParaMesAno(anoMesConta.intValue()));
					}

					// valorConta
					if (dados[3] != null) {
						BigDecimal valorConta = (BigDecimal) dados[3];
						bean.setValorConta(Util.formatarMoedaReal(valorConta));
					}

					// anoMesReferenciaPagamento
					if (dados[4] != null) {
						Integer anoMesReferenciaPagamento = (Integer) dados[4];
						bean.setAnoMesReferenciaPagamento(Util.formatarAnoMesParaMesAno(anoMesReferenciaPagamento));
					}

					// valorPrincipal
					BigDecimal valorPrincipal = new BigDecimal(0.0);
					if (dados[5] != null) {
						valorPrincipal = (BigDecimal) dados[5];
					}
					
					bean.setValorPrincipal(Util.formatarMoedaReal(valorPrincipal));

					BigDecimal valorEncargos = new BigDecimal(0.0);
					// valorEncargos
					if (dados[6] != null) {
						valorEncargos = (BigDecimal) dados[6];
					}
					
					bean.setValorEncargos(Util.formatarMoedaReal(valorEncargos));

					BigDecimal percentualEmpresa = new BigDecimal(0.0);
					// percentualEmpresa
					if (dados[7] != null) {
						percentualEmpresa = (BigDecimal) dados[7];

					}
					bean.setPercentualEmpresa(percentualEmpresa.toString().replace(".", ","));

					// Id da Localidade
					if (dados[8] != null) {
						Integer idLocalidade = (Integer) dados[8];
						bean.setIdLocalidade(idLocalidade.toString());
					}

					// nome da Localidade
					if (dados[9] != null) {
						bean.setNomeLocalidade((String) dados[9]);
					}

					// Id Gerencia Regional
					if (dados[10] != null) {
						Integer idGerenciaRegional = (Integer) dados[10];
						bean.setIdGerenciaRegional(idGerenciaRegional.toString());
					}

					// Nome Gerencia Regional
					if (dados[11] != null) {
						bean.setNomeGerenciaRegional((String) dados[11]);
					}

					// Id Unidade Negocio
					if (dados[12] != null) {
						Integer idUnidadeNegocio = (Integer) dados[12];
						bean.setIdUnidadeNegocio(idUnidadeNegocio.toString());
					}

					// Nome Unidade Negocio
					if (dados[13] != null) {
						bean.setNomeUnidadeNegocio((String) dados[13]);
					}

					// Id Rota
					if (dados[14] != null) {
						Integer idRota = (Integer) dados[14];
						bean.setIdRota(idRota.toString());
					}
					// Indicador do Tipo de Pagamento
					if (dados[15] != null) {
						Short indicadorTipoPagamento = (Short) dados[15];
						bean.setIndicadorTipoPagamento(indicadorTipoPagamento.toString());
						if (indicadorTipoPagamento.intValue() == ConstantesSistema.INDICADOR_PAGAMENTO_A_VISTA.intValue()) {
							bean.setTipoPagamento("� Vista");
						} else if (indicadorTipoPagamento.intValue() == ConstantesSistema.INDICADOR_PAGAMENTO_PARCELADO.intValue()) {
							bean.setTipoPagamento("Parcelado");
						} else {
							bean.setTipoPagamento("Entrada");
						}
					}

					// Numero Parcela Atual
					if (dados[16] != null) {
						Integer numeroParcelaAtual = (Integer) dados[16];
						bean.setNumeroParcelaAtual(numeroParcelaAtual.toString());
					}
					// Numero Total Parcelas
					if (dados[17] != null) {
						Integer numeroTotalParcelas = (Integer) dados[17];
						bean.setNumeroTotalParcelas(numeroTotalParcelas.toString());
					}

					BigDecimal valorTotal = new BigDecimal(0.0);

					valorTotal = valorTotal.add(valorPrincipal);
					valorTotal = valorTotal.add(valorEncargos);

					// Valor total
					bean.setValorTotalPagamentos(Util.formatarMoedaReal4Casas(valorTotal));

					// Valor Empresa
					BigDecimal valorEmpresa = new BigDecimal(0.0);

					BigDecimal aux = new BigDecimal(100.0);

					valorEmpresa = valorEmpresa.add(valorTotal.multiply(percentualEmpresa));
					valorEmpresa = valorEmpresa.setScale(4, BigDecimal.ROUND_HALF_UP);
					valorEmpresa = valorEmpresa.divide(aux);
					valorEmpresa = valorEmpresa.setScale(4, BigDecimal.ROUND_HALF_UP);

					bean.setValorEmpresa(Util.formatarMoedaReal4Casas(valorEmpresa));
					bean.setCodigoQuebra2("");
					bean.setDescricaoQuebra2("");

					if (helper.getOpcaoTotalizacao().equalsIgnoreCase("estadoGerencia")) {
						bean.setCodigoQuebra(bean.getIdGerenciaRegional());
						bean.setDescricaoQuebra(bean.getNomeGerenciaRegional());
					} else if (helper.getOpcaoTotalizacao().equalsIgnoreCase("estadoLocalidade")) {
						bean.setCodigoQuebra(bean.getIdLocalidade());
						bean.setDescricaoQuebra(bean.getNomeLocalidade());
					} else if (helper.getOpcaoTotalizacao().equalsIgnoreCase("gerenciaRegional")) {
						bean.setCodigoQuebra(bean.getIdGerenciaRegional());
						bean.setDescricaoQuebra(bean.getNomeGerenciaRegional());
					} else if (helper.getOpcaoTotalizacao().equalsIgnoreCase("gerenciaRegionalLocalidade")) {
						bean.setCodigoQuebra(bean.getIdGerenciaRegional());
						bean.setDescricaoQuebra(bean.getNomeGerenciaRegional());
						bean.setCodigoQuebra2(bean.getIdLocalidade());
						bean.setDescricaoQuebra2(bean.getNomeLocalidade());
					} else if (helper.getOpcaoTotalizacao().equalsIgnoreCase("localidade")) {
						bean.setCodigoQuebra(bean.getIdLocalidade());
						bean.setDescricaoQuebra(bean.getNomeLocalidade());
					} else if (helper.getOpcaoTotalizacao().equalsIgnoreCase("estadoUnidadeNegocio")) {
						bean.setCodigoQuebra(bean.getIdUnidadeNegocio());
						bean.setDescricaoQuebra(bean.getNomeUnidadeNegocio());
					} else if (helper.getOpcaoTotalizacao().equalsIgnoreCase("unidadeNegocio")) {
						bean.setCodigoQuebra(bean.getIdUnidadeNegocio());
						bean.setDescricaoQuebra(bean.getNomeUnidadeNegocio());
					} else {
						bean.setCodigoQuebra("");
						SistemaParametro sistemaParametro = getControladorUtil().pesquisarParametrosDoSistema();
						bean.setDescricaoQuebra(sistemaParametro.getNomeEstado());
					}
					retorno.add(bean);
				}
			}
		} catch (ErroRepositorioException e) {
			sessionContext.setRollbackOnly();
			throw new ControladorException("erro.sistema", e);
		}
		return retorno;
	}
	
	public int retirarSituacaoCobranca(BufferedReader buffer, Usuario usuario) throws ControladorException {
		try {
			int total = 0;
			String linha = buffer.readLine();
			while (linha != null && !linha.isEmpty()) {
				String[] dados = linha.split(";");
				Integer idImovel = Integer.parseInt(dados[0]);
				Date data = Util.converteStringParaDate(dados[1]);
				String motivo = dados[2].toUpperCase();
				
				List<Integer> imoveis = new ArrayList<Integer>();
				imoveis.add(idImovel);
				
				atualizarImovelCobrancaSituacao(imoveis, data);
				atualizarCobrancaSituacaoHistorico(imoveis, Util.formataAnoMes(data), "RETIRADA MANUAL - " + motivo, data, usuario);
				retirarCobrancaConta(idImovel);
				
				total++;
				linha = buffer.readLine();
			}
			
			buffer.close();
			
			return total;
		} catch (IOException e) {
			throw new ControladorException("erro.sistema", e);
		}
	}

	public void gerarComando(ComandoEmpresaCobrancaContaHelper helper) throws ControladorException {
		Integer id = inserirComandoEmpresaCobrancaConta(helper.getComando(), helper.getUsuario());
		
		logger.info("Gerando Comando de Cobran�a por Resultado - ID: " + id);
		inserirComandoUnidadeNegocio(helper.getIdsUnidadeNegocio(), id);
		inserirComandoGerenciaRegional(helper.getIdsGerenciaRegional(), id);
		inserirComandoImovelPerfil(helper.getIdsImovelPerfil(), id);
		inserirComandoLigacaoAguaSituacao(helper.getIdsLigacaoAguaSituacao(), id);

		totalizarComando(helper, id);
		logger.info("Gera��o de Comando de Cobran�a por Resultado finalizada - ID: " + id);
	}

	private void totalizarComando(ComandoEmpresaCobrancaContaHelper helper, Integer idComando) throws ControladorException {
		int qtdContas = 0;
		int qtdClientes = 0;
		BigDecimal valorTotal = new BigDecimal(0.00);
		
		EmpresaCobranca empresa = filtrarEmpresaCobranca(helper.getComando());
		
		List<Object[]> lista = pesquisarQuantidadeContas(helper, empresa.isPercentualInformado());
		if (lista != null && !lista.isEmpty()) {
			for (Iterator<Object[]> iterator = lista.iterator(); iterator.hasNext();) {
				Object[] dados = (Object[]) iterator.next();
				qtdClientes += 1;
				valorTotal = valorTotal.add((BigDecimal) dados[1]);
				qtdContas += (Integer) dados[2];
			}
		}
		
		atualizarComando(idComando, qtdContas, qtdClientes, valorTotal);
	}

	@SuppressWarnings("unchecked")
	private void atualizarComando(Integer idComando, int qtdContas, int qtdClientes, BigDecimal valorTotal) throws ControladorException {
		Filtro filtro = new FiltroComandoEmpresaCobrancaConta();		
		filtro.adicionarParametro(new ParametroSimples(FiltroComandoEmpresaCobrancaConta.ID, idComando));
		Collection<ComandoEmpresaCobrancaConta> colecao = getControladorUtil().pesquisar(filtro, ComandoEmpresaCobrancaConta.class.getName());
		
		ComandoEmpresaCobrancaConta comando = (ComandoEmpresaCobrancaConta) colecao.iterator().next();
		comando.setQuantidadeContas(qtdContas);
		comando.setQuantidadeClientes(qtdClientes);
		comando.setValorTotal(valorTotal);

		getControladorUtil().atualizar(comando);
	}

	
	private void inserirComandoLigacaoAguaSituacao(List<Integer> ids, Integer idComando) throws ControladorException {
		for (int i = 0; i < ids.size(); i++) {
			if (!ids.get(i).equals(ConstantesSistema.NUMERO_NAO_INFORMADO)) {
				CmdEmpresaCobrancaContaLigacaoAguaSituacao comandoLigacao = new CmdEmpresaCobrancaContaLigacaoAguaSituacao();
				CmdEmpresaCobrancaContaLigacaoAguaSituacaoPK pk = new CmdEmpresaCobrancaContaLigacaoAguaSituacaoPK();

				pk.setLigacaoAguaSituacaoId(ids.get(i));
				pk.setComandoEmpresaCobrancaContaId(idComando);

				comandoLigacao.setCels_id(pk);
				comandoLigacao.setUltimaAlteracao(new Date());

				getControladorUtil().inserir(comandoLigacao);
			}
		}
	}

	private void inserirComandoImovelPerfil(List<Integer> ids, Integer idComando) throws ControladorException {
		for (int i = 0; i < ids.size(); i++) {
			if (!ids.get(i).equals(ConstantesSistema.NUMERO_NAO_INFORMADO)) {
				ComandoEmpresaCobrancaContaImovelPerfil comandoImovelPerfil = new ComandoEmpresaCobrancaContaImovelPerfil();
				ComandoEmpresaCobrancaContaImovelPerfilPK pk = new ComandoEmpresaCobrancaContaImovelPerfilPK();

				pk.setImovelPerfilId(ids.get(i));
				pk.setComandoEmpresaCobrancaContaId(idComando);

				comandoImovelPerfil.setComp_id(pk);
				comandoImovelPerfil.setUltimaAlteracao(new Date());

				getControladorUtil().inserir(comandoImovelPerfil);
			}
		}
	}

	private void inserirComandoGerenciaRegional(List<Integer> ids, Integer idComando) throws ControladorException {
		for (int i = 0; i < ids.size(); i++) {
			if (!ids.get(i).equals(ConstantesSistema.NUMERO_NAO_INFORMADO)) {
				ComandoEmpresaCobrancaContaGerencia comandoGerencia = new ComandoEmpresaCobrancaContaGerencia();
				ComandoEmpresaCobrancaContaGerenciaPK pk = new ComandoEmpresaCobrancaContaGerenciaPK();

				pk.setGerenciaRegionalId(ids.get(i));
				pk.setComandoEmpresaCobrancaContaId(idComando);

				comandoGerencia.setComp_id(pk);
				comandoGerencia.setUltimaAlteracao(new Date());

				getControladorUtil().inserir(comandoGerencia);
			}
		}
	}

	private void inserirComandoUnidadeNegocio(List<Integer> ids, Integer idComando) throws ControladorException {
		for (int i = 0; i < ids.size(); i++) {
			if (!ids.get(i).equals(ConstantesSistema.NUMERO_NAO_INFORMADO)) {
				ComandoEmpresaCobrancaContaUnidadeNegocio comandoUnidade = new ComandoEmpresaCobrancaContaUnidadeNegocio();
				
				ComandoEmpresaCobrancaContaUnidadeNegocioPK pk = new ComandoEmpresaCobrancaContaUnidadeNegocioPK();
				pk.setUnidadeNegocioId(ids.get(i));
				pk.setComandoEmpresaCobrancaContaId(idComando);

				comandoUnidade.setComp_id(pk);
				comandoUnidade.setUltimaAlteracao(new Date());

				getControladorUtil().inserir(comandoUnidade);
			}
		}
	}
	
	public Object[] pesquisarDadosConsultaComando(Integer idComando, Date dateInicial, Date dateFinal) throws ControladorException {
		Object[] retorno = new Object[26];
		try {
			retorno = (Object[]) repositorio.pesquisarDadosConsultaComando(idComando).iterator().next();
		} catch (ErroRepositorioException e) {
			e.printStackTrace();
			throw new ControladorException("erro.sistema", e);
		}

		return retorno;
	}
	
	@SuppressWarnings({ "unchecked", "null", "unused" })
	public Collection<GerarExtensaoComandoContasCobrancaEmpresaHelper> pesquisarDadosGerarExtensaoComandoContasCobrancaEmpresa(
			Integer idEmpresa, Date comandoInicial, Date comandoFinal, int numeroIndice) throws ControladorException {
		try {
			Collection<GerarExtensaoComandoContasCobrancaEmpresaHelper> colecaoExtensaoHelper = new ArrayList<GerarExtensaoComandoContasCobrancaEmpresaHelper>();

			final int quantidadeRegistros = 10;

			Collection<GerarArquivoTextoContasCobrancaEmpresaHelper> colecaoHelper = null;
//			Collection<GerarArquivoTextoContasCobrancaEmpresaHelper> colecaoHelper = repositorio.pesquisarDadosComando(idEmpresa, comandoInicial, comandoFinal, numeroIndice, quantidadeRegistros);

			for (GerarArquivoTextoContasCobrancaEmpresaHelper helper : colecaoHelper) {
				Collection<ExtensaoComandoContasCobrancaEmpresaHelper> colecaoExtensaoComando = new ArrayList<ExtensaoComandoContasCobrancaEmpresaHelper>();
				GerarExtensaoComandoContasCobrancaEmpresaHelper extensaoHelper = new GerarExtensaoComandoContasCobrancaEmpresaHelper();

				Collection<Object[]> colecaoDadosExtensaoComando = repositorioCobranca.pesquisarDadosGerarExtensaoComandoContasCobrancaEmpresaParaCobranca(helper.getIdComandoEmpresaCobrancaConta());
				for (Object[] dados : colecaoDadosExtensaoComando) {
					ExtensaoComandoContasCobrancaEmpresaHelper helperExtensaoComando = new ExtensaoComandoContasCobrancaEmpresaHelper();
					
					if (dados[0] != null) {
						helperExtensaoComando.setIdComandoEmpresaCobrancaContaExtensao((Integer) dados[0]);
					}
					if (dados[1] != null) {
						helperExtensaoComando.setAnoMesInicialContaComandoEmpresaCobrancaContaExtensao((Integer) dados[1]);
					}

					if (dados[2] != null) {
						helperExtensaoComando.setAnoMesFinalContaComandoEmpresaCobrancaContaExtensao((Integer) dados[2]);
					}
					if (dados[3] != null) {
						helperExtensaoComando.setDataExecucaoComandoEmpresaCobrancaContaExtensao((Date) dados[3]);
					}

					helperExtensaoComando.setQtdeContasParaCobranca(new Integer(0));
					helperExtensaoComando.setValorTotalContasParaCobranca(new BigDecimal(0.0));

					extensaoHelper.setAnoMesInicial(Util.formatarAnoMesParaMesAno(Util.somaUmMesAnoMesReferencia(helperExtensaoComando
							.getAnoMesFinalContaComandoEmpresaCobrancaContaExtensao())));

					colecaoExtensaoComando.add(helperExtensaoComando);
				}

				extensaoHelper.setGerarArquivoTextoContasCobrancaEmpresaHelper(helper);
				extensaoHelper.setColecaoExtensaoComandoContasCobrancaEmpresaHelper(colecaoExtensaoComando);

				SistemaParametro sistemaParametro = getControladorUtil().pesquisarParametrosDoSistema();

				extensaoHelper.setAnoMesFinal(Util.formatarAnoMesParaMesAno(Util.subtrairMesDoAnoMes(sistemaParametro.getAnoMesFaturamento(), 1)));

				if (extensaoHelper.getAnoMesInicial() == null || extensaoHelper.getAnoMesInicial().equals("")) {
					extensaoHelper.setAnoMesInicial(Util.formatarAnoMesParaMesAno(Util.somaUmMesAnoMesReferencia(helper.getReferenciaContaFinal())));
				}
				colecaoExtensaoHelper.add(extensaoHelper);
			}

			return colecaoExtensaoHelper;
		} catch (ErroRepositorioException e) {
			throw new ControladorException("erro.sistema", e);
		}
	}
	
	public List<ComandoEmpresaCobrancaConta> pesquisarDadosComando(Integer idEmpresa, Date comandoInicial, Date comandoFinal, int pagina) throws ControladorException {
		try {
			return repositorio.pesquisarDadosComando(idEmpresa, comandoInicial, comandoFinal, pagina, 10);
		} catch (ErroRepositorioException e) {
			throw new ControladorException("erro.sistema", e);
		}
	}
	
	public Integer inserirComandoEmpresaCobrancaConta(ComandoEmpresaCobrancaConta comando, Usuario usuarioLogado) throws ControladorException {
		RegistradorOperacao registradorOperacao = new RegistradorOperacao(Operacao.OPERACAO_INFORMAR_CONTAS_EM_COBRANCA, 
				new UsuarioAcaoUsuarioHelper(usuarioLogado, UsuarioAcao.USUARIO_ACAO_EFETUOU_OPERACAO));

		OperacaoEfetuada operacaoEfetuada = new OperacaoEfetuada();
		operacaoEfetuada.setOperacao(new Operacao(Operacao.OPERACAO_INFORMAR_CONTAS_EM_COBRANCA));

		comando.setUltimaAlteracao(new Date());
		comando.setOperacaoEfetuada(operacaoEfetuada);
		comando.adicionarUsuario(usuarioLogado, UsuarioAcao.USUARIO_ACAO_EFETUOU_OPERACAO);
		registradorOperacao.registrarOperacao(comando);

		return (Integer) getControladorUtil().inserir(comando);
	}
	
	@SuppressWarnings("rawtypes")
	public Collection obterComandosParaIniciar(Integer[] comandos) throws ControladorException {
		try {
			return (Collection) repositorio.obterComandosParaIniciar(comandos);
		} catch (ErroRepositorioException e) {
			throw new ControladorException("erro.sistema", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private EmpresaCobranca filtrarEmpresaCobranca(ComandoEmpresaCobrancaConta comando) throws ControladorException {
		Filtro filtro = new FiltroEmpresaCobranca();
		filtro.adicionarParametro(new ParametroSimples(FiltroEmpresaCobranca.EMPRESA_ID, comando.getEmpresa().getId()));
		filtro.adicionarParametro(new MaiorQue(FiltroEmpresaCobranca.DATA_FIM_CONTRATO, new Date()));
		filtro.adicionarCaminhoParaCarregamentoEntidade(FiltroEmpresaCobranca.EMPRESA);

		Collection<EmpresaCobranca> colecaoEmpresaCobranca = getControladorUtil().pesquisar(filtro, EmpresaCobranca.class.getName());

		if (colecaoEmpresaCobranca != null && !colecaoEmpresaCobranca.isEmpty()) {
			return (EmpresaCobranca) Util.retonarObjetoDeColecao(colecaoEmpresaCobranca);
		} else {
			return null;
		}
	}
	
	private void retirarCobrancaConta(Integer idImovel) throws ControladorException {
		List<EmpresaCobrancaConta> contas = pesquisarContasEmCobranca(idImovel);
		
		for (EmpresaCobrancaConta conta : contas) {
			conta.setDataRetirada(new Date());
			conta.setUltimaAlteracao(new Date());
			getControladorUtil().atualizar(conta);
		}
	}

	@SuppressWarnings("unchecked")
	private List<EmpresaCobrancaConta> pesquisarContasEmCobranca(Integer idImovel) throws ControladorException {
		Filtro filtro = new FiltroEmpresaCobrancaConta("id");
		filtro.adicionarParametro(new ParametroSimples(FiltroEmpresaCobrancaConta.IMOVEL_ID, idImovel));
		filtro.adicionarParametro(new ParametroNulo(FiltroEmpresaCobrancaConta.DATA_RETIRADA));
		
		try {
			return (List<EmpresaCobrancaConta>) getControladorUtil().pesquisar(filtro, EmpresaCobrancaConta.class.getName());
		} catch (ControladorException e) {
			throw new ControladorException("erro.sistema", e);
		}
	}
	
	/**
	 * [UC1167] Consultar Comandos de Cobran�a por Empresa
	 * 
	 * Pesquisa os dados dos comandos
	 * 
	 * @author: Mariana Victor
	 * @date: 04/05/2011
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<ConsultarComandosContasCobrancaEmpresaHelper> pesquisarConsultarComandosContasCobrancaEmpresa(Integer idEmpresa,
			Date cicloInicial, Date cicloFinal, int pagina) throws ControladorException {

		Collection<ConsultarComandosContasCobrancaEmpresaHelper> colecaoConsultarComandosContasCobrancaEmpresaHelper = null;

		try {

			final int quantidadeRegistros = 10;

			colecaoConsultarComandosContasCobrancaEmpresaHelper = (Collection) repositorio
					.pesquisarDadosConsultarComandosContasCobrancaEmpresaResumido(idEmpresa, cicloInicial, cicloFinal, pagina,
							quantidadeRegistros);

		} catch (ErroRepositorioException e) {
			throw new ControladorException("erro.sistema", e);
		}

		return colecaoConsultarComandosContasCobrancaEmpresaHelper;
	}
	
	public void encerrarComandosCobrancaResultadoPorEmpresa(Integer idFuncionalidadeIniciada, Usuario usuarioLogado,
			Collection<Empresa> empresas, Integer idCobrancaSituacao) throws ControladorException {
		
		int idUnidadeIniciada = getControladorBatch().iniciarUnidadeProcessamentoBatch(idFuncionalidadeIniciada,
					UnidadeProcessamento.EMPRESA, ((Empresa) Util.retonarObjetoDeColecao(empresas)).getId());

		try {
			
			logger.info("***************************************");
			logger.info("ENCERRAR COMANDOS POR EMPRESA");
			logger.info("***************************************");
			
			Iterator<Empresa> itEmpresas = empresas.iterator();
			
			while (itEmpresas.hasNext()) {
				Empresa empresa = itEmpresas.next();
				
				List<ComandoEmpresaCobrancaConta> comandos = obterComandosVencidosPorEmpresa(empresa.getId());
				
				for (ComandoEmpresaCobrancaConta comando : comandos) {
					encerrarComandoCobranca(idFuncionalidadeIniciada, usuarioLogado, idCobrancaSituacao, comando);
				}
			}
			
			getControladorBatch().encerrarUnidadeProcessamentoBatch(null, idUnidadeIniciada, false);
			logger.info("******* FIM **********");
		} catch (Exception ex) {
			ex.printStackTrace();
			getControladorBatch().encerrarUnidadeProcessamentoBatch(ex, idUnidadeIniciada, true);
			throw new EJBException(ex);
		}
	
	}
			
	
	public void encerrarComandosCobrancaResultado(Integer idFuncionalidadeIniciada, Usuario usuarioLogado,
			List<ComandoEmpresaCobrancaConta> comandos, Integer idCobrancaSituacao) throws ControladorException {

		int idUnidadeIniciada = 0;
		
		if (!comandos.isEmpty()) {
			getControladorBatch().iniciarUnidadeProcessamentoBatch(idFuncionalidadeIniciada,
					UnidadeProcessamento.ROTA, ((ComandoEmpresaCobrancaConta) Util.retonarObjetoDeColecao(comandos)).getId());
		}

		try {
			
			for (ComandoEmpresaCobrancaConta comando : comandos) {
				idUnidadeIniciada = getControladorBatch().iniciarUnidadeProcessamentoBatch(idFuncionalidadeIniciada,
						UnidadeProcessamento.COMANDO_EMPRESA_COBRANCA_CONTA, comando.getId());
				
				encerrarComandoCobranca(idFuncionalidadeIniciada, usuarioLogado, idCobrancaSituacao, comando);
			}
			
			getControladorBatch().encerrarUnidadeProcessamentoBatch(null, idUnidadeIniciada, false);
			System.out.println("******* FIM **********");
		} catch (Exception ex) {
			ex.printStackTrace();
			getControladorBatch().encerrarUnidadeProcessamentoBatch(ex, idUnidadeIniciada, true);
			throw new EJBException(ex);
		}
	}

	private void encerrarComandoCobranca(Integer idFuncionalidadeIniciada, Usuario usuarioLogado, Integer idCobrancaSituacao, ComandoEmpresaCobrancaConta comando)
			throws ControladorException, ErroRepositorioException {
		
		boolean flagFimPesquisa = false;
		final int quantidadeImoveis = 1000;
		int quantidadeInicio = 0;
		
		while (!flagFimPesquisa) {
			Collection dadosEmpresaCobConta = this.repositorio.pesquisarImovelOrdemServicoParaEncerrarComando(quantidadeInicio, comando.getId());
			
			if (dadosEmpresaCobConta != null && !dadosEmpresaCobConta.isEmpty()) {
				
				Iterator iterDadosEmpresaCobConta = dadosEmpresaCobConta.iterator();
				
				if (dadosEmpresaCobConta.size() < quantidadeImoveis) {
					flagFimPesquisa = true;
				} else {
					quantidadeInicio = quantidadeInicio + 1000;
				}
				
				System.out.println("***************************************");
				System.out.println("QUANTIDADE: " + dadosEmpresaCobConta.size());
				System.out.println("***************************************");
				
				while (iterDadosEmpresaCobConta.hasNext()) {
					Object[] dados = (Object[]) iterDadosEmpresaCobConta.next();
					
					if (dados != null) {
						
						if (dados[0] != null) {
							Integer idImovel = (Integer) dados[0];
							
							this.getControladorImovel().retirarCobrancaImovelCobrancaPorEmpresa(idImovel, idCobrancaSituacao,  usuarioLogado);
						}
						
						if (dados[1] != null) {
							Integer idOrdemServico = (Integer) dados[1];
							
							Short idMotivoEncerramento = AtendimentoMotivoEncerramento.CANCELADO_POR_DERCURSO_DE_PRAZO;
							
							Date dataAtual = new Date();
							
							// encerrar a ordem de servi�o, com o motivo correspodente a decurso de prazo
							this.getControladorOrdemServico().encerrarOSSemExecucao(idOrdemServico, dataAtual, usuarioLogado,
									idMotivoEncerramento.toString(), dataAtual, null, null, null, null, null, null);
						}
					}
				}
			} else {
				flagFimPesquisa = true;
			}
		}
		
		this.repositorioCobranca.atualizarDataEncerramentoComando(comando.getId());
		//return idUnidadeIniciada;
	}
	
	public List<ComandoEmpresaCobrancaConta> obterComandosVencidosPorEmpresa(Integer idEmpresa) throws ControladorException {
		List<ComandoEmpresaCobrancaConta> retorno = new ArrayList<ComandoEmpresaCobrancaConta>();

		try {
			retorno = repositorio.obterComandosVencidosPorEmpresa(idEmpresa);
		} catch (ErroRepositorioException e) {
			e.printStackTrace();
		}
		return retorno;
	}
}
