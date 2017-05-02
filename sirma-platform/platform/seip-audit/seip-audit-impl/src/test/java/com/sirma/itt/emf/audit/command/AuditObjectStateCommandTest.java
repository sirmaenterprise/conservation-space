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
import com.sirma.itt.emf.audit.command.instance.AuditObjectStateCommand;
import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Tests the logic in {@link AuditObjectStateCommand}.
 *
 * @author Mihail Radkov
 */
@RunWith(EasyMockRunner.class)
public class AuditObjectStateCommandTest {

	@Mock
	private FieldValueRetrieverService fieldValueRetrieverService;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@TestSubject
	private AuditCommand command = new AuditObjectStateCommand();

	/**
	 * Tests the retrieval of the object state for specific instance.
	 */
	@Test
	public void testObjectStateCommand() {
		AuditCommand command = new AuditObjectStateCommand();
		AuditablePayload payload = AuditCommandTest.getTestPayload();
		AuditActivity activity = new AuditActivity();

		// Correct test
		command.execute(payload, activity);
		assertEquals("PRIMARY_STAT3", activity.getObjectState());

		// Null instance
		activity = new AuditActivity();
		payload = new AuditablePayload(null, null, null, true);
		command.execute(payload, activity);
		assertNull(activity.getObjectState());

	}

	/**
	 * Tests the logic in {@link AuditObjectStateCommand#assignLabel(AuditActivity, AuditContext)} when the provided
	 * activity has no object type or it's empty.
	 */
	@Test
	public void testLabelAssigningWithoutObjectType() {
		AuditActivity activity = new AuditActivity();

		command.assignLabel(activity, null);
		Assert.assertNull(activity.getObjectStateLabel());

		activity.setObjectType("");

		command.assignLabel(activity, null);
		Assert.assertNull(activity.getObjectStateLabel());
	}

	/**
	 * Tests the logic in {@link AuditObjectStateCommand#assignLabel(AuditActivity, AuditContext)} when the provided
	 * activity has object type.
	 */
	@Test
	public void testLabelAssigning() {
		AuditActivity activity = new AuditActivity();
		activity.setObjectState("state_id");
		activity.setObjectType("object_type");

		AuditCommandTestHelper.mockBuildFullUri(namespaceRegistryService, "the_full_object_type");
		AuditCommandTestHelper.mockGetLabel(fieldValueRetrieverService, FieldId.OBJECT_STATE, "state_id",
				"state_label");

		command.assignLabel(activity, null);

		Assert.assertEquals("state_label", activity.getObjectStateLabel());
	}

}
