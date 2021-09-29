package gcom.gui.cobranca;

import gcom.atendimentopublico.ligacaoagua.LigacaoAguaSituacao;
import gcom.atendimentopublico.ligacaoesgoto.LigacaoEsgotoSituacao;
import gcom.cadastro.cliente.Cliente;
import gcom.cadastro.cliente.ClienteImovel;
import gcom.cadastro.cliente.ClienteRelacaoTipo;
import gcom.cadastro.cliente.FiltroCliente;
import gcom.cadastro.cliente.FiltroClienteImovel;
import gcom.cadastro.imovel.FiltroImovel;
import gcom.cadastro.imovel.Imovel;
import gcom.cadastro.sistemaparametro.FiltroSistemaParametro;
import gcom.cadastro.sistemaparametro.SistemaParametro;
import gcom.cobranca.ResolucaoDiretoria;
import gcom.cobranca.bean.ContaValoresHelper;
import gcom.cobranca.bean.GuiaPagamentoValoresHelper;
import gcom.cobranca.bean.ObterDebitoImovelOuClienteHelper;
import gcom.cobranca.parcelamento.Parcelamento;
import gcom.fachada.Fachada;
import gcom.faturamento.FaturamentoGrupo;
import gcom.faturamento.credito.CreditoARealizar;
import gcom.faturamento.credito.CreditoOrigem;
import gcom.faturamento.credito.CreditoTipo;
import gcom.faturamento.debito.DebitoACobrar;
import gcom.faturamento.debito.DebitoTipo;
import gcom.financeiro.FinanciamentoTipo;
import gcom.gui.ActionServletException;
import gcom.gui.GcomAction;
import gcom.seguranca.acesso.PermissaoEspecial;
import gcom.seguranca.acesso.usuario.Usuario;
import gcom.util.ControladorException;
import gcom.util.Util;
import gcom.util.filtro.ParametroNulo;
import gcom.util.filtro.ParametroSimples;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.jboss.logging.Logger;

public class ExibirEfetuarParcelamentoDebitosProcesso1Action extends GcomAction {

	private static Logger logger = Logger.getLogger(ExibirEfetuarParcelamentoDebitosProcesso1Action.class);
	
