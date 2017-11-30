package br.com.caelum.leilao.servico;

import java.util.Calendar;
import java.util.List;

import br.com.caelum.leilao.dominio.Leilao;

public class EncerradorDeLeilao {

	private int total = 0;
	
	private InterfaceDao dao;
	private final EnviadorDeEmail carteiro;
	
	public interface EnviadorDeEmail {
		void envia(Leilao leilao);
		void notifica(Leilao leilao);
	}
	
	public EncerradorDeLeilao(InterfaceDao dao, EnviadorDeEmail carteiro) {
		this.dao = dao;
		this.carteiro = carteiro;
	}

	public void encerra() {
		//LeilaoDao dao = new LeilaoDao();
		List<Leilao> todosLeiloesCorrentes = dao.correntes();

		for (Leilao leilao : todosLeiloesCorrentes) {
			try {
				if (comecouSemanaPassada(leilao)) {
					leilao.encerra();
					total++;
					dao.atualiza(leilao);
					carteiro.envia(leilao);
				}
			} catch (RuntimeException e) {
				carteiro.notifica(leilao);
			}
		}
	}

	private boolean comecouSemanaPassada(Leilao leilao) {
		return diasEntre(leilao.getData(), Calendar.getInstance()) >= 7;
	}

	private int diasEntre(Calendar inicio, Calendar fim) {
		Calendar data = (Calendar) inicio.clone();
		int diasNoIntervalo = 0;
		while (data.before(fim)) {
			data.add(Calendar.DAY_OF_MONTH, 1);
			diasNoIntervalo++;
		}

		return diasNoIntervalo;
	}

	public int getTotalEncerrados() {
		return total;
	}
}
