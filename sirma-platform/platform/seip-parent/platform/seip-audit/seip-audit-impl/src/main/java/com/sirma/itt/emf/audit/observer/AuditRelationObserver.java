
package com.sirma.itt.emf.audit.observer;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.db.AuditDao;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.instance.event.ObjectPropertyAddEvent;
import com.sirma.itt.seip.domain.instance.event.ObjectPropertyEvent;
import com.sirma.itt.seip.domain.instance.event.ObjectPropertyRemoveEvent;
import com.sirma.itt.seip.security.context.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Date;

/**
 * Logs relation-related events in the audit log.
 *
 * @author BBonev
 */
@ApplicationScoped
public class AuditRelationObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private AuditDao auditDao;

	@Inject
	private AuditObserverHelper helper;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private SecurityContext securityContext;

	/**
	 * Log an add relation operation.
	 *
	 * @param event
	 *            the observed add event
	 */
	public void onRelationAdd(@Observes ObjectPropertyAddEvent event) {
		PropertyInstance relation = getAuditableProperty(event);
		if (relation == null) {
			return;
		}
		AuditActivity activity = createAuditActivity(event, AuditActivity.STATUS_ADDED);
		auditDao.publish(activity);
		LOGGER.debug("New audit event for {} for relation {} with id {} on add of {}", event.getSourceId(),
				event.getObjectPropertyName(), relation.getAuditEvent(), event.getTargetId());
	}

	/**
	 * Log a remove relation operation.
	 *
	 * @param event
	 *            the observed remove event
	 */
	public void onRelationRemove(@Observes ObjectPropertyRemoveEvent event) {
		PropertyInstance relation = getAuditableProperty(event);
		if (relation == null) {
			return;
		}
		AuditActivity activity = createAuditActivity(event, AuditActivity.STATUS_REMOVED);
		auditDao.publish(activity);
		LOGGER.debug("New audit event for {} for relation {} with id {} on remove of {}", event.getSourceId(),
				event.getObjectPropertyName(), relation.getAuditEvent(), event.getTargetId());
	}

	private PropertyInstance getAuditableProperty(ObjectPropertyEvent event) {
		PropertyInstance relation = semanticDefinitionService.getRelation(event.getObjectPropertyName());
		if (relation == null || !relation.isAuditable()) {
			return null;
		}
		return relation;
	}

	private AuditActivity createAuditActivity(ObjectPropertyEvent event, String status) {
		AuditActivity activity = new AuditActivity();
		activity.setEventDate(new Date());
		activity.setUserName(helper.getCurrentUser().getIdentityId());
		activity.setUserId(helper.getCurrentUser().getSystemId().toString());
		activity.setObjectSystemID(event.getSourceId().toString());
		activity.setRelationId(event.getObjectPropertyName());
		activity.setTargetProperties(event.getTargetId().toString());
		activity.setRelationStatus(status);
		activity.setRequestId(securityContext.getRequestId());
		return activity;
	}
}