	public ActionForward execute(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {

		ActionForward retorno = actionMapping.findForward("processo1");

		Fachada fachada = Fachada.getInstancia();

		HttpSession sessao = httpServletRequest.getSession(false);

		Usuario usuario = (Usuario) sessao.getAttribute("usuarioLogado");

		verificarPermissoes(httpServletRequest, fachada, usuario);

		DynaActionForm form = (DynaActionForm) actionForm;

		// Pega o codigo que o usuario digitou para a pesquisa direta de imovel
		String codigoImovel = (String) httpServletRequest.getParameter("matriculaImovel");
		String codigoImovelAntes = (String) form.get("codigoImovelAntes");

		validarCPF(form);

		pesquisarClienteParcelamento(httpServletRequest, fachada, form);

		String inscricaoImovel = null;
		if (httpServletRequest.getParameter("inscricaoImovel") != null && !httpServletRequest.getParameter("inscricaoImovel").equals("")) {
			inscricaoImovel = (String) httpServletRequest.getParameter("inscricaoImovel");
		}
		
		if (codigoImovel != null && !codigoImovel.trim().equals("") && inscricaoImovel == null) {
			logger.info("Parcelamento do im�vel " + codigoImovel);
			
			// Pesquisa os dados do cliente e do im�vel
			boolean existeImovel = pesquisarImovel(codigoImovel, actionForm, httpServletRequest, sessao, usuario);

			if (existeImovel) {
				Collection<Parcelamento> colecaoParcelamento = fachada.verificarParcelamentoMesImovel(new Integer(codigoImovel));

				if (colecaoParcelamento != null && !colecaoParcelamento.isEmpty()) {
					throw new ActionServletException("atencao.debito.ja.parcelado.mes.faturamento.corrente");
				}

				logger.info("[" + codigoImovel + "] obterDebitosImovelOuCliente()");
				ObterDebitoImovelOuClienteHelper colecaoDebitoImovel = obterDebitosImovelOuCliente(fachada, codigoImovel);

				validarExistenciaDebitosImovel(codigoImovel, colecaoDebitoImovel);

				// [FS0015] Verificar exist�ncia de contas. 
				// Caso n�o existam contas para o im�vel deixar indispon�vel o campo m�s/ano de refer�ncia inicial e m�s/ano de refer�ncia final
				if (isImovelSemContas(colecaoDebitoImovel)) {
					sessao.setAttribute("bloqueiaIntervaloParcelamento", "sim");
					form.set("inicioIntervaloParcelamento", "");
					form.set("fimIntervaloParcelamento", "");
				}

				// Para o c�lculo do D�bito Total Atualizado
				BigDecimal valorTotalContas = new BigDecimal("0.00");
				BigDecimal valorTotalAcrescimoImpontualidade = new BigDecimal("0.00");
				BigDecimal valorTotalRestanteServicosACobrar = new BigDecimal("0.00");
				BigDecimal valorTotalRestanteServicosACobrarCurtoPrazo = new BigDecimal("0.00");
				BigDecimal valorTotalRestanteServicosACobrarLongoPrazo = new BigDecimal("0.00");
				BigDecimal valorTotalRestanteParcelamentosACobrar = new BigDecimal("0.00");
				BigDecimal valorTotalRestanteParcelamentosACobrarCurtoPrazo = new BigDecimal("0.00");
				BigDecimal valorTotalRestanteParcelamentosACobrarLongoPrazo = new BigDecimal("0.00");
				BigDecimal valorTotalGuiasPagamento = new BigDecimal("0.00");
				BigDecimal valorTotalAcrescimoImpontualidadeContas = new BigDecimal("0.00");
				BigDecimal valorTotalAcrescimoImpontualidadeGuias = new BigDecimal("0.00");
				BigDecimal valorCreditoARealizar = new BigDecimal("0.00");
				BigDecimal valorRestanteACobrar = new BigDecimal("0.00");
				BigDecimal valorAtualizacaoMonetaria = new BigDecimal("0.00");
				BigDecimal valorJurosMora = new BigDecimal("0.00");
				BigDecimal valorMulta = new BigDecimal("0.00");

				BigDecimal valorTotalCreditosAnterioresCurtoPrazo = new BigDecimal("0.00");
				BigDecimal valorTotalCreditosAnterioresLongoPrazo = new BigDecimal("0.00");
				BigDecimal valorTotalCreditosAnteriores = new BigDecimal("0.00");
				
				// Dados do D�bito do Im�vel - Contas
				Collection<ContaValoresHelper> colecaoContasImovel = new ArrayList<ContaValoresHelper>();

				ContaValoresHelper contaRemovida = null;

				if (isImovelComContas(colecaoDebitoImovel)) {

					colecaoContasImovel.addAll(colecaoDebitoImovel.getColecaoContasValoresImovel());
					colecaoContasImovel.addAll(colecaoDebitoImovel.getColecaoContasValoresPreteritos());

					Iterator<ContaValoresHelper> contaValores = colecaoContasImovel.iterator();

					while (contaValores.hasNext()) {

						ContaValoresHelper contaValoresHelper = (ContaValoresHelper) contaValores.next();

						if (verificaReferenciaIgualReferencialFaturamento(contaValoresHelper.getConta().getAnoMesReferenciaConta())) {
							contaRemovida = contaValoresHelper;
							continue;
						}

						valorTotalContas.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
						valorTotalContas = valorTotalContas.add(contaValoresHelper.getValorTotalConta());
						if (contaPossuiAtualizacaoMonetaria(contaValoresHelper)) {
							valorAtualizacaoMonetaria.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
							valorAtualizacaoMonetaria = valorAtualizacaoMonetaria.add(contaValoresHelper.getValorAtualizacaoMonetaria().setScale(
									Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO));
						}
						if (contaPossuiJurosMora(contaValoresHelper)) {
							valorJurosMora.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
							valorJurosMora = valorJurosMora.add(contaValoresHelper.getValorJurosMora().setScale(Parcelamento.CASAS_DECIMAIS,
									Parcelamento.TIPO_ARREDONDAMENTO));
						}
						if (contaPossuiMulta(contaValoresHelper)) {
							valorMulta.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
							valorMulta = valorMulta.add(contaValoresHelper.getValorMulta().setScale(Parcelamento.CASAS_DECIMAIS,
									Parcelamento.TIPO_ARREDONDAMENTO));
						}

						logger.info("[" + codigoImovel + "] chamando m�todo contaValoresHelper.getValorTotalContaValoresParcelamento");
						valorTotalAcrescimoImpontualidadeContas.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
						valorTotalAcrescimoImpontualidadeContas = valorTotalAcrescimoImpontualidadeContas.add(contaValoresHelper
								.getValorTotalContaValoresParcelamento());

					}

					if (contaRemovida != null) {
						colecaoContasImovel.remove(contaRemovida);
					}
					
					sessao.setAttribute("colecaoContaValoresImovel", colecaoContasImovel);

					form.set("valorTotalContasImovel", Util.formatarMoedaReal(valorTotalContas));
				} else {
					form.set("valorTotalContasImovel", "0,00");
				}

				// Guias de Pagamento
				Collection<GuiaPagamentoValoresHelper> colecaoGuiaPagamentoValoresImovel = colecaoDebitoImovel.getColecaoGuiasPagamentoValores();
				Collection<GuiaPagamentoValoresHelper> guiasRemovidas = new ArrayList<GuiaPagamentoValoresHelper>();

				if (colecaoGuiaPagamentoValoresImovel != null && !colecaoGuiaPagamentoValoresImovel.isEmpty()) {
					Iterator<GuiaPagamentoValoresHelper> guiaPagamentoValores = colecaoGuiaPagamentoValoresImovel.iterator();
					while (guiaPagamentoValores.hasNext()) {

						GuiaPagamentoValoresHelper guiaPagamentoValoresHelper = (GuiaPagamentoValoresHelper) guiaPagamentoValores.next();

						if (verificaReferenciaIgualReferencialFaturamento(Util.recuperaAnoMesDaData(guiaPagamentoValoresHelper.getGuiaPagamento().getDataEmissao()))) {
							guiasRemovidas.add(guiaPagamentoValoresHelper);
							continue;
						}

						if (guiaPagamentoValoresHelper.getGuiaPagamento() != null && !guiaPagamentoValoresHelper.getGuiaPagamento().equals("")) {
							valorTotalGuiasPagamento.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
							valorTotalGuiasPagamento = valorTotalGuiasPagamento.add(guiaPagamentoValoresHelper.getGuiaPagamento().getValorDebito()
									.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO));
						}
						if (guiaPossuiAtualizacaoMonetaria(guiaPagamentoValoresHelper)) {
							valorAtualizacaoMonetaria.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
							valorAtualizacaoMonetaria = valorAtualizacaoMonetaria.add(guiaPagamentoValoresHelper.getValorAtualizacaoMonetaria().setScale(
									Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO));
						}
						if (guiaPossuiJurosMora(guiaPagamentoValoresHelper)) {
							valorJurosMora.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
							valorJurosMora = valorJurosMora.add(guiaPagamentoValoresHelper.getValorJurosMora().setScale(Parcelamento.CASAS_DECIMAIS,
									Parcelamento.TIPO_ARREDONDAMENTO));
						}
						if (guiaPossuiMulta(guiaPagamentoValoresHelper)) {
							valorMulta.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
							valorMulta = valorMulta.add(guiaPagamentoValoresHelper.getValorMulta());
						}

						if (guiaPossuiAcrescimosImpontualidade(guiaPagamentoValoresHelper)) {
							valorTotalAcrescimoImpontualidadeGuias.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
							valorTotalAcrescimoImpontualidadeGuias = valorTotalAcrescimoImpontualidadeGuias.add(guiaPagamentoValoresHelper
									.getValorAcrescimosImpontualidade());
						}
					}

					if (!guiasRemovidas.isEmpty())
						colecaoGuiaPagamentoValoresImovel.removeAll(guiasRemovidas);

					sessao.setAttribute("colecaoGuiaPagamentoValoresImovel", colecaoGuiaPagamentoValoresImovel);

					form.set("valorGuiasPagamentoImovel", Util.formatarMoedaReal(valorTotalGuiasPagamento));
				} else {
					form.set("valorGuiasPagamentoImovel", "0,00");
				}

				// Acrescimos por Impotualidade
				BigDecimal retornoSoma = new BigDecimal("0.00");
				retornoSoma.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
				retornoSoma = retornoSoma.add(valorTotalAcrescimoImpontualidadeContas);
				retornoSoma = retornoSoma.add(valorTotalAcrescimoImpontualidadeGuias);

				sessao.setAttribute("valorAcrescimosImpontualidadeImovel", retornoSoma);
				form.set("valorAcrescimosImpontualidadeImovel", Util.formatarMoedaReal(retornoSoma));

				// Para o c�lculo do D�bito Total Atualizado
				valorTotalAcrescimoImpontualidade = retornoSoma;

				final int indiceCurtoPrazo = 0;
				final int indiceLongoPrazo = 1;
				
				// Debitos A Cobrar
				Collection<DebitoACobrar> colecaoDebitoACobrar = colecaoDebitoImovel.getColecaoDebitoACobrar();
				Collection<DebitoACobrar> debitosRemovidos = new ArrayList<DebitoACobrar>();

				if (colecaoDebitoACobrar != null && !colecaoDebitoACobrar.isEmpty()) {
					Iterator<DebitoACobrar> debitoACobrarValores = colecaoDebitoACobrar.iterator();

					while (debitoACobrarValores.hasNext()) {
						DebitoACobrar debitoACobrar = (DebitoACobrar) debitoACobrarValores.next();

						if (verificaReferenciaIgualReferencialFaturamento(Util.recuperaAnoMesDaData(debitoACobrar.getGeracaoDebito()))) {
							debitosRemovidos.add(debitoACobrar);
							continue;
						}

						// [FS0022]-Verificar exist�ncia de juros sobre im�vel
						if (debitoACobrarNaoEhJurosParcelamento(debitoACobrar)) {

							valorRestanteACobrar = debitoACobrar.getValorTotalComBonus();

							BigDecimal[] valoresDeCurtoELongoPrazo = fachada.obterValorCurtoELongoPrazoParaParcelamento(
									debitoACobrar.getNumeroPrestacaoDebito(), 
									debitoACobrar.getNumeroPrestacaoCobradasMaisBonus(), 
									debitoACobrar.getValorDebito(), 
									valorRestanteACobrar);
							
							if (isDebitoACobrarServicoNormal(debitoACobrar)) {
								valorTotalRestanteServicosACobrarCurtoPrazo.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
								valorTotalRestanteServicosACobrarCurtoPrazo = valorTotalRestanteServicosACobrarCurtoPrazo.add(valoresDeCurtoELongoPrazo[indiceCurtoPrazo]);
								
								valorTotalRestanteServicosACobrarLongoPrazo.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
								valorTotalRestanteServicosACobrarLongoPrazo = valorTotalRestanteServicosACobrarLongoPrazo.add(valoresDeCurtoELongoPrazo[indiceLongoPrazo]);
							}

							if (isDebitoACobrarParcelamento(debitoACobrar)) {
								valorTotalRestanteParcelamentosACobrarCurtoPrazo.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
								valorTotalRestanteParcelamentosACobrarCurtoPrazo = valorTotalRestanteParcelamentosACobrarCurtoPrazo.add(valoresDeCurtoELongoPrazo[indiceCurtoPrazo]);
								
								valorTotalRestanteParcelamentosACobrarLongoPrazo.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
								valorTotalRestanteParcelamentosACobrarLongoPrazo = valorTotalRestanteParcelamentosACobrarLongoPrazo.add(valoresDeCurtoELongoPrazo[indiceLongoPrazo]);
							}
						}
					}

					if (!debitosRemovidos.isEmpty())
						colecaoDebitoACobrar.removeAll(debitosRemovidos);

					sessao.setAttribute("colecaoDebitoACobrarImovel", colecaoDebitoACobrar);

					// Servi�os
					valorTotalRestanteServicosACobrar.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
					valorTotalRestanteServicosACobrar = valorTotalRestanteServicosACobrarCurtoPrazo.add(valorTotalRestanteServicosACobrarLongoPrazo);
					
					form.set("valorDebitoACobrarServicoLongoPrazo",Util.formatarMoedaReal(valorTotalRestanteServicosACobrarLongoPrazo));
					form.set("valorDebitoACobrarServicoCurtoPrazo",Util.formatarMoedaReal(valorTotalRestanteServicosACobrarCurtoPrazo));
					form.set("valorDebitoACobrarServicoImovel", Util.formatarMoedaReal(valorTotalRestanteServicosACobrar));
					
					valorTotalRestanteParcelamentosACobrar = valorTotalRestanteParcelamentosACobrarCurtoPrazo.add(valorTotalRestanteParcelamentosACobrarLongoPrazo);
					
					form.set("valorDebitoACobrarParcelamentoLongoPrazo",Util.formatarMoedaReal(valorTotalRestanteParcelamentosACobrarLongoPrazo));
					form.set("valorDebitoACobrarParcelamentoCurtoPrazo",Util.formatarMoedaReal(valorTotalRestanteParcelamentosACobrarCurtoPrazo));
					form.set("valorDebitoACobrarParcelamentoImovel",Util.formatarMoedaReal(valorTotalRestanteParcelamentosACobrar));
				} else {
					form.set("valorDebitoACobrarServicoImovel", "0,00");
					form.set("valorDebitoACobrarParcelamentoImovel", "0,00");
				}

				// Cr�dito A Realizar
				Collection<CreditoARealizar> colecaoCreditoARealizar = colecaoDebitoImovel.getColecaoCreditoARealizar();
				Collection<CreditoARealizar> creditosRemovidos = new ArrayList<CreditoARealizar>();
				
				if (colecaoCreditoARealizar != null && !colecaoCreditoARealizar.isEmpty()) {
					
					Iterator<CreditoARealizar> creditoARealizarValores = colecaoCreditoARealizar.iterator();
					
					while (creditoARealizarValores.hasNext()) {
						CreditoARealizar creditoARealizar = (CreditoARealizar) creditoARealizarValores.next();
						if (verificaReferenciaIgualReferencialFaturamento(Util.recuperaAnoMesDaData(creditoARealizar.getGeracaoCredito()))) {
							creditosRemovidos.add(creditoARealizar);
							continue;
						}
						
						if (creditoARealizar.getCreditoTipo().getId().equals(CreditoTipo.CREDITO_BOLSA_AGUA)){
							creditosRemovidos.add(creditoARealizar);
							continue;
						}
						
						if (isCreditoDeParcelamento(creditoARealizar)) {

							BigDecimal[] valores = fachada.obterValorCurtoELongoPrazoParaParcelamento(
									creditoARealizar.getNumeroPrestacaoCredito(), 
									creditoARealizar.getNumeroPrestacaoRealizada(), 
									creditoARealizar.getValorCredito(), creditoARealizar.getValorNaoConcedido());
							
							valorTotalCreditosAnterioresCurtoPrazo.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
							valorTotalCreditosAnterioresCurtoPrazo = valorTotalCreditosAnterioresCurtoPrazo.add(valores[indiceCurtoPrazo]);
							
							valorTotalCreditosAnterioresLongoPrazo.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
							valorTotalCreditosAnterioresLongoPrazo = valorTotalCreditosAnterioresLongoPrazo.add(valores[indiceLongoPrazo]);
							
						} else {
							valorCreditoARealizar.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
							valorCreditoARealizar = valorCreditoARealizar.add(creditoARealizar.getValorTotalComBonus());
						}
					}

					if (!creditosRemovidos.isEmpty())
						colecaoCreditoARealizar.removeAll(creditosRemovidos);
					
					sessao.setAttribute("colecaoCreditoARealizarImovel", colecaoCreditoARealizar);

					valorTotalCreditosAnteriores = valorTotalCreditosAnterioresCurtoPrazo.add(valorTotalCreditosAnterioresLongoPrazo);
					
					valorCreditoARealizar = valorCreditoARealizar.add(valorTotalCreditosAnteriores);
					
					form.set("valorCreditosAnterioresCurtoPrazo",Util.formatarMoedaReal(valorTotalCreditosAnterioresCurtoPrazo));
					form.set("valorCreditosAnterioresLongoPrazo",Util.formatarMoedaReal(valorTotalCreditosAnterioresLongoPrazo));
					form.set("valorTotalCreditosAnteriores",Util.formatarMoedaReal(valorTotalCreditosAnteriores));
					form.set("valorCreditoARealizarImovel", Util.formatarMoedaReal(valorCreditoARealizar));
				} else {
					form.set("valorCreditoARealizarImovel", "0,00");
				}

				// D�bito Total Atualizado
				BigDecimal debitoTotalAtualizado = new BigDecimal("0.00");

				debitoTotalAtualizado.setScale(Parcelamento.CASAS_DECIMAIS, Parcelamento.TIPO_ARREDONDAMENTO);
				debitoTotalAtualizado = debitoTotalAtualizado.add(valorTotalContas);
				debitoTotalAtualizado = debitoTotalAtualizado.add(valorTotalGuiasPagamento);
				debitoTotalAtualizado = debitoTotalAtualizado.add(valorTotalAcrescimoImpontualidade);
				debitoTotalAtualizado = debitoTotalAtualizado.add(valorTotalRestanteServicosACobrar);
				debitoTotalAtualizado = debitoTotalAtualizado.add(valorTotalRestanteParcelamentosACobrar);
				debitoTotalAtualizado = debitoTotalAtualizado.subtract(valorCreditoARealizar);

				sessao.setAttribute("valorDebitoTotalAtualizadoImovel", debitoTotalAtualizado);
				form.set("valorDebitoTotalAtualizadoImovel", Util.formatarMoedaReal(debitoTotalAtualizado));

				validarMudancaImovel(sessao, form, codigoImovelAntes, codigoImovel);

				calcularIntervaloParcelamento(sessao, form, colecaoContasImovel);

				form.set("valorAtualizacaoMonetariaImovel", Util.formatarMoedaReal(valorAtualizacaoMonetaria));
				form.set("valorJurosMoraImovel", Util.formatarMoedaReal(valorJurosMora));
				form.set("valorMultaImovel", Util.formatarMoedaReal(valorMulta));
				form.set("matriculaImovel", codigoImovel);
			}

			// Atualizando o c�digo do im�vel na var�avel hidden do formul�rio
			codigoImovelAntes = codigoImovel;
			form.set("codigoImovelAntes", codigoImovelAntes);
		}

		// Coloca a data Atual na data de Parecelamento
		if (form.get("dataParcelamento").equals("") || form.get("dataParcelamento") == null) {
			SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");
			Calendar dataCorrente = new GregorianCalendar();

			form.set("dataParcelamento", "" + formatoData.format(dataCorrente.getTime()));
		}

		validarPermissaoRD(httpServletRequest, fachada);

		return retorno;
	}

