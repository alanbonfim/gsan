package gcom.gui.relatorio.cobranca;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import gcom.cobranca.CobrancaAcaoAtividadeComando;
import gcom.cobranca.FiltroCobrancaAcaoAtividadeComando;
import gcom.fachada.Fachada;
import gcom.gui.cobranca.MotivoNaoGeracaoDocumentoActionForm;
import gcom.relatorio.ExibidorProcessamentoTarefaRelatorio;
import gcom.relatorio.cobranca.RelatorioMotivoNaoGeracaoDocumentoCobranca;
import gcom.seguranca.acesso.usuario.Usuario;
import gcom.tarefa.TarefaRelatorio;
import gcom.util.Util;
import gcom.util.filtro.ParametroSimples;


/**
 * @author Anderson Italo
 * @date 30/11/2009
 * Classe respons�vel pelo pr�-precessamento 
 * da chamada do Relatorio Motivo de n�o gera�ao de Documentos de Cobran�a
 * UC9999 Consultar Motivo da n�o Gera��o de Documento de Cobran�a
 */
public class GerarRelatorioMotivoNaoGeracaoDocumentoEventualAction extends ExibidorProcessamentoTarefaRelatorio{
	
	public ActionForward execute(ActionMapping actionMapping,
			ActionForm actionForm, HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {
		
		MotivoNaoGeracaoDocumentoActionForm form = (MotivoNaoGeracaoDocumentoActionForm) actionForm;
		
		Fachada fachada = Fachada.getInstancia();
		CobrancaAcaoAtividadeComando cobrancaAcaoAtividadeComando = null;
		
		/*5.	Caso Contr�rio
		 * 5.1.	O sistema exibe os dados para o Comando
		 * (Chamada do Relatorio Motivo de n�o gera�ao de Documentos de Cobran�a)*/
		if ((httpServletRequest.getParameter("filtroPorComandoSintetico") != null 
				&& httpServletRequest.getParameter("filtroPorComandoSintetico").equals("true"))
				||(httpServletRequest.getParameter("filtroPorComandoAnalitico") != null 
				&& httpServletRequest.getParameter("filtroPorComandoAnalitico").equals("true"))){
			
			FiltroCobrancaAcaoAtividadeComando filtroCobrancaAcaoAtividadeComando = new FiltroCobrancaAcaoAtividadeComando();
			filtroCobrancaAcaoAtividadeComando.adicionarParametro(
				new ParametroSimples(
					FiltroCobrancaAcaoAtividadeComando.ID, 
					new Integer(form.getIdCobrancaAcaoAtividadeComando())));
			
			filtroCobrancaAcaoAtividadeComando.adicionarCaminhoParaCarregamentoEntidade(FiltroCobrancaAcaoAtividadeComando.CLIENTE);
			filtroCobrancaAcaoAtividadeComando.adicionarCaminhoParaCarregamentoEntidade(FiltroCobrancaAcaoAtividadeComando.CLIENTE_RELACAO_TIPO);
			filtroCobrancaAcaoAtividadeComando.adicionarCaminhoParaCarregamentoEntidade(FiltroCobrancaAcaoAtividadeComando.CLIENTE_SUPERIOR);
			filtroCobrancaAcaoAtividadeComando.adicionarCaminhoParaCarregamentoEntidade(FiltroCobrancaAcaoAtividadeComando.COBRANCA_ACAO);
			filtroCobrancaAcaoAtividadeComando.adicionarCaminhoParaCarregamentoEntidade(FiltroCobrancaAcaoAtividadeComando.COBRANCA_ATIVIDADE);
			filtroCobrancaAcaoAtividadeComando.adicionarCaminhoParaCarregamentoEntidade(FiltroCobrancaAcaoAtividadeComando.USUARIO);
			
			
			Collection colecaoCobrancaAcaoAtividadeComando = fachada.pesquisar(filtroCobrancaAcaoAtividadeComando, CobrancaAcaoAtividadeComando.class.getName());
			cobrancaAcaoAtividadeComando = (CobrancaAcaoAtividadeComando)Util.retonarObjetoDeColecao(colecaoCobrancaAcaoAtividadeComando);
			
		}
		
		
		Usuario usuario = (Usuario) httpServletRequest.getSession(false).getAttribute("usuarioLogado");
		int tipoRelatorio = TarefaRelatorio.TIPO_PDF;
		
		RelatorioMotivoNaoGeracaoDocumentoCobranca relatorio = new RelatorioMotivoNaoGeracaoDocumentoCobranca(usuario);
		relatorio.addParametro("cobrancaAcaoAtividadeComando", cobrancaAcaoAtividadeComando);
		relatorio.addParametro("tipoRelatorio", tipoRelatorio);
		relatorio.addParametro("indicadorCronograma", 2);
		relatorio.addParametro("form",form);
		relatorio.addParametro("descricaoTitulo", cobrancaAcaoAtividadeComando.getDescricaoTitulo());
		
		if(httpServletRequest.getParameter("filtroPorComandoSintetico") != null 
				&& httpServletRequest.getParameter("filtroPorComandoSintetico").equals("true")){
			relatorio.addParametro("sintetico", 1);
		}else{
			relatorio.addParametro("sintetico", 2);
		}
		
		return processarExibicaoRelatorio(
				relatorio, tipoRelatorio, httpServletRequest, httpServletResponse, actionMapping);
		
	}

}
