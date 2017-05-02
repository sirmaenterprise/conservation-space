package com.sirma.itt.emf.audit.observer;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.command.AuditActionIDCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.audit.command.instance.AuditContextCommand;
import com.sirma.itt.emf.audit.command.instance.AuditObjectIDCommand;
import com.sirma.itt.emf.audit.command.instance.AuditRelationStatusCommand;
import com.sirma.itt.emf.audit.command.instance.AuditTargetPropertiesCommand;
import com.sirma.itt.emf.audit.db.AuditDao;
import com.sirma.itt.emf.audit.processor.AuditProcessor;
import com.sirma.itt.emf.audit.processor.AuditProcessorImpl;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.content.event.CheckInEvent;
import com.sirma.itt.seip.instance.content.event.CheckOutEvent;
import com.sirma.itt.seip.instance.event.AfterInstanceMoveEvent;
import com.sirma.itt.seip.instance.event.InstanceAttachedEvent;
import com.sirma.itt.seip.instance.lock.AfterLockEvent;
import com.sirma.itt.seip.instance.lock.BeforeUnlockEvent;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.properties.PropertiesChangeEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.model.LinkSourceId;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

import de.akquinet.jbosscc.needle.annotation.InjectInto;
import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Test the {@link AuditObserverHelper}
 *
 * @author nvelkov
 */
public class AuditInstanceObserverTest {

	@Rule
	public NeedleRule needleRule = new NeedleRule();

	@ObjectUnderTest(id = "aes", implementation = AuditInstanceObserver.class)
	private AuditInstanceObserver instanceObserver;

	@ObjectUnderTest(id = "ap", implementation = AuditProcessorImpl.class, postConstruct = true)
	@InjectInto(targetComponentId = "aes")
	private AuditProcessor auditProcessor;

	@Inject
	private AuditDao auditDao;

	@Before
	public void before() {
		List<AuditCommand> commands = new ArrayList<>();
		commands.add(new AuditObjectIDCommand());
		commands.add(new AuditActionIDCommand());
		commands.add(new AuditContextCommand());
		commands.add(new AuditTargetPropertiesCommand());
		commands.add(new AuditRelationStatusCommand());
		InstanceUtil.init(Mockito.mock(DatabaseIdManager.class));
		ReflectionUtils.setField(auditProcessor, "commands", commands);
		TypeConverterUtil.setTypeConverter(mock(TypeConverter.class));
	}

	/**
	 * Tests the logic for events implementing {@link AuditableEvent}
	 */
	@Test
	public void testAuditableEvent() {
		Capture<AuditActivity> capturedActivity = capturePublishing();

		AuditableEvent auditableEvent = new AuditableEvent(new ObjectInstance(), "Alduin");
		instanceObserver.onAuditableEvent(auditableEvent);

		Assert.assertTrue(capturedActivity.hasCaptured());
		Assert.assertEquals("Alduin", capturedActivity.getValue().getActionID());
	}

	/**
	 * Audit processing should not happen when no operation id is provided
	 */
	@Test
	public void onAuditableEventShouldDoNothingIfOperationIsNotPresent() {

		AuditableEvent auditableEvent = new AuditableEvent(new EmfInstance(), null);
		instanceObserver.onAuditableEvent(auditableEvent);
	}

	/**
	 * Test if the instance attached event is being logged correctly.
	 */
	@Test
	public void testInstanceAttachedEvent() {
		Capture<AuditActivity> capturedQuery = capturePublishing();

		ObjectInstance projectInstance = new ObjectInstance();
		projectInstance.setId("testId");

		ObjectInstance objectInstance = new ObjectInstance();
		objectInstance.add(DefaultProperties.UNIQUE_IDENTIFIER, "testObjectId");

		InstanceAttachedEvent<ObjectInstance> instanceAttachedEvent = new InstanceAttachedEvent<>(projectInstance,
				objectInstance);
		instanceAttachedEvent.setOperationId(ActionTypeConstants.ATTACH_OBJECT);
		instanceObserver.onInstanceAttachedEvent(instanceAttachedEvent);

		AuditActivity activity = capturedQuery.getValue();
		Assert.assertEquals(ActionTypeConstants.ATTACH_OBJECT, activity.getActionID());
		Assert.assertEquals("testObjectId", activity.getObjectID());
	}