	private boolean isCreditoDeParcelamento(CreditoARealizar creditoARealizar) {
		return ( creditoARealizar.getCreditoOrigem().getId().intValue() == CreditoOrigem.DESCONTOS_CONCEDIDOS_NO_PARCELAMENTO
				|| creditoARealizar.getCreditoOrigem().getId().intValue() == CreditoOrigem.DESCONTOS_CONCEDIDOS_PARCELAMENTO_FAIXA_CONTA)
			&& creditoARealizar.getParcelamento() != null;
	}

	private void validarPermissaoRD(HttpServletRequest httpServletRequest, Fachada fachada) {
		Collection<ResolucaoDiretoria> colecaoResolucaoDiretoria = new ArrayList<ResolucaoDiretoria>();

		if ((Boolean) httpServletRequest.getAttribute("temPermissaoResolucaoDiretoria")) {
			colecaoResolucaoDiretoria = fachada.pesquisarResolucaoDiretoriaMaiorDataVigenciaInicioPermissaoEspecial();
		} else {
			colecaoResolucaoDiretoria = fachada.pesquisarResolucaoDiretoriaMaiorDataVigenciaInicio();
		}

		if (colecaoResolucaoDiretoria != null && colecaoResolucaoDiretoria.isEmpty()) {
			throw new ActionServletException("atencao.resolucao_diretoria.inexistente");
		} else {
			httpServletRequest.setAttribute("colecaoResolucaoDiretoria", colecaoResolucaoDiretoria);
		}
	}

