package com.sirma.itt.emf.audit.observer;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.event.relation.RelationEvent;
import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.audit.configuration.AuditConfigurationProperties;
import com.sirma.itt.emf.audit.schedule.AuditSchedulerAction;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.event.AuditableEvent;
import com.sirma.itt.emf.event.AuditableOperationEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.properties.event.PropertiesChangeEvent;
import com.sirma.itt.emf.scheduler.SchedulerConfiguration;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.scheduler.SchedulerEntryType;
import com.sirma.itt.emf.scheduler.SchedulerService;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.emf.security.event.UserLogoutEvent;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Observers authentication events and changes to EMF instances.
 * 
 * @author Mihail Radkov
 */
@ApplicationScoped
// TODO: Should this be application scoped?
public class AuditActivityObserver {
	//
	/** Logs actions related to this class. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AuditActivityObserver.class);

	/** Configuration property for enabling/disabling the audit module. */
	@Inject
	@Config(name = AuditConfigurationProperties.AUDIT_ENABLED, defaultValue = "false")
	private Boolean auditEnabled;

	/** The authentication service. */
	@Inject
	private javax.enterprise.inject.Instance<AuthenticationService> authenticationService;

	/** The service used for scheduling activities. */
	@Inject
	private SchedulerService service;
	/** Commands for gathering audit information. */
	// TODO: To be configured.
	@Inject
	@ExtensionPoint(AuditCommand.TARGET_NAME)
	private Iterable<AuditCommand> commands;

	/** A list of all allowed instances, on which events can be logged. */
	Collection<Class<? extends Instance>> allowedTypes;

	/**
	 * Set the allowed instances on which events can be logged.
	 */
	@PostConstruct
	public void init() {
		allowedTypes = new HashSet<>();
		allowedTypes.add(ProjectInstance.class);
		allowedTypes.add(CaseInstance.class);
		allowedTypes.add(DocumentInstance.class);
		allowedTypes.add(ObjectInstance.class);
		allowedTypes.add(TaskInstance.class);
		allowedTypes.add(StandaloneTaskInstance.class);
		allowedTypes.add(LinkInstance.class);
	}

	/**
	 * Observes login events in EMF.
	 * 
	 * @param event
	 *            the login event
	 */
	public void onLogin(
			@Observes(during = TransactionPhase.IN_PROGRESS) UserAuthenticatedEvent event) {
		if (!auditEnabled) {
			return;
		}
		if (event.getAuthenticatedUser() == null) {
			return;
		}
		TimeTracker tracker = TimeTracker.createAndStart();
		SchedulerContext contex = new SchedulerContext();

		AuditActivity activity = new AuditActivity();
		activity.setEventDate(new Date(System.currentTimeMillis()));
		activity.setUserName(event.getAuthenticatedUser().getName());
		activity.setActionID("login");

		contex.put("payload", activity);
		schedule(contex);

		LOGGER.debug("Login activity handled and scheduled in {} ms", tracker.stop());
	}

	/**
	 * Observes logout events in EMF.
	 * 
	 * @param event
	 *            the logout event
	 */
	public void onLogout(@Observes(during = TransactionPhase.IN_PROGRESS) UserLogoutEvent event) {
		if (!auditEnabled) {
			return;
		}
		if (event.getAuthenticatedUser() == null) {
			return;
		}
		TimeTracker tracker = TimeTracker.createAndStart();
		SchedulerContext contex = new SchedulerContext();

		AuditActivity activity = new AuditActivity();
		activity.setEventDate(new Date(System.currentTimeMillis()));
		activity.setUserName(event.getAuthenticatedUser().getName());
		activity.setActionID("logout");

		contex.put("payload", activity);
		schedule(contex);

		LOGGER.debug("Logout activity handled and scheduled in {} ms", tracker.stop());
	}

	/**
	 * Observes everything that is not object-related.
	 * 
	 * @param event
	 *            the event
	 */
	public void onAudibleOperation(
			@Observes(during = TransactionPhase.IN_PROGRESS) AuditableOperationEvent event) {
		if (!auditEnabled) {
			return;
		}
		TimeTracker tracker = TimeTracker.createAndStart();
		SchedulerContext context = new SchedulerContext();

		AuditActivity activity = new AuditActivity();
		activity.setEventDate(new Date());
		activity.setUserName(SecurityContextManager.getCurrentUser(authenticationService.get())
				.getName());
		activity.setActionID(event.getOperationId());

		context.put("payload", activity);
		schedule(context);

		LOGGER.debug("Logout activity handled and scheduled in {} ms", tracker.stop());
	}

