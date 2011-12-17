package org.wave.test.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wave.repository.core.Seeker;
import org.wave.repository.enums.ErrorEnum;
import org.wave.repository.enums.FieldEnum;
import org.wave.repository.enums.LikeEnum;
import org.wave.repository.enums.OrderEnum;
import org.wave.repository.exceptions.RepositoryException;
import org.wave.repository.propositions.And;
import org.wave.repository.propositions.Between;
import org.wave.repository.propositions.Equals;
import org.wave.repository.propositions.Greater;
import org.wave.repository.propositions.GreaterEquals;
import org.wave.repository.propositions.In;
import org.wave.repository.propositions.IsNotNull;
import org.wave.repository.propositions.IsNull;
import org.wave.repository.propositions.Lesser;
import org.wave.repository.propositions.LesserEquals;
import org.wave.repository.propositions.Like;
import org.wave.repository.propositions.Not;
import org.wave.repository.propositions.NotEquals;
import org.wave.repository.propositions.NotIn;
import org.wave.repository.propositions.Or;
import org.wave.repository.propositions.Order;
import org.wave.repository.propositions.Proposition;
import org.wave.test.entities.EntidadeExemplo;
import org.wave.test.entities.EntidadeInvalida;
import org.wave.utils.collection.CollectionUtil;
import org.wave.utils.reflection.ReflectionUtil;

public class SeekerTest {
//	TODO Testar nomes de atributos com pontos - busca encadeada.
//	TODO Equals para calendar.
//	TODO Implementar anotacao para equals e hascode.
	private Seeker seeker;

	private EntityManager manager;

	private EntityTransaction transaction;

	private Calendar calendar0;

	private Calendar calendar1;

	private Calendar calendar2;

	@Before
	public void setUp() {
		WeldContainer container = new Weld().initialize();
		this.seeker = container.instance().select(Seeker.class).get();

		this.manager = container.instance().select(EntityManager.class).get();
		this.transaction = this.manager.getTransaction();
		this.transaction.begin();

		this.calendar0 = Calendar.getInstance();
		this.calendar1 = Calendar.getInstance();
		this.calendar2 = Calendar.getInstance();

		Field active = ReflectionUtil.getField(FieldEnum.ACTIVE.getValue(), EntidadeExemplo.class);

		EntidadeExemplo entidade0 = new EntidadeExemplo();
		ReflectionUtil.set(Boolean.TRUE, active, entidade0);
		entidade0.setStringField("EntidadeExemploZero");
		entidade0.setIntegerField(0);
		entidade0.setLongField(0L);
		entidade0.setBigDecimalField(BigDecimal.ZERO);
		entidade0.setBooleanField(Boolean.TRUE);
		entidade0.setCalendarField(this.calendar0);
		this.manager.persist(entidade0);

		this.calendar1.add(Calendar.YEAR, 2);

		EntidadeExemplo entidade1 = new EntidadeExemplo();
		ReflectionUtil.set(Boolean.TRUE, active, entidade1);
		entidade1.setStringField("UmEntidadeExemplo");
		entidade1.setIntegerField(1);
		entidade1.setLongField(1L);
		entidade1.setBigDecimalField(BigDecimal.ONE);
		entidade1.setBooleanField(Boolean.FALSE);
		entidade1.setCalendarField(this.calendar1);
		entidade1.setEntidadeExemplo(entidade0);
		this.manager.persist(entidade1);

		this.calendar2.add(Calendar.YEAR, 4);

		EntidadeExemplo entidade2 = new EntidadeExemplo();
		ReflectionUtil.set(Boolean.TRUE, active, entidade2);
		entidade2.setStringField("ExemploDoisEntidade");
		entidade2.setLongField(2L);
		entidade2.setBigDecimalField(BigDecimal.ZERO);
		entidade2.setCalendarField(this.calendar2);
		entidade2.setEntidadeExemplo(entidade1);
		this.manager.persist(entidade2);

		EntidadeExemplo entidade3 = new EntidadeExemplo();
		ReflectionUtil.set(Boolean.FALSE, active, entidade3);
		this.manager.persist(entidade3);
	}

