package br.com.caelum.leilao.servico;

import java.util.List;

import br.com.caelum.leilao.dominio.Leilao;

public interface RepositorioDeLeiloes {
	List<Leilao> encerrados();

}