	/**
	 * Observes any fired {@link AuditableEvent}.
	 * 
	 * @param event
	 *            the event
	 */
	public void onAuditableEvent(@Observes AuditableEvent event) {
		if (!auditEnabled) {
			return;
		}

		TimeTracker tracker = TimeTracker.createAndStart();
		SchedulerContext contex = new SchedulerContext();
		AuditActivity activity = new AuditActivity();

		for (AuditCommand command : commands) {
			command.execute(event, activity);
		}
		activity.setUserName(SecurityContextManager.getCurrentUser(authenticationService.get())
				.getName());
		contex.put("payload", activity);
		schedule(contex);

		LOGGER.debug("EMF activity handled and scheduled in {} ms", tracker.stop());
	}

	/**
	 * Observes changes to EMF instances.
	 * 
	 * @param event
	 *            the event holding the changed EMF instance
	 */
	public void onInstanceChange(
			@Observes(during = TransactionPhase.IN_PROGRESS) PropertiesChangeEvent event) {
		if (!auditEnabled) {
			return;
		}

		if (!isEntityObservable(event.getEntity())) {
			return;
		}

		if (RuntimeConfiguration.isSet(RuntimeConfigurationProperties.DISABLE_AUDIT_LOG)) {
			// TODO: add logging for skipped operation
			return;
		}

		TimeTracker tracker = TimeTracker.createAndStart();
		SchedulerContext contex = new SchedulerContext();
		AuditActivity activity = new AuditActivity();

		for (AuditCommand command : commands) {
			command.execute(event, activity);
		}
		contex.put("payload", activity);
		schedule(contex);

		LOGGER.debug("EMF activity handled and scheduled in {} ms", tracker.stop());
	}

	/**
	 * Logs a {@link RelationEvent}
	 * 
	 * @param event
	 *            the event
	 */
	public void onRelationEvent(@Observes RelationEvent event) {
		if ((event.getFrom() != null) && (event.getTo() != null) && (event.getRelationType() != null)
				&& (event.getRelationId() != null)) {
			// Those shouldn't be empty. If the relation isn't changed from the relation
			// dashlet, it shouldn't be logged.
			logRelationOperation(event.getFrom(), event.getTo(), event.getRelationType(),
					event.getOperationId());
		}

	}

	/**
	 * Log a relation operation.
	 * 
	 * @param from
	 *            the from object
	 * @param to
	 *            the to object
	 * @param relationType
	 *            the relation type
	 * @param operationId
	 *            the operation id
	 */
	private void logRelationOperation(String from, String to, String relationType,
			String operationId) {
		if (!auditEnabled) {
			return;
		}
		TimeTracker tracker = TimeTracker.createAndStart();
		SchedulerContext context = new SchedulerContext();

		LinkInstance linkInstance = new LinkInstance();
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.TYPE, relationType);
		properties.put(DefaultProperties.TITLE, relationType);
		properties.put(DefaultProperties.MODIFIED_BY,
				SecurityContextManager.getCurrentUser(authenticationService.get()).getName());
		linkInstance.setProperties(properties);
		PropertiesChangeEvent event = new PropertiesChangeEvent(linkInstance, null, null,
				operationId);
		AuditActivity activity = new AuditActivity();
		activity.setContext(from + ";" + to);
		for (AuditCommand command : commands) {
			command.execute(event, activity);
		}
		context.put("payload", activity);
		schedule(context);

		LOGGER.debug("Relation activity handled and scheduled in {} ms", tracker.stop());

	}

	/**
	 * Checks if given event is observed or not.
	 * 
	 * @param entity
	 *            the entity
	 * @return true if yes or false if not
	 */
	// TODO: To be configured!
	private boolean isEntityObservable(Entity<?> entity) {
		return allowedTypes.contains(entity.getClass());
	}

	/**
	 * Builds configuration for scheduling an action. Sets the execution to asynchronous and to be
	 * executed ASAP.
	 * 
	 * @return the configuration
	 */
	private SchedulerConfiguration getConfiguration() {
		SchedulerConfiguration config = service.buildEmptyConfiguration(SchedulerEntryType.TIMED);
		config.setScheduleTime(new Date());
		config.setSynchronous(false);
		// TODO: What about setRetry?
		return config;
	}

	/**
	 * Schedules a context. For executor is specified {@link AuditSchedulerAction}.
	 * 
	 * @param contex
	 *            holds an event's information
	 */
	private void schedule(SchedulerContext contex) {
		service.schedule(AuditSchedulerAction.ACTION_NAME, getConfiguration(), contex);
	}
}
