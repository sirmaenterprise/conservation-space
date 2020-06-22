package com.sirma.itt.seip.instance.observer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.event.InstanceAttachedEvent;
import com.sirma.itt.seip.instance.event.InstanceDetachedEvent;
import com.sirma.itt.seip.instance.event.ParentChangedEvent;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link AutolinkObserver}.
 *
 * @author A. Kunchev
 */
@RunWith(MockitoJUnitRunner.class)
public class AutolinkObserverTest {

	@InjectMocks
	private AutolinkObserver autolinkObserver;
	@Spy
	private InstancePropertyNameResolver nameResolver = InstancePropertyNameResolver.NO_OP_INSTANCE;

	@Test
	public void testOnAfterInstanceMoved() throws Exception {
		InstanceReference target = InstanceReferenceMock.createGeneric("emf:target");
		InstanceReference toMove = InstanceReferenceMock.createGeneric("emf:toMove");
		InstanceReference moveFrom = InstanceReferenceMock.createGeneric("emf:moveFrom");
		autolinkObserver.onAfterInstanceMoved(
				new ParentChangedEvent(toMove.toInstance(), moveFrom.toInstance(), target.toInstance()));
	}

	@Test
	public void testOnInstanceDetachedEventOnObserved() throws Exception {
		InstanceReference target = InstanceReferenceMock.createGeneric("emf:target");
		InstanceReference toDetach = InstanceReferenceMock.createGeneric("emf:toDetach");
		InstanceDetachedEvent<? extends Instance> event = new InstanceDetachedEvent<>(target.toInstance(),
				toDetach.toInstance());
		event.setOperationId(ActionTypeConstants.ADD_LIBRARY);
		autolinkObserver.onInstanceDetachedEvent(event);
	}

	@Test
	public void testOnInstanceAttachedEventOnObserved() throws Exception {
		InstanceReference target = InstanceReferenceMock.createGeneric("emf:target");
		InstanceReference toAttach = InstanceReferenceMock.createGeneric("emf:toAttach");
		InstanceAttachedEvent<? extends Instance> event = new InstanceAttachedEvent<>(target.toInstance(),
				toAttach.toInstance());
		event.setOperationId(ActionTypeConstants.ADD_LIBRARY);
		autolinkObserver.onInstanceAttachedEvent(event);
	}

	@Test
	public void testOnInstanceAttachedEventOnSkipped() throws Exception {
		InstanceReference target = InstanceReferenceMock.createGeneric("emf:target");
		InstanceReference toAttach = InstanceReferenceMock.createGeneric("emf:toAttach");
		InstanceAttachedEvent<? extends Instance> event = new InstanceAttachedEvent<>(target.toInstance(),
				toAttach.toInstance());
		event.setOperationId(ActionTypeConstants.EDIT_DETAILS);
		autolinkObserver.onInstanceAttachedEvent(event);
	}

}