	private void calcularIntervaloParcelamento(HttpSession sessao, DynaActionForm form, Collection<ContaValoresHelper> colecaoContasImovel) {
		// Intervalo do Parcelamento
		if (colecaoContasImovel != null && colecaoContasImovel.size() != 0) {
			Iterator contaValores = colecaoContasImovel.iterator();
			int anoMesReferenciaColecao = 0;
			int menorAnoMesReferencia = 999999;
			int maiorAnoMesReferencia = 0;

			while (contaValores.hasNext()) {
				ContaValoresHelper contaValoresHelper = (ContaValoresHelper) contaValores.next();
				anoMesReferenciaColecao = contaValoresHelper.getConta().getReferencia();
				if (anoMesReferenciaColecao < menorAnoMesReferencia) {
					menorAnoMesReferencia = anoMesReferenciaColecao;
				}
				if (anoMesReferenciaColecao > maiorAnoMesReferencia) {
					maiorAnoMesReferencia = anoMesReferenciaColecao;
				}
			}

			// Quando n�o houver intervalo de parcelamento inicial e final
			if (menorAnoMesReferencia != 0) {
				if ((form.get("inicioIntervaloParcelamento") == null || form.get("inicioIntervaloParcelamento").equals(""))) {
					sessao.setAttribute("bloqueiaIntervaloParcelamento", "nao");
					form.set("inicioIntervaloParcelamento", Util.formatarAnoMesParaMesAno(menorAnoMesReferencia));
				}
			}

			if (maiorAnoMesReferencia != 0) {
				if ((form.get("fimIntervaloParcelamento") == null || form.get(
						"fimIntervaloParcelamento").equals(""))) {
					form.set("fimIntervaloParcelamento", Util.formatarAnoMesParaMesAno(maiorAnoMesReferencia));
					sessao.setAttribute("bloqueiaIntervaloParcelamento", "nao");
				}
			} else {
				form.set("fimIntervaloParcelamento", "00/0000");
			}
		} else {
			// [FS0015] Verificar exist�ncia de contas
			// Caso n�o existam contas para o im�vel deixar indispon�vel o campo m�s/ano
			// de refer�ncia inicial e m�s/ano de refer�ncia final
			form.set("inicioIntervaloParcelamento", "");
			form.set("fimIntervaloParcelamento", "");
			sessao.setAttribute("bloqueiaIntervaloParcelamento", "sim");
		}
	}

