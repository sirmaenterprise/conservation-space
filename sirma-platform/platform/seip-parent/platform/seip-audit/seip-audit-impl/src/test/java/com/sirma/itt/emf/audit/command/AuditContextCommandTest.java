package com.sirma.itt.emf.audit.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.instance.AuditContextCommand;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DefaultDbIdGenerator;
import com.sirma.itt.seip.db.SequenceEntityGenerator;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.mocks.InstanceContextServiceMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Tests the logic in {@link AuditContextCommand}
 *
 * @author Mihail Radkov
 */
public class AuditContextCommandTest {

	@InjectMocks
	private AuditContextCommand contextCommand;

	@Spy
	private InstanceContextServiceMock contextService;

	private DatabaseIdManager idManager = new SequenceEntityGenerator(new DefaultDbIdGenerator());

	@Before
	public void beforeTest() {
		contextCommand = new AuditContextCommand();
		MockitoAnnotations.initMocks(this);
		EmfTest.initInstanceUtil(idManager, contextService);
		TypeConverterUtil.setTypeConverter(mock(TypeConverter.class));
	}

	/**
	 * Tests the logic in {@link AuditContextCommand#assignLabel(AuditActivity, AuditContext)} when the provided
	 * activity has context.
	 */
	@Test
	public void testAssignContextHeadersWithContext() {
		AuditActivity activity = new AuditActivity();
		activity.setContext("1;2;3");

		Map<String, String> headers = new HashMap<>();
		headers.put("1", "one");
		headers.put("2", "two");

		AuditContext context = new AuditContext(headers);

		contextCommand.assignLabel(activity, context);

		assertEquals("one, two, 3", activity.getContext());
	}

	/**
	 * Tests the logic in {@link AuditContextCommand#assignLabel(AuditActivity, AuditContext)} when the provided
	 * activity does not have context.
	 */
	@Test
	public void testAssignContextHeadersWithoutContext() {
		AuditActivity activity = new AuditActivity();

		Map<String, String> headers = new HashMap<>();
		headers.put("1", "one");
		headers.put("2", "two");

		AuditContext context = new AuditContext(headers);

		contextCommand.assignLabel(activity, context);
		assertNull(activity.getContext());
	}

	/**
	 * Ensures the command will behave when the provided instance is null.
	 */
	@Test
	public void nullTests() {
		AuditablePayload payload = new AuditablePayload(null, null, null, true);
		AuditActivity activity = new AuditActivity();

		contextCommand.execute(payload, activity);
		assertNull(activity.getContext());
	}

	/**
	 * Tests the context retrieval when there is no context.
	 */
	@Test
	public void testContextRetrievalWithoutParentPath() {
		Instance project = populateInstance("1", null);
		AuditablePayload payload = new AuditablePayload(project, null, null, true);
		AuditActivity activity = new AuditActivity();

		contextCommand.execute(payload, activity);
		assertEquals(null, activity.getContext());
	}

	/**
	 * Tests the context retrieval when there is at least one context.
	 */
	@Test
	public void testContextRetrievalWithParentPath() {
		Instance project = populateInstance("1", null);
		Instance caseInst = populateInstance("2", project);

		AuditablePayload payload = new AuditablePayload(caseInst, null, null, true);
		AuditActivity activity = new AuditActivity();

		contextCommand.execute(payload, activity);
		assertEquals("1", activity.getContext());
	}

	/**
	 * Tests the context retrieval when there is at least one context and the {@link AuditActivity} already has some
	 * context set.
	 */
	@Test
	public void testContextRetrievalWithExistingContext() {
		Instance project = populateInstance("1", null);
		Instance caseInst = populateInstance("2", project);

		AuditablePayload payload = new AuditablePayload(caseInst, null, null, true);
		AuditActivity activity = new AuditActivity();
		activity.setContext("Ainulindalë");

		contextCommand.execute(payload, activity);
		assertEquals("Ainulindalë;1", activity.getContext());
	}

	/**
	 * Tests the context retrieval when there is at least two contexts one which is of a disallowed type.
	 */
	@Test
	public void testContextRetrievalWithDisallowedInstances() {
		Instance project = populateInstance("1", null);
		Instance caseInst = populateInstance("2", project);
		Instance document = populateInstance("4", caseInst);

		AuditablePayload payload = new AuditablePayload(document, null, null, true);
		AuditActivity activity = new AuditActivity();

		contextCommand.execute(payload, activity);
		assertEquals("1;2", activity.getContext());
	}

	/**
	 * Tests the context retrieval when there is at least two contexts one which is of a disallowed type.
	 */
	@Test
	public void testContextWithParentPathFalse() {
		Instance project = populateInstance("1", null);
		Instance caseInst = populateInstance("2", project);
		Instance section = populateInstance("3", caseInst);
		Instance document = populateInstance("4", section);

		AuditablePayload payload = new AuditablePayload(document, null, null, false);
		AuditActivity activity = new AuditActivity();

		contextCommand.execute(payload, activity);
		assertNull(activity.getContext());
	}

	/**
	 * Tests the context retrieval when there is an extra context.
	 */
	@Test
	public void testWithExtraContext() {
		Instance project = populateInstance("1", null);
		Instance object = populateInstance("2", null);

		AuditablePayload payload = new AuditablePayload(object, null, null, true);
		payload.setExtraContext(project);
		AuditActivity activity = new AuditActivity();

		contextCommand.execute(payload, activity);
		assertEquals("1", activity.getContext());
	}

	/**
	 * Sets the id and owning instance if any to the provided instance.
	 *
	 * @param id
	 *            - the id for the instance
	 * @param owningInstance
	 *            - the owning instance
	 * @return the provided instance with populated properties
	 */
	private Instance populateInstance(String id, Instance owningInstance) {
		InstanceReferenceMock reference = InstanceReferenceMock.createGeneric(id);
		if (owningInstance != null) {
			contextService.bindContext(reference.toInstance(), owningInstance);
		}
		return reference.toInstance();
	}
}
