package com.sirma.sep.template.patches;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.instance.version.VersionsResponse;
import com.sirma.itt.seip.template.db.TemplateEntity;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.CDI;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Adds missing published_template_version value in sep_template by extracting it from the latest active version of the
 * corresponding template instance.
 *
 * @author Adrian Mitev
 */
public class PublishedInstanceVersionAndTitlePatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private InstanceVersionService instanceVersionService;
	private TransactionSupport transactionSupport;
	private DbDao dbDao;

	@Override
	public void setUp() throws SetupException {
		instanceVersionService = CDI.instantiateBean(InstanceVersionService.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		transactionSupport = CDI.instantiateBean(TransactionSupport.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());

		dbDao = CDI.instantiateBean(DbDao.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		TimeTracker tracker = TimeTracker.createAndStart();

		LOGGER.info("Begin adding missing published template versions");

		List<String> templateInstanceIds = dbDao.fetch(
				"select correspondingInstance from TemplateEntity where correspondingInstance is not null",
				Collections.emptyList());

		for (String templateInstanceId : templateInstanceIds) {
			transactionSupport.invokeInNewTx(() -> this.migrate(templateInstanceId));
		}

		LOGGER.info("Finished adding missing template properties in {} ms.", tracker.stop());
	}

	private void migrate(String templateInstanceId) {
		Optional<Instance> latestActiveVersion = getLatestActiveVersion(templateInstanceId);

		if (latestActiveVersion.isPresent()) {
			Instance activeTemplate = latestActiveVersion.get();

			List<Pair<String, Object>> params = new ArrayList<>(3);
			params.add(new Pair<>("templateInstanceId", templateInstanceId));
			params.add(new Pair<>("instanceVersion", activeTemplate.getString(DefaultProperties.VERSION)));
			params.add(new Pair<>("title", activeTemplate.getString(DefaultProperties.TITLE)));

			dbDao.executeUpdate(TemplateEntity.QUERY_UPDATE_PUBLISHED_INSTANCE_VERSION_AND_TITLE_KEY, params);
			LOGGER.info("Updated published template for instance {}", templateInstanceId);
		} else {
			LOGGER.error("No version with 'ACTIVE' state found for instance {}", templateInstanceId);
		}
	}

	private Optional<Instance> getLatestActiveVersion(String instanceId) {
		VersionsResponse versions = instanceVersionService.getInstanceVersions(instanceId, 0, -1);

		return versions
				.getResults()
					.stream()
					.filter(instance -> "ACTIVE".equals(instance.getAsString(DefaultProperties.STATUS)))
					.findFirst();
	}

	@Override
	public String getConfirmationMessage() {
		return "Add missing published template versions";
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// Not used
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}
}