	@Test(expected = RepositoryException.class)
	public void deveLancarExcecaoQuandoAClasseForNulaException() throws RepositoryException {
		this.seeker.seekAll(null);
	}

	@Test
	public void deveLancarExcecaoQuandoAClasseForNula() {
		try {
			this.seeker.seekAll(null);
		} catch (RepositoryException e) {
			assertEquals(ErrorEnum.NULL_CLASS.getMessage(), e.getMessage());
		}
	}

	@Test(expected = RepositoryException.class)
	public void deveLancarExcecaoQuandoAClasseForInvalidaException() throws RepositoryException {
		this.seeker.seekAll(EntidadeInvalida.class);
	}

	@Test
	public void deveLancarExcecaoQuandoAClasseForInvalida() {
		try {
			this.seeker.seekAll(EntidadeInvalida.class);
		} catch (RepositoryException e) {
			assertEquals(ErrorEnum.ACTIVE_NOT_FOUND.getMessage(), e.getMessage());
		}
	}

	@Test
	public void deveRetornarTodasAsInstanciasAtivas() throws RepositoryException {
		System.out.println("At[e aqui");
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class);

		assertEquals(3, list.size());
	}

	@Test
	public void naoDeveRetornarAInstanciaDeMenorValor() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Lesser("integerField", 0));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarAInstanciaDeMenorValor() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Lesser("integerField", 1));

		assertEquals(1, list.size());
		assertEquals(Integer.valueOf(0), list.get(0).getIntegerField());
	}

	@Test
	public void naoDeveRetornarAInstanciaDeMaiorValor() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Greater("bigDecimalField", BigDecimal.ONE));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarAInstanciaDeMaiorValor() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Greater("bigDecimalField", BigDecimal.ZERO));

		assertEquals(1, list.size());
		assertEquals(BigDecimal.ONE, list.get(0).getBigDecimalField());
	}

	@Test
	public void naoDeveRetornarUmaInstanciaDeMenorOuIgualValor() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new LesserEquals("longField", -1L));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaDeMenorOuIgualValor() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new LesserEquals("longField", 0L));

		assertEquals(1, list.size());
		assertEquals(Long.valueOf(0), list.get(0).getLongField());
	}

	@Test
	public void deveRetornarDuasInstanciasDeMenorOuIgualValor() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new LesserEquals("longField", 1L));

		assertEquals(2, list.size());
		assertEquals(Long.valueOf(0), list.get(0).getLongField());
		assertEquals(Long.valueOf(1), list.get(1).getLongField());
	}

	@Test
	public void naoDeveRetornarUmaInstanciaDeMaiorOuIgualValor() throws RepositoryException {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 5);

		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new GreaterEquals("calendarField", calendar));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaDeMaiorOuIgualValor() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new GreaterEquals("calendarField", this.calendar2));

		assertEquals(1, list.size());
		assertEquals(this.calendar2, list.get(0).getCalendarField());
	}

	@Test
	public void deveRetornarDuasInstanciasDeMaiorOuIgualValor() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new GreaterEquals("calendarField", this.calendar1));

		assertEquals(2, list.size());
		assertEquals(this.calendar1, list.get(0).getCalendarField());
		assertEquals(this.calendar2, list.get(1).getCalendarField());
	}

	@Test
	public void naoDeveRetornarAInstanciaDeIgualValor() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Equals("integerField", 2));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarAInstanciaDeIgualValor() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Equals("integerField", 0));

		assertEquals(1, list.size());
		assertEquals(Integer.valueOf(0), list.get(0).getIntegerField());
	}

	@Test
	public void naoDeveRetornarUmaInstanciaDeValorDiferente() throws RepositoryException {
		List<Proposition> propositions = new ArrayList<Proposition>();
		propositions.add(new NotEquals("integerField", 0));
		propositions.add(new NotEquals("integerField", 1));
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, propositions);

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaDeValorDiferente() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new NotEquals("integerField", 0));

		assertEquals(1, list.size());
		assertEquals(Integer.valueOf(1), list.get(0).getIntegerField());
	}

	@Test
	public void deveRetornarDuasInstanciasDeValorDiferente() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new NotEquals("integerField", 2));

		assertEquals(2, list.size());
		assertEquals(Integer.valueOf(0), list.get(0).getIntegerField());
		assertEquals(Integer.valueOf(1), list.get(1).getIntegerField());
	}

	@Test
	public void naoDeveRetornarUmaInstanciaDeValorNulo() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new IsNull("calendarField"));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaDeValorNulo() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new IsNull("booleanField"));

		assertEquals(1, list.size());
		assertNull(list.get(0).getByteField());
	}

	@Test
	public void deveRetornarTresInstanciasDeValorNulo() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new IsNull("byteField"));

		assertEquals(3, list.size());
		assertNull(list.get(0).getByteField());
		assertNull(list.get(1).getByteField());
		assertNull(list.get(2).getByteField());
	}

	@Test
	public void naoDeveRetornarUmaInstanciaDeValorNaoNulo() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new IsNotNull("byteField"));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarDuasInstanciasDeValorNaoNulo() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new IsNotNull("booleanField"));

		assertEquals(2, list.size());
		assertNotNull(list.get(0).getBooleanField());
		assertNotNull(list.get(1).getBooleanField());
	}

	@Test
	public void deveRetornarTresInstanciasDeValorNaoNulo() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new IsNotNull("calendarField"));

		assertEquals(3, list.size());
		assertNotNull(list.get(0).getCalendarField());
		assertNotNull(list.get(1).getCalendarField());
		assertNotNull(list.get(2).getCalendarField());
	}

	@Test
	public void naoDeveRetornarInstanciasEntreDoisCalendarios() throws RepositoryException {
		Calendar c1 = Calendar.getInstance();
		c1.add(Calendar.YEAR, -2);

		Calendar c2 = Calendar.getInstance();
		c2.add(Calendar.YEAR, -1);

		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Between("calendarField", c1, c2));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaEntreDoisCalendarios() throws RepositoryException {
		Calendar c1 = Calendar.getInstance();
		c1.add(Calendar.YEAR, -1);

		Calendar c2 = Calendar.getInstance();
		c2.add(Calendar.YEAR, 1);

		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Between("calendarField", c1, c2));

		assertEquals(1, list.size());
		assertEquals(this.calendar0.get(Calendar.YEAR), list.get(0).getCalendarField().get(Calendar.YEAR));
	}

	@Test
	public void deveRetornarDuasInstanciasEntreDoisCalendarios() throws RepositoryException {
		Calendar c1 = Calendar.getInstance();

		Calendar c2 = Calendar.getInstance();
		c2.add(Calendar.YEAR, 2);

		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Between("calendarField", c1, c2));

		assertEquals(2, list.size());
		assertEquals(this.calendar0.get(Calendar.YEAR), list.get(0).getCalendarField().get(Calendar.YEAR));
		assertEquals(this.calendar1.get(Calendar.YEAR), list.get(1).getCalendarField().get(Calendar.YEAR));
	}

	@Test
	public void deveRetornarTresInstanciasEntreDoisCalendarios() throws RepositoryException {
		Calendar c1 = Calendar.getInstance();

		Calendar c2 = Calendar.getInstance();
		c2.add(Calendar.YEAR, 4);

		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Between("calendarField", c1, c2));

		assertEquals(3, list.size());
		assertEquals(this.calendar0.get(Calendar.YEAR), list.get(0).getCalendarField().get(Calendar.YEAR));
		assertEquals(this.calendar1.get(Calendar.YEAR), list.get(1).getCalendarField().get(Calendar.YEAR));
		assertEquals(this.calendar2.get(Calendar.YEAR), list.get(2).getCalendarField().get(Calendar.YEAR));
	}

	@Test
	public void naoDeveRetornarInstanciasEntreDoisValores() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Between("longField", Long.valueOf(-2), Long.valueOf(-1)));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaEntreDoisValores() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Between("longField", Long.valueOf(-1), Long.valueOf(0)));

		assertEquals(1, list.size());
		assertEquals(Long.valueOf(0), list.get(0).getLongField());
	}

	@Test
	public void deveRetornarDuasInstanciasEntreDoisValores() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Between("longField", Long.valueOf(0), Long.valueOf(1)));

		assertEquals(2, list.size());
		assertEquals(Long.valueOf(0), list.get(0).getLongField());
		assertEquals(Long.valueOf(1), list.get(1).getLongField());
	}

	@Test
	public void deveRetornarTresInstanciasEntreDoisValores() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Between("longField", Long.valueOf(0), Long.valueOf(3)));

		assertEquals(3, list.size());
		assertEquals(Long.valueOf(0), list.get(0).getLongField());
		assertEquals(Long.valueOf(1), list.get(1).getLongField());
		assertEquals(Long.valueOf(2), list.get(2).getLongField());
	}

	@Test
	public void naoDeveRetornarInstanciasComPrefixo() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Like("stringField", "Classe", LikeEnum.START));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaComPrefixo() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Like("stringField", "Entidade", LikeEnum.START));

		assertEquals(1, list.size());
		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
	}

	@Test
	public void naoDeveRetornarInstanciasComSufixo() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Like("stringField", "Interface", LikeEnum.END));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaComSufixo() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Like("stringField", "Exemplo", LikeEnum.END));

		assertEquals(1, list.size());
		assertEquals("UmEntidadeExemplo", list.get(0).getStringField());
	}

	@Test
	public void naoDeveRetornarInstanciasComRadical() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Like("stringField", "Enum"));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaComRadical() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Like("stringField", "Dois"));

		assertEquals(1, list.size());
		assertEquals("ExemploDoisEntidade", list.get(0).getStringField());
	}

	@Test
	public void deveRetornarTresInstanciaComRadical() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Like("stringField", "Entidade"));

		assertEquals(3, list.size());
		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
		assertEquals("UmEntidadeExemplo", list.get(1).getStringField());
		assertEquals("ExemploDoisEntidade", list.get(2).getStringField());
	}

	@Test
	public void deveRetornarUmaInstanciaComPrefixoMaiusculo() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Like("stringField", "ENTIDADE", LikeEnum.START));

		assertEquals(1, list.size());
		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
	}

	@Test
	public void deveRetornarUmaInstanciaComSufixoMinusculo() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Like("stringField", "exemplo", LikeEnum.END));

		assertEquals(1, list.size());
		assertEquals("UmEntidadeExemplo", list.get(0).getStringField());
	}

	@Test
	public void deveRetornarUmaInstanciaComRadicalMaiusculoEMinusculo() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Like("stringField", "DoIs", LikeEnum.ANY_WHERE));

		assertEquals(1, list.size());
		assertEquals("ExemploDoisEntidade", list.get(0).getStringField());
	}

	@Test
	public void naoDeveRetornarInstanciasForaDeUmalist() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new In("stringField", CollectionUtil.convert("Classe", "Interface", "Enum")));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaPresenteEmUmalist() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new In("stringField", CollectionUtil.convert("Classe", "EntidadeExemploZero", "Enum")));

		assertEquals(1, list.size());
		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
	}

	@Test
	public void naoDeveRetornarInstanciasForaDeUmArray() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new In("stringField", "Classe", "Interface", "Enum"));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaPresenteEmUmArray() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new In("stringField", "Classe", "Interface", "UmEntidadeExemplo"));

		assertEquals(1, list.size());
		assertEquals("UmEntidadeExemplo", list.get(0).getStringField());
	}

	@Test
	public void naoDeveRetornarInstanciasPresentesEmUmalist() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new NotIn("stringField", CollectionUtil.convert("EntidadeExemploZero", "UmEntidadeExemplo", "ExemploDoisEntidade")));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaForaDeUmalist() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new NotIn("stringField", CollectionUtil.convert("EntidadeExemploZero", "Interface", "ExemploDoisEntidade")));

		assertEquals(1, list.size());
		assertEquals("UmEntidadeExemplo", list.get(0).getStringField());
	}

	@Test
	public void naoDeveRetornarInstanciasPresentesEmUmArray() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new NotIn("stringField", "EntidadeExemploZero", "UmEntidadeExemplo", "ExemploDoisEntidade"));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaForaDeUmArray() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new NotIn("stringField", "EntidadeExemploZero", "UmEntidadeExemplo", "Enum"));

		assertEquals(1, list.size());
		assertEquals("ExemploDoisEntidade", list.get(0).getStringField());
	}

	@Test
	public void naoDeveRetornarUmaInstanciaComAnd() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new And(new NotEquals("integerField", 0), new NotEquals("integerField", 1)));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaComAnd() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new And(new Equals("integerField", 0), new Equals("longField", 0L)));

		assertEquals(1, list.size());
		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
	}

	@Test
	public void naoDeveRetornarUmaInstanciaComOr() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Or(new Equals("integerField", -1), new Equals("integerField", 2)));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaComOr() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Or(new Equals("integerField", 0), new Equals("integerField", 2)));

		assertEquals(1, list.size());
		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
	}

	@Test
	public void deveRetornarDuasInstanciasComOr() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Or(new Equals("integerField", 0), new Equals("integerField", 1)));

		assertEquals(2, list.size());
		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
		assertEquals("UmEntidadeExemplo", list.get(1).getStringField());
	}

	@Test
	public void naoDeveRetornarUmaInstanciaComNot() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Not(new Like("stringField", "Entidade")));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaComNot() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Not(new IsNotNull("booleanField")));

		assertEquals(1, list.size());
		assertEquals("ExemploDoisEntidade", list.get(0).getStringField());
	}

	@Test
	public void naoDeveRetornarUmaInstanciaComAndSequencial() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new NotEquals("integerField", 0), new NotEquals("longField", 1L), new IsNotNull("bigDecimalField"));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaComAndSequencial() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Equals("integerField", 0), new NotEquals("longField", 1L), new IsNotNull("bigDecimalField"));

		assertEquals(1, list.size());
		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
	}

	@Test
	public void naoDeveRetornarUmaInstanciaComOrEAndEncadeados() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Or(new Equals("integerField", 0), new Equals("integerField", 1)), new IsNotNull("byteField"));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaComOrEAndEncadeados() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Or(new Equals("integerField", 0), new Equals("integerField", 1)), new NotEquals("booleanField", Boolean.FALSE));

		assertEquals(1, list.size());
		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
	}

	@Test
	public void naoDeveRetornarUmaInstanciaComAndEOrEncadeados() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new IsNotNull("byteField"), new Or(new Equals("integerField", 0), new Equals("integerField", 1)));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaComAndEOrEncadeados() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new NotEquals("booleanField", Boolean.FALSE), new Or(new Equals("integerField", 0), new Equals("integerField", 1)));

		assertEquals(1, list.size());
		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
	}

	@Test
	public void naoDeveRetornarUmaInstanciaComOrSequencial() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Or(new Equals("integerField", -1), new Equals("integerField", 2), new Equals("integerField", 3)));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaComOrSequencial() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Or(new Equals("integerField", 0), new Equals("integerField", 2), new Equals("integerField", 3)));

		assertEquals(1, list.size());
		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
	}

	@Test
	public void deveRetornarDuasInstanciasComOrSequencial() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Or(new Equals("integerField", 0), new Equals("integerField", 1), new Equals("integerField", 2)));

		assertEquals(2, list.size());
		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
		assertEquals("UmEntidadeExemplo", list.get(1).getStringField());
	}

	@Test
	public void deveRetornarTresInstanciasComOrSequencial() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Or(new Equals("integerField", 0), new Equals("integerField", 1), new IsNull("integerField")));

		assertEquals(3, list.size());
		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
		assertEquals("UmEntidadeExemplo", list.get(1).getStringField());
		assertEquals("ExemploDoisEntidade", list.get(2).getStringField());
	}

	@Test
	public void naoDeveRetornarUmaInstanciaComAndEncadeadoComOrSequencial() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Or(new And(new NotEquals("integerField", 0), new NotEquals("longField", 1L)), new IsNotNull("byteField")));

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaComAndEncadeadoComOrSequencial() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Or(new And(new NotEquals("integerField", 0), new NotEquals("longField", 1L)), new Equals("integerField", 0)));

		assertEquals(1, list.size());
		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
	}

	@Test
	public void naoDeveRetornarUmaInstanciaAtiva() throws RepositoryException {
		EntidadeExemplo instance = this.seeker.seekOne(EntidadeExemplo.class, new Equals("stringField", "Classe"));

		assertNull(instance);
	}

	@Test
	public void deveRetornarUmaInstanciaAtiva() throws RepositoryException {
		EntidadeExemplo instance = this.seeker.seekOne(EntidadeExemplo.class, new Equals("stringField", "UmEntidadeExemplo"));

		assertNotNull(instance);
		assertEquals("UmEntidadeExemplo", instance.getStringField());
	}

	@Test(expected = RepositoryException.class)
	public void deveLancarExcecaoQuandoRetornarMaisDeUmaInstanciaAtivaException() throws RepositoryException {
		this.seeker.seekOne(EntidadeExemplo.class);
	}

	@Test
	public void deveLancarExcecaoQuandoRetornarMaisDeUmaInstanciaAtiva() {
		try {
			this.seeker.seekOne(EntidadeExemplo.class);
		} catch (RepositoryException e) {
			assertEquals(ErrorEnum.MORE_THAN_ONE_INSTANCE.getMessage(), e.getMessage());
		}
	}

	@Test
	public void deveRetornarQuantidadeZero() throws RepositoryException {
		Long amount = this.seeker.count(EntidadeExemplo.class, new Equals("stringField", "Classe"));

		assertNotNull(amount);
		assertEquals(Long.valueOf(0), amount);
	}

	@Test
	public void deveRetornarQuantidadeUm() throws RepositoryException {
		Long amount = this.seeker.count(EntidadeExemplo.class, new Equals("stringField", "UmEntidadeExemplo"));

		assertNotNull(amount);
		assertEquals(Long.valueOf(1), amount);
	}

	@Test
	public void deveRetornarQuantidadeTres() throws RepositoryException {
		Long amount = this.seeker.count(EntidadeExemplo.class);

		assertNotNull(amount);
		assertEquals(Long.valueOf(3), amount);
	}

	@Test
	public void deveOrdenarAsInstanciasDeFormaAscendente() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Order("stringField"));

		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
		assertEquals("ExemploDoisEntidade", list.get(1).getStringField());
		assertEquals("UmEntidadeExemplo", list.get(2).getStringField());
	}

	@Test
	public void deveOrdenarAsInstanciasComAtributoNaoNuloDeFormaAscendente() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new IsNotNull("stringField"), new Order("stringField", OrderEnum.ASC));

		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
		assertEquals("ExemploDoisEntidade", list.get(1).getStringField());
		assertEquals("UmEntidadeExemplo", list.get(2).getStringField());
	}

	@Test
	public void deveOrdenarAsInstanciasComAtributoNuloDeFormaAscendente() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Order("stringField", OrderEnum.ASC), new IsNull("byteField"));

		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
		assertEquals("ExemploDoisEntidade", list.get(1).getStringField());
		assertEquals("UmEntidadeExemplo", list.get(2).getStringField());
	}

	@Test
	public void deveOrdenarAsInstanciasPorValoresDeFormaAscendente() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Order("bigDecimalField"), new Order("longField"));

		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
		assertEquals("ExemploDoisEntidade", list.get(1).getStringField());
		assertEquals("UmEntidadeExemplo", list.get(2).getStringField());
	}

	@Test
	public void deveOrdenarAsInstanciasDeFormaDescendente() throws RepositoryException {
		List<EntidadeExemplo> list = this.seeker.seekAll(EntidadeExemplo.class, new Order("stringField", OrderEnum.DESC));

		assertEquals("UmEntidadeExemplo", list.get(0).getStringField());
		assertEquals("ExemploDoisEntidade", list.get(1).getStringField());
		assertEquals("EntidadeExemploZero", list.get(2).getStringField());
	}

	@Test(expected = RepositoryException.class)
	public void deveLancarExcecaoQuandoHouverUmaExcecaoJPANoSeekAllException() throws RepositoryException {
		this.seeker.seekAll(EntidadeExemplo.class, new Equals("stringAttribute", "UmEntidadeExemplo"));
	}

	@Test
	public void deveLancarExcecaoQuandoHouverUmaExcecaoJPANoSeekAll() {
		try {
			this.seeker.seekAll(EntidadeExemplo.class, new Equals("stringAttribute", "UmEntidadeExemplo"));
		} catch (RepositoryException e) {
			assertTrue(e.getMessage().contains(ErrorEnum.UNEXPECTED_EXCEPTION.getMessage("")));
		}
	}

	@Test(expected = RepositoryException.class)
	public void deveLancarExcecaoQuandoHouverUmaExcecaoJPANoSeekOneException() throws RepositoryException {
		this.seeker.seekOne(EntidadeExemplo.class, new Equals("stringAttribute", "UmEntidadeExemplo"));
	}

	@Test
	public void deveLancarExcecaoQuandoHouverUmaExcecaoJPANoSeekOne() {
		try {
			this.seeker.seekOne(EntidadeExemplo.class, new Equals("stringAttribute", "UmEntidadeExemplo"));
		} catch (RepositoryException e) {
			assertTrue(e.getMessage().contains(ErrorEnum.UNEXPECTED_EXCEPTION.getMessage("")));
		}
	}

	@Test(expected = RepositoryException.class)
	public void deveLancarExcecaoQuandoHouverUmaExcecaoJPANoCountException() throws RepositoryException {
		this.seeker.count(EntidadeExemplo.class, new Equals("stringAttribute", "UmEntidadeExemplo"));
	}

	@Test
	public void deveLancarExcecaoQuandoHouverUmaExcecaoJPANoCount() {
		try {
			this.seeker.count(EntidadeExemplo.class, new Equals("stringAttribute", "UmEntidadeExemplo"));
		} catch (RepositoryException e) {
			assertTrue(e.getMessage().contains(ErrorEnum.UNEXPECTED_EXCEPTION.getMessage("")));
		}
	}

	@Test(expected = RepositoryException.class)
	public void deveLancarExcecaoQuandoAInstanciaForNulaException() throws RepositoryException {
		this.seeker.seekByExample(null);
	}

	@Test
	public void deveLancarExcecaoQuandoAInstanciaForNula() {
		try {
			this.seeker.seekByExample(null);
		} catch (RepositoryException e) {
			assertEquals(ErrorEnum.NULL_INSTANCE.getMessage(), e.getMessage());
		}
	}

	@Test
	public void deveRetornarUmaInstanciaAPartirDeUmExemplo() throws RepositoryException {
		EntidadeExemplo instance = new EntidadeExemplo();

		Field id = ReflectionUtil.getField(FieldEnum.ID.getValue(), EntidadeExemplo.class);
		ReflectionUtil.set(Long.valueOf(100), id, instance);

		Field version = ReflectionUtil.getField(FieldEnum.VERSION.getValue(), EntidadeExemplo.class);
		ReflectionUtil.set(Integer.valueOf(100), version, instance);

		Field active = ReflectionUtil.getField(FieldEnum.ACTIVE.getValue(), EntidadeExemplo.class);
		ReflectionUtil.set(Boolean.FALSE, active, instance);

		instance.setLongField(Long.valueOf(1));

		List<EntidadeExemplo> list = this.seeker.seekByExample(instance);

		assertEquals(1, list.size());
		assertEquals("UmEntidadeExemplo", list.get(0).getStringField());
	}

	@Test
	public void deveRetornarDuasInstanciaAPartirDeUmExemplo() throws RepositoryException {
		EntidadeExemplo instance = new EntidadeExemplo();
		instance.setStringField("EntidadeExemplo");

		List<EntidadeExemplo> list = this.seeker.seekByExample(instance);

		assertEquals(2, list.size());
		assertEquals("EntidadeExemploZero", list.get(0).getStringField());
		assertEquals("UmEntidadeExemplo", list.get(1).getStringField());
	}

	@Test
	public void deveRetornarUmaInstanciaAPartirDeUmExemploComAssociacao() throws RepositoryException {
		EntidadeExemplo instance = new EntidadeExemplo();

		EntidadeExemplo entidadeExample = new EntidadeExemplo();
		entidadeExample.setStringField("Um");

		instance.setEntidadeExemplo(entidadeExample);

		List<EntidadeExemplo> list = this.seeker.seekByExample(instance);

		assertEquals(1, list.size());
		assertEquals("ExemploDoisEntidade", list.get(0).getStringField());
	}

	@Test
	public void deveRetornarUmaInstanciaAPartirDeUmExemploSemAssociacao() throws RepositoryException {
		EntidadeExemplo instance = new EntidadeExemplo();
		instance.setStringField("EntidadeExemplo");
		instance.setIntegerField(1);

		List<EntidadeExemplo> list = this.seeker.seekByExample(instance);

		assertEquals(1, list.size());
		assertEquals("UmEntidadeExemplo", list.get(0).getStringField());
	}

	@Test
	public void deveRetornarUmaInstanciaAPartirDeUmExemploComStringEAssociacao() throws RepositoryException {
		EntidadeExemplo entidadeExample = new EntidadeExemplo();
		entidadeExample.setStringField("Um");

		EntidadeExemplo instance = new EntidadeExemplo();
		instance.setStringField("Entidade");
		instance.setEntidadeExemplo(entidadeExample);

		List<EntidadeExemplo> list = this.seeker.seekByExample(instance);

		assertEquals(1, list.size());
		assertEquals("ExemploDoisEntidade", list.get(0).getStringField());
	}

	@Test
	public void deveRetornarUmaInstanciaAPartirDeUmExemploComBigDecimalEAssociacao() throws RepositoryException {
		EntidadeExemplo entidadeExample = new EntidadeExemplo();
		entidadeExample.setStringField("Um");

		EntidadeExemplo instance = new EntidadeExemplo();
		instance.setBigDecimalField(BigDecimal.ZERO);
		instance.setEntidadeExemplo(entidadeExample);

		List<EntidadeExemplo> list = this.seeker.seekByExample(instance);

		assertEquals(1, list.size());
		assertEquals("ExemploDoisEntidade", list.get(0).getStringField());
	}

	@Test
	public void naoDeveRetornarUmaInstanciaAPartirDeUmExemploComAssociacao() throws RepositoryException {
		EntidadeExemplo entidadeExample = new EntidadeExemplo();
		entidadeExample.setStringField("Classe");

		EntidadeExemplo instance = new EntidadeExemplo();
		instance.setStringField("Entidade");
		instance.setEntidadeExemplo(entidadeExample);

		List<EntidadeExemplo> list = this.seeker.seekByExample(instance);

		assertTrue(list.isEmpty());
	}

	@Test
	public void deveRetornarUmaInstanciaAPartirDeUmaCadeia() throws RepositoryException {
		EntidadeExemplo exemplo = new EntidadeExemplo();
		exemplo.setStringField("Zero");

		EntidadeExemplo entidadeExample = new EntidadeExemplo();
		entidadeExample.setEntidadeExemplo(exemplo);

		EntidadeExemplo instance = new EntidadeExemplo();
		instance.setEntidadeExemplo(entidadeExample);

		List<EntidadeExemplo> list = this.seeker.seekByExample(instance);

		assertEquals(1, list.size());
		assertEquals("ExemploDoisEntidade", list.get(0).getStringField());
	}

	@Test
	public void deveRetornarUmaInstanciaAPartirDeUmaCombinacao() throws RepositoryException {
		EntidadeExemplo entidadeExample = new EntidadeExemplo();
		entidadeExample.setStringField("Um");

		EntidadeExemplo instance = new EntidadeExemplo();
		instance.setStringField("Entidade");
		instance.setBigDecimalField(BigDecimal.ZERO);
		instance.setEntidadeExemplo(entidadeExample);

		List<EntidadeExemplo> list = this.seeker.seekByExample(instance);

		assertEquals(1, list.size());
		assertEquals("ExemploDoisEntidade", list.get(0).getStringField());
	}

	@After
	public void tearDown() {
		this.transaction.rollback();
		this.transaction = null;
	}

}