	/**
	 * Test the document check out event.
	 */
	@Test
	public void testDocumentCheckOutEvent() {
		Capture<AuditActivity> capturedQuery = capturePublishing();

		ObjectInstance instance = new ObjectInstance();
		instance.add(DefaultProperties.UNIQUE_IDENTIFIER, "test");

		CheckOutEvent checkOutEvent = new CheckOutEvent(instance);
		instanceObserver.onDocumentCheckOut(checkOutEvent);

		AuditActivity activity = capturedQuery.getValue();
		Assert.assertEquals(ActionTypeConstants.EDIT_OFFLINE, activity.getActionID());
		Assert.assertEquals("test", activity.getObjectID());
	}

	/**
	 * Test the instance move event with no source instance (The instance that is being moved didn't have a parent
	 * before).
	 */
	@Test
	public void testInstanceMoveEventNoSource() {
		Capture<AuditActivity> capturedQuery = capturePublishing();

		ObjectInstance instance = new ObjectInstance();
		instance.add(DefaultProperties.UNIQUE_IDENTIFIER, "test");

		ObjectInstance target = new ObjectInstance();
		target.setId("testDestination");

		AfterInstanceMoveEvent afterInstanceMoveEvent = new AfterInstanceMoveEvent(instance, null, target);
		instanceObserver.onAfterInstanceMoveEvent(afterInstanceMoveEvent);

		AuditActivity activity = capturedQuery.getValue();
		Assert.assertEquals(ActionTypeConstants.MOVE, activity.getActionID());
		Assert.assertEquals("test", activity.getObjectID());
		Assert.assertEquals("testDestination", activity.getContext());
		Assert.assertEquals(AuditActivity.STATUS_ADDED, activity.getRelationStatus());
		Assert.assertEquals("testDestination", activity.getTargetProperties());
	}

	/**
	 * Test the document check out event with an existing source instance (The instance that is being moved had a parent
	 * before).
	 */
	@Test
	public void testInstanceMoveEvent() {
		Capture<AuditActivity> capturedQuery = capturePublishing();

		ObjectInstance instance = new ObjectInstance();
		instance.add(DefaultProperties.UNIQUE_IDENTIFIER, "test");

		ObjectInstance source = new ObjectInstance();
		source.setId("testSource");

		ObjectInstance target = new ObjectInstance();
		target.setId("testDestination");

		AfterInstanceMoveEvent afterInstanceMoveEvent = new AfterInstanceMoveEvent(instance, source, target);
		instanceObserver.onAfterInstanceMoveEvent(afterInstanceMoveEvent);

		AuditActivity activity = capturedQuery.getValue();
		Assert.assertEquals(ActionTypeConstants.MOVE, activity.getActionID());
		Assert.assertEquals("test", activity.getObjectID());
		Assert.assertEquals("testSource;testDestination", activity.getContext());
	}

	/**
	 * Test the instance move event within the same case.
	 */
	@Test
	public void testInstanceMoveEventSameCase() {
		Capture<AuditActivity> capturedQuery = capturePublishing();

		ObjectInstance instance = new ObjectInstance();
		instance.add(DefaultProperties.UNIQUE_IDENTIFIER, "test");

		ObjectInstance parentInstance = new ObjectInstance();
		parentInstance.setId("destinationCaseId");

		ObjectInstance source = new ObjectInstance();
		source.setId("testSource");
		source.setOwningInstance(parentInstance);

		ObjectInstance target = new ObjectInstance();
		target.setId("testDestination");
		target.setOwningInstance(parentInstance);
		target.setOwningReference(new InstanceReferenceMock("destinationCaseId", ObjectInstance.class));

		AfterInstanceMoveEvent afterInstanceMoveEvent = new AfterInstanceMoveEvent(instance, source, target);
		instanceObserver.onAfterInstanceMoveEvent(afterInstanceMoveEvent);

		AuditActivity activity = capturedQuery.getValue();
		Assert.assertEquals(ActionTypeConstants.MOVE, activity.getActionID());
		Assert.assertEquals("test", activity.getObjectID());
		// Since we are moving the document from one section to another in the same case, both id's should be the ids of
		// the parent case.
		Assert.assertEquals("testSource;testDestination", activity.getContext());
	}

