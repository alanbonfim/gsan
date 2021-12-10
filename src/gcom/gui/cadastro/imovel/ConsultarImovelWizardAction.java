package gcom.gui.cadastro.imovel;

import gcom.gui.WizardAction;
import gcom.util.ControladorException;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Wizard do [UC0472] Consultar Im�vel
 * 
 * @author Rafael Santos
 * @since 07/09/2006 
 */
public class ConsultarImovelWizardAction extends WizardAction {

    // M�TODOS DE EXIBI��O
    // =======================================================


    /**
     * Metodo da 1� Aba - Dados Cadastrais do Im�vel
     * 
     * @param actionMapping
     *            Descri��o do par�metro
     * @param actionForm
     *            Descri��o do par�metro
     * @param httpServletRequest
     *            Descri��o do par�metro
     * @param httpServletResponse
     *            Descri��o do par�metro
     * @return Descri��o do retorno
     */
    public ActionForward exibirConsultarImovelDadosCadastraisAction(
            ActionMapping actionMapping, ActionForm actionForm,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
    	
        return new ExibirConsultarImovelDadosCadastraisAction().execute(
                actionMapping, actionForm, httpServletRequest,
                httpServletResponse);
    }

    /**
     * Metodo da 2� Aba - Dados Complementares do Imovel
     * 
     * @param actionMapping
     *            Descri��o do par�metro
     * @param actionForm
     *            Descri��o do par�metro
     * @param httpServletRequest
     *            Descri��o do par�metro
     * @param httpServletResponse
     *            Descri��o do par�metro
     * @return Descri��o do retorno
     */
    public ActionForward exibirConsultarImovelDadosComplementaresAction(
            ActionMapping actionMapping, ActionForm actionForm,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
    	
        return new ExibirConsultarImovelDadosComplementaresAction().execute(
                actionMapping, actionForm, httpServletRequest,
                httpServletResponse);
    }

     
    /**
     * Metodo da 3� Aba - Dados de Liga��es, consumo e medi��o
     * 
     * @param actionMapping
     *            Descri��o do par�metro
     * @param actionForm
     *            Descri��o do par�metro
     * @param httpServletRequest
     *            Descri��o do par�metro
     * @param httpServletResponse
     *            Descri��o do par�metro
     * @return Descri��o do retorno
     */
    public ActionForward exibirConsultarImovelDadosAnaliseMedicaoConsumoAction(
            ActionMapping actionMapping, ActionForm actionForm,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
    	
        return new ExibirConsultarImovelDadosAnaliseMedicaoConsumoAction().execute(
                actionMapping, actionForm, httpServletRequest,
                httpServletResponse);
    }

    
    /**
     * Metodo da 4� Aba - Hist�rico de  Faturamento
     * 
     * @param actionMapping
     *            Descri��o do par�metro
     * @param actionForm
     *            Descri��o do par�metro
     * @param httpServletRequest
     *            Descri��o do par�metro
     * @param httpServletResponse
     *            Descri��o do par�metro
     * @return Descri��o do retorno
     */
    public ActionForward exibirConsultarImovelHistoricoFaturamentoAction(
            ActionMapping actionMapping, ActionForm actionForm,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
    	
        return new ExibirConsultarImovelHistoricoFaturamentoAction().execute(
                actionMapping, actionForm, httpServletRequest,
                httpServletResponse);
    }
     
    /**
     * Metodo da 5� Aba - D�bitos do Im�vel
     * 
     * @param actionMapping
     *            Descri��o do par�metro
     * @param actionForm
     *            Descri��o do par�metro
     * @param httpServletRequest
     *            Descri��o do par�metro
     * @param httpServletResponse
     *            Descri��o do par�metro
     * @return Descri��o do retorno
     * @throws IOException 
     */
    public ActionForward exibirConsultarImovelDebitosAction(
            ActionMapping actionMapping, ActionForm actionForm,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws IOException {
    	
        return new ExibirConsultarImovelDebitosAction().execute(
                actionMapping, actionForm, httpServletRequest,
                httpServletResponse);
    }

     
    /**
     * Metodo da 6� Aba - Pagamento do Im�vel
     * 
     * @param actionMapping
     *            Descri��o do par�metro
     * @param actionForm
     *            Descri��o do par�metro
     * @param httpServletRequest
     *            Descri��o do par�metro
     * @param httpServletResponse
     *            Descri��o do par�metro
     * @return Descri��o do retorno
     */
    public ActionForward exibirConsultarImovelPagamentosAction(
            ActionMapping actionMapping, ActionForm actionForm,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
    	
        return new ExibirConsultarImovelPagamentosAction().execute(
                actionMapping, actionForm, httpServletRequest,
                httpServletResponse);
    }
    
    /**
     * Metodo da 7� Aba - Devolu��es do Im�vel
     * 
     * @param actionMapping
     *            Descri��o do par�metro
     * @param actionForm
     *            Descri��o do par�metro
     * @param httpServletRequest
     *            Descri��o do par�metro
     * @param httpServletResponse
     *            Descri��o do par�metro
     * @return Descri��o do retorno
     */
    public ActionForward exibirConsultarImovelDevolucoesAction(
            ActionMapping actionMapping, ActionForm actionForm,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
    	
        return new ExibirConsultarImovelDevolucoesAction().execute(
                actionMapping, actionForm, httpServletRequest,
                httpServletResponse);
    }
 
    /**
     * Metodo da 8� Aba - Dopcumento de Cobran�a e ordens de Servi�oi de Cobran�a Emitidos para o Im�vel
     * 
     * @param actionMapping
     *            Descri��o do par�metro
     * @param actionForm
     *            Descri��o do par�metro
     * @param httpServletRequest
     *            Descri��o do par�metro
     * @param httpServletResponse
     *            Descri��o do par�metro
     * @return Descri��o do retorno
     */
    public ActionForward exibirConsultarImovelDocumentosCobrancaAction(
            ActionMapping actionMapping, ActionForm actionForm,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
    	
        return new ExibirConsultarImovelDocumentosCobrancaAction().execute(
                actionMapping, actionForm, httpServletRequest,
                httpServletResponse);
    }
     
    /**
     * Metodo da 9� Aba - Parcelamento efetuados para o im�vel
     * 
     * @param actionMapping
     *            Descri��o do par�metro
     * @param actionForm
     *            Descri��o do par�metro
     * @param httpServletRequest
     *            Descri��o do par�metro
     * @param httpServletResponse
     *            Descri��o do par�metro
     * @return Descri��o do retorno
     */
    public ActionForward exibirConsultarImovelParcelamentosDebitosAction(
            ActionMapping actionMapping, ActionForm actionForm,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
    	
        return new ExibirConsultarImovelParcelamentosDebitosAction().execute(
                actionMapping, actionForm, httpServletRequest,
                httpServletResponse);
    }    
     
    /**
     * Metodo da 10� Aba - Registros de atendimento e ordens de servi�os associados ao im�vel
     * 
     * @param actionMapping
     *            Descri��o do par�metro
     * @param actionForm
     *            Descri��o do par�metro
     * @param httpServletRequest
     *            Descri��o do par�metro
     * @param httpServletResponse
     *            Descri��o do par�metro
     * @return Descri��o do retorno
     * @throws ControladorException 
     */
    public ActionForward exibirConsultarImovelRegistroAtendimentoAction(
            ActionMapping actionMapping, ActionForm actionForm,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ControladorException {
    	
        return new ExibirConsultarImovelRegistroAtendimentoAction().execute(
                actionMapping, actionForm, httpServletRequest,
                httpServletResponse);
    }        
    
    
}