	private void validarMudancaImovel(HttpSession sessao, DynaActionForm form, String codigoImovelAntes, String codigoImovel) {
		logger.info("[" + codigoImovel + "] validarMudancaImovel: " + !codigoImovelAntes.equals(codigoImovel));
		if (!codigoImovelAntes.equals(codigoImovel)) {
			// Reinicia a Data do Parcelamento
			SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");
			Calendar dataCorrente = new GregorianCalendar();
			
			form.set("dataParcelamento", "" + formatoData.format(dataCorrente.getTime()));
			
			// Limpa Resolu��o de Diretoria
			form.set("resolucaoDiretoria", "");
			
			form.set("inicioIntervaloParcelamento", "");
			// Limpa fim do Intervalo do Parcelamento
			form.set("fimIntervaloParcelamento", "");
			
			sessao.setAttribute("bloqueiaIntervaloParcelamento", "nao");
			
			// Limpa as perguntas
			form.set("indicadorContasRevisao", "");
			form.set("indicadorGuiasPagamento", "");
			form.set("indicadorAcrescimosImpotualidade", "");
			form.set("indicadorDebitosACobrar", "");
			form.set("indicadorCreditoARealizar", "");
		}
	}

	private boolean debitoACobrarNaoEhJurosParcelamento(DebitoACobrar debitoACobrar) {
		return debitoACobrar.getDebitoTipo().getId() != null && !debitoACobrar.getDebitoTipo().getId().equals(DebitoTipo.JUROS_SOBRE_PARCELAMENTO);
	}

	private boolean isDebitoACobrarServicoNormal(DebitoACobrar debitoACobrar) {
		return debitoACobrar.getFinanciamentoTipo().getId().equals(FinanciamentoTipo.SERVICO_NORMAL);
	}

	private boolean isDebitoACobrarParcelamento(DebitoACobrar debitoACobrar) {
		return debitoACobrar.getFinanciamentoTipo().getId().equals(FinanciamentoTipo.PARCELAMENTO_AGUA)
				|| debitoACobrar.getFinanciamentoTipo().getId().equals(FinanciamentoTipo.PARCELAMENTO_ESGOTO)
				|| debitoACobrar.getFinanciamentoTipo().getId().equals(FinanciamentoTipo.PARCELAMENTO_SERVICO);
	}

	private boolean guiaPossuiAcrescimosImpontualidade(GuiaPagamentoValoresHelper guiaPagamentoValoresHelper) {
		return guiaPagamentoValoresHelper.getValorAcrescimosImpontualidade() != null
				&& !guiaPagamentoValoresHelper.getValorAcrescimosImpontualidade().equals("");
	}

	private boolean guiaPossuiMulta(GuiaPagamentoValoresHelper guiaPagamentoValoresHelper) {
		return guiaPagamentoValoresHelper.getValorMulta() != null && !guiaPagamentoValoresHelper.getValorMulta().equals("");
	}

	private boolean guiaPossuiJurosMora(GuiaPagamentoValoresHelper guiaPagamentoValoresHelper) {
		return guiaPagamentoValoresHelper.getValorJurosMora() != null && !guiaPagamentoValoresHelper.getValorJurosMora().equals("");
	}

	private boolean guiaPossuiAtualizacaoMonetaria(GuiaPagamentoValoresHelper guiaPagamentoValoresHelper) {
		return guiaPagamentoValoresHelper.getValorAtualizacaoMonetaria() != null
				&& !guiaPagamentoValoresHelper.getValorAtualizacaoMonetaria().equals("");
	}

