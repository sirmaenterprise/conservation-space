package com.sirma.itt.emf.audit.observer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.event.relation.RelationChangeEvent;
import com.sirma.itt.cmf.event.relation.RelationCreateEvent;
import com.sirma.itt.cmf.event.relation.RelationDeleteEvent;
import com.sirma.itt.cmf.event.relation.RelationEvent;
import com.sirma.itt.cmf.help.HelpRequestEvent;
import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.command.AuditActionIDCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.audit.command.instance.AuditModifiedByCommand;
import com.sirma.itt.emf.event.AuditableEvent;
import com.sirma.itt.emf.event.AuditableOperationEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.properties.event.PropertiesChangeEvent;
import com.sirma.itt.emf.scheduler.DefaultSchedulerConfiguration;
import com.sirma.itt.emf.scheduler.SchedulerConfiguration;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.scheduler.SchedulerEntry;
import com.sirma.itt.emf.scheduler.SchedulerEntryType;
import com.sirma.itt.emf.scheduler.SchedulerService;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.emf.security.event.UserLogoutEvent;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.pm.domain.model.ProjectInstance;

import de.akquinet.jbosscc.needle.annotation.InjectInto;
import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Test the {@link AuditActivityObserver}
 */

public class AuditActivityObserverTest {

	/** The needle rule. */
	@Rule
	public NeedleRule needleRule = new NeedleRule();

	/** The export service. */
	@ObjectUnderTest(id = "aes", implementation = AuditActivityObserver.class)
	private AuditActivityObserver observer;

	/** The audit enabled. */
	@InjectInto(targetComponentId = "aes")
	private Boolean auditEnabled = true;

	/** The service. */
	@Inject
	private SchedulerService service;

	@Inject
	private AuthenticationService authenticationService;

	@Inject
	private javax.enterprise.inject.Instance<AuthenticationService> authenticationServiceInstance;

	/** The aaa. */
	AuditCommand aaa = new AuditModifiedByCommand();
	AuditCommand actionIdCommand = new AuditActionIDCommand();

	/** The commands. */
	@InjectInto(targetComponentId = "aes")
	private Iterable<AuditCommand> commands = Arrays.asList(aaa);

	/**
	 * Call the observer init method before each test.
	 */
	@Before
	public void setup() {
		observer.init();
	}

	/**
	 * Test login.
	 */
	@Test
	public void testLogin() {
		Capture<SchedulerContext> capturedQuery = setupMock();
		observer.onLogin(new UserAuthenticatedEvent(new EmfUser("kaloqn")));
		Assert.assertEquals("kaloqn",
				capturedQuery.getValue().getIfSameType("payload", AuditActivity.class)
						.getUserName());
	}

	/**
	 * Test logout.
	 */
	@Test
	public void testLogout() {
		Capture<SchedulerContext> capturedQuery = setupMock();
		observer.onLogout(new UserLogoutEvent(new EmfUser("kaloqn")));
		Assert.assertEquals("kaloqn",
				capturedQuery.getValue().getIfSameType("payload", AuditActivity.class)
						.getUserName());
	}

	/**
	 * Tests the logic for events implementing {@link AuditableOperationEvent}
	 */
	@Test
	public void testAuditableOperation() {
		Capture<SchedulerContext> capturedQuery = setupMock();
		mockAuthenticationService();
		AuditableOperationEvent event = new HelpRequestEvent();
		observer.onAudibleOperation(event);
		Assert.assertEquals("kaloqn",
				capturedQuery.getValue().getIfSameType("payload", AuditActivity.class)
						.getUserName());
	}

	/**
	 * Tests the logic for events implementing {@link AuditableEvent}
	 */
	@Test
	public void testAuditableEvent() {
		Capture<SchedulerContext> capturedQuery = setupMock();
		PropertiesChangeEvent changeEvent = setupTestEvent(new CaseInstance());
		AuditableEvent auditableEvent = new AuditableEvent((Instance) changeEvent.getEntity(),
				"Alduin");
		mockAuthenticationService();
		observer.onAuditableEvent(auditableEvent);

		AuditActivity activity = capturedQuery.getValue().getIfSameType("payload",
				AuditActivity.class);
		Assert.assertEquals("kaloqn", activity.getUserName());
	}

