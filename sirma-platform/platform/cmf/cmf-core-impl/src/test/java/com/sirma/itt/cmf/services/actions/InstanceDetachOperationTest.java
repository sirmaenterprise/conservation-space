package com.sirma.itt.cmf.services.actions;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.event.InstanceDetachedEvent;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * The Class InstanceAttachOperationTest.
 *
 * @author BBonev
 */
@Test
public class InstanceDetachOperationTest extends BaseInstanceOperationTest {

	@Mock
	protected LinkService linkService;
	@InjectMocks
	private InstanceDetachOperation detachOperation;

	/**
	 * Before method.
	 */
	@BeforeMethod
	@Override
	public void beforeMethod() {
		super.beforeMethod();
		createTypeConverter();

		when(eventProvider.createDetachEvent(any(Instance.class), any(Instance.class)))
				.then(invocation -> new InstanceDetachedEvent<>((Instance) invocation.getArguments()[0],
						(Instance) invocation.getArguments()[1]));

		addOperations(detachOperation);
	}

	/**
	 * Test attach_object.
	 */
	public void testDetach_object() {
		EmfInstance target = new EmfInstance();
		target.setId("emf:target");
		EmfInstance toDetach = new EmfInstance();
		toDetach.setId("emf:toDetach");
		EmfInstance attached = new EmfInstance();
		attached.setId("emf:attached");

		getInstanceOperations().invokeDetach(target, new Operation(ActionTypeConstants.DETACH_OBJECT), toDetach);

		verify(eventService, atLeast(1)).fire(any(InstanceDetachedEvent.class));

		verify(linkService).unlinkSimple(any(InstanceReference.class), any(InstanceReference.class),
				eq(LinkConstants.HAS_ATTACHMENT), eq(LinkConstants.IS_ATTACHED_TO));
		verify(linkService).unlinkSimple(any(InstanceReference.class), any(InstanceReference.class),
				eq(LinkConstants.PART_OF_URI), eq(LinkConstants.HAS_CHILD_URI));
	}

	/**
	 * Test attach_document.
	 */
	public void testDetach_document() {
		EmfInstance target = new EmfInstance();
		target.setId("emf:target");
		EmfInstance toDetach = new EmfInstance();
		toDetach.setId("emf:toDetach");
		EmfInstance attached = new EmfInstance();
		attached.setId("emf:attached");

		getInstanceOperations().invokeDetach(target, new Operation(ActionTypeConstants.DETACH_DOCUMENT),
				toDetach);

		verify(eventService, atLeast(1)).fire(any(InstanceDetachedEvent.class));
		verify(linkService).unlinkSimple(any(InstanceReference.class), any(InstanceReference.class),
				eq(LinkConstants.HAS_ATTACHMENT), eq(LinkConstants.IS_ATTACHED_TO));
		verify(linkService).unlinkSimple(any(InstanceReference.class), any(InstanceReference.class),
				eq(LinkConstants.PART_OF_URI), eq(LinkConstants.HAS_CHILD_URI));
	}

}
