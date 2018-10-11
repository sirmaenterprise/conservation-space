package com.sirma.itt.seip.instance.save;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.event.ParentChangedEvent;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Test for {@link AssignContextStep}.
 *
 * @author A. Kunchev
 */
@RunWith(MockitoJUnitRunner.class)
public class AssignContextStepTest {

	private static final String INSTANCE_ID = "emf:instanceId";
	private static final String PARENT_ONE_ID = "emf:parentOneId";
	private static final String PARENT_TWO_ID = "emf:parentTwoId";

	@Mock
	private InstanceContextService instanceContextService;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private EventService eventService;

	@Spy
	private InstancePropertyNameResolver fieldConverter = InstancePropertyNameResolver.NO_OP_INSTANCE;

	@InjectMocks
	private AssignContextStep assignContextStep;

	@Test
	public void should_persistContext_When_ParentIsRemoved() {
		executeTest(PARENT_ONE_ID, null, true);
	}

	@Test
	public void should_NotPersistContext_When_AddParentForTheFirstTime() {
		executeTest(null, PARENT_TWO_ID, true);
	}

	@Test
	public void should_NotPersistContext_When_OldParentAndNewOneAreDifferent() {
		executeTest(PARENT_ONE_ID, PARENT_TWO_ID, true);
	}

	@Test
	public void should_NotPersistContext_When_OldParentAndNewOneAreSame() {
		executeTest(PARENT_ONE_ID, PARENT_ONE_ID, false);
	}

	@Test
	public void should_NotPersistContext_When_OldParentAndNewOneAreNull() {
		executeTest(null, null, false);
	}

	@Test
	public void getName() {
		assertEquals("assignContext", assignContextStep.getName());
	}

	private InstanceSaveContext setupTest(String oldParentId, String newParentId) {
		Instance instance = new EmfInstance(INSTANCE_ID);
		instance.add(InstanceContextService.HAS_PARENT, newParentId);

		Mockito.when(instanceContextService.isContextChanged(instance)).thenReturn(oldParentId == null ? newParentId != null : !oldParentId.equals(newParentId));

		if (StringUtils.isNotBlank(oldParentId)) {
			Instance oldParent = createInstance(oldParentId);
			InstanceReference instanceReference = oldParent.toReference();
			when(instanceContextService.getContext(INSTANCE_ID)).thenReturn(Optional.of(instanceReference));
		} else {
			when(instanceContextService.getContext(any())).thenReturn(Optional.empty());
		}
		return InstanceSaveContext.create(instance, new Operation());
	}

	private Instance createInstance(String instanceId) {
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(instanceReference.getId()).thenReturn(instanceId);
		Instance instance = mock(Instance.class);
		when(instance.getId()).thenReturn(instanceId);
		when(instanceReference.toInstance()).thenReturn(instance);
		when(instance.toReference()).thenReturn(instanceReference);
		return instance;
	}

	private void executeTest(String oldParentId, String newParentId, boolean haveToPersist) {
		InstanceSaveContext instanceSaveContext = setupTest(oldParentId, newParentId);
		int countOfInvocation = haveToPersist ? 1 : 0;
		assignContextStep.afterSave(instanceSaveContext);
		verify(instanceContextService, times(countOfInvocation)).bindContext(Matchers.any(Instance.class),
																				Matchers.any(String.class));
		verify(eventService, times(countOfInvocation)).fire(Matchers.any(ParentChangedEvent.class));
	}
}