	private boolean contaPossuiMulta(ContaValoresHelper contaValoresHelper) {
		return contaValoresHelper.getValorMulta() != null && !contaValoresHelper.getValorMulta().equals("");
	}

	private boolean contaPossuiJurosMora(ContaValoresHelper contaValoresHelper) {
		return contaValoresHelper.getValorJurosMora() != null && !contaValoresHelper.getValorJurosMora().equals("");
	}

	private boolean contaPossuiAtualizacaoMonetaria(ContaValoresHelper contaValoresHelper) {
		return contaValoresHelper.getValorAtualizacaoMonetaria() != null && !contaValoresHelper.getValorAtualizacaoMonetaria().equals("");
	}

	private boolean isImovelComContas(ObterDebitoImovelOuClienteHelper colecaoDebitoImovel) {
		return (colecaoDebitoImovel.getColecaoContasValoresImovel() != null && !colecaoDebitoImovel.getColecaoContasValoresImovel().isEmpty())
				|| (colecaoDebitoImovel.getColecaoContasValoresPreteritos() != null && !colecaoDebitoImovel.getColecaoContasValoresPreteritos()
						.isEmpty());
	}

	private boolean isImovelSemContas(ObterDebitoImovelOuClienteHelper colecaoDebitoImovel) {
		return (colecaoDebitoImovel.getColecaoContasValoresImovel() == null || colecaoDebitoImovel.getColecaoContasValoresImovel().size() == 0)
				&& (colecaoDebitoImovel.getColecaoContasValoresPreteritos() == null || colecaoDebitoImovel.getColecaoContasValoresPreteritos().size() == 0);
	}

	private void validarExistenciaDebitosImovel(String codigoImovel, ObterDebitoImovelOuClienteHelper colecaoDebitoImovel) {
		if (imovelSemDebitos(colecaoDebitoImovel)) {
			throw new ActionServletException("atencao.imovel.sem.debitos", null, codigoImovel);
		}
	}

	private boolean imovelSemDebitos(ObterDebitoImovelOuClienteHelper colecaoDebitoImovel) {
		return (colecaoDebitoImovel.getColecaoContasValoresImovel() == null || colecaoDebitoImovel.getColecaoContasValoresImovel().size() == 0)
				&& (colecaoDebitoImovel.getColecaoContasValoresPreteritos() == null || colecaoDebitoImovel.getColecaoContasValoresPreteritos().size() == 0)
				&& (colecaoDebitoImovel.getColecaoGuiasPagamentoValores() == null || colecaoDebitoImovel.getColecaoGuiasPagamentoValores().size() == 0)
				&& (colecaoDebitoImovel.getColecaoDebitoACobrar() == null || colecaoDebitoImovel.getColecaoDebitoACobrar().size() == 0);
	}

	private ObterDebitoImovelOuClienteHelper obterDebitosImovelOuCliente(Fachada fachada, String codigoImovel) {
		FaturamentoGrupo grupo = fachada.pesquisarGrupoImovel(Integer.valueOf(codigoImovel));
		
		int referenciaCronograma = fachada.pesquisarMaiorAnoMesReferenciaCronogramaGrupoFaturamentoMensal(grupo.getId());

		// [UC0067] Obter D�bito do Im�vel ou Cliente
		ObterDebitoImovelOuClienteHelper colecaoDebitoImovel = fachada.obterDebitoImovelOuCliente(1, // Indicador d�bito im�vel
				codigoImovel, // Matr�cula do im�vel
				null, // C�digo do cliente
				null, // Tipo de rela��o do cliento com o im�vel
				"000101", // Refer�ncia inicial do d�bito
				String.valueOf(referenciaCronograma), // Refer�ncia final do d�bito
				Util.converteStringParaDate("01/01/0001"), // Inicio Vencimento
				Util.converteStringParaDate("31/12/9999"), // Final Vencimento
				1, // Indicador pagamento
				1, // Indicador conta em revis�o
				1, // Indicador d�bito a cobrar
				1, // Indicador cr�dito a realizar
				1, // Indicador notas promiss�rias
				1, // Indicador guias de pagamento
				1, // Indicador acr�scimos por impontualidade
				null); // Indicador Contas
		return colecaoDebitoImovel;
	}

	private void pesquisarClienteParcelamento(HttpServletRequest httpServletRequest, Fachada fachada, DynaActionForm form) {
		String idClienteParcelamento = (String) form.get("idClienteParcelamento");
		String cpfClienteParcelamento = (String) form.get("cpfClienteParcelamento");
		if (idClienteParcelamento != null && !idClienteParcelamento.equals("")) {
			FiltroCliente filtroCliente = new FiltroCliente();
			filtroCliente.adicionarParametro(new ParametroSimples(FiltroCliente.ID, idClienteParcelamento));

			Collection clientes = fachada.pesquisar(filtroCliente, Cliente.class.getName());

			if (clientes != null && !clientes.isEmpty()) {
				Cliente cliente = (Cliente) ((List) clientes).get(0);

				form.set("nomeClienteParcelamento", cliente.getNome());
				form.set("foneClienteParcelamento", fachada.pesquisarClienteFonePrincipal(cliente.getId()));
				form.set("cpfClienteParcelamento", cliente.getCpfFormatado());
				form.set("cpfClienteParcelamentoDigitado", "");

				if (cliente.getCpf() != null && !cliente.getCpf().equals("")) {

					httpServletRequest.setAttribute("cpfCliente", "true");
				}

			} else {
				httpServletRequest.setAttribute("clienteInexistente", "true");
				form.set("idClienteParcelamento", "");
				form.set("nomeClienteParcelamento", "CLIENTE INEXISTENTE");
				form.set("cpfClienteParcelamento", "");
				form.set("cpfClienteParcelamentoDigitado", "");

			}
		} else {
			form.set("nomeClienteParcelamento", "");

			if (cpfClienteParcelamento != null && !cpfClienteParcelamento.equals("")) {
				form.set("cpfClienteParcelamento", Util.formatarCPFApresentacao(cpfClienteParcelamento));
				Cliente clienteParcelamento = fachada.obterIdENomeCliente(cpfClienteParcelamento);

				if (clienteParcelamento != null) {
					httpServletRequest.setAttribute("cpfCliente", "true");
					form.set("idClienteParcelamento", clienteParcelamento.getId().toString());
					form.set("nomeClienteParcelamento", clienteParcelamento.getNome());
					form.set("foneClienteParcelamento", fachada.pesquisarClienteFonePrincipal(clienteParcelamento.getId()));
				} else {
					form.set("cpfClienteParcelamento", "");
					form.set("cpfClienteParcelamentoDigitado", "");
					httpServletRequest.setAttribute("cpfInexistente", "CPF INEXISTENTE");
				}
			}
		}
	}

