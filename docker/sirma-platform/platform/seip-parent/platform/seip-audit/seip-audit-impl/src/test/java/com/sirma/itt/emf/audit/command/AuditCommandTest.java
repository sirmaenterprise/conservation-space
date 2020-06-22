package com.sirma.itt.emf.audit.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.instance.AuditObjectIDCommand;
import com.sirma.itt.emf.audit.command.instance.AuditObjectSystemIDCommand;
import com.sirma.itt.emf.audit.command.instance.AuditObjectTypeCommand;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.properties.PropertiesChangeEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.testutil.fakes.InstanceTypeFake;

/**
 * Tests different {@link AuditCommand} implementations that does not have their test classes.
 *
 * @author Mihail Radkov
 */
public class AuditCommandTest {

	private AuditActivity activity;

	/**
	 * Executed before every method annotated with @Test. Initializes an audit activity object.
	 */
	@Before
	public void before() {
		activity = new AuditActivity();
	}

	/**
	 * Tests the {@link AuditAbstractCommand#getInstance(AuditablePayload)} when null or instantiated payload is passed
	 * to it.
	 */
	@Test
	public void testAbstractCommand() {
		AuditAbstractCommand command = new AuditObjectTypeCommand();

		Assert.assertNull(command.getInstance(null));

		AuditablePayload payload = new AuditablePayload(null, null, null, true);
		Assert.assertNull(command.getInstance(payload));

		Instance testIntance = new EmfInstance();
		payload = new AuditablePayload(testIntance, null, null, true);
		Assert.assertEquals(testIntance, command.getInstance(payload));
	}

	/**
	 * Tests the retrieval of the object ID for specific instance.
	 */
	@Test
	public void testObjectIdCommand() {
		AuditCommand command = new AuditObjectIDCommand();
		AuditablePayload payload = getTestPayload();

		// Correct test
		command.execute(payload, activity);
		assertEquals("1-2-3-4-5", activity.getObjectID());

		// Null instance
		activity = new AuditActivity();
		payload = new AuditablePayload(null, null, null, true);
		command.execute(payload, activity);
		assertNull(activity.getObjectID());
	}

	/**
	 * Tests the retrieval of the object system ID for specific instance.
	 */
	@Test
	public void testObjectSystemIdCommand() {
		AuditCommand command = new AuditObjectSystemIDCommand();
		AuditablePayload payload = getTestPayload();

		// Correct test
		command.execute(payload, activity);
		assertEquals("SOME:system-ID", activity.getObjectSystemID());

		// No system id
		Instance instance = getInstance();
		instance.setId(null);
		PropertiesChangeEvent changeEvent = new PropertiesChangeEvent(instance, null, null, null);
		activity = new AuditActivity();
		payload = new AuditablePayload(changeEvent.getInstance(), null, changeEvent, true);
		command.execute(payload, activity);
		assertNull(activity.getObjectSystemID());

		// Null instance
		activity = new AuditActivity();
		payload = new AuditablePayload(null, null, null, true);
		command.execute(payload, activity);
		assertNull(activity.getObjectSystemID());
	}

	/**
	 * Tests the retrieval of the timestamp.
	 */
	@Test
	public void testTimestampCommand() {
		AuditCommand command = new AuditTimestampCommand();
		AuditablePayload payload = getTestPayload();

		// Correct test
		command.execute(payload, activity);
		Long capturedDate = activity.getEventDate().getTime();
		assertTrue(capturedDate <= System.currentTimeMillis());

	}

	// TODO: In AuditCommandTestHelper ?
	/**
	 * Constructs an object of {@link PropertiesChangeEvent} for the tests. TODO: more javadoc
	 *
	 * @return the object
	 */
	public static AuditablePayload getTestPayload() {
		Map<String, Serializable> removed = new HashMap<>();
		removed.put(DefaultProperties.STATUS, "REMOVED_STAT3");

		PropertiesChangeEvent event = new PropertiesChangeEvent(getInstance(), null, removed,
				new Operation("some action"));
		return new AuditablePayload(event.getInstance(), "some action", event, true);
	}

	/**
	 * Builds an {@link Instance} object containing test data.
	 *
	 * @return the test instance
	 */
	public static Instance getInstance() {
		Map<String, Serializable> props = new HashMap<>();

		props.put(DefaultProperties.UNIQUE_IDENTIFIER, "1-2-3-4-5");
		props.put(DefaultProperties.TYPE, "MYSUBTYPE");
		props.put(DefaultProperties.TITLE, "Shiny title 123!@#");
		props.put(DefaultProperties.STATUS, "PRIMARY_STAT3");
		props.put("secondaryState", "SECONDARY_STAT3");

		Instance instance = new EmfInstance();
		instance.setProperties(props);
		instance.setId("SOME:system-ID");
		instance.setIdentifier("definitionId");
		InstanceTypeFake.setType(instance, "emf:Case", "caseinstance");

		return instance;
	}

}
