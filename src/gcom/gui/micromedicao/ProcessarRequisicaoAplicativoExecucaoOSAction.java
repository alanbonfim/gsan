package gcom.gui.micromedicao;

import gcom.atendimentopublico.bean.IntegracaoComercialHelper;
import gcom.atendimentopublico.ligacaoagua.LigacaoAgua;
import gcom.atendimentopublico.ordemservico.OrdemServico;
import gcom.atendimentopublico.ordemservico.ServicoNaoCobrancaMotivo;
import gcom.cadastro.imovel.Imovel;
import gcom.fachada.Fachada;
import gcom.gui.ActionServletException;
import gcom.gui.GcomAction;
import gcom.micromedicao.ArquivoRetornoAplicativoExecucaoOSHelper;
import gcom.micromedicao.SituacaoTransmissaoLeitura;
import gcom.seguranca.acesso.Operacao;
import gcom.seguranca.acesso.usuario.Usuario;
import gcom.util.Util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ProcessarRequisicaoAplicativoExecucaoOSAction extends GcomAction {
	
	// Constantes da classe	
	// Tipos de Respota
	private static final byte RESPOSTA_ERRO = '#';
	private static final byte RESPOSTA_OK = '*';
	private static final short ENCERRAR_ARQUIVO_RETORNO = 7;
	

	// Fachada
	Fachada fachada = Fachada.getInstancia();
	
	/**
	 * M�todo Execute do Action
	 * 
	 * @param actionMapping
	 * @param actionForm
	 * @param httpServletRequest
	 * @param httpServletResponse
	 * @return
	 * @throws IOException 
	 */
	public ActionForward execute(ActionMapping actionMapping,
			ActionForm actionForm, HttpServletRequest request,
			HttpServletResponse response){

		InputStream in = null;
		OutputStream out = null;
		DataInputStream din = null;
		
		// Verificamos qual foi a a��o solicitada pelo dispositivo movel
		try {
			in = request.getInputStream();
			out = response.getOutputStream();
			din = new DataInputStream(in);
			
			// O primeiro byte da requisi��o possue sempre o tipo da requisi��o
			// feita pelo dispositivo m�vel
			int acaoSolicitada = din.readByte();
			long imei = din.readLong();
			
		
			
			ArquivoRetornoAplicativoExecucaoOSHelper arquivoRetornoAplicativoExecucaoOSHelper = null;
						
			OrdemServico ordemServico = null;
			Boolean veioEncerrarOS = Boolean.TRUE;
			
			/*Boolean veioEncerrarOS = null;
			if (httpServletRequest.getAttribute("veioEncerrarOS") != null) {
				veioEncerrarOS = Boolean.TRUE;
			} else {
				veioEncerrarOS = Boolean.FALSE;
			}*/

			ordemServico = fachada.recuperaOSPorId(arquivoRetornoAplicativoExecucaoOSHelper.getIdOrdemServico()); 
			fachada.validarExibirRestabelecimentoLigacaoAgua(ordemServico, veioEncerrarOS);
				
			//String idServicoMotivoNaoCobranca = efetuarReligacaoAguaActionForm.getMotivoNaoCobranca();
			//String valorPercentual = efetuarReligacaoAguaActionForm.getPercentualCobranca();
			//String qtdParcelas = efetuarReligacaoAguaActionForm.getQuantidadeParcelas();

			if (ordemServico != null && arquivoRetornoAplicativoExecucaoOSHelper.getIdTipoDebito() != null) {

				ServicoNaoCobrancaMotivo servicoNaoCobrancaMotivo = null;

				ordemServico.setIndicadorComercialAtualizado(new Short("1"));

				BigDecimal valorAtual = new BigDecimal(0);
				if (arquivoRetornoAplicativoExecucaoOSHelper.getValorDebito() != null) {
					
					String valorDebito = arquivoRetornoAplicativoExecucaoOSHelper.getValorDebito().toString().replace(".", "");

					valorDebito = valorDebito.replace(",", ".");

					valorAtual = new BigDecimal(valorDebito);

					ordemServico.setValorAtual(valorAtual);
				}

				if (arquivoRetornoAplicativoExecucaoOSHelper.getIdServicoMotivoNaoCobranca() != null) {
					servicoNaoCobrancaMotivo = new ServicoNaoCobrancaMotivo();
					servicoNaoCobrancaMotivo.setId(arquivoRetornoAplicativoExecucaoOSHelper.getIdServicoMotivoNaoCobranca());
				}
				ordemServico.setServicoNaoCobrancaMotivo(servicoNaoCobrancaMotivo);

				if (arquivoRetornoAplicativoExecucaoOSHelper.getValorPercentual() != null) {
					ordemServico.setPercentualCobranca(new BigDecimal(arquivoRetornoAplicativoExecucaoOSHelper.getPercentualCobranca()));
				}
			}

			Imovel imovel = ordemServico.getRegistroAtendimento().getImovel();

			LigacaoAgua ligacaoAgua = new LigacaoAgua();

			Date data = Util.converteStringParaDate(arquivoRetornoAplicativoExecucaoOSHelper.getDataReligacao());
			ligacaoAgua.setId(imovel.getId());
			ligacaoAgua.setDataReligacao(data);

			IntegracaoComercialHelper integracaoComercialHelper = new IntegracaoComercialHelper();

			integracaoComercialHelper.setImovel(imovel);
			integracaoComercialHelper.setLigacaoAgua(ligacaoAgua);
			integracaoComercialHelper.setOrdemServico(ordemServico);
			integracaoComercialHelper.setQtdParcelas(arquivoRetornoAplicativoExecucaoOSHelper.getQtdParcelas());
			integracaoComercialHelper.setUsuarioLogado(arquivoRetornoAplicativoExecucaoOSHelper.getUsuario());
			
			
				
			fachada.atualizarOSViaApp(arquivoRetornoAplicativoExecucaoOSHelper.getIdServicoTipo(), integracaoComercialHelper,
					arquivoRetornoAplicativoExecucaoOSHelper.getUsuario());
			
			switch ( acaoSolicitada ) {
			// Baixar o arquivo ?			
			case Operacao.OPERACAO_INSTALACAO_HIDROMETRO_EFETUAR_INT: //49
				// No caso de baixas de arquivo, � sempre passado
				// o imei do celular
				this.baixarArquivo(imei,response,out);
				break;
			case Operacao.OPERACAO_RELIGACAO_AGUA_EFETUAR_INT: //50 
				// No caso de baixas de arquivo, � sempre passado
				// o imei do celular
				this.atualizarArquivo(imei,response,out);
				break;
			case Operacao.OPERACAO_RESTABELECIMENTO_LIGACAO_AGUA_EFETUAR_INT : //51
				this.baixarMensagem(imei,response,out);
				break;				
			case Operacao.OPERACAO_LIGACAO_AGUA_EFETUAR_INT: //257
				this.receberFoto(din, response, out);
				break;
			case Operacao.OPERACAO_SUBSTITUICAO_HIDROMETRO_EFETUAR_INT : //335
				atualizarSituacaoProgramacaoOrdemServico(din, response, out);
				break;
		
			case ENCERRAR_ARQUIVO_RETORNO:
				atualizarSituacaoArquivo(imei,response,out);
			default:
				break;
			}
			
		// Caso aconte�a qualquer problema, retornamos para o
		// dispositivo mobel o caracter de erro no processamento
		// da requisi��o
		} catch (Exception e) {						
			response.setContentLength( 1 );			
			try {
				out.write(RESPOSTA_ERRO);
				out.flush();
				
				System.out.println("ERRO: NO PROCESSAMENTO DO GSAN. RETORNOU " + RESPOSTA_ERRO + " PARA O CELULAR /n/n/n");
				
				e.printStackTrace();
			} catch (IOException e1) {
				System.out.println("ERRO: NO PROCESSAMENTO DO GSAN. N�O ENVIOU RESPOSTA PARA O CELULAR /n/n/n");
				e1.printStackTrace();
				
				throw new ActionServletException( e1.getMessage() );
			}
		}finally{
			if (in != null) {
				try {
					in.close();					
				} catch (IOException e) {
					e.printStackTrace();					
					throw new ActionServletException( e.getMessage() );					
				}
			}
			
			if(out != null){
				try {					
					out.close();
				} catch (IOException e) {
					e.printStackTrace();					
					throw new ActionServletException( e.getMessage() );				
				}
			}
			
		}
		
		return null;		
	}
	
	/**
	 * 
	 * [UCXXX] - 
	 * 
	 * SF/FS - 
	 *
	 * @author Bruno Barros
	 * @date 15/06/2011
	 *
	 * @param imei
	 * @param response
	 * @param out
	 * @throws IOException
	 */
	public void baixarArquivo( long imei, HttpServletResponse response, OutputStream out ) throws IOException{		
		
		try{
			// Arquivo retornado
			byte[] arq = fachada.baixarArquivoTextoAcompanhamentoServico( imei );
			
			// Nome do arquivo
			//String nomeArquivo = (String) retorno[1];
			
			if(arq != null && arq.length > 0){
				System.out.println("Inicio : Baixando arquivo Mobile");

				// Parametro que identifica que o tipo de arquivo da rota est� sendo enviado
				String parametroArquivoBaixarRota = "";				
				
				// 1 do tipo de resposta ok + parametro Arquivo baixar rota + tamanho do arquivo da rota
				response.setContentLength( 1+parametroArquivoBaixarRota.getBytes().length+arq.length );

				out.write( RESPOSTA_OK );
				out.write( parametroArquivoBaixarRota.getBytes() );
				out.write( arq );
				
				out.flush();

				System.out.println("Fim: Baixando arquivo Mobile");	        
			}else{
				System.out.println("N�o existem arquivos liberados para o imei " + imei);

				response.setContentLength( 1 );

				out.write(RESPOSTA_ERRO);
				out.flush();
			}
			
		} catch( Exception e ){
			System.out.println("Erro ao Baixar arquivo Mobile");
			response.setContentLength( 1 );
			out.write(RESPOSTA_ERRO);
			out.flush();

		}
	}

	/**
	 * 
	 * [UCXXX] - 
	 * 
	 * SF/FS - 
	 *
	 * @author Bruno Barros
	 * @date 15/06/2011
	 *
	 * @param imei
	 * @param response
	 * @param out
	 * @throws IOException
	 */
	public void atualizarArquivo( long imei, HttpServletResponse response, OutputStream out ) throws IOException{		
		
		try{
			// Arquivo retornado
			byte[] arq = fachada.atualizarArquivoTextoAcompanhamentoServico( imei );
			
			// Nome do arquivo
			//String nomeArquivo = (String) retorno[1];
			
			if(arq != null && arq.length > 0){
				System.out.println("Inicio : Atualizando Arquivo Mobile");

				// Parametro que identifica que o tipo de arquivo da rota est� sendo enviado
				String parametroArquivoBaixarRota = "";				
				
				// 1 do tipo de resposta ok + parametro Arquivo baixar rota + tamanho do arquivo da rota
				response.setContentLength( 1+parametroArquivoBaixarRota.getBytes().length+arq.length );

				out.write( RESPOSTA_OK );
				out.write( parametroArquivoBaixarRota.getBytes() );
				out.write( arq );
				
				out.flush();

				System.out.println("Fim: Atualizando Arquivo Mobile");	        
			}else{
				System.out.println("N�o existe atualiza��o");
				response.setContentLength( 1 );

				out.write(RESPOSTA_ERRO);
				out.flush();
			}
			
		} catch( Exception e ){
			System.out.println("Erro ao Baixar arquivo Mobile");
			response.setContentLength( 1 );
			out.write(RESPOSTA_ERRO);
			out.flush();

		}
	}	
	
	/**
	 * 
	 * [UCXXX] - 
	 * 
	 * SF/FS - 
	 *
	 * @author Th�lio Ara�jo
	 * @date 08/0/2011
	 *
	 * @param imei
	 * @param response
	 * @param out
	 * @throws IOException
	 */
	public void baixarMensagem( long imei, HttpServletResponse response, OutputStream out ) throws IOException{		
		
		try{
			// Mensagem Retornada
			String mensagemRetorno = fachada.retornaMensagemAcompanhamentoArquivosRoteiroImei( imei );
			
			if(mensagemRetorno != null && !mensagemRetorno.equals(null)){
				System.out.println("Inicio : Baixando Mensagem");

				String mensagem = "13|"+mensagemRetorno;
				
				byte[] bytesMensagem = mensagem.getBytes();
				
				response.setContentLength( 1+bytesMensagem.length );

				out.write( RESPOSTA_OK );
				out.write( bytesMensagem );
				
				out.flush();

				System.out.println("Fim: Baixando Mensagem");	        
			}else{
				System.out.println("N�o h� mensagens para baixar...");

				response.setContentLength( 1 );

				out.write(RESPOSTA_ERRO);
				out.flush();
			}
		} catch( Exception e ){
			System.out.println("Erro ao Baixar Mensagem");
			response.setContentLength( 1 );
			out.write(RESPOSTA_ERRO);
			out.flush();
		}
	}
	
	/**
	 * 
	/**
	 * [UC-1225] Incluir dados acompanhamento servico 
	 * 
	 * M�todo que insere o array de bytes vindo do celular
	 * e o insere no banco
	 *  
	 * @param data
	 * @param response
	 * @param out
	 * 
	 * @param fileLength 
	 * @throws IOException
	 */
	public void receberFoto( DataInputStream data, HttpServletResponse response, OutputStream out ) throws IOException{		
		// Lemos o n�mero da OS para qual essa foto pertence
		int numeroOS = data.readInt();
		
		// Verificamos qual o tipo da foto, se inicio, meio ou fim
		int tipoFoto = data.readInt();
		
		long fileSize = data.readLong();

		byte[] bytesFoto = new byte[(int)fileSize]; 
		int read = 0;
		int numRead = 0;
		while (read < bytesFoto.length && (numRead=data.read(bytesFoto, read, bytesFoto.length-read)) >= 0) {
			read = read + numRead;
		}
		
		// inserimos na base
		Fachada.getInstancia().inserirFotoOrdemServico( numeroOS, tipoFoto, bytesFoto );		
	}
	
	/**
	 * 
	 * [UC-1225] Incluir dados acompanhamento servico 
	 * 
	 * Altera a situa��o de uma ordem de servi�o no GSAN
	 * 
	 * 
	 * @param data
	 * @param response
	 * @param out
	 * @throws IOException
	 */
	public void atualizarSituacaoProgramacaoOrdemServico( 
			DataInputStream data, 
			HttpServletResponse response, 
			OutputStream out ) throws IOException{		
		try{		
			// Lemos o n�mero da OS para qual essa foto pertence
			int numeroOS = data.readInt();
		
			// Verificamos qual o tipo da foto, se inicio, meio ou fim
			short situacao = data.readShort();
		
			Fachada.getInstancia().atualizarSituacaoProgramacaoOrdemServico( numeroOS , situacao);
		} catch( Exception e ){
			System.out.println("Erro ao Baixar Mensagem");
			response.setContentLength( 1 );
			out.write(RESPOSTA_ERRO);
			out.flush();
		}
		
	}
	
	/**
	 * [UC-1225] Incluir dados acompanhamento servico 
	 * 
	 * M�todo que insere o array de bytes vindo do celular
	 * e o insere no banco
	 *  
	 * @param data
	 * @param response
	 * @param out
	 * 
	 * @param fileLength 
	 * @throws IOException
	 */
	public void encerrarOS( DataInputStream data, HttpServletResponse response, OutputStream out ) throws IOException{		
		try{
		
			long fileSize = data.readLong();
	
			byte[] bytesArquivo = new byte[(int)fileSize]; 
			int read = 0;
			int numRead = 0;
			while (read < bytesArquivo.length && (numRead=data.read(bytesArquivo, read, bytesArquivo.length-read)) >= 0) {
				read = read + numRead;
			}
			
			String arquivo = new String(bytesArquivo);
			
			BufferedReader buffer = new BufferedReader(new StringReader(arquivo));
			
			// inserimos na base
			Fachada.getInstancia().retornoAtualizarOrdemServicoAcompanhamentoServico( buffer );
			
			response.setContentLength( 1 );

			out.write( RESPOSTA_OK );

			out.flush();

		}catch( Exception e ){
			System.out.println("Erro ao efetuar opera��o.");
			response.setContentLength( 1 );
			out.write(RESPOSTA_ERRO);
			out.flush();
		}
	}
	
	/**
	 * 
	 * [UCXXX] - 
	 * 
	 * SF/FS - 
	 *
	 * @author Bruno Barros
	 * @date 15/06/2011
	 *
	 * @param imei
	 * @param response
	 * @param out
	 * @throws IOException
	 */
	public void atualizarSituacaoArquivo( long imei , HttpServletResponse response, OutputStream out ) throws IOException{		
		
		try{
			// Arquivo retornado
			fachada.atualizarSituacaoArquivoTextoAcompanhamentoServico( imei,SituacaoTransmissaoLeitura.TRANSMITIDO.shortValue() );
			
			response.setContentLength( 1 );

			out.write( RESPOSTA_OK );

			out.flush();

				
		} catch( Exception e ){
			System.out.println("Erro ao Baixar arquivo Mobile");
			response.setContentLength( 1 );
			out.write(RESPOSTA_ERRO);
			out.flush();

		}
	}
}
