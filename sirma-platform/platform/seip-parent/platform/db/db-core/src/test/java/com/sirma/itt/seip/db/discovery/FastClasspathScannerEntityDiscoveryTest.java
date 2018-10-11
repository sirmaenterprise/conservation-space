package com.sirma.itt.seip.db.discovery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.junit.Test;

/**
 * Test for {@link FastClasspathScannerEntityDiscovery}
 *
 * @since 2017-04-11
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
public class FastClasspathScannerEntityDiscoveryTest {

	@Test
	@SuppressWarnings("static-method")
	public void getEntities_shouldProvideOnlyPersistenceRelatedClasses() throws Exception {
		FastClasspathScannerEntityDiscovery discovery = new FastClasspathScannerEntityDiscovery(
				FastClasspathScannerEntityDiscoveryTest.class.getPackage().getName());
		Collection<Class<?>> entities = discovery.getEntities("test");
		assertNotNull(entities);
		// here is included also the entity defined in the DynamicEntityRegisterIntegratorTest
		assertTrue("Expected @Entity annotated class", entities.contains(DummyEntity.class));
		assertTrue("Expected @MappedSuperclass annotated class", entities.contains(DummyMappedSuperClass.class));
		assertTrue("Expected @Embeddable annotated class", entities.contains(DummyEmbeddable.class));
		assertFalse("Not expected @Entity annotated class with bound unit name that does not match the requested", entities.contains(DummyEntityUnit1.class));
		assertTrue("Expected @Entity annotated class with matching unit name", entities.contains(DummyEntityUnit2.class));

		assertNotNull(discovery.getEntities("test"));
	}

	@Test
	@SuppressWarnings("static-method")
	public void getEntities_shouldProvideAllPersistenceRelatedClassesIfNoUnitIsSpecified() throws Exception {
		FastClasspathScannerEntityDiscovery discovery = new FastClasspathScannerEntityDiscovery(
				FastClasspathScannerEntityDiscoveryTest.class.getPackage().getName());
		Collection<Class<?>> entities = discovery.getEntities(null);
		assertNotNull(entities);
		// here is included also the entity defined in the DynamicEntityRegisterIntegratorTest
		assertTrue("Expected @Entity annotated class", entities.contains(DummyEntity.class));
		assertTrue("Expected @MappedSuperclass annotated class", entities.contains(DummyMappedSuperClass.class));
		assertTrue("Expected @Embeddable annotated class", entities.contains(DummyEmbeddable.class));
		assertTrue("Expected @Entity annotated class", entities.contains(DummyEntityUnit1.class));
		assertTrue("Expected @Entity annotated class", entities.contains(DummyEntityUnit2.class));

		assertNotNull(discovery.getEntities("test"));
	}

	@Entity
	static class DummyEntity {
		// nothing to do
	}

	@MappedSuperclass
	static class DummyMappedSuperClass {
		// nothing to do
	}

	@Embeddable
	static class DummyEmbeddable {
		// nothing to do
	}

	@Entity
	@PersistenceUnitBinding("unit1")
	static class DummyEntityUnit1 {
		// nothing to do
	}

	@Entity
	@PersistenceUnitBinding({"unit2", "test"})
	static class DummyEntityUnit2 {
		// nothing to do
	}
}
