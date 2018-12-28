package gcom.atualizacaocadastral;

import gcom.cadastro.cliente.Cliente;
import gcom.cadastro.cliente.IClienteAtualizacaoCadastral;
import gcom.cadastro.endereco.EnderecoTipo;
import gcom.cadastro.endereco.Logradouro;
import gcom.cadastro.endereco.LogradouroBairro;
import gcom.cadastro.endereco.LogradouroCep;
import gcom.cadastro.endereco.LogradouroTipo;

import java.util.Date;

public class ClienteEnderecoRetorno implements IClienteEndereco {

	private Integer id;
	private String numero;
	private String complemento;
	private Date ultimaAlteracao;
	private EnderecoTipo enderecoTipo;
	private Cliente cliente;
	private String nomeMunicipio;
	private String nomeBairro;
	private String descricaoLogradouro;
	private Integer codigoCep;
	private Integer idClienteRetorno;
	private LogradouroTipo logradouroTipo;
	
	public ClienteEnderecoRetorno(){}
	
	public ClienteEnderecoRetorno(Integer idCliente, IClienteAtualizacaoCadastral clienteAtualizacaoCadastral){
		this.numero = clienteAtualizacaoCadastral.getNumeroImovel();
		this.complemento = clienteAtualizacaoCadastral.getComplementoEndereco();
		this.enderecoTipo = new EnderecoTipo(clienteAtualizacaoCadastral.getIdEnderecoTipo());
		this.cliente = new Cliente(idCliente);
		this.nomeMunicipio = clienteAtualizacaoCadastral.getNomeMunicipio();
		this.nomeBairro = clienteAtualizacaoCadastral.getNomeBairro();
		this.descricaoLogradouro = clienteAtualizacaoCadastral.getDescricaoLogradouro();
		this.codigoCep = clienteAtualizacaoCadastral.getCodigoCep();
		this.logradouroTipo = new LogradouroTipo(clienteAtualizacaoCadastral.getIdLogradouroTipo());
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public String getComplemento() {
		return complemento;
	}

	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}

	public Date getUltimaAlteracao() {
		return ultimaAlteracao;
	}

	public void setUltimaAlteracao(Date ultimaAlteracao) {
		this.ultimaAlteracao = ultimaAlteracao;
	}

	public EnderecoTipo getEnderecoTipo() {
		return enderecoTipo;
	}

	public void setEnderecoTipo(EnderecoTipo enderecoTipo) {
		this.enderecoTipo = enderecoTipo;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public String getNomeMunicipio() {
		return nomeMunicipio;
	}

	public void setNomeMunicipio(String nomeMunicipio) {
		this.nomeMunicipio = nomeMunicipio;
	}

	public String getNomeBairro() {
		return nomeBairro;
	}

	public void setNomeBairro(String nomeBairro) {
		this.nomeBairro = nomeBairro;
	}
	
	public String getDescricaoLogradouro() {
		return descricaoLogradouro;
	}

	public void setDescricaoLogradouro(String descricaoLogradouro) {
		this.descricaoLogradouro = descricaoLogradouro;
	}
	
	public Integer getCodigoCep(){
		return this.codigoCep;
	}
	
	public void setCodigoCep(Integer codigoCep){
		this.codigoCep = codigoCep;
	}

	public Integer getIdClienteRetorno() {
		return idClienteRetorno;
	}

	public void setIdClienteRetorno(Integer idClienteRetorno) {
		this.idClienteRetorno = idClienteRetorno;
	}

	public LogradouroTipo getLogradouroTipo() {
		return logradouroTipo;
	}

	public void setLogradouroTipo(LogradouroTipo logradouroTipo) {
		this.logradouroTipo = logradouroTipo;
	}

	public LogradouroCep getLogradouroCep() {
		return null;
	}

	public void setLogradouroCep(LogradouroCep logradouroCep) {
	}

	public LogradouroBairro getLogradouroBairro() {
		return null;
	}

	public void setLogradouroBairro(LogradouroBairro logradouroBairro) {
	}

	public Logradouro getPerimetroInicial() {
		return null;
	}

	public void getPerimetroInicial(Logradouro perimetroInicial) {
	}

	public Logradouro getPerimetroFinal() {
		return null;
	}

	public void setPerimetroFinal(Logradouro perimetroFinal) {
	}
	
	public String getEnderecoCompleto() {
		StringBuilder endereco = new StringBuilder();
		
		if (this.enderecoTipo != null && this.enderecoTipo.getDescricao() != null)
			endereco.append(this.enderecoTipo.getDescricao()).append(": ");
		
		if (this.logradouroTipo != null && this.logradouroTipo.getDescricao() != null)
			endereco.append(this.logradouroTipo.getDescricao()).append(" ");
		
		if (this.descricaoLogradouro != null)
			endereco.append(this.descricaoLogradouro).append(" ");
		
		if (this.complemento != null)
			endereco.append(this.complemento).append(" ");
			
		if (this.numero != null)
			endereco.append("n ").append(this.numero).append(" - ");
		
		if (this.nomeBairro != null)
			endereco.append(this.nomeBairro).append(", ");
		
		if (this.nomeMunicipio != null)
			endereco.append(this.nomeMunicipio).append(" - ");
		
		if (this.codigoCep != null)
			endereco.append(this.codigoCep).append(".");
		
		return endereco.toString();
	}
}