	private void validarCPF(DynaActionForm form) {
		String cpf = (String) form.get("cpfClienteParcelamentoDigitado");

		if (cpf != null && !cpf.trim().equalsIgnoreCase("")) {

			boolean igual = true;
			Integer i = 0;

			Integer tam = cpf.length() - 1;

			while (i < tam) {
				if ((cpf.charAt(i)) != (cpf.charAt(i + 1))) {
					igual = false;
					break;
				} else {
					i++;
				}
			}

			if (igual) {
				throw new ActionServletException("atencao.cpf_invalido");
			}
		}
	}

	/**
	 * Verifica as permiss�es especiais para a primeira p�gina de Efetuar
	 * Parcelamento D�bitos
	 * 
	 * @author Rodrigo Silveira
	 * @date 07/11/2006
	 * 
	 * @param httpServletRequest
	 * @param fachada
	 * @param usuario
	 */
	private void verificarPermissoes(HttpServletRequest httpServletRequest, Fachada fachada, Usuario usuario) {

		boolean temPermissaoAcrescimoImpontualidade = fachada.verificarPermissaoEspecial(PermissaoEspecial.PARCELAR_SEM_INCLUIR_ACRESCIMOS_POR_IMPONTUALIDADE,
				usuario);

		boolean temPermissaoDebitoACobrar = fachada.verificarPermissaoEspecial(PermissaoEspecial.PARCELAR_SEM_INCLUIR_DEBITO_A_COBRAR, usuario);

		boolean temPermissaoResolucaoDiretoria = fachada.verificarPermissaoEspecial(PermissaoEspecial.USAR_PLANO_PAI_PARA_ORGAO_PUBLICO, usuario);

		httpServletRequest.setAttribute("temPermissaoAcrescimoImpontualidade", temPermissaoAcrescimoImpontualidade);
		httpServletRequest.setAttribute("temPermissaoDebitoACobrar", temPermissaoDebitoACobrar);
		httpServletRequest.setAttribute("temPermissaoResolucaoDiretoria", temPermissaoResolucaoDiretoria);

	}

