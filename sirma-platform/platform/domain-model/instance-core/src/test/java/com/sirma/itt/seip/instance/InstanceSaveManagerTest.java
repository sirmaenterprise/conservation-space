package com.sirma.itt.seip.instance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * Test for {@link InstanceSaveManager}.
 *
 * @author A. Kunchev
 */
public class InstanceSaveManagerTest {

	@InjectMocks
	private InstanceSaveManager manager;

	@Mock
	private InstanceSaveStep instanceSaveStep1;

	@Mock
	private InstanceSaveStep instanceSaveStep2;

	private List<InstanceSaveStep> stepsList = new ArrayList<>();

	@Spy
	private Plugins<InstanceSaveStep> steps = new Plugins<>("", stepsList);

	@Mock
	private InstanceService instanceService;

	@Before
	public void setup() {
		manager = new InstanceSaveManager();
		MockitoAnnotations.initMocks(this);

		stepsList.clear();
		stepsList.add(instanceSaveStep1);
		stepsList.add(instanceSaveStep2);
	}

	@Test(expected = NullPointerException.class)
	public void saveInstance_nullContext() {
		manager.saveInstance(null);
	}

	@Test
	public void saveInstance_failInBeforePhase_firstStep() {
		EmfInstance instance = new EmfInstance();
		Operation operation = new Operation();
		InstanceSaveContext context = InstanceSaveContext.create(instance, operation);
		try {
			doThrow(new RuntimeException()).when(instanceSaveStep1).beforeSave(context);
			manager.saveInstance(context);
		} catch (@SuppressWarnings("unused") Exception e) {
			// nothing to do here
		} finally {
			verify(instanceService, never()).save(instance, operation);

			verify(instanceSaveStep1).beforeSave(context);
			verify(instanceSaveStep1, never()).rollbackBeforeSave(eq(context), any(Throwable.class));
			verify(instanceSaveStep1, never()).afterSave(context);
			verify(instanceSaveStep1, never()).rollbackAfterSave(eq(context), any(Throwable.class));

			verify(instanceSaveStep2, never()).beforeSave(context);
			verify(instanceSaveStep2, never()).rollbackBeforeSave(eq(context), any(Throwable.class));
			verify(instanceSaveStep2, never()).afterSave(context);
			verify(instanceSaveStep2, never()).rollbackAfterSave(eq(context), any(Throwable.class));
		}
	}

	@Test
	public void saveInstance_failInBeforePhase_secondStep() {
		EmfInstance instance = new EmfInstance();
		Operation operation = new Operation();
		InstanceSaveContext context = InstanceSaveContext.create(instance, operation);
		try {
			doThrow(new RuntimeException()).when(instanceSaveStep2).beforeSave(context);
			manager.saveInstance(context);
		} catch (@SuppressWarnings("unused") Exception e) {
			// nothing to do here
		} finally {
			verify(instanceService, never()).save(instance, operation);

			verify(instanceSaveStep1).beforeSave(context);
			verify(instanceSaveStep1).rollbackBeforeSave(eq(context), any(Throwable.class));
			verify(instanceSaveStep1, never()).afterSave(context);
			verify(instanceSaveStep1, never()).rollbackAfterSave(eq(context), any(Throwable.class));

			verify(instanceSaveStep2).beforeSave(context);
			verify(instanceSaveStep2, never()).rollbackBeforeSave(eq(context), any(Throwable.class));
			verify(instanceSaveStep2, never()).afterSave(context);
			verify(instanceSaveStep2, never()).rollbackAfterSave(eq(context), any(Throwable.class));
		}
	}

	@Test
	public void saveInstance_failInSave() {
		EmfInstance instance = new EmfInstance();
		Operation operation = new Operation();
		InstanceSaveContext context = InstanceSaveContext.create(instance, operation);
		try {
			doThrow(new RuntimeException()).when(instanceService).save(instance, operation);
			manager.saveInstance(context);
		} catch (@SuppressWarnings("unused") Exception e) {
			// nothing to do here
		} finally {
			verify(instanceService).save(instance, operation);

			verify(instanceSaveStep1).beforeSave(context);
			verify(instanceSaveStep1).rollbackBeforeSave(eq(context), any(Throwable.class));
			verify(instanceSaveStep1, never()).afterSave(context);
			verify(instanceSaveStep1, never()).rollbackAfterSave(eq(context), any(Throwable.class));

			verify(instanceSaveStep2).beforeSave(context);
			verify(instanceSaveStep2).rollbackBeforeSave(eq(context), any(Throwable.class));
			verify(instanceSaveStep2, never()).afterSave(context);
			verify(instanceSaveStep2, never()).rollbackAfterSave(eq(context), any(Throwable.class));
		}
	}