	/**
	 * Tests the logic for case instances.
	 */
	@Test
	public void testCaseInstance() {
		Capture<SchedulerContext> capturedQuery = setupMock();

		PropertiesChangeEvent event = setupTestEvent(new CaseInstance());

		observer.onInstanceChange(event);
		Assert.assertEquals("admin",
				capturedQuery.getValue().getIfSameType("payload", AuditActivity.class)
						.getUserName());
	}

	/**
	 * Tests the logic for project instances.
	 */
	@Test
	public void testProjectInstance() {
		Capture<SchedulerContext> capturedQuery = setupMock();

		PropertiesChangeEvent event = setupTestEvent(new ProjectInstance());

		observer.onInstanceChange(event);
		Assert.assertEquals("admin",
				capturedQuery.getValue().getIfSameType("payload", AuditActivity.class)
						.getUserName());
	}

	/**
	 * Tests the logic for document instances.
	 */
	@Test
	public void testDocumentInstance() {
		Capture<SchedulerContext> capturedQuery = setupMock();

		PropertiesChangeEvent event = setupTestEvent(new DocumentInstance());

		observer.onInstanceChange(event);
		Assert.assertEquals("admin",
				capturedQuery.getValue().getIfSameType("payload", AuditActivity.class)
						.getUserName());
	}

	/**
	 * Tests the logic for object instances.
	 */
	@Test
	public void testObjectInstance() {
		Capture<SchedulerContext> capturedQuery = setupMock();

		PropertiesChangeEvent event = setupTestEvent(new ObjectInstance());

		observer.onInstanceChange(event);
		Assert.assertEquals("admin",
				capturedQuery.getValue().getIfSameType("payload", AuditActivity.class)
						.getUserName());
	}

	/**
	 * Tests the logic for task instances.
	 */
	@Test
	public void testTaskInstance() {
		Capture<SchedulerContext> capturedQuery = setupMock();

		PropertiesChangeEvent event = setupTestEvent(new TaskInstance());

		observer.onInstanceChange(event);
		Assert.assertEquals("admin",
				capturedQuery.getValue().getIfSameType("payload", AuditActivity.class)
						.getUserName());
	}

	/**
	 * Tests the logic for task instances.
	 */
	@Test
	public void testStandaloneTaskInstance() {
		Capture<SchedulerContext> capturedQuery = setupMock();

		PropertiesChangeEvent event = setupTestEvent(new StandaloneTaskInstance());

		observer.onInstanceChange(event);
		Assert.assertEquals("admin",
				capturedQuery.getValue().getIfSameType("payload", AuditActivity.class)
						.getUserName());
	}

	/**
	 * Tests the observer for unobserved instances.
	 */
	@Test
	public void testUnobservedInstance() {
		Capture<SchedulerContext> capturedQuery = setupMock();

		PropertiesChangeEvent event = setupTestEvent(new SectionInstance());

		observer.onInstanceChange(event);
		Assert.assertFalse(capturedQuery.hasCaptured());
	}

	/**
	 * Test relation create.
	 */
	@Test
	public void testRelationCreate() {
		Capture<SchedulerContext> capturedQuery = setupMock();
		mockAuthenticationService();
		RelationEvent event = setupRelationEvent("create");

		observer.onRelationEvent(event);
		AuditActivity activityAfterCommands = capturedQuery.getValue().getIfSameType("payload",
				AuditActivity.class);
		actionIdCommand.execute(event, activityAfterCommands);
		Assert.assertEquals("createLink",
				capturedQuery.getValue().getIfSameType("payload", AuditActivity.class)
						.getActionID());
	}