	/**
	 * Pesquisa um imovel informado e prepara os dados para exibi��o na tela
	 * 
	 * @author Rodrigo Avellar/Roberta Costa
	 * @created 11/02/2006
	 */
	private boolean pesquisarImovel(String idImovel, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpSession sessao, Usuario usuario) {

		boolean existeImovel = true;

		Fachada fachada = Fachada.getInstancia();

		DynaActionForm efetuarParcelamentoDebitosActionForm = (DynaActionForm) actionForm;

		// Pesquisa o imovel na base
		FiltroImovel filtroImovel = new FiltroImovel();

		filtroImovel.adicionarParametro(new ParametroSimples(FiltroImovel.ID, idImovel));
		filtroImovel.adicionarCaminhoParaCarregamentoEntidade("localidade");
		filtroImovel.adicionarCaminhoParaCarregamentoEntidade("setorComercial");
		filtroImovel.adicionarCaminhoParaCarregamentoEntidade("quadra");
		filtroImovel.adicionarCaminhoParaCarregamentoEntidade("logradouroBairro.bairro.municipio.unidadeFederacao");
		filtroImovel.adicionarCaminhoParaCarregamentoEntidade("logradouroCep.cep");
		filtroImovel.adicionarCaminhoParaCarregamentoEntidade("logradouroCep.logradouro.logradouroTipo");
		filtroImovel.adicionarCaminhoParaCarregamentoEntidade("logradouroCep.logradouro.logradouroTitulo");
		filtroImovel.adicionarCaminhoParaCarregamentoEntidade("enderecoReferencia");
		filtroImovel.adicionarCaminhoParaCarregamentoEntidade("ligacaoAguaSituacao");
		filtroImovel.adicionarCaminhoParaCarregamentoEntidade("ligacaoEsgotoSituacao");
		filtroImovel.adicionarCaminhoParaCarregamentoEntidade("imovelPerfil");
		filtroImovel.adicionarCaminhoParaCarregamentoEntidade("cobrancaSituacao");
		filtroImovel.adicionarCaminhoParaCarregamentoEntidade("areaConstruidaFaixa");
		filtroImovel.adicionarCaminhoParaCarregamentoEntidade("cobrancaSituacaoTipo");

		filtroImovel.adicionarCaminhoParaCarregamentoEntidade(FiltroImovel.CONSUMO_TARIFA);

		Usuario usuarioLogado = (Usuario) sessao.getAttribute(Usuario.USUARIO_LOGADO);
		Collection<Imovel> imovelPesquisado = fachada.pesquisarImovelEfetuarParcelamento(filtroImovel, usuarioLogado);
		
		// Limpando cache de sessao
		sessao.removeAttribute("imovel");

		// Verificar exist�ncioa da matr�cula do im�vel
		if (imovelPesquisado == null || imovelPesquisado.isEmpty()) {
			efetuarParcelamentoDebitosActionForm.set("matriculaImovel", "");
			efetuarParcelamentoDebitosActionForm.set("inscricaoImovel", "MATR�CULA INEXISTENTE");
			httpServletRequest.setAttribute("nomeCampo", "matriculaImovel");
			httpServletRequest.setAttribute("idImovelNaoEncontrado", "exception");
			existeImovel = false;
		} else {
			// Verifica se o Imovel est� Exclu�do
			Imovel imovel = imovelPesquisado.iterator().next();
			if (imovel.getIndicadorExclusao() == Imovel.IMOVEL_EXCLUIDO) {
				efetuarParcelamentoDebitosActionForm.set("matriculaImovel", "");
				efetuarParcelamentoDebitosActionForm.set("inscricaoImovel", "MATR�CULA INEXISTENTE");
				httpServletRequest.setAttribute("nomeCampo", "matriculaImovel");
				httpServletRequest.setAttribute("idImovelNaoEncontrado", "exception");
				existeImovel = false;
			} else {
				httpServletRequest.setAttribute("nomeCampo", "resolucaoDiretoria");
				// Verifica se Usu�rio est� com d�bito em cobran�a
				// administrativa

				// Verifica situa��o liga��o de �gua e esgoto
				if ((imovel.getLigacaoAguaSituacao() != null)
						&& ((imovel.getLigacaoAguaSituacao().getId() == LigacaoAguaSituacao.POTENCIAL) || (imovel.getLigacaoEsgotoSituacao().getId() == LigacaoAguaSituacao.FACTIVEL))
						&& (imovel.getLigacaoEsgotoSituacao().getId() != LigacaoEsgotoSituacao.LIGADO)) {
					throw new ActionServletException("atencao.pesquisa.imovel.inativo");
				}

				// Pega a descri��o da situa��o de �gua
				if (imovel.getLigacaoAguaSituacao() != null) {
					efetuarParcelamentoDebitosActionForm.set("situacaoAgua", imovel.getLigacaoAguaSituacao().getDescricao());
					efetuarParcelamentoDebitosActionForm.set("situacaoAguaId", "" + imovel.getLigacaoAguaSituacao().getId());
				}

				// Pega a descri��o da situa��o de esgoto
				if (imovel.getLigacaoEsgotoSituacao() != null) {
					efetuarParcelamentoDebitosActionForm.set("situacaoEsgoto", imovel.getLigacaoEsgotoSituacao().getDescricao());
					efetuarParcelamentoDebitosActionForm.set("situacaoEsgotoId", "" + imovel.getLigacaoEsgotoSituacao().getId());
				}

				efetuarParcelamentoDebitosActionForm.set("perfilImovel", "" + imovel.getImovelPerfil().getId());

				efetuarParcelamentoDebitosActionForm.set("descricaoPerfilImovel", "" + imovel.getImovelPerfil().getDescricao());

				sessao.setAttribute("imovel", imovel);
				
				logger.info(String.format("PESQUISANDO IMOVEL: %d", imovel.getIdImovel()));

				// Pega o endere�o do Imovel
				String enderecoFormatado = "";
				try {
					enderecoFormatado = fachada.pesquisarEnderecoFormatado(new Integer(idImovel));
					efetuarParcelamentoDebitosActionForm.set("endereco", enderecoFormatado);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (ControladorException e) {
					e.printStackTrace();
				}

				// Pega a inscri��o do Imovel
				efetuarParcelamentoDebitosActionForm.set("inscricaoImovel", imovel.getInscricaoFormatada());

				// Pega as Quantidades de Parcelamentos
				if (imovel.getNumeroParcelamento() != null) {
					efetuarParcelamentoDebitosActionForm.set("numeroParcelamento", "" + imovel.getNumeroParcelamento());
				} else {
					efetuarParcelamentoDebitosActionForm.set("numeroParcelamento", "0");
				}

				// Pega as Quantidades de Reparcelamentos
				if (imovel.getNumeroReparcelamento() != null) {
					efetuarParcelamentoDebitosActionForm.set("numeroReparcelamento", "" + imovel.getNumeroReparcelamento());
				} else {
					efetuarParcelamentoDebitosActionForm.set("numeroReparcelamento", "0");
				}

				// Pega as Quantidades de Reparcelamentos Consecutivos
				if (imovel.getNumeroReparcelamentoConsecutivos() != null) {
					efetuarParcelamentoDebitosActionForm.set("numeroReparcelamentoConsecutivos", "" + imovel.getNumeroReparcelamentoConsecutivos());
				} else {
					efetuarParcelamentoDebitosActionForm.set("numeroReparcelamentoConsecutivos", "0");
				}

				// Filtro para recuperar informa��o do cliente relacionado com o
				// im�vel
				FiltroClienteImovel filtroClienteImovel = new FiltroClienteImovel();

				filtroClienteImovel.adicionarParametro(new ParametroSimples(FiltroClienteImovel.IMOVEL_ID, idImovel));
				filtroClienteImovel.adicionarParametro(new ParametroSimples(FiltroClienteImovel.CLIENTE_RELACAO_TIPO, ClienteRelacaoTipo.USUARIO));
				filtroClienteImovel.adicionarParametro(new ParametroNulo(FiltroClienteImovel.DATA_FIM_RELACAO));
				filtroClienteImovel.adicionarCaminhoParaCarregamentoEntidade("cliente.clienteTipo");

				Collection<ClienteImovel> clientesImovel = fachada.pesquisar(filtroClienteImovel, ClienteImovel.class.getName());

				Cliente cliente = clientesImovel.iterator().next().getCliente();

				// Manda os dados do cliente para a p�gina
				if (cliente != null) {
					sessao.setAttribute("idClienteImovel", cliente.getId());
					efetuarParcelamentoDebitosActionForm.set("nomeCliente", cliente.getNome());
					if (cliente.getCpf() != null && !cliente.getCpf().equals("")) {
						efetuarParcelamentoDebitosActionForm.set("cpfCnpj", cliente.getCpfFormatado());
					} else if (cliente.getCnpj() != null && !cliente.getCnpj().equals("")) {
						efetuarParcelamentoDebitosActionForm.set("cpfCnpj", cliente.getCnpjFormatado());
					} else {
						efetuarParcelamentoDebitosActionForm.set("cpfCnpj", "N�O INFORMADO");
					}
				}
			}
		}

		if (existeImovel) {
			Imovel imovel = imovelPesquisado.iterator().next();

			if (imovel.getAreaConstruida() != null) {

				efetuarParcelamentoDebitosActionForm.set("areaConstruidaImovel", imovel.getAreaConstruida().toString());

			} else if (imovel.getAreaConstruidaFaixa() != null && imovel.getAreaConstruidaFaixa().getMaiorFaixa() != null) {

				efetuarParcelamentoDebitosActionForm.set("areaConstruidaImovel", imovel.getAreaConstruidaFaixa().getMaiorFaixa().toString());
			}
		}

		return existeImovel;
	}
}
