package com.sirma.itt.seip.instance.save;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Test for {@link AssignCustomIdStep}
 * 
 * @author kirq4e
 */
public class AssignCustomIdStepTest {

	@InjectMocks
	private AssignCustomIdStep step;

	@Mock
	private DatabaseIdManager idManager;

	@Before
	public void setup() {
		step = new AssignCustomIdStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void beforeSave_notPersistedInstance_withCustomId() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		String expextedId = "emf:customId-test";
		instance.add("customIRI", expextedId);

		Mockito.when(idManager.isPersisted(Mockito.eq(instance))).thenReturn(false);

		step.beforeSave(InstanceSaveContext.create(instance, Operation.NO_OPERATION));
		assertEquals(instance.getId(), expextedId);
		verify(idManager, Mockito.times(1)).unregister(Mockito.eq(instance));
		verify(idManager, Mockito.times(1)).register(Mockito.eq(instance));

	}

	@Test
	public void beforeSave_persistedInstance_withCustomId() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.add("customIRI", "emf:customId-test");

		Mockito.when(idManager.isPersisted(Mockito.eq(instance))).thenReturn(true);

		step.beforeSave(InstanceSaveContext.create(instance, Operation.NO_OPERATION));
		assertEquals(instance.getId(), "emf:instance");
		verify(idManager, Mockito.never()).unregister(Mockito.eq(instance));
		verify(idManager, Mockito.never()).register(Mockito.eq(instance));
	}

	@Test
	public void beforeSave_persistedInstance_withoutCustomId() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");

		Mockito.when(idManager.isPersisted(Mockito.eq(instance))).thenReturn(true);

		step.beforeSave(InstanceSaveContext.create(instance, Operation.NO_OPERATION));
		assertEquals(instance.getId(), "emf:instance");
		verify(idManager, Mockito.never()).unregister(Mockito.eq(instance));
		verify(idManager, Mockito.never()).register(Mockito.eq(instance));
	}

	@Test
	public void beforeSave_notPersistedInstance_withoutCustomId() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");

		Mockito.when(idManager.isPersisted(Mockito.eq(instance))).thenReturn(false);

		step.beforeSave(InstanceSaveContext.create(instance, Operation.NO_OPERATION));
		assertEquals(instance.getId(), "emf:instance");
		verify(idManager, Mockito.never()).unregister(Mockito.eq(instance));
		verify(idManager, Mockito.never()).register(Mockito.eq(instance));
	}

	@Test
	public void getName() {
		assertEquals("setCustomIdStep", step.getName());
	}

}
