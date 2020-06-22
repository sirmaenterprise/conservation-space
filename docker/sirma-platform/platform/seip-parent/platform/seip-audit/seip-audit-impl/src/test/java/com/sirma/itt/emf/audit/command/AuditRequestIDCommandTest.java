package com.sirma.itt.emf.audit.command;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.instance.AuditRequestIDCommand;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Tests the logic in {@link AuditRequestIDCommand}.
 *
 * @author nvelkov
 */
public class AuditRequestIDCommandTest {

	@Mock
	private SecurityContext securityContext;

	@InjectMocks
	private AuditRequestIDCommand command;

	/**
	 * Set up the mocks.
	 */
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the command execute method. It should set the request id.
	 */
	@Test
	public void testExecute() {
		Mockito.when(securityContext.getRequestId()).thenReturn("id");
		AuditablePayload payload = AuditCommandTest.getTestPayload();
		AuditActivity activity = new AuditActivity();

		activity = new AuditActivity();
		command.execute(payload, activity);
		Assert.assertEquals(activity.getRequestId(), "id");
	}

}
