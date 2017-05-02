package com.sirma.itt.emf.audit.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.instance.AuditObjectStateCommand;
import com.sirma.itt.emf.audit.command.instance.AuditObjectTitleCommand;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;

/**
 * Tests the logic in {@link AuditObjectTitleCommand}.
 *
 * @author Mihail Radkov
 */
@RunWith(EasyMockRunner.class)
public class AuditObjectTitleCommandTest {

	private static final String RELATION_TYPE = "emf:Relation";

	@Mock
	private LabelProvider labelProvider;

	@TestSubject
	private AuditCommand command = new AuditObjectTitleCommand();

	/**
	 * Tests the retrieval of the object title for specific instance.
	 */
	@Test
	public void testObjectTitleCommand() {
		AuditCommand auditCommand = new AuditObjectTitleCommand();
		AuditablePayload payload = AuditCommandTest.getTestPayload();
		AuditActivity activity = new AuditActivity();

		// Correct test
		auditCommand.execute(payload, activity);
		assertEquals("Shiny title 123!@#", activity.getObjectTitle());

		// Null instance
		activity = new AuditActivity();
		payload = new AuditablePayload(null, null, null, true);
		auditCommand.execute(payload, activity);
		assertNull(activity.getObjectTitle());
	}

	/**
	 * Tests the logic in {@link AuditObjectTitleCommand#assignLabel(AuditActivity, AuditContext)} when the provided
	 * activity has no object type or it's empty.
	 */
	@Test
	public void testLabelAssigningForTitleWithoutObjectType() {
		AuditActivity activity = new AuditActivity();

		command.assignLabel(activity, null);
		Assert.assertNull(activity.getObjectTitle());

		activity.setObjectType("");

		command.assignLabel(activity, null);
		Assert.assertNull(activity.getObjectTitle());
	}

	/**
	 * Tests the logic in {@link AuditObjectStateCommand#assignLabel(AuditActivity, AuditContext)} when the provided
	 * activity has object type and it is a relation.
	 */
	@Test
	public void testLabelAssigningForRelationTitle() {
		AuditActivity activity = new AuditActivity();
		activity.setObjectType(RELATION_TYPE);
		activity.setObjectSubTypeLabel("1234");

		mockLabelService("Awesome relation");

		command.assignLabel(activity, null);

		Assert.assertEquals("Awesome relation \"1234\"", activity.getObjectTitle());
	}

	/**
	 * Tests the logic in {@link AuditObjectStateCommand#assignLabel(AuditActivity, AuditContext)} when the provided
	 * activity has object type and it is NOT a relation.
	 */
	@Test
	public void testLabelAssigning() {
		AuditActivity activity = new AuditActivity();
		activity.setObjectType("some_kind_of_a_monster");
		activity.setObjectTitle("one_lame_title");
		activity.setObjectSystemID("1");

		Map<String, String> headers = new HashMap<>();
		headers.put("1", "one_awesome_title");

		AuditContext context = new AuditContext(headers);
		command.assignLabel(activity, context);
		Assert.assertEquals("one_awesome_title", activity.getObjectTitle());
	}

	/**
	 * Tests the logic in {@link AuditObjectStateCommand#assignLabel(AuditActivity, AuditContext)} when the provided
	 * activity has object type and it is NOT a relation but the provided context lacks the header.
	 */
	@Test
	public void testLabelAssigningWithMissingLabel() {
		AuditActivity activity = new AuditActivity();
		activity.setObjectType("some_kind_of_a_monster");
		activity.setObjectTitle("one_lame_title");
		activity.setObjectSystemID("1");

		Map<String, String> headers = new HashMap<>();

		AuditContext context = new AuditContext(headers);
		command.assignLabel(activity, context);
		Assert.assertEquals("one_lame_title", activity.getObjectTitle());
	}

	private void mockLabelService(String label) {
		EasyMock.expect(labelProvider.getValue(EasyMock.anyString())).andReturn(label);
		EasyMock.replay(labelProvider);
	}
}
