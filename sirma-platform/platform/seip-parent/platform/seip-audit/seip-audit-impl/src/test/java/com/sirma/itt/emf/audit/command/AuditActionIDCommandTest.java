package com.sirma.itt.emf.audit.command;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

/**
 * Tests the logic in {@link AuditActionIDCommand}
 *
 * @author Mihail Radkov
 */
public class AuditActionIDCommandTest {

	@InjectMocks
	private AuditActionIDCommand command;

	@Mock
	private LabelProvider labelProvider;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests the retrieval of the action ID (operation ID).
	 */
	@Test
	public void testActionIdCommand() {
		AuditablePayload payload = AuditCommandTest.getTestPayload();
		AuditActivity activity = new AuditActivity();

		// Correct test
		command.execute(payload, activity);
		assertEquals("some action", activity.getActionID());

		// Null operationId
		activity = new AuditActivity();
		payload = new AuditablePayload(null, null, null, true);
		command.execute(payload, activity);
		assertNull(activity.getActionID());

		// Without instance & event
		payload = new AuditablePayload(null, "Alduin", null, true);
		command.execute(payload, activity);
		assertEquals("Alduin", activity.getActionID());
	}

	/**
	 * Tests the logic in {@link AuditActionIDCommand#assignLabel(AuditActivity, AuditContext)}.
	 */
	@Test
	public void testAssignActionLabel() {
		AuditActivity activity = new AuditActivity();
		activity.setActionID("the_action_id");
		when(labelProvider.getLabel("the_action_id.label")).thenReturn("awesome_action_label");

		command.assignLabel(activity, null);
		Assert.assertEquals("awesome_action_label", activity.getAction());
	}
}
