package com.sirma.sep.instance.operation;

import static com.sirma.itt.seip.instance.actions.InstanceOperationProperties.OPERATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.actions.InstanceOperationProperties;
import com.sirma.itt.seip.instance.event.BeforeInstanceMoveEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.InstanceContextServiceMock;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

/**
 * Test for {@link InstanceMoveOperation}.
 */
@RunWith(DataProviderRunner.class)
public class InstanceMoveOperationTest {

	@Mock
	protected EventService eventService;

	@Mock
	private InstanceContextServiceMock contextService;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Spy
	private InstancePropertyNameResolver fieldConverter = InstancePropertyNameResolver.NO_OP_INSTANCE;

	@InjectMocks
	private InstanceMoveOperation instanceMoveOperation;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Options.CURRENT_OPERATION.clear();
	}

	@Test
	public void should_SaveInstanceWithOperation_When_OperationIsSet() {
		String operationName = "operation-name";
		Instance target = mock(Instance.class);
		Instance sourceInstance = mock(Instance.class);
		Instance parent = mock(Instance.class);
		Context<String, Object> executionContext = createExecutionContext(sourceInstance, target);
		executionContext.put(OPERATION, new Operation(operationName));
		setupContextService(sourceInstance, parent);

		instanceMoveOperation.execute(executionContext);

		verify(domainInstanceService).save(argThat(instanceSaveContextMatcher(sourceInstance, operationName)));
	}

	@Test
	public void should_SaveInstanceWithMoveOperation_When_OperationIsNotSet() {
		Instance target = mock(Instance.class);
		Instance sourceInstance = mock(Instance.class);
		Instance parent = mock(Instance.class);
		Context<String, Object> executionContext = createExecutionContext(sourceInstance, target);
		setupContextService(sourceInstance, parent);

		instanceMoveOperation.execute(executionContext);

		verify(domainInstanceService).save(argThat(instanceSaveContextMatcher(sourceInstance, ActionTypeConstants.MOVE)));
	}

	@Test
	@UseDataProvider("should_FireEventsDP")
	public void should_FireEvents(Instance parent) {
		Instance target = mock(Instance.class);
		Instance sourceInstance = mock(Instance.class);
		Context<String, Object> executionContext = createExecutionContext(sourceInstance, target);
		setupContextService(sourceInstance, parent);

		instanceMoveOperation.execute(executionContext);

		verify(eventService).fire(argThat(beforeInstanceMoveEventMatcher(sourceInstance, parent, target)));
		verify(eventService).fireNextPhase(argThat(beforeInstanceMoveEventMatcher(sourceInstance, parent, target)));
	}

	@DataProvider
	public static Object[][] should_FireEventsDP() {
		return new Object[][] {
				{ null },
				{ mock(Instance.class) }
		};
	}

	@Test
	public void should_ReturnMoveOperation_When_CheckSupportedOperations() {
		Set<String> supportedOperations = instanceMoveOperation.getSupportedOperations();

		assertEquals(1, supportedOperations.size());
		assertTrue(supportedOperations.contains(ActionTypeConstants.MOVE));
	}

	private static CustomMatcher<InstanceSaveContext> instanceSaveContextMatcher(Instance instance,
			String operationName) {
		return CustomMatcher.of((InstanceSaveContext instanceSaveContext) -> {
			assertEquals(instance, instanceSaveContext.getInstance());
			assertEquals(operationName, instanceSaveContext.getOperation().getOperation());
		});
	}

	private static CustomMatcher<BeforeInstanceMoveEvent> beforeInstanceMoveEventMatcher(Instance src, Instance parent,
			Instance target) {
		return CustomMatcher.of((BeforeInstanceMoveEvent beforeInstanceMoveEvent) -> {
			assertEquals(parent, beforeInstanceMoveEvent.getSourceInstance());
			assertEquals(src, beforeInstanceMoveEvent.getInstance());
			assertEquals(target, beforeInstanceMoveEvent.getTargetInstance());
		});
	}

	private static Context<String, Object> createExecutionContext(Instance sourceInstance, Instance target) {
		Context<String, Object> context = new Context<>();
		context.put(InstanceOperationProperties.INSTANCE, target);
		context.put(InstanceOperationProperties.SOURCE_INSTANCE, sourceInstance);
		return context;
	}

	private void setupContextService(Instance instance, Instance parent) {
		if (parent == null) {
			when(contextService.getContext(instance)).thenReturn(Optional.empty());
		} else {
			InstanceReference parentReference = mock(InstanceReference.class);
			when(parentReference.toInstance()).thenReturn(parent);
			when(contextService.getContext(instance)).thenReturn(Optional.of(parentReference));
		}
	}
}