	/**
	 * Test relation delete.
	 */
	@Test
	public void testRelationDelete() {
		Capture<SchedulerContext> capturedQuery = setupMock();
		mockAuthenticationService();
		RelationEvent event = setupRelationEvent("delete");

		observer.onRelationEvent(event);
		AuditActivity activityAfterCommands = capturedQuery.getValue().getIfSameType("payload",
				AuditActivity.class);
		actionIdCommand.execute(event, activityAfterCommands);
		Assert.assertEquals("delete",
				capturedQuery.getValue().getIfSameType("payload", AuditActivity.class)
						.getActionID());
	}

	/**
	 * Test relation change.
	 */
	@Test
	public void testRelationChange() {
		Capture<SchedulerContext> capturedQuery = setupMock();
		mockAuthenticationService();
		RelationEvent event = setupRelationEvent("change");

		observer.onRelationEvent(event);
		AuditActivity activityAfterCommands = capturedQuery.getValue().getIfSameType("payload",
				AuditActivity.class);
		actionIdCommand.execute(event, activityAfterCommands);
		Assert.assertEquals("editDetails",
				capturedQuery.getValue().getIfSameType("payload", AuditActivity.class)
						.getActionID());
	}

	/**
	 * Constructs simple test event from the provided instance with 'modified by' property.
	 * 
	 * @param instance
	 *            the provided instance
	 * @return the test event
	 */
	private PropertiesChangeEvent setupTestEvent(Instance instance) {
		Map<String, Serializable> props = new HashMap<String, Serializable>();
		props.put(DefaultProperties.MODIFIED_BY, "admin");
		instance.setProperties(props);
		PropertiesChangeEvent event = new PropertiesChangeEvent(instance, null, null, null);
		return event;
	}

	/**
	 * Setups a {@link Capture} to
	 * {@link SchedulerService#schedule(Class, SchedulerConfiguration, SchedulerContext)}.
	 * 
	 * @return the captured mock.
	 */
	private Capture<SchedulerContext> setupMock() {
		Capture<SchedulerContext> capturedContext = new Capture<SchedulerContext>();
		EasyMock.expect(
				service.schedule(EasyMock.anyString(),
						EasyMock.anyObject(SchedulerConfiguration.class),
						EasyMock.capture(capturedContext))).andAnswer(
				new IAnswer<SchedulerEntry>() {

					@Override
					public SchedulerEntry answer() throws Throwable {
						// TODO Auto-generated method stub
						return null;
					}
				});
		EasyMock.expect(
				service.buildEmptyConfiguration(EasyMock.anyObject(SchedulerEntryType.class)))
				.andAnswer(new IAnswer<SchedulerConfiguration>() {

					@Override
					public SchedulerConfiguration answer() throws Throwable {
						SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
						configuration.setType(SchedulerEntryType.TIMED);
						return configuration;
					}
				});
		EasyMock.replay(service);
		return capturedContext;
	}

	/**
	 * Mocks {@link javax.enterprise.inject.Instance<AuthenticationService>} &
	 * {@link AuthenticationService}.
	 */
	private void mockAuthenticationService() {
		EasyMock.expect(authenticationServiceInstance.get()).andAnswer(
				new IAnswer<AuthenticationService>() {
					@Override
					public AuthenticationService answer() throws Throwable {
						return authenticationService;
					}
				});
		EasyMock.replay(authenticationServiceInstance);

		EasyMock.expect(authenticationService.getCurrentUser()).andAnswer(new IAnswer<User>() {
			@Override
			public User answer() throws Throwable {
				return new EmfUser("kaloqn");
			}
		});
		EasyMock.replay(authenticationService);
	}

	/**
	 * Setup e relation event based on the type we want.
	 * 
	 * @param type
	 *            the type
	 * @return the relation event
	 */
	private RelationEvent setupRelationEvent(String type) {
		switch (type) {
			case "create":
				return new RelationCreateEvent("fromId", "toId", "relationTyp", "relationId");
			case "delete":
				return new RelationDeleteEvent("fromId", "toId", "relationTyp", "relationId");
			case "change":
				return new RelationChangeEvent("fromId", "toId", "relationTyp", "relationId");
			default:
				break;
		}
		return null;
	}
}
