package com.sirma.itt.emf.audit.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.event.cases.CaseDeleteEvent;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.command.instance.AuditModifiedByCommand;
import com.sirma.itt.emf.audit.command.instance.AuditObjectIDCommand;
import com.sirma.itt.emf.audit.command.instance.AuditObjectStateCommand;
import com.sirma.itt.emf.audit.command.instance.AuditObjectSubTypeCommand;
import com.sirma.itt.emf.audit.command.instance.AuditObjectSystemIDCommand;
import com.sirma.itt.emf.audit.command.instance.AuditObjectTitleCommand;
import com.sirma.itt.emf.audit.command.instance.AuditObjectTypeCommand;
import com.sirma.itt.emf.audit.command.instance.AuditPreviousStateCommand;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.event.AuditableEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.properties.event.PropertiesChangeEvent;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Tests different {@link AuditCommand} implementations.
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
	 * Performs various null tests.
	 */
	@Test
	public void nullTests() {
		AuditModifiedByCommand command = new AuditModifiedByCommand();

		command.execute(null, null);

		command.execute(null, activity);
		assertNull(activity.getUserName());

		PropertiesChangeEvent event = new PropertiesChangeEvent(null, null, null, null);
		command.execute(event, activity);
		assertNull(activity.getUserName());

		Instance instance = new DocumentInstance();
		event = new PropertiesChangeEvent(instance, null, null, null);
		command.execute(event, activity);
		assertNull(activity.getUserName());

		// TODO: null property
		// TODO: another event
	}

	/**
	 * Tests the logic at {@link AuditAbstractCommand#getInstance(com.sirma.itt.emf.event.EmfEvent)}
	 * when passed AbstractInstanceEvent as argument.
	 */
	@Test
	public void testAbstractInstanceEvent() {
		AuditCommand command = new AuditModifiedByCommand();
		CaseDeleteEvent event = new CaseDeleteEvent((CaseInstance) getInstance());
		command.execute(event, activity);
		assertEquals("admin", activity.getUserName());
	}

	/**
	 * Tests the retrieval of the user name who last modified an instance.
	 */
	@Test
	public void testUsernameCommand() {
		AuditCommand command = new AuditModifiedByCommand();
		command.execute(getEvent(), activity);
		assertEquals("admin", activity.getUserName());
	}

	/**
	 * Tests the retrieval of the object ID for specific instance.
	 */
	@Test
	public void testObjectIdCommand() {
		AuditCommand command = new AuditObjectIDCommand();
		command.execute(getEvent(), activity);
		assertEquals("1-2-3-4-5", activity.getObjectID());
	}

	/**
	 * Tests the retrieval of the object system ID for specific instance.
	 */
	@Test
	public void testObjectSystemIdCommand() {
		AuditCommand command = new AuditObjectSystemIDCommand();
		command.execute(getEvent(), activity);
		assertEquals("SOME:system-ID", activity.getObjectSystemID());
	}

	/**
	 * Tests the retrieval of the object type for specific instance.
	 */
	@Test
	public void testObjectTypeCommand() {
		AuditCommand command = new AuditObjectTypeCommand();
		NamespaceRegistryService namespaceRegistryService = EasyMock
				.createMock(NamespaceRegistryService.class);
		DataType dataType = EasyMock.createMock(DataType.class);
		DictionaryService dictionaryService = EasyMock.createMock(DictionaryService.class);
		EasyMock.expect(namespaceRegistryService.getShortUri(EasyMock.anyString()))
				.andReturn(CaseInstance.class.getSimpleName().toLowerCase()).anyTimes();
		EasyMock.expect(dictionaryService.getDataTypeDefinition(EasyMock.anyString()))
				.andReturn(dataType).anyTimes();
		EasyMock.replay(namespaceRegistryService, dictionaryService);
		ReflectionUtils.setField(command, "dictionaryService", dictionaryService);
		ReflectionUtils.setField(command, "namespaceRegistryService", namespaceRegistryService);

		command.execute(getEvent(), activity);
		assertEquals(CaseInstance.class.getSimpleName().toLowerCase(), activity.getObjectType());
	}

	/**
	 * Tests the retrieval of the object sub type for specific instance.
	 */
	@Test
	public void testObjectSubTypeCommand() {
		AuditCommand command = new AuditObjectSubTypeCommand();
		command.execute(getEvent(), activity);
		assertEquals("MYSUBTYPE", activity.getObjectSubType());
	}

	/**
	 * Tests the retrieval of the object title for specific instance.
	 */
	@Test
	public void testObjectTitleCommand() {
		AuditCommand command = new AuditObjectTitleCommand();
		command.execute(getEvent(), activity);
		assertEquals("Shiny title 123!@#", activity.getObjectTitle());
	}

	/**
	 * Tests the retrieval of the object primary state for specific instance.
	 */
	@Test
	public void testObjectPrimaryStateCommand() {
		AuditCommand command = new AuditObjectStateCommand();
		command.execute(getEvent(), activity);
		assertEquals("PRIMARY_STAT3", activity.getObjectState());
	}

	/**
	 * Tests the retrieval of the object previous state for specific instance.
	 */
	@Test
	public void testObjectPreviousStateCommand() {
		AuditCommand command = new AuditPreviousStateCommand();
		command.execute(getEvent(), activity);
		assertEquals("REMOVED_STAT3", activity.getObjectPreviousState());
	}

	/**
	 * Tests the retrieval of the timestamp.
	 */
	@Test
	public void testTimestampCommand() {
		AuditCommand command = new AuditTimestampCommand();
		command.execute(getEvent(), activity);

		Long capturedDate = activity.getEventDate().getTime();

		assertTrue(capturedDate <= System.currentTimeMillis());
	}

	/**
	 * Tests the retrieval of the action ID (operation ID).
	 */
	@Test
	public void testActionIdCommand() {
		AuditCommand command = new AuditActionIDCommand();
		command.execute(getEvent(), activity);
		assertEquals("some action", activity.getActionID());

		AuditableEvent event = new AuditableEvent(null, "Alduin");
		command.execute(event, activity);
		assertEquals("Alduin", activity.getActionID());
	}

	/**
	 * Constructs an object of {@link PropertiesChangeEvent} for the tests. TODO: more javadoc
	 * 
	 * @return the object
	 */
	private PropertiesChangeEvent getEvent() {
		Map<String, Serializable> removed = new HashMap<String, Serializable>();
		removed.put(CaseProperties.STATUS, "REMOVED_STAT3");

		PropertiesChangeEvent event = new PropertiesChangeEvent(getInstance(), null, removed,
				"some action");
		return event;
	}

	/**
	 * Builds an {@link Instance} object containing test data. TODO: more javadoc
	 * 
	 * @return the test instance
	 */
	private Instance getInstance() {
		Map<String, Serializable> props = new HashMap<String, Serializable>();
		props.put(DefaultProperties.MODIFIED_BY, "admin");
		props.put(DefaultProperties.UNIQUE_IDENTIFIER, "1-2-3-4-5");
		props.put(DefaultProperties.TYPE, "MYSUBTYPE");
		props.put(DefaultProperties.TITLE, "Shiny title 123!@#");
		props.put(DefaultProperties.STATUS, "PRIMARY_STAT3");
		props.put(CaseProperties.SECONDARY_STATE, "SECONDARY_STAT3");

		Instance instance = new CaseInstance();
		instance.setProperties(props);
		instance.setId("SOME:system-ID");

		return instance;
	}
}