	/**
	 * Test the document check in event.
	 */
	@Test
	public void testDocumentCheckInEvent() {
		Capture<AuditActivity> capturedQuery = capturePublishing();

		Content content = mock(Content.class);
		ObjectInstance instance = new ObjectInstance();
		instance.add(DefaultProperties.UNIQUE_IDENTIFIER, "testID");

		CheckInEvent checkInEvent = new CheckInEvent(instance, content);
		instanceObserver.onDocumentCheckIn(checkInEvent);

		AuditActivity activity = capturedQuery.getValue();
		Assert.assertEquals(ActionTypeConstants.UPLOAD_NEW_VERSION, activity.getActionID());
		Assert.assertEquals("testID", activity.getObjectID());
	}

	/**
	 * Tests the logic for project instances.
	 */
	@Test
	public void testProjectInstance() {
		Capture<AuditActivity> capturedQuery = capturePublishing();

		PropertiesChangeEvent event = setupTestEvent(new ObjectInstance());
		instanceObserver.onInstanceChange(event);

		Assert.assertTrue(capturedQuery.hasCaptured());
	}

	/**
	 * Tests the logic for object instances.
	 */
	@Test
	public void testObjectInstance() {
		Capture<AuditActivity> capturedQuery = capturePublishing();

		PropertiesChangeEvent event = setupTestEvent(new ObjectInstance());
		instanceObserver.onInstanceChange(event);

		Assert.assertTrue(capturedQuery.hasCaptured());
	}

	/**
	 * Tests the logic for task instances.
	 */
	@Test
	public void testStandaloneTaskInstance() {
		Capture<AuditActivity> capturedQuery = capturePublishing();

		PropertiesChangeEvent event = setupTestEvent(new ObjectInstance());
		instanceObserver.onInstanceChange(event);

		Assert.assertTrue(capturedQuery.hasCaptured());
	}

	/**
	 * Tests the observer for unobserved instances.
	 */
	@Test
	public void testUnobservedInstance() {
		Capture<AuditActivity> capturedQuery = capturePublishing();

		PropertiesChangeEvent event = setupTestEvent(new EmfInstance());
		instanceObserver.onInstanceChange(event);

		Assert.assertFalse(capturedQuery.hasCaptured());
	}

	@Test
	public void test_instanceLocked() {
		Capture<AuditActivity> capturedQuery = capturePublishing();

		ObjectInstance instance = new ObjectInstance();
		instance.setId("emf:case");
		instance.setReference(new LinkSourceId(instance.getId().toString(), mock(DataTypeDefinition.class), instance));
		instanceObserver.onInstanceLocked(new AfterLockEvent(
				new LockInfo(instance.toReference(), "emf:admin", new Date(), null, (user) -> true)));

		Assert.assertTrue(capturedQuery.hasCaptured());
	}

	@Test
	public void test_instanceUnlocked() {
		Capture<AuditActivity> capturedQuery = capturePublishing();

		ObjectInstance instance = new ObjectInstance();
		instance.setId("emf:case");
		instance.setReference(new LinkSourceId(instance.getId().toString(), mock(DataTypeDefinition.class), instance));
		instanceObserver.onInstanceUnlocked(new BeforeUnlockEvent(
				new LockInfo(instance.toReference(), "emf:admin", new Date(), null, (user) -> true)));

		Assert.assertTrue(capturedQuery.hasCaptured());
	}

	/**
	 * Constructs simple test event from the provided instance
	 *
	 * @param instance
	 *            the provided instance
	 * @return the test event
	 */
	private static PropertiesChangeEvent setupTestEvent(Instance instance) {
		return new PropertiesChangeEvent(instance, null, null, new Operation("operation"));
	}

	/**
	 * Captures the argument passed to {@link AuditDao#publish(AuditActivity)}.
	 *
	 * @return the captured argument
	 */
	private Capture<AuditActivity> capturePublishing() {
		Capture<AuditActivity> capturedActivity = new Capture<>();
		auditDao.publish(EasyMock.capture(capturedActivity));
		EasyMock.expectLastCall();
		EasyMock.replay(auditDao);
		return capturedActivity;
	}
}
