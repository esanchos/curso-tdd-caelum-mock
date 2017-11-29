package br.com.caelum.leilao.servico;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Usuario;

public class EncerradorDeLeilaoTest {
	
	private Usuario jose;
	private Usuario renato;
	
	@Before
	public void setup() {
		jose = new Usuario("Jose");
		renato = new Usuario("Renato");
	}

	@Test
	public void garanteQueDeveEncerrarLeilaoComMaisDeUmaSemana() {
		Leilao leilao = new CriadorDeLeilao()
				.para("TV")
				.naData(new GregorianCalendar(2017, Calendar.NOVEMBER, 20))
				.lance(jose, 200)
				.lance(renato, 300)
				.constroi();
		
		InterfaceDao daoFalso = mock(InterfaceDao.class);
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao));
		
		EncerradorDeLeilao.EnviadorDeEmail carteiroFalso = mock(EncerradorDeLeilao.EnviadorDeEmail.class);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso,carteiroFalso);
		encerrador.encerra();
		
		assertThat(encerrador.getTotalEncerrados(), equalTo(1));
		
		InOrder inOrder = inOrder(daoFalso, carteiroFalso);
		inOrder.verify(daoFalso).atualiza(leilao);
		inOrder.verify(carteiroFalso).envia(leilao);
	}
	
	@Test
	public void naoDeveEncerrarLeilosComMenosDeUmSemana() {
		Leilao leilao = new CriadorDeLeilao()
				.para("TV")
				.naData(new GregorianCalendar(2017, Calendar.NOVEMBER, 27))
				.lance(jose, 200)
				.lance(renato, 300)
				.constroi();
		
		InterfaceDao daoFalso = mock(InterfaceDao.class);
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao));
		
		EncerradorDeLeilao.EnviadorDeEmail carteiroFalso = mock(EncerradorDeLeilao.EnviadorDeEmail.class);
				
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso,carteiroFalso);
		encerrador.encerra();
		
		assertThat(encerrador.getTotalEncerrados(), equalTo(0));
		
		InOrder inOrder = inOrder(daoFalso, carteiroFalso);
		inOrder.verify(daoFalso, never()).atualiza(leilao);
		inOrder.verify(carteiroFalso, never()).envia(leilao);
	}
}
