package com.sirma.itt.emf.audit.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;

/**
 * Tests the logic in {@link AuditActionIDCommand}
 *
 * @author Mihail Radkov
 */
@RunWith(EasyMockRunner.class)
public class AuditActionIDCommandTest {

	@Mock
	private FieldValueRetrieverService retrieverService;

	@TestSubject
	private AuditCommand command = new AuditActionIDCommand();

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

		AuditCommandTestHelper.mockGetLabel(retrieverService, FieldId.ACTION_ID, "the_action_id",
				"awesome_action_label");

		command.assignLabel(activity, null);

		Assert.assertEquals("awesome_action_label", activity.getAction());
	}
}
