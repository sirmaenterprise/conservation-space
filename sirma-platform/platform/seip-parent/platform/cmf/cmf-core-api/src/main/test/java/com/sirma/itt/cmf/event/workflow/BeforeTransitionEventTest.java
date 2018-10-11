package com.sirma.itt.cmf.event.workflow;

import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.notification.NotificationMessage;

/**
 * Tests for AbstractWorkflowTransitionEvent.
 *
 * @author Boyan Tonchev
 *
 */
@SuppressWarnings("static-method")
public class BeforeTransitionEventTest {

	@Spy
	@InjectMocks
	private BeforeTransitionEvent beforeTransitionEvent;

	/**
	 * Sets the up.
	 */
	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test creation instance of class.
	 */
	@Test
	public void creationOfEventTest() {
		WorkflowInstanceContext context = Mockito.mock(WorkflowInstanceContext.class);
		TaskInstance instance = Mockito.mock(TaskInstance.class);
		String transitionId = "transitionId";
		BeforeTransitionEvent event = new BeforeTransitionEvent(context, instance, transitionId);

		Assert.assertEquals(event.getTransitionId(), transitionId);
		Assert.assertEquals(event.getContext(), context);
		Assert.assertEquals(event.getInstance(), instance);
	}

	/**
	 * Tests method haveTransitionNotificationMessages.
	 */
	@Test
	public void haveTransitionNotificationMessagesTest() {
		Assert.assertEquals(beforeTransitionEvent.haveTransitionNotificationMessages(), false);

		beforeTransitionEvent.addTransitionNotificationMessage(Mockito.mock(NotificationMessage.class));
		beforeTransitionEvent.addTransitionNotificationMessage(Mockito.mock(NotificationMessage.class));
		beforeTransitionEvent.addTransitionNotificationMessage(Mockito.mock(NotificationMessage.class));

		Assert.assertEquals(beforeTransitionEvent.haveTransitionNotificationMessages(), true);
		Assert.assertEquals(beforeTransitionEvent.getTransitionNotificationMessages().size(), 3);
	}

	/**
	 * Tests method isTransitionSuccessful. Test if we set ones false
	 * method always have to return false.
	 */
	@Test
	public void isTransitionSuccessfulTest() {
		//setup test
		beforeTransitionEvent.setTransitionSuccessful(false);
		beforeTransitionEvent.setTransitionSuccessful(true);
		beforeTransitionEvent.setTransitionSuccessful(true);
		beforeTransitionEvent.setTransitionSuccessful(false);
		beforeTransitionEvent.setTransitionSuccessful(true);

		//verification
		Assert.assertFalse(beforeTransitionEvent.isTransitionSuccessful());
	}
}
