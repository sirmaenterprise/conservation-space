package com.sirma.itt.seip.permissions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.security.exception.NoPermissionsException;

/**
 * Test for {@link ValidatePermissionsStep}.
 *
 * @author A. Kunchev
 */
public class ValidatePermissionsStepTest {

	@InjectMocks
	private ValidatePermissionsStep step;

	@Mock
	private DatabaseIdManager databaseIdManager;

	@Mock
	private InstanceAccessEvaluator instanceAccessEvaluator;

	@Before
	public void setup() {
		step = new ValidatePermissionsStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void beforeSave_instanceNotPersisted() {
		Instance instance = new EmfInstance();
		instance.setId("instance-id");
		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());
		when(databaseIdManager.isIdPersisted("instance-id")).thenReturn(false);
		step.beforeSave(context);
		verify(instanceAccessEvaluator, never()).canWrite(any(InstanceReference.class));
	}

	@Test(expected = NoPermissionsException.class)
	public void beforeSave_instancePersistedNoPermission() {
		Instance instance = new EmfInstance();
		instance.setId("instance-id");
		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());
		when(databaseIdManager.isIdPersisted("instance-id")).thenReturn(true);
		when(instanceAccessEvaluator.canWrite(any(InstanceReference.class))).thenReturn(false);
		step.beforeSave(context);
	}

	@Test
	@SuppressWarnings("unused")
	public void beforeSave_instancePersistedWithPermission() {
		Instance instance = new EmfInstance();
		instance.setId("instance-id");
		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());
		when(databaseIdManager.isIdPersisted("instance-id")).thenReturn(true);
		when(instanceAccessEvaluator.canWrite(any(InstanceReference.class))).thenReturn(true);
		try {
			step.beforeSave(context);
		} catch (Exception e) {
			fail("When the flow is okay, there should not be any exceptions.");
		}
	}

	@Test
	public void getName() {
		assertEquals("validatePermissions", step.getName());
	}

}
