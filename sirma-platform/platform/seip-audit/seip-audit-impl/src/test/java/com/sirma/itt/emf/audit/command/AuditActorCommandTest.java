package com.sirma.itt.emf.audit.command;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.instance.AuditActorCommand;
import com.sirma.itt.emf.audit.command.instance.AuditContextCommand;
import com.sirma.itt.emf.audit.observer.AuditObserverHelper;
import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.seip.resources.EmfUser;

/**
 * Tests the label assignment in {@link AuditContextCommand}.
 *
 * @author Mihail Radkov
 */
@RunWith(EasyMockRunner.class)
public class AuditActorCommandTest {

	private static final String EXPECTED_USER_NAME = "someUser";
	private static final String EXPECTED_USER_ID = "someUserId";

	@Mock
	private FieldValueRetrieverService fieldValueRetrieverService;

	@TestSubject
	private AuditCommand command = new AuditActorCommand();

	/**
	 * Tests the retrieval of the user executing the audited operation
	 */
	@Test
	public void testUsernameCommand() {
		AuditCommand command = new AuditActorCommand();
		AuditablePayload payload = AuditCommandTest.getTestPayload();
		AuditActivity activity = new AuditActivity();

		// Mocking
		AuditObserverHelper auditObserverHelper = EasyMock.createMock(AuditObserverHelper.class);

		EmfUser user = new EmfUser(EXPECTED_USER_NAME);
		user.setId(EXPECTED_USER_ID);
		EasyMock.expect(auditObserverHelper.getCurrentUser()).andReturn(user).anyTimes();
		EasyMock.replay(auditObserverHelper);

		ReflectionUtils.setField(command, "auditObserverHelper", auditObserverHelper);

		// Correct test
		command.execute(payload, activity);
		assertEquals(EXPECTED_USER_NAME, activity.getUserName());
		assertEquals(EXPECTED_USER_ID, activity.getUserId());

		// Null instance
		activity = new AuditActivity();
		payload = new AuditablePayload(null, null, null, true);
		command.execute(payload, activity);
		assertEquals(EXPECTED_USER_NAME, activity.getUserName());
	}

	/**
	 * Tests the logic in {@link AuditContextCommand#assignLabel(AuditActivity, AuditContext)}.
	 */
	@Test
	public void testAssignUsernameLabel() {
		AuditActivity activity = new AuditActivity();
		activity.setUserName("the_username");

		AuditCommandTestHelper.mockGetLabel(fieldValueRetrieverService, FieldId.USERNAME, "the_username",
				"awesome_label");

		command.assignLabel(activity, null);

		Assert.assertEquals("awesome_label", activity.getUserDisplayName());
	}

}
