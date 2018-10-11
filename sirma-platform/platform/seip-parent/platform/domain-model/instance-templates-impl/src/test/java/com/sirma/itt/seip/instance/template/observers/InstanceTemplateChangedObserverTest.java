package com.sirma.itt.seip.instance.template.observers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.save.event.AfterInstanceSaveEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.testutil.CustomMatcher;

/**
 * Test for {@link InstanceTemplateChangedObserver}.
 *
 * @author A. Kunchev
 */
public class InstanceTemplateChangedObserverTest {

	@InjectMocks
	private InstanceTemplateChangedObserver observer;

	@Mock
	private InstanceVersionService instanceVersionService;

	@Mock
	private EventService eventService;
	@Spy
	private InstancePropertyNameResolver nameResolver = InstancePropertyNameResolver.NO_OP_INSTANCE;

	@Before
	public void setup() {
		observer = new InstanceTemplateChangedObserver();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void onInstanceSave_instanceWithInitialVersion_auditableEventNotFired() {
		when(instanceVersionService.hasInitialVersion(any(Instance.class))).thenReturn(true);
		observer.onInstanceSave(new AfterInstanceSaveEvent(new EmfInstance(), null, Operation.NO_OPERATION));
		verifyZeroInteractions(eventService);
	}

	@Test
	public void onInstanceSave_noCurrentInstance_auditableEventNotFired() {
		when(instanceVersionService.hasInitialVersion(any(Instance.class))).thenReturn(false);
		observer.onInstanceSave(new AfterInstanceSaveEvent(new EmfInstance(), null, Operation.NO_OPERATION));
		verifyZeroInteractions(eventService);
	}

	@Test
	public void onInstanceSave_templateNotChanged_auditableEventNotFired() {
		when(instanceVersionService.hasInitialVersion(any(Instance.class))).thenReturn(false);
		Instance instanceToSave = new EmfInstance();
		instanceToSave.add(LinkConstants.HAS_TEMPLATE, "correspondingId");
		Instance currentInstance = new EmfInstance();
		currentInstance.add(LinkConstants.HAS_TEMPLATE, "correspondingId");
		observer.onInstanceSave(new AfterInstanceSaveEvent(instanceToSave, currentInstance, Operation.NO_OPERATION));
		verifyZeroInteractions(eventService);
	}

	@Test
	public void onInstanceSave_templateChanged_auditableEventFired() {
		when(instanceVersionService.hasInitialVersion(any(Instance.class))).thenReturn(false);
		Instance instanceToSave = new EmfInstance();
		instanceToSave.add(LinkConstants.HAS_TEMPLATE, "newCorrespondingId");
		Instance currentInstance = new EmfInstance();
		currentInstance.add(LinkConstants.HAS_TEMPLATE, "oldCorrespondingId");
		observer.onInstanceSave(new AfterInstanceSaveEvent(instanceToSave, currentInstance, Operation.NO_OPERATION));
		verify(eventService).fire(argThat(CustomMatcher.of((AuditableEvent event) -> {
			assertEquals(instanceToSave, event.getInstance());
			assertEquals("changeTemplate", event.getOperationId());
		})));
	}

}
