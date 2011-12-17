package org.wave.test.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wave.repository.core.Keeper;
import org.wave.repository.enums.ErrorEnum;
import org.wave.repository.enums.FieldEnum;
import org.wave.repository.enums.RemoveEnum;
import org.wave.repository.exceptions.RepositoryException;
import org.wave.test.entities.EntidadeExemplo;
import org.wave.utils.reflection.ReflectionUtil;

public class KeeperTest {

	private Keeper keeper;

	private EntityManager manager;

	private EntityTransaction transaction;

	@Before
	public void setUp() {
		WeldContainer container = new Weld().initialize();
		this.keeper = container.instance().select(Keeper.class).get();

		this.manager = container.instance().select(EntityManager.class).get();
		this.transaction = this.manager.getTransaction();
		this.transaction.begin();
	}

	@Test(expected = RepositoryException.class)
	public void deveLancarExcecaoQuandoPersistirUmaInstanciaNulaException() throws RepositoryException {
		this.keeper.persist(null);
	}

	@Test
	public void deveLancarExcecaoQuandoPersistirUmaInstanciaNula() {
		try {
			this.keeper.persist(null);
		} catch (RepositoryException e) {
			assertEquals(ErrorEnum.NULL_INSTANCE.getMessage(), e.getMessage());
		}
	}

	@Test
	public void devePersistirUmaInstanciaEmUmRepositorio() throws RepositoryException {
		EntidadeExemplo instance = new EntidadeExemplo();

		this.keeper.persist(instance);

		assertNotNull(instance.getId());
		assertTrue(instance.getActive());
		assertTrue(this.manager.contains(instance));
		assertNotNull(this.manager.find(EntidadeExemplo.class, instance.getId()));
	}

	@Test
	public void deveAlterarUmaInstanciaEmUmRepositorio() throws RepositoryException {
		EntidadeExemplo instance = new EntidadeExemplo();

		this.keeper.persist(instance);
		assertNotNull(instance.getId());
		assertTrue(instance.getActive());
		assertTrue(this.manager.contains(instance));

		this.manager.detach(instance);
		assertFalse(this.manager.contains(instance));

		Field active = ReflectionUtil.getField(FieldEnum.ACTIVE.getValue(), EntidadeExemplo.class);
		ReflectionUtil.set(Boolean.FALSE, active, instance);

		String value = "Teste";
		instance.setStringField(value);

		this.keeper.persist(instance);

		assertNotNull(instance.getId());
		assertTrue(instance.getActive());

		EntidadeExemplo actualInstance = this.manager.find(EntidadeExemplo.class, instance.getId());
		assertEquals(value, actualInstance.getStringField());
	}

	@Test(expected = RepositoryException.class)
	public void deveLancarExcecaoQuandoRemoverUmaInstanciaNulaException() throws RepositoryException {
		this.keeper.remove(null);
	}

	@Test
	public void deveLancarExcecaoQuandoRemoverUmaInstanciaNula() {
		try {
			this.keeper.remove(null);
		} catch (RepositoryException e) {
			assertEquals(ErrorEnum.NULL_INSTANCE.getMessage(), e.getMessage());
		}
	}

	@Test
	public void deveRemoverDeFormaLogicaUmaInstanciaEmUmRepositorio() throws RepositoryException {
		EntidadeExemplo instance = new EntidadeExemplo();

		this.keeper.persist(instance);
		assertNotNull(instance.getId());
		assertTrue(instance.getActive());
		assertTrue(this.manager.contains(instance));

		this.keeper.remove(instance);

		assertNotNull(instance.getId());
		assertFalse(instance.getActive());
		assertTrue(this.manager.contains(instance));
		assertNotNull(this.manager.find(EntidadeExemplo.class, instance.getId()));
	}

	@Test
	public void deveRemoverDeFormaLogicaUmaInstanciaDetachedEmUmRepositorio() throws RepositoryException {
		EntidadeExemplo instance = new EntidadeExemplo();

		this.keeper.persist(instance);
		assertNotNull(instance.getId());
		assertTrue(instance.getActive());
		assertTrue(this.manager.contains(instance));

		this.manager.detach(instance);
		assertFalse(this.manager.contains(instance));

		Field active = ReflectionUtil.getField(FieldEnum.ACTIVE.getValue(), EntidadeExemplo.class);
		ReflectionUtil.set(Boolean.TRUE, active, instance);

		this.keeper.remove(instance);

		assertNotNull(instance.getId());
		assertFalse(instance.getActive());
		assertNotNull(this.manager.find(EntidadeExemplo.class, instance.getId()));
	}

	@Test
	public void deveRemoverDeFormaLogicaComParametroUmaInstanciaEmUmRepositorio() throws RepositoryException {
		EntidadeExemplo instance = new EntidadeExemplo();

		this.keeper.persist(instance);
		assertNotNull(instance.getId());
		assertTrue(instance.getActive());
		assertTrue(this.manager.contains(instance));

		this.keeper.remove(instance, RemoveEnum.LOGICAL);

		assertNotNull(instance.getId());
		assertFalse(instance.getActive());
		assertTrue(this.manager.contains(instance));
		assertNotNull(this.manager.find(EntidadeExemplo.class, instance.getId()));
	}

