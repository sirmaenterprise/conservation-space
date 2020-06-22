package com.sirma.itt.seip.instance.version.revert;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.MODIFIED_ON;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.REVISION_NUMBER;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.STATUS;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.StateTransitionMock;
import com.sirmaenterprise.sep.instance.validator.exceptions.InstanceValidationException;

/**
 * Test for {@link ProcessSystemPropertiesOnRevertStep}.
 *
 * @author A. Kunchev
 */
public class ProcessSystemPropertiesOnRevertStepTest {

	@InjectMocks
	private ProcessSystemPropertiesOnRevertStep step;

	@Mock
	private DatabaseIdManager databaseIdManager;
	@Mock
	private DefinitionService definitionService;
	@Spy
	private InstancePropertyNameResolver fieldConverter = InstancePropertyNameResolver.NO_OP_INSTANCE;

	@Before
	public void setup() {
		step = new ProcessSystemPropertiesOnRevertStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("processSystemProperties", step.getName());
	}

	@Test
	public void invoke_systemPropertiesTransferred() {
		mockValidStatus("DRAFT");
		Instance current = new EmfInstance();
		current.setId("instance-id");
		current.add(VERSION, "1.8");
		current.add(STATUS, "DRAFT");
		current.add(REVISION_NUMBER, "10");
		current.add("lastPublishedRevision", "r-01");
		current.add("lastRevision", "r-001");
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -2);
		Date modifiedOnVersion = calendar.getTime();
		current.add(MODIFIED_ON, modifiedOnVersion);
		RevertContext context = RevertContext
				.create("instance-id-v1.5")
					.setCurrentInstance(current)
					.setRevertResultInstance(new EmfInstance());
		step.invoke(context);

		assertEquals("instance-id", context.getRevertResultInstance().getId());
		assertEquals("1.8", context.getRevertResultInstance().getString(VERSION));
		assertEquals("DRAFT", context.getRevertResultInstance().getString(STATUS));
		assertEquals("10", context.getRevertResultInstance().getString(REVISION_NUMBER));
		assertEquals("r-01", context.getRevertResultInstance().getString("lastPublishedRevision"));
		assertEquals("r-001", context.getRevertResultInstance().getString("lastRevision"));
		Date resultInstanceModifiedOn = context.getRevertResultInstance().get(MODIFIED_ON, Date.class);
		assertTrue(modifiedOnVersion.compareTo(resultInstanceModifiedOn) < 0);
		verify(databaseIdManager).unregisterId(any());
	}

	@Test(expected = InstanceValidationException.class)
	public void invoke_systemPropertiesTransferred_shouldFailIfStatusNotSupported() {
		mockValidStatus();

		Instance current = new EmfInstance();
		current.setId("instance-id");
		current.add(VERSION, "1.8");
		current.add(STATUS, "DRAFT");
		EmfInstance revertInstance = new EmfInstance();
		revertInstance.add(STATUS, "SomeInvalidStatus");
		RevertContext context = RevertContext
				.create("instance-id-v1.5")
				.setCurrentInstance(current)
				.setRevertResultInstance(revertInstance);
		step.invoke(context);
	}

	@Test
	public void invoke_systemPropertiesTransferred_shouldKeepRevertStatusIfCurrentNotValid() {
		mockValidStatus("APPROVED");
		Instance current = new EmfInstance();
		current.setId("instance-id");
		current.add(VERSION, "1.8");
		current.add(STATUS, "DRAFT");
		EmfInstance revertInstance = new EmfInstance();
		revertInstance.add(STATUS, "APPROVED");
		RevertContext context = RevertContext
				.create("instance-id-v1.5")
				.setCurrentInstance(current)
				.setRevertResultInstance(revertInstance);
		step.invoke(context);

		assertEquals("instance-id", context.getRevertResultInstance().getId());
		assertEquals("1.8", context.getRevertResultInstance().getString(VERSION));
		assertEquals("APPROVED", context.getRevertResultInstance().getString(STATUS));
	}

	@Test
	public void invoke_systemPropertiesTransferred_shouldKeepTheState() {
		mockValidStatus("APPROVED", "DRAFT");
		Instance current = new EmfInstance();
		current.setId("instance-id");
		current.add(VERSION, "1.8");
		current.add(STATUS, "DRAFT");
		EmfInstance revertInstance = new EmfInstance();
		revertInstance.add(STATUS, "APPROVED");
		RevertContext context = RevertContext
				.create("instance-id-v1.5")
				.setCurrentInstance(current)
				.setRevertResultInstance(revertInstance);
		step.invoke(context);

		assertEquals("instance-id", context.getRevertResultInstance().getId());
		assertEquals("1.8", context.getRevertResultInstance().getString(VERSION));
		assertEquals("DRAFT", context.getRevertResultInstance().getString(STATUS));
	}

	private void mockValidStatus(String... states) {
		DefinitionMock definition = new DefinitionMock();
		for (String fromState : states) {
			StateTransitionMock transition = new StateTransitionMock();
			transition.setFromState(fromState);
			definition.getStateTransitions().add(transition);
		}
		when(definitionService.getInstanceDefinition(any())).thenReturn(definition);
	}
}
