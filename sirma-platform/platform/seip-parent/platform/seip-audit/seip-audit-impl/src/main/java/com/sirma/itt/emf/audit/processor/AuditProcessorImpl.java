package com.sirma.itt.emf.audit.processor;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.audit.db.AuditDao;
import com.sirma.itt.emf.audit.exception.MissingOperationIdException;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.User;

/**
 * Processes Audit activities by persisting them in a DB.
 *
 * @author Mihail Radkov
 */
@Singleton
public class AuditProcessorImpl implements AuditProcessor {

	/** A list of all allowed instance types, on which events can be processed. */
	private Collection<Class<? extends Instance>> allowedTypes;

	@Inject
	@ExtensionPoint(AuditCommand.TARGET_NAME)
	private Iterable<AuditCommand> commands;

	@Inject
	private AuditDao auditDao;

	/**
	 * Set the allowed instances on which events can be logged.
	 */
	@PostConstruct
	public void initialize() {
		// TODO: To be configured!
		allowedTypes = new HashSet<>();
		allowedTypes.add(ObjectInstance.class);
		allowedTypes.add(LinkInstance.class);
		allowedTypes.add(EmfUser.class);
		allowedTypes.add(EmfGroup.class);
	}

	/**
	 * Persist the provided activity.
	 *
	 * @param activity
	 *            - the provided activity
	 */
	private void publishActivity(AuditActivity activity) {
		auditDao.publish(activity);
	}

	@Override
	public void auditUserOperation(User user, String operationId, EmfEvent triggeredBy) {
		if (user == null) {
			// TODO: throw AuthenticationException?
			return;
		}
		if (operationId == null) {
			throw new MissingOperationIdException("Operation ID is required to log audit activity.");
		}

		AuditActivity auditActivity = new AuditActivity();
		auditActivity.setUserName(user.getName());
		auditActivity.setUserId(user.getSystemId().toString());
		auditActivity.setActionID(operationId);
		auditActivity.setEventDate(new Date());

		publishActivity(auditActivity);
	}

	@Override
	public void process(AuditablePayload payload) {
		auditPayload(payload, null);
	}

	@Override
	public void process(AuditablePayload payload, String context) {
		auditPayload(payload, context);
	}

	@Override
	public void process(Instance instance, String operationId, EmfEvent triggeredBy) {
		process(instance, operationId, triggeredBy, null);
	}

	@Override
	public void process(Instance instance, String operationId, EmfEvent triggeredBy, String context,
			boolean showParentPath) {
		AuditablePayload payload = new AuditablePayload(instance, operationId, triggeredBy, showParentPath);
		auditPayload(payload, context);
	}

	@Override
	public void process(Instance instance, String operationId, EmfEvent triggeredBy, String context) {
		process(instance, operationId, triggeredBy, context, true);
	}

	/**
	 * Constructs a new {@link AuditActivity} and populates it via {@link AuditCommand}.
	 *
	 * @param payload
	 *            - the payload to be audited
	 * @param context
	 *            - any additional context
	 */
	private void auditPayload(AuditablePayload payload, String context) {
		if (StringUtils.isBlank(payload.getOperationId())) {
			throw new MissingOperationIdException("Operation ID is required to log audit activity.");
		}

		AuditActivity auditActivity = new AuditActivity();
		auditActivity.setContext(context);

		for (AuditCommand command : commands) {
			command.execute(payload, auditActivity);
		}

		publishActivity(auditActivity);
	}

	@Override
	public boolean isInstanceApplicable(Instance instance) {
		return allowedTypes.contains(instance.getClass());
	}

}
