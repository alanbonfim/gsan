package gcom.util.email;

public class ModeloFaturaPorEmail {

	private static String NOME_CLIENTE = "NOME_CLIENTE";
	
	private static String CABECALHO_PAGINA = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">" + 
			"<html>" + 
			"<head> " + 
			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">" + 
			"</head>"; 
	
	private static String CORPO_EMAIL = "<body>" + 
			"<div>" + 
			"	<div style='font-size: 20px; font-family: Helvetica, sans-serif; color: #293483; repeat-y 0 0; display:table; margin:35px auto 0; padding:0 5px; width:950px	'>" + 
			"		<header>" +
			"         <div>" +
			"			<img src=\"http://www.cosanpa.pa.gov.br/wp-content/uploads/2021/09/cosanpa_header.jpg\" width=\"50%\" height=\"50%\">" +
			"		    <br><br>" +
			"         </div>" + 
			"       </header>" +
			"	<div> " + 
			"		<h3>Ol&aacute;, <b>NOME_CLIENTE</b></h3>" + 
			"		<p>" +		
			"			Segue sua fatura por email.<br>" +
			"			A Cosanpa agradece a sua aten&ccedil;&atilde;o e est&aacute; &agrave; disposi&ccedil;&atilde;o de voc&ecirc;, sempre!" +
			"			Se precisar entrar em contato conosco,<br>" +
			"			acesse o site <b>www.cosanpa.pa.gov.br</b> ou ligue para 0800 70 71 195." +
			"	    </p>" +
			"       <br>" +
			"    </div>" +  
			"	" + 
			"	<div><p><b>\"Esta &eacute; uma mensagem autom&aacute;tica, este email n&atilde;o deve ser respondido.\"</b></p></div>";
	
	private static String RODAPE_PAGINA = "<footer>" + 
			"		<div>" + 
			"			<img src=\"http://www.cosanpa.pa.gov.br/wp-content/uploads/2021/09/cosanpa_footer.jpg\" class=\"img-fluid\" alt=\"COSANPA\" width=\"100%\" height=\"100%\">" + 
			"		</div> " + 
			"		</footer> <br><br>" + 
			"	</div>			" + 
			"</div>" + 
			"</body>" + 
			"</html:html>";
	
	public static String getMensagem(String nomeCliente) {
		StringBuilder texto = new StringBuilder();
		
		texto.append(CABECALHO_PAGINA)
			.append(CORPO_EMAIL)
			.append(RODAPE_PAGINA);
		
		return texto.toString().replace(NOME_CLIENTE, nomeCliente);
	}
}
