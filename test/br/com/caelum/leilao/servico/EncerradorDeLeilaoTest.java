package br.com.caelum.leilao.servico;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Usuario;

@RunWith(MockitoJUnitRunner.class)
public class EncerradorDeLeilaoTest {
	
	private Usuario jose;
	private Usuario renato;
	private Leilao leilaoNaoEncerrado;
	private Leilao leilao1;
	private Leilao leilao2;
	
	private EncerradorDeLeilao encerrador;
	
	@Mock
	private InterfaceDao daoFalso;
	@Mock
	EncerradorDeLeilao.EnviadorDeEmail carteiroFalso;
	
	@Before
	public void setup() {
		jose = new Usuario("Jose");
		renato = new Usuario("Renato");
		
		leilaoNaoEncerrado = new CriadorDeLeilao()
				.para("TV")
				.naData(Calendar.getInstance())
				.lance(jose, 200)
				.lance(renato, 300)
				.constroi();
		
		leilao1 = new CriadorDeLeilao()
				.para("TV")
				.naData(new GregorianCalendar(2017, Calendar.NOVEMBER, 2))
				.lance(jose, 200)
				.lance(renato, 300)
				.constroi();
		
		leilao2 = new CriadorDeLeilao()
				.para("TV")
				.naData(new GregorianCalendar(2017, Calendar.NOVEMBER, 2))
				.lance(jose, 200)
				.lance(renato, 300)
				.constroi();
		
		encerrador = new EncerradorDeLeilao(daoFalso,carteiroFalso);
	}

	@Test
	public void garanteQueDeveEncerrarLeilaoComMaisDeUmaSemana() {
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));
		
		encerrador.encerra();
		
		assertThat(encerrador.getTotalEncerrados(), equalTo(1));
		
		InOrder inOrder = inOrder(daoFalso, carteiroFalso);
		inOrder.verify(daoFalso).atualiza(leilao1);
		inOrder.verify(carteiroFalso).envia(leilao1);
	}
	
	@Test
	public void naoDeveEncerrarLeilosComMenosDeUmSemana() {
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilaoNaoEncerrado));
						
		encerrador.encerra();
		
		assertThat(encerrador.getTotalEncerrados(), equalTo(0));
		
		InOrder inOrder = inOrder(daoFalso, carteiroFalso);
		inOrder.verify(daoFalso, never()).atualiza(leilaoNaoEncerrado);
		inOrder.verify(carteiroFalso, never()).envia(leilaoNaoEncerrado);
	}
	
	@Test
	public void garanteQueEnviaMailDepoisDeUmaExcecao() {

		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
		
		doThrow(new RuntimeException()).when(carteiroFalso).envia(leilao1);
		
		encerrador.encerra();
		
		verify(carteiroFalso, times(1)).envia(leilao2);
		verify(carteiroFalso, times(1)).notifica(leilao1);
	}
	
	@Test
	public void garanteQueNaoChamaCarteiroSeDaoFalhar() {
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
				
		doThrow(new RuntimeException()).when(daoFalso).atualiza(any(Leilao.class));
		
		encerrador.encerra();
		
		verify(carteiroFalso, never()).envia(any(Leilao.class));
	}
}