	@Test
	public void saveInstance_failInAfterPhase_firstStep() {
		EmfInstance instance = new EmfInstance();
		Operation operation = new Operation();
		InstanceSaveContext context = InstanceSaveContext.create(instance, operation);
		try {
			doThrow(new NullPointerException()).when(instanceSaveStep1).afterSave(context);
			manager.saveInstance(context);
		} catch (@SuppressWarnings("unused") Exception e) {
			// nothing to do here
		} finally {
			verify(instanceService).save(instance, operation);

			verify(instanceSaveStep1).beforeSave(context);
			verify(instanceSaveStep1).rollbackBeforeSave(eq(context), any(Throwable.class));
			verify(instanceSaveStep1).afterSave(context);
			verify(instanceSaveStep1, never()).rollbackAfterSave(eq(context), any(Throwable.class));

			verify(instanceSaveStep2).beforeSave(context);
			verify(instanceSaveStep2).rollbackBeforeSave(eq(context), any(Throwable.class));
			verify(instanceSaveStep2, never()).afterSave(context);
			verify(instanceSaveStep2, never()).rollbackAfterSave(eq(context), any(Throwable.class));
		}
	}

	@Test
	public void saveInstance_failInAfterPhase_secondStep() {
		EmfInstance instance = new EmfInstance();
		Operation operation = new Operation();
		InstanceSaveContext context = InstanceSaveContext.create(instance, operation);
		try {
			doThrow(new NullPointerException()).when(instanceSaveStep2).afterSave(context);
			manager.saveInstance(context);
		} catch (@SuppressWarnings("unused") Exception e) {
			// nothing to do here
		} finally {
			verify(instanceService).save(instance, operation);

			verify(instanceSaveStep1).beforeSave(context);
			verify(instanceSaveStep1).rollbackBeforeSave(eq(context), any(Throwable.class));
			verify(instanceSaveStep1).afterSave(context);
			verify(instanceSaveStep1).rollbackAfterSave(eq(context), any(Throwable.class));

			verify(instanceSaveStep2).beforeSave(context);
			verify(instanceSaveStep2).rollbackBeforeSave(eq(context), any(Throwable.class));
			verify(instanceSaveStep2).afterSave(context);
			verify(instanceSaveStep2, never()).rollbackAfterSave(eq(context), any(Throwable.class));
		}
	}

	@Test
	public void saveInstance_successful() {
		EmfInstance instance = new EmfInstance();
		Operation operation = new Operation();
		InstanceSaveContext context = InstanceSaveContext.create(instance, operation);

		doAnswer(a -> {
			Instance target = a.getArgumentAt(0, InstanceSaveContext.class).getInstance();
			target.add("test-property", "property-value");
			return target;
		}).when(instanceSaveStep1).beforeSave(context);

		Instance result = manager.saveInstance(context);
		assertNotNull(result);
		assertEquals("property-value", result.getString("test-property"));

		verify(instanceService).save(instance, operation);

		verify(instanceSaveStep1).beforeSave(context);
		verify(instanceSaveStep1, never()).rollbackBeforeSave(eq(context), any(Throwable.class));
		verify(instanceSaveStep1).afterSave(context);
		verify(instanceSaveStep1, never()).rollbackAfterSave(eq(context), any(Throwable.class));

		verify(instanceSaveStep2).beforeSave(context);
		verify(instanceSaveStep2, never()).rollbackBeforeSave(eq(context), any(Throwable.class));
		verify(instanceSaveStep2).afterSave(context);
		verify(instanceSaveStep2, never()).rollbackAfterSave(eq(context), any(Throwable.class));
	}
}
