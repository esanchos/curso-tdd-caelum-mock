package br.com.caelum.leilao.servico;

import java.util.List;

import br.com.caelum.leilao.dominio.Leilao;

public interface InterfaceDao {

	void salva(Leilao leilao);
	List<Leilao> encerrados();
	List<Leilao> correntes();
	void atualiza(Leilao leilao);
}
