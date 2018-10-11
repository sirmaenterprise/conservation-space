package com.sirma.itt.emf.audit.processor;

import static java.util.stream.Collectors.maxBy;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.db.AuditDao;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.tasks.Schedule;
import com.sirma.itt.seip.tasks.TransactionMode;

/**
 * Import Trigger service. Defines a cron method that regularly imports data to recent activities core. Using the last
 * known date from the current solr data.
 *
 * @author BBonev
 */
@ApplicationScoped
public class RecentActivitiesImporter {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "audit.solr.recentActivitiesImport.batchSize", system = true, sensitive = true, type = Integer.class, defaultValue = "50", label = "Recent activities import batch size of the unique request ids")
	private ConfigurationProperty<Integer> requestBatchSize;

	@Inject
	private AuditDao auditDao;
	@Inject
	private RecentActivitiesSolrImporter activitiesSolrImporter;

	/**
	 * Trigger automatic solr import of non imported recent activities
	 *
	 * @throws RollbackedException if cannot communicate with solr server to fetch the last known activity date
	 */
	@Schedule(identifier = "recentActivitiesImport", system = false)
	@ConfigurationPropertyDefinition(name = "audit.solr.recentActivitiesImport.cron", defaultValue = "0/5 * * ? * *", system = true, sensitive = true, label = "Recent activities solr import trigger")
	void triggerImport() throws RollbackedException {
		Date knownActivityDate = activitiesSolrImporter.getLastKnownActivityDate();
		Collection<AuditActivity> activities;
		try {
			while (!(activities = auditDao.getActivitiesAfter(knownActivityDate, requestBatchSize.get())).isEmpty()) {
				LOGGER.trace("Importing {} entries", activities.size());
				activitiesSolrImporter.importActivities(activities);

				// get the last processed date
				// this could also be changed with query to solr but for now is not needed
				knownActivityDate = getLastKnownActivityDate(activities);

			}
		} catch (RollbackedException e) {
			LOGGER.warn("Fail to perform solr import due to: {}", e.getMessage(), e);
		}
	}

	private static Date getLastKnownActivityDate(Collection<AuditActivity> activities) {
		return activities
				.stream()
					.map(AuditActivity::getEventDate)
					.filter(Objects::nonNull)
					.max(Comparator.naturalOrder())
					.orElse(null);
	}
}
