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
import com.sirma.itt.emf.audit.command.instance.AuditPreviousStateCommand;
import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.seip.instance.properties.PropertiesChangeEvent;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Tests the logic in {@link AuditPreviousStateCommand}
 *
 * @author Mihail Radkov
 */
@RunWith(EasyMockRunner.class)
public class AuditPreviousStateCommandTest {

	@Mock
	private FieldValueRetrieverService fieldValueRetrieverService;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@TestSubject
	private AuditCommand command = new AuditPreviousStateCommand();

	/**
	 * Tests the retrieval of the object previous state for specific instance.
	 */
	@Test
	public void testObjectPreviousStateCommand() {
		AuditCommand command = new AuditPreviousStateCommand();
		AuditablePayload payload = AuditCommandTest.getTestPayload();
		AuditActivity activity = new AuditActivity();

		// Correct test
		activity = new AuditActivity();
		command.execute(payload, activity);
		assertEquals("REMOVED_STAT3", activity.getObjectPreviousState());

		// Empty removed properties
		activity = new AuditActivity();
		((PropertiesChangeEvent) payload.getTriggeredBy()).getRemoved().clear();
		command.execute(payload, activity);
		assertNull(activity.getObjectPreviousState());

		// No removed properties
		activity = new AuditActivity();
		PropertiesChangeEvent event = new PropertiesChangeEvent(AuditCommandTest.getInstance(), null, null, null);
		payload = new AuditablePayload(event.getInstance(), null, event, true);
		command.execute(payload, activity);
		assertNull(activity.getObjectPreviousState());

		// Null instance
		activity = new AuditActivity();
		payload = new AuditablePayload(null, null, null, true);
		command.execute(payload, activity);
		assertNull(activity.getObjectPreviousState());
	}

	/**
	 * Tests the logic in {@link AuditPreviousStateCommand#assignLabel(AuditActivity, AuditContext)} when the provided
	 * activity has no object type or it's empty.
	 */
	@Test
	public void testLabelAssigningWithoutObjectType() {
		AuditActivity activity = new AuditActivity();

		command.assignLabel(activity, null);
		Assert.assertNull(activity.getObjectPreviousStateLabel());

		activity.setObjectType("");

		command.assignLabel(activity, null);
		Assert.assertNull(activity.getObjectPreviousStateLabel());
	}

	/**
	 * Tests the logic in {@link AuditPreviousStateCommand#assignLabel(AuditActivity, AuditContext)} when the provided
	 * activity has object type and previous state.
	 */
	@Test
	public void testLabelAssigning() {
		AuditActivity activity = new AuditActivity();
		activity.setObjectType("object_type");
		activity.setObjectPreviousState("prev_state_id");

		AuditCommandTestHelper.mockBuildFullUri(namespaceRegistryService, "the_full_object_type");
		AuditCommandTestHelper.mockGetLabel(fieldValueRetrieverService, FieldId.OBJECT_STATE, "prev_state_id",
				"PAST STATE LABEluru");

		command.assignLabel(activity, null);

		Assert.assertEquals("PAST STATE LABEluru", activity.getObjectPreviousStateLabel());
	}

	/**
	 * Tests the logic in {@link AuditPreviousStateCommand#assignLabel(AuditActivity, AuditContext)} when the provided
	 * activity has object type and previous state.
	 */
	@Test
	public void testLabelAssigningWithoutPrevousState() {
		AuditActivity activity = new AuditActivity();
		activity.setObjectType("object_type");

		AuditCommandTestHelper.mockBuildFullUri(namespaceRegistryService, "the_full_object_type");
		AuditCommandTestHelper.mockGetLabel(fieldValueRetrieverService, FieldId.OBJECT_STATE, null, "");

		command.assignLabel(activity, null);

		Assert.assertNull(activity.getObjectPreviousStateLabel());
	}

}