	@Test
	public void deveRemoverDeFormaLogicaComParametroUmaInstanciaDetachedEmUmRepositorio() throws RepositoryException {
		EntidadeExemplo instance = new EntidadeExemplo();

		this.keeper.persist(instance);
		assertNotNull(instance.getId());
		assertTrue(instance.getActive());
		assertTrue(this.manager.contains(instance));

		this.manager.detach(instance);
		assertFalse(this.manager.contains(instance));

		Field active = ReflectionUtil.getField(FieldEnum.ACTIVE.getValue(), EntidadeExemplo.class);
		ReflectionUtil.set(Boolean.TRUE, active, instance);

		this.keeper.remove(instance, RemoveEnum.LOGICAL);

		assertNotNull(instance.getId());
		assertFalse(instance.getActive());
		assertNotNull(this.manager.find(EntidadeExemplo.class, instance.getId()));
	}

	@Test
	public void deveRemoverDeFormaFisicaUmaInstanciaEmUmRepositorio() throws RepositoryException {
		EntidadeExemplo instance = new EntidadeExemplo();

		this.keeper.persist(instance);
		assertNotNull(instance.getId());
		assertTrue(instance.getActive());
		assertTrue(this.manager.contains(instance));

		this.keeper.remove(instance, RemoveEnum.PHYSICAL);

		assertNotNull(instance.getId());
		assertTrue(instance.getActive());
		assertFalse(this.manager.contains(instance));
		assertNull(this.manager.find(EntidadeExemplo.class, instance.getId()));
	}

	@Test
	public void deveRemoverDeFormaFisicaUmaInstanciaDetachedEmUmRepositorio() throws RepositoryException {
		EntidadeExemplo instance = new EntidadeExemplo();

		this.keeper.persist(instance);
		assertNotNull(instance.getId());
		assertTrue(instance.getActive());
		assertTrue(this.manager.contains(instance));

		this.manager.detach(instance);
		assertFalse(this.manager.contains(instance));

		this.keeper.remove(instance, RemoveEnum.PHYSICAL);

		assertNotNull(instance.getId());
		assertTrue(instance.getActive());
		assertFalse(this.manager.contains(instance));
		assertNull(this.manager.find(EntidadeExemplo.class, instance.getId()));
	}

	@Test
	public void naoDeveLancarExcecaoQuandoRemoverDeFormaFisicaUmaInstanciaInexistente() {
		try {
			EntidadeExemplo instance = new EntidadeExemplo();

			this.keeper.persist(instance);
			assertNotNull(instance.getId());
			assertTrue(instance.getActive());
			assertTrue(this.manager.contains(instance));

			this.keeper.remove(instance, RemoveEnum.PHYSICAL);

			assertNotNull(instance.getId());
			assertTrue(instance.getActive());
			assertFalse(this.manager.contains(instance));
			assertNull(this.manager.find(EntidadeExemplo.class, instance.getId()));

			this.keeper.remove(instance, RemoveEnum.PHYSICAL);
		} catch (RepositoryException e) {
			fail(e.getMessage());
		}
	}

	@Test(expected = RepositoryException.class)
	public void deveLancarExcecaoQuandoHouverUmaExcecaoJPANoPersistException() throws RepositoryException {
		this.manager.close();

		EntidadeExemplo instance = new EntidadeExemplo();
		this.keeper.persist(instance);
	}

	@Test
	public void deveLancarExcecaoQuandoHouverUmaExcecaoJPANoPersist() {
		try {
			this.manager.close();

			EntidadeExemplo instance = new EntidadeExemplo();
			this.keeper.persist(instance);
		} catch (RepositoryException e) {
			assertTrue(e.getMessage().contains(ErrorEnum.UNEXPECTED_EXCEPTION.getMessage("")));
		}
	}

	@Test(expected = RepositoryException.class)
	public void deveLancarExcecaoQuandoHouverUmaExcecaoJPANoRemoveLogicoException() throws RepositoryException {
		this.manager.close();

		EntidadeExemplo instance = new EntidadeExemplo();
		this.keeper.remove(instance);
	}

	@Test
	public void deveLancarExcecaoQuandoHouverUmaExcecaoJPANoRemoveLogico() {
		try {
			this.manager.close();

			EntidadeExemplo instance = new EntidadeExemplo();
			this.keeper.remove(instance);
		} catch (RepositoryException e) {
			assertTrue(e.getMessage().contains(ErrorEnum.UNEXPECTED_EXCEPTION.getMessage("")));
		}
	}

	@Test(expected = RepositoryException.class)
	public void deveLancarExcecaoQuandoHouverUmaExcecaoJPANoRemoveFisicoException() throws RepositoryException {
		this.manager.close();

		EntidadeExemplo instance = new EntidadeExemplo();
		this.keeper.remove(instance, RemoveEnum.PHYSICAL);
	}

	@Test
	public void deveLancarExcecaoQuandoHouverUmaExcecaoJPANoRemoveFisico() {
		try {
			this.manager.close();

			EntidadeExemplo instance = new EntidadeExemplo();
			this.keeper.remove(instance, RemoveEnum.PHYSICAL);
		} catch (RepositoryException e) {
			assertTrue(e.getMessage().contains(ErrorEnum.UNEXPECTED_EXCEPTION.getMessage("")));
		}
	}

	@After
	public void tearDown() {
		this.transaction.rollback();
		this.transaction = null;
	}

}
