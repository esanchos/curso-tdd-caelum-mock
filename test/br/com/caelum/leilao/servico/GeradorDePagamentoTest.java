package br.com.caelum.leilao.servico;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.dominio.Usuario;

@RunWith(MockitoJUnitRunner.class)
public class GeradorDePagamentoTest {
	
	private Usuario jose;
	private Usuario maria;
	
	private Calendar sabado;
	private Calendar domingo;
	private Calendar segunda;
	
	private Leilao leilao;
	private GeradorDePagamento gerador;
	
	@Mock
	private RepositorioDeLeiloes leiloes;
	@Mock
	private RepositorioDePagamentos pagamentos;
	@Mock
	private Avaliador avaliador;
	@Mock
	private Relogio relogio;
	
	@Captor
	ArgumentCaptor<Pagamento> argumento;
	
	@Before
	public void setup() {
		jose = new Usuario("Jose");
		maria = new Usuario("Maria");
		
		sabado = new GregorianCalendar(2017, Calendar.NOVEMBER, 25);
		domingo = new GregorianCalendar(2017, Calendar.NOVEMBER, 26);
		segunda = new GregorianCalendar(2017, Calendar.NOVEMBER, 27);
		
		leilao = new CriadorDeLeilao()
				.para("TV")
				.lance(jose, 100)
				.lance(maria, 300)
				.constroi();
		
		gerador = new GeradorDePagamento(leiloes, pagamentos, avaliador, relogio);
	}

	@Test
	public void deveGerarPagamentoParaUmLeilaoEncerrado() {
		
		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));
		when(avaliador.getMaiorLance()).thenReturn(300.0);
		when(relogio.hoje()).thenReturn(Calendar.getInstance());
		
		gerador.gera();
		
		verify(pagamentos).salva(argumento.capture());
		Pagamento pagamentoGerado = argumento.getValue();
		
		assertThat(pagamentoGerado.getValor(), equalTo(300.0));
		
	}
	
	@Test
	public void devePagarNaSegundaSeTentarNoSabado() {
		
		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));
		when(avaliador.getMaiorLance()).thenReturn(300.0);
		when(relogio.hoje()).thenReturn(sabado);
		
		gerador.gera();
		
		verify(pagamentos).salva(argumento.capture());
		Pagamento pagamentoGerado = argumento.getValue();
		
		assertThat(pagamentoGerado.getData(), equalTo(segunda));	
	}
	
	@Test
	public void devePagarNaSegundaSeTentarNoDomingo() {
		
		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));
		when(avaliador.getMaiorLance()).thenReturn(300.0);
		when(relogio.hoje()).thenReturn(domingo);
		
		gerador.gera();
		
		verify(pagamentos).salva(argumento.capture());
		Pagamento pagamentoGerado = argumento.getValue();
		
		assertThat(pagamentoGerado.getData(), equalTo(segunda));	
	}

}
