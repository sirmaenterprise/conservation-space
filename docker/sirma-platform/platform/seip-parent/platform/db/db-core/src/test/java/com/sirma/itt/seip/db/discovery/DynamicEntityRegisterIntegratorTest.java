package com.sirma.itt.seip.db.discovery;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import javax.persistence.Entity;

import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.PersistentClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link DynamicEntityRegisterIntegrator}
 *
 * @since 2017-04-11
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
public class DynamicEntityRegisterIntegratorTest {

	@InjectMocks
	private DynamicEntityRegisterIntegrator integrator;
	@Mock
	private EntityDiscovery discovery;
	@Mock
	private Configuration configuration;
	private Properties properties = new Properties();

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		properties.put(DynamicEntityRegisterIntegrator.HIBERNATE_EJB_PERSISTENCE_UNIT_NAME, "persistenceUnit");
		when(configuration.getProperties()).thenReturn(properties);
	}

	@Test
	public void integrate_should_RegisterClasses() throws Exception {
		when(discovery.getEntities(anyString())).thenReturn(Arrays.asList(DummyEntity.class));

		integrator.integrate(configuration, null, null);

		verify(configuration).addAnnotatedClass(DummyEntity.class);
		verify(configuration).buildMappings();
	}

	@Test
	public void integrate_should_IgnoreAlreadyRegisteredClasses() throws Exception {
		when(discovery.getEntities(anyString())).thenReturn(Arrays.asList(DummyEntity.class));
		when(configuration.getClassMapping(DummyEntity.class.getName())).thenReturn(mock(PersistentClass.class));

		integrator.integrate(configuration, null, null);

		verify(configuration, never()).addAnnotatedClass(DummyEntity.class);
		verify(configuration).buildMappings();
	}

	@Test
	public void integrate_should_DoNothing_onNotEntities() throws Exception {
		when(discovery.getEntities(anyString())).thenReturn(Collections.emptyList());

		integrator.integrate(configuration, null, null);

		verify(configuration, never()).addAnnotatedClass(DummyEntity.class);
		verify(configuration, never()).buildMappings();
	}

	@Entity
	private static class DummyEntity {

	}
}
