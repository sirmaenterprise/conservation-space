package com.sirma.itt.cmf.services.actions;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.instance.event.InstanceAttachedEvent;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.rest.exceptions.MethodNotAllowedException;

/**
 * The Class InstanceAttachOperationTest.
 *
 * @author BBonev
 */
@Test
public class InstanceAttachOperationTest extends BaseInstanceOperationTest {

	@Mock
	protected LinkService linkService;

	@Mock
	private InstanceContextInitializer instanceContextInitializer;

	@InjectMocks
	private InstanceAttachOperation attachOperation;

	/**
	 * Before method.
	 */
	@BeforeMethod
	@Override
	public void beforeMethod() {
		super.beforeMethod();
		createTypeConverter();

		when(eventProvider.createAttachEvent(any(Instance.class), any(Instance.class)))
				.then(new Answer<InstanceAttachedEvent<Instance>>() {

					@Override
					public InstanceAttachedEvent<Instance> answer(InvocationOnMock invocation) throws Throwable {
						return new InstanceAttachedEvent<>((Instance) invocation.getArguments()[0],
								(Instance) invocation.getArguments()[1]);
					}
				});

		addOperations(attachOperation);
	}

	/**
	 * Test attach_object.
	 */
	public void testAttach_object() {
		EmfInstance target = new EmfInstance();
		target.setId("emf:target");
		EmfInstance toAttach = new EmfInstance();
		toAttach.setId("emf:toAttach");
		EmfInstance attached = new EmfInstance();
		attached.setId("emf:attached");

		getInstanceOperations().invokeAttach(target, new Operation(ActionTypeConstants.ATTACH_OBJECT), toAttach);

		verify(eventService, atLeast(1)).fire(any(InstanceAttachedEvent.class));
		verify(instanceContextInitializer, times(1)).restoreHierarchy(target);
		verify(linkService).linkSimple(any(InstanceReference.class), any(InstanceReference.class),
				eq(LinkConstants.HAS_ATTACHMENT), eq(LinkConstants.IS_ATTACHED_TO));
		verify(linkService).linkSimple(any(InstanceReference.class), any(InstanceReference.class),
				eq(LinkConstants.PART_OF_URI));
	}

	/**
	 * Test attach_object with circular dependency.
	 */
	@Test(expectedExceptions = MethodNotAllowedException.class)
	public void testAttach_object_invalid() {
		EmfInstance target = new EmfInstance();
		target.setId("emf:target");
		EmfInstance toAttach = new EmfInstance();
		toAttach.setId("emf:toAttach");

		target.toReference().setParent(toAttach.toReference());
		getInstanceOperations().invokeAttach(target, new Operation(ActionTypeConstants.ATTACH_OBJECT), toAttach);
	}

	/**
	 * Test attach_document.
	 */
	public void testAttach_document() {
		EmfInstance target = new EmfInstance();
		target.setId("emf:target");
		EmfInstance toAttach = new EmfInstance();
		toAttach.setId("emf:toAttach");
		EmfInstance attached = new EmfInstance();
		attached.setId("emf:attached");

		getInstanceOperations().invokeAttach(target, new Operation(ActionTypeConstants.ATTACH_DOCUMENT), toAttach);

		verify(eventService, atLeast(1)).fire(any(InstanceAttachedEvent.class));
		verify(instanceContextInitializer, times(1)).restoreHierarchy(target);
		verify(linkService).linkSimple(any(InstanceReference.class), any(InstanceReference.class),
				eq(LinkConstants.HAS_ATTACHMENT), eq(LinkConstants.IS_ATTACHED_TO));
		verify(linkService).linkSimple(any(InstanceReference.class), any(InstanceReference.class),
				eq(LinkConstants.PART_OF_URI));
	}

}
