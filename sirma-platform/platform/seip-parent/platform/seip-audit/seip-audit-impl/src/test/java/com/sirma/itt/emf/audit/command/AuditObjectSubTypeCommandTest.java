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
import com.sirma.itt.emf.audit.command.instance.AuditObjectSubTypeCommand;
import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;

/**
 * Tests the logic in {@link AuditObjectStateCommand}
 *
 * @author Mihail Radkov
 */
@RunWith(EasyMockRunner.class)
public class AuditObjectSubTypeCommandTest {

	@Mock
	private FieldValueRetrieverService fieldValueRetrieverService;

	@TestSubject
	private AuditCommand command = new AuditObjectSubTypeCommand();

	/**
	 * Tests the retrieval of the object sub type for specific instance.
	 */
	@Test
	public void testObjectSubTypeCommand() {
		AuditCommand command = new AuditObjectSubTypeCommand();
		AuditablePayload payload = AuditCommandTest.getTestPayload();
		AuditActivity activity = new AuditActivity();

		// Correct test
		command.execute(payload, activity);
		assertEquals("definitionId", activity.getObjectSubType());

		// Null instance
		activity = new AuditActivity();
		payload = new AuditablePayload(null, null, null, true);
		command.execute(payload, activity);
		assertNull(activity.getObjectSubType());

	}

	/**
	 * Tests the logic in {@link AuditObjectSubTypeCommand#assignLabel(AuditActivity, AuditContext)} when the provided
	 * activity has no object type or it's empty.
	 */
	@Test
	public void testLabelAssigningWithoutObjectType() {
		AuditActivity activity = new AuditActivity();

		command.assignLabel(activity, null);
		Assert.assertNull(activity.getObjectSubTypeLabel());

		activity.setObjectType("");

		command.assignLabel(activity, null);
		Assert.assertNull(activity.getObjectSubTypeLabel());
	}

	/**
	 * Tests the logic in {@link AuditObjectSubTypeCommand#assignLabel(AuditActivity, AuditContext)} when the provided
	 * activity has object type.
	 */
	@Test
	public void testLabelAssigning() {
		AuditActivity activity = new AuditActivity();
		activity.setObjectSubType("sub_type");
		activity.setObjectType("object_type");

		AuditCommandTestHelper.mockGetLabel(fieldValueRetrieverService, FieldId.OBJECT_SUBTYPE, "sub_type",
				"subType_labEl");

		command.assignLabel(activity, null);

		Assert.assertEquals("subType_labEl", activity.getObjectSubTypeLabel());
	}
}
