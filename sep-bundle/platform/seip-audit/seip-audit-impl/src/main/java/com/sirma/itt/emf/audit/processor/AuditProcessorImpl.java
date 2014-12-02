package com.sirma.itt.emf.audit.processor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.command.AuditContextCommand;
import com.sirma.itt.emf.audit.configuration.AuditConfigurationProperties;
import com.sirma.itt.emf.audit.db.AuditDao;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.time.TimeTracker;

/**
 * Processes Audit activities by persisting them in a DB.
 * 
 * @author Mihail Radkov
 */
@ApplicationScoped
public class AuditProcessorImpl implements AuditProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuditProcessorImpl.class);

	@Inject
	@Config(name = AuditConfigurationProperties.AUDIT_ENABLED, defaultValue = "false")
	private Boolean auditEnabled;

	@Inject
	private AuditDao auditDao;

	// TODO: Configure it!
	@Inject
	private AuditContextCommand contextCommand;

	@Override
	public void processActivity(AuditActivity activity) {
		if (auditEnabled) {
			TimeTracker tracker = TimeTracker.createAndStart();
			contextCommand.execute(activity);
			auditDao.publish(activity);
			LOGGER.debug("Audit activity processed in {} ms", tracker.stop());
		}
	}
}
